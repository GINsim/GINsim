package org.ginsim.core.graph;

import java.io.File;

import org.ginsim.common.application.GsException;

/**
 * Base class for graphs which need an associated graph.
 * It derives from the more generic AbstractGraph class and implements method to maintain the graph association.
 *
 * @author Lionel Spinelli
 * @author Aurelien Naldi
 *
 * @param <V>   type of vertices
 * @param <E>   type of edges
 * @param <AG>  type of associated graph
 * @param <AV>  type of vertices in the associated graph
 * @param <AE>  type of edges in the associated graph
 */
abstract public class AbstractDerivedGraph<V, E extends Edge<V>, AG extends Graph<AV, AE>, AV, AE extends Edge<AV>>
			 extends AbstractGraph<V,E>
			 implements GraphAssociation<AG, AV, AE>, GraphListener<AG> {
    /**
     * associated graph attribute
     */
    protected AG associatedGraph = null;
    /**
     *  associated graph ID
     */
    protected String associatedID = null;

    /**
     * constructor of Graph factory
     * @param factory the factory
     */
    public AbstractDerivedGraph( GraphFactory factory) {
    	super(factory);
    }


    /**
     * Associate the given graph to the current one
     * 
     * @param associated_graph  the associated graph
     */
	@Override
	public void setAssociatedGraph( AG associated_graph) {

        if (associated_graph == null || !isAssociationValid( associated_graph)) {
            return;
        }

        if (associatedGraph != null) {
            GSGraphManager.getInstance().removeGraphListener( associatedGraph, this);
            associatedGraph = null;
            return;
        }
        associatedGraph = associated_graph;
        GSGraphManager.getInstance().addGraphListener( associatedGraph, this);
    }
	
    /**
     * @return the graph associated with this one.
     * @throws GsException  exception
     */
	@Override
    public AG getAssociatedGraph() throws GsException{

        if ( associatedGraph == null && getAssociatedGraphID() != null) {
        	
            AG ag = (AG) GSGraphManager.getInstance().getGraphFromPath( associatedID);
            if (ag != null) {
                setAssociatedGraph( ag);
            } else {
                File f = new File( associatedID);
                if (f.exists()) {
                    ag = (AG) GSGraphManager.getInstance().open(f);
                    setAssociatedGraph(ag);
                } else {
                	throw new GsException(GsException.GRAVITY_INFO, "STR_openAssociatedGraphFailed"+"\n"+associatedID);
                }
            }
        }

        // check association
        if (associatedGraph != null && !isAssociationValid(associatedGraph)) {
            associatedGraph = null;
            associatedID = null;
        }

        return associatedGraph;
    }
    
    /**
     * @return the ID (path) of the associated graph.
     * @throws GsException  exception
     */
	@Override
    public String getAssociatedGraphID() throws GsException{
		
        if (associatedGraph != null) {
            associatedID = GSGraphManager.getInstance().getGraphPath( associatedGraph);
            if (associatedID == null) {
                throw new GsException(GsException.GRAVITY_INFO, "STR_associate_save");
            }
        }

        if (associatedID != null) {
            File f = new File(associatedID);
            if (!f.exists() || !f.canRead()) {
            	throw new GsException(GsException.GRAVITY_INFO, new String[]{"STR_associate_notfound",associatedID});
            }
        } else {
        	throw new GsException(GsException.GRAVITY_INFO, "STR_associate_manual");
        }

        return associatedID;
    }
	
    /**
     * test if a graph can be associated with this one.
     * this is a default implementation and will always return false, override to do something useful.
     *
     * @param graph the graph
     * @return true if this is a valid associated graph.
     */
	protected abstract boolean isAssociationValid( Graph<?,?> graph);
    
    /**
     * set the path to the associated graph.
     * @param value the string value
     */
	@Override	
    public void setAssociatedGraphID(String value) {
        associatedID = value;
    }

	@Override
	public GraphEventCascade graphChanged(Graph g, GraphChangeType type, Object data) {
		if (g != this) {
			return null;
		}
		switch (type) {
		case EDGEADDED:
		case EDGEREMOVED:
		case NODEADDED:
		case NODEREMOVED:
		case GRAPHMERGED:
			setAssociatedGraph(null);
			break;
		}
        return null;
    }

}
