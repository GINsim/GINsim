package org.ginsim.service.tool.avatar.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.io.avatar.AvatarUtils;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDManagerFactory;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.MDDVariableFactory;
import org.colomoto.mddlib.PathSearcher;

/**
 * Representation of a state-set recurring to a compact set of patterns defining the internal states
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public class CompactStateSet extends AbstractStateSet {

	private List<byte[]> states = new ArrayList<byte[]>();
	private MDDManager mdd;
	private int mddNode = 0;

	/**
	 * Creates an empty state-set
	 */
	public CompactStateSet(List<NodeInfo> vars){
		MDDVariableFactory mvf = new MDDVariableFactory();
		for(NodeInfo ni : vars) mvf.add(ni,(byte)(ni.getMax()+1));
		mdd = MDDManagerFactory.getManager(mvf,10);
	}

	/**
	 * Creates a state-set with the given list of states and their probability of occurrence
	 * @param hashkey key identifying the state-set (state-sets with the same states share the same key)
	 * @param vars lists the components
	 * @param stateset of states to be compacted
	 */
	public CompactStateSet(String hashkey, List<NodeInfo> vars, AbstractStateSet stateset) {
		this(vars);
		setKey(hashkey);
		List<byte[]> noncompactstates = new ArrayList<byte[]>();
		if(stateset instanceof StateSet)
			for(State s : ((StateSet)stateset).getStates()) noncompactstates.add(s.state);
		else if(stateset instanceof MDDStateSet)
			for(State s : ((MDDStateSet)stateset).getStates()) noncompactstates.add(s.state);
		initialize(noncompactstates);
	}

	/**
	 * Creates a state-set with the given list of states and their probability of occurrence
	 * @param hashkey key identifying the state-set (state-sets with the same states share the same key)
	 * @param vars lists the components
	 * @param noncompactstates  a list of patterns representing the states of a given state set<br>
	 *        &emsp;&emsp;a pattern is represented as a byte array, where each position defines the possible states of a component (according to a well defined-order)<br>
	 *        &emsp;&emsp;when all the states of a given component are allowed to occur, the associated position in byte array is specified as -1
	 */
	public CompactStateSet(String hashkey, List<NodeInfo> vars, List<byte[]> noncompactstates) {
		this(vars);
		initialize(noncompactstates);
	}

	private void initialize(List<byte[]> noncompactstates) {
		mddNode = mdd.nodeFromStates(noncompactstates,1);
		PathSearcher searcher = new PathSearcher(mdd,1);
		int[] path = searcher.setNode(mddNode);
		for (@SuppressWarnings("unused") int p : searcher) states.add(AvatarUtils.toByteArray(path));
	}

	/**
	 * Accesses the states in the state-set
	 * @return states in the state-set
	 */
	public List<byte[]> getStates(){
		return states;
	}
	
	/**
	 * Checks whether there are states in the state-set
	 * @return true if the state-set has no states
	 */
	public boolean isEmpty() { 
		return states.isEmpty(); 
	}
	
	@Override
	public boolean contains(State state) {
		return (mdd.reach(mddNode, state.state)>0);
		/*byte[] s1 = state.state;
		for(byte[] s2 : states)
			if(contains(s2,s1)) return true;
		return false;*/ 
	}
	private boolean contains(byte[] s2, byte[] s1) {
		for(int i=0, l=Math.min(s1.length,s2.length); i<l; i++)
			if(s2[i]!=-1 && s1[i]!=s2[i]) return false;
		return true;
	}
	
	@Override
	public String toString() { 
		return AvatarUtils.toString(states); 
	}

	@Override
	public int size() {
		int total = 0;
		MDDVariable[] vars = mdd.getAllVariables();
		for(byte[] s : states){
			int sum = 1;
			for(int i=0, l=s.length; i<l; i++) 
				if(s[i]==-1) sum *= vars[i].nbval;
			total += sum;
		}
		return total;
	}

	@Override
	public Collection<String> getKeys() {
		return null;
	}
}
