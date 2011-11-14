package org.ginsim.gui.service.tools.interactionanalysis;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.graph.common.Graph;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.service.GsServiceGUI;
import org.ginsim.gui.service.common.GUIFor;
import org.ginsim.gui.service.common.GsToolsAction;
import org.ginsim.service.action.interactionanalysis.InteractionAnalysisService;
import org.mangosdk.spi.ProviderFor;

import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraph;

@ProviderFor(GsServiceGUI.class)
@GUIFor(InteractionAnalysisService.class)
public class InteractionAnalysisServiceGUI implements GsServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof GsRegulatoryGraph) {
			List<Action> actions = new ArrayList<Action>();
			actions.add(new InteractionAnalysisAction((GsRegulatoryGraph)graph));
			return actions;
		}
		return null;
	}
}

class InteractionAnalysisAction extends GsToolsAction {
	
	private final GsRegulatoryGraph graph;
	
	public InteractionAnalysisAction(GsRegulatoryGraph graph) {
		
		super("STR_interactionAnalysis");
		this.graph = graph;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		new InteractionAnalysisFrame( GUIManager.getInstance().getFrame( graph), graph);
	}
}