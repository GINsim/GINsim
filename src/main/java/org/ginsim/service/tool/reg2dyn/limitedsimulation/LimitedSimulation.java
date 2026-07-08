package org.ginsim.service.tool.reg2dyn.limitedsimulation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.colomoto.biolqm.LogicalModel;
import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.Txt;
import org.ginsim.common.callable.ProgressListener;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.dynamicgraph.DynamicNode;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNode;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalTransitionGraph;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;
import org.ginsim.service.tool.reg2dyn.SimulationQueuedState;
import org.ginsim.service.tool.reg2dyn.helpers.SimulationHelper;
import org.ginsim.service.tool.reg2dyn.updater.SimulationUpdater;

/**
 * This is the main part of the simulation. It supports pluggable backends
 * to generate the following states and to switch from building a full
 * state transition graph to a "simple" reachability set.
 */
public class LimitedSimulation implements Runnable {

	protected LinkedList<SimulationQueuedState> queue = new LinkedList<SimulationQueuedState>(); // exploration queue

	private ProgressListener<Graph> simulationManager;
	private boolean breadthFirst;
	private SimulationUpdater updater;
	private SimulationHelper helper;
	private SimulationConstraint constraint;
	private boolean ready;

	private int nbnode;

	private final HierarchicalTransitionGraph htg;

	/**
	 * Constructs an empty dynamic graph
	 *
	 * @param model             the logical model on which we are working
	 * @param htg               the hierarchical graph
	 * @param params
	 * @param simulationManager
	 */
	public LimitedSimulation(LogicalModel model, HierarchicalTransitionGraph htg, SimulationConstraint constraint,
			SimulationParameters params, ProgressListener<Graph> simulationManager) {
		this.htg = htg;
		this.simulationManager = simulationManager;
		this.breadthFirst = params.breadthFirst;
		this.nbnode = 0;

		helper = new STGLimitedSimulationHelper(model, htg, params, constraint);
		updater = params.getPriorityClassDefinition().getUpdater(model);
		this.constraint = constraint;
	}

	public void interrupt() {
		ready = false;
	}

	/**
	 * run the simulation in a new thread.
	 */
	public void run() {

		try {
			simulationManager.setResult(do_simulation());
			constructMapping(helper.getDynamicGraph());
		} catch (GsException ge) {
			GUIMessageUtils.openErrorDialog("Unable to launch the simulation");
			LogManager.error("Unable to start Simulation");
		}
	}

	private void constructMapping(Graph dynamicGraph) {
		HashMap<DynamicNode, HierarchicalNode> state2htg = (HashMap<DynamicNode, HierarchicalNode>) ObjectAssociationManager
				.getInstance().getObject(dynamicGraph, StatesToHierarchicalMappingManager.KEY, true);
		state2htg.clear();
		for (Iterator it = dynamicGraph.getNodes().iterator(); it.hasNext();) {
			DynamicNode state = (DynamicNode) it.next();
			state2htg.put(state, htg.getNodeForState(state.state));
		}
	}

	public Graph do_simulation() throws GsException {
		ready = true;
		try {
			Iterator<byte[]> initStatesIterator = constraint.getNewIterator();
			// iterate through initial states and run the simulation from each of them
			while (initStatesIterator.hasNext()) {
				// add the next proposed state
				queue.add(new SimulationQueuedState((byte[]) initStatesIterator.next(), 0, null, false));
			}
			// do the simulation itself
			while (!queue.isEmpty()) {
				SimulationQueuedState item = (SimulationQueuedState) (breadthFirst ? queue.removeFirst()
						: queue.removeLast());

				if (helper.addNode(item)) {
					// this is a new node, increase node count, do some checks and so on
					nbnode++;
					if (nbnode % 100 == 0) {
						if (simulationManager != null) {
							simulationManager.setProgress(nbnode);
						}
					}
					if (!ready) {
						throw new GsException(GsException.GRAVITY_NORMAL, Txt.t("STR_interrupted"));
					}

					// run the simulation on the new node
					updater.setState(item.state, item.depth, helper.getNode());
					if (!updater.hasNext()) {
						helper.setStable();
						simulationManager.milestone(item);
						String display = "";
						for (int i = 0; i < item.state.length; i++) {
							display += item.state[i] + " ";
						}
						display += "\n";
						LogManager.trace(display, false);
					} else {
						while (updater.hasNext()) {
							SimulationQueuedState next = (SimulationQueuedState) updater.next();
							if (this.constraint.shouldAdd(next.state, null) == 1) {
								// Only add state if it is contained in the limited space
								queue.addLast(next);
							}
						}
					}
				}
			}

		} catch (GsException e) {
			LogManager.error("Simulation was interrupted");
		} catch (OutOfMemoryError e) {
			LogManager.error("Out of Memory");
			GUIMessageUtils.openErrorDialog("Out Of Memory");
			return null;
		}
		return helper.endSimulation();
	}

}
