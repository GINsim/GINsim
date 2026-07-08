package org.ginsim.servicegui.tool.avatar.algopanels;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.colomoto.biolqm.StatefulLogicalModel;
import org.ginsim.service.tool.avatar.params.AvatarParameters;
import org.ginsim.service.tool.avatar.service.EnumAlgorithm;
import org.ginsim.service.tool.avatar.simulation.MonteCarloSimulation;
import org.ginsim.service.tool.avatar.simulation.Simulation;

/**
 * Class for managing the panel for Monte Carlo simulations
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public class MonteCarloPanel extends SimulationPanel {

	private static final long serialVersionUID = 1L;

	private JTextField runs = new JTextField();
	private JTextField depth = new JTextField();

	private String open = "<html><div style=\"width:265px;\">", end = "</div></html>";
	private String runsVar = open + "Maximum number of iterations [default: 1000]" + end;
	private String depthVar = open + "Maximum depth for each simulation [default: 1000]" + end;

	/**
	 * Instantiates the context of a simulation panel
	 *
	 * icon for the help tooltip
	 */
	public MonteCarloPanel() {
		this.setBorder(BorderFactory.createTitledBorder(EnumAlgorithm.MONTE_CARLO + " Parameters"));

		JLabel runsL = new JLabel("# Runs");
		JLabel depthL = new JLabel("Max depth");

		setLayout(new GridLayout(2, 2));
		runsL.setToolTipText(runsVar);
		depthL.setToolTipText(depthVar);

		runs.setText("1000");
		depth.setText("1000");
		add(runsL);
		add(runs);
		add(depthL);
		add(depth);
	}

	@Override
	public Simulation getSimulation(StatefulLogicalModel model, boolean quiet) throws Exception {
		MonteCarloSimulation sim = new MonteCarloSimulation();
		sim.addModel(model);
		sim.isGUI = true;
		if (this.runs.getText().equals(""))
			this.runs.setText("1000");
		sim.runs = (int) Double.parseDouble(this.runs.getText());
		sim.maxSteps = (int) Double.parseDouble(this.depth.getText());
		sim.quiet = quiet;

		return sim;
	}

	@Override
	public void load(AvatarParameters p) {
		p.mcDepth = depth.getText();
		p.mcRuns = runs.getText();
	}

	@Override
	public void unload(AvatarParameters param) {
		depth.setText(param.mcDepth);
		runs.setText(param.mcRuns);
	}

}
