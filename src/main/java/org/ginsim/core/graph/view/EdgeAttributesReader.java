package org.ginsim.core.graph.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.view.style.EdgeStyle;

/**
 * graphic info on an edge.
 * this extension of the GsGraphManager is used to get or change visual info on edges objects.
 * It can also change default value (but they aren't persistant yet).
 * @param <V> vertex
 * @param <E> the edge
 */
public interface EdgeAttributesReader<V,E extends Edge<V>> extends AttributesReader {

	/**
	 * Get the default style used for the edges in this graph.
	 * @return the default style for edges.
	 */
	EdgeStyle<V, E> getDefaultEdgeStyle();
	
	/**
	 * @return the width of this edge
	 */
	float getLineWidth();
	/**
	 * set the edge on which we work
	 * @param obj
	 */
	void setEdge(E obj);

	/**
	 * Set the edge on which we work, knowing that it is selected.
	 * 
	 * @param obj
	 * @param selected
	 */
	void setEdge(E obj, boolean selected);
	
	/**
	 * @return the color of this edge
	 */
	Color getLineColor();

	/**
	 * Is this edge curve?
	 * This only affects edges with intermediate points.
	 * 
	 * @return true if the edge is curve
	 */
	boolean isCurve();

	/**
	 * Set the edge as curve property.
	 * 
	 * @param curve
	 */
	void setCurve(boolean curve);
	
	/**
	 * @return the list of points used by this edge.
	 */
	List<Point> getPoints();

    /**
     * set points used by this edge
     * @param l
     */
    void setPoints(List<Point> l);
    /**
     * @param fereader
     */
    void copyFrom(EdgeAttributesReader fereader);

    /**
     * @return the lineEnd
     */
    EdgeEnd getLineEnd();

    /**
     * Check if the graph also contains the reversed edge.
     *
     * @return true if the reverse edge is in the graph, false otherwise (or for self-loops)
     */
    boolean hasReverseEdge();

    /**
     * set the edge as dashed.
     * @return the dash motif
     */
    EdgePattern getDash();
    
    /**
     * Render an edge on a given graphics.
     * 
     * @param g
     */
	void render(Graphics2D g);

	/**
	 * Render an edge while the edge or at least one of its connected node is moved.
	 * 
	 * @param g
	 * @param type
	 * @param movex
	 * @param movey
	 */
	void renderMoving(Graphics2D g, MovingEdgeType type, int movex, int movey);
	
	void renderMovingPoint(Graphics2D g, int idx, int movex, int movey);

	void setStyle(EdgeStyle style);

	EdgeStyle getStyle();
    
	EdgeAnchor getAnchor();
	
	void setAnchor(EdgeAnchor anchor);

}
