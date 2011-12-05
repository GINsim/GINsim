package org.ginsim.servicegui.tool.reg2dyn;

import java.util.Iterator;

import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.mutant.RegulatoryMutantDef;
import org.ginsim.core.graph.regulatorygraph.omdd.OMDDNode;


/**
 * This is the part of the simulation in charge for generating the following states.
 *
 * supported methods are:
 * <ul>
 * 	<li>synchronous search: all changes at once, each state has at most one successor</li>
 * 	<li>asynchronous search: changes one by one, each state can have many successors</li>
 * 	<li>by priority class: genes are classed by priority groups.
 *  <ul>
 *      <li>only changes on the higher priority group(s) containing changing genes will be applied.</li>
 *      <li>if this concerns several groups, changes corresponding to each group will be applied asynchronously</li>
 *      <li>each group can be either synchronous or asynchronous, changes will be applied all at once or one by one, depending on the group!</li>
 *      <li>+1 and -1 transitions can be separated to create really fine-tuned groups</li>
 *  </ul></li>
 * </ul>
 */
abstract public class SimulationUpdater implements Iterator {
	protected OMDDNode[] t_tree;
	protected int length;
	protected byte[] cur_state;
	protected byte[] next = null;
	protected int depth;
	protected Object node;
	protected boolean multiple;
	
	public SimulationUpdater(RegulatoryGraph regGraph, SimulationParameters params) {
        t_tree = regGraph.getParametersForSimulation(true);
        RegulatoryMutantDef mutant = (RegulatoryMutantDef)params.store.getObject(SimulationParameters.MUTANT);
        if (mutant != null) {
            mutant.apply(t_tree, regGraph);
        }
		this.length = t_tree.length;
	}

	public SimulationUpdater(RegulatoryGraph regGraph, RegulatoryMutantDef mutant) {
        t_tree = regGraph.getParametersForSimulation(true);
        if (mutant != null) {
            mutant.apply(t_tree, regGraph);
        }
		this.length = t_tree.length;
	}

	public boolean hasNext() {
		return next != null;
	}
	public Object next() {
		SimulationQueuedState ret = new SimulationQueuedState(next, depth+1, node, multiple);
		multiple = false;
		doBuildNext();
		return ret;
	}
    public byte[] nextState() {
        byte[] ret = next;
        doBuildNext();
        return ret;
    }
	
	public void remove() {
	}

	/**
	 * set the state which should be used as basis.
	 * @param node 
	 * @param depth 
	 * @param state
	 */
	public void setState(byte[] state, int depth, Object node) {
		this.cur_state = state;
		this.depth = depth;
		this.node = node;
		multiple = false;
		doSetState();
	}
	
	abstract protected void doSetState();
	abstract protected void doBuildNext();
	
	/**
	 * get change step for a gene
	 *
	 * @param initState
	 * @param i index of the gene to test
	 * @return the direction in which the gene want to change: 0 for no change, 1 for increase and -1 for decrease
	 */
	protected int nodeChange(byte[] initState, int i) {
		byte curState = initState[i];
		byte nextState = t_tree[i].testStatus(initState);

		// now see if the node is willing to change it's state
		if (nextState > curState){
		    return 1;
		} else if (nextState < curState){
		    return -1;
		}
		return 0;
	}
	
    static public SimulationUpdater getInstance(RegulatoryGraph regGraph, SimulationParameters params) {
		PriorityClassDefinition pcdef = params.getPriorityClassDefinition();
		if (pcdef.getNbElements(null) < 2) {
			Reg2dynPriorityClass pc = (Reg2dynPriorityClass)pcdef.getElement(null,0);
			if (pc.getMode() == Reg2dynPriorityClass.SYNCHRONOUS) {
				return new SynchronousSimulationUpdater(regGraph, params);
			}
			return new AsynchronousSimulationUpdater(regGraph, params);
		}
		return new PrioritySimulationUpdater(regGraph, params);
	}
}


/* **************************** Asynchronous ***********************************/

class AsynchronousSimulationUpdater extends SimulationUpdater {

	int nextChange = -1;
	int nextUpdate;

	public AsynchronousSimulationUpdater(RegulatoryGraph regGraph, SimulationParameters params) {
		super(regGraph, params);
	}

	protected void doBuildNext() {
		if (nextChange == -1) {
			next = null;
			return;
		}
		next = (byte[])cur_state.clone();
		next[nextChange] += nextUpdate;
		int i = nextChange + 1;
		nextChange = -1;
		for ( ; i<length ; i++){
			int change = nodeChange(cur_state, i);
			if (change != 0) {
				nextChange = i;
				nextUpdate = change;
				break;
			}
		}
	}

	protected void doSetState() {
		next = (byte[])cur_state.clone();
		nextChange = -1;
		int i = 0;
		for ( ; i<length ; i++){
			int change = nodeChange(cur_state, i);
			if (change != 0) {
				nextChange = i;
				next[i] += change;
				break;
			}
		}
		if (nextChange == -1) {
			next = null;
			return;
		}
		i = nextChange + 1;
		nextChange = -1;
		for ( ; i<length ; i++){
			int change = nodeChange(cur_state, i);
			if (change != 0) {
				nextChange = i;
				nextUpdate = change;
				break;
			}
		}
	}
}


/* **************************** by priority ***********************************/

class PrioritySimulationUpdater extends SimulationUpdater {
	
	int[][] pclass;
    int[] classChangesList;
    int nextIndex;
    int curClass;
    int priority;

	
    public PrioritySimulationUpdater(RegulatoryGraph regGraph, SimulationParameters params) {
		super(regGraph, params);
		pclass = params.getPriorityClassDefinition().getPclass(params.nodeOrder);
	}

	protected void doBuildNext() {
		
		if (classChangesList != null && nextIndex < classChangesList.length) {
			// we are processing an asynchronous class, continue this
	        next = (byte[])cur_state.clone();
	        next[classChangesList[nextIndex++]] += classChangesList[nextIndex++];
	        return;
		}
		
		int change;
		int[] tclass;
        int[] tchanges = null;
		int p;
		classChangesList = null;
        while(++curClass < pclass.length && pclass[curClass][0] == priority) {
            tclass = pclass[curClass];
            p = 0;
            tchanges = new int[tclass.length*2-1];
            tchanges[p++] = tclass[1];
            for (int j=2 ; j<tclass.length ; j+=2) {
                change = nodeChange(cur_state, tclass[j]);
                if (change != 0 && change+tclass[j+1] != 0) {
                    tchanges[p++] = tclass[j];
                    tchanges[p++] = change;
                }
            }
            if (p>1) {
                // copy the useful part of tchanges in the returned array
                classChangesList = new int[p];
                for (int a=0 ; a<p ; a++) {
                    classChangesList[a] = tchanges[a];
                }
                break;
            }
        }
        
        /* *********************************************
         * build the next state from the list of changes
         */
        if (classChangesList == null) {
        	next = null;
        	return;
        }
        next = (byte[])cur_state.clone();
        nextIndex = 1;
        if (classChangesList[0] == Reg2dynPriorityClass.SYNCHRONOUS) {
        	for ( ; nextIndex<classChangesList.length ; nextIndex++) {
        		next[classChangesList[nextIndex++]] += classChangesList[nextIndex];
        	}
        	multiple = classChangesList.length > 1;
        } else {
        	next[classChangesList[1]] += classChangesList[2];
        	nextIndex = 3;
        }
	}

	protected void doSetState() {
		/* ****************************
		 *  build the list of changes
		 */
		classChangesList = null;
		priority = 0;
        int change = 0;
        boolean changed = false;
        curClass = 0;
        int j = 0;
        int[] tchanges = null;
        int[] tclass = null;

        // look for the first changing priority class
        for ( ; curClass<pclass.length ; curClass++) {
            tclass = pclass[curClass];

            for (j=2 ; j<tclass.length ; j+=2) {
                change = nodeChange(cur_state, tclass[j]);
                if (change != 0 && change+tclass[j+1] != 0) {
                    changed = true;
                    break;
                }
            }
            if (changed) {
                priority = tclass[0];
                break;
            }
        }
        // if something changes, analyse the rest of the corresponding priority class
        // AND of the next priority classes with the same priority
        if (changed && tclass != null) {
            int p = 0;
            tchanges = new int[tclass.length*2-1];
            tchanges[p++] = tclass[1];
            tchanges[p++] = tclass[j];
            tchanges[p++] = change;

            for (j+=2 ; j<tclass.length ; j+=2) {
                change = nodeChange(cur_state, tclass[j]);
                if (change != 0 && change+tclass[j+1] != 0) {
                    tchanges[p++] = tclass[j];
                    tchanges[p++] = change;
                }
            }

            // copy the usefull part of tchanges in the returned array
            classChangesList = new int[p];
            for (int a=0 ; a<p ; a++) {
                classChangesList[a] = tchanges[a];
            }
        }
        
        /* *********************************************
         * build the next state from the list of changes
         */
        if (classChangesList == null) {
        	next = null;
        	return;
        }
        next = (byte[])cur_state.clone();
        nextIndex = 1;
        if (classChangesList[0] == Reg2dynPriorityClass.SYNCHRONOUS) {
        	for ( ; nextIndex<classChangesList.length ; nextIndex++) {
        		next[classChangesList[nextIndex++]] += classChangesList[nextIndex];
        	}
        	multiple = classChangesList.length > 1;
        } else {
        	next[classChangesList[1]] += classChangesList[2];
        	nextIndex = 3;
        }
	}
}