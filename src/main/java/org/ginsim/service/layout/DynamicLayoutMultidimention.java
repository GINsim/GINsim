package org.ginsim.service.layout;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.GraphChangeType;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.dynamicgraph.DynamicNode;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.graph.view.ViewHelper;


/**
 * Layout State Transition Graphs: place nodes according to activity levels.
 * This layout should use a pivot to handle STG with more dimensions, but this part seems broken.
 *
 * @author Duncan Berenguier
 */
public class DynamicLayoutMultidimention extends BaseSTGLayout {
    private static final int padx = 25;
    private static final int pady = 25;
   
    private int width;
	private int height;
    
    private int pivot;

	private EdgeAttributesReader ereader;
    private NodeAttributesReader vreader;
	
    private final Color[] colorPalette;
	private final DynamicGraph graph;
	private final byte[] newNodeOrder;
	private final boolean useStraightEdges;

	public DynamicLayoutMultidimention(DynamicGraph graph, byte[] nodeOrder, boolean straightEdges, Color[] colorPalette) throws GsException{
		this.graph = graph;
		this.newNodeOrder = nodeOrder;
		this.colorPalette = colorPalette;
		this.useStraightEdges = straightEdges;
		
		runLayout();
	}
    

	public void runLayout() throws GsException{
        //Check if it is a DynamicGraph
		Iterator it = graph.getNodes().iterator();
		Object v = it.next();
	    if (v == null || !(v instanceof DynamicNode)) {
	    	LogManager.error( "Wrong type of graph for this layout");
	    	return;
	    }
		vreader = graph.getNodeAttributeReader();
		ereader = graph.getEdgeAttributeReader();
		
	    byte[] maxValues = getMaxValues( graph.getAssociatedGraph().getNodeOrder());
	    
	    //move the nodes
	    DynamicNode vertex = (DynamicNode)v;
	    vreader.setNode(vertex);
	    this.width = vreader.getWidth() + padx*maxValues.length/2;
	    this.height = vreader.getHeight() + pady*maxValues.length/2;	   
	    
	    do {
	    	moveNode(vertex, maxValues);
		    vertex = (DynamicNode)it.next();
		} while (it.hasNext());
    	moveNode(vertex, maxValues);
    	
    	//move the edges
    	it = graph.getEdges().iterator();
    	for (Edge edge: graph.getEdges()) {
    		moveEdge(edge, maxValues);
    	}

        graph.fireGraphChange(GraphChangeType.GRAPHVIEWCHANGED, null);
    }
	
	/**
	 * Move the node to its correct position.
     *
	 * @param node
	 * @param maxValues
	 */
	private void moveNode(DynamicNode node, byte[] maxValues) {
	    vreader.setNode(node);
    	byte[] state = node.state;
       	int x = 0;
    	int dx = 1;
    	for (int i = 0; i < pivot; i++) {
			x += getState(state, i)*dx;
			dx *= maxValues[i];
		}
    	int y = 0;
    	int dy = 1;
    	for (int i = pivot; i < maxValues.length; i++) {
			y += getState(state, i)*dy;
			dy *= maxValues[i];
		}
	    vreader.setPos(5+x*width, 5+y*height);
        vreader.refresh();		
	}
	
	/**
	 * Move an edge and set the proper style.
     *
	 * @param edge
	 * @param maxValues
	 */
	private void moveEdge(Edge edge, byte[] maxValues) {
		byte[] diffstate = getDiffStates((DynamicNode)edge.getSource(), (DynamicNode)edge.getTarget());
		
		ereader.setEdge(edge);
	   	List points = ViewHelper.getPoints(vreader, ereader, edge);
		Point2D first, p1, p2, last;
		first = (Point2D)points.get(0);
		last =  (Point2D)points.get(points.size()-1);
		p1 =(Point2D) first.clone();
		p2 = null;
		double dx, dy;
		double pad = 25;

		if (useStraightEdges) {
			dx = get_dx(diffstate, maxValues, 0);
			dy = get_dy(diffstate, maxValues, 0);
			if (dx > 0 && dy > 0 ) { //the edge is diagonal
				dx = get_dx(diffstate, maxValues, 1);
				dy = get_dy(diffstate, maxValues, 1);
				p1.setLocation(first.getX()+(last.getX()-first.getX())/2+dy*pad, first.getY()+(last.getY()-first.getY())/2+dx*pad);
			} else {
				p2 =(Point2D) last.clone();
				int w = vreader.getWidth();
				int h = vreader.getHeight();
				p1.setLocation(first.getX()+gap(dx, dy, w, h), first.getY()+gap(dy, dx, h, w));
				p2.setLocation( last.getX()+gap(dx, dy, w, h),  last.getY()+gap(dy, dx, h, w));
			}
		} else {
			dx = get_dx(diffstate, maxValues, 1);
			dy = get_dy(diffstate, maxValues, 1);
			p1.setLocation(p1.getX()+(last.getX()-p1.getX())/2+dy*pad, p1.getY()+(last.getY()-p1.getY())/2+dx*pad);
		}
		points = new LinkedList();
    	points.add(first);
    	points.add(p1);
		if (p2 != null) points.add(p2);
    	points.add(last);
		ereader.setPoints(points);
		
		ereader.setCurve(p2 == null);
		ereader.refresh();
	}

    /**
     * Compute a gap used to place straight edges.
     *
     * @param d_main
     * @param d_orth
     * @param size_main
     * @param size_orth
     * @return a computed gap for the edge.
     */
    private double gap(double d_main, double d_orth, int size_main, int size_orth) {
		return size_main/1.75*(d_orth>0?1:0)+d_orth*12+d_main*3-size_orth/4;
	}

    /**
     * Compute a distance from "no change between two states".
     * The change have different weights depending on their index in the newNodeOrder.
     *
     * @param diffstate     represents the differences between the two states
     * @param maxValues     the maximal levels for all components
     * @param start         the first component to consider
     *
     * @return a value representing the distance between the two states.
     */
	private double get_dx(byte[] diffstate, byte[] maxValues, int start) {
    	int dx = 0;
    	int ddx = 1;
    	for (int i = start; i < diffstate.length/2; i++) {
			dx +=  diffstate[i]*ddx;
			ddx *= maxValues[i];
		}
		return dx;
	}
    /**
     * Compute a distance from "no change between two states".
     * The change have different weights depending on their index in the newNodeOrder.
     *
     * @param diffstate     represents the differences between the two states
     * @param maxValues     the maximal levels for all components
     * @param start         the first component to consider
     * @return a value representing the distance between the two states.
     */
	private double get_dy(byte[] diffstate, byte[] maxValues, int start) {
      	int dx = 0;
    	int ddx = 1;
    	for (int i = pivot+start; i < diffstate.length; i++) {
			dx +=  diffstate[i]*ddx;
			ddx *= maxValues[i];
		}
		return dx;
	}

}
