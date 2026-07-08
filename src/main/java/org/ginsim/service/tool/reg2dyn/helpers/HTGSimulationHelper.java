package org.ginsim.service.tool.reg2dyn.helpers;

import java.util.Collection;
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
import org.ginsim.service.tool.reg2dyn.SimulationStrategy;
import org.ginsim.core.graph.regulatorygraph.LogicalModel2RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.service.tool.modelreduction.ReductionConfig;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
//

public class HTGSimulationHelper  implements SimulationHelper {
	protected HierarchicalNode node;
	protected HierarchicalTransitionGraph htg;
	public Map arcs;
	protected LogicalModel model;
	//Collection<RegulatoryMultiEdge> incEdges;
	public List<NodeInfo> incEdges;
	public HTGSimulationHelper(LogicalModel model, SimulationParameters params, ReductionConfig reduction) {
		this.model = model;
		boolean compacted = false;

		if (params.strategy == SimulationStrategy.HTG) {
			compacted = true;
		}
		List<NodeInfo> nodes = model.getComponents();
		this.htg = GSGraphManager.getInstance().getNewGraph( HierarchicalTransitionGraph.class, nodes, compacted);
		this.htg.setSimulationStrategy(params.strategy);
		// FIXME: associated graph based on LogicalModel
        //RegulatoryGraph lrg = LogicalModel2RegulatoryGraph.importModel(model);
		htg.setAssociatedGraph(params.param_list.graph);

		htg.setLogicalModel(model);
		NodeAttributesReader vreader = htg.getNodeAttributeReader();
        htg.getAnnotation().setComment(params.getDescr(nodes)+"\n");
		//Edges = params.param_list.graph.getIncomingEdges(node);
		if (reduction != null ){
			htg.setReduction(reduction);
		}
        arcs = new HashMap();
		//incEdges =  htg.getAssociatedGraph()..getNodeInfos();
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
