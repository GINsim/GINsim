package fr.univmrs.tagc.GINsim.graphComparator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.univmrs.tagc.GINsim.annotation.Annotation;
import fr.univmrs.tagc.GINsim.css.VertexStyle;
import fr.univmrs.tagc.GINsim.data.GsDirectedEdge;
import fr.univmrs.tagc.GINsim.graph.GsEdgeAttributesReader;
import fr.univmrs.tagc.GINsim.graph.GsGraph;
import fr.univmrs.tagc.GINsim.graph.GsGraphManager;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryEdge;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraph;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryMultiEdge;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryVertex;
import fr.univmrs.tagc.GINsim.regulatoryGraph.LogicalParameterList;
import fr.univmrs.tagc.GINsim.regulatoryGraph.OmddNode;

/**
 * Compare 2 GsRegulatoryGraph
 * @author Berenguier Duncan
 * @since January 2009
 *
 */
public class RegulatoryGraphComparator extends GraphComparator {
	public static final Color COMMON_COLOR_DIFF_FUNCTIONS = new Color(0, 0, 255);
	public static final Color COMMON_COLOR_DIFF_MAXVALUES = new Color(115, 194, 220);
	private GsRegulatoryGraph g, g1, g2; //g is the graph merging g1 and g2, the graphs to compare.
	/**
	 * indicates if the node order of both graph is the same.
	 */
	private boolean sameNodeOrder;
	private List logicalFunctionPending;
	private Map meMap;

	public RegulatoryGraphComparator(GsGraph g1, GsGraph g2, GsGraph g) {
        if (g  == null || !(g  instanceof GsRegulatoryGraph))  return;
        if (g1 == null || !(g1 instanceof GsRegulatoryGraph))  return;
        if (g2 == null || !(g2 instanceof GsRegulatoryGraph))  return;
       	this.g = (GsRegulatoryGraph)g; 
       	this.g1 = (GsRegulatoryGraph)g1; 
       	this.g2 = (GsRegulatoryGraph)g2;
       	
		g1m = g1.getGraphManager(); g2m = g2.getGraphManager(); gm = g.getGraphManager();
		stylesMap = new HashMap();
		logicalFunctionPending = new ArrayList();
		
		sameNodeOrder = compareNodeOrder();
		if (!sameNodeOrder) {
			String comment = "diff: The node order is the same for both graph";
			log(comment+"\n");
			g.getAnnotation().appendToComment(comment);
			log(((GsRegulatoryGraph) g1).getNodeOrder()+"\n");
			log(((GsRegulatoryGraph) g2).getNodeOrder()+"\n");
		}
		buildDiffGraph();
	}
	
	public RegulatoryGraphComparator(GsRegulatoryGraph g1, GsRegulatoryGraph g2) {
		this(g1, g2, new GsRegulatoryGraph());
	}
	
	public boolean isCommonVertex(Object id) {
		VertexStyle style = (VertexStyle)((ItemStore)stylesMap.get(id)).v;
		return style.background != SPECIFIC_G1_COLOR && style.background != SPECIFIC_G2_COLOR;
	}
	
	public void buildDiffGraph() {
		super.buildDiffGraph();
		
		meMap = new HashMap();
		GsEdgeAttributesReader ereader = gm.getEdgeAttributesReader();
		for (Iterator it = gm.getEdgeIterator(); it.hasNext();) {
			GsDirectedEdge e = (GsDirectedEdge) it.next();
			GsRegulatoryMultiEdge me = (GsRegulatoryMultiEdge)e.getUserObject();
			String sid = ((GsRegulatoryVertex) e.getSourceVertex()).getId();
			String tid = ((GsRegulatoryVertex) e.getTargetVertex()).getId();
			
			GsDirectedEdge e1 = (GsDirectedEdge) g1m.getEdge((GsRegulatoryVertex) g1m.getVertexByName(sid), (GsRegulatoryVertex) g1m.getVertexByName(tid));
			GsDirectedEdge e2 = (GsDirectedEdge) g2m.getEdge((GsRegulatoryVertex) g2m.getVertexByName(sid), (GsRegulatoryVertex) g2m.getVertexByName(tid));
			
			String comment = "The edge "+me.toToolTip()+" ";
			ereader.setEdge(me);
			ereader.setRouting(GsEdgeAttributesReader.ROUTING_AUTO);
			Color col = ereader.getLineColor();
			if (col == SPECIFIC_G1_COLOR) comment+= "is specific to g1";
			else if (col == SPECIFIC_G2_COLOR) comment+= "is specific to g2";
			else comment+= "is common to both graphs";
			int edgeCount = ((GsRegulatoryMultiEdge) me.getUserObject()).getEdgeCount();
			for (int j = 0; j < edgeCount; j++) {
				((GsRegulatoryMultiEdge) me.getUserObject()).getGsAnnotation(j).appendToComment(comment);
			}
			log(comment+"\n");
			if (e1 != null && e2 != null) compareEdges((GsRegulatoryMultiEdge)e1.getUserObject(), (GsRegulatoryMultiEdge)e2.getUserObject());
			if (e1 != null) meMap.put((GsRegulatoryMultiEdge)e1.getUserObject(),me);
			else if (e2 != null) meMap.put((GsRegulatoryMultiEdge)e2.getUserObject(),me);
		}
		setAllLogicalFunctions();
		meMap = null;
		logicalFunctionPending = null;
	}
	
	protected void setVerticesColor() {
		for (Iterator it=verticesIdsSet.iterator() ; it.hasNext() ;) {	//For all the vertices
			GsRegulatoryVertex v, v1, v2;
			String id = (String)it.next();
			v1 = (GsRegulatoryVertex)g1m.getVertexByName(id);
			v2 = (GsRegulatoryVertex)g2m.getVertexByName(id);
			String comment = null;
			
			//Check which graph own the vertex, set the appropriate color to it and if it is owned by both graph, compare its attributes.
			if (v1 == null) {
				comment = "The vertex "+id+" is specific to "+g2.getGraphName()+"\n";
				v = g.addNewVertex(id, v2.getName(), v2.getMaxValue());
				mergeVertexAttributes(v, v2, null, gm.getVertexAttributesReader(), g2m.getVertexAttributesReader(), null, SPECIFIC_G2_COLOR);
				setLogicalFunction(v, v2, g2);
			} else if (v2 == null) {
				comment = "The vertex "+id+" is specific to "+g1.getGraphName()+"\n";
				v = g.addNewVertex(id, v1.getName(), v1.getMaxValue());
				mergeVertexAttributes(v, v1, null, gm.getVertexAttributesReader(), g1m.getVertexAttributesReader(), null, SPECIFIC_G1_COLOR);
				setLogicalFunction(v, v1, g1);
			} else {
				comment = "The vertex "+id+" is common to both graphs\n";
				v = g.addNewVertex(id, v1.getName(), (short) Math.max(v1.getMaxValue(), v2.getMaxValue()));
				Color[] color = {COMMON_COLOR};
				comment += compareVertices(v ,v1, v2, color);
				mergeVertexAttributes(v, v1, v2, gm.getVertexAttributesReader(), g1m.getVertexAttributesReader(), g2m.getVertexAttributesReader(), color[0]);
				setLogicalFunction(v, v1, g1);
			}
			Annotation gsa;
			if (v1 != null) gsa = v1.getAnnotation();
			else gsa = v2.getAnnotation();
			if (gsa != null) gsa = (Annotation) gsa.clone();
			else gsa = new Annotation();
			gsa.appendToComment(comment);
			v.setGsa(gsa);
			log(comment);
		}		
	}

	protected void addVerticesFromGraph(GsGraphManager gm) {
		for (Iterator it=gm.getVertexIterator() ; it.hasNext() ;) {
			GsRegulatoryVertex vertex = (GsRegulatoryVertex)it.next();
			verticesIdsSet.add(vertex.getId());
		}
	}

	
	protected void addEdgesFromGraph(GsGraphManager gm_main, GsGraphManager gm_aux, String id, Color vcol, Color pcol, GsEdgeAttributesReader ereader) {
		GsRegulatoryVertex v = (GsRegulatoryVertex) gm_main.getVertexByName(id);
		GsRegulatoryEdge e = null;
		GsDirectedEdge e1, e2;
		GsEdgeAttributesReader e1reader = gm_main.getEdgeAttributesReader();
		GsEdgeAttributesReader e2reader = gm_aux.getEdgeAttributesReader();

		if (v != null) { //If v is a vertex from the studied graph, we look at its edges
			for (Iterator edge_it = gm_main.getOutgoingEdges(v).iterator(); edge_it.hasNext();) {
				e1 = (GsDirectedEdge) edge_it.next();
				GsRegulatoryMultiEdge me1 = (GsRegulatoryMultiEdge)e1.getUserObject();
				String tid = ((GsRegulatoryVertex)e1.getTargetVertex()).getId();
				GsRegulatoryVertex target = (GsRegulatoryVertex) gm.getVertexByName(tid);
				GsRegulatoryVertex source = (GsRegulatoryVertex) gm.getVertexByName(id);
				
				if (gm.getEdge(source, target) != null) {
					continue;
				}
				for (int i = 0; i < me1.getEdgeCount(); i++) {
					e = g.addNewEdge(id, tid, (short)me1.getMin(i) , (short)me1.getSign(i));
				}
				if (vcol == SPECIFIC_G1_COLOR || vcol == SPECIFIC_G2_COLOR|| !isCommonVertex(target)) { //The edge's vertices are specific to a graph therefore the edge is specific, and we add it with the right color.
					mergeEdgeAttributes(e.me, me1, null, pcol, ereader, e1reader, null);
				} else { //source and target are common to both graph.
					e2 = (GsDirectedEdge) gm_aux.getEdge(gm_aux.getVertexByName(id), gm_aux.getVertexByName(tid));
					if (e2 != null)	mergeEdgeAttributes(e.me, me1, (GsRegulatoryMultiEdge)e2.getUserObject(), vcol, ereader, e1reader, e2reader);
					else			mergeEdgeAttributes(e.me, me1, null, pcol, ereader, e1reader, null);
				}
			}
		}
	}


	public String compareVertices(GsRegulatoryVertex v, GsRegulatoryVertex v1, GsRegulatoryVertex v2, Color[] color) {
		String comment = "";
		if (!v1.getName().equals(v2.getName())) {
			String n1 = v1.getName();
			String n2 = v2.getName();
			if (n1.equals("")) {
				v.setName(n2);
				n1 = "no name";
			}
			if (n2.equals("")) n2 = "no name";
			comment += "   names are differents : "+n1+" and "+n2+"\n";
		}
		if (v1.getMaxValue() != v2.getMaxValue()) {
			short mv1 = v1.getMaxValue();
			short mv2 = v2.getMaxValue();
			comment += "   max values are differents : "+mv1+" and "+mv2+"\n";
			color[0] = COMMON_COLOR_DIFF_MAXVALUES;
		} else if (sameNodeOrder) comment += compareLogicalFunction(v1, v2, color); //Compare logical function only if they have the same maxValue.
		return comment;
	}

	
	/**
	 * Compare the logical function of vertex 'v1' and 'v2'.
	 * @param v1 
	 * @param v2
	 */
	private String compareLogicalFunction(GsRegulatoryVertex v1, GsRegulatoryVertex v2, Color[] color) {
		String comment = "";
		OmddNode omdd1 = v1.getTreeParameters(g1);
		OmddNode omdd2 = v2.getTreeParameters(g2);
		if (!compareLogicalFunction(omdd1, omdd2)) {
			comment = "   logical functions are differents : \n      "+omdd1+"\n      "+omdd2;
			color[0] = COMMON_COLOR_DIFF_FUNCTIONS;
		}
		return comment;
	}
	private boolean compareLogicalFunction(OmddNode omdd1, OmddNode omdd2) {
		if (omdd1.level != omdd2.level) return false;
		//if (omdd1.min 	!= omdd2.min) 	return false; //TODO : usefull to compare ?
		//if (omdd1.max 	!= omdd2.max) 	return false;
		if (omdd1.value != omdd2.value) return false;
		if (omdd1.next != null && omdd2.next != null) {
			if (omdd1.next.length != omdd2.next.length) 	return false;
			int i = 0;
			while (i < omdd1.next.length) {
				if (compareLogicalFunction(omdd1.next[i], omdd2.next[i]) == false) return false;
				i++;
			}
		} 
		return true;
	}

	/**
	 * Set the logical function for the new node 'v' to the logical function contained in the node 'v_source' of the graph 'g_source'
	 * 
	 * @param v 
	 * @param v_source
	 * @param g_source
	 */
	private void setLogicalFunction(GsRegulatoryVertex v, GsRegulatoryVertex v_source, GsGraph g_source) { //TODO : do we really want to do that ?
		GsRegulatoryVertex[] t = new GsRegulatoryVertex[2];
		t[0] = v;
		t[1] = v_source;
		logicalFunctionPending.add(t);
	}
	
	private void setAllLogicalFunctions() {
		for (Iterator it = logicalFunctionPending.iterator(); it.hasNext();) {
			GsRegulatoryVertex[] t = (GsRegulatoryVertex[]) it.next();
			LogicalParameterList lpl = t[1].getV_logicalParameters();
			lpl.applyNewGraph(t[0], meMap);			
		}
	}
	

	
	/**
	 * Compare the node order from both g1 and g2 
	 */
	private boolean compareNodeOrder() {
		String[] no1 = nodeOrderListToStringArray(g1.getNodeOrder());
		String[] no2 = nodeOrderListToStringArray(g2.getNodeOrder());

		int i1 = 0;//index for the current item in the list
		int i2 = 0;
		boolean shouldReturnFalseIfFailAgain = false;
		while (i1 < no1.length && i2 < no2.length) {
			
			if (no1[i1].equals(no2[i2])) { //The node are the same, go to next node.
				i1++;i2++;
			} else {
				int next = nodeOrderContainsIdAfter(no2, i2, no1[i1]);
				if (next != -1) { 			//if 1 is in 2, its bad if both in the other
					shouldReturnFalseIfFailAgain = true;
				} else { 									//if 1 is not in 2, its OK
					i1++;
				}
				next = nodeOrderContainsIdAfter(no1, i1, no2[i2]);
				if (next != -1) { 			//if 2 is in 1, its bad if both in the other
					if (shouldReturnFalseIfFailAgain == true) return false;
				} else { 									//if 2 is not in 1, its OK
					i2++;
				}
			}
		}
		return true;
	}
	
	/**
	 * does nodeOrder "no" contains the id "id" after the index "depart"
	 * @param no node order
	 * @param depart index to start search
	 * @param id search id
	 * @return the position of the id if found else -1
	 */
	private int nodeOrderContainsIdAfter(String[] no, int depart, String id) {
		for (int i = depart + 1; i < no.length; i++) {
			if (id.equals(no[i])) return i;
		}
		return -1; //Not found
	}
	
	private String[] nodeOrderListToStringArray(List nodeOrder) {
		String[] s = new String[nodeOrder.size()];
		int i = 0;
		Iterator it = nodeOrder.iterator();
		while (it.hasNext()) {
			GsRegulatoryVertex v = (GsRegulatoryVertex) it.next();
			s[i++] = v.getId();
		}
		return s;

	}

	public String compareEdges(GsRegulatoryMultiEdge e1, GsRegulatoryMultiEdge e2) {
		String comment = "";
		if (e1.getEdgeCount() != e2.getEdgeCount()) comment += "   multiarcs have different number of edges: "+e1.getEdgeCount()+" and "+e2.getEdgeCount()+"\n";
		return comment;
	}
	
	public GsGraph getDiffGraph() {
		return g;
	}
	public GsGraph getG1() {
		return g1;
	}
	public GsGraph getG2() {
		return g2;
	}
}