package org.ginsim.service.tool.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;
import org.ginsim.core.graph.regulatorygraph.RegulatoryEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameter;
import org.ginsim.core.graph.regulatorygraph.perturbation.Perturbation;


/**
 * analyse circuits to detect their functionality/functional range.
 *
 * <p>on circuits:
 * <ul>
 *  <li>a circuit is non-functional if any of it's edge is inactive</li>
 *  <li>a circuit is negative if it contains 2k+1 negative interactions</li>
 * </ul>
 *
 * <p>on interactions:
 * <ul>
 *  <li>an interaction is non-functional if the main range is non-functional</li>
 *  <li>an interaction is negative if the negative range is wider than the positive one</li>
 * </ul>
 *
 * <p>about interaction's contexts:
 * <ul>
 *  <li>a context is non-functional if in this context, the activation of the interaction
 *      (considering min and max of it's activation range)
 *      has no effect on the activity of the target interaction</li>
 *  <li>a context is functional, positive (resp negative) if in this context, the activation of the interaction
 *      triggers (resp inhibits) the activation of the target interaction<li>
 * </ul>
 */
public class CircuitAlgo {

	private static final boolean testOutLimit = false;

    private Map m_report = new HashMap();
    private byte[][] t_constraint = null;

    // some context data
    RegulatoryNode target;
    RegulatoryMultiEdge me;
    LogicalParameter gsi;

    RegulatoryGraph graph;

    List<NodeInfo> nodeOrder;
    int[] t_maxValues;
    long fullPhaseSpace;
    long score;
    private final MDDManager ddmanager;
    private final int[] functions;
    boolean do_cleanup;

    public CircuitAlgo(RegulatoryGraph graph, boolean do_cleanup) {
        this(graph, null, do_cleanup);
    }

    /**
    * @param graph the studied graph
    * @param mutant  perturbation
    * @param do_cleanup constraints on the nodes
    */
    public CircuitAlgo(RegulatoryGraph graph, Perturbation mutant, boolean do_cleanup) {
        this.do_cleanup = do_cleanup;
        LogicalModel lmodel = graph.getModel();
        ddmanager = lmodel.getMDDManager();
        if (mutant != null) {
            lmodel = mutant.apply(lmodel);
        }

        // TODO: propagate fixed components to avoid showing useless contexts
        // This requires propagating the reduction to the calling objects or mapping reduced elements
//        lmodel = FixedComponentRemover.reduceFixed(lmodel, true);

        functions = lmodel.getLogicalFunctions();
        this.nodeOrder = lmodel.getComponents();
        t_maxValues = new int[functions.length];
        fullPhaseSpace = 1;
        int i=0;
        for (NodeInfo node: nodeOrder) {
        	t_maxValues[i] = node.getMax()+1;
        	fullPhaseSpace *= t_maxValues[i];
        }
        this.graph = graph;
    }

    /**
     * @param ei the edge to test
     * @param t_circuit members of the circuit
     * @param nextmin
     * @param nextmax
     *
     * @return the functionality context for this edge
     */
    public int checkEdge(RegulatoryEdge ei, int[] t_circuit, int nextmin, int nextmax) {
        me = ei.me;
        // first look for a previous test on this interaction with the same constraints
        List[] t_report = (List[])m_report.get(me);
        if (!testOutLimit || nextmax == -1) {
            nextmax = me.getTarget().getMaxValue();
        }
        if (t_report != null) {
            if (t_report[ei.index] != null) {
                List v_lr = t_report[ei.index];
                for (int i=0 ; i<v_lr.size() ; i++) {
                	subReport sr = (subReport)v_lr.get(i);
                	if (sr.min == nextmin && sr.max == nextmax) {
                		return sr.node;
                	}
                }
                // this edge had previously been tested but for different parameters
                // the next step will add a relevant test
            } else {
                // some tests did exist on the same multiedge, but not exactly the right "subedge"
                t_report[ei.index] = new ArrayList();
            }
        } else { // no report existed on this multiedge, create a new one
            t_report = new List[me.getEdgeCount()];
            t_report[ei.index] = new ArrayList();
            m_report.put(me, t_report);
        }

        RegulatoryNode source = me.getSource();
        target = me.getTarget();

        byte min = me.getMin(ei.index);
        byte max = me.getMax(ei.index);
        byte smax = source.getMaxValue();
        if (max == -1) {
			max = smax;
		}

        // t_lc = where are local contexts, its length depends on the context:
        //   * 2 if only the down limit to test ==> under, inside
        //   * 3 if min == max  ==> under, inside, out
        //   * 4 if min != max  ==> under, inside bottom, inside top, out
        byte[] t_lc;
        if (max < smax) {
            if (max > min) {
                t_lc = new byte[4];
                t_lc[2] = max;
                t_lc[3] = (byte) (max+1);
            } else {
                t_lc = new byte[3];
                t_lc[2] = (byte) (max+1);
            }
        } else {
            t_lc = new byte[2];
        }
        t_lc[0] = (byte) (min-1);
        t_lc[1] = min;

        List[] t_ei = new List[t_lc.length];
        for (int i=0 ; i<t_lc.length ; i++) {
            List v = new ArrayList();
            int val = t_lc[i];
            for (int j=0 ; j<me.getEdgeCount() ; j++) {
                if (val >= me.getMin(j) && (me.getMax(j) == -1 || val <= me.getMax(j))) {
                    v.add(me.getEdge(j));
                }
            }
            t_ei[i] = v;
        }
        if (t_ei.length == 4) {
            // if both inside refers to the same parameters, remove the unnecessary one
            if (t_ei[1].size() == t_ei[2].size() && t_ei[1].containsAll(t_ei[2])) {
                t_ei = new List[] { t_ei[0], t_ei[1], t_ei[3]};
            }
            // TODO: what if both inside/outside are inverted ? (==> action will also be inverted)
            // Has all this still a meaning now ??
        }
        if (t_ei.length == 3) {
            // if both outside refers to the same parameters, remove the unnecessary one
            if (t_ei[0].size() == t_ei[2].size() && t_ei[0].containsAll(t_ei[2])) {
                t_ei = new List[] { t_ei[0], t_ei[1]};
            }
        }

        // get the context tree
        int node = getContextFromParameters(functions[nodeOrder.indexOf(target)],
        		nodeOrder.indexOf(source), min, t_circuit, nextmin, nextmax);

        node = checkConstraint(node);
        // cache the result
        subReport sr = new subReport();
        sr.min = nextmin;
        sr.max = nextmax;
        sr.node = node;
        t_report[ei.index].add(sr);
        return node;
    }

    /**
     * build the context of functionality from the regulation tree.
     * In this context, we have not yet meet the considered gene.
     *
     *  - take as input the tree view of logical parameters.
     *  - build from it the "activity" tree of this node in this circuit by finding branches
     *    that differ only by the current node.
     *
     * @param node
     * @param level level of the considered gene
     * @param thresold threshold of activity for the considered gene
     * @param t_circuit
     * @param nextmin
     * @param nextmax
     * @return the context of functionality
     */
    private int getContextFromParameters(int node, int level, int thresold, int[] t_circuit, int nextmin, int nextmax) {
        MDDVariable var = ddmanager.getNodeVariable(node);
        if (var == null || var.order  > level) { // no meeting: not functional
            return 0;
        }
        if (var.order < level ) { // may still meet it later
            int[] children = new int[var.nbval];
            for (int i=0 ; i<children.length ; i++) {
                children[i] = getContextFromParameters(ddmanager.getChild(node, i), level, thresold, t_circuit, nextmin, nextmax);
            }
            return var.getNode(children);
        }
        // now level == node.level.
        return getContextFromParameters(ddmanager.getChild(node, thresold-1), ddmanager.getChild(node, thresold), t_circuit, nextmin, nextmax);
    }

    /**
     * build the context of functionality from the regulation tree.
     * In this context, we are analysing two parallel branches with the considered node being
     * inactive in the first one and active in the second.
     *
     *  - take as input the tree view of logical parameters.
     *  - build from it the "activity" tree of this node in this circuit by finding branches
     *    that differ only by the current node.
     *
     * @param node
     * @param next
     * @param t_circuit
     * @param nextmin
     * @param nextmax
     * @return the context of functionality
     */
    private int getContextFromParameters(int node, int next, int[] t_circuit, int nextmin, int nextmax) {
        MDDVariable var = ddmanager.getNodeVariable(node);
        MDDVariable nextVar = ddmanager.getNodeVariable(next);
        if (var == null) {
            if (nextVar == null) {
                // the real end: choose the sign.
                // activate next edge = positive, desactivate it = negative.
                if (testOutLimit) {
                    if (node < nextmin || node > nextmax) {
                        if (next >= nextmin && next <= nextmax) {
                            return 1;
                        }
                        return 0;
                    }
                    if (next < nextmin || next > nextmax) {
                        return 2;
                    }
                } else {
                    if (node < nextmin) {
                        if (next >= nextmin) {
                            return 1;
                        }
                        return 0;
                    }
                    if (next < nextmin) {
                        return 2;
                    }
                }
                return 0;
            }
        }

        if (var == null || nextVar != null && var.order > nextVar.order) {
            int[] children = new int[nextVar.nbval];
            for (int i=0 ; i<children.length ; i++) {
                children[i] = getContextFromParameters(node, ddmanager.getChild(next, i), t_circuit, nextmin, nextmax);
            }
            return nextVar.getNode(children);
        }
        if (nextVar == null || var.order < nextVar.order) {
            int[] children = new int[var.nbval];
            for (int i=0 ; i<children.length ; i++) {
                children[i] = getContextFromParameters(ddmanager.getChild(node, i), next, t_circuit, nextmin, nextmax);
            }
            return var.getNode(children);
        }

        // both are non-terminal of the same level
        int[] children = new int[nextVar.nbval];
        for (int i=0 ; i<children.length ; i++) {
            children[i] = getContextFromParameters(ddmanager.getChild(node, i), ddmanager.getChild(next, i), t_circuit, nextmin, nextmax);
        }
        return nextVar.getNode(children);
    }

    /**
     * compute a score for the context.
     * higher is better (score increases with the size of the phase space region)
     * return: [score, sign]
     * sign can be: FALSE, POSITIVE, NEGATIVE or DUAL
     *
     * This method is _NOT_ thread safe
     *
     * @param node
     * @return a score along with a sign indication
     */
    public long[] score (int node) {
    	int[] state = new int[t_maxValues.length];
    	for (int i=0 ; i<state.length ; i++) {
    		state[i] = -1;
    	}
    	score = fullPhaseSpace;
    	long[] ret = new long[2];
    	ret[1] = score(node, state);
    	ret[0] = score;  // FIXME: still need a better, "scaled", score
    	return ret;
    }

    private byte score (int node, int[] state) {
        if (ddmanager.isleaf(node)) {
            if (node == 0) {
                return CircuitDescr.FALSE;
            }
            long addScore = 1;
            for (int i=0 ; i<state.length ; i++) {
            	if (state[i] != -1) {
            		addScore *= t_maxValues[i];
            	}
            }
            score -= addScore;
            return (byte)(node == 1 ? CircuitDescr.POSITIVE : CircuitDescr.NEGATIVE);
        }

        byte sign = CircuitDescr.FALSE;
        MDDVariable var = ddmanager.getNodeVariable(node);
        for (int i=0 ; i<var.nbval ; i++) {
        	state[var.order] = i;
            byte tmp = score(ddmanager.getChild(node,i), state);
        	if (sign == CircuitDescr.FALSE) {
        		sign = tmp;
        	} else if (tmp == CircuitDescr.FALSE) {
        	} else if (tmp == CircuitDescr.DUAL || sign == CircuitDescr.DUAL || tmp != sign){
        		sign = CircuitDescr.DUAL;
        	}
        }
        state[var.order] = -1;
        return sign;
    }

    /**
     * *very* quickly added constraint on all genes.
     *
     * @param node
     * @return the new node
     */
    private int checkConstraint(int node) {
        if (t_constraint == null || ddmanager.isleaf(node)) {
            return node;
        }
        MDDVariable var = ddmanager.getNodeVariable(node);
        byte[] constraint = t_constraint[var.order];

        int[] children = new int[var.nbval];
        for (int i=0 ; i<t_constraint[var.order][0] ; i++) {
            children[i] = 0;
        }
        for (int i=constraint[0] ; i<=constraint[1] ; i++) {
            children[i] = checkConstraint(ddmanager.getChild(node, i));
        }
        for (int i=constraint[1]+1 ; i<var.nbval ; i++) {
            children[i] = 0;
        }
        return var.getNode(children);
    }

    /**
     * Merge functionality contexts.
     *
     * @param context
     * @param node
     * @return
     */
    public int mergeContexts(int context, int node) {
        return FunctionalityMergeOperator.MERGE.combine(ddmanager, context, node);
    }

    /**
     * Remove constraint on the members from a functionality context.
     *
     * @param context
     * @param t_circuit
     * @return
     */
    public int cleanupContext(int context, int[] t_circuit) {
        if (ddmanager.isleaf(context)) {
            return context;
        }

        MDDVariable var = ddmanager.getNodeVariable(context);
        int cst = t_circuit[var.order];
        if (cst != 0) {
            int n = ddmanager.getChild(context, cst-1);
            int n2 = ddmanager.getChild(context, cst);
            int cleaned = FunctionalityCleanupOperator.CLEANUP.combine(ddmanager, n, n2);

            return cleanupContext(cleaned, t_circuit);
        }

        int[] children = new int[var.nbval];
        for (int i=0 ; i<var.nbval ; i++) {
            children[i] = cleanupContext(ddmanager.getChild(context, i), t_circuit);
        }

        return var.getNode(children);
    }

    protected class subReport {
    	int min;
    	int max;
    	int node;
    }

    public MDDManager getManager() {
        return ddmanager;
    }
}
