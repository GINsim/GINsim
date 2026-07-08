package org.ginsim.core.utils.data;

public interface GenericListListener {

	/**
	 * Added function
	 * @param item object
	 * @param pos position
	 */
	public void itemAdded(Object item, int pos);

	/**
	 * Removed function
	 * @param item object
	 * @param pos position
	 */
	public void itemRemoved(Object item, int pos);

	/**
	 * content chnged function
	 */
	public void contentChanged();

	/**
	 * structureChange function
	 */
	public void structureChanged();
}
