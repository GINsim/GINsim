package org.ginsim.core.graph.view;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;

import org.ginsim.common.xml.XMLWriter;

/**
 * Interface AttributeReader
 */
public interface AttributesReader {

	/**
	 * Let listeners know that the item has been updated
	 */
	void refresh();
	
	/**
	 * The current item will be changed: mark its current area as damaged if needed.
	 */
	void damage();

	
	/**
	 * get the boundary rectangle for the selected item.
	 * @return a rectangle representing the bounds
	 */
	Rectangle getBounds();
	
	
	/**
	 * Test if a point can be used to select the current item.
	 * 
	 * @param p point
	 * @return true if the item should be selected
	 */
	boolean select(Point p);
	
    /**
     * Move the item.
     * 
     * @param dx position x
     * @param dy position y
     */
    void move(int dx, int dy);

    /**
     * Save the visual settings in GINML.
     * 
     * @param writer  xml writer
     * @throws IOException io exception
     */
	void writeGINML(XMLWriter writer) throws IOException;
}
