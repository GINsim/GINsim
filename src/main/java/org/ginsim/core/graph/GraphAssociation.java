package org.ginsim.core.graph;

import org.ginsim.common.application.GsException;

/**
 * Defines a graph associated with the implementor of this interface.
 * This interface should be implemented by all objects which depend on a separate Graph for interpretation.
 * This association will ensure that this associated graph can be retrieved properly.
 * 
 * @author Lionel Spinelli
 * @author Aurelien Naldi
 *
 * @param <AG>  ag graph
 * @param <AV> av vertex
 * @param <AE> ae edge
 */
public interface GraphAssociation<AG extends Graph<AV, AE>, AV, AE extends Edge<AV>> {

    /**
     * Associate the given graph to the current one
     * 
     * @param associated_graph  the associated graph to set
     */
    public void setAssociatedGraph( AG associated_graph);
    
    /**
     * Given access to the graph that has been associated to the current graph
     * 
     * @return the graph associated with this one.
     * @throws GsException exception
     */
    public AG getAssociatedGraph() throws GsException;

    /**
     * getter of association graph id
     * @return id of association graph
     * @throws GsException exception Gs
     */
    public String getAssociatedGraphID() throws GsException;

    /**
     * setter of association graph id
     * @param value  the string value
     */
    public void setAssociatedGraphID(String value);
}
