package org.ginsim.gui.utils.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;

/**
 * A simple Table model to display a list of state.
 * 
 */
public class SimpleStateListTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 7616168924487846012L;
	private static final int STAR = -1;

	private RegulatoryGraph g;

	/**
	 * A list of state : byte[]
	 */
	public List data;
	private boolean isEditable;
	private byte[] statemax;

	public SimpleStateListTableModel(RegulatoryGraph g) {
		this(g, false, new ArrayList());
	}

	public SimpleStateListTableModel(RegulatoryGraph g, List data) {
		this(g, false, data);
	}

	public SimpleStateListTableModel(RegulatoryGraph g, boolean isEditable) {
		this(g, isEditable, new ArrayList());
	}

	public SimpleStateListTableModel(RegulatoryGraph g, boolean isEditable, List data) {
		this.g = g;
		this.isEditable = isEditable;
		this.data = data;

		this.statemax = new byte[g.getNodeOrderSize()];
		int i = 0;
		for (Iterator it = g.getNodeOrder().iterator(); it.hasNext();) {
			RegulatoryNode v = (RegulatoryNode) it.next();
			this.statemax[i++] = v.getMaxValue();
		}
	}

	public String getColumnName(int col) {
		return g.getNodeOrder().get(col).toString();
	}

	public int getColumnCount() {
		return g.getNodeOrderSize();
	}

	public int getRowCount() {
		if (data == null) {
			return 0;
		}
		return data.size() + (isEditable ? 1 : 0);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= data.size())
			return "";

		byte[] state = (byte[]) data.get(rowIndex);
		int val = state[columnIndex];
		if (val == DynamicGraph.STARLEVEL) {
			return "*";
		}
		if (val == statemax[columnIndex]) {
			return "M" + String.valueOf(val);
		}
		return String.valueOf(val);
	}

	public boolean isCellEditable(int row, int col) {
		return isEditable;
	}

	/**
	 * @param value must be a String
	 */
	public void setValueAt(Object value, int row, int col) {
		byte[] state;
		if (row == data.size()) {
			state = new byte[g.getNodeOrderSize()];
			for (int i = 0; i < state.length; i++) {
				state[i] = 0;
			}
			data.add(state);
			fireTableRowsInserted(row - 1, row);
		}
		String v = (String) value;
		state = (byte[]) data.get(row);
		if (state != null) {
			if (v.equals("*")) {
				state[col] = STAR;
			} else {
				try {
					byte val = Integer.valueOf(v).byteValue();
					if (val > statemax[col]) {
						state[col] = statemax[col];
					} else {
						state[col] = val;
					}
				} catch (Exception e) {
					// Does nothing
				}
			}
			fireTableCellUpdated(row, col);
		}
	}

	public void addState(byte[] value) {
		if (value.length == g.getNodeOrderSize()) {
			data.add(value);
		}
		fireTableRowsInserted(data.size() - 1, data.size());
	}

	public byte[] getState(int row) {
		if (data.size() == 0)
			return new byte[statemax.length];
		if (row >= data.size() || row < 0)
			return null;
		return (byte[]) data.get(row);
	}

	/**
	 * 
	 * @return an array of the maxvalues
	 */
	public byte[] getMaxValues() {
		return statemax;
	}
}
