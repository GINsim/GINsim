package org.ginsim.graph.regulatorygraph.initialstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ginsim.graph.regulatorygraph.RegulatoryVertex;
import org.ginsim.graph.regulatorygraph.omdd.OMDDNode;

import fr.univmrs.tagc.common.datastore.NamedObject;

public class InitialState implements NamedObject {
	String name;
	Map<RegulatoryVertex, List<Integer>> m = new HashMap<RegulatoryVertex, List<Integer>>();
	
    public void setState(int[] state, List<RegulatoryVertex> nodeOrder) {
        setState(state, nodeOrder, false);
    }
    public void setState(int[] state, List<RegulatoryVertex> nodeOrder, boolean input) {
        String[] t_s = new String[state.length];
        for (int i=0 ; i<t_s.length ; i++) {
            RegulatoryVertex vertex = (RegulatoryVertex)nodeOrder.get(i);
            if (vertex.isInput() == input) {
                t_s[i] = vertex + ";" + state[i];
            } else {
                t_s[i] = "";
            }
        }
        setData(t_s, nodeOrder);
    }
    
    public Map<RegulatoryVertex, List<Integer>> getMaxValueTable() {
    	
		return m;
	}
    
    public void setMaxValueTable( Map<RegulatoryVertex, List<Integer>> m) {
    	
		this.m = m;
	}
    
	public void setData(String[] t_s, List<RegulatoryVertex> nodeOrder) {
        for (int i=0 ; i<t_s.length ; i++) {
            RegulatoryVertex vertex = null;
            String[] t_val = t_s[i].split(";");
            if (t_val.length > 1) {
                for (int j=0 ; j<nodeOrder.size() ; j++) {
                    if (((RegulatoryVertex)nodeOrder.get(j)).getId().equals(t_val[0])) {
                        vertex = (RegulatoryVertex)nodeOrder.get(j);
                        break;
                    }
                }
                if (vertex != null) {
                	List<Integer> v_val = new ArrayList<Integer>();
                    for (int j=1 ; j<t_val.length ; j++) {
                        try {
                        	int v = Integer.parseInt(t_val[j]);
                            if (v >= 0 && v <= vertex.getMaxValue()) {
                                boolean ok = true;
                                for (int k=0 ; k<v_val.size() ; k++) {
                                    if (v_val.get(k).equals(v)) {
                                        ok = false;
                                        break;
                                    }
                                }
                                if (ok) {
                                    v_val.add(v);
                                }
                            } else {
                                // TODO: report error in file
                            }
                        } catch (NumberFormatException e) {
                            // TODO: report error in file
                        }
                    }
                    if (!v_val.isEmpty() && v_val.size() <= vertex.getMaxValue()) {
                        m.put(vertex, v_val);
                    }
                }
            }
        }
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<RegulatoryVertex,List<Integer>> getMap() {
		return m;
	}
	public OMDDNode getMDD(List<RegulatoryVertex> nodeOrder) {
		OMDDNode falseNode = OMDDNode.TERMINALS[0];
		OMDDNode ret = OMDDNode.TERMINALS[1];
		for (int i=nodeOrder.size()-1 ; i>-1 ; i--) {
			RegulatoryVertex vertex = (RegulatoryVertex)nodeOrder.get(i);
			Object o = m.get(vertex);
			if (o != null) {
				OMDDNode newNode = new OMDDNode();
				newNode.level = i;
				newNode.next = new OMDDNode[vertex.getMaxValue()+1];
				
				for (int v=0 ; v<newNode.next.length ; v++) {
					newNode.next[v] = falseNode;
				}
				List<Integer> l_val = m.get(vertex);
				for (int n: l_val) {
					newNode.next[n] = ret;
				}
				ret = newNode;
			}
		}
		return ret;
	}
}