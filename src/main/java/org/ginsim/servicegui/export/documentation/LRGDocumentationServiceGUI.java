package org.ginsim.servicegui.export.documentation;

import org.ginsim.Launcher;
import org.ginsim.common.application.GsException;
import org.ginsim.common.document.DocumentWriter;
import org.ginsim.common.document.GenericDocumentFormat;
import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.shell.actions.ExportAction;
import org.ginsim.service.export.documentation.LRGDocumentationService;
import org.kohsuke.MetaInfServices;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.ginsim.gui.service.ServiceGUI.W_EXPORT_DOC;


/**
 * GenericDocumentExport is a plugin to export the documentation of a model into multiples document format.
 * 
 * It export using a documentWriter. You can add support for your own document writer using <u>addSubFormat</u>
 * 
 * @see DocumentWriter
 */
@MetaInfServices(ServiceGUI.class)
@GUIFor(LRGDocumentationService.class)
@ServiceStatus( EStatus.RELEASED)
public class LRGDocumentationServiceGUI extends AbstractServiceGUI {

    private final LRGDocumentationService service;

	public LRGDocumentationServiceGUI() {
        service = GSServiceManager.getService(LRGDocumentationService.class);
	}


    @Override
    public List<Action> getAvailableActions(Graph<?, ?> graph) {
        if (graph instanceof RegulatoryGraph) {
        	RegulatoryGraph lrg = (RegulatoryGraph)graph;
            List<Action> actions = new ArrayList<Action>();
        	for (GenericDocumentFormat format: GenericDocumentFormat.getAllFormats()) {
                actions.add(new LRGDocumentationAction(lrg, format, service));
        	}
            if (Launcher.developer_mode) {
                actions.add(new JSONDocumentationAction(lrg, this));
            }

            return actions;
        }
        return null;
    }

    @Override
    public int getInitialWeight() {
        return W_EXPORT_DOC  + 4;
    }
}

/**
 * Export documentation as an interactive HTML page
 *
 * @author Aurelien Naldi
 */
class JSONDocumentationAction extends ExportAction<RegulatoryGraph> {

    private static final FileFormatDescription FORMAT = new FileFormatDescription("HTML page", "html");

    public JSONDocumentationAction( RegulatoryGraph graph, ServiceGUI serviceGUI) {
        super( graph, "STR_JSON_Doc", "STR_JSON_Doc_descr", serviceGUI);
    }

    @Override
    public FileFormatDescription getFileFilter() {
        return FORMAT;
    }

    @Override
    protected void doExport(String filename) throws GsException, IOException, Exception {

        LRGDocumentationService service = GSServiceManager.getService( LRGDocumentationService.class);

        if( service != null){
            service.export(graph, filename);
        }
        else{
            throw new GsException( GsException.GRAVITY_ERROR, "No LRGDocumentationService service available");
        }
    }
}
