package org.ginsim.gui.utils.dialog.stackdialog;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.colomoto.biolqm.service.LQMServiceManager;
import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.modifier.reduction.ReductionModifier;
import org.colomoto.biolqm.modifier.reduction.ReductionService;
import org.ginsim.common.application.GsException;
import org.ginsim.common.callable.ProgressListener;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedState;
import org.ginsim.core.graph.regulatorygraph.perturbation.ListOfPerturbations;
import org.ginsim.core.graph.regulatorygraph.perturbation.Perturbation;
import org.ginsim.core.graph.regulatorygraph.perturbation.PerturbationHolder;
import org.ginsim.core.graph.regulatorygraph.perturbation.PerturbationManager;
import org.ginsim.gui.graph.regulatorygraph.perturbation.PerturbationSelectionPanel;
import org.ginsim.service.tool.modelreduction.ListOfReductionConfigs;
import org.ginsim.service.tool.modelreduction.ReductionConfig;
import org.ginsim.service.tool.modelreduction.ReductionConfigManager;
import org.ginsim.service.tool.modelreduction.ReductionHolder;
import org.ginsim.servicegui.tool.modelreduction.ReductionSelectionPanel;

abstract public class LogicalModelActionDialog extends StackDialog implements ProgressListener, PerturbationHolder, ReductionHolder, ChangeListener {

	private static final ObjectAssociationManager OManager = ObjectAssociationManager.getInstance();
	private static final ReductionService reductionService = LQMServiceManager.get(ReductionService.class);
	
	protected final RegulatoryGraph lrg;
	
	public ListOfPerturbations perturbations;
	public ListOfReductionConfigs reductions;
	public PerturbationSelectionPanel perturbationPanel;
	public ReductionSelectionPanel reductionPanel;
	public Perturbation perturbation = null;
	public ReductionConfig reduction = null;
	public ReductionConfig realReduction = null;

    private NamedState pattern = null;
	private String userID = null;

	protected JPanel mainPanel = new JPanel(new GridBagLayout());
    private JCheckBox cb_propagate = new JCheckBox("Propagate fixed values");
    //
	protected JCheckBox cb_simplify = new JCheckBox("Strip (pseudo-)outputs");

    public LogicalModelActionDialog(RegulatoryGraph lrg, Frame parent, String id, int w, int h) {
        super(parent, id, w, h);
		//this.cb_simplify.setVisible(false);
        this.lrg = lrg;
        this.perturbations = (ListOfPerturbations)OManager.getObject(lrg, PerturbationManager.KEY, true);
		this.reductions = (ListOfReductionConfigs)OManager.getObject(lrg, ReductionConfigManager.KEY, true);
        if (reductions.size() < 1 || (reductions.size() >= 1 && !reductions.ifOutputIn())){
			reductions.create(true) ;}
		perturbationPanel = new PerturbationSelectionPanel(this, lrg, this);
        reductionPanel = new ReductionSelectionPanel(this, lrg, this);
        super.setMainPanel(getMainPanel());
		cb_simplify.setVisible(false);
        cb_simplify.addChangeListener(this);
        cb_propagate.addChangeListener(this);
		this.addWindowListener(new java.awt.event.WindowAdapter() { 
			public void windowClosing(java.awt.event.WindowEvent e) {
				cancel();
			}
		});
		if(getReduction() != null && reduction.getName().contains("Output")){
			cb_simplify.setSelected(true);

		    realReduction = null;}
		else {
			realReduction = reduction;
			cb_simplify.setSelected(false);}
    }

    /**
     * Change the ID used to remember the selected perturbation (and other settings)
     * @param userID string of user id
     */
    public void setUserID(String userID) {
    	this.userID = userID;
		this.perturbation = perturbations.getUsedPerturbation(userID);
		this.reduction = reductions.getUsedReduction(userID);
		if (reduction != null && reduction.getName().contains("Output")){
			cb_simplify.setSelected(true);
			realReduction = null;
		}
		else {
			cb_simplify.setSelected(false);
			realReduction = reduction;
		}
        cb_propagate.setSelected(reductions.isPropagatingFixed(userID));
		cb_simplify.repaint();
		perturbationPanel.refresh();
		reductionPanel.refresh();
    }

	/**
	 * Get the main panel
	 * @return the JPanel
	 */
	protected JPanel getMainPanel() {

        GridBagConstraints c = new GridBagConstraints();
        // perturbation panel
        c.weightx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(perturbationPanel, c);
        // reduction panel
        c.gridy = 1;
		c.weightx = 1;
        c.gridwidth = 1;
//        c.gridheight = 2;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(reductionPanel, c);

        // simplification checkboxes
        c.gridx = 1;
        c.weightx = 0;
       // mainPanel.add(cb_propagate, c);
        c.gridx++; // TODO: adapt layout!
        mainPanel.add(cb_simplify, c);
        return mainPanel;
    }
    
    public void setProgress(int n) {
    	setMessage(""+n);
    }
    public void setProgress(String s) {
    	setMessage(s);
    }

	@Override
	public void milestone(Object data) {
    	setMessage(data.toString());
	}

	@Override
	public Perturbation getPerturbation() {
		return perturbation;
	}
	
	@Override
	public void setPerturbation(Perturbation perturbation) {
		if (userID != null) {
			perturbations.usePerturbation(userID, perturbation);
			this.perturbation = perturbations.getUsedPerturbation(userID);
		} else {
			this.perturbation = perturbation;
		}
	}

	@Override
	public ReductionConfig getReduction() {
		return reduction;
	}

	public ReductionConfig getRealReduction() {
		return realReduction;
	}

	@Override
	public void setReduction(ReductionConfig reduc) {
		if (userID != null ) {
			reductions.useReduction(userID, reduc);
			this.reduction = reductions.getUsedReduction(userID);
			if (reduction != null && reduction.getName().contains("Output")) {
			     realReduction = null;
				reductions.setOutputStrippers(true);
				 this.cb_simplify.setSelected(true);
			}
			else {
				realReduction = reduction;
				this.cb_simplify.setSelected(false);
				reductions.setOutputStrippers(false);
			}
		} else {
			reduction = reduc;
			if (reduction != null && reduction.getName().contains("Output")){
					reductions.setOutputStrippers(true);
					realReduction = null;
					this.cb_simplify.setSelected(true);
				}
				else {
					realReduction = reduction;
					reductions.setOutputStrippers(false);
					this.cb_simplify.setSelected(false);
				}
			}
		cb_simplify.repaint();
	}

	@Override
	public NamedState getPattern() {
		return pattern;
	}

	@Override
	public void setPattern(NamedState pattern) {
//		if (userID != null) {
//			reductions.useReduction(userID, reduction);
//			this.pattern = reductions.getUsedReduction(userID);
//		} else {
			this.pattern = pattern;
//		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (userID != null){
			//if (e.getSource().equals(reductions)) {
				if (reduction != null && reduction.getName().contains("Output")) {
					reductions.setOutputStrippers(true);
					realReduction = null;
					cb_simplify.setSelected(true);
				}
				else {
					realReduction = reduction;
					reductions.setOutputStrippers(false);
					cb_simplify.setSelected(false);
				}
			//}
			//setReduction(null);
			//&&} reductions.getUsedReduction(userID) != null) {
			reductions.setPropagateFixed(userID, cb_propagate.isSelected());
		}
		else {
			if (reduction != null && reduction.getName().contains("Output")){
					reductions.setOutputStrippers(true);
					realReduction = null;
					this.cb_simplify.setSelected(true);
				}
			else {
					realReduction = reduction;
					reductions.setOutputStrippers(false);
					this.cb_simplify.setSelected(false);
				}
		}
		reductionPanel.refresh();
		cb_simplify.repaint();
	}

	@Override
	public void setResult(Object result) {
		// empty implementation: not all derived classes will want to do something
	}

	/**
	 * Intercept the setMainPanel method to integrate with this frame's panel
	 * 
	 * @param panel component panel
	 */
	@Override
	public void setMainPanel(Component panel) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 2;
		c.gridwidth = 2;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		
		mainPanel.add(panel, c);
		mainPanel.validate();
	}
	
	@Override
	protected void run() throws GsException {

        // retrieve the model
        LogicalModel model = lrg.getModel();


        // apply model modifiers: perturbation and reduction
        Perturbation p = getPerturbation();
        if (p != null) {
        	model = getPerturbation().apply(model);
        }

		// TODO: merge all reductions in a single pass
		ReductionConfig reduction = getRealReduction();
		if (reduction != null ) {
			model = reduction.apply(model);
			
		}

        ReductionModifier reducer = reductionService.getModifier(model);
        if (cb_propagate.isSelected()) {
			reducer.handleFixed = true;
			reducer.purgeFixed = true;
        }
        
       if (cb_simplify.isSelected()) {//cb_simplify.isSelected(
			reducer.handleOutputs = true;
        }
	   else reducer.handleOutputs = false;

		// Apply input pattern
		if (pattern != null) {
			reducer.pattern = pattern.getStatePattern();
		} else {
			reducer.pattern = null;
		}

        try {
        	model = reducer.call();
		} catch (Exception e) {
        	throw new GsException("Error applying model reduction", e);
		}

		run(model);
	}

	/**
	 * Run function
	 * @param model the model LogicalModel
	 */
	public abstract void run(LogicalModel model);
}
