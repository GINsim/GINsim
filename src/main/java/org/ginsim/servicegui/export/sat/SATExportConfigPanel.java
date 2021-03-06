package org.ginsim.servicegui.export.sat;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.colomoto.biolqm.LogicalModel;
import org.ginsim.common.application.Txt;
import org.ginsim.gui.graph.regulatorygraph.initialstate.InitialStatePanel;
import org.ginsim.gui.utils.dialog.stackdialog.LogicalModelActionDialog;
import org.ginsim.service.export.sat.SATConfig;
import org.ginsim.service.export.sat.SATExportType;

public class SATExportConfigPanel extends LogicalModelActionDialog {
	private static final long serialVersionUID = -7398674287463858306L;

	private final SATConfig config;
	private final SATExportAction action;

	private JPanel mainPanel;
	private InitialStatePanel initPanel;
	private SATExportType type;

	public SATExportConfigPanel(SATConfig config, SATExportAction action) {
		super(config.getGraph(), null, Txt.t("STR_SAT"), 600, 400);
		this.setTitle(Txt.t("STR_SAT_descr"));
		setUserID(Txt.t("STR_SAT"));
		this.config = config;
		this.action = action;
		this.type = SATExportType.STABILITY_CONDITION;

		mainPanel = new JPanel(new BorderLayout());

		JPanel stateInputsPanel = new JPanel(new BorderLayout());
		stateInputsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("State inputs"),
				stateInputsPanel.getBorder()));

		// NORTH begin
		JPanel typePanel = new JPanel();
		typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
		typePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Txt.t("STR_SAT_Type")),
				typePanel.getBorder()));
		
		// Stability conditions
		JRadioButton jrbSCs = new JRadioButton(Txt.t("STR_SAT_Type_SCs"), true);
		jrbSCs.setToolTipText(Txt.t("STR_SAT_Type_SCs_desc"));
		jrbSCs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				type = SATExportType.STABILITY_CONDITION;
			}
		});
		jrbSCs.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Stable states
		JRadioButton jrbSSs = new JRadioButton(Txt.t("STR_SAT_Type_SSs"), true);
		jrbSSs.setToolTipText(Txt.t("STR_SAT_Type_SSs_desc"));
		jrbSSs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				type = SATExportType.STABLE_STATE;
			}
		});
		jrbSSs.setAlignmentX(Component.LEFT_ALIGNMENT);

		// JRadioButton jrbInterv = new JRadioButton(Txt.t("STR_SAT_Type_Interv"));
		// jrbInterv.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// type = SATExportType.INTERVENTION;
		// }
		// });
		ButtonGroup bg = new ButtonGroup();
		bg.add(jrbSCs);
		bg.add(jrbSSs);
		// bg.add(jrbInterv);
		typePanel.add(jrbSCs);
		typePanel.add(jrbSSs);
		// typePanel.add(jrbInterv);
		mainPanel.add(typePanel, BorderLayout.NORTH);

		// CENTER begin
		initPanel = new InitialStatePanel(config.getGraph(), false, Txt.t("STR_SAT_title_internal"), Txt.t("STR_SAT_title_input"));
		initPanel.setParam(config);
		mainPanel.add(initPanel, BorderLayout.CENTER);

		setMainPanel(mainPanel);
	}

	@Override
	public void run(LogicalModel model) {
		config.updateModel(model);
		config.setExportType(this.type);
		action.selectFile();
		cancel();
	}
}