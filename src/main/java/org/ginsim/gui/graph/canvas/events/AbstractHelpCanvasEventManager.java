package org.ginsim.gui.graph.canvas.events;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import org.ginsim.core.graph.common.Graph;
import org.ginsim.gui.graph.AddNodeAction;
import org.ginsim.gui.graph.EditAction;
import org.ginsim.gui.graph.EditMode;
import org.ginsim.gui.graph.canvas.CanvasEventManager;
import org.ginsim.gui.graph.canvas.GraphCanvasRenderer;
import org.jgrapht.event.GraphEdgeChangeEvent;

abstract public class AbstractHelpCanvasEventManager implements CanvasEventManager {

	protected final GraphCanvasRenderer renderer;

	public AbstractHelpCanvasEventManager(GraphCanvasRenderer renderer) {
		this.renderer = renderer;
	}
	
	@Override
	public void helpOverlay(Graphics2D g, Rectangle area) {
		g.setColor(Color.LIGHT_GRAY);
		g.fill(area);
		g.setColor(Color.BLACK);
		g.drawString("Using the canvas:", 50, 50);
		g.drawString("Help: Click to hide, '?' or 'h' to show it again", 70, 70);
		g.drawString("Zoom: ctrl + mouse-wheel", 70, 90);
		g.drawString("Scroll: shift for faster scrolling, alt for horizontal scrolling", 70, 110);
		
		g.drawLine(20, 130, area.width-20, 130);
		extraHelpOverlay(g, area, 150);
	}

	abstract void extraHelpOverlay(Graphics2D g, Rectangle area, int voffset);

}
