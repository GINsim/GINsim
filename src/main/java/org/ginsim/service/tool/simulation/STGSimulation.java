package org.ginsim.service.tool.simulation;

import java.util.List;
import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;

import org.colomoto.biolqm.tool.simulation.multiplesuccessor.MultipleSuccessorSimulation;
import org.colomoto.biolqm.tool.simulation.multiplesuccessor.MultipleSuccessorsUpdater;
import org.colomoto.biolqm.tool.simulation.multiplesuccessor.SimulationStrategy;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.dynamicgraph.DynamicNode;

/**
 * Simulation module to construct STG based on the updaters in LogicalModel
 *
 * @author Lucas Baudin
 * @author Aurelien Naldi
 */
public class STGSimulation extends MultipleSuccessorSimulation {

	private final DynamicGraph stateTransitionGraph;

	public STGSimulation(LogicalModel model, MultipleSuccessorsUpdater updater) {
		this(model, updater, SimulationStrategy.DEPTH_FIRST);
	}

	public STGSimulation(LogicalModel model, MultipleSuccessorsUpdater updater, SimulationStrategy strategy) {
		super(updater, strategy);
		
		List<NodeInfo> nodes = model.getComponents();
		stateTransitionGraph = GSGraphManager.getInstance().getNewGraph( DynamicGraph.class, nodes);
		stateTransitionGraph.setLogicalModel(model);
	}

	public DynamicGraph getGraph() {
		return stateTransitionGraph;
	}

	@Override
	public void addState(byte[] state) {
		DynamicNode node = new DynamicNode(state);
		if (stateTransitionGraph.addNode(node)) {
			enqueue( state);
		}
	}

	@Override
	public void addTransition(byte[] from, byte[] to) {
		DynamicNode node_from = new DynamicNode(from);
		DynamicNode node_to = new DynamicNode(to);
		stateTransitionGraph.addNode(node_from);
		stateTransitionGraph.addNode(node_to);

		stateTransitionGraph.addEdge(node_from, node_to, true);
	}

}
