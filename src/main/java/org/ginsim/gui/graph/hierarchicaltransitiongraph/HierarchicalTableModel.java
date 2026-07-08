package org.ginsim.gui.graph.hierarchicaltransitiongraph;

import java.util.List;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.LogicalModelImpl;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.modifier.reverse.ReverseModifier;
import org.colomoto.mddlib.MDDManager;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNode;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalTransitionGraph;
import org.ginsim.core.graph.hierarchicaltransitiongraph.StatesSet;


public class HierarchicalTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 2922634659695976653L;

	private List<byte[]> content = null;

	private List<byte[]> contentNode = null;


	private byte[] childsCount = null;
	private byte[][] extraContent = null;
	private String[][] extraContentString = null;
	private List<NodeInfo> extraNodes = null;
	private int colCount;
	private String[] extraNames = null;

	private int[] extraFunctions = null;
	private int len;
	private boolean showExtra = true;

	private HierarchicalTransitionGraph htg;

	public HierarchicalTableModel(HierarchicalTransitionGraph g) {

		super();
		this.htg = g;
		len = colCount = g.getNodeOrderSize();
		if (showExtra) {
			extraNames = g.getExtraNames();
			//extraNodes = g.
			if (extraNames != null && extraNames.length > 0) {
				len += extraNames.length;
			}
		}
	}

	@Override
	public int getRowCount() {
		if (content == null) {
			return 0;
		}
		return content.size();
	}

	@Override
	public int getColumnCount() {
		return len;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ArrayList<Integer> listValueUnique = new ArrayList<Integer>();
		if (rowIndex >= getRowCount() || columnIndex >= len) {
			return null;
		}

		int i = -10;
		if (columnIndex >= colCount) {
			i = extraContent[rowIndex][columnIndex - colCount];
			if (i < 0) {
				if (extraNodes.get(columnIndex - colCount).getMax() > 1) {
					// multivalued: i<0 might be a * or a multivalued interval
					return htg.getExtraValueInterval(content.get(rowIndex), columnIndex - colCount);
				} else {
					// Boolean: i<0 is a *
					return "*";
				}
			}
			return String.valueOf(i);
		}

		i = content.get(rowIndex)[columnIndex];
		if (i == -1) {
			return "*";
		}
		return String.valueOf(i);
	}

	@Override
	public String getColumnName(int column) {
		if (column >= colCount) {
			return  "[O] " + extraNames[column - colCount];
		}
		return htg.getNodeOrder().get(column).getNodeID();
	}

	public void setContent(HierarchicalNode hnode) {
		this.content = hnode.statesToList();//hnode.statesSet.getChildsCount();
		this.contentNode = hnode.statesSet.statesToFullList();
		this.childsCount = hnode.statesSet.getChildsCount();
		if (extraNames != null && extraNames.length > 0) {
			// fill the extra content
			extraContent = fillExtra(hnode);
			this.extraNodes = htg.getExtraNodes();
			this.extraFunctions = htg.getExtraFunctions();
		}

		fireTableDataChanged();
	}

	private byte[][] fillExtra(HierarchicalNode hnode) {
		// lot of stuff for extraContent but not working correctly should be remove
		if (content == null || content.size() < 1) {
			return null;
		}
		byte[][] extraStates = new byte[content.size()][extraNames.length];
		this.extraContentString = new String[content.size()][extraNames.length];

		int i = 0;
		for (byte[] state : content) {
			extraStates[i] = htg.fillExtraValues(state, extraStates[i]);
			i++;
		}
		return extraStates;
	}
}

