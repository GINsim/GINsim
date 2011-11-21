package org.ginsim.gui.service.tool.reg2dyn;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.exception.NotificationMessage;
import org.ginsim.exception.NotificationMessageHolder;
import org.ginsim.graph.common.Graph;
import org.ginsim.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.graph.regulatorygraph.initialstate.InitialStateManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.graph.regulatorygraph.GsMutantListManager;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.common.GUIFor;
import org.ginsim.gui.service.common.ToolAction;
import org.ginsim.service.tool.reg2dyn.Reg2DynService;
import org.mangosdk.spi.ProviderFor;

import fr.univmrs.tagc.common.managerresources.Translator;
import fr.univmrs.tagc.common.widgets.Frame;

/**
 * main method for the reg2dyn plugin
 */
@ProviderFor( ServiceGUI.class)
@GUIFor( Reg2DynService.class)
public class Reg2DynServiceGUI implements ServiceGUI {

    static {
    	if( !ObjectAssociationManager.getInstance().isObjectManagerRegistred( RegulatoryGraph.class, GsMutantListManager.key)){
    		ObjectAssociationManager.getInstance().registerObjectManager(RegulatoryGraph.class, new GsMutantListManager());
    	}
        ObjectAssociationManager.getInstance().registerObjectManager( RegulatoryGraph.class, new InitialStateManager());
        ObjectAssociationManager.getInstance().registerObjectManager( RegulatoryGraph.class, new SimulationParametersManager());
    }
    

//    public void runAction(int actionType, int ref, Graph graph, JFrame frame) throws GsException {
//        if (actionType != ACTION_ACTION) {
//            return;
//        }
//        if (!(graph instanceof RegulatoryGraph) || graph.getNodeOrderSize() < 1) {
//            graph.addNotificationMessage(new NotificationMessage(graph, 
//            		Translator.getString(graph instanceof RegulatoryGraph ? "STR_emptyGraph" : "STR_notRegGraph"), 
//            		NotificationMessage.NOTIFICATION_WARNING));
//            return;
//        }
//		if (ref == 0 || ref == 1) {
////            Map m_params = (Map)graph.getObject("reg2dyn_parameters");
////            if (m_params == null) {
////                m_params = new HashMap();
////                graph.addObject("reg2dyn_parameters", m_params);
////            }
////            new Reg2dynFrame(frame, (RegulatoryGraph)graph, m_params).setVisible(true);
//            GsMainFrame mainFrame = graph.getGraphManager().getMainFrame();
//            if (mainFrame != null) {
//            	mainFrame.getActions().setCurrentMode(GsActions.MODE_DEFAULT, 0, false);
//            }
//
//            SimulationParameterList paramList = (SimulationParameterList)graph.getObject(SimulationParametersManager.key, true);
//            if (ref == 0) {
//                new SingleSimulationFrame(frame, paramList).setVisible(true);
//            } else {
//                new BatchSimulationFrame(frame, paramList).setVisible(true);
//            }
//		}
//	}
    
	@Override
	public List<Action> getAvailableActions( Graph<?, ?> graph) {
		
		if( graph instanceof RegulatoryGraph){
			List<Action> actions = new ArrayList<Action>();
			actions.add(new Reg2DynAction( (RegulatoryGraph) graph));
			return actions;
		}
		return null;
	}
}


class Reg2DynAction extends ToolAction {

	private final RegulatoryGraph graph;
	private final boolean batch = false;
	
	public Reg2DynAction( RegulatoryGraph graph) {
		super( "STR_reg2dyn", "STR_reg2dyn_descr");
		this.graph = graph;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        
		if ( graph.getNodeOrderSize() < 1) {
            new NotificationMessage( (NotificationMessageHolder) graph, Translator.getString("STR_emptyGraph"), NotificationMessage.NOTIFICATION_WARNING);

            return;
        }
		
		Frame mainFrame = GUIManager.getInstance().getFrame( graph);
		if (mainFrame != null) {
			GraphGUI<?, ?, ?> gui = GUIManager.getInstance().getGraphGUI( graph);
			// TODO: replace this with a mode set on the gui
			// mainFrame.getActions().setCurrentMode( GsActions.MODE_DEFAULT, 0, false);
		}

		SimulationParameterList paramList = (SimulationParameterList) ObjectAssociationManager.getInstance().getObject( graph, SimulationParametersManager.key, true);
		// TODO : Restore the 
		//if (ref == 0 || ref == 1) {
		if (batch) {
			new BatchSimulationFrame(mainFrame, paramList).setVisible(true);
		} else {
			new SingleSimulationFrame(mainFrame, paramList).setVisible(true);
		}
	}
		
}