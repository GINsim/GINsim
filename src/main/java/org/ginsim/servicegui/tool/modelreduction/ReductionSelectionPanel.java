package org.ginsim.servicegui.tool.modelreduction;

import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.gui.graph.regulatorygraph.initialstate.PatternSelectionPanel;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.service.tool.modelreduction.ReductionHolder;

import javax.swing.*;
import java.awt.*;


public class ReductionSelectionPanel extends JPanel {
	private static final long serialVersionUID = 1213902700181873169L;

	private ReductionConfigSelectionPanel cfgPanel;
	PatternSelectionPanel patternPanel;

	public ReductionSelectionPanel(StackDialog dialog, RegulatoryGraph graph, ReductionHolder holder) {
		this.internalConstructorReductionSelectionPanel(dialog, graph, holder);
	}
	//public ReductionSelectionPanel(StackDialog dialog, RegulatoryGraph graph, ReductionHolder holder, JCheckBox  cb_simplify) {
	//	this.internalConstructorReductionSelectionPanel(dialog, graph, holder);
		//cfgPanel.setCBSimplify(cb_simplify);
	//}
	public void refresh() {
		cfgPanel.refresh();
	}

	private void internalConstructorReductionSelectionPanel(StackDialog dialog, RegulatoryGraph graph, ReductionHolder holder){
		cfgPanel = new ReductionConfigSelectionPanel(dialog, graph, holder);
		patternPanel = PatternSelectionPanel.getInputSelectorOrNull(graph, holder);
		setLayout(new GridBagLayout());
		GridBagConstraints cst = new GridBagConstraints();
		cst.gridx = cst.gridy = 1;
		cst.weightx = 1;
		cst.fill = GridBagConstraints.HORIZONTAL;
		add(cfgPanel, cst);
		if (patternPanel != null) {
			cst.gridy++;
			add(patternPanel, cst);
		}
	}
}
