package org.ginsim.service.tool.avatar.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.io.avatar.AvatarUtils;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDManagerFactory;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.MDDVariableFactory;
import org.colomoto.mddlib.PathSearcher;
import org.colomoto.mddlib.internal.MDDStore;
import org.colomoto.mddlib.operators.MDDBaseOperators;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedState;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateList;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesHandler;
import org.ginsim.service.tool.avatar.domain.AbstractStateSet;
import org.ginsim.service.tool.avatar.domain.Dictionary;
import org.ginsim.service.tool.avatar.domain.MDDStateSet;
import org.ginsim.service.tool.avatar.domain.State;
import org.ginsim.service.tool.avatar.domain.StateSet;

/**
 * Facilities associated with simulation services. Including:<br>
 * 1) Random generation of an initial state from a given pattern or
 * state-set.<br>
 * 2) Inference of state patterns from a given set of states.<br>
 * 3) Generating a drawable graph from a complex attractor.<br>
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public final class MDDUtils {

	/**
	 * Maps an oracle (set of state patterns) into a set of specific states
	 * 
	 * @param oracle
	 *            the oracle (set of state patterns) to be unfolded
	 * @param nodes
	 *            knowledge of the states of the components within a state pattern
	 * @return the set of specific states associated with the given state patterns
	 */
	public static StateSet toStateSet(List<byte[]> oracle, List<NodeInfo> nodes) {
		List<byte[]> all = new ArrayList<byte[]>();
		while (!oracle.isEmpty()) {
			byte[] state = oracle.remove(0);
			int index = firstMultiple(state);
			if (index == -1)
				all.add(state);
			else {
				for (int i = 0, l = nodes.get(index).getMax(); i <= l; i++) {
					byte[] newstate = Arrays.copyOf(state, state.length);
					newstate[index] = (byte) i;
					oracle.add(newstate);
				}
			}
		}
		return new StateSet(all);
	}

	private static int firstMultiple(byte[] state) {
		for (int i = 0, l = state.length; i < l; i++)
			if (state[i] == -1)
				return i;
		return -1;
	}

	/**
	 * Returns a list of states given a handler for NamedStates and the ordered set
	 * of components
	 * 
	 * @param states
	 *            states handler containing a named list of normal and input states
	 * @param nodes
	 *            list of components
	 * @return the list of states represented as byte arrays from the inputted
	 *         handler
	 */
	public static List<byte[]> getStates(NamedStatesHandler states, List<NodeInfo> nodes) {
		List<byte[]> result = new ArrayList<byte[]>();
		if (states.getInitialStates().size() == 0) {
			for (NamedState state2 : states.getInputConfigs()) {
				byte[] state = new byte[nodes.size()];
				for (int i = 0, l = nodes.size(); i < l; i++) {
					NodeInfo node = nodes.get(i);
					List<Integer> values = node.isInput() ? state2.getMap().get(node) : null;
					if (values == null || values.size() > 1)
						state[i] = -1;
					else
						state[i] = (byte) ((int) values.get(0));
				}
				result.add(state);
			}
		} else {
			for (NamedState state1 : states.getInitialStates()) {
				if (states.getInputConfigs().size() == 0) {
					byte[] state = new byte[nodes.size()];
					for (int i = 0, l = nodes.size(); i < l; i++) {
						NodeInfo node = nodes.get(i);
						List<Integer> values = state1.getMap().get(node);
						if (values == null || values.size() > 1)
							state[i] = -1;
						else
							state[i] = (byte) ((int) values.get(0));
					}
					result.add(state);
				}
				for (NamedState state2 : states.getInputConfigs()) {
					byte[] state = new byte[nodes.size()];
					for (int i = 0, l = nodes.size(); i < l; i++) {
						NodeInfo node = nodes.get(i);
						List<Integer> values = node.isInput() ? state2.getMap().get(node) : state1.getMap().get(node);
						if (values == null || values.size() > 1)
							state[i] = -1;
						else
							state[i] = (byte) ((int) values.get(0));
					}
					result.add(state);
				}
			}
		}
		return result;
	}

	/**
	 * Infers a set of state patterns from a given set of states
	 * 
	 * @param vars list of node info
	 *            the logical model to facilitate the generation of the MDD for the
	 *            extraction of pattern
	 * @param stateset
	 *            the set of states to be compacted
	 * @return the set of state patterns associated with the given states
	 */
	public static List<byte[]> getStatePatterns(List<NodeInfo> vars, AbstractStateSet stateset) {
		MDDVariableFactory mvf = new MDDVariableFactory();
		for (NodeInfo ni : vars)
			mvf.add(ni, (byte) (ni.getMax() + 1));
		List<Set<Integer>> statesPerComponent = new ArrayList<Set<Integer>>();
		for (int i = 0, l = vars.size(); i < l; i++)
			statesPerComponent.add(new HashSet<Integer>());
		MDDManager ddmanager = MDDManagerFactory.getManager(mvf, 10);

		List<byte[]> states = new ArrayList<byte[]>();
		if (stateset instanceof StateSet)
			for (State s : ((StateSet) stateset).getStates())
				states.add(s.state);
		else if (stateset instanceof MDDStateSet)
			for (State s : ((MDDStateSet) stateset).getStates())
				states.add(s.state);
		int mdd = ddmanager.nodeFromStates(states, 1);

		PathSearcher searcher = new PathSearcher(ddmanager, 1);
		int[] path = searcher.setNode(mdd);
		List<byte[]> result = new ArrayList<byte[]>();
		for (int p : searcher)
			result.add(AvatarUtils.toByteArray(path));
		// for(byte[] s : result) System.out.println(">>"+AvatarUtils.toString(s));
		return result;
	}

	// mdd = getMDD(states.get(1),ddmanager);
	/*
	 * for(State s : stateset.getStates()){ if(mdd==0)
	 * mdd=getMDD(s.state,ddmanager); else mdd = MDDBaseOperators.OR.combine(
	 * ddmanager, mdd, getMDD(s.state,ddmanager)); for(int i=0, l=vars.size(); i<l;
	 * i++) statesPerComponent.get(i).add((int)s.state[i]); }
	 */

	private static int getMDD(byte[] state, MDDManager ddmanager) {
		return ddmanager.nodeFromState(state, 1);
		/*
		 * int mdd=1, i=0; for(MDDVariable var : ddmanager.getAllVariables()){ byte
		 * s=state[i++]; if(s==-1) continue; if(var.nbval>2){ int[] children =
		 * AvatarUtils.getChildrenWithSingleNode(var.nbval, s); mdd =
		 * MDDBaseOperators.AND.combine(ddmanager,mdd,var.getNode(children)); } else mdd
		 * = MDDBaseOperators.AND.combine(ddmanager,mdd,s==0 ? var.getNode(1,0) :
		 * var.getNode(0,1)); } return mdd;
		 */
	}

	public static boolean contained(NamedStateList oracles, List<byte[]> oracle) {
		int nstates = oracles.getNodeOrder().size();
		for (NamedState o : oracles) {
			byte[] state = new byte[nstates];
			for (int i = 0; i < nstates; i++) {
				RegulatoryNode node = (RegulatoryNode) oracles.getNodeOrder().get(i);
				List<Integer> values = o.getMap().get(node);
				if (values == null || values.size() > 1)
					state[i] = -1;
				else
					state[i] = (byte) ((int) values.get(0));
			}
			if (equals(state, oracle.get(0)))
				return true;
		}
		return false;
	}

	private static boolean equals(byte[] s1, byte[] s2) {
//		System.out.println(AvatarUtils.toString(s1) + "<->" + AvatarUtils.toString(s2));
		for (int i = 0, l = s1.length; i < l; i++)
			if (s1[i] != s2[i])
				return false;
		return true;
	}
}
