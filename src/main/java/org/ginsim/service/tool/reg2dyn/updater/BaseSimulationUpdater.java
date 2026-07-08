package org.ginsim.service.tool.reg2dyn.updater;

import java.util.NoSuchElementException;

import org.colomoto.biolqm.LogicalModel;
import org.ginsim.service.tool.reg2dyn.SimulationQueuedState;
import org.ginsim.service.tool.reg2dyn.priorityclass.PriorityClass;
import org.ginsim.service.tool.reg2dyn.priorityclass.PrioritySetDefinition;


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
abstract public class BaseSimulationUpdater implements SimulationUpdater {


	/**
	 * final int length
	 */
	protected final int length;
	/**
	 * byte[] cur_state
	 */
	protected byte[] cur_state;
	/**
	 * byte[] next
	 */
	protected byte[] next = null;
	/**
	 *  int depth
	 */
	protected int depth;
	/**
	 * Object node
	 */
	protected Object node;
	/**
	 * boolean multiple
	 */
	protected boolean multiple;
	/**
	 *  ModelHelper modelHelper
	 */
	protected ModelHelper modelHelper;

	/**
	 * Constructor
	 * @param model the logical model
	 */
	public BaseSimulationUpdater(LogicalModel model) {
		this(new ModelHelper(model));
	}

	/**
	 * Constructor
	 * @param helper moel helper
	 */
	public BaseSimulationUpdater(ModelHelper helper) {
		modelHelper = helper;
		this.length = modelHelper.size();
	}

	/**
	 * Thest if hasnext
	 * @return boolean if has next
	 */
	public boolean hasNext() {
		return next != null;
	}

	/**
	 * next method
	 * @return next object
	 */
	public Object next() {
		if (next == null) {
			throw new NoSuchElementException();
		}
		SimulationQueuedState ret = new SimulationQueuedState(next, depth+1, node, multiple);
		multiple = false;
		doBuildNext();
		return ret;
	}

	/**
	 * Get next state
	 * @return byte array for state
	 */
	public byte[] nextState() {
        byte[] ret = next;
        doBuildNext();
        return ret;
    }

	/**
	 * Remove function
	 */
	public void remove() {
	}

	/**
	 * set the state which should be used as basis.
	 * @param node  the node
	 * @param depth  the depth
	 * @param state the state
	 */
	public void setState(byte[] state, int depth, Object node) {
		this.cur_state = state;
		this.depth = depth;
		this.node = node;
		multiple = false;
		doSetState();
	}

	/**
	 * Do
	 */
	abstract protected void doSetState();

	/**
	 * build next
	 */
	abstract protected void doBuildNext();

	/**
	 * Clone function
	 * @return a clone SimulationUpdater
	 */
	abstract protected SimulationUpdater doClone();

	/**
	 * Clone function
	 * @param state byte array state
	 * @return SimulationUpdater cloned
	 */
	public SimulationUpdater cloneForState(byte[] state) {
		SimulationUpdater updater = doClone();
		updater.setState(state, 0, null);
		return updater;
	}
	
	/**
	 * get change step for a gene
	 *
	 * @param initState the init states
	 * @param i index of the gene to test
	 * @return the direction in which the gene want to change: 0 for no change, 1 for increase and -1 for decrease
	 */
	protected int nodeChange(byte[] initState, int i) {
		return modelHelper.nodeChange(initState, i);
	}

	/**
	 * Getter AsynchronousInstance
	 * @param model logical model
	 * @return SimulationUpdater
	 */
	static public SimulationUpdater getAsynchronousInstance(LogicalModel model) {
		return new AsynchronousSimulationUpdater(model);
    }

	/**
	 * Getter SynchronousInstance
	 * @param model logical model
	 * @return SimulationUpdater
	 */
	static public SimulationUpdater getSynchronousInstance(LogicalModel model) {
		return new SynchronousSimulationUpdater(model);
    }

	/**
	 * Instance getter
	 * @param model the logical model
	 * @param pcdef the  PrioritySetDefinition
	 * @return SimulationUpdater
	 */
	static public SimulationUpdater getInstance(LogicalModel model, PrioritySetDefinition pcdef) {
		if (pcdef.size() < 2) {
			PriorityClass pc = (PriorityClass)pcdef.get(0);
			if (pc.getMode() == PriorityClass.SYNCHRONOUS) {
				return getSynchronousInstance(model);
			}
			return getAsynchronousInstance(model);
		}
		return new PrioritySimulationUpdater(model, pcdef);
	}
}

/* ******* MDD helper *********/
class ModelHelper {

	private final int size;
	private final LogicalModel model;

	public ModelHelper(LogicalModel model) {
		this.model = model;
		size = model.getLogicalFunctions().length;
	}
	
	public int size() {
		return size;
	}
	
	public int nodeChange(byte[] initState, int i) {
		byte curState = initState[i];
		byte nextState = model.getTargetValue(i, initState);

		// now see if the node is willing to change it's state
		if (nextState > curState){
		    return 1;
		} else if (nextState < curState){
		    return -1;
		}
		return 0;
	}
}


/* **************************** Asynchronous ***********************************/

class AsynchronousSimulationUpdater extends BaseSimulationUpdater {

	int nextChange = -1;
	int nextUpdate;

	public AsynchronousSimulationUpdater(ModelHelper helper) {
		super(helper);
	}
	public AsynchronousSimulationUpdater(LogicalModel model) {
		super(model);
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
	@Override
	public SimulationUpdater doClone() {
		return new AsynchronousSimulationUpdater(this.modelHelper);
	}
}


/* **************************** by priority ***********************************/

class PrioritySimulationUpdater extends BaseSimulationUpdater {
	
	int[][] pclass;
    int[] classChangesList;
    int nextIndex;
    int curClass;
    int priority;

	
    public PrioritySimulationUpdater(LogicalModel model, PrioritySetDefinition pcdef) {
		super(model);
		pclass = pcdef.getPclass(model.getComponents());
	}
    public PrioritySimulationUpdater(ModelHelper helper,int[][] pclass) {
		super(helper);
		this.pclass = pclass;
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
        if (classChangesList[0] == PriorityClass.SYNCHRONOUS) {
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
        if (classChangesList[0] == PriorityClass.SYNCHRONOUS) {
        	for ( ; nextIndex<classChangesList.length ; nextIndex++) {
        		next[classChangesList[nextIndex++]] += classChangesList[nextIndex];
        	}
        	multiple = classChangesList.length > 1;
        } else {
        	next[classChangesList[1]] += classChangesList[2];
        	nextIndex = 3;
        }
	}
	@Override
	protected SimulationUpdater doClone() {
		return new PrioritySimulationUpdater(this.modelHelper, this.pclass);
	}
}
