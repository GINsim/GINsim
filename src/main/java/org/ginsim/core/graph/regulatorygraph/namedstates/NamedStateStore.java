package org.ginsim.core.graph.regulatorygraph.namedstates;

import java.util.Map;

/**
 * Store a reference to a group of named states.
 *
 * @author Aurelien Naldi.
 */
public interface NamedStateStore {

    /**
     * Getter of initial states
     * @return map of initial states
     */
    Map getInitialState();

    /**
     * Getter of input staes
     * @return map of input states
     */
    Map getInputState();
}
