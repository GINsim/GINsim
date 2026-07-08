package org.ginsim.core.graph;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.ginsim.common.application.GsException;
import org.ginsim.core.annotation.Annotation;


/**
 * Handle the basic structure of a graph.
 * This provides methods to: <ul>
 *   <li>add and remove vertices and edges</li>
 *   <li>get the list of existing vertices and edges</li>
 *   <li>some simple graph operations (shortest path, connected components)</li>
 *   <li>Handle some basic metadata: graph name and annotation</li>
 * </ul>
 *
 * @author Aurelien Naldi
 * @author Lionel Spinelli
 *
 * @param <V> type for vertices
 * @param <E> type for edges
 */
public interface GraphModel<V,E extends Edge<V>> {

    /**
     * Give access to the name of the graph
     * 
     * @return the name associated with this graph.
     */
    String getGraphName();
    
    /**
     * changes (if success) the name associated with this graph.
     * By default only valid xmlid are accepted.
     *
     * @param graphName the new name.
     * @throws GsException if the name is invalid.
     */
    void setGraphName( String graphName) throws GsException;

    /**
     * Give access to the annotation associated with this graph.
     *
     * @return the association associated with this graph
     */
    Annotation getAnnotation();


	/**
	 * Add a node to this graph structure
	 * 
	 * @param node the node
	 * @return the created node
	 */
	boolean addNode( V node);
	
	/**
	 * Add an edge to this graph structure.
	 * 
	 * @param edge the edge
	 * @return the created edge
	 */
	boolean addEdge( E edge);

    /**
     * Remove a node from the graph.
     * 
     * @param node the node
     * @return true if the node was effectively removed
     */ 
	boolean removeNode( V node);

    /**
     * Remove an edge from the graph.
     * 
     * @param edge the edge
     * @return true if the edge was effectively removed
     */
	boolean removeEdge( E edge);
	
	/**
	 * Return the number of node in this graph
	 * 
	 * @return the number of node in this graph.
	 */
	int getNodeCount();
	
    
    /**
     * Return the Collection of the graph nodes
     * 
     * @return a Collection of the graph nodes
     */
    Collection<V> getNodes();
    
    
	/**
	 * Give access to the node named with the given name
	 * 
	 * @param id name of a node
	 * @return the node corresponding to this unique id or null if not found.
	 */
	V getNodeByName( String id);
	
    /**
     * Return the size of the node order
     * 
     * @return the size of the node order
     */
	int getNodeOrderSize();
	
	
	/**
	 * Search the vertices with ID matching the given regular expression. 
	 * Other kind of graph could overwrite this method. 
	 * 
	 * @param regexp the regular expression node ID must match to be selected
	 * @return a List of vertices
	 */
	List<V> searchNodes( String regexp);
	
	
    /**
	 * getter of edge
     * @param source the Vertex source
     * @param target the vertex target
     * @return the edge between source and target or null if not found.
     */
    E getEdge(V source, V target);
    
	
    /**
	 * getter of collection of edges
     * @return a Collection of the graph edges.
     */
	Collection<E> getEdges();

	
    /**
	 * test if contains node
     * @param node the vertex node
     * @return true if the node is in the graph, false if not.
     */
    boolean containsNode(V node);

	/**
	 * node getter
	 * @param node the node V
	 * @return the node objecy
	 */
	V getExistingNode(V node);
    
    /**
	 * test if contains edges
     * @param from the from vertex V
     * @param to the to vertex V
     * @return true if an edge between the two provided vertices exists in the graph, false if not.
     */
    boolean containsEdge(V from, V to);
    
    
    /**
	 * getter of incoming edges
     * @param node the node V
     * @return incoming edges of the given node.
     */
    Collection<E> getIncomingEdges(V node);
    
    
    /**
	 * getter of outgoing edges
     * @param node the node V
     * @return outgoing edges of the given node.
     */
    Collection<E> getOutgoingEdges(V node);
    
    
	/**
	 * Find the shortest path between the two given vertices
	 * 
	 * @param source the node at the beginning of the searched path
	 * @param target the node at the end of the searched path
	 * @return the list of edges composing the shortest path
	 */
	List<E> getShortestPath(V source, V target);
	


	/**
	 * Return a list of set of node, each set containing a strongly connected component of the graph
	 * 
	 * @return a list of set of node, each set containing a strongly connected component of the graph
	 */
	List<Set<V>> getStronglyConnectedComponents();

    
    /**
     * Build a graph from the provided vertices and edges based on the current graph
     * 
     * @param node the collection of vertices used to create the subgraph
     * @param edges the collection of edges used to create the subgraph
     * @return a Graph composed of the provided vertices and edges and based on the current graph
     */
    Graph<V,E> getSubgraph( Collection<V> node, Collection<E> edges);
    
    /**
     * Merge the provided graph with the current one
     * 
     * @param graph The graph to merge with the current graph
	 * @return merged list
     */
    List<?> merge( Graph<V,E> graph);

}
