package org.ginsim.servicegui.tool.pathfinding;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.dynamicgraph.DynamicNode;

/**
 * table model for the path constraint table.
 */
public class DynamicAnalyserPathModel extends DefaultTableModel implements PathFindingTableModel<DynamicNode> {

    private static final long serialVersionUID = 1737959610482682294L;
    
    private int len;
    private int nbBad;
    private List nodeOrder;
    private List<byte[]> v;
    private List v_in;
    private DynamicGraph graph;
    
    /**
     * @param graph  the dynamical graph
     */
    public DynamicAnalyserPathModel(DynamicGraph graph) {
        nodeOrder = graph.getNodeOrder();
        len = nodeOrder.size();
        this.graph = graph;
        this.v = new ArrayList();
        v_in = new ArrayList();
        if (v == null) {
            v = new ArrayList(2);
        }
        if (v.size() == 0 ) {
            v.add(new byte[len]);
            v.add(new byte[len]);
            if (graph.containsNode(new DynamicNode((byte[])v.get(0)))) {
                v_in.add(Boolean.TRUE);
                v_in.add(Boolean.TRUE);
                nbBad = 0;
            } else {
                v_in.add(Boolean.FALSE);
                v_in.add(Boolean.FALSE);
                nbBad = 2;
            }
        }
    }

    public int getColumnCount() {
        return len;
    }
    public int getRowCount() {
        if (v == null) {
            return 0;
        }
        return v.size();
    }
    public Object getValueAt(int row, int column) {
        if (row < v.size() && column < len) {
            return ""+((byte[])v.get(row))[column];
        }
        return null;
    }
    public void setValueAt(Object aValue, int row, int column) {
        if (aValue != null && aValue instanceof String && row < v.size() && column < len) {
        	byte[] t = (byte[])v.get(row);
            try {
                t[column] = Byte.parseByte((String)aValue);
                if (graph.containsNode(new DynamicNode(t))) {
                    if (v_in.get(row).equals(Boolean.FALSE)) {
                        nbBad--;
                    }
                    v_in.set(row, Boolean.TRUE);
                } else {
                    if (v_in.get(row).equals(Boolean.TRUE)) {
                        nbBad++;
                    }
                    v_in.set(row, Boolean.FALSE);
                }
                fireTableRowsUpdated(row,row);
            } catch (Exception e) {}
        }
    }
    
    public String getColumnName(int column) {
        if (column < len) {
            return nodeOrder.get(column).toString();
        }
        return super.getColumnName(column);
    }

    /**
     * @param index
     */
    public void add(int index) {
        if (index < 0 || index >= v.size()) {
            add(0);
            return;
        }

        v.add(index+1, ((byte[])v.get(index)).clone());
        v_in.add(index+1, v_in.get(index));
        if (v_in.get(index).equals(Boolean.FALSE)) {
            nbBad++;
        }
        fireTableRowsInserted(index, index);
    }
    
    /**
     * @param index
     */
    public boolean del(int index) {
        if (index < 0 || index >= v.size() || v.size() <= 2) {
            return false;
        }
        v.remove(index);
        if (v_in.get(index).equals(Boolean.FALSE)) {
            nbBad--;
        }
        v_in.remove(index);
        fireTableRowsDeleted(index, index);
        return true;
    }

    /**
     * test if the entered states are valid (ie are in the graph)
     * @return true if all states are in the graph
     */
    public boolean isOk() {
        return nbBad == 0;
    }

	@Override
	public List<DynamicNode> getNodes() {
		List<DynamicNode> nodes = new ArrayList<DynamicNode>(v.size());
		for (byte[] state: v) {
			DynamicNode node = new DynamicNode(state);
			if (graph.containsNode(node)) {
				nodes.add(node);
			} else {
				return null;
			}
		}
		return nodes;
	}

	@Override
	public void setNode(DynamicNode node, int[] selectedRows) {
		if (selectedRows == null || selectedRows.length < 1) {
			return;
		}
		int index = selectedRows[0];
		v.set(index, node.state);
		fireTableRowsUpdated(index, index);
	}
}
