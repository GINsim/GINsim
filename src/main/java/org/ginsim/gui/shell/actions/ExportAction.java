package org.ginsim.gui.shell.actions;

import java.awt.event.ActionEvent;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.commongui.utils.FileFormatFilter;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.shell.FileSelectionHelper;
import org.ginsim.gui.utils.dialog.stackdialog.HandledStackDialog;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialogHandler;



public abstract class ExportAction<G extends Graph> extends BaseAction {

	protected final G graph;
	private final String id;
	
	/**
     * 
     * @param graph
     * @param name Entry to insert in the menu
     * @param tooltip Long description of the action
	 * @param serviceGui serviceGui
     */
	public ExportAction(G graph, String name, String tooltip, ServiceGUI serviceGui) {
		super(name, null, tooltip, null, serviceGui);
		this.graph = graph;
		String className = getClass().getName();
		this.id = className.substring(className.lastIndexOf('.')+1);
	}
	
	/**
	 * @return the identifier for this export.
	 * The identifier is based on the class name
	 * and should be used to store settings.
	 */
	public String getID() {
		return id;
	}

	/**
	 * The action was launched, show a GUI if needed, select a file and if all went fine, call the backend
	 *
	 */
	public void selectFile() {
		// TODO: restore file filters
		String filename = getSelectedFile();
		if (filename == null) {
			return;
		}
		try {
			doExport(filename);
            notifySuccess();
		} catch (Exception e) {
            notifyFailure(e);
		}
	}

    public String getSelectedFile() {
        return FileSelectionHelper.selectSaveFilename( null, new FileFormatFilter(getFileFilter()));
    }

    public void notifySuccess() {
        NotificationManager.publishInformation( graph, "Export finished");
    }
    public void notifyFailure(Exception e) {
        LogManager.error("Error in export "+getID());
        LogManager.error(e);
        NotificationManager.publishError( graph, "Export failed: " + e.getLocalizedMessage() + ". See logs for details");
    }
	public void notifyWarning(String message) {
        NotificationManager.publishWarning( graph, message);
	}


	public StackDialogHandler getConfigPanel() {
		return null;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		StackDialogHandler handler = getConfigPanel();
		if (handler != null) {
			new HandledStackDialog(handler);
		} else {
			selectFile();
		}
	}

	/**
	 * Get the file filter to be used for this export.
	 * It will be called after showing the configuration panel and before selecting the file
	 * Thus, the configuration can be used to return a different filter.
	 * 
	 * @return the active file filter
	 */
	abstract protected FileFormatDescription getFileFilter();
	
	/**
	 * Main export function: will be called after the target file was selected
	 */
	abstract protected void doExport(String filename) throws Exception;

}
