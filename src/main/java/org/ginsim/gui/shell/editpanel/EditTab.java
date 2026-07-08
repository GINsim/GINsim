package org.ginsim.gui.shell.editpanel;

import java.awt.Component;

import javax.swing.JPopupMenu;

import org.ginsim.gui.graph.GraphSelection;

/**
 * Simple interface for edition panels.
 * They provide a title, a component, and can decide to be disabled depending on the selection.
 */
public interface EditTab {

	/**
	 * title getter
	 * @return the title to be used by this tab
	 */
	public String getTitle();

	/**
	 * Component getter
	 * @return the component
	 */
	public Component getComponent();

	/**
	 * Test if active
	 * @param selection graph selection
	 * @return boolean if actif
	 */
	public boolean isActive( GraphSelection<?, ?> selection);

    default JPopupMenu getMenu() {
        return null;
    }

}
