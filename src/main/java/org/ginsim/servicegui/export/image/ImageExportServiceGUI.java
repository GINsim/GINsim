package org.ginsim.servicegui.export.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.common.application.GsException;
import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.shell.actions.ExportAction;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.service.export.image.ImageExportService;
import org.kohsuke.MetaInfServices;
import static org.ginsim.gui.service.ServiceGUI.W_EXPORT_DOC;

/**
 * GUI integration of the PNG and SVG exports.
 *
 * @author Aurelien Naldi
 */
@MetaInfServices( ServiceGUI.class)
@GUIFor( ImageExportService.class)
@ServiceStatus( EStatus.RELEASED)
public class ImageExportServiceGUI extends AbstractServiceGUI {
	
	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		
		List<Action> actions = new ArrayList<Action>();
		actions.add( new ExportImageAction( graph, this));
        actions.add( new ExportSVGAction( graph, this));

		return actions;
	}

	@Override
	public int getInitialWeight() {
		return W_EXPORT_DOC+1 ; // ,+ 10
	}
}


/**
 * Export to PNG image Action
 * 
 * @author Lionel Spinelli
 *
 */
class ExportImageAction extends ExportAction {

	private static final FileFormatDescription FORMAT = new FileFormatDescription("PNG image", "png");

	public ExportImageAction( Graph graph, ServiceGUI serviceGUI) {
		super( graph, "STR_Image", "STR_Image_descr", serviceGUI);
	}

	@Override
	public FileFormatDescription getFileFilter() {
		return FORMAT;
	}

	@Override
	protected void doExport(String filename) throws GsException, IOException {
        ImageExportService service = GSServiceManager.getService( ImageExportService.class);
        GraphGUI gui = GUIManager.getInstance().getGraphGUI(graph);
        int f = 4;
        if (gui != null) {
            f = (int)Math.ceil(2*gui.getZoomLevel());
        }
        service.exportPNG(graph, filename, f);
	}
}


/**
 * Export to SVG File Action
 *
 * @author Lionel Spinelli
 *
 */
class ExportSVGAction extends ExportAction {

    private static final FileFormatDescription FORMAT = new FileFormatDescription("SVG image", "svg");

    public ExportSVGAction( Graph graph, ServiceGUI serviceGUI) {
        super( graph, "STR_SVG", "STR_SVG_descr", serviceGUI);
    }

    @Override
    public FileFormatDescription getFileFilter() {
        return FORMAT;
    }

    @Override
    protected void doExport(String filename) throws GsException, IOException {

        ImageExportService service = GSServiceManager.getService( ImageExportService.class);

        if( service != null){
            service.exportSVG(graph, null, null, filename);
        }
        else{
            throw new GsException( GsException.GRAVITY_ERROR, "No SVGExportService service available");
        }
    }
}
