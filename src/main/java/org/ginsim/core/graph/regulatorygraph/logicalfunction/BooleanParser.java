package org.ginsim.core.graph.regulatorygraph.logicalfunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryEdgeSign;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.parser.TBooleanParser;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.parser.TBooleanTreeNode;


public class BooleanParser extends TBooleanParser {
	private Hashtable operandList;
	private static String returnClassName = LogicalFunctionList.class.getName();
	private static String operandClassName = BooleanGene.class.getName();
	private Object[] allParams;
	private RegulatoryGraph graph;
	private RegulatoryNode vertex;
	private boolean shouldAutoAddNewElements;

	public BooleanParser( Collection<RegulatoryMultiEdge> edgesList) throws ClassNotFoundException {
		this(edgesList, false);
	}
	public BooleanParser( Collection<RegulatoryMultiEdge> edgesList, boolean shouldAutoAddNewElements) throws ClassNotFoundException {
		super(returnClassName, operandClassName);
		nodeFactory = new BooleanTreeNodeFactory(returnClassName, operandClassName, this);
		if (edgesList != null && edgesList.size() > 0) {
			makeOperandList(edgesList);
			setAllData(edgesList);			
		}
		this.shouldAutoAddNewElements = shouldAutoAddNewElements;
	}
	public boolean verifOperandList(List list) {
		// FIXME: revert or delete dead code
		return true;
//		List v = new ArrayList();
//		Object o;
//		Iterator it = operandList.keySet().iterator();
//		RegulatoryEdge re;
//		GsDirectedEdge e;
//		RegulatoryNode source;
//
//		while (it.hasNext()) {
//			o = it.next();
//			if (o instanceof RegulatoryEdge) {
//				re = (RegulatoryEdge) o;
//				source = re.me.getSource();
//				v.add(source.getId());
//				for (int i = 0; i < re.me.getEdgeCount(); i++) {
//			v.add(re.me.getEdge(i).getShortInfo("#"));
//		}
//			}
//			else if (o instanceof GsDirectedEdge) {
//				e = (GsDirectedEdge) o;
//				source = (RegulatoryNode)e.getSourceNode();
//				v.add(source.getId() + "#1");
//			}
//			else if (o instanceof RegulatoryNode) {
//				source = (RegulatoryNode)o;
//				v.add(source.getId());
//			}
//		}
//		return v.containsAll(list);
	}
	protected void setAllData(Collection<RegulatoryMultiEdge> edgesList) {
		List[] F = new List[operandList.size()];
		int[] N = new int[operandList.size()];
		int[] K = new int[operandList.size()];
		int n, i, p, j;

		List L, v;
		v = new ArrayList();

		i = 0;
		p = 1;
		for (RegulatoryMultiEdge me: edgesList) {
			n = me.getEdgeCount();
			F[i] = new ArrayList(n + 1);
			F[i].add(new LogicalFunctionListElement(null, -1));
			for (int k = 0; k < n; k++) {
				F[i].add(new LogicalFunctionListElement(me, k));
			}
			N[i] = n;
			K[i] = 0;
			p *= n + 1;
			i++;
		}
		K[edgesList.size() - 1] = -1;
		for (i = 1; i <= p; i++) {
			for (j = edgesList.size() - 1; j >= 0; j--) {
				K[j]++;
				if (K[j] > N[j]) {
					K[j] = 0;
				} else {
					break;
				}
			}
			if (j >= 0) {
				L = new ArrayList();
				for (j = 0; j < edgesList.size(); j++) {
					if (!((LogicalFunctionListElement) F[j].get(K[j])).toString().equals("")) {
						L.add(F[j].get(K[j]));
					}
				}
				v.add(L);
			} else {
				break;
			}
		}
		allParams = v.toArray();
		allData = new ArrayList(allParams.length);
		for (i = 0; i < allParams.length; i++) {
			allData.add(new Integer(i));
		}
	}

	public List getParams(List<Integer> indexes) {
		List v = new ArrayList();
		for (int i: indexes) {
			v.add(allParams[i]);
		}
		return v;
	}
	public Object[] getAllParams() {
		return allParams;
	}
	private void makeOperandList(Collection<RegulatoryMultiEdge> edgesList) {
		RegulatoryNode source;
		RegulatoryEdge re;

		operandList = new Hashtable();
		for (RegulatoryMultiEdge me: edgesList) {
			source = me.getSource();
			operandList.put(source, source.getId());
			for (int i = 0; i < me.getEdgeCount(); i++) {
				re = me.getEdge(i);
				if (me.getEdgeCount() > 1) {
					operandList.put(re/*.getShortInfo("#")*/, re.getShortDetail("#"));
				} else {
					operandList.put(me/*.getId() + "#" + (i + 1)*/, re.getShortDetail("#"));
				}
			}
		}
	}
	public String getSaveString(String s) {
		return (String)operandList.get(s);
	}
	public TBooleanTreeNode getRoot() {
		return root;
	}
	public static String getReturnClassName() {
		return returnClassName;
	}
	public static String getOperandClassName() {
		return operandClassName;
	}
	public void setRoot(TBooleanTreeNode root) {
		this.root = root;
	}
	
	public boolean compile(String v, RegulatoryGraph graph, RegulatoryNode vertex) {
		this.graph = graph;
		this.vertex = vertex;
		boolean shouldReInit = false;
		if (shouldAutoAddNewElements) {
			List sourceNodes = getSourceNodes(v);
			for (Iterator it = sourceNodes.iterator(); it.hasNext();) {
				String nodeID = (String) it.next();
				RegulatoryNode source = null;
				for (Iterator itno = graph.getNodeOrder().iterator(); itno.hasNext();) {
					RegulatoryNode node = (RegulatoryNode) itno.next();
					if (node.getId().equals(nodeID)) {
						source = node;
						shouldReInit = true;
						break;
					}
				}
				if (source == null)	source = graph.addNewNode(nodeID, null, (byte) 1);

				Edge edge = graph.getEdge(source, this.vertex);
				if (edge == null) {
					try{
						edge = graph.addNewEdge(nodeID, this.vertex.getId(), (byte)1, RegulatoryEdgeSign.POSITIVE.getLongDesc()).me;
						shouldReInit = true;
					}
					catch( GsException gs_exception){
						LogManager.error( "Unable to create new edge between vertices '" + nodeID + "' and '" + this.vertex.getId() + "' : one of the vertex was not found in the graph");
						LogManager.error( gs_exception);
					}
				}
			}
		}
		if (shouldReInit) {
			Collection edgesList = graph.getIncomingEdges(vertex);
			makeOperandList(edgesList);
			setAllData(edgesList);		
		}
		
		
		try {
			return super.compile(v);
		} 
		catch (Exception e) {
			if (e instanceof GsException) {
				LogManager.trace( "Working");
			}
			e.printStackTrace();
			return false;
		}
	}
	private List getSourceNodes(String v) {
		String[] split = v.split("(!|&|\\||\\(|\\)|\\s|:\\d+)");
		List sourceNodes = new ArrayList(split.length);
		for (int i = 0; i < split.length; i++) {
			if (split[i].length() > 0) {
				sourceNodes.add(split[i]);
			}
		}
		return sourceNodes;
	}
	public Object getEdge(String value) throws GsException {
		String nodeID;
		int edgeTh = value.indexOf(":");
		int edgeNumber = value.indexOf("#");
		if (edgeTh != -1) {
			int i = value.lastIndexOf(":");
			if (edgeTh != i) {
				throw new GsException(GsException.GRAVITY_ERROR, "invalid identifier: "+value);
			}
			nodeID = value.substring(0, i);
			try {
				edgeTh = Integer.parseInt(value.substring(i+1));
			} 
			catch (Exception e) {
				throw new GsException(GsException.GRAVITY_ERROR, "invalid edge threshold in "+value);
			}
		} else if (edgeNumber != -1) {
			int i = value.lastIndexOf("#");
			if (edgeNumber != i) {
				throw new GsException(GsException.GRAVITY_ERROR, "invalid identifier: "+value);
			}
			nodeID = value.substring(0, i);
			try {
				edgeNumber = Integer.parseInt(value.substring(i+1));
			} 
			catch (Exception e) {
				throw new GsException(GsException.GRAVITY_ERROR, "invalid edge number in "+value);
			}
		} else {
			nodeID = value;
		}
		Iterator it = graph.getNodeOrder().iterator();
		RegulatoryNode vertex = null;
		boolean found = false;
		while (it.hasNext()) {
			vertex = (RegulatoryNode)it.next();
			if (vertex.getId().equals(nodeID)) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new GsException(GsException.GRAVITY_NORMAL, "The node is not defined in the graph");
		}
		RegulatoryMultiEdge me = graph.getEdge(vertex, this.vertex);
		if (me == null) {
			throw new GsException(GsException.GRAVITY_NORMAL, "The node is not linked by any edge in the graph");
		}
		if (edgeTh != -1) {
			RegulatoryEdge edge = me.getEdgeForThreshold(edgeTh);
			if (edge == null) {
				throw new GsException(GsException.GRAVITY_NORMAL, "no such edge threshold");
			}
			return edge;
		}
		if (edgeNumber != -1) {
			return me.getEdge(edgeNumber-1);
		}	
		return me;
	}
}
