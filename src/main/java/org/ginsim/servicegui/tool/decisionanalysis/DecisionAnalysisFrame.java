package org.ginsim.servicegui.tool.decisionanalysis;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.colomoto.biolqm.LogicalModel;
import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalTransitionGraph;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.utils.dialog.stackdialog.LogicalModelActionDialog;
import org.ginsim.service.tool.reg2dyn.SimulationParameterList;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;
import org.ginsim.service.tool.reg2dyn.SimulationParametersManager;
import org.ginsim.servicegui.tool.reg2dyn.PrioritySelectionPanel;


public class DecisionAnalysisFrame extends LogicalModelActionDialog  {
	private static final long serialVersionUID = -7619253564236142617L;
	private HierarchicalTransitionGraph htg;
	private JPanel mainPanel;
	private PrioritySelectionPanel selectPriorityClass;
	private SimulationParameters currentParameter;
	
	
	public DecisionAnalysisFrame(JFrame frame, HierarchicalTransitionGraph graph, RegulatoryGraph lrg) {
		super(lrg, frame, "STR_htg_decision_analysis", 475, 260);
		this.htg = (HierarchicalTransitionGraph) graph;
		if (this.htg.getReduction() != null && this.getReduction() == null){
			this.setReduction(this.htg.getReduction());
		}

    }
	
	protected JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
		
			c.gridx = 0;
			c.gridy = 0;
			JLabel label = new JLabel(Txt.t("STR_htg_decision_analysis_instructions"));
			mainPanel.add(label, c);
			c.gridy++;
			
			mainPanel.add(getPriorityClassSelector(), c);
			c.gridy++;
			
		}
		return mainPanel;
	}


	private PrioritySelectionPanel getPriorityClassSelector() {
        SimulationParameterList paramList = (SimulationParameterList) ObjectAssociationManager.getInstance().getObject( lrg, SimulationParametersManager.KEY, true);
		if (selectPriorityClass == null) {
			selectPriorityClass = new PrioritySelectionPanel(this, paramList.pcmanager);
			this.currentParameter = paramList.get(0);
			selectPriorityClass.setStore(currentParameter);
			for (Component comp : selectPriorityClass.getComponents()){
			    comp.setEnabled(false);}
		}
		return selectPriorityClass;
	}

	@Override
	public void run(LogicalModel model) {
		// enlever curentparameter
		//LogicalModel lg = model.getView(htg.getNodeOrder());

		DecisionAnalysis decisionAnalysis = new DecisionAnalysis(model, htg, currentParameter);
		this.brun.setEnabled(false);
		decisionAnalysis.run( GUIManager.getInstance().getGraphGUI( htg).getSelection().getSelectedNodes());
		this.brun.setEnabled(true);
		cancel();
	}

}
