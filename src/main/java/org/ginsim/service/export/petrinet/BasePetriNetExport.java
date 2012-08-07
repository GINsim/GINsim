package org.ginsim.service.export.petrinet;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.ginsim.common.application.LogManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.initialstate.InitialStatesIterator;
import org.ginsim.core.graph.regulatorygraph.mutant.Perturbation;
import org.ginsim.core.graph.regulatorygraph.omdd.OMDDNode;
import org.ginsim.service.tool.reg2dyn.priorityclass.PriorityClassDefinition;



/**
 * Export a regulatory graph to petri net (shared methods).
 *
 *<p> translating a regulatory graph to a petri net is done as follow:
 * <ul>
 *  <li>each node will be represented by two places, a negative one and a positive one.
 *      Markers in the positive place represent it's level. if it is not at it's maximum
 *      missing marker(s) will be in it's negative place: the number of markers in the petri net is constant</li>
 *  <li>each logical parameter will be represented by transition(s) with "test" arcs to
 *      non-modified places and "normal" arcs to the positive and negative place of the modified place.</li>
 * </ul>
 *
 * with some simplifications:
 * <ul>
 *  <li>work on the tree representation of logical parameters and use ranges instead of exact values as precondition of transitions</li>
 *  <li>"input" nodes are specials: no transition will affect them and their basal value will be used as initial markup</li>
 *  <li>autoregulation can trigger some cases where a transition can't be fired, these are not created</li>
 * </ul>
 *
 *<p>references:
 *<ul>
 *  <li>Simao, E., Remy, E., Thieffry, D. and Chaouiya, C.: Qualitative modelling of
 *      Regulated Metabolic Pathways: Application to the Tryptophan biosynthesis in E. Coli.</li>
 *  <li>Chaouiya, C., Remy, E. and Thieffry, D.: Petri Net Modelling of Biological Regulatory
 *      Networks</li>
 *</ul>
 */
public abstract class BasePetriNetExport {

	// TODO: make extension data available
	
	private final String extension;
	private final String filterDescr;
	
	public BasePetriNetExport(String extension, String filterDescr) {
		this.extension = extension;
		this.filterDescr = filterDescr;
	}
	
    /**
     * extract transitions from a tree view of logical parameters.
     *
     * @param v_result
     * @param node tree view of logical parameters on one node
     * @param nodeIndex index of the considered node (in the regulatory graph)
     * @param v_node all nodes
     * @param len number of nodes in the original graph
     */
    protected void browse(List v_result, OMDDNode node, int[][] t_priorities, int nodeIndex, List v_node, int len) {
        if (node.next == null) {
            TransitionData td = new TransitionData();
            td.value = node.value;
            td.maxValue = ((RegulatoryNode)v_node.get(nodeIndex)).getMaxValue();
            td.nodeIndex = nodeIndex;
            td.t_cst = null;
            if (t_priorities != null) {
				td.increasePriority = t_priorities[nodeIndex][0];
				td.decreasePriority = t_priorities[nodeIndex][1];
			}
            v_result.add(td);
        } else {
            int[][] t_cst = new int[len][3];
            for (int i=0 ; i<t_cst.length ; i++) {
                t_cst[i][0] = -1;
            }
            browse(v_result, t_cst, 0, node, t_priorities, nodeIndex, v_node);
        }
    }

    private void browse(List v_result, int[][] t_cst, int level, OMDDNode node, int[][] t_priorities, int nodeIndex, List v_node) {
        if (node.next == null) {
            TransitionData td = new TransitionData();
            td.value = node.value;
            td.maxValue = ((RegulatoryNode)v_node.get(nodeIndex)).getMaxValue();
            td.nodeIndex = nodeIndex;
            if (t_priorities != null) {
				td.increasePriority = t_priorities[nodeIndex][0];
				td.decreasePriority = t_priorities[nodeIndex][1];
			}
            td.t_cst = new int[t_cst.length][3];
            int ti = 0;
            for (int i=0 ; i<t_cst.length ; i++) {
                int index = t_cst[i][0];
                if (index == -1) {
                    break;
                }
                if (index == nodeIndex) {
                    td.minValue = t_cst[i][1];
                    td.maxValue = t_cst[i][2];
                } else {
                    td.t_cst[ti][0] = index;
                    td.t_cst[ti][1] = t_cst[i][1];
                    td.t_cst[ti][2] = ((RegulatoryNode)v_node.get(index)).getMaxValue() - t_cst[i][2];
                    if (td.t_cst[ti][1] > 0 || td.t_cst[ti][2] > 0) {
                        ti++;
                    }
                }
            }
            if (ti == 0) {
                td.t_cst = null;
            } else {
                td.t_cst[ti][0] = -1;
            }
            v_result.add(td);
            return;
        }

        // specify on which node constraints are added
        t_cst[level][0] = node.level;
        for (int i=0 ; i<node.next.length ; i++) {
            OMDDNode next = node.next[i];
            int j=i+1;
            while(j<node.next.length) {
                if (node.next[j] == next) {
                    j++;
                } else {
                    break;
                }
            }
            j--;
            t_cst[level][1] = i;
            t_cst[level][2] = j;
            browse(v_result, t_cst, level+1, next, t_priorities, nodeIndex, v_node);
            i = j;
        }
        // "forget" added constraints
        t_cst[level][0] = -1;
    }

	/**
	 * prepare the PN export:
	 *   - read/set initial markup
	 *   - build the set of transitions
	 *
	 * @param config
	 * @param t_transition
	 * @param t_tree
	 * @return the initial markup
	 */
    protected byte[][] prepareExport( PNConfig config, List[] t_transition, OMDDNode[] t_tree) {
    	List nodeOrder = config.graph.getNodeOrder();
		int len = nodeOrder.size();
		// get the selected initial state
		Iterator it_state = new InitialStatesIterator(nodeOrder, config);
		byte[] t_state = (byte[])it_state.next();

		// apply mutant
		Perturbation mutant = (Perturbation)config.store.getObject(0);
		if (mutant != null) {
			mutant.apply(t_tree, config.graph);
		}
		// use priority classes
		PriorityClassDefinition priorities = (PriorityClassDefinition)config.store.getObject(1);
		int[][] t_priorities = null;
		if (priorities != null) {
			t_priorities = new int[len][2];
			int[][] t_pclass = priorities.getPclass(nodeOrder);
			for (int i=0 ; i<t_pclass.length ; i++) {
				int[] t_class = t_pclass[i];
				int priority = t_class[0];
				for (int j=2 ; j<t_class.length ; j++) {
					int index = t_class[j++];
					LogManager.trace( "priority of "+priority+" for "+index+" ("+t_class[j]+")");
					switch (t_class[j]) {
						case 1:
							t_priorities[index][0] = priority;
							break;
						case -1:
							t_priorities[index][1] = priority;
							break;
						default:
							t_priorities[index][0] = priority;
							t_priorities[index][1] = priority;
					}
				}
			}
		}
		
		byte[][] t_markup = new byte[len][2];
        for (int i=0 ; i<len ; i++) {
            OMDDNode node = t_tree[i];
            RegulatoryNode vertex = (RegulatoryNode)nodeOrder.get(i);

//            if (manager.getIncomingEdges(vertex).size() == 0) {
//                // input node: no regulator, use basal value as initial markup ??
//                t_markup[i][0] = vertex.getBaseValue();
//                t_markup[i][1] = (byte)(vertex.getMaxValue() - vertex.getBaseValue());
//            } else {
                // normal node, initial markup = 0
                t_markup[i][0] = (byte)t_state[i];
                t_markup[i][1] = (byte)(vertex.getMaxValue()-t_state[i]);
                Vector v_transition = new Vector();
                t_transition[i] = v_transition;
                browse(v_transition, node, t_priorities, i, nodeOrder, len);
//            }
        }
		return t_markup;
    }
    
    abstract protected void doExport( PNConfig config, String filename) throws IOException;
    
    public void export(PNConfig config, String filename) throws IOException {
    	// TODO: share outputstream creation here
    	doExport(config, filename);
    }
}

class TransitionData {
    /** target value of this transition */
    public int value;
    
    /** index of the concerned node */
    public int nodeIndex;

    /** minvalue for the concerned node (0 unless an autoregulation is present) */
    public int minValue;
    /** maxvalue for the concerned node (same as node's maxvalue unless an autoregulation is present) */
    public int maxValue;
    
    /** priority when decreasing */
    public int decreasePriority = 0;
    /** priority when increasing */
    public int increasePriority = 0;
    
    /** constraints of this transition: each row express range constraint for one of the nodes
     * and contains 3 values:
     *  <ul>
     *      <li>index of the node (or -1 after the last constraint)</li>
     *      <li>bottom and top limit of the range (top limit is pre-processed: maxvalue - realLimit)</li>
     *  </ul>
     */
    public int[][] t_cst;
    
    public Perturbation mutant;
    public Perturbation getMutant() {
        return mutant;
    }
	public void setMutant(Perturbation mutant) {
		this.mutant = mutant;
	}
	
}
