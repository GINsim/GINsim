package org.ginsim.core.graph.view.style;

import java.awt.Color;

import org.ginsim.core.graph.view.NodeBorder;
import org.ginsim.core.graph.view.NodeShape;

/**
 * Define the visual parameters of a node.
 * 
 * @author Aurelien Naldi
 *
 * @param <V> the vertex
 */
public interface NodeStyle<V> extends Style {

	Color getBackground(V obj);
	Color getForeground(V obj);
	Color getTextColor(V obj);
	
	int getWidth(V obj);
	int getHeight(V obj);
	
	NodeShape getNodeShape(V obj);
	NodeBorder getNodeBorder(V obj);

	boolean enforceColors();
	boolean enforceShape();
	boolean enforceSize();
	boolean enforceBorder();
	
	boolean matches(NodeShape shape, Color bg, Color fg, Color text, int w, int h);

    /**
     * get the CSS class(es) used by a specific node.
     *
     * @param node
     * @return a String with one or several class names.
     */
    String getCSSClass(V node);
}
