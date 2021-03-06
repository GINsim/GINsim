package org.ginsim.service.tool.reg2dyn.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNode;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalTransitionGraph;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;
import org.ginsim.service.tool.reg2dyn.SimulationQueuedState;


public class HTGSimulationHelper  implements SimulationHelper {
	protected HierarchicalNode node;
	protected HierarchicalTransitionGraph htg;
	public Map arcs;
	protected LogicalModel model;
	
	public HTGSimulationHelper(LogicalModel model, SimulationParameters params) {
		this.model = model;
		boolean compacted = false;
		if (params.simulationStrategy == SimulationParameters.STRATEGY_HTG) {
			compacted = true;
		}
		List<NodeInfo> nodes = model.getComponents();
		this.htg = GSGraphManager.getInstance().getNewGraph( HierarchicalTransitionGraph.class, nodes, compacted);
		
		// FIXME: associated graph based on LogicalModel
		htg.setAssociatedGraph(params.param_list.graph);
		htg.setLogicalModel(model);
		
		NodeAttributesReader vreader = htg.getNodeAttributeReader();
        htg.getAnnotation().setComment(params.getDescr(nodes)+"\n");
        arcs = new HashMap();
	}

	public boolean addNode(SimulationQueuedState item) {
		return false;
	}

	public Graph endSimulation() {
		
		return htg;
	}

	public void setStable() {
	}

	public Object getNode() {
		return node;
	}
	
	public void setNode(Object node) {
		this.node = (HierarchicalNode) node;
	}
	
	public Graph getDynamicGraph() {
		
		return this.htg;
	}
}
