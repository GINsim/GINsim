package org.ginsim.servicegui.tool.circuit;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.service.EStatus;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.shell.actions.ToolAction;
import org.ginsim.service.tool.circuit.CircuitService;
import org.kohsuke.MetaInfServices;


/**
 * main method for the circuit service
 * 
 * @author Aurelien Naldi
 */
@MetaInfServices( ServiceGUI.class)
@GUIFor( CircuitService.class)
@ServiceStatus( EStatus.RELEASED)
public class CircuitServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<>();
			actions.add(new CircuitAction((RegulatoryGraph)graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return ServiceGUI.W_TOOLS_MAIN + 5;
	}


	class CircuitAction extends ToolAction {

		private final RegulatoryGraph graph;

		private CircuitAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
			super("STR_circuit", serviceGUI);
			this.graph = graph;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (graph.getNodeCount() < 1) {
				NotificationManager.publishWarning( graph, Txt.t("STR_emptyGraph"));
				return;
			}
			new CircuitFrame( GUIManager.getInstance().getFrame( graph), graph);
		}

	}
}
