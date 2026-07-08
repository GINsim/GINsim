package org.ginsim.service.tool.reg2dyn.htg;

import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNode;

public class HTGSimulationQueueSCC implements HTGSimulationQueueItem {

	/**
	 * The state itself
	 */
	private HierarchicalNode scc;
	/**
	 * It's associated index. The k-th state to be discovered, will have its index = k. See Tarjan Algorithm's
	 */
	private int index;
	/**
	 * Its associated lowindex. See Tarjan Algorithm's 
	 */
	private int low_index;
	
	/**
	 * Simple constructor.
	 * @param index  index value
	 * @param low_index  low index value
	 * @param scc  HierarchicalNode
	 */
	public HTGSimulationQueueSCC(HierarchicalNode scc, int index, int low_index) {
		this.setSCC(scc);
		this.setIndex(index);
		this.setLow_index(low_index);
	}

	@Override
	public String toString() {
		return "["+scc.toString()+", i:"+getIndex()+", li:"+getLow_index()+"]";
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

	public void setSCC(HierarchicalNode scc) {
		this.scc = scc;
	}

	public HierarchicalNode getSCC() {
		return scc;
	}

	@Override
	public boolean containsState(byte[] state) {
		return scc.contains(state);
	}

	public boolean isCycle() {
		return true;
	}
}
