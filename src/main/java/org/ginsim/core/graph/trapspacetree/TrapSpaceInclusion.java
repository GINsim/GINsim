package org.ginsim.core.graph.trapspacetree;

import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;

/**
 * class TrapSpaceInclusion
 * @author Aurelien Naldi
 */
public class TrapSpaceInclusion extends Edge<TrapSpaceNode> {

	public TrapSpaceInclusion(Graph<TrapSpaceNode, TrapSpaceInclusion> g, TrapSpaceNode source, TrapSpaceNode target) {
		super(g, source, target);
	}

}
