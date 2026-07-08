package org.ginsim.servicegui.tool.trapspace;

import javax.swing.table.AbstractTableModel;

import org.colomoto.biolqm.tool.trapspaces.TrapSpace;
import org.colomoto.biolqm.tool.trapspaces.TrapSpaceList;

public class TrapSpaceTableModel extends AbstractTableModel {

	TrapSpaceList solutions = null;

	public void setSolutions(TrapSpaceList solutions) {
		this.solutions = solutions;
		fireTableStructureChanged();
	}

	public byte[] getState(int sel) {
		if (sel < 0 || solutions == null | sel > solutions.size()) {
			return null;
		}
		return solutions.get(sel).pattern;
	}

	@Override
	public int getRowCount() {
		if (solutions == null) {
			return 0;
		}

		return solutions.size();
	}

	@Override
	public int getColumnCount() {
		if (solutions == null) {
			return 0;
		}

		return solutions.getNVars() + 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		TrapSpace t = solutions.get(rowIndex);
		if (columnIndex == 0) {
			return "";
		}

		byte v = t.pattern[columnIndex - 1];

		if (v < 0) {
			return "*";
		}

		return "" + v;
	}

	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "Name";
		}

		if (solutions == null) {
			return null;
		}

		return solutions.nodes[column - 1].toString();
	}

}
