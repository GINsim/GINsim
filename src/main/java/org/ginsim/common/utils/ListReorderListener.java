package org.ginsim.common.utils;

public interface ListReorderListener {

	/**
	 * Re ordered function
	 * @param mapping array int
	 */
	void reordered(int[] mapping);

	/**
	 * delete function
	 * @param sel  array int
	 */
	void deleted(int[] sel);
	
}
