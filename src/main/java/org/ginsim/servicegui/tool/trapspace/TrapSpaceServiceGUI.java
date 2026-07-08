package org.ginsim.servicegui.tool.trapspace;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.shell.actions.ToolAction;
import org.ginsim.service.tool.trapspace.TrapSpaceServiceWrapper;
import org.kohsuke.MetaInfServices;

/**
 * Define the action for stableStates service
 */
@MetaInfServices(ServiceGUI.class)
@GUIFor(TrapSpaceServiceWrapper.class)
@ServiceStatus(EStatus.RELEASED)
public class TrapSpaceServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {

		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<>();
			actions.add(new NewTrapSpaceAction((RegulatoryGraph) graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_TOOLS_MAIN + 4;
	}

	class NewTrapSpaceAction extends ToolAction {

		private final RegulatoryGraph graph;

		private NewTrapSpaceAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
			super("STR_trapSpaces", "STR_trapSpaces_descr", serviceGUI);
			this.graph = graph;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (graph.getNodeOrderSize() < 1) {
				NotificationManager.publishWarning(graph, Txt.t("STR_emptyGraph"));
				return;
			}
			new TrapSpaceSwingUI(null, graph);
		}
	}

}
