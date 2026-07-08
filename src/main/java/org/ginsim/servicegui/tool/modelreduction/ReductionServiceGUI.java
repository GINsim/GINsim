package org.ginsim.servicegui.tool.modelreduction;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.service.EStatus;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.shell.actions.ToolAction;
import org.ginsim.service.tool.modelreduction.ModelReductionService;
import org.kohsuke.MetaInfServices;

/**
 * main method for the model simplification plugin
 */
@MetaInfServices(ServiceGUI.class)
@GUIFor(ModelReductionService.class)
@ServiceStatus( EStatus.RELEASED)
public class ReductionServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<>();
			actions.add(new ReductionAction((RegulatoryGraph)graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_TOOLS_MAIN + W_UNDER_DEVELOPMENT + 1;
	}


	class ReductionAction extends ToolAction {

		private static final long serialVersionUID = 2341667285128867588L;
		private final RegulatoryGraph graph;

		private ReductionAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
			super("STR_reduce", "STR_reduce_descr", serviceGUI);
			this.graph = graph;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (graph.getNodeCount() < 1) {
				NotificationManager.publishWarning( graph, "STR_emptyGraph");
				return;
			}

			// TODO: reset edit mode
			// mframe.getActions().setCurrentMode(GsActions.MODE_DEFAULT, 0, false);
			new ReductionConfigDialog(graph);
		}
	}
}
