package org.ginsim.gui.graph;

import javax.swing.ImageIcon;

import org.ginsim.commongui.utils.ImageLoader;


/**
 * Minimalistic description of the available edit types for a Graph.
 * 
 * @author Aurelien Naldi
 */
public class EditAction {

	private final EditMode mode;
	private final String name;
	private final String icon;
	
	public EditAction(EditMode mode, String name) {
		this(mode, name, null);
	}
	public EditAction(EditMode mode, String name, String icon) {
		this.mode = mode;
		this.name = name;
		this.icon = icon;
	}
	
	/**
	 * Getter of edit mode
	 * @return the type of edit performed (to group nodes and edges related actions)
	 */
	public EditMode getMode() {
		return mode;
	}
	
	/**
	 * Name getter
	 * @return the name of this edit 
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Icon image getter
	 * @return the icon for this edit mode 
	 */
	public ImageIcon getIcon() {
		if (icon == null) {
			return null;
		}
		
		return ImageLoader.getImageIcon(icon);
	}
	
	/**
	 * Warn the edit action manager that this action was performed.
	 * The manager will revert to the edit action is unlocked.
	 * @param manager  EditActionManager manager
	 */
	public void performed(EditActionManager manager) {
		manager.actionPerformed(this);
	}
}
