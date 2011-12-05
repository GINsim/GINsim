package org.ginsim.servicegui.tool.decisionanalysis;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.common.utils.GUIMessageUtils;
import org.ginsim.core.exception.GsException;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.core.graph.hierachicaltransitiongraph.HierarchicalTransitionGraph;
import org.ginsim.core.utils.log.LogManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.service.tool.decisionanalysis.DecisionAnalysisService;
import org.ginsim.servicegui.ServiceGUI;
import org.ginsim.servicegui.common.GUIFor;
import org.ginsim.servicegui.common.ToolAction;
import org.mangosdk.spi.ProviderFor;




@ProviderFor( ServiceGUI.class)
@GUIFor( DecisionAnalysisService.class)
public class DecisionAnalysisServiceGUI implements ServiceGUI {
	
	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof HierarchicalTransitionGraph) {
			List<Action> actions = new ArrayList<Action>();
			actions.add(new DecisionAnalysisAction((HierarchicalTransitionGraph)graph));
			return actions;
		}
		return null;
	}
}


class DecisionAnalysisAction extends ToolAction {

	private final HierarchicalTransitionGraph graph;
	
	public DecisionAnalysisAction( HierarchicalTransitionGraph graph) {
		
		super( "STR_htg_decision_analysis", "STR_htg_decision_analysis_descr");
		this.graph = graph;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        // TODO : REFACTORING ACTION		
		// TODO : what is ref? Is this test useful? Ref was set to 0 in the GsPluggableActionDescriptor definition in the getT_action
		//if (ref == 0) {
		try{
			new DecisionAnalysisFrame( GUIManager.getInstance().getFrame( graph), graph);
		}
		catch( GsException ge){
    		GUIMessageUtils.openErrorDialog( "Unable to launch the analysis");
    		LogManager.error( "Unable to execute the service");
    		LogManager.error( ge);
		}
		//}
	}
	
}