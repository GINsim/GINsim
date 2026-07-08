package org.ginsim.servicegui.tool.reg2dyn;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.service.EStatus;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.shell.actions.ToolAction;
import org.ginsim.gui.utils.widgets.Frame;
import org.ginsim.service.tool.reg2dyn.Reg2DynService;
import org.ginsim.service.tool.reg2dyn.SimulationParameterList;
import org.ginsim.service.tool.reg2dyn.SimulationParametersManager;
import org.kohsuke.MetaInfServices;


/**
 * main method for the reg2dyn plugin
 */
@MetaInfServices( ServiceGUI.class)
@GUIFor( Reg2DynService.class)
@ServiceStatus( EStatus.RELEASED)
public class Reg2DynServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions( Graph<?, ?> graph) {
		
		if( graph instanceof RegulatoryGraph){
			List<Action> actions = new ArrayList<>();
			actions.add( new Reg2DynAction( (RegulatoryGraph) graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_TOOLS_MAIN + 1;
	}


	class Reg2DynAction extends ToolAction {

		private final RegulatoryGraph graph;

		private Reg2DynAction( RegulatoryGraph graph, ServiceGUI serviceGUI) {
			super( "STR_reg2dyn", "STR_reg2dyn_descr", serviceGUI);
			this.graph = graph;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			if ( graph.getNodeOrderSize() < 1) {
				NotificationManager.publishWarning( graph, Txt.t("STR_emptyGraph"));
				return;
			}

			Frame mainFrame = GUIManager.getInstance().getFrame( graph);
			GraphGUI<?, ?, ?> gui = null;
			if (mainFrame != null) {
				gui = GUIManager.getInstance().getGraphGUI( graph);
				// TODO: replace this with a mode set on the gui
				// mainFrame.getActions().setCurrentMode( GsActions.MODE_DEFAULT, 0, false);
			}

			SimulationParameterList paramList = (SimulationParameterList) ObjectAssociationManager.getInstance().getObject( graph, SimulationParametersManager.KEY, true);
			SingleSimulationFrame simFrame = new SingleSimulationFrame(mainFrame, paramList);
			simFrame.setAssociatedGUI(gui);
			simFrame.setVisible(true);
		}

	}

}
