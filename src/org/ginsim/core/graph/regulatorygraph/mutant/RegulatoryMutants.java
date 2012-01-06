package org.ginsim.core.graph.regulatorygraph.mutant;

import java.util.Collection;
import java.util.Vector;

import org.ginsim.core.GraphEventCascade;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.core.graph.common.GraphListener;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.utils.data.SimpleGenericList;


/**
 * Associate a list of mutants to the regulatory graph, and offer the UI to edit this list.
 */
public class RegulatoryMutants extends SimpleGenericList implements GraphListener<RegulatoryNode, RegulatoryMultiEdge> {

    Vector v_listeners = new Vector();
    Graph<RegulatoryNode,RegulatoryMultiEdge> graph;
    
    /**
     * edit mutants associated with the graph
     * @param graph
     */
    public RegulatoryMutants( Graph<RegulatoryNode,RegulatoryMultiEdge> graph) {
        this.graph = graph;
        graph.addGraphListener(this);
        
        prefix = "mutant_";
        canAdd = true;
        canOrder = true;
        canRemove = true;
        canEdit = true;
    }
    
    public GraphEventCascade edgeAdded(RegulatoryMultiEdge data) {
        return null;
    }
    public GraphEventCascade edgeRemoved(RegulatoryMultiEdge data) {
        return null;
    }
    public GraphEventCascade nodeAdded(RegulatoryNode data) {
        return null;
    }
    public GraphEventCascade nodeRemoved(RegulatoryNode data) {
        Vector v = new Vector();
        for (int i=0 ; i<v_data.size() ; i++) {
            RegulatoryMutantDef m = (RegulatoryMutantDef)v_data.get(i);
            for (int j=0 ; j<m.getChanges().size() ; j++) {
                RegulatoryMutantChange change = (RegulatoryMutantChange)m.getChange( j);
                if (change.getNode() == data) {
                    m.removeChange( change);
                    v.add(m);
                }
            }
        }
        if (v.size() > 0) {
            return new MutantCascadeUpdate (v);
        }
        return null;
    }
	public GraphEventCascade graphMerged(Collection<RegulatoryNode> data) {
		return null;
	}
    public GraphEventCascade nodeUpdated(RegulatoryNode data) {
        Vector v = new Vector();
        for (int i=0 ; i<v_data.size() ; i++) {
            RegulatoryMutantDef m = (RegulatoryMutantDef)v_data.get(i);
            for (int j=0 ; j<m.getChanges().size() ; j++) {
                RegulatoryMutantChange change = (RegulatoryMutantChange)m.getChange(j);
                if (change.getNode() == data) {
                    // check that it is up to date
                    RegulatoryNode vertex = (RegulatoryNode)data;
                    if (change.getMax() > vertex.getMaxValue()) {
                        change.setMax( vertex.getMaxValue());
                        if (change.getMin() > vertex.getMaxValue()) {
                            change.setMin( vertex.getMaxValue());
                        }
                        v.add(m);
                    }
                }
            }
        }
        if (v.size() > 0) {
            return new MutantCascadeUpdate (v);
        }
        return null;
    }
    public GraphEventCascade edgeUpdated(RegulatoryMultiEdge data) {
        return null;
    }
    
    /**
     * @param o
     * @return the index of o, -1 if not found
     */
    public int indexOf(Object o) {
        return v_data.indexOf(o);
    }
    /**
     * register a new listener for this object
     * @param listener
     */
    public void addListener(RegulatoryMutantListener listener) {
        v_listeners.add(listener);
    }
    /**
     * un-register a listener
     * @param listener
     */
    public void removeListener(RegulatoryMutantListener listener) {
        v_listeners.remove(listener);
    }

    /**
     * get a mutant by its name.
     * @param value
     * @return the correct mutant, or null if none.
     */
    public RegulatoryMutantDef get(String value) {
        for (int i=0 ; i<v_data.size() ; i++) {
            RegulatoryMutantDef mdef = (RegulatoryMutantDef)v_data.get(i);
            if (mdef.getName().equals(value)) {
                return mdef;
            }
        }
        return null;
    }

	protected Object doCreate(String name, int mode) {
        RegulatoryMutantDef m = new RegulatoryMutantDef();
        m.setName( name);
		return m;
	}

	public void endParsing() {
	}
}

class MutantCascadeUpdate implements GraphEventCascade {
    protected MutantCascadeUpdate(Vector v) {
        this.v = v;
    }
    Vector v;

    public String toString() {
        StringBuffer s = new StringBuffer("updated mutants:");
        for (int i=0 ; i<v.size() ; i++) {
            s.append(" ");
            s.append(v.get(i));
        }
        return s.toString();
    }
}