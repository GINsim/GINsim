package org.ginsim.servicegui.tool.composition;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * Widget to specify module topology
 * 
 * @author Nuno D. Mendes
 */
public class AdjacencyMatrixWidget extends JPanel {

	private static final long serialVersionUID = -7735335091138597285L;
	private boolean symmetricTopology = false; // default is asymmetric
	private JCheckBox[][] matrix = null;
	private Map<JCheckBox, AbstractMap.Entry<Integer, Integer>> reverseMatrix = null;
	ButtonGroup symmetryGroup = new ButtonGroup();
	JRadioButton buttonSym = new JRadioButton("Symmetric topology");
	JRadioButton buttonAsy = new JRadioButton("Non-symmetric topology");

	private CompositionSpecificationDialog dialog = null;

	/**
	 * Constructor
	 * @param dialog the CompositionSpecificationDialog
	 */
	public AdjacencyMatrixWidget(final CompositionSpecificationDialog dialog) {
		super();
		this.dialog = dialog;
		int instances = this.dialog.getNumberInstances();
		matrix = new JCheckBox[instances][instances];
		reverseMatrix = new HashMap<JCheckBox, AbstractMap.Entry<Integer, Integer>>();

		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		GridBagConstraints topConstraints = new GridBagConstraints();
		GridBagConstraints bottomConstraints = new GridBagConstraints();

		// TODO: replace with STR_s
		setBorder(BorderFactory
				.createTitledBorder("Specify Neighbouring Modules"));

		constraints.fill = GridBagConstraints.NONE;
		topConstraints.fill = GridBagConstraints.NONE;
		bottomConstraints.fill = GridBagConstraints.NONE;

		if (instances > 1) {

			JPanel top = new JPanel();
			top.setLayout(new GridBagLayout());

			int col = 0;
			int row = 0;
			while (col <= instances && row <= instances) {
				topConstraints.gridx = col;
				topConstraints.gridy = row;
				topConstraints.weighty = 0;
				topConstraints.weightx = 0;

				if (col == 0 && row == 0)
					top.add(new JLabel(), topConstraints);

				if (row == 0 && col > 0)
					top.add(new JLabel("M" + col), topConstraints);
				if (col == 0 && row > 0) {
					top.add(new JLabel("M" + row), topConstraints);
				}
				if (col > 0 && row > 0) {
					JCheckBox checkBox = new JCheckBox();

					// Modules cannot be their own neighbours
					if (col == row)
						checkBox.setEnabled(false);
					else
						checkBox.setEnabled(true);

					checkBox.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							JCheckBox current = (JCheckBox) e.getSource();
							boolean selection = current.isSelected();

							AbstractMap.Entry<Integer, Integer> entry = reverseMatrix
									.get(current);
							int x = entry.getKey().intValue();
							int y = entry.getValue().intValue();

							if (selection) {
								dialog.addNeighbour(x, y);
								if (symmetricTopology) {
									matrix[y][x].setSelected(true);
									dialog.addNeighbour(y, x);
								}
							} else {
								dialog.removeNeighbour(x, y);
								if (symmetricTopology) {
									matrix[y][x].setSelected(false);
									dialog.removeNeighbour(y, x);
								}
							}
							
							dialog.fireIntegrationMappingChange();

						}

					});
					matrix[row - 1][col - 1] = checkBox;
					reverseMatrix.put(checkBox,
							new AbstractMap.SimpleEntry<Integer, Integer>(
									row - 1, col - 1));
					top.add(checkBox, topConstraints);

				}

				if (col == instances) {
					topConstraints.gridx = col + 1;
					topConstraints.gridy = row;
					topConstraints.weightx = 1;
					top.add(new JLabel(), topConstraints);
				}

				col++;
				if (col > instances && row <= instances) {
					row++;
					col = 0;
				}
			}

			buttonSym.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					symmetricTopology = true;
					forceSymmetry(true);
				}

			});

			buttonAsy.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					symmetricTopology = false;

				}

			});

			symmetryGroup.add(buttonSym);
			symmetryGroup.add(buttonAsy);

			updateSymmetryGroupSelection();

			constraints.gridx = 0;
			constraints.gridy = GridBagConstraints.RELATIVE;
			constraints.weighty = 1;
			constraints.weightx = 1;
			constraints.gridwidth = GridBagConstraints.REMAINDER;
			add(new JLabel(), constraints);

			top.setSize(top.getPreferredSize());
			constraints.gridwidth = GridBagConstraints.REMAINDER;
			add(top, constraints);

			JPanel bottom = new JPanel();
			bottom.setLayout(new GridBagLayout());

			bottomConstraints.gridx = 0;
			bottomConstraints.gridy = 0;
			bottomConstraints.weighty = 0;
			bottomConstraints.weightx = 0;
			bottomConstraints.gridwidth = 1;
			bottom.add(new JLabel(), bottomConstraints);

			bottomConstraints.gridx = 1;
			bottomConstraints.weightx = 0;
			bottom.add(buttonSym, bottomConstraints);

			bottomConstraints.gridx = GridBagConstraints.RELATIVE;
			bottomConstraints.gridwidth = GridBagConstraints.REMAINDER;
			bottomConstraints.weightx = 1;
			bottom.add(new JLabel(), bottomConstraints);

			bottomConstraints.gridx = 0;
			bottomConstraints.gridy = 1;
			bottomConstraints.weightx = 0;
			bottomConstraints.weighty = 0;
			bottomConstraints.gridwidth = 1;
			bottom.add(new JLabel(), bottomConstraints);

			bottomConstraints.gridx = 1;
			bottomConstraints.weightx = 0;
			bottom.add(buttonAsy, bottomConstraints);

			bottomConstraints.gridx = GridBagConstraints.RELATIVE;
			bottomConstraints.gridwidth = GridBagConstraints.REMAINDER;
			bottomConstraints.weightx = 1;
			bottom.add(new JLabel(), bottomConstraints);

			bottomConstraints.gridy = 2;
			bottomConstraints.weighty = 1;
			bottomConstraints.weightx = 1;
			bottomConstraints.gridheight = GridBagConstraints.REMAINDER;
			bottomConstraints.gridwidth = GridBagConstraints.REMAINDER;
			bottom.add(new JLabel(), bottomConstraints);

			bottom.setSize(bottom.getPreferredSize());
			constraints.gridx = 0;
			constraints.gridy = GridBagConstraints.RELATIVE;
			constraints.gridheight = GridBagConstraints.REMAINDER;
			constraints.gridwidth = GridBagConstraints.REMAINDER;
			add(bottom, constraints);

		}
		setSize(getPreferredSize());

	}

	private void updateSymmetryGroupSelection() {
		if (symmetricTopology) {
			symmetryGroup.clearSelection();
			buttonSym.setSelected(true);
			buttonAsy.setSelected(false);
		} else {
			symmetryGroup.clearSelection();
			buttonAsy.setSelected(true);
			buttonSym.setSelected(false);
		}

	}

	private void forceSymmetry(boolean selection) {
		for (int x = 0; x < matrix.length; x++)
			for (int y = 0; y < matrix.length; y++)
				if (selection) {
					if (matrix[x][y].isSelected()) {
						matrix[y][x].setSelected(true);
						dialog.addNeighbour(y, x);
					}
				} else {
					if (!matrix[x][y].isSelected()) {
						matrix[y][x].setSelected(false);
						dialog.removeNeighbour(y, x);
					}
				}

	}

	/**
	 * Test if selected
	 * @param x position x
	 * @param y position y
	 * @return boolean if selected
	 */
	public boolean isSelected(int x, int y) {
		if (x < matrix.length && y < matrix.length)
			return matrix[x][y].isSelected();
		return false;
	}

	/**
	 * Setter selection
	 * @param x position x
	 * @param y position y
	 */
	public void setSelected(int x, int y) {
		if (x < matrix.length && y < matrix.length)
			matrix[x][y].setSelected(true);

	}

	/**
	 * Test if symetric
	 * @return booelean if symetric
	 */
	public boolean isSymmetric() {
		return this.symmetricTopology;
	}

	/**
	 * Setter if symetric
	 * @param symmetry boolean for symetry property
	 */
	public void setSymmetry(boolean symmetry) {
		this.symmetricTopology = symmetry;
		updateSymmetryGroupSelection();
	}

	/**
	 * rebuild éfunction
	 * @return a new AdjacencyMatrixWidget
	 */
	public AdjacencyMatrixWidget reBuild() {
		AdjacencyMatrixWidget widget = new AdjacencyMatrixWidget(dialog);
		for (int x = 0; x < matrix.length; x++)
			for (int y = 0; y < matrix.length; y++)
				if (matrix[x][y].isSelected()) {
					widget.setSelected(x, y);
					if (widget.isSelected(x, y))
						dialog.addNeighbour(x, y);
				} else
					dialog.removeNeighbour(x, y);

		widget.setSymmetry(isSymmetric());
		return widget;

	}

}
