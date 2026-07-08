package org.ginsim.servicegui.export.cadp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.gui.utils.widgets.EnhancedJTable;
import org.ginsim.service.tool.composition.IntegrationFunction;
import org.ginsim.service.tool.composition.IntegrationFunction.IntegrationFunctionReification;
import org.ginsim.servicegui.tool.composition.CompositionSpecificationDialog;

/**
 * 
 * Model for the specification of the initial states of the LRMs being composed
 * 
 * @author Nuno D. Mendes
 * 
 */
public class LRMInitialStateModel extends AbstractTableModel {

	private static final long serialVersionUID = -1133482826268556900L;
	private List<RegulatoryNode> nodeOrder;
	private List<byte[]> initialStates = null;
	private CompositionSpecificationDialog dialog;
	private int nbCol;
	private int nbRow;
	private InitialStatesWidget panel;
	private JTable theTable;

	/**
	 * Constructor
	 * 
	 * @param panel
	 * @param dialog
	 */
	public LRMInitialStateModel(InitialStatesWidget panel,
			CompositionSpecificationDialog dialog) {
		super();
		this.panel = panel;
		this.dialog = dialog;
		this.nodeOrder = this.dialog.getGraph().getNodeOrder();
		this.nbCol = this.nodeOrder.size();
		this.nbRow = this.dialog.getNumberInstances();
		this.initialStates = new ArrayList<byte[]>();
		for (int i = 0; i < this.nbRow; i++) {
			byte state[] = new byte[this.nbCol];
			this.initialStates.add(state);
		}

	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {

		if (rowIndex < 0 || columnIndex < 0)
			return false;

		if (this.dialog
				.isTrulyMapped(this.nodeOrder.get(columnIndex), rowIndex))
			return false;
		else
			return true;
	}

	/**
	 * show string value
	 * @param element the integer number element
	 * @param maxValue the max value
	 * @return a formated String showing values for this component
	 */
	public static String showValue(Integer element, int maxValue) {
		String out = "";

		if (element == null)
			return out;

		if (element.intValue() == maxValue)
			out += "M" + maxValue;
		else
			out += element.intValue();

		return out;
	}

	/**
	 * 

	 */
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		panel.setMessage("");

		if (rowIndex > this.nbRow || columnIndex > this.nbCol) {
			return;
		}

		if (rowIndex < 0 || columnIndex < 0)
			return;

		int[] r_sel = theTable.getSelectedRows();
		int[] c_sel = theTable.getSelectedColumns();
		for (int i = 0; i < r_sel.length; i++) {
			for (int j = 0; j < c_sel.length; j++) {
				doSetValueAt(value, r_sel[i], c_sel[j]);
			}
		}
		fireTableDataChanged();
	}

	public void doSetValueAt(Object value, int rowIndex, int columnIndex) {
		if (columnIndex < 0 || !isCellEditable(rowIndex, columnIndex))
			return;

		int maxvalue = this.nodeOrder.get(columnIndex).getMaxValue();

		String stringValue = (String) value;
		if (stringValue.startsWith("M"))
			stringValue = stringValue.substring(1);
		else {
			try {
				stringValue = "" + Integer.parseInt(stringValue);
			} catch (Exception e) {
				stringValue = "0";
			}
		}

		Integer intValue = Integer.parseInt((String) stringValue);
		if (intValue > maxvalue) {
			this.panel.setMessage("Indicated value (" + intValue
					+ ") exceeds maximum value allowed for the component");
		} else {
			this.initialStates.get(rowIndex)[columnIndex] = (byte) intValue
					.intValue();

			fireTableCellUpdated(rowIndex, columnIndex);

			updateInitialStates();

		}

	}

	public void setTable(EnhancedJTable tableInitStates) {
		theTable = tableInitStates;
	}

	@Override
	public int getColumnCount() {
		return this.nbCol;
	}

	@Override
	public int getRowCount() {
		return this.nbRow;
	}

	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	public String getColumnName(int columnIndex) {
		if (columnIndex < 0 || columnIndex >= getColumnCount())
			return "";
		return this.nodeOrder.get(columnIndex).getNodeInfo().getNodeID();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= getRowCount() || columnIndex >= getColumnCount())
			return "";

		updateInitialStates();

		Integer value = new Integer(
				(int) this.initialStates.get(rowIndex)[columnIndex]);
		int maxvalue = this.nodeOrder.get(columnIndex).getMaxValue();
		return showValue(value, maxvalue);
	}

	private void updateInitialStates() {

		for (int rowIndex = 0; rowIndex < getRowCount(); rowIndex++) {
			for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {

				RegulatoryNode node = this.nodeOrder.get(columnIndex);

				if (node.isInput() && !isCellEditable(rowIndex, columnIndex)) {

					IntegrationFunction integrationFunction = dialog
							.getMapping().getIntegrationFunctionForInput(node);
					Collection<Map.Entry<RegulatoryNode, Integer>> arguments = dialog
							.getMappedToModuleArguments(node, rowIndex);

					IntegrationFunctionReification computer = IntegrationFunction
							.getIntegrationFunctionComputer(integrationFunction);

					Collection<Integer> argumentValues = new ArrayList<Integer>();

					for (Map.Entry<RegulatoryNode, Integer> argument : arguments)
						argumentValues.add(new Integer(this.initialStates
								.get(argument.getValue())[this.nodeOrder
								.indexOf(argument.getKey())]));

					if (!argumentValues.isEmpty()) {

						Integer result = computer.compute(argumentValues);

						this.initialStates.get(rowIndex)[this.nodeOrder
								.indexOf(node)] = (byte) result.intValue();
						fireTableCellUpdated(rowIndex,
								this.nodeOrder.indexOf(node));
					}

				}

			}
		}

	}

	public List<byte[]> getInitialStates() {
		return this.initialStates;
	}
}
