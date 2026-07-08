package org.ginsim.service.tool.reg2dyn;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.LogicalModel;
import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.Txt;
import org.ginsim.common.callable.ProgressListener;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesIterator;
import org.ginsim.service.tool.reg2dyn.helpers.STGSimulationHelper;
import org.ginsim.service.tool.reg2dyn.helpers.SimulationHelper;
import org.ginsim.service.tool.reg2dyn.updater.SimulationUpdater;



/**
 * This is the main part of the simulation. It supports pluggable backends
 * to generate the following states and to switch from building a full
 * state transition graph to a "simple" reachability set.
 */
public class Simulation implements Runnable {

	protected LinkedList queue = new LinkedList(); // exploration queue

	protected final ProgressListener<Graph> frame;
	protected int maxnodes, maxdepth;
	protected Iterator<byte[]> initStatesIterator;
	protected SimulationHelper helper;
	protected SimulationUpdater updater;

	protected boolean breadthFirst = false;
	public int nbnode = 0;
	protected boolean ready = false;

	/**
	 * Constructs an empty dynamic graph
	 *
	 * @param model the logical model on which we are working
	 * @param frame
	 * @param params
	 */
    public Simulation(LogicalModel model, ProgressListener<Graph> frame, SimulationParameters params) {
    	this.frame = frame;
		this.maxdepth = params.maxdepth;
		this.maxnodes = params.maxnodes;

		if (params.strategy == SimulationStrategy.STG) {
			helper = new STGSimulationHelper(model, params);
		}
		breadthFirst = params.breadthFirst;
		// TODO: fully remove regulatory graph from here
   		updater = params.getUpdatingMode().getUpdater(model);
   		set_initialStates(model.getComponents(), params.m_input, params.m_initState);
    }

    public void set_initialStates(List nodeOrder, Map inputs, Map m_initState) {
        setInitStatesIterator(new NamedStatesIterator(nodeOrder, inputs, m_initState));
    }

    public void interrupt() {
		ready = false;
	}

	/**
	 * Create and initialize a SimulationUpdater for a given __state__.
	 * @param state
	 * @return
	 */
	public SimulationUpdater getUpdaterForState(byte[] state) {
   		return updater.cloneForState(state);
	}


    /**
     * run the simulation in a new thread.
     */
    public void run() {
    	
    	try{
    		frame.setResult( do_simulation());
    	}
    	catch ( GsException ge) {
    		GUIMessageUtils.openErrorDialog( "Unable to launch the simulation");
    		LogManager.error( "Unable to start Simulation");
		}
    }

	public Graph do_simulation() throws GsException {
        ready = true;
		boolean maxDepthReached = false;
		try {
			// iterate through initial states and run the simulation from each of them
			while(getInitStatesIterator().hasNext()) {
				// add the next proposed state
				queue.add(new SimulationQueuedState((byte[])getInitStatesIterator().next(), 0, null, false));
				
				// do the simulation itself
				while (!queue.isEmpty()) {
					SimulationQueuedState item = (SimulationQueuedState)(
							breadthFirst ? queue.removeFirst() 
										: queue.removeLast());

					if (helper.addNode(item)) {
						// this is a new node, increase node count, do some checks and so on
						nbnode++;
						if (nbnode % 100 == 0) {
						    if (frame != null) {
				                frame.setProgress(nbnode);
				            }
						}
						if (maxnodes != 0 && nbnode >= maxnodes){
							LogManager.error( "Maxnodes reached: " + maxnodes);
						    throw new GsException(GsException.GRAVITY_NORMAL, (String)null);
						}

//						// stop if it has been asked or if memory becomes unsufficient
//						if (ready && Runtime.getRuntime().freeMemory() < 5000) {
//							Runtime.getRuntime().gc();
//							if (Runtime.getRuntime().freeMemory() > 40000 ) {
//								System.out.println("out of memory: saved by garbage collector: "+nbgc);
//							} else {
//								GsEnv.error("out of memory, I'll stop to prevent loosing everything", null);
//								System.out.println("not ready anymore!!");
//								ready = false;
//							}
//						}
						if (!ready) {
						    throw new GsException(GsException.GRAVITY_NORMAL, Txt.t("STR_interrupted"));
						}

						// run the simulation on the new node
						updater.setState(item.state, item.depth, helper.getNode());
						if (!updater.hasNext()) {
							helper.setStable();
							frame.milestone(item);
						} else {
							if (maxdepth == 0 || item.depth < maxdepth) {
								while (updater.hasNext()) {
									queue.addLast(updater.next());
								}
							} else {
								maxDepthReached = true;
							}
						}
					}
				}
			}
		} catch (GsException e) {
			LogManager.error( "Simulation was interrupted");
		} catch (OutOfMemoryError e) {
			LogManager.error( "Out of Memory");
		    GUIMessageUtils.openErrorDialog("Out Of Memory");
		    return null;
		} finally {
			if (maxDepthReached) {
				GUIMessageUtils.openErrorDialog("Reached the max depth");
				//TODO: explain what happened and give some hints
			}
		}
		return helper.endSimulation();
	}

	/**
	 * @return the initStatesIterator
	 */
	public Iterator<byte[]> getInitStatesIterator() {
		return initStatesIterator;
	}

	/**
	 * @param initStatesIterator the initStatesIterator to set
	 */
	public void setInitStatesIterator(Iterator<byte[]> initStatesIterator) {
		this.initStatesIterator = initStatesIterator;
	}
}
