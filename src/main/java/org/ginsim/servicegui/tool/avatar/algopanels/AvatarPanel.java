package org.ginsim.servicegui.tool.avatar.algopanels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.colomoto.biolqm.StatefulLogicalModel;
import org.ginsim.service.tool.avatar.params.AvatarParameters;
import org.ginsim.service.tool.avatar.service.EnumAlgorithm;
import org.ginsim.service.tool.avatar.simulation.AvatarSimulation;
import org.ginsim.service.tool.avatar.simulation.AvatarSimulation.AvatarStrategy;
import org.ginsim.service.tool.avatar.simulation.Simulation;

/**
 * Class for managing the panel for Avatar simulations
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public class AvatarPanel extends SimulationPanel {

	private static final long serialVersionUID = 1L;
	private JTextField runsB = new JTextField(), tauB = new JTextField(), depthB = new JTextField(),
			aproxDepth = new JTextField();
	private JTextField minTranB = new JTextField(), minCycleB = new JTextField(), maxPsizeB = new JTextField(),
			maxRewiringSizeB = new JTextField();
	private JCheckBox keepTransB = new JCheckBox("Keep transients");
	private JComboBox<String> strategy = new JComboBox<String>(new DefaultComboBoxModel<String>(
			new String[] { "Exact exit probabilities", "Uniform exit probabilities" }));

	private String open = "<html><div style=\"width:265px;\">", end = "</div></html>";
	private String runsVar = open + "Number of simulations to perform (default: 1000)" + end;
	private String tauVar = open + "Degree of SCC expansion (default: n=3)" + end;
	private String depthVar = open + "Maximum depth of the search per simulation" + end;
	private String keepTransVar = open
			+ "Check this book to keep in memory the discovered transient SCCs for upcoming simulations" + end;
	private String strategyVar = open
			+ "Method for SCC rewiring:<br>1) 'Exact exit probabilities' computes the exact probability of exit paths"
			// +"2)'Approximate' strategy computes the path probabilities up to a maximum
			// depth (memory-efficient yet can lead to time overhead),"
			+ "<br>2) 'Uniform exit probabilities' creates transitions from all SCC states to all exit states with uniform probabilities"
			+ end;
	private String minTransVar = open
			+ "Mininum size of a transient SCC to be kept for subsequent simulations (default: 200)" + end;
	private String minCycleVar = open + "Minimum number of states in a SCC to trigger rewiring (default: 4)" + end;
	private String maxPSizeVar = open + "Minimum number of states in a SCC to stop expansion (default: 10000)" + end;
	private String maxRewiringSizeVar = open + "Maximum number of states in a SCC to be rewired at a time" + end;
	private JPanel panelAvatarP1, panelAvatarP2;

	/**
	 * Instantiates the context of a simulation panel
	 *
	 * icon for the help tooltip
	 */
	public AvatarPanel() {
		// this.setBorder(BorderFactory.createTitledBorder(EnumAlgorithm.AVATAR + "
		// parameters"));

		runsB.setText("1000");
		tauB.setText("3");
		depthB.setText("1E6");
		keepTransB.setSelected(true);
		aproxDepth.setText("7");
		minTranB.setText("200");
		minCycleB.setText("4");
		maxPsizeB.setText("1E4");
		maxRewiringSizeB.setText("1E3");
		refresh();

		// TODO: ptgm: is this change really necessary ?
		strategy.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (strategy.getSelectedIndex() == 0) {
					keepTransB.setSelected(true);
					maxRewiringSizeB.setText("1E3");
				} else {
					keepTransB.setSelected(false);
					maxRewiringSizeB.setText("1E5");
				}
				// if(strategy.getSelectedIndex()==1) p1c.setVisible(true);
				// else p1c.setVisible(false);
				repaint();// refresh();
			}
		});
	}

	private void refresh() {
		removeAll();
		setLayout(new GridBagLayout());

		JLabel runsL = new JLabel("#Runs");
		JLabel tauL = new JLabel("Tau");
		JLabel depthL = new JLabel("Max depth");
		JLabel strategyL = new JLabel("Rewiring");
		JLabel aproxDepthL = new JLabel("with depth ");
		JLabel minTranL = new JLabel("Min transient size");
		JLabel minCycleL = new JLabel("Min states to rewire ");
		JLabel maxPsizeL = new JLabel("Expansion limit ");
		JLabel maxRewiringSizeL = new JLabel("Rewiring limit ");

		keepTransB.setToolTipText(keepTransVar);
		runsL.setToolTipText(runsVar);
		strategyL.setToolTipText(strategyVar);
		maxPsizeL.setToolTipText(maxPSizeVar);
		maxRewiringSizeL.setToolTipText(maxRewiringSizeVar);
		minTranL.setToolTipText(minTransVar);

		panelAvatarP1 = new JPanel(new GridLayout(5, 2));
		panelAvatarP1.setBorder(BorderFactory.createTitledBorder(EnumAlgorithm.AVATAR + " parameters"));

		panelAvatarP1.add(runsL);
		panelAvatarP1.add(runsB);
		panelAvatarP1.add(maxPsizeL);
		panelAvatarP1.add(maxPsizeB);
		panelAvatarP1.add(maxRewiringSizeL);
		panelAvatarP1.add(maxRewiringSizeB);
		panelAvatarP1.add(strategyL);
		panelAvatarP1.add(strategy);
		// p1a.add(aproxDepthL);
		// p1a.add(aproxDepth);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 2;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelAvatarP1.add(keepTransB, c);
		// JPanel p1d = new JPanel(new GridLayout(1, 1));
		// p1d.add(keepTransB);

		JPanel p1e = new JPanel(new GridLayout(1, 1));
		p1e.add(minTranL);
		p1e.add(minTranB);

		GridBagConstraints g = new GridBagConstraints();
		g.weightx = 1;
		g.gridwidth = 1;
		g.fill = GridBagConstraints.HORIZONTAL;
		add(panelAvatarP1, g);

		panelAvatarP2 = new JPanel();
		panelAvatarP2.setBorder(BorderFactory.createTitledBorder("Expert parameters"));

		minTranL.setToolTipText(minTransVar);
		minCycleL.setToolTipText(minCycleVar);
		tauL.setToolTipText(tauVar);
		depthL.setToolTipText(depthVar);
		panelAvatarP2.setLayout(new GridLayout(4, 2));
		panelAvatarP2.add(minTranL);
		panelAvatarP2.add(minTranB);
		panelAvatarP2.add(minCycleL);
		panelAvatarP2.add(minCycleB);
		panelAvatarP2.add(tauL);
		panelAvatarP2.add(tauB);
		panelAvatarP2.add(depthL);
		panelAvatarP2.add(depthB);
		g.gridy = 1;
		add(panelAvatarP2, g);

	}

	@Override
	public Simulation getSimulation(StatefulLogicalModel model, boolean quiet) throws Exception {
		AvatarSimulation sim = new AvatarSimulation();
		sim.addModel(model);
		sim.isGUI = true;
		sim.runs = (int) Double.parseDouble(runsB.getText());
		sim.tauInit = (int) Double.parseDouble(tauB.getText());
		sim.maxSteps = (int) Double.parseDouble(depthB.getText());
		sim.minCSize = (int) Double.parseDouble(minCycleB.getText());
		sim.maxPSize = (int) Double.parseDouble(maxPsizeB.getText());
		sim.maxRewiringSize = (int) Double.parseDouble(maxRewiringSizeB.getText());
		sim.minTransientSize = (int) Double.parseDouble(minTranB.getText());
		sim.keepTransients = keepTransB.isSelected();
		sim.keepOracle = true;
		sim.quiet = quiet;
		String stg = (String) strategy.getSelectedItem();
		if (stg.contains("xact"))
			sim.strategy = AvatarStrategy.MatrixInversion;
		/*
		 * else if(stg.contains("Approx")){ try { sim.approxDepth =
		 * Integer.parseInt(aproxDepth.getText()); } catch (NumberFormatException nfe) {
		 * sim.approxDepth=-1; } sim.strategy = AvatarStrategy.Approximate; }
		 */
		else
			sim.strategy = AvatarStrategy.RandomExit;
		sim.smallStateSpace = (int) Math.pow(2, 10);
		return sim;
	}

	@Override
	public void load(AvatarParameters p) {
		p.avaRuns = runsB.getText();
		p.avaTau = tauB.getText();
		p.avaDepth = depthB.getText();
		p.avaAproxDepth = aproxDepth.getText();
		p.avaMinTran = minTranB.getText();
		p.avaMinCycle = minCycleB.getText();
		p.avaMaxPSize = maxPsizeB.getText();
		p.avaMaxRewiringSize = maxRewiringSizeB.getText();
		p.avaKeepTrans = keepTransB.isSelected();
		p.avaStrategy = strategy.getSelectedIndex();
	}

	@Override
	public void unload(AvatarParameters param) {
		runsB.setText(param.avaRuns);
		tauB.setText(param.avaTau);
		depthB.setText(param.avaDepth);
		aproxDepth.setText(param.avaAproxDepth);
		minTranB.setText(param.avaMinTran);
		minCycleB.setText(param.avaMinCycle);
		maxPsizeB.setText(param.avaMaxPSize);
		maxRewiringSizeB.setText(param.avaMaxRewiringSize);
		keepTransB.setSelected(param.avaKeepTrans);
		strategy.setSelectedIndex(param.avaStrategy);
	}
}
