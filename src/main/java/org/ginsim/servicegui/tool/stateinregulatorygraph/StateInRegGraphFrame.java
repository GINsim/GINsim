package org.ginsim.servicegui.tool.stateinregulatorygraph;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.tool.fixpoints.FixpointList;
import org.colomoto.biolqm.tool.fixpoints.FixpointTask;
import org.colomoto.biolqm.tool.trapspaces.TrapSpaceList;
import org.colomoto.biolqm.tool.trapspaces.TrapSpaceTask;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.perturbation.Perturbation;
import org.ginsim.core.graph.regulatorygraph.perturbation.PerturbationStore;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.graph.dynamicgraph.StableTableModel;
import org.ginsim.gui.graph.regulatorygraph.perturbation.PerturbationSelectionPanel;
import org.ginsim.gui.graph.view.style.StyleColorizerCheckbox;
import org.ginsim.gui.utils.data.SimpleStateListTableModel;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.gui.utils.widgets.EnhancedJTable;
import org.ginsim.service.tool.stablestates.StableStatesService;
import org.ginsim.servicegui.tool.regulatorygraphanimation.LRGStateStyleProvider;
import org.ginsim.servicegui.tool.trapspace.TrapSpaceTableModel;

/**
 * The main frame.
 * 
 * Display a JTabbedPane that contains different TabComponantProvidingAState
 * having a method to get a state
 *
 */
public class StateInRegGraphFrame extends StackDialog {
	private static final long serialVersionUID = -5576209151262677441L;

	private RegulatoryGraph regGraph;

	private Container mainPanel;
	private JTabbedPane tabbedPane;

	private final LRGStateStyleProvider styleProvider;

	private StyleColorizerCheckbox colorizerCheckbox;

	public StateInRegGraphFrame(JFrame frame, Graph graph) {
		super(frame, "stateInRegGraph", 420, 260);
		this.regGraph = (RegulatoryGraph) graph;
		this.styleProvider = new LRGStateStyleProvider(regGraph);
		setMainPanel(getMainPanel());
	}

	private Container getMainPanel() {
		if (mainPanel == null) {
			this.setTitle(Txt.t("STR_stateInRegGraph"));
			mainPanel = new javax.swing.JPanel();
			mainPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			c.gridx = 0;
			c.gridy = 0;
			c.fill = GridBagConstraints.BOTH;
			c.ipadx = 10;
			mainPanel.add(new JLabel(Txt.t("STR_stateInRegGraph_descr")), c);

			c.gridy++;
			c.gridx = 0;
			c.ipadx = 0;
			c.ipady = 0;
			c.weightx = 1;
			c.weighty = 1;
			tabbedPane = new JTabbedPane();
			tabbedPane.add(Txt.t("STR_stateInRegGraph_state"), new State(regGraph, this));
			tabbedPane.add(Txt.t("STR_stateInRegGraph_stablestate"), new StableState(regGraph, this));
			tabbedPane.add(Txt.t("STR_stateInRegGraph_trapspaces"), new TrapSpaces(regGraph, this));
			tabbedPane.add(Txt.t("STR_stateInRegGraph_maxvalues"), new MaxValues(regGraph, this));
			mainPanel.add(tabbedPane, c);

			c.gridy++;
			c.ipady = 20;
			c.weightx = 0;
			c.weighty = 0;
			mainPanel.add(new JLabel(""), c);

			c.gridy++;
			c.ipady = 0;
			c.fill = GridBagConstraints.CENTER;
			colorizerCheckbox = new StyleColorizerCheckbox("stateInRegGraph.", regGraph, styleProvider);
			mainPanel.add(colorizerCheckbox, c);
		}
		return mainPanel;
	}

	protected void run() {
		byte[] state = ((TabComponantProvidingAState) tabbedPane.getSelectedComponent()).getState();
		styleProvider.setState(state);
		colorizerCheckbox.refresh();
	}

	public void cancel() {
		colorizerCheckbox.undoColorize();
		super.cancel();
	}

	@Override
	public void doClose() {
		colorizerCheckbox.undoColorize();
		dispose();
		// super.cancel();
	}
}

/**
 * Provide proper initial layout and an abstract method returning a state.
 */
abstract class TabComponantProvidingAState extends JPanel {
	private static final long serialVersionUID = -7502297761417113651L;
	protected SimpleStateListTableModel ssl;
	protected EnhancedJTable table;

	protected StateInRegGraphFrame stateInRegGraphFrame;

	abstract public byte[] getState();

	public TabComponantProvidingAState(StateInRegGraphFrame stateInRegGraphFrame) {
		this.stateInRegGraphFrame = stateInRegGraphFrame;
	}

	protected GridBagConstraints initPanel(RegulatoryGraph g, String desckey, boolean isEditable) {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 8;
		c.fill = GridBagConstraints.BOTH;
		add(new JLabel(Txt.t(desckey)), c);

		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.ipady = 0;
		ssl = new SimpleStateListTableModel(g, isEditable);
		table = new EnhancedJTable(ssl);
		table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
		add(new JScrollPane(table), c);
		return c;
	}
}

/**
 * Return the state (1,1,...,1) and display the state (max_1, max_2,..., max_n)
 * where max_i is the max value of the node i.
 */
class MaxValues extends TabComponantProvidingAState implements ListSelectionListener {
	private static final long serialVersionUID = -6227864741059321245L;
	private byte[] state1;

	public MaxValues(RegulatoryGraph g, StateInRegGraphFrame stateInRegGraphFrame) {
		super(stateInRegGraphFrame);
		state1 = new byte[g.getNodeOrderSize()];
		for (int i = 0; i < g.getNodeOrderSize(); i++) {
			state1[i] = 1;
		}
		initPanel(g, "STR_stateInRegGraph_maxValuesdescr", false);
		ssl.addState(ssl.getMaxValues());
		table.getSelectionModel().addListSelectionListener(this);
	}

	public byte[] getState() {
		return state1;
	}

	public void valueChanged(ListSelectionEvent e) {
		stateInRegGraphFrame.run();
	}
}

/**
 * Provide a table to enter a state manually.
 *
 */
class State extends TabComponantProvidingAState implements ListSelectionListener, TableModelListener {
	private static final long serialVersionUID = 918581816104803491L;

	public State(RegulatoryGraph g, StateInRegGraphFrame stateInRegGraphFrame) {
		super(stateInRegGraphFrame);
		initPanel(g, "STR_stateInRegGraph_statedescr", true);
		table.getSelectionModel().addListSelectionListener(this);
		table.getModel().addTableModelListener(this);
	}

	public byte[] getState() {
		return ssl.getState(table.getSelectedRow());
	}

	public void tableChanged(TableModelEvent e) {
		stateInRegGraphFrame.run();
	}

	public void valueChanged(ListSelectionEvent e) {
		stateInRegGraphFrame.run();
	}
}

/**
 * Return a state provided by the StableState plugin.
 */
class StableState extends TabComponantProvidingAState {
	private static final long serialVersionUID = 1301082532863004279L;

	JTable table;
	private PerturbationSelectionPanel mutantSelectionPanel;
	private PerturbationStore mutantStore;
	private RegulatoryGraph g;
	private JButton computeStableStateButton;

	private StableTableModel tableModel;

	public StableState(RegulatoryGraph g, StateInRegGraphFrame stateInRegGraphFrame) {
		super(stateInRegGraphFrame);
		this.g = g;
		setMainPanel();
	}

	private void setMainPanel() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 8;
		c.fill = GridBagConstraints.BOTH;
		add(new JLabel(Txt.t("STR_stateInRegGraph_stabledescr")), c);

		c.gridy++;
		c.ipady = 0;
		mutantStore = new PerturbationStore();
		mutantSelectionPanel = new PerturbationSelectionPanel(stateInRegGraphFrame, g, mutantStore);
		add(mutantSelectionPanel, c);

		c.gridy++;
		c.weightx = 1;
		c.weighty = 1;
		c.ipady = 0;
		tableModel = new StableTableModel();
		tableModel.setResult(null);
		table = new EnhancedJTable(tableModel);
		table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					stateInRegGraphFrame.run(); // Colorize the graph when the selection change
				}
			}
		});
		add(new JScrollPane(table), c);

		c.gridy++;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.CENTER;
		computeStableStateButton = new JButton(Txt.t("STR_stateInRegGraph_computeStableState"));
		add(computeStableStateButton, c);
		computeStableStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run(); // call run() when the button is clicked
			}
		});
	}

	protected void run() {
		LogicalModel model = g.getModel();
		Perturbation p = mutantStore.getPerturbation();
		if (p != null) {
			p.update(model);
		}
		FixpointTask task = GSServiceManager.getService(StableStatesService.class).getTask(model);
		FixpointList result = null;
		try {
			result = task.call();
			tableModel.setResult(result);
		} catch (Exception e) {
			LogManager.error(e);
		}
	}

	public byte[] getState() {
		byte[] state = tableModel.getState(table.getSelectedRow());
		if (state == null) {
			return null;
		}
		byte[] ret = new byte[state.length];
		for (int i = 0; i < state.length; i++) {
			ret[i] = (byte) state[i];
		}
		return ret;
	}
}

/**
 * Return a state provided by the TrapSpaces plugin.
 */
class TrapSpaces extends TabComponantProvidingAState {
	private static final long serialVersionUID = 1301082532863004279L;

	JTable table;
	private PerturbationSelectionPanel mutantSelectionPanel;
	private PerturbationStore mutantStore;
	private RegulatoryGraph g;
	private JButton computeTrapStateButton;

	private TrapSpaceTableModel tableModel;

	public TrapSpaces(RegulatoryGraph g, StateInRegGraphFrame stateInRegGraphFrame) {
		super(stateInRegGraphFrame);
		this.g = g;
		setMainPanel();
	}

	private void setMainPanel() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 8;
		c.fill = GridBagConstraints.BOTH;
		add(new JLabel(Txt.t("STR_stateInRegGraph_trapSpacesdescr")), c);

		c.gridy++;
		c.ipady = 0;
		mutantStore = new PerturbationStore();
		mutantSelectionPanel = new PerturbationSelectionPanel(stateInRegGraphFrame, g, mutantStore);
		add(mutantSelectionPanel, c);

		c.gridy++;
		c.weightx = 1;
		c.weighty = 1;
		c.ipady = 0;
		tableModel = new TrapSpaceTableModel();
		tableModel.setSolutions(null);
		table = new EnhancedJTable(tableModel);
		table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					stateInRegGraphFrame.run(); // Colorize the graph when the selection change
				}
			}
		});
		add(new JScrollPane(table), c);

		c.gridy++;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.CENTER;
		if (this.g.getModel().isBoolean()) {
			computeTrapStateButton = new JButton(Txt.t("STR_stateInRegGraph_computeTrapSpaces"));
			add(computeTrapStateButton, c);
			computeTrapStateButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					run(); // call run() when the button is clicked
				}
			});
		} else {
			add(new JLabel("Cannot map Boolean trap spaces on a non-boolean table. Booleanize the model first."), c);
		}
	}

	protected void run() {
		LogicalModel model = g.getModel();
		Perturbation p = mutantStore.getPerturbation();
		if (p != null) {
			p.update(model);
		}

		TrapSpaceTask task = new TrapSpaceTask(model);
		TrapSpaceList result = null;
		try {
			task.run();
			result = task.getResult();
			tableModel.setSolutions(result);
		} catch (Exception e) {
			LogManager.error(e);
		}
	}

	public byte[] getState() {
		byte[] state = tableModel.getState(table.getSelectedRow());
		if (state == null) {
			return null;
		}
		byte[] ret = new byte[state.length];
		for (int i = 0; i < state.length; i++) {
			ret[i] = (byte) state[i];
		}
		return ret;
	}
}