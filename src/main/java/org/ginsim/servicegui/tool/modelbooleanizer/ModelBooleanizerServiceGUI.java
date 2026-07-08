package org.ginsim.servicegui.tool.modelbooleanizer;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.colomoto.biolqm.LogicalModel;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.LogicalModel2RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.shell.actions.ToolAction;
import org.ginsim.service.tool.modelbooleanizer.ModelBooleanizerService;
import org.kohsuke.MetaInfServices;

/**
 * Main method to Booleanize a Multivalued model
 * 
 * @author Pedro T. Monteiro
 */
@MetaInfServices(ServiceGUI.class)
@GUIFor(ModelBooleanizerService.class)
@ServiceStatus(EStatus.RELEASED)
public class ModelBooleanizerServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<>();
			actions.add(new BooleanizeAction((RegulatoryGraph) graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_TOOLS_MAIN + W_UNDER_DEVELOPMENT + 2;
	}

	class BooleanizeAction extends ToolAction {

		private static final long serialVersionUID = 4751364133411945974L;
		private final RegulatoryGraph graph;

		private BooleanizeAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
			super("STR_booleanize", "STR_booleanize_descr", serviceGUI);
			this.graph = graph;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (this.graph.getModel().isBoolean()) {
				GUIMessageUtils.openErrorDialog("STR_booleanize_isBoolean");
				return;
			}

			try {
				ModelBooleanizerService service = GSServiceManager.getService(ModelBooleanizerService.class);
				LogicalModel bModel = service.booleanize(this.graph.getModel());

				RegulatoryGraph bGraph = LogicalModel2RegulatoryGraph
						.importModel(bModel);
				service.copyNodeStyles(this.graph, bGraph);

				// Show the Booleanized graph
				GUIManager.getInstance().whatToDoWithGraph(bGraph);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: error dialog
			}
		}
	}
}

