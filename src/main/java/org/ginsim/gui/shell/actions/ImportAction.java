package org.ginsim.gui.shell.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.commongui.utils.FileFormatFilter;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.shell.FileSelectionHelper;

public abstract class ImportAction extends BaseAction {

	private static final long serialVersionUID = 4466248789435823349L;
	private final String id;

	public ImportAction(String name) {
		this(name, null, null, null);
	}
		
	/**
	 * 
	 * @param name
	 *            Entry to insert in the menu
	 * @param tooltip
	 *            Long description of the action
	 */
	public ImportAction(String name, String tooltip) {
		this(name, null, tooltip, null);
	}

	/**
	 * @return the identifier for this export. The identifier is based on the
	 *         class name and should be used to store settings.
	 */
	public String getID() {
		return id;
	}

	/**
	 * Constructor
	 * @param name Entry to insert in the menu
	 * @param icon icon image for menu and toolbar
	 * @param tooltip Long description of the action
	 * @param accelerator
	 *            the keyboard bytecut
	 */
	public ImportAction(String name, ImageIcon icon, String tooltip,
			KeyStroke accelerator) {
		super(name, icon, tooltip, accelerator, null, null);
		String className = getClass().getName();
		this.id = className.substring(className.lastIndexOf('.') + 1);
	}

	/**
	 * The action was launched, select a file and if all
	 * went fine, call the import service gui
	 */
	public void selectFile() {
		String filename = FileSelectionHelper.selectOpenFilename(null,
				new FileFormatFilter(getFileFilter()));
		if (filename == null) {
			return;
		}
		try {
			doImport(filename);
		} catch (Exception e) {
			LogManager.error("Error in export " + getID());
			LogManager.error(e);
			GUIMessageUtils.openErrorDialog("Import failed:\n"+e.getMessage());
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		selectFile();
		GUIManager.getInstance().closeEmptyGraphs();
	}

	/**
	 * Get the file filter to be used for this import.
	 * 
	 * @return the active file filter
	 */
	abstract protected FileFormatDescription getFileFilter();

	/**
	 * Main import function: will be called after the target file was selected
	 */
	abstract protected void doImport(String filename) throws GsException,
			IOException;
}
