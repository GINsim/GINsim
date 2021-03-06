package org.ginsim.core.graph.regulatorygraph.logicalfunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.operators.MDDBaseOperators;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.common.xml.XMLize;
import org.ginsim.core.graph.AbstractGraph;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;


/**
 * the Class in which we store biological data for logical parameters
 * (in the vertex)
 */
public class LogicalParameter implements XMLize {

	//value of the parameter
	private int value;
	//vector of incoming active interaction
	private List<RegulatoryEdge> edge_index;

	protected boolean isDup = false;
	protected boolean hasConflict = false;

	/**
	 * Constructs an empty vector and set the value
	 *
	 * @param v of the interaction
	 */
	public LogicalParameter(int v) {
		value = v;
		edge_index = new ArrayList<RegulatoryEdge>();
	}

	public boolean isDup() {
		return isDup;
	}
	
	public boolean hasConflict() {
		return hasConflict;
	}

	/**
     * @param newEI
     * @param v
     */
    public LogicalParameter(List newEI, int v) {
        value = v;
        edge_index = newEI;
    }

    /**
	 * get value of the interaction
	 * @return int
	 */
	public int getValue() {
		return value;
	}

	/**
	 * set value of the interaction
	 * @param i
	 */
	public void setValue(int i, Graph graph) {
		if (i != value) {
			value = i;
			((AbstractGraph) graph).fireMetaChange();
		}
	}

	/**
	 * Adds the GsEdgeIndex to the interaction
	 * @param edge
	 */
	public void addEdge(RegulatoryEdge edge) {
		edge_index.add(edge);
	}

	public boolean isDirty() {
		for (int i=0 ; i<edge_index.size() ; i++) {
			if (((RegulatoryEdge)edge_index.get(i)).index == -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param index
	 * @return the GsEdgeIndex at the specified position index in the parameter.
	 */
	public RegulatoryEdge getEdge(int index) {
		try{
			return (RegulatoryEdge)edge_index.get(index);
		}
		catch (java.lang.ArrayIndexOutOfBoundsException e) {return null;}
	}

	/**
	 * @return the number of edges in this parameter.
	 */
	public int EdgeCount() {
		return edge_index.size();
	}

	/**
	 * @return all the GsEdgeIndex
	 */
	public List getEdges() {
		return edge_index;
	}

	/**
	 * Set the vector of GsEdgeIndex
	 * @param list
	 */
	public void setEdges(List list) {
		edge_index = list;
	}

    /**
     * build a structure reflecting expression constraints of the parameter
     * that should be faster to test.
     * call it before beginning the simulation!
     *
     * details: the t_ac is a simple int[][].
     * each line represent allowed values for a gene:
     * <ul>
     *  <li> the first int is the index of this gene in the nodeOrder</li>
     *  <li> all other are either 0 or -1, -1 meaning that the value is forbidden</li>
     * </ul>
     *
     *  the first line is special and only contains the value for this interaction
     *
     *  <p>example: [ 3, 0, -1, -1, 0, 0, -1 ]
     *  the third gene can take the values 0, 3 or 4 ; values 1, 2 and 5 are forbidden
     *
     * @param regGraph
     * @param node
     * @return the t_ac
     */
	private int[][] buildTac(RegulatoryGraph regGraph, RegulatoryNode node, List<RegulatoryNode> nodeOrder) {
		
	    Collection<RegulatoryMultiEdge> incEdges = regGraph.getIncomingEdges(node);
        int[][] t_ac = new int[incEdges.size()+1][];
        t_ac[0] = new int[1];
        t_ac[0][0] = (byte)value;
        if (incEdges.size() == 0 && edge_index.size() == 0) {
        	// special case for the old "basal value"
        	return t_ac;
        }
        boolean ok = false;
        int i = 0;
        for (RegulatoryMultiEdge me: incEdges) {
        	i++;
            RegulatoryNode vertex = me.getSource();
            int max = vertex.getMaxValue();
            int[] t_val = new int[max+2];
            t_val[0] = nodeOrder.indexOf(vertex);
            t_ac[i] = t_val;
            int nbedges = me.getEdgeCount();
            for (int j=0 ; j<nbedges ; j++) {
                int im = me.getMax(j);
                if (im == -1) {
                    im = max;
                }
                if (!edge_index.contains(me.getEdge(j))) {
                    // must be inactive
                    for (int l=me.getMin(j) ; l<=im ; l++) {
                        t_val[l+1] = -1;
                    }
                } else {
                    // must be active
                    for (int l=0 ; l<me.getMin(j) ; l++) {
                        t_val[l+1] = -1;
                    }
                    for (int l=im+1 ; l<=max ; l++) {
                        t_val[l+1] = -1;
                    }
                }
            }
            // once all constraints are in, check that it is activable..
            ok = false;
            for (int k=1 ; k<t_val.length ; k++) {
                if (t_val[k] != -1) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                break;
            }
        }
        if (!ok) {
            t_ac = null;
        }
        return t_ac;
    }

    /**
     *
     * @param regGraph
     * @param node
     * @return true if this logical parameter is activable
     */
    public boolean activable(RegulatoryGraph regGraph, RegulatoryNode node) {
        return buildTac(regGraph, node, regGraph.getNodeOrder()) != null;
    }

	/**
	 * Build a MDD corresponding to this parameter (using the new MDD toolkit)
	 * 
	 * @param factory
	 * @return
	 */
	public int getMDD(RegulatoryGraph graph, RegulatoryNode node, MDDManager factory) {

		if (value == 0) {
			return 0;
		}
		
		List<RegulatoryNode> nodeOrder = graph.getNodeOrder();
        int[][] t_ac = buildTac(graph, node, nodeOrder);
		int[] constraints = new int[t_ac.length-1];
		for ( int i=1 ; i< t_ac.length ; i++) {
			int[] curCst = t_ac[i];
			RegulatoryNode src = nodeOrder.get(curCst[0]);
			int[] allowed = new int[curCst.length-1];
			for (int j=0 ; j < allowed.length ; j++) {
				if (curCst[j+1] != -1) {
					allowed[j] = value;
				}
			}
			constraints[i-1] = factory.getVariableForKey(src).getNode(allowed);
		}
		
		int result;
		switch (constraints.length) {
		case 0:
			result = value;
			break;
		case 1:
			result = constraints[0];
			break;
		case 2:
			result = MDDBaseOperators.AND.combine(factory, constraints[0], constraints[1]);
			factory.free(constraints[0]);
			factory.free(constraints[1]);
			break;
		default:
			result = MDDBaseOperators.AND.combine(factory, constraints);
			// free intermediate results
			for (int n: constraints) {
				factory.free(n);
			}
			break;
		}
		return result;
	}

	@Override
    public void toXML(XMLWriter out) throws IOException {
    	out.openTag("parameter");
		int len = edge_index.size();
		if (len != 0) {
			String sEdges = "";
			for (int i=0 ; i<len ; i++) {
				RegulatoryEdge e = edge_index.get(i);
				sEdges = sEdges + " " + e.getLongInfo(":");
			}
    		out.addAttr("idActiveInteractions", sEdges);
		}
    	out.addAttr("val", ""+value);
    	out.closeTag();
	}

	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof LogicalParameter)) {
			return false;
		}
		List o_edge = ((LogicalParameter)obj).edge_index;
		if (o_edge.size() != edge_index.size() || !o_edge.containsAll(edge_index)) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		Iterator it = edge_index.iterator();
		RegulatoryEdge ed;
		StringBuffer sb = new StringBuffer();
		TreeSet ts = new TreeSet();
		while (it.hasNext()) {
			ed = (RegulatoryEdge)it.next();
			ts.add(ed.getShortInfo());
		}
		it = ts.iterator();
		while (it.hasNext()) sb.append(it.next().toString());
		return sb.toString().hashCode();
	}

    /**
     * @param clone
     * @param copyMap
     */
    public void applyNewGraph(RegulatoryNode clone, Map copyMap) {
    	List newEI = new ArrayList();
        for (int i=0 ; i<edge_index.size() ; i++) {
        	RegulatoryEdge ei = (RegulatoryEdge)edge_index.get(i);
            RegulatoryMultiEdge me = (RegulatoryMultiEdge)copyMap.get(ei.me);
            if (me != null) {
                newEI.add(me.getEdge(ei.index));
            } else {
                // if any of the nodes involved in the interaction hasn't been copied: don't restore the interaction!
                return;
            }
        }
        clone.addLogicalParameter(new LogicalParameter(newEI, value), true);
    }

    public String toString() {
        if (edge_index.size() == 0) {
            return "(basal value)";
        }
        String str = "";
        for (int i = 0; i < edge_index.size(); i++) {
            str += getEdge(i).getShortInfo(":") + " ";
        }
        return str;
    }
}
