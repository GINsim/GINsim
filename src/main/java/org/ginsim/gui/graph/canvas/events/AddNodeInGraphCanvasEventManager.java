package org.ginsim.gui.graph.canvas.events;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import org.ginsim.gui.graph.AddNodeAction;
import org.ginsim.gui.graph.EditAction;
import org.ginsim.gui.graph.canvas.GraphCanvasRenderer;

/**
 * class AddNodeInGraphCanvasEventManager
 */
public class AddNodeInGraphCanvasEventManager extends AbstractHelpCanvasEventManager {
	/**
	 * Constructor AddNodeInGraphCanvasEventManager
	 * @param renderer the GraphCanvasRenderer
	 */
	public AddNodeInGraphCanvasEventManager(GraphCanvasRenderer renderer) {
		super(renderer);
	}
	
	@Override
	public void click(Point p, boolean alternate) {
		EditAction currentAction = renderer.amanager.getSelectedAction();
		if (currentAction instanceof AddNodeAction) {
			AddNodeAction action = (AddNodeAction)currentAction;
			action.addNode(renderer.amanager, p.x, p.y);
		}
	}

	@Override
	public void pressed(Point p, boolean alternate) {
	}

	@Override
	public void released(Point p) {
	}

	@Override
	public void dragged(Point p) {
	}

	@Override
	public void cancel() {
	}

	@Override
	public void overlay(Graphics2D g, Rectangle area) {
	}

	@Override
	void extraHelpOverlay(Graphics2D g, Rectangle area, int voffset) {
		g.drawString("Click on the canvas to add a new node", 70, voffset);
	}


}
