package org.ginsim.core.graph.regulatorygraph;

import java.util.*;

import org.colomoto.biolqm.ConnectivityMatrix;
import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.ModelLayout;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.StatefulLogicalModel;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.PathSearcher;
import org.colomoto.mddlib.VariableEffect;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameter;
import org.ginsim.core.graph.view.NodeAttributesReader;

/**
 * Create a RegulatoryGraph based on a LogicalModel.
 * 
 * @author Aurelien Naldi
 */
public class LogicalModel2RegulatoryGraph {

	private final MDDManager ddmanager;
	private final ConnectivityMatrix matrix;
	private final RegulatoryGraph lrg;

	private final List<NodeInfo> coreNodes;
	private final Map<NodeInfo, RegulatoryNode> node2node;

	/**
	 * Creates a new Regulatory graph, based on an existing logical model
	 * @param model the (possibly stateful) logical model
	 * @return the derived regulatory graph
	 */
    public static RegulatoryGraph importModel(LogicalModel model) {
        return new LogicalModel2RegulatoryGraph(model).getRegulatoryGraph();
    }

    /**
	 * Creates a new Regulatory graph, based on an existing logical model
	 * @param model the (possibly stateful) logical model
     * @param to_remove the nodes to remove
	 * @return the derived regulatory graph
     */
    public static RegulatoryGraph importModel(LogicalModel model, Collection<NodeInfo> to_remove) {
        return new LogicalModel2RegulatoryGraph(model, to_remove).getRegulatoryGraph();
    }
    
    
    
    private LogicalModel2RegulatoryGraph( LogicalModel model) {
        this(model, new ArrayList<>());
    }

    private LogicalModel2RegulatoryGraph( LogicalModel model, Collection<NodeInfo> to_remove) {
		this.ddmanager = model.getMDDManager();
		this.lrg = RegulatoryGraphFactory.getInstance().create();
		this.matrix = new ConnectivityMatrix(model);

		// mapping
		coreNodes = model.getComponents();
		node2node = new HashMap<NodeInfo, RegulatoryNode>();
		
		// add all components
		addNodes(coreNodes, null);
		addNodes(model.getExtraComponents(), to_remove);
		
		// import the logical functions
		addRegulators(model.getLogicalFunctions(), coreNodes, false);
		addRegulators(model.getExtraLogicalFunctions(), model.getExtraComponents(), true);
		
		// add initial state
		if(model instanceof StatefulLogicalModel){
			lrg.setStates(((StatefulLogicalModel)model).getInitialStates());
			lrg.setOracles(((StatefulLogicalModel)model).getOracles());
		}

		// add layout information
		if (model.hasLayout()) {
			ModelLayout layout = model.getLayout();
			NodeAttributesReader nreader = lrg.getNodeAttributeReader();
			List<RegulatoryNode> nodes = lrg.getNodeOrder();
			for (NodeInfo ni: model.getComponents()) {
				ModelLayout.LayoutInfo li = layout.getInfo(ni);
				if (li == null) {
					continue;
				}
				RegulatoryNode node = lrg.getNodeByName(ni.getNodeID());
				if (node == null) {
					continue;
				}
				try {

					nreader.setNode( node);
					nreader.setPos(li.x, li.y);
					// TODO: also import size information
				} catch (Exception e) {

				}
			}
		}

		// annotations are matched by node id as the graph nodes have their own NodeInfo
		Map<String, NodeInfo> name2node = new HashMap<>();
		for (RegulatoryNode node: lrg.getNodes()) {
			name2node.put(node.getId(), node.getNodeInfo());
		}
		RegulatoryGraphImpl.importAnnotations(lrg.getAnnotator(), model.getAnnotator().writeAnnotationsInJSON(), name2node);
	}
	
	private RegulatoryGraph getRegulatoryGraph() {
		return lrg;
	}
	
	private void addNodes(List<NodeInfo> nodes, Collection<NodeInfo> to_remove) {
    	for (NodeInfo ni: nodes) {
            if (to_remove != null && to_remove.contains(ni)) {
                continue;
            }
    		RegulatoryNode node = lrg.addNode( );
    		node.setId(ni.getNodeID(), lrg);
    		node.setName(ni.getName(), lrg);
    		node.setMaxValue(ni.getMax(), lrg);
    		node2node.put(ni, node);
    	}
	}

	private void addRegulators(int[] functions, List<NodeInfo> nodes, boolean extra) {
		PathSearcher searcher = new PathSearcher(ddmanager);
		for (int idx=0 ; idx<functions.length ; idx++) {
			NodeInfo target = nodes.get(idx);
			RegulatoryNode regNode = node2node.get(target);

            if (regNode == null) {
                continue;
            }

			if (target.isInput()) {
				regNode.setInput(true, lrg);
				continue;
			}
			
			int[] regulators = matrix.getRegulators(idx, extra);
			VariableEffect allEffects[][] = matrix.getRegulatorEffects(idx, extra);
			int[][] t_values = new int[regulators.length][4];
			RegulatoryMultiEdge[] t_me = new RegulatoryMultiEdge[t_values.length];

			for (int regIdx=0 ; regIdx<regulators.length ; regIdx++) {
				int reg = regulators[regIdx];
				VariableEffect[] effects = allEffects[regIdx];
				NodeInfo regulator = coreNodes.get(reg);
				
				byte idxTh=1;
				RegulatoryMultiEdge me = null;
				for (VariableEffect curEffect: effects) {
					if (curEffect != null && curEffect != VariableEffect.NONE) {
						RegulatoryEdgeSign sign = getSign(curEffect);
						me = lrg.addEdge(node2node.get(regulator), node2node.get(target), sign);
						me.setMin(0, idxTh, lrg);
						break;
					}
					idxTh++;
				}
				
				if (me == null) {
					throw new RuntimeException("No effect found for " + regulator + " on " + target);
				}
				
				t_values[regIdx][0] = reg;
				t_me[regIdx] = me;
				
				for ( ; idxTh < effects.length ; idxTh++) {
					VariableEffect curEffect = effects[idxTh];
					if (curEffect != null && curEffect != VariableEffect.NONE) {
						RegulatoryEdgeSign sign = getSign(curEffect);
						me.addEdge(sign, idxTh+1, lrg);
					}
				}
				
			}
			
			searcher.setNode(functions[idx]);
			browse(regNode, t_me, t_values, searcher);
			
		}
	}
	
	/**
	 * Assemble logical parameters for a target node
	 */
	private void browse(RegulatoryNode targetNode, RegulatoryMultiEdge[] t_me, int[][] t_values, PathSearcher searcher) {
		
		List<LogicalParameter> paramList = new ArrayList<LogicalParameter>();
		
		int[] path = searcher.getPath();
		for (int leaf: searcher) {
			if (leaf == 0) {
				continue;
			}

			// transform constraints on values to constraints on edges
			for (int i=0 ; i<t_values.length ; i++) {
				int nb = t_values[i][0];
				int begin = path[nb];
				RegulatoryMultiEdge me = t_me[i];
				nb = me.getEdgeCount();
				
				if (begin == -1) {
					// all values are allowed
					t_values[i][1] = -1;
					t_values[i][2] = nb-1;
				} else {
					// find the first edge
					if (begin == 0) {
						// only the first edge
						t_values[i][1] = -1;
						t_values[i][2] = -1;
					} else {
						// lookup the start
						boolean found = false;
						for (int j=0 ; j<nb ; j++) {
							if (me.getMin(j) == begin) {
								t_values[i][1] = j;
								t_values[i][2] = j;
								found = true;
								break;
							}
						}
						
						if (!found) {
							continue;
						}
					}
				}
			}
			
			// prepare to iterate through logical parameters
			for (int i=0 ; i<t_values.length ; i++) {
				t_values[i][3] = t_values[i][1];
			}
			
			while (true) {
				List<RegulatoryEdge> l = new ArrayList<RegulatoryEdge>();
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
				
				paramList.add(new LogicalParameter(l, leaf));

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
		
		targetNode.getV_logicalParameters().setManualParameters(paramList);
	}
	
	private RegulatoryEdgeSign getSign(VariableEffect[] effects) {
		VariableEffect effect = VariableEffect.NONE;
		for (VariableEffect ef: effects) {
			effect = effect.combine(ef);
		}
		return getSign(effect);
	}
	private RegulatoryEdgeSign getSign(VariableEffect effect) {
		switch (effect) {
		case POSITIVE:
			return RegulatoryEdgeSign.POSITIVE;
		case NEGATIVE:
			return RegulatoryEdgeSign.NEGATIVE;
		case DUAL:
			return RegulatoryEdgeSign.DUAL;
		}
		return RegulatoryEdgeSign.UNKNOWN;
	}
}
