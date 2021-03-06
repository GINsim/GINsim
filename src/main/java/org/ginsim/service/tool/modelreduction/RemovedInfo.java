package org.ginsim.service.tool.modelreduction;

import java.util.Collection;

import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;

public class RemovedInfo {
	
	public final RegulatoryNode vertex;
	public final int pos;
	public final Collection<RegulatoryMultiEdge> targets;
	
	public RemovedInfo(RegulatoryNode vertex, int pos, Collection<RegulatoryMultiEdge> targets) {
		super();
		this.vertex = vertex;
		this.pos = pos;
		this.targets = targets;
	}
}

