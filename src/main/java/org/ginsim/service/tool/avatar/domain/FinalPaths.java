package org.ginsim.service.tool.avatar.domain;

import java.util.Collection;
import java.util.Map;

import org.colomoto.biolqm.io.avatar.AvatarLogicalModelException;

/**
 * Interface for the storage transitions within and going-out of a state-set<br>
 * Transitions can be represented in multiple ways (e.g. sparse structures, arrays, etc.)<br>
 * Classes implementing this interface should be defined accordingly
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public interface FinalPaths {

	/**
	 * Creates transitions with uniform probability from a given cycle to its exits
	 * @param states the states within the cycle
	 * @param exits the states accessible from the states within the cycle
	 * @param prob the uniform probability of the transitions between the all the cycle states and exit states
	 */
	public void addOutputPaths(Collection<String> states, Collection<String> exits, double prob);
	
	/**
	 * Inserts transitions from a given cycle to its exits
	 * @param states the states within the cycle
	 * @param exits the states accessible from the states within the cycle
	 * @param probs a matrix specifying the probabilities of the transitions from the all the states of the cycle towards all the exit states
	 * @throws AvatarLogicalModelException  exception
	 */
	public void addOutputPaths(Collection<String> states, Collection<String> exits, double[][] probs) throws AvatarLogicalModelException;
	
	/**
	 * Returns the transitions to reachable exit states from a given state in the cycle
	 * @param key the state in the cycle whose exits are to be computed
	 * @return the transitions to reachable exit states from a given state (the transitions maintain the keys of the exit states and the associated probability to reach them)
	 */
	public Map<String, Double> getPaths(String key);
	
}
