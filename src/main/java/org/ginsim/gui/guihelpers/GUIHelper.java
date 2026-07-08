package org.ginsim.gui.guihelpers;

import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;

import java.awt.Component;

/**
 * A GUIHelper provides a GUI for a given type of data.
 * It will be used to add hooks allowing other to edit a specific type of data without knowing its GUI
 * @param <T>  helper
 * @author Aurelien Naldi
 */
public interface GUIHelper<T> {

	/**
	 * get a panel to edit this data
	 * @param dialog stack dialog
	 * @param o  T object
	 * @return a specialised component.
	 */
	Component getPanel(T o, StackDialog dialog);
	
	/**
	 * get a panel to select an item (for lists of elements)
	 * @param o T object
	 * @param dialog stackdialog
	 * @return a specialised component.
	 */
	Component getSelectionPanel(T o, StackDialog dialog);

	/**
	 * Test if this helper supports a given object
	 * 
	 * @param o the object to be tested
	 * @return true if this helper can edit this object, false otherwise
	 */
	boolean supports(Object o);
}
