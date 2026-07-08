package org.ginsim.service.tool.reg2dyn.limitedsimulation;

import java.util.Iterator;
import java.util.List;

import org.ginsim.core.graph.hierarchicaltransitiongraph.StatesSet;

/**
 * Indicates from a set of states and a strategy if a given sate should be added to the simulation.
 * 
 * @author Duncan Berenguier
 *
 */
public class SimulationConstraint {
	/**
	 * The simulation is limited to this set of states
	 */
	private StatesSet space;
	/**
	 * This strategy define if we should add the first states going out of the space
	 */
	private OutgoingNodesHandlingStrategy strategy;
	
	/**
	 * Create a new simulation constraint
	 * @param space the set of states used as an initial state.
	 * @param strategy the OutgoingNodesHandlingStrategy
	 */
	public SimulationConstraint(StatesSet space, OutgoingNodesHandlingStrategy strategy) {
		this.space = space;
		this.strategy = strategy;
	}
	
	/**
	 * Return a positive value if the state should be added to the graph.
	 * Return 1 if it is in the space
	 * Return 2 if it is a direct successor of the space
	 * @param state
	 * @param parentState
	 * @return a positive value if the state should be added to the graph.
	 */
	public int shouldAdd(byte[] state, byte[] parentState) {
		if (space.contains(state)) {
			return 1;
		} else if (strategy == OutgoingNodesHandlingStrategy.ADD_FIRST_OUTGOING_STATE && parentState != null && space.contains(parentState)) {
			return 2;
		}
		return 0;
	}
	
	/**
	 * Return a new iterator for the given state space.
	 * @return a new iterator for the given state space
	 */
	public Iterator<byte[]> getNewIterator() {
		List<byte[]> statesList = space.statesToFullList();
		return statesList.iterator();
	}

	/**
	 * Indicates if the space is valid for computation (not null and non empty)
	 * @return true if the space is valid for computation (not null and non empty)
	 */
	public boolean isValid() {
		return space != null && space.getSizeOrUpdate() > 0;
	}
}

