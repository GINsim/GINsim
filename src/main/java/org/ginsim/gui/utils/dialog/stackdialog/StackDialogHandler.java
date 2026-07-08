package org.ginsim.gui.utils.dialog.stackdialog;

import java.awt.Component;

import org.ginsim.commongui.dialog.DefaultDialogSize;


public interface StackDialogHandler {
	/**
	 * Dialog setter
	 * @param dialog the dialog
	 */
	void setStackDialog( HandledStackDialog dialog);

	/**
	 * Getter for main component
	 * @return component
	 */
	Component getMainComponent();

	/**
	 * Run function
	 * @return boolean status
	 */
	boolean run();

	/**
	 * Close function
	 */
	void close();

	/**
	 * Getter for default size
	 * @return the size DefaultDialogSize
	 */
	DefaultDialogSize getDefaultSize();
}
