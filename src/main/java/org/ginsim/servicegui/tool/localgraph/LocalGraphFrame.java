package org.ginsim.servicegui.tool.localgraph;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.dynamicgraph.DynamicNode;
import org.ginsim.core.graph.regulatorygraph.ActivityLevel;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.perturbation.Perturbation;
import org.ginsim.core.graph.regulatorygraph.perturbation.PerturbationHolder;
import org.ginsim.core.graph.view.style.StyleManager;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.graph.GraphSelection;
import org.ginsim.gui.graph.regulatorygraph.perturbation.PerturbationSelectionPanel;
import org.ginsim.gui.utils.data.SimpleStateListTableModel;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.gui.utils.widgets.EnhancedJTable;
import org.ginsim.service.tool.localgraph.LocalGraphCategory;
import org.ginsim.service.tool.localgraph.LocalGraphConfig;
import org.ginsim.service.tool.localgraph.LocalGraphService;
import org.ginsim.service.tool.localgraph.LocalGraphStyleProvider;

@SuppressWarnings("serial")
public class LocalGraphFrame extends StackDialog implements ActionListener,
		TableModelListener, ListSelectionListener, PerturbationHolder {

	private final int THRESHOLD_AUTO_REFRESH = 15;
	private Container mainPanel;
	private JButton addStatesButton, replaceStatesButton;

	private LocalGraphConfig config;
	private Map<RegulatoryMultiEdge, LocalGraphCategory> functionalityMap;
	private Map<RegulatoryNode, ActivityLevel> activityMap;

	private LocalGraphStyleProvider styleProvider;
	private final StyleManager<RegulatoryNode, RegulatoryMultiEdge> styleManager;

	private PerturbationSelectionPanel mutantSelectionPanel;
	private Perturbation perturbation;
	private StateSelectorTable sst;

	private boolean isColorized = false;

	public LocalGraphFrame(LocalGraphConfig config) {
		super(config.getGraph(), "STR_localGraph", 420, 260);
		this.styleManager = config.getGraph().getStyleManager();
		this.config = config;
		initialize();
	}

	public void initialize() {
		if (mainPanel == null) {
			this.setTitle(Txt.t("STR_localGraph"));

			mainPanel = new javax.swing.JPanel();
			mainPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			c.fill = GridBagConstraints.BOTH;
			c.weightx = 2;
			c.gridx = 0;
			c.gridy = 0;
			mutantSelectionPanel = new PerturbationSelectionPanel(this,
					config.getGraph(), this);
			mainPanel.add(mutantSelectionPanel, c);

			c.gridy++;
			c.weighty = 2;
			sst = new StateSelectorTable();
			sst.initPanel(config.getGraph(), "STR_localGraph_descr", true);
			sst.setState(new byte[config.getGraph().getNodeOrderSize()]);
			sst.table.getModel().addTableModelListener(this);
			sst.table.getSelectionModel().addListSelectionListener(this);
			mainPanel.add(sst, c);

			if (config.getDynamic() != null) {
				c.weightx = 0;
				c.weighty = 0;
				c.fill = GridBagConstraints.EAST;
				c.gridy++;
				addStatesButton = new JButton(
						Txt.t("STR_localGraph_addStates"));
				addStatesButton.addActionListener(this);
				mainPanel.add(addStatesButton, c);
				c.gridx++;
				replaceStatesButton = new JButton(
						Txt.t("STR_localGraph_getStates"));
				replaceStatesButton.addActionListener(this);
				mainPanel.add(replaceStatesButton, c);
			}
		}
		setMainPanel(mainPanel);
	}

	private void undoColorize() {
		if (isColorized) {
			isColorized = false;
			styleManager.setStyleProvider(null);
		}
	}

	public void doClose() {
		this.undoColorize();
		super.doClose();
	}

	protected void run() {
		LocalGraphService service = GSServiceManager.getService(LocalGraphService.class);
		List<byte[]> states = sst.getStates();
		if (states == null || states.size() < 1) {
			functionalityMap = null;
			activityMap = null;
			undoColorize();
			return;
		}
		try {
			activityMap = getActivities(states);
			functionalityMap = service.run(config.getGraph(), states, getPerturbation());
			doColorize();
		} catch (GsException e) {
			LogManager.error(e);
			NotificationManager.publishError(config.getGraph(), "Local Graph: "
					+ e.getLocalizedMessage());
		}
	}

	private Map<RegulatoryNode, ActivityLevel> getActivities(List<byte[]> states) {
		if (states == null || states.size() < 1) {
			return null;
		}
		// TODO: should we color nodes for multiple states?
		byte[] state = states.get(0);
		if (activityMap == null) {
			activityMap = new HashMap<RegulatoryNode, ActivityLevel>();
		} else {
			activityMap.clear();
		}
		int idx = 0;
		for (RegulatoryNode node : config.getGraph().getNodeOrder()) {
			byte cur = state[idx++];
			if (cur == node.getMaxValue()) {
				activityMap.put(node, ActivityLevel.ACTIVE);
			} else if (cur > 0) {
				activityMap.put(node, ActivityLevel.MIDLEVEL);
			} else if (cur == 0) {
				activityMap.put(node, ActivityLevel.INACTIVE);
			} else {
				activityMap.put(node, ActivityLevel.FREE);
			}
		}
		return activityMap;
	}

	public void actionPerformed(ActionEvent e) {
		GraphGUI graphGUI = GUIManager.getInstance().getGraphGUI(
				config.getDynamic());
		if (graphGUI != null) {
			GraphSelection<DynamicNode, Edge<DynamicNode>> selection = graphGUI
					.getSelection();
			if (e.getSource() == addStatesButton) {
				List<DynamicNode> selected = selection.getSelectedNodes();
				if (selected != null) {
					List<byte[]> states = new ArrayList<byte[]>();
					for (DynamicNode state : selected) {
						states.add(state.state);
					}
					sst.setStates(states);
				}
			} else if (e.getSource() == replaceStatesButton) {
				List<DynamicNode> selected = selection.getSelectedNodes();
				if (selected != null) {
					sst.ssl.data.clear();
					List<byte[]> states = new ArrayList<byte[]>();
					for (DynamicNode state : selected) {
						states.add(state.state);
					}
					sst.setStates(states);
				}
			}
		}

	}

	public void tableChanged(TableModelEvent e) {
		changed();
	}

	public void valueChanged(ListSelectionEvent e) {
		changed();
	}

	private void changed() {
		// if (isColorized){
		// undoColorize();
		// }
		functionalityMap = null;
		activityMap = null;
		isColorized = false;
		doColorize();
	}

	private void doColorize() {
		if (isColorized) {
			return;
		}
		isColorized = true;
		if (functionalityMap == null) {
			run();
			if (functionalityMap == null) {
				return;
			}
		}

		if (styleProvider == null) {
			styleProvider = new LocalGraphStyleProvider(styleManager, functionalityMap, activityMap);
		} else {
			styleProvider.setMapping(functionalityMap, activityMap);
		}
		styleManager.setStyleProvider(styleProvider);
	}

	public void cancel() {
		this.undoColorize();
		super.cancel();
	}

	@Override
	public Perturbation getPerturbation() {
		return perturbation;
	}

	@Override
	public void setPerturbation(Perturbation perturbation) {
		if (perturbation != this.perturbation) {
			this.perturbation = perturbation;
			changed();
		}
	}

}

class StateSelectorTable extends JPanel {
	private static final long serialVersionUID = -7502297761417113651L;
	protected SimpleStateListTableModel ssl;
	protected EnhancedJTable table;

	public byte[] getState() {
		return ssl.getState(0);
	}

	public void setStates(List<byte[]> states) {
		for (Iterator<byte[]> it = states.iterator(); it.hasNext();) {
			ssl.addState(it.next());
		}
	}

	public List<byte[]> getStates() {
		int selectedRowCount = table.getSelectedRowCount();
		int rowCount = table.getRowCount();
		List<byte[]> states = new ArrayList<byte[]>();

		if (rowCount <= 1
				|| (selectedRowCount == 1 && table.getSelectedRow() >= rowCount - 1))
			return null;

		if (selectedRowCount > 0) {
			int[] selectedRows = table.getSelectedRows();
			for (int i = 0; i < selectedRows.length; i++) {
				if (selectedRows[i] != rowCount - 1)
					states.add(ssl.getState(selectedRows[i]));
			}
			return states;
		} else if (rowCount > 0) {
			for (int i = 0; i < rowCount - 1; i++) {
				states.add(ssl.getState(i));
			}
			return states;
		}
		states.add(new byte[table.getColumnCount()]);
		return states;
	}

	public void setState(byte[] state) {
		if (ssl.data.size() > 0)
			ssl.data.remove(0);
		ssl.addState(state);
	}

	protected GridBagConstraints initPanel(RegulatoryGraph g, String desckey,
			boolean isEditable) {
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
