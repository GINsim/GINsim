package fr.univmrs.tagc.GINsim.regulatoryGraph.modelModifier;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fr.univmrs.tagc.GINsim.annotation.Annotation;
import fr.univmrs.tagc.GINsim.data.GsDirectedEdge;
import fr.univmrs.tagc.GINsim.export.regulatoryGraph.LogicalFunctionBrowser;
import fr.univmrs.tagc.GINsim.graph.GsEdgeAttributesReader;
import fr.univmrs.tagc.GINsim.graph.GsGraphManager;
import fr.univmrs.tagc.GINsim.graph.GsVertexAttributesReader;
import fr.univmrs.tagc.GINsim.reg2dyn.GsReg2dynPriorityClass;
import fr.univmrs.tagc.GINsim.reg2dyn.GsSimulationParameterList;
import fr.univmrs.tagc.GINsim.reg2dyn.GsSimulationParameters;
import fr.univmrs.tagc.GINsim.reg2dyn.GsSimulationParametersManager;
import fr.univmrs.tagc.GINsim.reg2dyn.PriorityClassDefinition;
import fr.univmrs.tagc.GINsim.reg2dyn.PriorityClassManager;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsLogicalParameter;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsMutantListManager;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryEdge;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraph;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryMultiEdge;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryVertex;
import fr.univmrs.tagc.GINsim.regulatoryGraph.OmddNode;
import fr.univmrs.tagc.GINsim.regulatoryGraph.initialState.GsInitialState;
import fr.univmrs.tagc.GINsim.regulatoryGraph.initialState.GsInitialStateList;
import fr.univmrs.tagc.GINsim.regulatoryGraph.initialState.GsInitialStateManager;
import fr.univmrs.tagc.GINsim.regulatoryGraph.initialState.InitialStateList;
import fr.univmrs.tagc.GINsim.regulatoryGraph.mutant.GsRegulatoryMutantDef;
import fr.univmrs.tagc.GINsim.regulatoryGraph.mutant.GsRegulatoryMutants;
import fr.univmrs.tagc.common.GsException;

class RemovedInfo {
	GsRegulatoryVertex vertex;
	int pos;
	List<GsDirectedEdge> targets;
	public RemovedInfo(GsRegulatoryVertex vertex, int pos, List<GsDirectedEdge> targets) {
		super();
		this.vertex = vertex;
		this.pos = pos;
		this.targets = targets;
	}
}

/**
 * Build a simplified model, based on a complete one, by removing some nodes.
 * 
 * The first step is to build new MDD for the targets of the removed nodes.
 * If this succeeded (no circuit was removed...), a new regulatory graph is created
 * and all non-removed nodes are copied into it, as well as all remaining interactions.
 * Then the logical parameters of the unaffected nodes are restored.
 * For the affected nodes, some work is required, using the newly built MDD for their logical function:
 * <ul>
 *   <li>new edges are added if needed (coming from the regulators of their deleted regulators)</li> 
 *   <li>new logical parameters are extracted from the MDD</li>
 * </ul>
 */
public class ModelSimplifier extends Thread implements Runnable {

	GsGraphManager manager;
	ModelSimplifierConfigDialog dialog;
	int[] t_remove = null;

	GsRegulatoryGraph graph;
	List<GsRegulatoryVertex> oldNodeOrder;
	GsRegulatoryGraph simplifiedGraph;

	Map<GsRegulatoryVertex,boolean[]> m_edges = new HashMap<GsRegulatoryVertex, boolean[]>();
	Map<Object, Object> copyMap = new HashMap<Object, Object>();
	Map<GsRegulatoryVertex, List<GsRegulatoryVertex>> m_removed;
	
	Map<GsRegulatoryVertex, OmddNode> m_affected = new HashMap<GsRegulatoryVertex, OmddNode>();
	String s_comment = "";
	List<GsRegulatoryVertex> l_removed = new ArrayList<GsRegulatoryVertex>();

	TargetEdgesIterator it_targets;
	
	boolean strict;
	ParameterGenerator pgen;

	public ModelSimplifier(GsRegulatoryGraph graph, ModelSimplifierConfig config, ModelSimplifierConfigDialog dialog, boolean start) {
		this.graph = graph;
		this.oldNodeOrder = graph.getNodeOrder();
		this.dialog = dialog;
		this.m_removed = new HashMap<GsRegulatoryVertex, List<GsRegulatoryVertex>>(config.m_removed);
		this.it_targets = new TargetEdgesIterator(m_removed);
		this.strict = config.strict;
		manager = graph.getGraphManager();
		
		if (start) {
		    start();
		}
	}
	
	/**
	 * Run the reduction method.
	 */
	@Override
    public void run() {
    	// prepare the list of removal requests
		List<RemovedInfo> l_todo = new ArrayList<RemovedInfo>();
		for (GsRegulatoryVertex vertex: m_removed.keySet()) {
			int index = graph.getNodeOrder().indexOf(vertex);
			RemovedInfo ri = new RemovedInfo(vertex, index, manager.getOutgoingEdges(vertex));
			l_todo.add(ri);
		}
		
		
		// perform the actual reduction
		l_todo = remove_all(l_todo);

		// the "main" part is done, did it finish or fail ?
		if (l_todo.size() > 0) {
			if (dialog != null) {
				if (!dialog.showPartialReduction(l_todo)) {
					return;
				}
				
				for (RemovedInfo ri: l_todo) {
					m_removed.remove(ri.vertex);
				}
				System.out.println("Partial reduction result...");
			} else {
				// it failed, trigger an error message
				StringBuffer sb = new StringBuffer("Reduction failed.\n  Removed: ");
				for (GsRegulatoryVertex v: l_removed) {
					sb.append(" "+v);
				}
				sb.append("\n  Failed: ");
				for (RemovedInfo ri: l_todo) {
					sb.append(" "+ri.vertex);
				}
				throw new RuntimeException(sb.toString());
			}
		}

		// go ahead and extract the result
        GsRegulatoryGraph simplifiedGraph = extractReducedGraph();
        
        if (dialog != null) {
            dialog.endSimu(simplifiedGraph, null);
        }
    }
    
    private List<RemovedInfo> remove_all(List<RemovedInfo> l_todo) {
		// first do the "real" simplification work
		int todoSize = l_todo.size();
		int oldSize = todoSize + 1;
		while (todoSize > 0 && todoSize < oldSize) {
			oldSize = todoSize;
			l_todo = remove_batch(l_todo);
			todoSize = l_todo.size();
		}
		return l_todo;
    }
	
    /**
     * Go through a list of nodes to remove and try to remove all of them.
     * <p>
     * It may fail on some removals, in which case it will go on with the others and add them to the list of failed.
     * 
     * @param l_todo
     * @return the list of failed removals.
     */
    private List<RemovedInfo> remove_batch(List<RemovedInfo> l_todo) {
		System.out.println("batch of removal...");
    	List<RemovedInfo> l_failed = new ArrayList<RemovedInfo>();
    	
		for (RemovedInfo ri: l_todo) {
			GsRegulatoryVertex vertex = ri.vertex;
			List<GsRegulatoryVertex> targets = new ArrayList<GsRegulatoryVertex>();
			OmddNode deleted = m_affected.get(vertex);
			if (deleted == null) {
				deleted = vertex.getTreeParameters(graph);
			}
			try {
				if (strict) {
					// check that the node is not self-regulated
					checkNoSelfReg(deleted, ri.pos);
				}
				s_comment += ", "+vertex.getId();
			
				// mark all its targets as affected
				it_targets.setOutgoingList(ri.targets);
				while (it_targets.hasNext()) {
					GsRegulatoryVertex target = (GsRegulatoryVertex)it_targets.next();
					if (!target.equals(vertex)) {
						targets.add(target);
						OmddNode targetNode = m_affected.get(target);
						if (targetNode == null) {
							targetNode = target.getTreeParameters(graph);
						}
						m_affected.put(target, remove(targetNode, deleted, ri.pos).reduce());
					}
				}
				m_removed.put(ri.vertex, new ArrayList<GsRegulatoryVertex>(targets));
				l_removed.add(vertex);
			} catch (GsException e) {
				// this removal failed, remember that we may get a second chance
				l_failed.add(ri);
			}
		}
    	return l_failed;
    }

    /**
     * After the reduction, build a new regulatory graph with the result.
     * 
     * @return the reduced graph obtained after reduction
     */
    private GsRegulatoryGraph extractReducedGraph() {
		// create the new regulatory graph
		simplifiedGraph = new GsRegulatoryGraph();
		Annotation note = simplifiedGraph.getAnnotation();
		note.copyFrom(graph.getAnnotation());
		if (s_comment.length() > 2) {
			note.setComment("Model Generated by GINsim on "+
					DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date()) +
					", by removing the following nodes: "+s_comment.substring(2)+
					"\n\n"+note.getComment());
		}
		
		GsGraphManager simplifiedManager = simplifiedGraph.getGraphManager();
		List<GsRegulatoryVertex> simplified_nodeOrder = simplifiedGraph.getNodeOrder();
		
		// Create all the nodes of the new model
		GsVertexAttributesReader vreader = manager.getVertexAttributesReader();
		GsVertexAttributesReader simplified_vreader = simplifiedManager.getVertexAttributesReader();
		for (GsRegulatoryVertex vertex: (List<GsRegulatoryVertex>)graph.getNodeOrder()) {
			if (!m_removed.containsKey(vertex)) {
				GsRegulatoryVertex clone = (GsRegulatoryVertex)vertex.clone();
				simplifiedManager.addVertex(clone);
				vreader.setVertex(vertex);
				simplified_vreader.setVertex(clone);
				simplified_vreader.copyFrom(vreader);
				copyMap.put(vertex, clone);
				simplified_nodeOrder.add(clone);
			}
		}
		
		// copy all unaffected edges
		GsEdgeAttributesReader ereader = manager.getEdgeAttributesReader();
		GsEdgeAttributesReader simplified_ereader = simplifiedManager.getEdgeAttributesReader();
		Iterator<GsDirectedEdge>it = manager.getEdgeIterator();
		while (it.hasNext()) {
			GsRegulatoryMultiEdge me = (GsRegulatoryMultiEdge)((GsDirectedEdge)it.next()).getUserObject();
			GsRegulatoryVertex src = (GsRegulatoryVertex)copyMap.get(me.getSource());
			GsRegulatoryVertex target = (GsRegulatoryVertex)copyMap.get(me.getTarget());
			if (src != null && target != null) {
				GsRegulatoryMultiEdge me_clone = new GsRegulatoryMultiEdge(src, target);
				me_clone.copyFrom(me);
				Object new_me = simplifiedManager.addEdge(src, target, me_clone);
				copyMap.put(me, me_clone);
				ereader.setEdge(me);
				simplified_ereader.setEdge(new_me);
				simplified_ereader.copyFrom(ereader);
			}
		}

		// build a mapping between new nodes and old position
		Map<GsRegulatoryVertex, Integer> m_orderPos = new HashMap<GsRegulatoryVertex, Integer>();
		Iterator<GsRegulatoryVertex> it_oldOrder = oldNodeOrder.iterator();
		int pos = -1;
		for (GsRegulatoryVertex vertex: simplified_nodeOrder) {;
			String id = vertex.getId();
			while (it_oldOrder.hasNext()) {
				pos++;
				GsRegulatoryVertex oldVertex = it_oldOrder.next();
				if (id.equals(oldVertex.getId())) {
					m_orderPos.put(vertex, new Integer(pos));
					break;
				}
			}
		}
		// create the parameter generator with it
		pgen = new ParameterGenerator(oldNodeOrder, m_orderPos);

		// copy parameters/logical functions on the unaffected nodes
		for (GsRegulatoryVertex vertex: oldNodeOrder) {
			GsRegulatoryVertex clone = (GsRegulatoryVertex)copyMap.get(vertex);
			if (m_removed.containsKey(vertex)) {
				continue;
			}
			if (!m_affected.containsKey(vertex)) {
				vertex.cleanupInteractionForNewGraph(copyMap);
				continue;
			}
			
			// this node needs new parameters
			OmddNode newNode = m_affected.get(vertex);

			// make sure that the needed edges target the affected node
			m_edges.clear();
			extractEdgesFromNode(newNode);
			GsRegulatoryVertex target = (GsRegulatoryVertex)copyMap.get(vertex);
			for (Entry<GsRegulatoryVertex,boolean[]> e: m_edges.entrySet()) {
				GsRegulatoryVertex src = (GsRegulatoryVertex)copyMap.get(e.getKey());
				GsDirectedEdge de = (GsDirectedEdge)simplifiedManager.getEdge(src, target);
				GsRegulatoryMultiEdge new_me;
				if (de == null) {
					new_me = new GsRegulatoryMultiEdge(src, target);
					simplifiedManager.addEdge(src, target, new_me);
				} else {
					new_me = (GsRegulatoryMultiEdge)de.getUserObject();
				}
				boolean[] t_required = e.getValue();
				new_me.copyFrom(t_required);
			}
			// rebuild the parameters
			m_edges.clear();
			List<GsDirectedEdge> edges = simplifiedManager.getIncomingEdges(clone);
			for (GsDirectedEdge e: edges) {
				GsRegulatoryVertex src = (GsRegulatoryVertex)e.getSourceVertex();
				
				// FIXME: not sure what this should be! (used to be a integer[])
				boolean[] t_val = {false, true};
				m_edges.put(src, t_val);
			}
			pgen.browse(edges, clone, newNode);
		}
		
		// get as much of the associated data as possible
		Map m_alldata = new HashMap();
		// mutants: only copy mutants that don't affect removed nodes
		GsRegulatoryMutants mutants = (GsRegulatoryMutants)graph.getObject(GsMutantListManager.key, false);
		if (mutants != null && mutants.getNbElements(null) > 0) {
			GsRegulatoryMutants newMutants = (GsRegulatoryMutants)simplifiedGraph.getObject(GsMutantListManager.key, true);
			GsRegulatoryMutantDef mutant, newMutant;
			int mutantPos=0;
			for (int i=0 ; i<mutants.getNbElements(null) ; i++) {
				mutant = (GsRegulatoryMutantDef)mutants.getElement(null, i);
				mutantPos = newMutants.add();
				newMutant = (GsRegulatoryMutantDef)newMutants.getElement(null, mutantPos);
				newMutant.setName(mutant.getName());
				boolean ok = true;
				for (int j=0 ; j<mutant.getNbChanges() ; j++ ) {
					String id = mutant.getName(j);
					GsRegulatoryVertex vertex = null;
					for (GsRegulatoryVertex v: simplified_nodeOrder) {
						if (id.equals(v.getId())) {
							vertex = v;
							break;
						}
					}
					if (vertex == null) {
						ok = false;
						break;
					}
					newMutant.addChange(vertex, mutant.getMin(j), mutant.getMax(j));
					// TODO: transfer condition only if it does not involve removed nodes
					newMutant.setCondition(j, simplifiedGraph, mutant.getCondition(j));
				}
				if (!ok) {
					newMutants.remove(null, new int[] {mutantPos});
				} else {
                    m_alldata.put(mutant, newMutant);
				}
			}
		}
		
		// initial states
        GsInitialStateList linit = (GsInitialStateList)graph.getObject(GsInitialStateManager.key, false);
		if (linit != null && !linit.isEmpty()) {
			GsInitialStateList newLinit = (GsInitialStateList)simplifiedGraph.getObject(GsInitialStateManager.key, true);
            InitialStateList[] inits = {linit.getInitialStates(), linit.getInputConfigs()};
            InitialStateList[] newInits = {newLinit.getInitialStates(), newLinit.getInputConfigs()};

			for (int i=0 ; i<inits.length ; i++) {
                InitialStateList init = inits[i];
                InitialStateList newInit = newInits[i];
    			if (init != null && init.getNbElements(null) > 0) {
    				for (int j=0 ; j<init.getNbElements(null) ; j++) {
    					GsInitialState istate = (GsInitialState)init.getElement(null, j);
    					int epos = newInit.add();
    					GsInitialState newIstate = (GsInitialState)newInit.getElement(null, epos);
    					newIstate.setName(istate.getName());
    					m_alldata.put(istate, newIstate);
    					Map<GsRegulatoryVertex, List<Integer>> m_init = newIstate.getMap();
    					for (Entry<GsRegulatoryVertex, List<Integer>> e: istate.getMap().entrySet()) {
    						GsRegulatoryVertex o = (GsRegulatoryVertex)copyMap.get(e.getKey());
    						if (o != null) {
    							m_init.put( o, e.getValue());
    						}
    					}
    				}
    			}
			}
		}
		
		// priority classes definition and simulation parameters
		GsSimulationParameterList params = (GsSimulationParameterList)graph.getObject(GsSimulationParametersManager.key, false);
		if (params != null) {
			PriorityClassManager pcman = params.pcmanager;
			GsSimulationParameterList new_params = (GsSimulationParameterList)simplifiedGraph.getObject(GsSimulationParametersManager.key, true);
			PriorityClassManager new_pcman = new_params.pcmanager;
			for (int i=2 ; i<pcman.getNbElements(null) ; i++) {
				PriorityClassDefinition pcdef = (PriorityClassDefinition)pcman.getElement(null, i);
				int index = new_pcman.add();
				PriorityClassDefinition new_pcdef = (PriorityClassDefinition)new_pcman.getElement(null, index);
				new_pcdef.setName(pcdef.getName());
				m_alldata.put(pcdef, new_pcdef);
				Map<GsReg2dynPriorityClass, GsReg2dynPriorityClass> m_pclass = new HashMap<GsReg2dynPriorityClass, GsReg2dynPriorityClass>();
				// copy all priority classes
				for (int j=0 ; j<pcdef.getNbElements(null) ; j++) {
					GsReg2dynPriorityClass pc = (GsReg2dynPriorityClass)pcdef.getElement(null, j);
					if (j>0) {
						new_pcdef.add();
					}
					GsReg2dynPriorityClass new_pc = (GsReg2dynPriorityClass)new_pcdef.getElement(null, j);
					new_pc.setName(pc.getName());
					new_pc.rank = pc.rank;
					new_pc.setMode(pc.getMode());
					m_pclass.put(pc, new_pc);
				}
				
				// properly place nodes
				for (Entry<?,?> e: pcdef.m_elt.entrySet()) {
					GsRegulatoryVertex vertex = (GsRegulatoryVertex)copyMap.get(e.getKey());
					if (vertex != null) {
						new_pcdef.m_elt.put(vertex,	m_pclass.get(e.getValue()));
					}
				}
			}
			int[] t_index = {0};
			new_pcman.remove(null, t_index);
			
			// simulation parameters
			for (int i=0 ; i<params.getNbElements() ; i++) {
			    GsSimulationParameters param = (GsSimulationParameters)params.getElement(null, i);
			    int index = new_params.add();
			    GsSimulationParameters new_param = (GsSimulationParameters)new_params.getElement(null, index);
			    m_alldata.put("", new_pcman);
			    param.copy_to(new_param, m_alldata);
			}
		}
		return simplifiedGraph;
	}
	
	/**
	 * extract the list of required edges for a given logical function.
	 * @param node
	 */
	private void extractEdgesFromNode(OmddNode node) {
		if (node.next == null) {
			return;
		}
		GsRegulatoryVertex vertex = (GsRegulatoryVertex)oldNodeOrder.get(node.level);
		boolean[] t_threshold = (boolean[])m_edges.get(vertex);
		if (t_threshold == null) {
			t_threshold = new boolean[vertex.getMaxValue()+1];
			for (int i=0 ; i<t_threshold.length ; i++) {
				t_threshold[i] = false;
			}
			m_edges.put(vertex, t_threshold);
		}

		OmddNode child = null;
		for (int i=0 ; i<node.next.length ; i++) {
			if (child != node.next[i]) {
				if (child != null) {
					t_threshold[i] = true;
				}
				child = node.next[i];
				extractEdgesFromNode(node.next[i]);
			}
		}
	}

	/* *************************************************************
	 *  
	 *  The real algo is here
	 *  
	 *  Deleting a node means removing it (by taking into account its logical
	 *  function) from all of its targets
	 *  
	 ***************************************************************/

	/**
	 * Preliminary check: a node should not be self-regulated: check it
	 */
	private void checkNoSelfReg(OmddNode node, int level) throws GsException {
		if (node.next == null || node.level > level) {
			return;
		}
		if (node.level == level) {
			throw new GsException(GsException.GRAVITY_ERROR, "self regulated node");
		}
		for (int i=0 ; i<node.next.length ; i++) {
			checkNoSelfReg(node.next[i], level);
		}
	}
	/**
	 * Remove <code>regulator</code> from its target <code>node</code>.
	 * This is the first part of the algo: we have not yet found the 
	 * regulator in the logical function.
	 * It will be called recursively until we find it (or go too far)
	 * 
	 * @param node
	 * @param regulator
	 * @param level
	 * @return
	 */
	public OmddNode remove(OmddNode node, OmddNode regulator, int level) throws GsException {
		if (node.next == null || node.level > level) {
			return node;
		}
		if (node.level == level) {
			if (regulator.next == null) {
				return node.next[regulator.value];
			}
			if (regulator.level == level) {
				throw new GsException(GsException.GRAVITY_ERROR, 
						"Can not continue the simplification: a circuit would get lost");
			}
			return remove(node.next, regulator);
		}
		
		OmddNode ret = new OmddNode();
		if (regulator.next == null || regulator.level > node.level) {
			ret.level = node.level;
			ret.next = new OmddNode[node.next.length];
			for (int i=0 ; i<ret.next.length ; i++) {
				ret.next[i] = remove(node.next[i], regulator, level);
			}
		} else if (node.level > regulator.level) {
			ret.level = regulator.level;
			ret.next = new OmddNode[regulator.next.length];
			for (int i=0 ; i<ret.next.length ; i++) {
				ret.next[i] = remove(node, regulator.next[i], level);
			}
		} else {
			ret.level = node.level;
			ret.next = new OmddNode[node.next.length];
			for (int i=0 ; i<ret.next.length ; i++) {
				ret.next[i] = remove(node.next[i], regulator.next[i], level);
			}
		}
		return ret;
	}

	/**
	 * Remove <code>regulator</code> from its target <code>node</code>.
	 * This is the second part of the algo: we have found the regulator 
	 * in the logical function.
	 * We must thus follow all branches corresponding to its possible values,
	 * until we can take the final decision.
	 * 
	 * @param t_ori
	 * @param regulator
	 * @return
	 */
	public OmddNode remove(OmddNode[] t_ori, OmddNode regulator) {
		if (regulator.next == null) {
			return t_ori[regulator.value];
		}
		// first, lookup for the best next step
		int best = regulator.level;
		int index = -1;
		for (int i=0 ; i<t_ori.length ; i++) {
			OmddNode node = t_ori[i];
			if (node.next != null && node.level <= best) { 
				// also update when equal to avoid stupid optimisations...
				best = node.level;
				index = i;
			}
		}
		
		OmddNode ret = new OmddNode();
		ret.level = best;
		if (index == -1) {
			ret.next = new OmddNode[regulator.next.length];
			for (int i=0 ; i<ret.next.length ; i++) {
				ret.next[i] = remove(t_ori, regulator.next[i]);
			}
		} else {
			ret.next = new OmddNode[t_ori[index].next.length];
			for (int i=0 ; i<ret.next.length ; i++) {
				OmddNode[] t_recur = new OmddNode[t_ori.length];
				for (int j=0 ; j<t_recur.length ; j++) {
					OmddNode node = t_ori[j];
					if (node.next == null || node.level > best) {
						t_recur[j] = node;
					} else {
						t_recur[j] = node.next[i];
					}
				}
				if (regulator.level == best) {
					ret.next[i] = remove(t_recur, regulator.next[i]);
				} else {
					ret.next[i] = remove(t_recur, regulator);
				}
			}
		}
		return ret;
	}
}


class TargetEdgesIterator implements Iterator<GsRegulatoryVertex> {

	LinkedList<GsRegulatoryVertex> queue = new LinkedList<GsRegulatoryVertex>();
	Set<GsRegulatoryVertex> m_visited = new HashSet<GsRegulatoryVertex>();
	Map<GsRegulatoryVertex, List<GsRegulatoryVertex>> m_removed;
	
	GsRegulatoryVertex next;
	
	public TargetEdgesIterator(Map<GsRegulatoryVertex, List<GsRegulatoryVertex>> m_removed) {
		this.m_removed = m_removed;
	}
	
	public boolean hasNext() {
		return next != null;
	}

	public GsRegulatoryVertex next() {
		GsRegulatoryVertex ret = next;
		
		// find the next.
		// it can be a "normal next target" if it was not removed
		// if it was removed, it may be one of the targets of the removed node
		next = null;
		while (queue.size() > 0) {
			GsRegulatoryVertex vertex = queue.removeFirst();
			if (m_visited.contains(vertex)) {
				// this node was checked already, skip it
				continue;
			}
			m_visited.add(vertex);
			List<GsRegulatoryVertex> targets = m_removed.get(vertex);
			if (targets == null) {
				// "clean" node: go for it!
				next = vertex;
				break;
			}
			
			// "dirty" node: enqueue its targets
			for (GsRegulatoryVertex v: targets) {
				queue.addLast(v);
			}
		}
		return ret;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public void setOutgoingList(List<GsDirectedEdge> outgoing) {
		m_visited.clear();
		queue.clear();
		for (GsDirectedEdge e: outgoing) {
			queue.addLast((GsRegulatoryVertex)e.getTargetVertex());
		}
		next();
	}
}

class ParameterGenerator extends LogicalFunctionBrowser {
	private List<GsLogicalParameter> paramList;
	private int[][] t_values;
	private GsRegulatoryMultiEdge[] t_me;
	private Map<GsRegulatoryVertex, Integer> m_orderPos;
	
	public ParameterGenerator(List<GsRegulatoryVertex> nodeOrder, Map<GsRegulatoryVertex, Integer> m_orderPos) {
		super(nodeOrder);
		this.m_orderPos = m_orderPos;
	}

	public void browse(List<GsDirectedEdge> edges, GsRegulatoryVertex targetVertex, OmddNode node) {
		this.paramList = new ArrayList<GsLogicalParameter>();
		t_values = new int[edges.size()][4];
		t_me = new GsRegulatoryMultiEdge[t_values.length];
		
		for (int i=0 ; i<t_values.length ; i++) {
			GsDirectedEdge de = edges.get(i);
			GsRegulatoryMultiEdge me = (GsRegulatoryMultiEdge)de.getUserObject();
			t_me[i] = me;
			t_values[i][0] = m_orderPos.get(me.getSource());
		}

		browse(node);
		targetVertex.getV_logicalParameters().setManualParameters(paramList);
	}
	
	protected void leafReached(OmddNode leaf) {
		if (leaf.value == 0) {
			return;
		}
		// transform constraints on values to constraints on edges
		for (int i=0 ; i<t_values.length ; i++) {
			int nb = t_values[i][0];
			int begin = path[nb][0];
			int end = path[nb][1];
			GsRegulatoryMultiEdge me = t_me[i];
			nb = me.getEdgeCount();
			
			if (begin == -1) {
				// all values are allowed
				t_values[i][1] = -1;
				t_values[i][2] = nb-1;
			} else {
				// find the first edge
				if (begin == 0) {
					// start before the first edge
					t_values[i][1] = -1;
				} else {
					// lookup the start
					for (int j=0 ; j<nb ; j++) {
						if (me.getMin(j) >= begin) {
							t_values[i][1] = j;
							break;
						}
					}
				}
				// find the last edge
				for (int j=t_values[i][1] ; j<nb ; j++) {
					if (j == -1) {
						if (end < me.getMin(0)) {
							t_values[i][2] = -1;
							break;
						}
						continue;
					}
					int max = me.getMax(j);
					if (max == -1 || end <= max) {
						t_values[i][2] = j;
						break;
					}
				}
			}
		}
		
		// prepare to iterate through logical parameters
		for (int i=0 ; i<t_values.length ; i++) {
			t_values[i][3] = t_values[i][1];
		}
		
		while (true) {
			List<GsRegulatoryEdge> l = new ArrayList<GsRegulatoryEdge>();
			int lastIndex = -1;
			for (int i=0 ; i<t_values.length ; i++) {
				if (t_values[i][3] != -1) {
					// add interaction to the vector
					l.add(t_me[i].getEdge(t_values[i][3]));
				}
				if (t_values[i][3] < t_values[i][2]) {
					lastIndex = i;
				}
			}
			
			paramList.add(new GsLogicalParameter(l, leaf.value));

			// stop if no free value was found
			if (lastIndex == -1) {
				break;
			}
			// go to next step
			t_values[lastIndex][3]++;
			for (int i=lastIndex+1 ; i<t_values.length ; i++) {
				t_values[i][3] = t_values[i][1];
			}
		}
	}
}
