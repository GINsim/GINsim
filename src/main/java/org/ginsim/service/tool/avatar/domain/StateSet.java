package org.ginsim.service.tool.avatar.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.colomoto.biolqm.io.avatar.AvatarUtils;
import org.ginsim.service.tool.avatar.utils.AvaMath;

/**
 * Default representation of a state-set recurring to a hash-map of states<br>
 * States are efficiently stored and accessed using minimal keys
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public class StateSet extends AbstractStateSet {

	private Map<String,State> states = new HashMap<String,State>();
	private Random random = new Random(); 
	private StateSet exitStates = null;
	
	/**
	 * Creates an empty state-set
	 */
	public StateSet(){}

	/**
	 * Creates a state-set with the given state
	 * @param s state to be enclosed within the state-set
	 */
	public StateSet(State s) { 
		states.put(s.key,s); 
	}
	
	/**
	 * Creates a state-set with the given list of states
	 * @param successors states to be included in the new state-set
	 */
	public StateSet(List<byte[]> successors) { 
		this(successors,1); 
	}
	
	/**
	 * Creates a state-set with the given list of states and their probability of occurrence
	 * @param successors states to be included in the new state-set
	 * @param p probability associated with each of the given set of states (between 0 and 1)
	 */
	public StateSet(List<byte[]> successors, double p) {
		for(byte[] succ : successors){
			State s = new State(succ,p);
			if(states.containsKey(s.key)) s.probability+=states.get(s.key).probability; 
			states.put(s.key,s);
		}
	}
	
	/**
	 * Clones a given state-set
	 * @param _states the states to be included in the cloned state-set
	 */
	public StateSet(StateSet _states) {
		for(State s : _states.getStates())
			states.put(s.key,s);
	}
	
	/**
	 * Returns a state given its key
	 * @param sKey the key of the desirable state
	 * @return the state associated with a given key (null if there is no state in the state-set with the given key)
	 */
	public State getState(String sKey) {
		return states.get(sKey);
	}
	
	/* (non-Javadoc)
	 * @see org.ginsim.service.tool.avatar.domain.AbstractStateSet#getKey()
	 */
	/*public String getKey(){
		if(key!=null) return key;
		key = "";
		Set<String> keys = new TreeSet<String>();
		keys.addAll(states.keySet());
		for(String s : keys) key+=s+":";
		key = key.substring(0,key.length()-1);
		return key;
	}*/
	
	/**
	 * Adds a state to the given state-set
	 * @param s the state to be added
	 */
	public void add(State s) { 
		states.put(s.key,s); 
	}
	
	/**
	 * Adds a state to the state-set (whenever the given state is already present within the state-set, 
	 * the probability of the state to be inserted is added to the original state in the state-set)
	 * @param s the state to be added
	 */
	public void addCumulative(State s) {
		if(contains(s)) states.get(s.key).probability+=s.probability;
		else states.put(s.key, s);
	}
	
	/**
	 * Adds a set of states to the state-set
	 * @param set the set of states to be added
	 */
	public void addAll(StateSet set) {
		for(State s : set.states.values())
			if(!states.containsKey(s.key)) states.put(s.key,s);
	}
	
	/**
	 * Checks whether two state-sets share states
	 * @param set the state-set to be compared
	 * @return true if the two states share one or more states
	 */
	public boolean hasOverlapping(StateSet set) {
		for(State s : set.states.values())
			if(states.containsKey(s.key)) return true;
		return false;
	}
	
	/**
	 * Checks whether the state-set has states
	 * @return true if the state-set has one or more states
	 */
	public boolean isEmpty() { 
		return states.isEmpty(); 
	}
	
	@Override
	public boolean contains(State state) { 
		if(states == null) System.out.println("States are null");
		if(state == null) System.out.println("State is null");
		return states.containsKey(state.key); 
	}
	
	/**
	 * Checks whether a given state (based on its key) is in the state-set
	 * @param key identifier of the state to be checked
	 * @return true if the given state (based on its key) is in the state-set
	 */
	public boolean contains(String key) {
		return states.containsKey(key); 
	}
	
	/**
	 * Returns the number of states in the state-set
	 * @return number of states in the state-set
	 */
	public int size() { 
		return states.size(); 
	}
	
	/**
	 * Returns a collection with the states in the state-set
	 * @return states in the state-set
	 */
	public Collection<State> getStates() { 
		return states.values(); 
	}
	
	/**
	 * Returns a collection with the keys of the states in the state-set
	 * @return keys of the states in the state-set
	 */
	public Collection<String> getKeys() {
		return states.keySet();
	}
	
	/**
	 * Accesses the first state in the state-set (useful to iterate and remove)
	 * @return the first state in the state-set
	 */
	public State getFirstState(){
		for(State s : states.values()) return s;
		return null;
	}
	
	/**
	 * Selects a state assuming an uniform distribution of the probabilities of the states in the state-set
	 * @return a randomly selected state
	 */
	public State getUniformRandomState() {
		return (State) states.values().toArray()[random.nextInt(states.size())];
	}
	
	/**
	 * Selects a state by adequately considering the probabilities of the states in the state-set
	 * @return a probable state randomly selected according to the probabilities of the states in the state-set
	 */
	public State getProbableRandomState() {
		double total = totalProbability();
		double point = new Random().nextDouble()*total;
		double sum = 0;
		for(State s : states.values()){  
			sum+=s.probability;
			if(point<=sum) return s;
		}
		return getFirstState(); //never reached
	}
	
	/**
	 * Returns the probability of a given state in the state-set
	 * @param s the state whose probability is to be checked
	 * @return the probability of the given state in the state-set
	 */
	public double getProbability(State s) {
		if(states.containsKey(s.key)) return states.get(s.key).probability;
		return 0;
	}
	
	/**
	 * Sums the probability of all of the internal states
	 * @return
	 */
	public double totalProbability() {
		double sum = 0;
		for(State s : states.values()) sum+=s.probability;
		return sum;
	}
	
	/**
	 * Removes a given state from the state-set
	 * @param s the state to be removed from the state-set
	 */
	public void remove(State s) {
		states.remove(s.key);
	}
		
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if(size()==0) return "";
		String result = "";
		for(State s : states.values())
			result += AvatarUtils.toString(s.state)+",";
		return result.substring(0, result.length()-1);
	}
	
	/**
	 * Detailed description of the states in the state-set as a String object
	 * @return detailed description of the states in the state-set
	 */
	public String toLongString() { 
		return states.values().toString(); 
	}
	
	/******************************
	 **** CYCLES AS STATE-SETS **** 
	 ******************************/

	private FinalPaths paths;
	
	/**
	 * Stores the set of outgoing transitions from the current state-set  
	 * @param exit the set of outgoing transitions from the current state-set
	 */
	public void setProbPaths(FinalPaths exit) {
		paths = exit;
	}

	public boolean hasExits() {
		return exitStates != null && (exitStates.size()>0);
	}
	public boolean hasPaths() {
		return paths != null;
	}

	/**
	 * Returns the states accessible from the state-set
	 * @see setExitStates
	 * @return states accessible from the state-set
	 */
	public StateSet getExitStateSet() {
		return exitStates;
	}
	
	/**
	 * Stores the set of reachable states from the current state-set  
	 * @param exit the set of states accessible from the current state-set
	 */
	public void setExitStates(StateSet exit) {
		exitStates = exit;
	}
	
	/**
	 * Randomly accesses a probable exit state from a given state in the state-set
	 * @param s state in the state-set
	 * @return a probable exit state selected according to its reachability
	 */
	public State getProbableExitState(State s){
		if(paths == null) {
			//System.out.println("paths is null #exits:"+exitStates.size());
			return getExitStateSet().getProbableRandomState();
		}
		Map<String,Double> exits = paths.getPaths(s.key);
		double total = AvaMath.sum(exits.values());
		double point = new Random().nextDouble()*total;
		double sum = 0;
		for(String s1 : exits.keySet()){
			double v=exits.get(s1);
			sum+=v;
			if(!exitStates.contains(s1)) continue;
			if(point<sum) return new State(exitStates.getState(s1).state,s.probability*v);
		}
		//System.out.println("#exits:"+exitStates.size());
		/*never reached
		for(String s1 : exits.keySet()){
			if(exitStates.contains(s1)) 
				return new State(exitStates.getState(s1).state,s.probability*exits.get(s1));
		}*/
		//System.out.println("Warning: #"+exitStates.size());
		return getExitStateSet().getProbableRandomState();
	}	

}
