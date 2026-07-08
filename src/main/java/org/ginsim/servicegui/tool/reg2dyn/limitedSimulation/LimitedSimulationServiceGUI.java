package org.ginsim.servicegui.tool.reg2dyn.limitedSimulation;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNode;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalTransitionGraph;
import org.ginsim.core.graph.hierarchicaltransitiongraph.StatesSet;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphSelection;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.service.StandaloneGUI;
import org.ginsim.gui.shell.actions.ToolAction;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;
import org.ginsim.service.tool.reg2dyn.limitedsimulation.LimitedSimulationService;
import org.ginsim.service.tool.reg2dyn.limitedsimulation.OutgoingNodesHandlingStrategy;
import org.ginsim.service.tool.reg2dyn.limitedsimulation.SimulationConstraint;
import org.ginsim.service.tool.reg2dyn.priorityclass.PrioritySetList;
import org.kohsuke.MetaInfServices;

@StandaloneGUI
@MetaInfServices(ServiceGUI.class)
@ServiceStatus(EStatus.RELEASED)
public class LimitedSimulationServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof HierarchicalTransitionGraph) {
			List<Action> actions = new ArrayList<Action>();
			actions.add(new LimitedSimulationAction((HierarchicalTransitionGraph) graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_TOOLS_MAIN + 130;
	}
}

class LimitedSimulationAction extends ToolAction {
	private static final long serialVersionUID = -2719039497869822805L;
	private final HierarchicalTransitionGraph graph;

	public LimitedSimulationAction(HierarchicalTransitionGraph graph, ServiceGUI serviceGUI) {

		super("STR_limitedSimulation", "STR_limitedSimulation_descr", serviceGUI);
		this.graph = graph;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			RegulatoryGraph lrg = graph.getAssociatedGraph();

			GraphSelection<HierarchicalNode, ?> selection = GUIManager.getInstance().getGraphGUI(graph).getSelection();
			List<HierarchicalNode> selectedNodes = selection.getSelectedNodes();
			if (selectedNodes == null || selectedNodes.size() == 0) {
				NotificationManager.publishError(graph, "No HTG or SCC node selected");
				return;
			}
			StatesSet s = (StatesSet) selectedNodes.get(0).statesSet.clone();
			for (Iterator<HierarchicalNode> iterator = selectedNodes.listIterator(1); iterator.hasNext();) {
				HierarchicalNode hierarchicalNode = iterator.next();
				s.merge(hierarchicalNode.statesSet);
			}

			SimulationConstraint constraint = new SimulationConstraint(s,
					OutgoingNodesHandlingStrategy.CONTAIN_TO_SELECTION);
			if (!constraint.isValid()) {
				NotificationManager.publishError(graph, "No HTG or SCC node selected");
				return;
			}

			LimitedSimulationService service = GSServiceManager.getService(LimitedSimulationService.class);
			SimulationParameters params = new SimulationParameters(lrg);
			DynamicGraph dynGraph = service.run(graph, constraint, lrg.getModel(), params);

			// show the generated dynamic graph
			GUIManager.getInstance().whatToDoWithGraph(dynGraph);
		} catch (GsException ex) {
			LogManager.error("The scc/htg is not associated with a regulatory graph");
		}
	}

}
