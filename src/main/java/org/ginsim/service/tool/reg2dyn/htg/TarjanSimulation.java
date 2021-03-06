package org.ginsim.service.tool.reg2dyn.htg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.Txt;
import org.ginsim.common.callable.ProgressListener;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNode;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNodeSet;
import org.ginsim.service.tool.reg2dyn.SimulationQueuedState;
import org.ginsim.service.tool.reg2dyn.updater.SimulationUpdater;




/**
 * 
 * Run a simulation to construct a HierarchicalTransitionGraph
 * 
 *
 */
public class TarjanSimulation {
	protected LinkedList<HTGSimulationQueueState> queue = new LinkedList<HTGSimulationQueueState>(); // exploration queue

	/**
	 * The index of the next state found during the dfs.
	 */
	protected int index;
	/**
	 * The current depth in the dfs. Used mainly for the simulation limit's.
	 */
	protected int depth;
	/**
	 * The current maximal depth of the dfs of the current initial state.
	 */
	protected int max_depth_reached;
	protected long lastDraw = 0;
	protected int nbinitialstates = 0;
	protected int step = 0;
	
	private HTGSimulation htgSimulation;
	
	private Map<SimpleState, Integer> indexesMap;
	private Map<SimpleState, HierarchicalNode> state2sccMap;

	private int nbnode;

	private final ProgressListener<Graph> frame;
	
	private long queueSearchCount = 0;
	protected Map<SimpleState, HTGSimulationQueueState> inQueue; 
	
	private SimpleState s_e = null; // for explore

	public TarjanSimulation(HTGSimulation htgSimulation, ProgressListener<Graph> frame) {
		this.htgSimulation = htgSimulation;
		this.frame = frame;
		nbnode = 0;
		this.indexesMap = new HashMap<SimpleState, Integer>();
		this.state2sccMap = new HashMap<SimpleState, HierarchicalNode>();
		this.inQueue = new HashMap<SimpleState, HTGSimulationQueueState>();
	}




	/* ****************** DEBUG AND LogManager.log STUFF**********/

	/**
	 * The main entrance for the algorithm. Basically, initialize the main variables, and run the recursive explore function on every initial states
	 *
	 * <pre> index = 0
	 * For each initial state __state__
	 *     If __state__ is not in __nodeSet__, that is is not already processed
	 *         If __state__ has no successors
	 *             It is a stable state; continue
	 *         Else
	 *             Explore({__state__, index, index})
	 * </pre>
	 */
	protected void runSimulationOnInitialStates() throws Exception {
		htgSimulation.nodeSet = new HierarchicalNodeSet();
		htgSimulation.childsCount = htgSimulation.htg.getChildsCount();
		index = 0;
		max_depth_reached = -1;
		Iterator<byte[]> initStatesIterator = htgSimulation.getInitStatesIterator();
		while(initStatesIterator.hasNext()) { 					//For each initial states
			byte[] state = initStatesIterator.next();			//  __state__ is the current initial state.
			nbinitialstates++;
			                                                                                        						
			
			if (indexesMap.get(new SimpleState(state)) == null) {
									//  If the new state was not in the nodeSet, that is has not been processed
				SimulationUpdater updater = htgSimulation.getUpdaterForState(state);//    Get the updater of the state
				if (!updater.hasNext()) {                                           //    If it has no successor
					processStableState(state);                                  //      Process it as a stable state
					continue;                                                                                                     
				}                                                                                                                 
				HTGSimulationQueueState e = new HTGSimulationQueueState(state, index, index);
							//    Create __e__ a queue item with the state, index and updater
				depth = -1;             //    Set the depth to -1, (as it will be incremented immediatly to 0)
				explore(e, updater);    //    Call the recursive function explore() on __e__.
			}
		}
		htgSimulation.setMaxDepth(max_depth_reached);
		htgSimulation.setQueueSearchCount(queueSearchCount);
	}



	
	/**
	 * The recursive function of the algorithm.
	 *
	 */
	private void explore(HTGSimulationQueueState e, SimulationUpdater e_updater) throws Exception {
		checkStopConditions();
		index++;
		depth++;
		s_e = new SimpleState(e.getState());
		indexesMap.put(s_e, new Integer(index));
		queue.add(e);										//Queueing the current state
		inQueue.put(s_e, e);
		
		while (e_updater.hasNext()) {								//For each successors
			byte[] n_state= ((SimulationQueuedState)e_updater.next()).state;		// n_state is the state of the successor
			SimulationUpdater n_updater = htgSimulation.getUpdaterForState(n_state);                          							
			if (!n_updater.hasNext()) {							// n_state has no child No child => stable state
				processStableState(n_state);
			} else {
				HTGSimulationQueueItem n = getTripletInQueueForState(n_state);		//Search the state in the queue
				if (n != null) {							//If found
					e.setLow_index(Math.min(e.getLow_index(), n.getLow_index()));	//  update the index
				} else {								//Else the state is not in the queue
					if (indexesMap.get(new SimpleState(n_state)) == null) {
						n = new HTGSimulationQueueState(n_state, index, index);	//     explore it
						explore((HTGSimulationQueueState) n, n_updater);	//     update the index
						e.setLow_index(Math.min(e.getLow_index(), n.getLow_index()));
					}
				}
			}
		}
		if (e.getIndex() == e.getLow_index()) {
			buildSCC(e);
			nbnode++;
		}
	}
	
	
	
	/**
	 * A back edge in the queue has been found, and it points to __stopItemInQueue__.
	 * All the QueueItems in the queue up to __stopItemInQueue__ will be removed and merged into a single SCC, which is added to the queue.
	 * @param stopItemInQueue the target of the back edge
	 * @param index the current index
	 * @param low_index the current low index
	 * @return
	 */
	private HierarchicalNode cycleFound(HTGSimulationQueueItem stopItemInQueue, int index, int low_index) {
		HierarchicalNode cycle = new HierarchicalNode(htgSimulation.htg);
		cycle.setType(HierarchicalNode.TYPE_TRANSIENT_COMPONENT);
		HTGSimulationQueueItem n;
		do {
			n = queue.removeLast();
			inQueue.remove(new SimpleState(((HTGSimulationQueueState)n).getState()));
			if (n.getLow_index() < low_index) low_index = n.getLow_index();
			cycle.addState(((HTGSimulationQueueState)n).getState(), 1);
			state2sccMap.put(new SimpleState(((HTGSimulationQueueState)n).getState()), cycle);
		} while (!n.equals(stopItemInQueue));
		return cycle;
	}
	
	/**
	 * Called when a head of a SCC is found on the stateItem __e__, to construct the new GSHierarchicalNode.
	 * If __e__ is not already in a HierarchicalNode (because it is in a cycle), it is added into a new HierarchicalNode.
	 * The GsHierarhicalNode __scc__, is added to the nodeSet, and its edges and its sigma is computed.
	 * @param e
	 * @return scc
	 */
	private HierarchicalNode buildSCC(HTGSimulationQueueItem e) {
		HierarchicalNode scc = cycleFound(e, e.getIndex(), e.getLow_index());
		htgSimulation.nodeSet.add(scc);

		scc.addAllTheStatesInQueue();
		scc.updateSize();
		boolean isTerminal = true;
		for (Iterator<byte[]> it = scc.statesSet.statesToFullList().iterator(); it.hasNext();) { //compute the edges and the sigma
			byte[] state = it.next();
			SimulationUpdater updater = htgSimulation.getUpdaterForState(state);						//    Get the updater of the state
			while (updater.hasNext()) {
				SimulationQueuedState successor = (SimulationQueuedState) updater.next();
				HierarchicalNode hnode = state2sccMap.get(new SimpleState(successor.state));
				if (!hnode.equals(scc)) {
					isTerminal = false;
					scc.addEdgeTo(hnode);
				}
			}
		}
		if (scc.getSize() > 1) {
			if (isTerminal) {
				scc.setType(HierarchicalNode.TYPE_TERMINAL_CYCLE);
			} else {
				scc.setType(HierarchicalNode.TYPE_TRANSIENT_CYCLE);
			}
		}
		return scc;
	}

	
	/**
	 * Called when __state__ is identified as a stable state, ie. has no successors.
	 * If the state is not already processed, ie. a HierarchicalNode exists, a new HierarchicalNode is created.
	 * 
	 * @param state
	 * @return the HierarchicalNode (newly created or already processed) of the stable state
	 */
	private HierarchicalNode processStableState(byte[] state) {
		HierarchicalNode hnode = state2sccMap.get(new SimpleState(state));								//  If it already processed (in the nodeSet)	
		if (hnode != null) {
			return hnode;
		}
		index++;
		hnode = new HierarchicalNode(htgSimulation.htg);
		hnode.addState(state, 1);
		hnode.setType(HierarchicalNode.TYPE_STABLE_STATE);
		htgSimulation.nodeSet.add(hnode);
		state2sccMap.put(new SimpleState(state), hnode);
		nbnode++;
		return hnode;
	}

	/**
	 * Search __state__ in the queue
	 * @param state
	 * @return
	 */
	private HTGSimulationQueueItem getTripletInQueueForState(byte[] state) {
		queueSearchCount++;
		return inQueue.get(new SimpleState(state));
	}


/* ****************** DEBUG AND LogManager.log STUFF**********/
	
	/**
	 * Throws an exception if any condition to stop the algorithm is reached (maxnode, maxdepth.
	 * 
	 * if max node reached
	 * if max depth reached
	 * if the Interrupt button has been pushed.
	 * 
	 * @throws GsException
	 */
	private void checkStopConditions() throws GsException { 
		if (htgSimulation.getMaxNodes() != 0 && nbnode >= htgSimulation.getMaxNodes()){
			LogManager.error("Simulation of the HTG : maxnodes reached @" + nbnode);
		    throw new GsException(GsException.GRAVITY_NORMAL, "Reached the maximum count of node");
		}
		if (htgSimulation.getMaxDepth() > 0 && depth >= htgSimulation.getMaxDepth()) {
		    throw new GsException(GsException.GRAVITY_NORMAL, "Reached the maximum depth");
		}
		if (!htgSimulation.getReady()) {
		    throw new GsException(GsException.GRAVITY_NORMAL, Txt.t("STR_interrupted"));
		}
		if (depth > max_depth_reached) max_depth_reached = depth;
	    if (frame != null) {
	    	if (System.currentTimeMillis() - lastDraw > 250) {
	    		frame.setProgress("init:"+nbinitialstates+", total:"+nbnode+", depth:"+depth+"/"+max_depth_reached);
	    		lastDraw = System.currentTimeMillis();
	    	}
		}
	}
}


class SimpleState {
	protected byte[] state;
	
	public SimpleState(byte[] state) {
		this.state = state;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SimpleState) || ((SimpleState)obj).state.length != state.length) return false;
		for (int i = 0; i < state.length; i++) {
			if (((SimpleState)obj).state[i] != state[i]) return false;  
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (int i = 0 ; i < state.length ; i++){
			s.append(""+state[i]);
		}
		return s.toString();
	}
	
	public int hashCode() {
		int r = 0, e = 1;
		
		for (int i = 0 ; i < state.length ; i++){
			r+=state[i]*e;
			e*=2;
		}
		return r;
	}

}

