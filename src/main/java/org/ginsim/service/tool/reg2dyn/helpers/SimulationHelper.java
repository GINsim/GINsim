package org.ginsim.service.tool.reg2dyn.helpers;

import org.colomoto.biolqm.NodeInfo;
import org.ginsim.core.graph.Graph;
import org.ginsim.service.tool.reg2dyn.SimulationQueuedState;

import java.util.List;


public interface SimulationHelper {
	
	boolean addNode(SimulationQueuedState item);
	Graph endSimulation();
	void setStable();
	Object getNode();
	void setNode(Object node);
	Graph getDynamicGraph();
}