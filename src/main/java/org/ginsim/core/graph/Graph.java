package org.ginsim.core.graph;

import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Map;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.graph.view.ViewCopyHelper;
import org.ginsim.core.graph.view.style.StyleManager;


/**
 * The main graph object in GINsim.
 * Beside the basic graph methods from the GraphModel interface, this provides
 * access to the view (styles and attribute readers), and define methods to save a graph.
 * @param <E> the edge E
 * @param <V> the vertex V
 * @author Aurelien Naldi
 * @author Lionel Spinelli
**/
public interface Graph<V,E extends Edge<V>> extends GraphModel<V,E>{

	/**
	 * Retrieve the style manager for this graph.
	 * @return the style manager
	 */
	StyleManager<V, E> getStyleManager();
	
	/**
	 * Give access to the attribute reader of edges
	 * 
	 * @return the attribute reader of edges
	 */
	EdgeAttributesReader getEdgeAttributeReader();
	
	/**
	 * Give access to the attribute reader of vertices
	 * 
	 * @return the attribute reader of vertices
	 */
	NodeAttributesReader getNodeAttributeReader();
	
	/**
	 * Get the global bounds of the graph.
	 * 
	 * @return the dimention
	 */
	Dimension getDimension();
	
	/**
	 * Save this graph.
     *
	 * @param path the path string
	 * @throws GsException  exeption returned
	 */
	void save(String path) throws GsException;
	
	/**
	 * Save some components of this graph.
     *
	 * @param path the path string
	 * @param edges the edges collection
	 * @param nodes the nodes collection
	 * @throws GsException graph exception
	 */
	void save(String path, Collection<V> nodes, Collection<E> edges) throws GsException;

	default void saveAssociated(ZipOutputStream zos, OutputStreamWriter osw) throws IOException {}

	default void parseAssociated(ZipFile f, String prefix) throws GsException {}

	/**
	 * Add a listener for view change events.
	 * Note: only one listener is supported: the GraphGUI
	 * @param listener GraphViewListener listener
	 */
	void addViewListener(GraphViewListener listener);
	
	/**
	 * Transfer view settings from a related graph.
	 * The position and styles of matching nodes and edges
	 * will be copied from the source graph to the current one
	 * 
	 * @param src the source graph
	 * @param helper    copy helper
	 */
	void copyView(Graph<V,E> src, ViewCopyHelper<Graph<V,E>,V,E> helper);
	
	/**
	 * The graph has changed, all listeners will be notified.
	 * It will also be marked as unsaved.
	 * @param type graphchange type
     * @param data object data
	 */
	void fireGraphChange(GraphChangeType type, Object data);

	/**
	 * Retrieve the name to use when displaying a graph node.
	 *
	 * @param node the V node
	 * @return the display name
	 */
	String getDisplayName(V node);

	/**
	 * Retrieve graph-level attributes
	 *
	 * @return a map of attribute values, it can be empty but should not be null
	 */
	Map<String,String> getAttributes();

	/**
	 * Setter attribute
	 * @param name  name to set
	 * @param value value to set
	 */
	void setAttribute(String name, String value);

	/**
	 * update Evsmap attribute
	 */
	void updateEvsmap();
}
