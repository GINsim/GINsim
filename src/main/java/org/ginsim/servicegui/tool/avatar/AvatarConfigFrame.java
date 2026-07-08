package org.ginsim.servicegui.tool.avatar;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.text.DefaultCaret;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.StatefulLogicalModel;
import org.colomoto.biolqm.StatefulLogicalModelImpl;
import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedState;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateList;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesHandler;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesManager;
import org.ginsim.core.graph.regulatorygraph.perturbation.ListOfPerturbations;
import org.ginsim.core.graph.regulatorygraph.perturbation.PerturbationManager;
import org.ginsim.gui.graph.regulatorygraph.initialstate.CompleteStatePanel;
import org.ginsim.service.tool.avatar.params.AvatarParameterList;
import org.ginsim.service.tool.avatar.params.AvatarParameters;
import org.ginsim.service.tool.avatar.params.AvatarParametersManager;
import org.ginsim.service.tool.avatar.params.AvatarStateStore;
import org.ginsim.service.tool.avatar.service.EnumAlgorithm;
import org.ginsim.service.tool.avatar.simulation.Simulation;
import org.ginsim.service.tool.modelreduction.ListOfReductionConfigs;
import org.ginsim.service.tool.modelreduction.ReductionConfigManager;
import org.ginsim.servicegui.tool.avatar.algopanels.AvatarPanel;
import org.ginsim.servicegui.tool.avatar.algopanels.FirefrontPanel;
import org.ginsim.servicegui.tool.avatar.algopanels.MonteCarloPanel;
import org.ginsim.servicegui.tool.avatar.algopanels.SimulationPanel;
import org.ginsim.servicegui.tool.avatar.others.TitleToolTipPanel;
import org.ginsim.servicegui.tool.avatar.parameters.AvaParameterEditionPanel;
import org.ginsim.servicegui.tool.avatar.parameters.AvatarParametersHelper;

/**
 * Main panel for displaying the all of the context associated with avatar
 * simulations
 * 
 * @author Pedro T. Monteiro
 * @author Rui Henriques
 * @version 1.0
 */
public class AvatarConfigFrame extends AvatarLogicalModelActionDialog {
	private static final long serialVersionUID = -8243962416980316054L;
	private static final int W = 950, H = 550;
	private static final String ID = "avatar_gui";

	/****************/
	/** PARAMETERS **/
	/****************/

	/** pointer to the panel with the parameters for an avatar simulation */
	public SimulationPanel panelAvatar;
	/** pointer to the panel with the parameters for a firefront simulation */
	public SimulationPanel panelFF;
	/** pointer to the panel with the parameters for a Monte Carlo simulation */
	public SimulationPanel panelMC;
	/** pointer to the panel with the initial states and oracles */
	public CompleteStatePanel states;
	/** selected simulation: Avatar, Firefront or Monte Carlo */
	public JComboBox<EnumAlgorithm> jcbAlgorithm = new JComboBox<EnumAlgorithm>(new DefaultComboBoxModel<EnumAlgorithm>(
			new EnumAlgorithm[] { EnumAlgorithm.AVATAR, EnumAlgorithm.FIREFRONT, EnumAlgorithm.MONTE_CARLO }));
	/**
	 * whether detailed logs should be printed (not advisable for complex models)
	 */
	public JCheckBox quiet = new JCheckBox("Quiet mode");
	/** named store with the initial states and oracles */
	public AvatarStateStore statestore;

	private JPanel outputPanel;
	private String filename;
	private JSplitPane horizontalPanel;
	/**
	 * JTextArea jtaOutput
	 */
	public JTextArea jtaOutput = new JTextArea();
	private JButton forceStop = new JButton("Pause");
	private AvatarResults results;
	private File memorizedFile = new File("chart.png"), logFile = new File("log.txt"),
			resFile = new File("result.html"), csvFile = new File("result.csv");
	private AvaParameterEditionPanel editionPanel;

	private String open = "<html><div style=\"width:265px;\">", end = "</div></html>";
	private String algoVar = open
			+ "AVATAR - an adapted Monte Carlo simulation, for attractor identification and approximation of reachability probabilities"
			+ "<br>FIREFRONT - for a quasi-exact reachability probabilities of stable states and small complex attractors"
			+ "<br>MONTECARLO - for an approximation of reachability probabilities of stable states" + end;

	/**
	 * Creates the panel with avatar-based simulations from the current graph
	 * 
	 * @param graph   regulatory graph (with possibly contextual information) on
	 *                which
	 *                to apply the services
	 * @param _parent pointer to the parent panel (to return upon closing the panel
	 *                for
	 *                the analysis of attractors)
	 */
	public AvatarConfigFrame(RegulatoryGraph graph, JFrame _parent) {
		super(graph, _parent, ID, W, H);
		filename = graph.getGraphName();
		this.setTitle(Txt.t("STR_avatar"));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// this.setSize(900, 600);

		ToolTipManager.sharedInstance().setInitialDelay(0);
		ToolTipManager.sharedInstance().setDismissDelay(40000);
		this.jtaOutput.setEditable(false);

		/** A: initial states panel **/
		List<byte[]> istates = lrg.isStateful() ? lrg.getStates() : new ArrayList<byte[]>();
		// for(byte[] istate : istates)
		// System.out.println("IState="+AvatarUtils.toString(istate));

		ListOfPerturbations pertHandler = (ListOfPerturbations) ObjectAssociationManager.getInstance().getObject(lrg,
				PerturbationManager.KEY, true);

		this.perturbations = (ListOfPerturbations) ObjectAssociationManager.getInstance().getObject(lrg,
				PerturbationManager.KEY, true);
		this.reductions = (ListOfReductionConfigs) ObjectAssociationManager.getInstance().getObject(lrg,
				ReductionConfigManager.KEY, true);

		// System.out.println("initial refresh");
		perturbationPanel.refresh();
		reductionPanel.refresh();

		NamedStatesHandler nstatesHandler = (NamedStatesHandler) ObjectAssociationManager.getInstance().getObject(lrg,
				NamedStatesManager.KEY, true);
		if (nstatesHandler.getInitialStates().size() == 0)
			statestore = new AvatarStateStore(istates, lrg);
		else
			statestore = new AvatarStateStore(lrg);

		statestore.addStateList(nstatesHandler.getInitialStates(), nstatesHandler.getInputConfigs(),
				new NamedStateList(graph.getNodeOrder(), false));
		states = new CompleteStatePanel(statestore.nstates, statestore.instates, statestore.oracles, true);
		states.setParam(statestore);

		int i = 0;
		List<List<byte[]>> ioracles = lrg.hasOracles() ? lrg.getOracles() : new ArrayList<List<byte[]>>();
		Map<String, List<byte[]>> oracles = new HashMap<String, List<byte[]>>();
		for (List<byte[]> o : ioracles) {
			// System.out.println("IOracle=" + AvatarUtils.toString(o));
			oracles.put("Att_" + (i++), o);
		}
		statestore.addOracle(oracles);
		states.updateParam(statestore);

		panelAvatar = new AvatarPanel();
		panelFF = new FirefrontPanel();
		panelMC = new MonteCarloPanel();
		quiet.setSelected(true);
		quiet.setToolTipText(
				"Check this box to enable logs production (may lead to significant computational time overhead)");
		jcbAlgorithm.setSelectedIndex(0);
		jcbAlgorithm.setToolTipText(algoVar);

		AvatarParameterList paramList = (AvatarParameterList) ObjectAssociationManager.getInstance().getObject(lrg,
				AvatarParametersManager.KEY, false);
		AvatarParameters param;
		if (paramList == null || paramList.size() == 0) {
			param = AvatarParametersHelper.load(this);
			paramList = new AvatarParameterList(graph, param);
		} else {
			param = paramList.get(0);
			statestore.addOracle(param.statestore.oracles);
			param.statestore = statestore;
		}
		editionPanel = new AvaParameterEditionPanel(this, lrg, paramList);

		refresh(param);

		/** H: CLOSING **/
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				ObjectAssociationManager.getInstance().addObject(lrg, AvatarParametersManager.KEY,
						editionPanel.paramList);
				copyStates();
				dispose();
				// parent.dispose();
			}
		});
	}

	private void copyStates() {
		NamedStatesHandler imanager = (NamedStatesHandler) ObjectAssociationManager.getInstance().getObject(lrg,
				NamedStatesManager.KEY, true);
		NamedStateList nstates = new NamedStateList(statestore.nodes, false);// imanager.getInitialStates();
		NamedStateList instates = new NamedStateList(statestore.inodes, true);// imanager.getInputConfigs();
		for (AvatarParameters p : editionPanel.paramList) {
			AvatarStateStore store = p.statestore;
			for (NamedState s : store.nstates)
				if (nstates.getByName(s.getName()) == null && s.getMap().size() > 0)
					nstates.add(s);
			for (NamedState s : store.instates)
				if (instates.getByName(s.getName()) == null && s.getMap().size() > 0)
					instates.add(s);
		}
		imanager.setNormalStates(nstates);
		imanager.setInputStates(instates);
		ObjectAssociationManager.getInstance().addObject(lrg, NamedStatesManager.KEY, imanager);
	}

	/**
	 * Updates the parameters of the simulation panel given the new context
	 * 
	 * @param param the simulation context (parameters) to use to update the fields
	 *              of the main panel
	 */
	public void refresh(AvatarParameters param) {
		mainPanel.removeAll();

		/** LOAD PARAMS **/
		AvatarParametersHelper.unload(param, this);

		/** A: perturbations and reductions **/
		JPanel topPanel = new TitleToolTipPanel();
		topPanel.setLayout(new GridBagLayout());

		topPanel.setBorder(BorderFactory.createTitledBorder("Model modifications"));
		// topPanel.setToolTipText("Applicable perturbations and reductions");
		ToolTipManager.sharedInstance().setInitialDelay(0);
		GridBagConstraints c = new GridBagConstraints();

		// perturbation panel
		c.weightx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		topPanel.add(perturbationPanel, c);

		// reduction panel
		c.gridy = 1;
		c.weightx = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		topPanel.add(reductionPanel, c);

		// simplification checkboxes
		c.gridx = 1;
		c.weightx = 0;
		// topPanel.add(cb_simplify, c);

		/** B: avatar-specific stuff **/
		JPanel rightPanel = new JPanel();
		rightPanel.removeAll();
		rightPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.weightx = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 2;
		gbc.fill = GridBagConstraints.BOTH;
		rightPanel.add(topPanel, gbc);

		/*
		 * if(statestore.getInputState().size()==0){ this.setResizable(true);
		 * //this.setSize(getWidth(),getHeight()-70); heightStates=heightStates-70; }
		 */
		// statesPanel.setBorder(BorderFactory.createTitledBorder("States and
		// oracles"));
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 2;
		gbc.fill = GridBagConstraints.BOTH;
		// states.setPreferredSize(new Dimension(500,200));
		// states.scrollRectToVisible(states.getBounds());
		// states.setMinimumSize(new Dimension(widthstates - 10, heightStates - 15));
		// System.out.println("Size:"+param.states.getAllStateList().size()+"<->"+param.states.getAllIStateList().size());
		rightPanel.add(states, gbc);
		gbc.gridheight = 1;

		/** C: select algorithm **/
		jcbAlgorithm.setSelectedIndex(param.algorithm);
		jcbAlgorithm.setVisible(true);
		quiet.setSelected(param.quiet);
		JPanel panelOutput = new JPanel();
		panelOutput.setLayout(new GridLayout(1, 1));
		panelOutput.setBorder(BorderFactory.createTitledBorder("Output"));
		panelOutput.add(quiet);

		JPanel panelSelAlgo = new JPanel();
		panelSelAlgo.setBorder(BorderFactory.createTitledBorder("Algorithm"));
		panelSelAlgo.setLayout(new GridLayout(1, 1));
		panelSelAlgo.add(jcbAlgorithm);

		JPanel panelAlgo = new JPanel();
		panelAlgo.setLayout(new GridLayout(2, 1));
		panelAlgo.setBorder(BorderFactory.createTitledBorder("Simulation"));
		panelAlgo.add(panelSelAlgo);
		panelAlgo.add(panelOutput);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		rightPanel.add(panelAlgo, gbc);

		jcbAlgorithm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EnumAlgorithm algo = (EnumAlgorithm) jcbAlgorithm.getSelectedItem();
				panelAvatar.setVisible(false);
				panelFF.setVisible(false);
				panelMC.setVisible(false);
				if (algo == EnumAlgorithm.FIREFRONT) {
					panelFF.setVisible(true);
				} else if (algo == EnumAlgorithm.MONTE_CARLO) {
					panelMC.setVisible(true);
				} else if (algo == EnumAlgorithm.AVATAR) {
					panelAvatar.setVisible(true);
				}
			}
		});

		/** D: Output **/
		/*
		 * panelOutput.add(plots); rightPanel.add(panelOutput);
		 */

		/** D: Side Panels **/
		// gbc = new GridBagConstraints();
		gbc.gridy = 2;
		gbc.gridx = 1;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		rightPanel.add(panelAvatar, gbc);
		rightPanel.add(panelFF, gbc);
		rightPanel.add(panelMC, gbc);
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.NORTH;
		/*
		 * JButton dynamicUpdate = new JButton("Data-driven Params");
		 * dynamicUpdate.setMinimumSize(new Dimension(160,20));
		 * rightPanel.add(dynamicUpdate,gbc); dynamicUpdate.addActionListener(new
		 * ParamActionListener(param,this,lrg));
		 */

		panelAlgo.setVisible(true);
		panelOutput.setVisible(true);
		panelAvatar.setVisible(false);
		panelFF.setVisible(false);
		panelMC.setVisible(false);
		if (jcbAlgorithm.getSelectedItem() == EnumAlgorithm.AVATAR) {
			panelAvatar.setVisible(true);
		} else if (jcbAlgorithm.getSelectedItem() == EnumAlgorithm.FIREFRONT) {
			panelFF.setVisible(true);
		} else if (jcbAlgorithm.getSelectedItem() == EnumAlgorithm.MONTE_CARLO) {
			panelMC.setVisible(true);
		}

		/** PARAM PANEL **/
		JSplitPane paramPanel = editionPanel.getEditionPanel();
		paramPanel.setRightComponent(rightPanel);
		// paramPanel.setContinuousLayout(true);
		paramPanel.setEnabled(true);
		paramPanel.setIgnoreRepaint(false);
		paramPanel.setOneTouchExpandable(true);
		paramPanel.setDividerLocation(150);
		// paramPanel.setMinimumSize(new Dimension(300,100));
		paramPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		// paramPanel.setBorder(null);

		/** G: Progress bar **/
		// jtpOutput.setContentType("text/html");
		// jtpOutput.setFont(new Font(Font.MONOSPACED, 3, 5));
		jtaOutput.setWrapStyleWord(true);
		((DefaultCaret) jtaOutput.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane jsp = new JScrollPane(jtaOutput);
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		// jsp.setMaximumSize(new Dimension(getWidth(), 50));
		outputPanel = new JPanel();
		outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));
		outputPanel.setLayout(new GridBagLayout());
		gbc.gridy = 3;
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		outputPanel.add(jsp, gbc);
		gbc.gridy = 3;
		gbc.gridx = 3;
		gbc.weightx = 0;
		gbc.gridwidth = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		forceStop.setEnabled(false);
		outputPanel.add(forceStop, gbc);

		forceStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (results != null)
					results.togglePause();
			}
		});
		horizontalPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, paramPanel, outputPanel);
		horizontalPanel.setEnabled(true);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		mainPanel.add(horizontalPanel, gbc);

		horizontalPanel.setDividerLocation(getHeight() - 200);

		mainPanel.repaint();
		mainPanel.validate();
	}

	@Override
	public void run(LogicalModel _model) {

		brun.setEnabled(false);
		forceStop.setEnabled(true);
		forceStop.setText("Pause");
		jtaOutput.append("Initializing simulation\n");
		outputPanel.setVisible(true);
		horizontalPanel.getRightComponent().setVisible(true);
		horizontalPanel.setDividerLocation(0.8);

		/** ARGUMENTS **/
		StatefulLogicalModel model = null;
		try {

			/** A: extract selected states */
			List<byte[]> initialStates = new ArrayList<byte[]>();
			int nstates = _model.getComponents().size();
			if (states.getStateList().size() == 0) {
				for (NamedState state2 : states.getIStateList()) {
					byte[] state = new byte[nstates];
					for (int i = 0; i < nstates; i++) {
						NodeInfo node = _model.getComponents().get(i);
						List<Integer> values = node.isInput() ? state2.getMap().get(node) : null;
						if (values == null || values.size() > 1)
							state[i] = -1;
						else
							state[i] = (byte) ((int) values.get(0));
					}
					// System.out.println("Final istate="+AvatarUtils.toString(state));
					initialStates.add(state);
				}
			} else {
				for (NamedState state1 : states.getStateList()) {
					if (states.getIStateList().size() == 0) {
						byte[] state = new byte[nstates];
						for (int i = 0; i < nstates; i++) {
							NodeInfo node = _model.getComponents().get(i);
							List<Integer> values = state1.getMap().get(node);
							if (values == null || values.size() > 1)
								state[i] = -1;
							else
								state[i] = (byte) ((int) values.get(0));
						}
						// System.out.println("Final istate="+AvatarUtils.toString(state));
						initialStates.add(state);
					}
					for (NamedState state2 : states.getIStateList()) {
						byte[] state = new byte[nstates];
						for (int i = 0; i < nstates; i++) {
							NodeInfo node = _model.getComponents().get(i);
							List<Integer> values = node.isInput() ? state2.getMap().get(node)
									: state1.getMap().get(node);
							if (values == null || values.size() > 1)
								state[i] = -1;
							else
								state[i] = (byte) ((int) values.get(0));
						}
						// System.out.println("Final istate="+AvatarUtils.toString(state));
						initialStates.add(state);
					}
				}
			}
			if (initialStates.size() == 0) {
				byte[] state = new byte[nstates];
				for (int i = 0; i < nstates; i++)
					state[i] = -1;
				initialStates.add(state);
			}
			model = new StatefulLogicalModelImpl(_model, initialStates, filename);

			/** B: extract selected oracles */
			List<List<byte[]>> oracles = new ArrayList<List<byte[]>>();
			String name = "";
			for (NamedState ostate : states.getOracles()) {
				if (!ostate.getName().equals(name)) {
					oracles.add(new ArrayList<byte[]>());
					name = ostate.getName();
				}
				byte[] state = new byte[nstates];
				for (int i = 0; i < nstates; i++) {
					NodeInfo node = _model.getComponents().get(i);
					List<Integer> values = ostate.getMap().get(node);
					if (values == null || values.size() > 1)
						state[i] = -1;
					else
						state[i] = (byte) ((int) values.get(0));
				}
				oracles.get(oracles.size() - 1).add(state);
			}

		} catch (Exception e) {
			jtaOutput.setEnabled(false);
			brun.setEnabled(true);
			String fileErrorMessage = "Unfortunately we were not able to finish your request.<br><em>Reason:</em> Exception while reading the input states and parsing the model.";
			AvatarResults.errorDisplay(fileErrorMessage, e);
			e.printStackTrace();
			return;
		}
		Simulation sim = null;
		try {
			/** B: extract algo-specific parameters and run */
			EnumAlgorithm algo = (EnumAlgorithm) jcbAlgorithm.getSelectedItem();
			if (algo == EnumAlgorithm.FIREFRONT)
				sim = ((FirefrontPanel) panelFF).getSimulation(model, quiet.isSelected());
			else if (algo == EnumAlgorithm.AVATAR)
				sim = ((AvatarPanel) panelAvatar).getSimulation(model, quiet.isSelected());
			else if (algo == EnumAlgorithm.MONTE_CARLO)
				sim = ((MonteCarloPanel) panelMC).getSimulation(model, quiet.isSelected());
		} catch (Exception e) {
			jtaOutput.setEnabled(false);
			brun.setEnabled(true);
			String fileErrorMessage = "Unfortunately we were not able to finish your request.<br><em>Reason:</em> Exception while parameterizing the algorithm!";
			AvatarResults.errorDisplay(fileErrorMessage, e);
			e.printStackTrace();
			return;
		}
		results = new AvatarResults(sim, jtaOutput, this, quiet.isSelected(), model, memorizedFile, logFile, resFile,
				csvFile, brun, forceStop);
		results.runAvatarResults();
	}

	@Override
	public void doClose() {
		if (results != null)
			results.kill(false);
		// System.out.println(AvatarUtils.toString(editionPanel.paramList.get(0).statesSelected));
		editionPanel.selectionUpdated(new int[] {});
		// System.out.println(AvatarUtils.toString(editionPanel.paramList.get(0).statesSelected));
		ObjectAssociationManager.getInstance().addObject(lrg, AvatarParametersManager.KEY, editionPanel.paramList);
		copyStates();
		dispose();
		// parent.dispose();
	}

	/**
	 * St the current parameter
	 * 
	 * @param p avatar parameter
	 */
	public void setCurrent(AvatarParameters p) {
		editionPanel.setCurrent(p);
	}
}