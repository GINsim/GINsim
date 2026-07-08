package org.ginsim.service.tool.reg2dyn.htg;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNode;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNodeSet;


/**
 * Used to represent the elements in the queue of the HTGSimulation
 */
public class HTGSimulationQueueState implements HTGSimulationQueueItem  {

	/**
	 * The state itself
	 */
	private byte[] state;
	/**
	 * It's associated index. The k-th state to be discovered, will have its index = k. See Tarjan Algorithm's
	 */
	private int index;
	/**
	 * Its associated lowindex. See Tarjan Algorithm's 
	 */
	private int low_index;
	/**
	 * Indicates if it is in a cycle
	 */
	private HTGSimulationQueueSCC inCycle = null;
	
	/**
	 * Simple constructor.
	 * @param state the state
	 * @param index  the index
	 * @param low_index the low index
	 */
	public HTGSimulationQueueState(byte[] state, int index, int low_index) {
		this.setState(state);
		this.setIndex(index);
		this.setLow_index(low_index);
	}

	@Override
	public String toString() {
		return "["+printStateToString(getState())+", i:"+getIndex()+", li:"+getLow_index()+"]";
	}
	
	private static String printStateToString(byte[] t) {
		StringBuffer s = new StringBuffer();
		for (int i = 0 ; i < t.length ; i++){
			s.append(""+t[i]);
		}
		return s.toString();
	}
	

	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public void setLow_index(int low_index) {
		this.low_index = low_index;
	}

	@Override
	public int getLow_index() {
		return low_index;
	}

	public void setState(byte[] state) {
		this.state = state;
	}

	public byte[] getState() {
		return state;
	}

	@Override
	public boolean containsState(byte[] state) {
		return Arrays.equals(this.state, state);
	}

	@Override
	public boolean isCycle() {
		return false;
	}

	/**
	 * Set the SCC that contains the state. 
	 * 
	 * Note it could be a discarded SCC after a merge.
	 * 
	 * @param newCycleItem
	 */
	public void setInCycle(HTGSimulationQueueSCC newCycleItem) {
		this.inCycle  = newCycleItem;
	}
	/**
	 * Retrieve the real SCC containing the state.
	 * @param nodeSet
	 * @param queue
	 * @return
	 */
	public HierarchicalNode getInCycle(HierarchicalNodeSet nodeSet, List queue) {
		if (inCycle == null) return null;
		HierarchicalNode n = nodeSet.getHNodeForState(state);
		if (n != null) return n;
		for (ListIterator it = queue.listIterator(queue.size()); it.hasPrevious();) {
			HTGSimulationQueueItem e = (HTGSimulationQueueItem) it.previous();
			if (e.isCycle() && e.containsState(state)) return ((HTGSimulationQueueSCC) e).getSCC();
		}
		return inCycle.getSCC();
	}
}
