package org.ginsim.core.graph.hierarchicaltransitiongraph;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDOperator;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.operators.MDDBaseOperators;
import org.ginsim.common.application.GsException;
import org.xml.sax.SAXException;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>A wrapper of OMDDNode that adds the essentials methods to store states.</p>
 *
 * <p>Each path terminating with a value &gt; 0 indicates the state is present in the set.<br>
 * The value is used to indicates several status.</p>
 *
 * <h2>Working with the size</h2>
 * <p>The value of the <b>size</b> attribute, that is the number of state inside the set, could be inconsistent.<br>
 * When adding a state (or a merge several Omdds), there is no efficient way to know if the state is added or if it was already present.<br>
 * In this class, the size is always incremented when we try to add a new state. Therefore the value of the size could be inconsistent, more
 * precisely be an overapproximation of the real value<br>
 * To cope with this problem, there is several methods to work with the <b>size</b> :
 * 	<ul>
 * 		<li><code>public int isSizeConsistent()</code> indicates if the size is consistent</li>
 * 		<li><code>public int updateSize()</code> count the current size, by exploring the whole diagram</li>
 * 		<li><code>public int getSize()</code> gives the size or INCONSISTENT_SIZE is the size is not consistent</li>
 * 		<li><code>public int getSizeOrOverApproximation()</code> gives the size no matter its consistency</li>
 * 		<li><code>public int getSizeOrUpdate()</code>gives the size and eventually update it before if needed</li>
 * 	</ul>
 *

 * @author Duncan Berenguier
 */
public class StatesSet {

    public static final int INCONSISTENT_SIZE = -42;

    private static final MDDOperator OR = MDDBaseOperators.OR;

    /**
     * An array indicating such that childsCount[i] indicates the maxValue of the i-th gene
     */
    private byte[] childsCount;

    /**
     * Contain the count of states in the set
     */
    private int size;

    /**
     * Indicates if the count of states is consistent. Mainsly because merge_or could not add any state in the set if it is already present.
     */
    private boolean size_consistancy;


    /**
     * The root of the diagram (OMDDNode)
     */
    private final MDDManager ddmanager;

    private int root;


/* **************** CONSTRUCTORS ************/


    /**
     * Initialize a new set of states
     */
    public StatesSet(MDDManager ddmanager, byte[] childsCount) {
        this.childsCount = childsCount;
        this.size = 0;
        this.size_consistancy = true;
        this.root = -1;
        this.ddmanager = ddmanager;
    }

    /**
     * Initialize a new set of states
     */
    private StatesSet(StatesSet other) {
        this.childsCount = other.childsCount;
        this.size = other.size;
        this.size_consistancy = other.size_consistancy;
        this.ddmanager = other.ddmanager;
        this.root = other.root;
    }

    /**
     * Initialize a new set of states
     */
    public StatesSet(MDDManager ddmanager, int mdd, byte[] childsCount) {
        this.childsCount = childsCount;
        this.size = 0;
        this.size_consistancy = false;
        this.ddmanager = ddmanager;
        this.root = mdd;
        updateSize();
    }

    public StatesSet clone() {
        return new StatesSet(this);
    }

/* **************** ADD STATE ************/

    /**
     * Add <b>state</b> to the set with <b>status</b><br>
     * If the state was already there, the old status remains.<br>
     *  NB: use updateStatus to change the status.
     * @param state byte array for state
     * @param newStatus new status int
     */
    public void addState(byte[] state, int newStatus) {
        int newState = stateFromArray(state, newStatus);
        addMDD(newState, 1);
    }

    /**
     * Add a state or group of states already transformed into MDD.
     * If the state was already there, the old status remains.<br>
     * NB: use updateStatus to change the status.
     *
     * @param newState indice
     * @param incSize the number of added state(s)
     */
    private void addMDD(int newState, int incSize) {
        if (root == -1) {
            root = newState;
            size = 1;
            size_consistancy = true;
        } else  {
            root = OR.combine(ddmanager, root, newState);
            size += incSize;//Introduce an overapproximation
            size_consistancy = false;
        }
    }

    /**
     * Add <b>state</b> to the set with <b>status</b><br>
     * If the state was already there, the old status remains.<br>
     *  NB: use updateStatus to change the status.
     * @param state string
     * @param newStatus the new status int
     */
    public void addState(String state, int newStatus) {
        addState(byteArrayFromString(state), newStatus);
    }

    /**
     * Add <b>states</b> to the set with status 1<br>
     * If the states were already there, their old status remains.<br>
     *  NB: use updateStatus to change the status.
     * @param states, a <b>List&lt;byte[]&gt;</b> of states to add
     */
    public void addStates(List<byte[]> states) {
        int newState = ddmanager.nodeFromStates(states, 1);
        addMDD(newState, states.size());
    }

    /**
     * Merge this set with the stateSet of the slaveNode.
     * @param slaveNodeStateSet
     */
    public void merge(StatesSet slaveNodeStateSet) {
        if (this.root != -1 && slaveNodeStateSet.root != -1) {
            this.root = OR.combine(ddmanager, root, slaveNodeStateSet.root);
            this.size += slaveNodeStateSet.getSizeOrOverApproximation();
            this.size_consistancy = false;
        }
    }


/* **************** SIZE CONSISTENCY CHECK ************/

    /**
     * Count the number of states in the Omdd and update the <b>size</b> accordingly
     * @return an array indiced by the status and counting the number of state per status
     */
    public int[] updateSize() {
        int[] counts = new int[10];
        size = 0;
        updateSize(root, -1, 1, counts);
        size_consistancy = true;
        return counts;
    }

    /**
     * A simple recursive parse of the diagram, counting the states in the diagram (all of them in <b>size</b> and depending on their values in <b>counts</b>.
     *
     * @param mdd the current omddNode to parse
     * @param last_depth
     * @param coef
     * @param counts
     */
    private void updateSize(int mdd, int last_depth, int coef, int[] counts) {
        MDDVariable var = ddmanager.getNodeVariable(mdd);
        if (var == null) {
            if (mdd == 0) {
                return;
            }

            int s = 1;
            for (int i = last_depth+1; i < childsCount.length; i++) {
                s *= childsCount[i];
            }
            counts[mdd] += s*coef;
            size += s*coef;
            return;
        }

        for (int i = last_depth+1; i < var.order; i++) {
            coef *= childsCount[i];
        }

        for (int i = 0 ; i < var.nbval ; i++) {
            updateSize(ddmanager.getChild(mdd, i), var.order, coef, counts);
        }
    }

    /**
     * Indicates if the size is in a consistent state, that is if it really reflect the number of state in the set, or only an overapproximation.
     * @return true if size is equal to the exact count of state in the set.
     */
    public boolean isSizeConsistent() {
        return size_consistancy;
    }


    /**
     * Gives the count of states in the diagram or INCONSISTENT_SIZE if the size is in inconsistent state.
     *
     * @return the count of states in the set or INCONSISTENT_SIZE
     */
    public int getSize() {
        if (size_consistancy == true) return size;
        else return INCONSISTENT_SIZE;
    }

    /**
     * Gives the count of states in the diagram or its overapproximation if the size is in inconsistent state.
     *
     * @return the count of states in the set
     */
    public int getSizeOrOverApproximation() {
        return size;
    }


    /**
     * <p>Gives the count of states in the diagram.</p>
     * If the size is in inconsistent state, then call <code>updateSize()</code> before.
     *
     * <p>Note that by calling this function, you will not be able to retrieve the count of state per status.</p>
     *
     * @return the count of states in the set
     */
    public int getSizeOrUpdate() {
        if (size_consistancy == true) return size;
        else {
            updateSize();
            return size;
        }
    }



/* **************** CONTAINS, UPDATESTATUS, REDUCE************/

    /**
     * Test if the set contains <b>state</b> that is, if its status is not 0
     * @param state
     * @return true if the set contains <b>state</b>
     */

    public boolean contains(byte[] state) {
        return ddmanager.reach(root, state) != 0;
    }


    /**
     * Change the status associated of <b>state</b> in the set to <b>newStatus</b>
     * @param state  byte state
     * @param newStatus  the new status
     * @return <ul>
     * 		<li><b>true</b> if the status is changed or was already equal to newStatus.</li>
     * 		<li><b>false</b> if the state wasn't in the set.</li>
     * </ul>
     */
    public boolean updateStatus(byte[] state, int newStatus) {
        int currentStatus = ddmanager.reach(root, state);
        if (currentStatus == newStatus) {
            return true;
        }
        if (currentStatus > 0) {
            int newState = stateFromArray(state, 2);
            // FIXME: use a MAX operation instead
            int newRoot = OR.combine(ddmanager, root, newState);
            ddmanager.free(root);
            ddmanager.free(newState);
            root = newRoot;
            return true;
        } else {
            return false;
        }
    }


/* **************** STATES CONSTRUCTORS ************/


    /**
     * Generate a new omdd corresponding to the <b>state</b> and with the status 1
     * @param state the state to generate
     * @return the omdd
     */
    public int stateFromArray(byte[] state) {
        return ddmanager.nodeFromState(state, 1);
    }

    /**
     * Generate a new omdd corresponding to the <b>state</b> and with the status <b>status</b>
     * @param state the state to generate
     * @param status the value to append at the leaf.
     * @return the omdd
     */
    public int stateFromArray(byte[] state, int status) {
        return ddmanager.nodeFromState(state, status);
    }

    /**
     * Generate a new omdd corresponding to the <b>state</b> and with the status 1
     * @param state the state to generate
     * @return the omdd
     */
    public int stateFromString(String state) {
        return stateFromString(state, 1);
    }

    /**
     * Generate a new omdd corresponding to the <b>state</b> and with the status 1
     * @param str_state the state to generate
     * @return the omdd
     */
    public int stateFromString(String str_state, int status) {
        byte[] state = byteArrayFromString(str_state);
        for (int i=0 ; i<state.length ; i++) {
            state[i] = Byte.parseByte(""+str_state.charAt(i));
        }

        return stateFromArray(state, status);
    }

    /**
     * Transform a String into an array of byte
     * @param state
     * @return
     */
    private byte[] byteArrayFromString(String state) {
        byte[] array = new byte[state.length()];
        for (int i = 0; i < state.length(); i++) {
            array[i] = (byte) Character.getNumericValue(state.charAt(i));
        }
        return array;
    }

/* **************** GETTERS AND SETTERS ************/


    /**
     * <p>Return An array indicating such that childsCount[i] indicates the maxValue of the i-th gene</p>
     *
     * <p>Use :</p> <pre>
     * 	<code>int countChilds = childsCount[level];</code>
     * 	<code>for (int i = 0; i &lt; countChilds; i++) {</code>
     * 	<code>	....omdd.next[i]....</code>
     * </pre>
     * @return the ChildsCount array
     */
    public byte[] getChildsCount() {
        return childsCount;
    }



/* **************** STATE TO LISTS ************/
    /**
     * Initialize and fill a list with all the states in the omdd
     * Each item of the returned list is a string representation using wildcard * (-1).
     * Note the order in the list is relative to the omdd structure.
     * @return a list made of all the states as schemata (using *)
     */
    public List<byte[]> statesToSchemaList() {
        List<byte[]> v = new LinkedList<byte[]>();
        byte[] t = new byte[childsCount.length];
        statesToSchemaList(root, v, t, -1);
        return v;
    }

    /**
     * Fill a list with all the states in the omdd
     * Each item of the returned list is a string representation using wildcard * (-1).
     * Note the order in the list is relat * @param stateRestrictionive to the omdd structure.
     * @param v  vertex
     * a list made of all the states as schemata (using *)
     */
    public void statesToSchemaList(List<byte[]> v) {
        byte[] t = new byte[childsCount.length];
        statesToSchemaList(root, v, t, -1);
    }

    private void statesToSchemaList(int omdd, List<byte[]> v, byte[] t, int last_depth) {
        MDDVariable var = ddmanager.getNodeVariable(omdd);
        if (var == null) {
            if (omdd == 0) return;
            for (int i = last_depth+1; i < childsCount.length; i++) {
                t[i] = -1;
            }
            v.add(t.clone());
            return;
        }

        for (int i = last_depth+1; i < var.order; i++) {
            t[i] = -1;
        }
        for (int i = 0 ; i < var.nbval ; i++) {
            t[var.order] = (byte) i;
            statesToSchemaList(ddmanager.getChild(omdd,i), v ,t, var.order);
        }

    }

    public List<byte[]> statesToFullList() {
        List<byte[]> v = new LinkedList<byte[]>();
        byte[] t = new byte[childsCount.length];
        statesToFullList(root, v, t, -1);
        return v;
    }
    /**
     *
     * Fill a list with all the states in the omdd
     * Each item of the returned list is a string representation using wildcard * (-1).
     * Note the order in the list is relative to the omdd structure.
     * a list made of all the states as schemata (using *)
     * @param v list byte vertex
     */
    public void statesToFullList(List<byte[]> v) {
        byte[] t = new byte[childsCount.length];
        statesToFullList(root, v, t, -1);
    }

    private void statesToFullList(int omdd, List<byte[]> v, byte[] t, int last_depth) {
        MDDVariable var = ddmanager.getNodeVariable(omdd);
        if (var == null) {
            if (omdd == 0) return;
            statesToList_leaf(omdd, v, t, last_depth+1);
            return;
        }

        statesToFullList_inner(omdd, v, t, last_depth+1, var.order);
    }

    private void statesToFullList_inner(int omdd, List<byte[]> v, byte[] t, int depth, int limit_depth) {
        MDDVariable var = ddmanager.getNodeVariable(omdd);
        if (depth == limit_depth) {
            for (int i = 0 ; i < var.nbval ; i++) {
                t[var.order] = (byte) i;
                statesToFullList(ddmanager.getChild(omdd,i), v ,t, var.order);
            }
            return;
        }
        for (byte i = 0; i < childsCount[depth]; i++) {
            t[depth] = i;
            statesToFullList_inner(omdd, v, t, depth+1, limit_depth);
        }
    }

    private void statesToList_leaf(int omdd, List<byte[]> v, byte[] t, int depth) {
        if (depth == childsCount.length) {
            v.add(t.clone());
            return;
        }
        for (byte i = 0; i < childsCount[depth]; i++) {
            t[depth] = i;
            statesToList_leaf(omdd, v, t, depth+1);
        }
    }



/* **************** TO STRINGS ************/

    /**
     * Return a parenthesis based representation of the MDD
     */

	public String write() {
        return ddmanager.dumpMDD(root);
	}


    /**
     * Return a String representation of the omdd
     */

    public StringBuffer statesToString(boolean addValue) {
        StringBuffer res = new StringBuffer();
        int length = childsCount.length+(addValue?3:1);
        StringBuffer s = new StringBuffer(length);

        for (int i = 0; i < length; i++) {
            s.append('.');
        }
        statesToString(root, s, res, 0, childsCount.length, addValue);
        return res;
    }
    private void statesToString(int omdd, StringBuffer s, StringBuffer res, int last_depth, int nbNodes, boolean addValue) {
        MDDVariable var = ddmanager.getNodeVariable(omdd);
        if (var == null) {
            if (omdd == 0) return;
            for (int i = last_depth+1; i < nbNodes; i++) {
                s.setCharAt(i, '*');
            }
            if (addValue) {
                s.setCharAt(nbNodes, '-');
                s.setCharAt(nbNodes+1, String.valueOf(omdd).charAt(0));
                s.setCharAt(nbNodes+2, '\n');
            } else {
                s.setCharAt(nbNodes, '\n');
            }
            res.append(s);
            return;
        }

        for (int i = last_depth+1; i < var.order; i++) {
            s.setCharAt(i, '*');
        }
        for (int i = 0 ; i < var.nbval ; i++) {
            s.setCharAt(var.order, String.valueOf(i).charAt(0));
            statesToString(ddmanager.getChild(omdd,i), s, res, var.order, nbNodes, addValue);
        }
        return;
    }

    /**
     * Return a String representation of a state in the set (the first in the pile or the first reach in the omdd)
     */
    public StringBuffer firstStatesToString() {
        StringBuffer s  = new StringBuffer(childsCount.length);
        firstStatesToString(root, s, 0);
        return s;
    }

    /**
     * Return a String representation of a state in the set (the first in the pile or the first reach in the omdd)
     * @param omdd
     * @param s
     * @param last_depth
     * @return is used internally for the recursion
     */
    private boolean firstStatesToString(int omdd, StringBuffer s, int last_depth) {
        MDDVariable var = ddmanager.getNodeVariable(omdd);
        if (var == null) {
            if (omdd == 0) return false;
            return true;
        }
        if (s.length() <= var.order) {
            for (int j = s.length(); j <= var.order; j++) {
                s.append('*');
            }
        }
        for (int i = 0 ; i < var.nbval ; i++) {
            s.setCharAt(var.order, String.valueOf(i).charAt(0));
            boolean res = firstStatesToString(ddmanager.getChild(omdd,i), s, var.order);
            if (res) return true;
        }
        return false;
    }


    /*
     * Parse a MDD from a text representation.
     * @param parse
     * @throws SAXException
     */
	public void parse(String parse) throws SAXException {
		try {
            root = ddmanager.parseDump(parse);
			updateSize();
		} catch (ParseException e) {
            e.printStackTrace();
			throw new SAXException( new GsException( "STR_ParsingError", e));
		}
	}

}
