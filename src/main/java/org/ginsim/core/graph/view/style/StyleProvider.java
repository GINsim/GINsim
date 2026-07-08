package org.ginsim.core.graph.view.style;

import org.ginsim.core.graph.Edge;

import java.util.Map;
import java.awt.Color;


/**
 * Define a style provider: it provides styles for nodes and edges
 * and can be applied to a StyleManager to temporarily override the graph styles.
 * 
 * @author Aurelien Naldi
 *
 * @param <V> the vertex
 * @param <E> the edge
 */
public interface StyleProvider<V, E extends Edge<V>> {

	/**
	 * Get the style to use for a node.
	 * 
	 * @param node the node
	 * @param baseStyle the style to use as fallback
	 * @return the style to use or null to use the graph's style
	 */
	NodeStyle<V> getNodeStyle(V node, NodeStyle<V> baseStyle);
	
	/**
	 * Get the style used for an edge.
	 * @param edge
	 * @param baseStyle the style to use as fallback
	 * @return the style to use, or null to use the graph's style
	 */
	EdgeStyle<V, E> getEdgeStyle(E edge, EdgeStyle<V, E> baseStyle);

	default String getCSS() {
		return "";
	}

	default Map<Object, Color>  getMColorsMap(){
		return null;
	};
}
