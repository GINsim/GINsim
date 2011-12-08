package org.ginsim.service.tool.interactionanalysis;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.mutant.RegulatoryMutantDef;
import org.ginsim.core.graph.regulatorygraph.omdd.OMDDNode;
import org.ginsim.core.utils.log.LogManager;

/**
 * 
 * Search all the non functional interactions in the graph 'regGraph' and do some actions on them depending on the options (opt_*).
 * 
 */
public class InteractionAnalysisAlgo {
	
	private long before; //to know the time elapsed in the algorithm
	private int i_leafs;
	
	private List<PathItem> currentPath;
	private List<ReportItem> currentSource;
	
	protected static final byte FUNC_NON = 1;
	protected static final byte FUNC_POSITIVE = 2;
	protected static final byte FUNC_NEGATIVE = 3;
	protected static final byte FUNC_DUAL = 4;
	

	/**
	 * Search all the non functional interactions in the graph 'g' and do some actions on them depending on the options (opt_*).
	 * 
	 * For each vertices, scan the reduced Omdd representation of the node logical function to know which vertices are effective (<=>present). If an edge source vertex incoming on this vertex is not in the Omdd, then its non functional.
	 * 
	 * @param g the graph where to search the non functional interactions.
	 * @param mutant the mutant definition
	 * @param selectedNodes the set of selected nodes to run the analysis on.
	 * @return 
	 * 
	 */
	protected InteractionAnalysisAlgoResult run(RegulatoryGraph  regGraph, RegulatoryMutantDef mutant, List<RegulatoryNode> selectedNodes) {
		before = new Date().getTime();//measuring the time spend for this algorithm
		
		int total_level;		//The total number of node in a complete omdd tree. (products of levels of each interactor)
		byte [] leafs;			//The values of all the leafs for a complete omdd from all the node to 0, to all the node to max.
		int [] subtree_size;	//The size of all the complete subtree (tree of a child) of the current node
		Map<RegulatoryNode, Integer> node_in_subtree;    //The level of each node in the subtree regarding the nodeOrder

		int [] small_node_order_level; //The node order in the omdd.
		RegulatoryNode [] small_node_order_vertex; //The node order in the omdd.

		//Preparing the logical function for the right mutant
		LogManager.info("Getting the logical functions");
		OMDDNode[] t_tree =  regGraph.getAllTrees(true);
		if (mutant != null) {
			LogManager.info("Preparing the mutants");
			mutant.apply(t_tree, regGraph);
		}
		
		
		//Create a map associating to a regulatoryNode, its position in the nodeOrder as an Integer.
		Map<RegulatoryNode, Integer> node_to_position = new HashMap<RegulatoryNode, Integer>((int) (regGraph.getNodeCount()*1.5));
		int i = 0;
		for (RegulatoryNode node : regGraph.getNodeOrder()) {	//Build the map m
			node_to_position.put(node, Integer.valueOf(i++));
		}
		
		//Initializing the results of the algorithm
		
		LogManager.info("Preparing the report");
		InteractionAnalysisReport report =  new InteractionAnalysisReport();
		
		Map<RegulatoryMultiEdge, String> functionalityMap = new HashMap<RegulatoryMultiEdge, String>();
		InteractionAnalysisSelector selector = new InteractionAnalysisSelector();
		selector.setCache(functionalityMap);
		InteractionAnalysisColorizer colorizer = new InteractionAnalysisColorizer(selector, regGraph, functionalityMap);
		
		InteractionAnalysisAlgoResult algoResult = new InteractionAnalysisAlgoResult(colorizer, report);
		
		//Run the analysis on each regulatory node
		node_in_subtree = new HashMap<RegulatoryNode, Integer>();
		i = -1;
		for(RegulatoryNode target : regGraph.getNodeOrder()) { //  For each vertex target in the graph
		    i++;
			if (target.isInput() || (selectedNodes != null && !selectedNodes.contains(target))) { 	//skip the inputs or unselected nodes
			    continue;
			}
			LogManager.info("Computing "+target.getId());
			Collection<RegulatoryMultiEdge> l = regGraph.getIncomingEdges(target);											//  get the list l of incoming edges
			OMDDNode omdd = t_tree[i];
			
			total_level = 1;																//  Compute the total number of level in the omdd tree
			for (RegulatoryMultiEdge edge: l) {
				RegulatoryNode source = edge.getSource();
				total_level *= source.getMaxValue()+1;
			}
			leafs = new byte[total_level];
			i_leafs = 0;
						
			subtree_size = new int[l.size()+1];											//Compute the size of the subtrees
			subtree_size[0] = total_level;
			small_node_order_vertex = new RegulatoryNode[l.size()];
			small_node_order_level = new int[l.size()];
			int m = 0;
			for (RegulatoryNode source: regGraph.getNodeOrder()) {
				if (regGraph.getEdge(source, target) != null) {
					node_in_subtree.put(source, new Integer(m));
					subtree_size[m+1] = 1;
					small_node_order_vertex[m] = source;
					small_node_order_level[m] = node_to_position.get(source).intValue();
					for (int n = 0; n < m; n++) {
						subtree_size[n+1] *= source.getMaxValue()+1;
					}
					m++;
				}
			}
		    scannOmdd(omdd, 0, leafs, subtree_size, small_node_order_vertex, small_node_order_level);												//  scan the logical function of v

			for (RegulatoryMultiEdge me: l) {									//	For each incoming edge
				RegulatoryNode source = me.getSource();
//				for (int k = 0; k < me.getEdgeCount(); k++) {									// 		For each sub-edge of the multiedge
//					RegulatoryEdge e = me.getEdge(k);
					SourceItem sourceItem = report.reportFor(target, source);
					currentSource = sourceItem.reportItems;
					byte functionality = computeFunctionality(source.getMaxValue()+1, node_in_subtree.get(source).intValue(), leafs, subtree_size, small_node_order_vertex); //Compute its functionality
					sourceItem.sign = functionality;
					if (functionality == FUNC_POSITIVE) {
						functionalityMap.put(me, InteractionAnalysisSelector.CAT_POSITIVE);
					} else if (functionality == FUNC_NEGATIVE) {
						functionalityMap.put(me, InteractionAnalysisSelector.CAT_NEGATIVE);
					} else if (functionality == FUNC_DUAL) {
						functionalityMap.put(me, InteractionAnalysisSelector.CAT_DUAL);
					} else {
						functionalityMap.put(me, InteractionAnalysisSelector.CAT_NONFUNCTIONNAL);

					}
	//			}

			}
		}
				
		report.timeSpent = new Date().getTime()-before;
		LogManager.info("Done in "+report.timeSpent+"ms");
		return algoResult;
	}

	/**
	 * Recursive function scanning an OMDDNode 'omdd' and call itself on the 'omdd' children.
	 * When the function end, the array 'leaf' contains the value of all the real leafs.
	 * 	 * 
	 * @param omdd the current OMDDNode to scan. Should be the root at the first call.
	 * @param deep the deep call.
	 * @param leafs the resulting array.
	 * @param small_node_order 
	 * @param subtree_size 
	 */
	private int scannOmdd(OMDDNode omdd, int deep, byte [] leafs, int[] subtree_size, RegulatoryNode[] small_node_order_vertex, int[] small_node_order_levels) {
		if (omdd.next == null) { 								//If the current node is leaf
			if (subtree_size[deep] == 1) { 							//a real leaf, ie. all the inputs are present in the branch
				leafs[i_leafs++] = omdd.value;							//Save the current value.
				return i_leafs;
			} // else
			if (omdd.value == 0) {
				i_leafs += subtree_size[deep];		//if value is 0, skip them because the array is initialized to 0
			} else {													//else add the unreal leaf value to each of the real leafs 
				for (int i = 0; i < subtree_size[deep]; i++) {
					leafs[i_leafs++] = omdd.value;
				}
			}
			return subtree_size[deep];
		}
		
		boolean hasJumpedNode = false;
		int current_i = i_leafs, current_deep = deep;
		while (omdd.level != small_node_order_levels[deep]) {
			deep++;
			hasJumpedNode = true;
		}

		int res = -1, max = 0;
		for (int i = 0; i < omdd.next.length; i++) {		//Scan all the childs
			res = scannOmdd(omdd.next[i], deep+1, leafs, subtree_size, small_node_order_vertex, small_node_order_levels);
			if (res > max) {
                max = res;
            }
		}
		if (hasJumpedNode) {
			int added = i_leafs-current_i;
			for (int i = 0; i < subtree_size[current_deep]/added-1; i++) { //The first pass has ben computed, copy the results for the others
				for (int j = i_leafs; j < i_leafs+added; j++) {
					leafs[j] = leafs[j-added];
				}
				i_leafs += added;
			} 
			return i_leafs;
		}
		return res;
	}
	
	/**
	 * Compute the functionality of the 'node_index'-nth node in the omdd represented by 'leafs'.
	 * 
	 * @param count_childs the count of child above 'node_index'
	 * @param node_index the node to consider
	 * @param leafs a table of all the leafs of the complete omdd tree.
	 * @param subtree_size_t the size of the subtree
	 * @param small_node_order the node order in the subtree
	 * @return
	 */
	private byte computeFunctionality(int count_childs, int node_index, byte[] leafs, int[] subtree_size_t, RegulatoryNode[] small_node_order) {
		int size_of_subtree = subtree_size_t[node_index+1];
		
		ReportItem ri = null;
		byte res = FUNC_NON;
		boolean containsPositive = false, containsNegative = false;
		
		int index = 0;
		while (index+size_of_subtree < leafs.length) {
			for (int i_childs = 0; i_childs < count_childs - 1; i_childs++) {
				for (int i_subtree = 0; i_subtree < size_of_subtree; i_subtree++) {
					int low = leafs[index];
					int high = leafs[index+size_of_subtree];
					
					ri = new ReportItem();
					ri.targetValue_low = (byte) low;
					ri.targetValue_high = (byte) high;
					currentPath = new LinkedList<PathItem>();
					log_path(index, node_index, ri, subtree_size_t, small_node_order);
					
					if (low < high) {
						containsPositive = true;
						res = FUNC_POSITIVE;
					} else if (low > high) {
						containsNegative = true;
						res = FUNC_NEGATIVE;
					} else {
						res = FUNC_NON;
					}
					index++;
					ri.sign = res;
					ri.path = currentPath;
					currentSource.add(ri);
				}
			}
			index+=size_of_subtree;
		}
		if (containsNegative) {
	        if (containsPositive) {
	            return FUNC_DUAL;
	        }
	        return FUNC_NEGATIVE;
		}
		if (containsPositive) {
		    return FUNC_POSITIVE;
		}
		return FUNC_NON;
	}

	/**
	 * Log the path corresponding to the 'index'-nth leaf.
	 *
	 * @param index the leaf to consider.
	 * @param node_index the index of the source node of the current interaction.
	 * @param subtree_size_t a table of all the subtree size.
	 * @param small_node_order the node order in the subtree.
	 */
	private void log_path(int index, int node_index, ReportItem ri, int[] subtree_size_t, RegulatoryNode[] small_node_order) {
		for (int k = 0; k < small_node_order.length; k++) {
			RegulatoryNode v = small_node_order[k];
			byte count = (byte) (index/subtree_size_t[k+1]%(v.getMaxValue()+1));
			if (k != node_index) {
				PathItem pi = new PathItem();
				pi.targetValue_low = count;
				pi.vertex = v;
				currentPath.add(pi);
			} else {
				ri.sourceValue_low = count;
			}
		}
	}
}
