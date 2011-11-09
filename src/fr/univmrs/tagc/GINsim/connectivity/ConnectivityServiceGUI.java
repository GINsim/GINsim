package fr.univmrs.tagc.GINsim.connectivity;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.graph.Graph;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.gui.service.GsActionAction;
import org.ginsim.gui.service.GsServiceGUI;
import org.ginsim.service.connectivity.ConnectivityService;
import org.mangosdk.spi.ProviderFor;

import fr.univmrs.tagc.GINsim.global.GsEnv;
import fr.univmrs.tagc.GINsim.graph.GsGinsimGraphDescriptor;
import fr.univmrs.tagc.GINsim.graph.GsGraphDescriptor;
import fr.univmrs.tagc.GINsim.gui.GsOpenAction;

/**
 * register the connectivity service
 */
@ProviderFor(GsServiceGUI.class)
@GUIFor(ConnectivityService.class)
public class ConnectivityServiceGUI implements GsServiceGUI {

	@Override
	public List<Action> getAvailableActions( Graph<?, ?> graph) {
		List<Action> actions = new ArrayList<Action>();
		actions.add( new ConnectivityAction( graph));
		
		return actions;
	}

	class ConnectivityAction extends GsActionAction{
		
		private final Graph graph;
		
		protected ConnectivityAction( Graph graph) {
	        super( "STR_connectivityExtract", null, "STR_connectivityExtract_descr", null);
			this.graph = graph;
		}
		
		@Override
		public void actionPerformed( ActionEvent arg0) {
			
		    if (graph instanceof GsReducedGraph) {
		        GsGraphDescriptor gd = GsGinsimGraphDescriptor.getInstance();
	            String s_ag = ((GsReducedGraph) graph).getAssociatedGraphID();
	            if (s_ag != null) {
	            	// TODO : REFACTORING ACTION
	            	// Change the GsOpenAction
	    	        Graph subgraph = GsOpenAction.open(gd, null, ((GsReducedGraph)graph).getSelectedMap(), s_ag);
	    	        if (subgraph != null) {
	    	            GsEnv.whatToDoWithGraph(null, subgraph, true);
	    	        }
	            }
		    } 
		    else {
		    	// TODO: get the parent frame
	            new ConnectivityFrame( null, graph);
		    }
		}
	}
	
}
