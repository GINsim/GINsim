package org.ginsim.gui.utils.data;


public interface ObjectPropertyEditorUI {
	/**
	 * refresh
	 * @param force boolean
	 */
	void refresh(boolean force);

	/**
	 * Apply function
	 */
	void apply();

	/**
	 * Edit property setter
	 * @param pinfo  property info
	 * @param panel property holder
	 */
	void setEditedProperty(GenericPropertyInfo pinfo, GenericPropertyHolder panel);

	/**
	 * Releas function
	 */
	void release();
}
