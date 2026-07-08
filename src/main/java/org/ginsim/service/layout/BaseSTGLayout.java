package org.ginsim.service.layout;

import org.ginsim.core.graph.dynamicgraph.DynamicNode;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;

import java.util.List;

/**
 * Base class for layouts working on State Transition Graphs.
 *
 * @author Duncan Berenguier
 * @author aurelien Naldi
 */
public class BaseSTGLayout {

    /**
     * Get the level of a component
     *
     * @param state the state to consider
     * @param i the index of the component
     * @return the level of the component
     */
    protected int getState(byte[] state, int i) {
        if (state.length > i) return state[i];
        else return 0;
    }

    /**
     * Find the first change between two states.
     *
     * @param diffstate the difference between the two states
     * @return the coordinate of the first change between the two states.
     */
    protected int getChange(byte[] diffstate) {
        for (int i = 0; i < diffstate.length; i++) {
            if (diffstate[i] != 0) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Construct the | bit operator for a table of byte.
     * A value in the table is 0, if the corresponding component (according to the newNodeOrder)
     * has the same value in the two states, otherwise its the absolute difference (should be 1)
     *
     * @param sourceNode the source node
     * @param targetNode the target node
     *
     * @return a list of changes between the states
     */
    protected byte[] getDiffStates(DynamicNode sourceNode, DynamicNode targetNode) {
        byte[] delta = new byte[sourceNode.state.length];
        for (int i = 0; i < delta.length; i++) {
            delta[i] = (byte) Math.abs(getState(sourceNode.state,i) - getState(targetNode.state,i));
        }
        return delta;
    }

    /**
     * Retrieve the maximal values of all components.
     * @param nodeOrder node order list
     * @return the max levels for all components.
     */
    public byte[] getMaxValues(List<RegulatoryNode> nodeOrder) {
        byte[] maxValues = new byte[nodeOrder.size()];
        for (int i = 0; i < nodeOrder.size(); i++) {
            maxValues[i] = nodeOrder.get(i).getMaxValue();
        }
        return maxValues;
    }

}
