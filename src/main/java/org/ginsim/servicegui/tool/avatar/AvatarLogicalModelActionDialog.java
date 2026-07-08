package org.ginsim.servicegui.tool.avatar;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeListener;

import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.perturbation.PerturbationHolder;
import org.ginsim.gui.utils.dialog.stackdialog.LogicalModelActionDialog;
import org.ginsim.service.tool.modelreduction.ReductionHolder;
import org.ginsim.servicegui.tool.avatar.others.TitleToolTipPanel;

/**
 * Class for managing contextual aspects associated with the main panel
 * (including the responsibility to update the applicable perturbations and
 * reductions to the input model)
 * 
 * @author Rui Henriques
 * @version 1.0
 */
abstract public class AvatarLogicalModelActionDialog extends LogicalModelActionDialog
		implements PerturbationHolder, ReductionHolder, ChangeListener {

	private static final long serialVersionUID = 6959189835195994930L;
	/**
	 * int yAdjustment
	 */
	protected int yAdjustment = 130;

	/**
	 * Creates the necessary context to instantiate the panel of avatar simulations
	 * 
	 * @param lrg the target regulatory graph
	 * @param parent the parent panel
	 * @param id the ID of the current panel (avatar simulations)
	 * @param w the suggested width for the main panel
	 * @param h the suggested height for the main panel
	 */
	public AvatarLogicalModelActionDialog(RegulatoryGraph lrg, Frame parent, String id, int w, int h) {
		super(lrg, parent, id, w, h);

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				cancel();
			}
		});
	}

	@Override
	protected JPanel getMainPanel() {
		return mainPanel;
	}

	/**
	 * Adds a component to the main panel satisfying the inputted gridbag layout
	 * constraint
	 * 
	 * @param panel
	 *            the component to be added to the main panel
	 * @param g
	 *            the constraints associated with the positioning of the component
	 *            (according to gridbaglayout)
	 */
	public void setMainPanel(Component panel, GridBagConstraints g) {
		mainPanel.add(panel, g);
	}
}
