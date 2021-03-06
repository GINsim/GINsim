package org.ginsim.servicegui.tool.reg2dyn.limitedSimulation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.colomoto.biolqm.LogicalModel;
import org.ginsim.common.application.GsException;
import org.ginsim.common.application.OptionStore;
import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNode;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalTransitionGraph;
import org.ginsim.core.graph.hierarchicaltransitiongraph.StatesSet;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphSelection;
import org.ginsim.gui.service.GSServiceGUIManager;
import org.ginsim.gui.shell.actions.LayoutAction;
import org.ginsim.gui.shell.MainFrame;
import org.ginsim.gui.utils.dialog.stackdialog.LogicalModelActionDialog;
import org.ginsim.gui.utils.widgets.Frame;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;
import org.ginsim.service.tool.reg2dyn.limitedsimulation.LimitedSimulationService;
import org.ginsim.service.tool.reg2dyn.limitedsimulation.OutgoingNodesHandlingStrategy;
import org.ginsim.service.tool.reg2dyn.limitedsimulation.SimulationConstraint;
import org.ginsim.service.tool.reg2dyn.limitedsimulation.StatesToHierarchicalEditTab;
import org.ginsim.service.tool.reg2dyn.priorityclass.PrioritySetList;
import org.ginsim.servicegui.tool.reg2dyn.PrioritySelectionPanel;

public class LimitedSimulationFrame extends LogicalModelActionDialog {
	private static final String OPTION_LIMITED_SIMULATION_INCLUDE_FIRST_OUTGOING_STATES = "limitedSimulation_includeFirstOutgoingStates";
	private static final long serialVersionUID = 5659168888297711105L;
	private HierarchicalTransitionGraph htg;
	private JPanel mainPanel;
	private JCheckBox strategy;
	private PrioritySelectionPanel selectPriorityClass = null;
	private PrioritySetList pcmanager = null;
	private SimulationParameters params = null;


	public LimitedSimulationFrame(JFrame frame, HierarchicalTransitionGraph htg, RegulatoryGraph lrg) {
		super(lrg, frame, "STR_limitedSimulation", 475, 260);
		this.htg = htg;
		setMainPanel(getMainPanel());			
	}

	protected JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
		
			c.gridx = 0;
			c.gridy = 0;
			JLabel label = new JLabel(Txt.t("STR_limitedSimulation_instructions"));
			mainPanel.add(label, c);
			
			c.gridy++;
			strategy = new JCheckBox(Txt.t("STR_limitedSimulation_alsoIncludeFirstOutgoingStates"));
			Boolean isStrategySelected = OptionStore.getOption(OPTION_LIMITED_SIMULATION_INCLUDE_FIRST_OUTGOING_STATES, true);
			strategy.setSelected(isStrategySelected.booleanValue());
			strategy.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					OptionStore.setOption(OPTION_LIMITED_SIMULATION_INCLUDE_FIRST_OUTGOING_STATES, strategy.isSelected());
				}
			});
			mainPanel.add(strategy, c);

			c.gridy++;
			mainPanel.add(getPriorityClassSelector(), c);

		}
		return mainPanel;
	}

	private PrioritySelectionPanel getPriorityClassSelector() {
		if (selectPriorityClass == null) {
			pcmanager = new PrioritySetList(lrg);
			selectPriorityClass = new PrioritySelectionPanel(this, pcmanager);
			params = new SimulationParameters(lrg);
			selectPriorityClass.setStore(params);
		}
		return selectPriorityClass;
	}

	private OutgoingNodesHandlingStrategy getStrategy() {
		if (strategy.isSelected()) {
			return OutgoingNodesHandlingStrategy.ADD_FIRST_OUTGOING_STATE;
		} 
		return OutgoingNodesHandlingStrategy.CONTAIN_TO_SELECTION;
	}
	
	private StatesSet getStateSet() {
		GraphSelection<HierarchicalNode, ?> selection = GUIManager.getInstance().getGraphGUI(htg).getSelection();
		List<HierarchicalNode> selectedNodes = selection.getSelectedNodes();
		if (selectedNodes == null || selectedNodes.size() == 0) {
			
			return null;
		}
		
		StatesSet s = (StatesSet)selectedNodes.get(0).statesSet.clone();
		if (selectedNodes.size() > 1) {
			for (Iterator<HierarchicalNode> iterator = selectedNodes.listIterator(1); iterator.hasNext();) {
				HierarchicalNode hierarchicalNode = iterator.next();
				s.merge(hierarchicalNode.statesSet);
			}
		}
		return s;
	}

	@Override
	public void run(LogicalModel model) {
		SimulationConstraint constraint = new SimulationConstraint(getStateSet(), getStrategy());
		if (!constraint.isValid()) {
			NotificationManager.publishError(htg, "no_hierarchicalNode_selected");
			return;
		}

		LimitedSimulationService service = GSServiceManager.getService(LimitedSimulationService.class);
		try {
			DynamicGraph dynGraph = service.run(htg, constraint, model, params);

			// force a layout on the STG: not perfect but better than the current weird situation
			for (Action action: GSServiceGUIManager.getAvailableActions( dynGraph)) {
				if (action instanceof LayoutAction) {
					action.actionPerformed(null);
					break;
				}
			}
			Frame frame = GUIManager.getInstance().newFrame(dynGraph);
			if (frame != null && frame instanceof MainFrame) {
				((MainFrame)frame).addTabToEditPanel(new StatesToHierarchicalEditTab(dynGraph, htg));
			}
		} catch (GsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
