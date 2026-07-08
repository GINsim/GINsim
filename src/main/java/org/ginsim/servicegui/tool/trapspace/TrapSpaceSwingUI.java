package org.ginsim.servicegui.tool.trapspace;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Enumeration;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.helper.clingo.ClingoLauncher;
import org.colomoto.biolqm.tool.trapspaces.TrapSpaceList;
import org.colomoto.biolqm.tool.trapspaces.TrapSpaceTask;
import org.colomoto.common.task.Task;
import org.colomoto.common.task.TaskListener;
import org.colomoto.common.task.TaskStatus;
import org.ginsim.common.application.Txt;
import org.ginsim.commongui.utils.VerticalTableHeaderCellRenderer;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.trapspacetree.TrapSpaceInclusionDiagram;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.utils.dialog.stackdialog.LogicalModelActionDialog;
import org.ginsim.gui.utils.widgets.EnhancedJTable;
import org.ginsim.service.tool.trapspace.TrapSpaceServiceWrapper;
import org.ginsim.servicegui.tool.stablestates.ColoredCellRenderer;

/**
 * A simple GUI to launch the identification of trap-spaces and view the result.
 * 
 * @author Aurelien Naldi
 */
@SuppressWarnings("serial")
public class TrapSpaceSwingUI extends LogicalModelActionDialog implements TaskListener {

	private static TrapSpaceServiceWrapper srv = GSServiceManager.getService(TrapSpaceServiceWrapper.class);

	TrapSpaceTableModel model;
	EnhancedJTable tresult;
	JCheckBox cb_diag;

	private final TrapSpaceParameters settings;
	Task<TrapSpaceList> m_identifier;

	public TrapSpaceSwingUI(JFrame f, RegulatoryGraph lrg) {
		super(lrg, f, "stableStatesGUI", 600, 400);
		setUserID("stable_search");
		this.setTitle(Txt.t("STR_trapSpaces"));

		model = new TrapSpaceTableModel();
		tresult = new EnhancedJTable(model);
		settings = new TrapSpaceParameters();
		settings.terminal = false;
		settings.diag = true;
		settings.bdd = !ClingoLauncher.isAvailable();
		settings.reduce = false;

		// needed for the scroll bars to appear as needed
		tresult.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
		tresult.getTableHeader().setReorderingAllowed(false);
		tresult.setDefaultRenderer(Object.class, new ColoredCellRenderer());
		tresult.setAutoCreateRowSorter(true);
		tresult.setCopyHeaders();

		JPanel main = new JPanel(new GridBagLayout());
		GridBagConstraints cst = new GridBagConstraints();
		cst.gridx = 0;
		cst.gridy = 0;
		cb_diag = new JCheckBox("Generate inclusion diagram");
		// main.add(cb_diag, cst); TODO: Inclusion diagrams is disabled in 3.1 version
		cst.gridy++;
		cst.weightx = 1;
		cst.weighty = 1;
		cst.fill = GridBagConstraints.BOTH;
		main.add(new JScrollPane(tresult), cst);
		setMainPanel(main);
	}

	@Override
	public void run(LogicalModel lmodel) {
		setRunning(true);
		if (cb_diag.isSelected()) {
			settings.diag = true;
			settings.terminal = false;
		} else {
			settings.diag = false;
			settings.terminal = true;
		}
		m_identifier = srv.getTask(lmodel);
		m_identifier.background(this);
	}

	public void taskUpdated(Task task) {
		if (task != m_identifier) {
			return;
		}

		TaskStatus status = m_identifier.getStatus();
		if (status == TaskStatus.CANCELED) {
			setRunning(false);
			cancel();
			return;
		}

		setRunning(false);
		TrapSpaceList solutions = m_identifier.getResult();
		if (solutions == null) {
			System.out.println("No solution");
			return;
		}

		if (settings.diag) {
			TrapSpaceInclusionDiagram diag = GSGraphManager.getInstance().getNewGraph(TrapSpaceInclusionDiagram.class,
					solutions);
			diag.setAssociatedGraph(lrg);
			GUIManager.getInstance().whatToDoWithGraph(diag);
			cancel();
			return;
		}

		model.setSolutions(solutions);

		TableCellRenderer headerRenderer = new VerticalTableHeaderCellRenderer();
		Enumeration<TableColumn> columns = tresult.getColumnModel().getColumns();

		if (columns.hasMoreElements()) {
			TableColumn col = columns.nextElement();
			col.setMinWidth(150);
			col.setMaxWidth(400);
		}
		while (columns.hasMoreElements()) {
			TableColumn col = columns.nextElement();
			col.setHeaderRenderer(headerRenderer);
			col.setMinWidth(20);
			col.setMaxWidth(40);
			col.setPreferredWidth(30);
		}
	}

	@Override
	protected boolean doCancel() {
		if (m_identifier != null && m_identifier.getStatus() == TaskStatus.RUNNING) {
			m_identifier.cancel();
			return false;
		}
		return true;
	}
}

class TrapSpaceParameters {

	public boolean reduce = false;

	public boolean bdd = false;

	public boolean terminal = false;
	public boolean diag = false;

	public TrapSpaceTask getTask(LogicalModel model) {
		TrapSpaceTask task = new TrapSpaceTask(model);

		task.bdd = this.bdd;
		task.reduce = this.reduce;
		task.terminal = this.terminal;
		task.diag = this.diag;

		return task;
	}
}
