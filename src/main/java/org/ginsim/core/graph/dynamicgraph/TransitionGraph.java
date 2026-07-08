package org.ginsim.core.graph.dynamicgraph;


import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;
import org.ginsim.service.tool.reg2dyn.SimulationStrategy;
import org.colomoto.mddlib.MDDManager;

import java.util.List;

public interface TransitionGraph<V,E extends Edge<V>> extends Graph<V,E> {

    /**
     * Get the list of known extra components names.
     * These components have no explicitly assigned value in the STG,
     * but their values can be retrieved based on a given state.
     *
     * @return the list of names, or null if none
     */
    String[] getExtraNames();

    int[] getExtraFunctions();

    List<NodeInfo> getExtraNodes();

    int[] getCoreFunctions();

    MDDManager getMDDManager();
    /**
     * Retrieve the values for all extra components for a given state.
     * If the provided array to fill is null or of the wrong size, a new array will be created and returned.
     * Otherwise, extraValues will be filled and returned.
     *
     * @param state
     * @param extraValues array in which to put the values.
     *
     * @return extraValues properly filled or a new array
     */
    byte[] fillExtraValues(byte[] state, byte[] extraValues);


    /**
     * Associate a logicalModel with this STG, notably to retrieve extra values.
     * @param model
     */
    void setLogicalModel(LogicalModel model);

    /**
     * Set the simulation strategy to use.
     * This will determine how the simulation is performed, and which
     * extra components are available.
     * <p>
     * The strategy can be one of the following:
     * <ul>
     * <li>SimulationParameters.STRATEGY_STG: the default strategy, which
     *     uses the state transition graph (STG) to simulate the system.</li>
     * <li>SimulationParameters.STRATEGY_MDD: the MDD strategy, which         uses the MDD to simulate the system.</li>
     * </ul>
     * @param strategy
     */
    public void setSimulationStrategy(SimulationStrategy strategy);
    /**
     * Get the current simulation strategy.
     * This will return one of the following:
     * <ul>
     * <li>SimulationStrategy.STG: the default strategy, which
     *     uses the state transition graph (STG) to simulate the system.</li>
     * <li>SimulationStrategy.HTG: the Hierarchical Transition Graph, which uses the HTG to simulate the system.</li>
     * <li>SimulationStrategy.SCCG: the Strongly Connected Components Graph, which uses the SCC to simulate the system.</li>
     * </ul>
     *
     * @return the current simulation strategy
     */
    public SimulationStrategy getSimulationStrategy();
    }

