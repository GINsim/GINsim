package org.ginsim.servicegui.tool.decisionanalysis;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.colomoto.biolqm.LogicalModel;
import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalTransitionGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.LogicalModel2RegulatoryGraph;
import org.ginsim.core.service.EStatus;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.service.StandaloneGUI;
import org.ginsim.gui.shell.actions.ToolAction;
import org.kohsuke.MetaInfServices;
import org.ginsim.service.tool.reg2dyn.SimulationParameterList;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;
import org.ginsim.service.tool.reg2dyn.SimulationParametersManager;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;

@StandaloneGUI
@MetaInfServices( ServiceGUI.class)
@ServiceStatus( EStatus.RELEASED)
public class DecisionAnalysisServiceGUI extends AbstractServiceGUI {
	
	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof HierarchicalTransitionGraph) {
			List<Action> actions = new ArrayList<>();
			actions.add(new DecisionAnalysisAction((HierarchicalTransitionGraph)graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_TOOLS_MAIN + 120;
	}

	class DecisionAnalysisAction extends ToolAction {
		private final HierarchicalTransitionGraph graph;

		private DecisionAnalysisAction( HierarchicalTransitionGraph graph, ServiceGUI serviceGUI) {
			super( "STR_htg_decision_analysis", "STR_htg_decision_analysis_descr", serviceGUI);
			this.graph = graph;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			try {
                RegulatoryGraph lrg = graph.getAssociatedGraph();
				new DecisionAnalysisFrame(GUIManager.getInstance().getFrame(graph), graph, lrg);

			} catch( GsException ge){
				GUIMessageUtils.openErrorDialog( "Unable to launch the analysis");
				LogManager.error( "Unable to execute the service");
				LogManager.error( ge);
			}
		}
	}
}


