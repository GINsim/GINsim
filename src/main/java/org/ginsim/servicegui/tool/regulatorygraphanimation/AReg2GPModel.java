package org.ginsim.servicegui.tool.regulatorygraphanimation;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.ginsim.common.application.Txt;


/**
 * tableModel to configure gene state blockers
 */
public class AReg2GPModel extends AbstractTableModel {

	private static final long serialVersionUID = 864660594916225977L;
	private List nodeOrder;
	private boolean[] t_selected;
	
	/**
	 * Constructor AReg2GPModel
	 * @param nodeOrder node order list
	 * @param t_selected boolean array
	 */
	public AReg2GPModel(List nodeOrder, boolean[] t_selected) {
		this.nodeOrder = nodeOrder;
		this.t_selected = t_selected;
	}

	public int getRowCount() {
		return t_selected.length;
	}

	public int getColumnCount() {
		return 2;
	}

	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Txt.t("STR_node");
		case 1:
			return Txt.t("STR_selected");
		}
		return null;
	}

	public Class getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return String.class;
		}
		if (columnIndex == 1) {
		    return Boolean.class;
		}
		return Object.class;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 1 && rowIndex < t_selected.length && rowIndex >=0) {
			return true;
		}
		return false;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < t_selected.length && rowIndex >=0) {
		    if (columnIndex == 0) {
		        return ""+nodeOrder.get(rowIndex);
		    } else if (columnIndex == 1) {
		        if (t_selected[rowIndex]) {
		            return Boolean.TRUE;
		        }
	            return Boolean.FALSE;
		    }
		}
		return null;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 1 && rowIndex < t_selected.length && rowIndex >=0 && aValue instanceof Boolean) {
	        t_selected[rowIndex] = ((Boolean)aValue).booleanValue();
		}
	}
}
