package org.ginsim.core.graph.trapspacetree;

import java.util.Collection;
import java.util.List;

import org.ginsim.core.graph.AbstractDerivedGraph;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.GraphChangeType;
import org.ginsim.core.graph.GraphEventCascade;
import org.ginsim.core.graph.GraphFactory;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;

/**
 * class TrapSpaceTreeImpl
 * @author Aurelien Naldi
 */
public class TrapSpaceTreeImpl extends AbstractDerivedGraph<TrapSpaceNode, TrapSpaceInclusion, RegulatoryGraph, RegulatoryNode, RegulatoryMultiEdge>
	implements TrapSpaceInclusionDiagram {

	public TrapSpaceTreeImpl(GraphFactory factory) {
		super(factory);
	}

	@Override
	public int getNodeOrderSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Graph<TrapSpaceNode, TrapSpaceInclusion> getSubgraph(Collection<TrapSpaceNode> node,
			Collection<TrapSpaceInclusion> edges) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphEventCascade graphChanged(RegulatoryGraph g, GraphChangeType type, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean isAssociationValid(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			return true;
		}
		
		return false;
	}

	@Override
	protected List<?> doMerge(Graph<TrapSpaceNode, TrapSpaceInclusion> graph) {
		// TODO Auto-generated method stub
		return null;
	}

}
