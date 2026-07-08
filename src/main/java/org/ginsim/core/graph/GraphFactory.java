package org.ginsim.core.graph;

import org.ginsim.core.graph.view.style.EdgeStyle;
import org.ginsim.core.graph.view.style.NodeStyle;

/**
 * Factory to open or create specialised graph instances.
 * Implementations of this interface will be loaded by the GraphManager and used
 * to create new graph or load them from file.
 *
 * <br>To be properly loaded, they must use the <code>@MetaInfServices( GraphFactory.class)</code> annotation.
 *
 * @author Lionel Spinelli
 * @author Aurelien Naldi
 *
 * @param <G>  graph
 */
public interface GraphFactory<G extends Graph<?,?>> {
	
    /**
     * Return the class of graph this factory is managing
     * 
     * @return the name of the class of graph this factory is managing
     */
	Class<G> getGraphClass();
	
    /**
     * Return the type of graph this factory is managing
     * 
     * @return the name of the type of graph this factory is managing
     */
	String getGraphType();
	
	/**
	 * Return the class of the parser to use to read from file the type
	 * of graph the factory manager
	 * 
	 * @return the class of the parser to use with this factory
	 */
	Class getParser();
	
    /**
     * Create a new graph of the type factory is managing
     * 
     * @return an instance of the graph type the factory is managing
     */
	G create();

	/**
	 * create Node style
	 * @param graph graph from Node style
	 * @return the style NodeStyle
	 */
	NodeStyle createDefaultNodeStyle(G graph);

	/**
	 * create a graph
	 * @param graph graph to construct
	 * @return edge style
	 */
	EdgeStyle createDefaultEdgeStyle(G graph);
}
