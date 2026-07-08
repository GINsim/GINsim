package org.ginsim.gui.graph.view.style;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;

import org.ginsim.core.graph.view.EdgeAnchor;

/**
 * Custom widget to select the anchor of self-edges
 *  
 * @author Aurelien Naldi
 */
@SuppressWarnings("serial")
public class AnchorSelector extends JButton implements MouseListener {

	private EdgeAnchor anchor = null;
	private final EdgeRoutingPanel routing;

	/**
	 * Constructor
	 * @param routing object EdgeRoutingPanel
	 */
	public AnchorSelector(EdgeRoutingPanel routing) {
		super("A");
		this.routing = routing;
		addMouseListener(this);
	}

	/**
	 * Setter for  EdgeAnchor
	 * @param edgeAnchor object  EdgeAnchor
	 */
	public synchronized void setAnchor(EdgeAnchor edgeAnchor) {
		this.anchor = edgeAnchor;
		repaint();
	}

	/**
	 * Getter Edge anchor
	 * @return  EdgeAnchor object
	 */
	public EdgeAnchor getAnchor() {
		return anchor;
	}

	/**
	 * Paint component
	 * @param g the <code>Graphics</code> object to protect
	 */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);       

        // Retrieve component dimension
        Dimension dim = getSize();
        int w = (int)dim.getWidth()-1;
        int h = (int)dim.getHeight()-1;
        int mw = w/2, mh=h/2;

        g.setColor(Color.WHITE);
		g.fillRect(0,0,w,h);
		
        g.setColor(Color.GRAY);
        g.drawLine(0,  0, w,  0);
        g.drawLine(0, mh, w, mh);
        g.drawLine(0,  h, w,  h);
        
        g.drawLine( 0, 0,  0, h);
        g.drawLine(mw, 0, mw, h);
        g.drawLine( w, 0,  w, h);
        
        if (anchor == null) {
        	return;
        }
        
        g.setColor(Color.BLACK);
        switch (anchor) {
		case NE:
			g.fillRect(mw,  0, mw, mh);
			break;
		case NW:
			g.fillRect( 0,  0, mw, mh);
			break;
		case SE:
			g.fillRect(mw, mh, mw, mh);
			break;
		case SW:
			g.fillRect( 0, mh, mw, mh);
			break;
		default:
			break;
		}
    }

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
        // Retrieve component dimension
        Dimension dim = getSize();

        EdgeAnchor newAnchor = anchor;
        if (x > dim.getWidth()/2) {
        	if (y > dim.getHeight()/2) {
        		newAnchor = EdgeAnchor.SE;
        	} else {
        		newAnchor = EdgeAnchor.NE;
        	}
        } else {
        	if (y > dim.getHeight()/2) {
        		newAnchor = EdgeAnchor.SW;
        	} else {
        		newAnchor = EdgeAnchor.NW;
        	}
        }
        
        if (newAnchor != anchor) {
        	setAnchor(newAnchor);
        	routing.updateAnchor();
        }
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}  
}
