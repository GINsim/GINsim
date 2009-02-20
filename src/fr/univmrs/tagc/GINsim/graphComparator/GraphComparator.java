package fr.univmrs.tagc.GINsim.graphComparator;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;

import fr.univmrs.tagc.GINsim.graph.GsEdgeAttributesReader;
import fr.univmrs.tagc.GINsim.graph.GsGraph;
import fr.univmrs.tagc.GINsim.graph.GsGraphManager;
import fr.univmrs.tagc.GINsim.graph.GsGraphicalAttributesStore;
import fr.univmrs.tagc.GINsim.graph.GsVertexAttributesReader;
import fr.univmrs.tagc.common.GsException;

/**
 * Compare 2 GsGraph
 * @author Berenguier Duncan
 * @since January 2009
 *
 */
public abstract class GraphComparator {
	protected GsGraphManager gm, g1m, g2m;
	protected HashMap verticesMap;
	protected GsGraphicalAttributesStore g1gas, g2gas;
	
	public static Color SPECIFIC_G1_COLOR = new Color(0, 255, 0); //green
	public static Color SPECIFIC_G2_COLOR = new Color(255, 0, 0); //red
	public static Color COMMON_COLOR = new Color(51, 153, 255);   //blue

	protected GraphComparator() {}

	/**
	 * Return an HashMap containing all the vertices from both graphs. 
	 * With the IDs as a key and the corresponding color (SPECIFIC_G1_COLOR, SPECIFIC_G2_COLOR or COMMON_COLOR) as a value. 
	 * 
	 * @return the verticesMap
	 */	
	public HashMap getVerticesMap() {
		return verticesMap;
	}
	
	/**
	 * Build the basic topology for the diff graph (vertex+edges) by calling others functions
	 *  1) addVerticesFromGraph on both graphs
	 *  2) setVerticesColor
	 *  3) addEdgesFromGraph on each node on both graphs
	 */
	public void buildDiffGraph() {
		setDiffGraphName();
		addVerticesFromGraph(g1m);
		addVerticesFromGraph(g2m);
		
		g1gas = new GsGraphicalAttributesStore(g1m);
		g2gas = new GsGraphicalAttributesStore(g2m);
		setVerticesColor();
		
		GsEdgeAttributesReader ereader = gm.getEdgeAttributesReader();
		for (Iterator it = verticesMap.keySet().iterator(); it.hasNext();) { 		//For all edges
			String id = (String) it.next();
			Color col = (Color) verticesMap.get(id);
			
			addEdgesFromGraph(g1m, g2m, id, col, SPECIFIC_G1_COLOR, ereader);
			addEdgesFromGraph(g2m, g1m, id, col, SPECIFIC_G2_COLOR, ereader);
		}
	}

	/**
	 * Copy the vertex graphical attributes from the vertex source, to the newly created vertex v. The background color of the vertex is set to the parent color.
	 * 
	 * @param v the vertex just created
	 * @param source the vertex to copy from
	 * @param vreader a vertexAttributesReader for the new graph
	 * @param vsourcereader a vertexAttributesReader for the old graph
	 * @param col the color to apply to its background
	 */
	protected void mergeVertexAttributes(Object v, Object source, GsVertexAttributesReader vreader, GsVertexAttributesReader vsourcereader, Color col) {
		vreader.setVertex(v);
		if (source != null) {
			vsourcereader.setVertex(source);
			vreader.copyFrom(vsourcereader);
		}
		vreader.setBackgroundColor(col);
		vreader.refresh();			
	}

	/**
	 * Copy the edge graphical attributes from the edge source, to the newly created edge e. The line color for the edge is set to the parent color.
	 * 
	 * @param e the edge just created
	 * @param source the edge to copy from
	 * @param ereader a vertexAttributesReader for the new graph
	 * @param esourcereader a vertexAttributesReader for the graph to copy from
	 * @param col the color to apply to its lineColor
	 */
	protected void mergeEdgeAttributes(Object e, Object source, Color col, GsEdgeAttributesReader ereader, GsEdgeAttributesReader esourcereader) {
		ereader.setEdge(e);
		esourcereader.setEdge(source);
		ereader.copyFrom(esourcereader);
		ereader.setLineColor(col);
		ereader.refresh();			
	}
	/**
	 * define the name of the diff graph
	 */
	protected void setDiffGraphName() {
		try {
			getDiffGraph().setGraphName("diff_"+getG1().getGraphName()+"_"+getG1().getGraphName());
		} catch (GsException e) {} //Could not append normally, the g1 and g2 graph name can't be invalid at this point
	}
	
	/**
	 * Add all the vertices from a graph to the verticeMap.
	 * The key of the map should be the vertex ID
	 * The value should be null for the moment.
	 * @param gm the graph manager for the graph containing the vertices.
	 */
	abstract protected void addVerticesFromGraph(GsGraphManager gm);

	 /**
	 * Set the value for the vertex to the right color in the verticeMap.
	 */
	abstract protected void setVerticesColor() ;
	
	/**
	 * Add the edges for one vertex from one graph to the merge graph.
	 * 
	 * @param gm the graph manager for the studied graph
	 * @param gm_aux a graph manager for the other graph
	 * @param id the vertex id's 
	 * @param vcol the vertex color (parent)
	 * @param pcol the color corresponding to the studied graph (gm).
	 * @param ereader an edge attribute reader for the diff graph.
	 * 
	 */
	abstract protected void addEdgesFromGraph(GsGraphManager gm, GsGraphManager gm_aux, String id, Color vcol, Color pcol, GsEdgeAttributesReader ereader);
	
	/**
	 * Return a merge graph colored to indicates vertices and edges parent graph.
	 * @return the diff graph
	 */
	abstract public GsGraph getDiffGraph();
	/**
	 * Return the first graph to compare
	 * @return the graph
	 */
	abstract public GsGraph getG1();
	/**
	 * Return the second graph to compare
	 * @return the graph
	 */
	abstract public GsGraph getG2();
}