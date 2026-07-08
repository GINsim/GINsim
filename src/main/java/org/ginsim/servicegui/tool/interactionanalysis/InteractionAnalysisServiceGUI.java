package org.ginsim.servicegui.tool.interactionanalysis;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.EStatus;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.shell.actions.ToolAction;
import org.ginsim.service.tool.interactionanalysis.InteractionAnalysisService;
import org.kohsuke.MetaInfServices;


@MetaInfServices(ServiceGUI.class)
@GUIFor(InteractionAnalysisService.class)
@ServiceStatus( EStatus.RELEASED)
public class InteractionAnalysisServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<>();
			actions.add(new InteractionAnalysisAction<RegulatoryGraph>((RegulatoryGraph)graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_TOOLS_MAIN + 6;
	}

	class InteractionAnalysisAction<G extends Graph<?, ?>> extends ToolAction {
		private static final long serialVersionUID = 216892824635448627L;
		private final RegulatoryGraph graph;

		private InteractionAnalysisAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
			super("STR_interactionAnalysis", "STR_interactionAnalysis_descr", serviceGUI);
			this.graph = graph;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			new InteractionAnalysisFrame( GUIManager.getInstance().getFrame( graph), graph);
		}
	}
}

