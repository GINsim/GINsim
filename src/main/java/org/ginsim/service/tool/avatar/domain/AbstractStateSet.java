package org.ginsim.service.tool.avatar.domain;

import java.util.Collection;

/**
 * Abstract class enclosing a state-set<br>
 * State-sets can be implemented in multiple ways (e.g. lists/maps of states, MDDs, etc.)<br>
 * Subclasses can be defined accordingly<br>
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public abstract class AbstractStateSet {

	private String key;

	/**
	 * Checks whether a given state is contained in the state-set
	 * @param s state to be checked
	 * @return true if the given state is contained in the state-set
	 */
	public abstract boolean contains(State s);

	/**
	 * get the size
	 * @return the size
	 */
	public abstract int size();

	/**
	 * getter og keys collection
	 * @return collection of keys
	 */
	public abstract Collection<String> getKeys();
	
	/**
	 * Returns the unique identifier of the state-set
	 * If two state-sets have the same states they share the same key
	 * @return the unique identifier of the state-set
	 */
	public String getKey(){
		return key;
	}

	/**
	 * dsetter the key value
	 * @param _key the key value
	 */
	public void setKey(String _key){
		key = _key;
	}
}
