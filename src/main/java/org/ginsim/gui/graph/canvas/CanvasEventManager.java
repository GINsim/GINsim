package org.ginsim.gui.graph.canvas;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public interface CanvasEventManager {
	/**
	 * Click function
	 * @param p point
	 * @param alternate boolean yes or no alternate
	 */
	void click(Point p, boolean alternate);

	/**
	 * Preseed function
	 * @param p point
	 * @param alternate boolean yes or no alternate
	 */
	void pressed(Point p, boolean alternate);

	/**
	 * release function
	 * @param p point
	 */
	void released(Point p);

	/**
	 * Dragged funtion
	 * @param p point
	 */
	void dragged(Point p);

	/**
	 * Cancel function
	 */
	void cancel();

	/**
	 * Overlay function
	 * @param g graph in 2D
	 * @param area rectangle area
	 */
	void overlay(Graphics2D g, Rectangle area);

	/**
	 * help function
	 * @param g  the graph
	 * @param area  the area
	 */
	void helpOverlay(Graphics2D g, Rectangle area);
}
