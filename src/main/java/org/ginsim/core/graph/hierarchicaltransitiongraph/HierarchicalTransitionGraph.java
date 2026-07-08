package org.ginsim.core.graph.hierarchicaltransitiongraph;

import java.util.List;

import org.colomoto.biolqm.NodeInfo;
import org.ginsim.core.graph.GraphAssociation;
import org.ginsim.core.graph.dynamicgraph.TransitionGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.service.tool.modelreduction.ReductionConfig;
/**
 * Hierarchical Transition Graphs provide a compact representation of the dynamical behaviour.
 * Each node in this graph represent a set of states with a similar fate in the STG (cycle, linear path...)
 *
 * @author Duncan Berenguier
 */
public interface HierarchicalTransitionGraph extends TransitionGraph<HierarchicalNode, DecisionOnEdge>, GraphAssociation<RegulatoryGraph, RegulatoryNode, RegulatoryMultiEdge>{

	/**
	 * add an edge between source and target
	 * @param source a HierarchicalNode
	 * @param target a HierarchicalNode
	 * @return the new edge
	 */
	public DecisionOnEdge addEdge(HierarchicalNode source, HierarchicalNode target);
	
	/**
	 * 
	 * Return the Node with the given state
	 * 
	 * @param state
	 * @return the Node with the given state
	 */
	public HierarchicalNode getNodeForState(byte[] state);
	
	
	/**
	 * Set a list of NodeInfo representing the order of node as defined by the model
	 * 
	 * @param node_order the list of NodeInfo representing the order of node as defined by the model
	 */
	public void setNodeOrder( List<NodeInfo> node_order);
	
	
	/**
	 * Return the node order as a List of NodeInfo
	 * 
	 * @return the node order as a List of NodeInfo
	 */
	public List<NodeInfo> getNodeOrder();


	/**
	 * @return an array indicating for each node in the nodeOrder their count of childs. (ie. their max value)
	 */
	public byte[] getChildsCount();
	
	
	/**
	 * Set the array indicating for each node in the nodeOrder their count of childs. (ie. their max value)
	 * 
	 * @param cc the array indicating for each node in the nodeOrder their count of childs. (ie. their max value)
	 */
	
	public void setChildsCount(byte[] cc);
	
	/**
	 * Set the mode
	 * 
	 * @param compacted
	 */
	public void setMode(boolean compacted);
	
	
	/**
	 * Return <b>true</b> if the transients are compacted into component by their atteignability of attractors.
	 * 
	 * @return <b>true</b> if the transients are compacted into component by their atteignability of attractors.
	 */
	public boolean areTransientCompacted();

    StatesSet createStateSet();

	public void setReduction(ReductionConfig reduction);

	public ReductionConfig getReduction();
	
	/**
	 * Gets the range of values of a extra node on a given (set of) state(s).
	 * 
	 * @return "*" if range is 0-max, or "min-max"
	 */
	public String getExtraValueInterval(byte[] state, int idx);
}
