package org.ginsim.common.utils;

import java.util.ArrayList;
import java.util.Set;

/**
 * A sorted set, based on an ArrayList.
 * All it does is check if an item is already in the array before adding it again.
 * 
 * @author Aurelien Naldi
 *
 * @param <T>  element T
 */
public class ArraySet<T> extends ArrayList<T> implements Set<T> {

	public boolean add(T element) {
		if (contains(element)) {
			return false;
		}
		super.add(element);
		return true;
	}
}
