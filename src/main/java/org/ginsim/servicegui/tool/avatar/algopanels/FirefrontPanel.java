package org.ginsim.servicegui.tool.avatar.algopanels;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.colomoto.biolqm.StatefulLogicalModel;
import org.ginsim.service.tool.avatar.params.AvatarParameters;
import org.ginsim.service.tool.avatar.service.EnumAlgorithm;
import org.ginsim.service.tool.avatar.simulation.FirefrontSimulation;
import org.ginsim.service.tool.avatar.simulation.Simulation;

/**
 * Class for managing the panel for Firefront simulations
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public class FirefrontPanel extends SimulationPanel {

	private static final long serialVersionUID = 1L;

	private JTextField maxExpandffB = new JTextField(), depthffB = new JTextField();
	private JTextField alphaB = new JTextField(), betaB = new JTextField();

	private String open = "<html><div style=\"width:265px;\">", end = "</div></html>";
	private String alphaVar = open
			+ "Minimum probability required for a state to be explored (be included the firefront set) [default: 10E-5]"
			+ end;
	private String betaVar = open
			+ "Maximum residual probability in the firefront set to stop the algorithm [default: 10E5]" + end;
	private String maxExpandVar = open
			+ "Maximum number of states in the Firefront to be expanded at each iteration [default: 10E3]"
			+ end;
	private String depthffVar = open + "Maximum depth (number of visited states) [default: 1E4]" + end;

	/**
	 * Instantiates the context of a simulation panel
	 *
	 * icon for the help tooltip
	 */
	public FirefrontPanel() {
		this.setBorder(BorderFactory.createTitledBorder(EnumAlgorithm.FIREFRONT + " parameters"));

		JLabel alphaL = new JLabel("Alpha");
		JLabel betaL = new JLabel("Beta");
		JLabel maxExpandffL = new JLabel("Expansion size ");
		JLabel depthffL = new JLabel("Max depth");

		setLayout(new GridLayout(4, 2));
		alphaL.setToolTipText(alphaVar);
		betaL.setToolTipText(betaVar);
		maxExpandffL.setToolTipText(maxExpandVar);
		depthffL.setToolTipText(depthffVar);

		depthffB.setText("1E4");
		alphaB.setText("1E-5");
		betaB.setText("1E-5");
		maxExpandffB.setText("1E3");
		add(depthffL);
		add(depthffB);
		add(alphaL);
		add(alphaB);
		add(betaL);
		add(betaB);
		add(maxExpandffL);
		add(maxExpandffB);
	}

	@Override
	public Simulation getSimulation(StatefulLogicalModel model, boolean quiet) throws Exception {
		int depth = (int) Double.parseDouble(depthffB.getText());
		double alpha = Double.parseDouble(alphaB.getText());
		double beta = Double.parseDouble(betaB.getText());
		FirefrontSimulation sim = new FirefrontSimulation();
		sim.addModel(model);
		sim.isGUI = true;
		// sim.maxRuns=Integer.valueOf(runsffB.getText());
		if (alpha > 0)
			sim.alpha = alpha; // optional
		if (beta > 0)
			sim.beta = beta; // optional
		if (depth > 0)
			sim.maxDepth = depth; // optional
		sim.maxExpand = (int) Double.parseDouble(maxExpandffB.getText());
		sim.quiet = quiet;

		return sim;
	}

	@Override
	public void load(AvatarParameters p) {
		p.ffDepth = depthffB.getText();
		p.ffMaxExpand = maxExpandffB.getText();
		p.ffAlpha = alphaB.getText();
		p.ffBeta = betaB.getText();
	}

	@Override
	public void unload(AvatarParameters param) {
		depthffB.setText(param.ffDepth);
		maxExpandffB.setText(param.ffMaxExpand);
		alphaB.setText(param.ffAlpha);
		betaB.setText(param.ffBeta);
	}
}
