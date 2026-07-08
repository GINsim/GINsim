package org.ginsim.core.graph.view;

import java.awt.Point;
import java.util.List;

import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.view.style.EdgeStyle;

/**
 * Store basic view information on an edge: intermediate points and optional style.
 * 
 * @author Aurelien Naldi
 *
 * @param <V>  vertex
 * @param <E>  edge
 */
public interface EdgeViewInfo<V, E extends Edge<V>> {

	List<Point> getPoints();
	
	EdgeStyle<V, E> getStyle();
	
	void setStyle(EdgeStyle<V,E> style);
	
	boolean isCurve();
	
	void setCurve(boolean curve);
	
	EdgeAnchor getAnchor();
	
	void setAnchor(EdgeAnchor anchor);

	void setPoints(List<Point> l);
}
