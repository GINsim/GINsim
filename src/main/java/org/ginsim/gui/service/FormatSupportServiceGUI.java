package org.ginsim.gui.service;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.colomoto.biolqm.io.LogicalModelFormat;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.service.FormatSupportService;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.shell.actions.ExportAction;
import org.ginsim.gui.shell.actions.ImportAction;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialogHandler;

/**
 * Generic GUI integration service for formats providers.
 * 
 * @author Aurelien Naldi
 *
 * @param <S> ormatSupportServ to extend
 */
public class FormatSupportServiceGUI<S extends FormatSupportService> extends AbstractServiceGUI {

	private final S service;
	private final String format_name;
	private final FileFormatDescription format;

	protected String import_tip = "";
	protected String export_tip = "";


	/**
	 * Constructor
	 * @param name the string name
	 * @param service The s service
	 * @param format the file  FileFormatDescription
	 */
	public FormatSupportServiceGUI(String name, S service, FileFormatDescription format) {
		this.format_name = name;
		this.format = format;
		this.service = service;
	}
	
	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		List<Action> actions = new ArrayList<Action>();
		if (service.canImport()) {
			actions.add(new FormatImportAction(format_name, import_tip, this));
		}
		if (service.canExport() && graph instanceof RegulatoryGraph) {
			actions.add(new FormatExportAction(format_name, export_tip, (RegulatoryGraph)graph, this));
		}
		return actions;
	}

	@Override
	public int getInitialWeight() {
		return 5;
	}
	
	public StackDialogHandler getConfigPanel(ExportAction action, RegulatoryGraph graph) {
		return null;
	}

	public void doImport(String filename) {
		if (filename == null) {
			return;
		}

		try {
			RegulatoryGraph newGraph = service.importLRG(filename);
			GUIManager.getInstance().whatToDoWithGraph(newGraph);
		} catch (Exception e) {
			LogManager.error("Error in "+format_name+" import:\n "+e);
		}
	}
	
	public void doExport(RegulatoryGraph graph, String filename, ExportAction<?> action) {
		if (filename == null) {
			return;
		}

		try {
			String message = service.export(graph, filename);
			if (message != null) {
				action.notifyWarning(message);
			}
		} catch (Exception e) {
			LogManager.error("Error in "+format_name+" export:\n"+e);
		}

	}
	
	public FileFormatDescription getFileFilter() {
		return format;
	}

    protected boolean canExport(RegulatoryGraph lrg) {
        return service.canExportModel(lrg);
    }

}


class FormatImportAction extends ImportAction {

	private final FormatSupportServiceGUI serviceGUI;
		
	public FormatImportAction(String title, String tooltip, FormatSupportServiceGUI serviceGUI) {
		super(title, tooltip);
		this.serviceGUI = serviceGUI;
	}

	@Override
	protected FileFormatDescription getFileFilter() {
		return serviceGUI.getFileFilter();
	}

	@Override
	protected void doImport(String filename) {
		serviceGUI.doImport(filename);
	}
}

class FormatExportAction extends ExportAction<RegulatoryGraph> {

	private final FormatSupportServiceGUI serviceGUI;

	public FormatExportAction(String title, String tooltip, RegulatoryGraph graph, FormatSupportServiceGUI serviceGUI) {
		super(graph, title, tooltip, serviceGUI);
		this.serviceGUI = serviceGUI;
	}

	@Override
	public FileFormatDescription getFileFilter() {
		return serviceGUI.getFileFilter();
	}

	@Override
	protected void doExport(String filename) throws IOException {
		serviceGUI.doExport(graph, filename, this);
	}
	
	public StackDialogHandler getConfigPanel() {
		return serviceGUI.getConfigPanel(this, graph);
	}

    public void actionPerformed(ActionEvent arg0) {
        if (!serviceGUI.canExport(graph)) {
            NotificationManager.publishError(graph, "Graph not supported by this format (is it multivalued?");
            return;
        }
        super.actionPerformed(arg0);
    }

}
