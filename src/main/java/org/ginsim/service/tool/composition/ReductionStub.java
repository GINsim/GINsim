package org.ginsim.service.tool.composition;

import java.util.List;

import org.colomoto.biolqm.NodeInfo;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.service.tool.modelreduction.ReductionLauncher;

/**
 * Implements a ReductionLauncher interface to recover the results of invoking
 * ModuleSimplifier
 * 
 * @author Nuno D. Mendes
 */

public class ReductionStub implements ReductionLauncher {
	private RegulatoryGraph graph = null;
	private RegulatoryGraph reducedGraph = null;

	public ReductionStub(RegulatoryGraph graph) {
		this.graph = graph; // This is not really used
	}

	/**
	 * @param graph a Graph object (which needs to be a RegulatoryGraph)
	 * 
	 * @param e an Exception which might have occured during reduction
	 *            (non-Javadoc)
	 * 
	 * @see org.ginsim.service.tool.modelreduction.ReductionLauncher
	 *      .ginsim.core.graph.common.Graph, java.lang.Exception)
	 */
	public void endSimu(Graph graph, Exception e) {
		// TODO: Verify whether graph is in fact a RegulatoryGraph
		// TODO: Deal with Exception
		this.reducedGraph = (RegulatoryGraph) graph;

	}

	/**
	 * Stub method ignoring GUI options
	 * 
	 * (non-Javadoc)
	 * @param l_todo  to do list
	 * @see org.ginsim.service.tool.modelreduction.ReductionLauncher
	 *      showPartialReduction(java.util.List)
	 */
	@Override
	public boolean showPartialReduction(List<NodeInfo> l_todo) {
		return false;
	}

	/**
	 * @return The computed reduced graph
	 */
	public RegulatoryGraph getReducedGraph() {
		return this.reducedGraph;
	}

}
