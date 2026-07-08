package org.ginsim.service.tool.avatar.params;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.colomoto.biolqm.NodeInfo;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedState;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateList;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateStore;

/**
 * List of named states that maintains the initial states and oracles for a given simulation
 * @author Rui Henriques
 * @version 1.0
 */
public class AvatarStateStore implements NamedStateStore {

	//enum StateType { Init, Input, Oracle }
	
	/** list of named states associated with non-input components */
	public NamedStateList nstates;
	/** list of named states associated with input components */
	public NamedStateList instates;
	/** list of named states associated with oracles */
	public NamedStateList oracles;
	
	private Map m_initStates, m_input, m_oracles;
	/**
	 * ListRegulatoryNode nodes,
	 */
	public List<RegulatoryNode> nodes,
	/**
	 * List RegulatoryNode inodes,
	 */
	inodes,
	/**
	 * List RegulatoryNode  allnodes
	 */
	allnodes;

	/**
	 * Creates a named state store given a set of states and the current graph
	 * @param initialstates the states to be stored (including input and non-input components)
	 * @param graph the regulatory graph
	 */
	public AvatarStateStore(List<byte[]> initialstates, RegulatoryGraph graph) {
		initialize(graph);
		populate(initialstates,null);
	}

	/**
	 * Constructor
	 * @param graph the graph RegulatoryGraph
	 */
	public AvatarStateStore(RegulatoryGraph graph) {
		initialize(graph);
	}
	
	/**
	 * Creates a named state store given a set of states from common and input components and the current graph
	 * @param initialstates list with the states of non-input components
	 * @param initialIStates list with the states of input components
	 * @param graph the regulatory graph
	 * @param _oracles  states list
	 */
	public AvatarStateStore(List<byte[]> initialstates, List<byte[]> initialIStates, List<byte[]> _oracles, RegulatoryGraph graph) {
		initialize(graph);
		for(byte[] istate : initialstates) populate(istate,false);
		for(byte[] istate : initialIStates) populate(istate,true);
		for(byte[] oracle : _oracles) populateOracle(oracle);
	}
	
	/**
	 * Creates a named state store given a set of states, their names and the current graph
	 * @param states list with the states of non-input components
	 * @param namestates list with the names of the previous states
	 * @param istates list with the states of input components
	 * @param inamestates list with the names of the previous states
	 * @param graph the regulatory graph
	 * @param _oracles states list
	 * @param onames    list of output names
	 */
	public AvatarStateStore(List<byte[]> states, String[] namestates, List<byte[]> istates, String[] inamestates, List<byte[]> _oracles, String[] onames, RegulatoryGraph graph) {
		this(states,istates,_oracles,graph);
		int i=0;
		for(NamedState s : nstates) s.setName(namestates[i++]);
		i=0;
		for(NamedState s : instates) s.setName(inamestates[i++]);
		i=0;
		for(NamedState s : oracles) s.setName(onames[i++]);
	}
	
	/**
	 * Creates a named state store based on node info and the list of named states for common and input components 
	 * @param _nodes ordered set with non-input components
	 * @param _inodes ordered set with input components
	 * @param _allnodes ordered set with all components
	 * @param _states the list of named states of non-input components
	 * @param _istates the list of named states of input components
	 * @param _oracles states liste
	 */
	public AvatarStateStore(List<RegulatoryNode> _nodes, List<RegulatoryNode> _inodes, List<RegulatoryNode> _allnodes, NamedStateList _states, NamedStateList _istates, NamedStateList _oracles) {
		nodes = _nodes;
		inodes = _inodes;
		allnodes = _allnodes;
		initialize();
		addStateList(_states,_istates,_oracles);
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 * @return cnew AvatarStateStore
	 */
	public AvatarStateStore clone() {
		return new AvatarStateStore(nodes,inodes,allnodes,nstates,instates,oracles);
	}
	
	private void initialize(RegulatoryGraph graph) {
		nodes = new ArrayList<RegulatoryNode>();
		inodes = new ArrayList<RegulatoryNode>();
		allnodes = graph.getNodeOrder();
		for(RegulatoryNode node : allnodes)
			if(node.isInput()) inodes.add(node);
			else nodes.add(node);
		initialize();
	}
	
	private void initialize(){
		nstates = new NamedStateList(nodes,false);
		instates = new NamedStateList(inodes,true);
		oracles = new NamedStateList(allnodes,true);
		m_initStates = new HashMap<NodeInfo, List<Integer>>();
		m_input = new HashMap<NodeInfo, List<Integer>>();
		m_oracles = new HashMap<NodeInfo, List<Integer>>();
	}
		
	/*private void populate(List<byte[]> oracle) {
		if(oracle != null){
			for(byte[] o : oracle){
				NamedState states = new NamedState(), istates = new NamedState();
				int[] state = new int[o.length];
				for(int i=0, l=o.length; i<l; i++) state[i]=o[i];
				states.setState(state, allnodes, false);
				if(name!=null) states.setName(name);
				m_oracles.putAll(states.getMap());
				oracles.add(states);
			}
		}
	}*/
	private void populate(List<byte[]> initialstates, String name) {
		if(initialstates != null){
		  for(byte[] initialstate : initialstates){
			NamedState states = new NamedState(), istates = new NamedState();
			int[] state = new int[initialstate.length];
			for(int i=0, l=initialstate.length; i<l; i++) state[i]=initialstate[i];
			states.setState(state, allnodes, false);
			istates.setState(state, allnodes, true);
			if(name!=null){
				states.setName(name);
				istates.setName(name);
			}
			m_initStates.putAll(states.getMap());
			nstates.add(states);
			m_input.putAll(istates.getMap());
			instates.add(istates);
		}
	  }
	}
	/*private List<NamedState> compact(NamedStateList states) {
		Set<String> keys = new HashSet<String>();
		List<NamedState> removals = new ArrayList<NamedState>();
		for(NamedState s : states){
			if(keys.contains(s.getMap().toString())) removals.add(s);
			else keys.add(s.getMap().toString());
		}
		for(NamedState s : removals) states.remove(s);
		return states;
	}*/
	
	private void populate(byte[] initialstate, boolean input) {
		NamedState states = new NamedState();
		int[] state = new int[initialstate.length];
		for(int i=0, l=initialstate.length; i<l; i++) state[i]=initialstate[i];
		if(input){
			states.setState(state, allnodes, true);
			m_input.putAll(states.getMap());
			instates.add(states);
		} else {
			states.setState(state, allnodes, false);
			m_initStates.putAll(states.getMap());
			nstates.add(states);
		}
	}
	private void populateOracle(byte[] oracle) {
		NamedState states = new NamedState();
		int[] state = new int[oracle.length];
		for(int i=0, l=oracle.length; i<l; i++) state[i]=oracle[i];
		states.setState(state, allnodes);
		m_oracles.putAll(states.getMap());
		oracles.add(states);
	}

	/**
	 * Adds an oracle as a set of named state-patterns
	 * @param oracle the list of named oracles as a map (name and the corresponding set of state-patterns)
	 *
	 */
	public void addOracle(Map<String,List<byte[]>> oracle) {
		if(oracle==null) return;
		Set<String> keyset = oracle.keySet(); 
		int index=-1;
		if(oracles!=null && oracles.size()>0){
			String lastName = oracles.get(oracles.size()-1).getName();
			index = Integer.valueOf(lastName.substring(lastName.indexOf("_")+1));
		}
		for(String key : keyset){
			index++;
			for(byte[] o : oracle.get(key)){
				NamedState states = new NamedState();
				int[] state = new int[o.length];
				for(int i=0, l=o.length; i<l; i++) state[i]=o[i];
				states.setStateAll(state, allnodes);
				states.setName("Att_"+index);
				m_oracles.putAll(states.getMap());
				oracles.add(states);
			}		
		}
	}

	/**
	 * Adds a list of named states of common and input components
	 * @param states the list of named states associated with non-input components
	 * @param istates the list of named states associated with input components
	 * @param _oracles name state list
	 */
	public void addStateList(NamedStateList states, NamedStateList istates, NamedStateList _oracles){
		for(NamedState state : states){
			m_initStates.putAll(state.getMap());
			nstates.add(state);
		}
		for(NamedState istate : istates){
			m_input.putAll(istate.getMap());
			instates.add(istate);
		}
		addOracle(_oracles);
	}

	/**
	 * add state list
	 * @param _oracles states list
	 */
	public void addOracle(NamedStateList _oracles) {
		if(_oracles!=null){
			for(NamedState oracle : _oracles){
				m_oracles.putAll(oracle.getMap());
				oracles.add(oracle);
			}
		}
	}

	/** (non-Javadoc)
	 * Init
	 * @return Map
	 * @see org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateStore#getInitialState()
	 */
	public Map getInitialState() {
		return m_initStates;
	}
	
	/** (non-Javadoc)
	 * @return map of input states
	 * @see org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateStore#getInputState()
	 */
	public Map getInputState() {
		return m_input;
	}

	/**
	 * Getter
	 * @return map of list states
	 */
	public Map getOracleState() {
		return m_oracles;
	}
	
	/**
	 * Gathers all the names of the stored states
	 * @param input true if the names of states associated with input components is to return (false otherwise)
	 * @return a list with the names with the stored states
	 */
	public List<String> getNames(boolean input) {
		List<String> names = new ArrayList<String>();
		int i=0;
		if(input){
			for(NamedState s : instates) 
				if(s.getName()==null) names.add("states_"+(i++));
				else names.add(s.getName());			
		} else {
			for(NamedState s : nstates) 
				if(s.getName()==null) names.add("states_"+(i++));
				else names.add(s.getName());
			}
		return names;
	}

	/**
	 * getter of Names list
	 * @return list of names
	 */
	public List<String> getOracleNames() {
		List<String> names = new ArrayList<String>();
		int i=0;
		for(NamedState s : oracles) 
			if(s.getName()==null) names.add("Att_"+(i++));
			else names.add(s.getName());			
		return names;
	}
}
