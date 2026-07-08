package org.ginsim.servicegui.export.struct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.common.application.GsException;
import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.shell.actions.ExportAction;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.service.export.struct.GraphStructureExportService;
import org.kohsuke.MetaInfServices;

import static org.ginsim.gui.service.ServiceGUI.W_EXPORT_GRAPH;


/**
 * Export service to struct format
 *
 * @author Aurelien Naldi
 */
@MetaInfServices( ServiceGUI.class)
@ServiceStatus( EStatus.RELEASED)
@GUIFor( GraphStructureExportService.class)
public class GraphStructureExportServiceGUI extends AbstractServiceGUI {
	

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		
		List<Action> actions = new ArrayList<Action>();
        if (graph instanceof RegulatoryGraph) {
            actions.add( new CytoscapeExportAction<Graph<?, ?>>( graph, this));
        }
        actions.add( new ExportGraphVizAction( graph, this));
		actions.add( new ExportBioLayoutAction( graph, this));
		return actions;
	}

	@Override
	public int getInitialWeight() {
		return W_EXPORT_GRAPH + 1 ;//W_EXPORT_GENERIC + 10;
	}
}


/**
 * Export to Biolayout file Action
 * 
 * @author Lionel Spinelli
 */
class ExportBioLayoutAction extends ExportAction {

	static final FileFormatDescription FORMAT = new FileFormatDescription("struct", "layout");
	
	public ExportBioLayoutAction( Graph graph, ServiceGUI serviceGUI) {
		
		super( graph, "STR_biolayout", "STR_biolayout_descr", serviceGUI);
	}

	@Override
	public FileFormatDescription getFileFilter() {
		return FORMAT;
	}

	@Override
	protected void doExport(String filename) throws GsException, IOException {
		
		GraphStructureExportService service = GSServiceManager.getService( GraphStructureExportService.class);
		
		if( service != null){
			service.biolayoutExport( graph, null, filename);
		}
		else{
			throw new GsException( GsException.GRAVITY_ERROR, "No GraphStructureExportService service available");
		}
	}
}


/**
 * Export to Cytoscape XGMMLfile Action
 *
 * @author Duncan Berenguier
 */
class CytoscapeExportAction<G extends Graph> extends ExportAction {
    private static final long serialVersionUID = 7934695744239100292L;
    private static final FileFormatDescription FORMAT = new FileFormatDescription("cytoscape network", "xgmml");

    public CytoscapeExportAction(G graph, ServiceGUI serviceGUI) {
        super( graph, "STR_cytoscape", "STR_cytoscape_descr", serviceGUI);
    }

    @Override
    protected void doExport(String filename) throws GsException, IOException {

        GraphStructureExportService service = GSServiceManager.getService( GraphStructureExportService.class);

        if( service != null){
            service.cytoscapeExport( (RegulatoryGraph)graph, filename);
        }
    }

    @Override
    public FileFormatDescription getFileFilter() {
        return FORMAT;
    }
}


/**
 * Export to GraphViz file Action
 *
 * @author Lionel Spinelli
 */
class ExportGraphVizAction extends ExportAction {

    private static final FileFormatDescription FORMAT = new FileFormatDescription("graphviz", "dot");

    public ExportGraphVizAction( Graph graph, ServiceGUI serviceGUI) {
        super( graph, "STR_graphviz", "STR_graphviz_descr", serviceGUI);
    }

    @Override
    protected void doExport(String filename) throws IOException {

        GraphStructureExportService service = GSServiceManager.getService( GraphStructureExportService.class);
        service.graphvizExport(graph, null, null, filename);
    }

    @Override
    public FileFormatDescription getFileFilter() {
        return FORMAT;
    }
}

