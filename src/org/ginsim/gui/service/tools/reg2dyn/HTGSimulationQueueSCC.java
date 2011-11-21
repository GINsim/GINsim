package org.ginsim.gui.service.tools.reg2dyn;

import org.ginsim.graph.hierachicaltransitiongraph.HierarchicalNode;

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
	 * @param state
	 * @param index
	 * @param low_index
	 * @param updater
	 */
	public HTGSimulationQueueSCC(HierarchicalNode scc, int index, int low_index) {
		this.setSCC(scc);
		this.setIndex(index);
		this.setLow_index(low_index);
	}

	/* (non-Javadoc)
	 * @see org.ginsim.gui.service.tools.reg2dyn.HTGSimulationQueueItem#toString()
	 */
	public String toString() {
		return "["+scc.toString()+", i:"+getIndex()+", li:"+getLow_index()+"]";
	}
		
	/* (non-Javadoc)
	 * @see org.ginsim.gui.service.tools.reg2dyn.HTGSimulationQueueItem#setIndex(int)
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/* (non-Javadoc)
	 * @see org.ginsim.gui.service.tools.reg2dyn.HTGSimulationQueueItem#getIndex()
	 */
	public int getIndex() {
		return index;
	}

	/* (non-Javadoc)
	 * @see org.ginsim.gui.service.tools.reg2dyn.HTGSimulationQueueItem#setLow_index(int)
	 */
	public void setLow_index(int low_index) {
		this.low_index = low_index;
	}

	/* (non-Javadoc)
	 * @see org.ginsim.gui.service.tools.reg2dyn.HTGSimulationQueueItem#getLow_index()
	 */
	public int getLow_index() {
		return low_index;
	}

	/* (non-Javadoc)
	 * @see org.ginsim.gui.service.tools.reg2dyn.HTGSimulationQueueItem#setState(byte[])
	 */
	public void setSCC(HierarchicalNode scc) {
		this.scc = scc;
	}

	/* (non-Javadoc)
	 * @see org.ginsim.gui.service.tools.reg2dyn.HTGSimulationQueueItem#getState()
	 */
	public HierarchicalNode getSCC() {
		return scc;
	}

	/* (non-Javadoc)
	 * @see org.ginsim.gui.service.tools.reg2dyn.HTGSimulationQueueItem#containsState(byte[])
	 */
	public boolean containsState(byte[] state) {
		return scc.contains(state);
	}

	public boolean isCycle() {
		return true;
	}
}
