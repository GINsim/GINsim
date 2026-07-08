package org.ginsim.service.tool.reg2dyn.limitedsimulation;

import java.util.HashMap;

import org.colomoto.biolqm.LogicalModel;
import org.ginsim.common.application.GsException;
import org.ginsim.common.callable.BasicProgressListener;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.dynamicgraph.DynamicNode;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNode;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalTransitionGraph;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.Service;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;
import org.kohsuke.MetaInfServices;

@MetaInfServices(Service.class)
@Alias("htg-simulation")
public class LimitedSimulationService implements Service {

	private LimitedSimulation simu;

	/**
	 * Construct the dynamic graph correponding the contraint on the HTG.
	 * 
	 * @param htg
	 * @param constraint
	 * @return the dynamic graph
	 * @throws GsException if getAssociatedGraph goes wrong
	 */
	public DynamicGraph run(HierarchicalTransitionGraph htg, SimulationConstraint constraint, LogicalModel model,
			SimulationParameters params) throws GsException {
		if (!constraint.isValid()) {
			throw new GsException(GsException.GRAVITY_ERROR, "No HTG or SCC node selected");
		}

		if (params == null) {
			params = new SimulationParameters(htg.getAssociatedGraph());
		}

		BasicProgressListener<Graph> simulationManager = new BasicProgressListener<Graph>();
		this.simu = new LimitedSimulation(model, htg, constraint, params, simulationManager);
		simu.run();
		return (DynamicGraph) simulationManager.result;
	}

	public static HashMap<DynamicNode, HierarchicalNode> getStatesToHierarchicalNodes(DynamicGraph dynamicGraph) {
		return (HashMap<DynamicNode, HierarchicalNode>) ObjectAssociationManager.getInstance().getObject(dynamicGraph,
				StatesToHierarchicalMappingManager.KEY, true);
	}
}
