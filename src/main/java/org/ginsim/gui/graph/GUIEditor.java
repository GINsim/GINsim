package org.ginsim.gui.graph;

import java.awt.Component;

/**
 * An object in charge of graphical edition of an object type
 * 
 * @author Aurelien Naldi
 *
 * @param <T>  extend T
 */
public interface GUIEditor<T> {

	void setEditedItem(T item);

	/**
	 * component getter
	 * @return component
	 */
	Component getComponent();
}
