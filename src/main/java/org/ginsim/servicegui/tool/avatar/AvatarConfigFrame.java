package org.ginsim.servicegui.tool.avatar;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.NodeInfo;
import org.colomoto.logicalmodel.StatefulLogicalModel;
import org.colomoto.logicalmodel.StatefulLogicalModelImpl;
import org.colomoto.logicalmodel.io.avatar.AvatarImport;
import org.colomoto.logicalmodel.io.avatar.AvatarUtils;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.GraphManager;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.LogicalModel2RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedState;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesHandler;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.regulatorygraph.initialstate.CompleteStatePanel;
import org.ginsim.gui.graph.regulatorygraph.initialstate.InitialStatePanel;
import org.ginsim.service.tool.avatar.params.AvatarParameterList;
import org.ginsim.service.tool.avatar.params.AvatarParameters;
import org.ginsim.service.tool.avatar.params.AvatarParametersManager;
import org.ginsim.service.tool.avatar.params.AvatarStateStore;
import org.ginsim.service.tool.avatar.simulation.Simulation;
import org.ginsim.servicegui.tool.avatar.algopanels.AvatarPanel;
import org.ginsim.servicegui.tool.avatar.algopanels.FirefrontPanel;
import org.ginsim.servicegui.tool.avatar.algopanels.MonteCarloPanel;
import org.ginsim.servicegui.tool.avatar.algopanels.SimulationPanel;
import org.ginsim.servicegui.tool.avatar.others.MyTitledBorder;
import org.ginsim.servicegui.tool.avatar.others.TitleToolTipPanel;
import org.ginsim.servicegui.tool.avatar.parameters.AvaParameterEditionPanel;
import org.ginsim.servicegui.tool.avatar.parameters.AvatarParametersHelper;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Main panel for displaying the all of the context associated with avatar simulations
 * 
 * @author Pedro Monteiro, Rui Henriques
 * @version 1.0
 */
public class AvatarConfigFrame extends AvatarLogicalModelActionDialog {

	private static final int W = 1020, H = 600;
    private static final String ID = "avatar_gui";
	private static final long serialVersionUID = 1L;
	private Color purple = new Color(204,153,255), blue = new Color(130,180,246), marine = new Color(204,204,255);

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
	public JComboBox algorithm = new JComboBox(new DefaultComboBoxModel(new String[] {"Avatar","FireFront","MonteCarlo"}));
	/** whether charts should be created and plotted */
	public JCheckBox plots = new JCheckBox("Plot statistics");
	/** whether detailed logs should be printed (not advisable for complex models) */
	public JCheckBox quiet = new JCheckBox("Quiet mode");
	/** named store with the initial states and oracles */
	public AvatarStateStore statestore;
	
	private JPanel progressBar;
	public JLabel progress = new JLabel("");
	private JButton forceStop = new JButton("Force exit");
	private AvatarResults results;
	private File memorizedFile = new File("chart.png"), logFile = new File("log.txt");
	private AvaParameterEditionPanel editionPanel;

	private String open = "<html><div style=\"width:265px;\">", end = "</div></html>";
	private String statesVar =open+"Specification of available initial states in each run. The specification allows for only a subset of components to be fixed (\"-1\" means undefined component)"+end;
	private String outputVar =open+"Select whether logs are outputted. Note that unselecting the 'quiet' box may lead to significant computational overhead in terms of time. For this reason, we only advise going out of 'quiet' mode for debug purposes under a low number of simulations/runs."+end;
	private String algoVar =open+"Select the algorithm: 1) avatar should be preffered to find both point and complex attractors, 2) firefront should be preferred for a quasi-exhaustive characterization of point attractors, 3) MonteCarlo should be preferred for a baseline."+end;
	
    /**
     * Creates the panel with avatar-based simulations from the current graph
     * @param graph regulatory graph (with possibly contextual information) on which to apply the services
     * @param _parent pointer to the parent panel (to return upon closing the panel for the analysis of attractors)
     */
    public AvatarConfigFrame(RegulatoryGraph graph, JFrame _parent) {
        super(graph, true /*flexible layout*/, _parent, ID, W, H);
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
		ToolTipManager.sharedInstance().setInitialDelay(0);

        /** A: initial states panel **/		
        List<byte[]> istates = lrg.isStateful() ? lrg.getStates() : new ArrayList<byte[]>();
        statestore = new AvatarStateStore(istates,lrg);
        for(byte[] istate : istates) System.out.println("IState="+AvatarUtils.toString(istate));
                
        NamedStatesHandler nstatesHandler = (NamedStatesHandler) ObjectAssociationManager.getInstance().getObject(lrg, NamedStatesManager.KEY, true);
        statestore.addStateList(nstatesHandler.getInitialStates(), nstatesHandler.getInputConfigs()); 
    	states = new CompleteStatePanel(statestore.nstates,statestore.instates,true);
        states.setParam(statestore);
        
        int i=0;
        List<List<byte[]>> ioracles = lrg.hasOracles() ? lrg.getOracles() : new ArrayList<List<byte[]>>();
        Map<String,List<byte[]>> oracles = new HashMap<String,List<byte[]>>();
        for(List<byte[]> o : ioracles){
            System.out.println("IOracle="+AvatarUtils.toString(o));
        	oracles.put("oracle_"+(i++), o);
        }
        statestore.addOracle(oracles);
        states.updateParam(statestore);

		Icon img = new ImageIcon(getClass().getResource("/greyQuestionMark.png"));
        panelAvatar = new AvatarPanel(img,flexible);
        panelFF = new FirefrontPanel(img,flexible);
		panelFF.setBorder(new TitledBorder(new LineBorder(purple,2), "FireFront Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panelMC = new MonteCarloPanel(img,flexible);
		panelMC.setBorder(new TitledBorder(new LineBorder(purple,2), "MonteCarlo Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        plots.setSelected(true);
        quiet.setSelected(true);
        algorithm.setSelectedIndex(0);

		AvatarParameterList paramList = (AvatarParameterList) ObjectAssociationManager.getInstance().getObject(lrg, AvatarParametersManager.KEY, false);
		AvatarParameters param;
		if(paramList == null){
	        param = AvatarParametersHelper.load(this);
	        paramList = new AvatarParameterList(graph,param);
			System.out.println("creating default parameters");
		} else param = paramList.get(0);
		editionPanel = new AvaParameterEditionPanel(this,lrg,paramList);
		
        refresh(param);
        
		/** H: CLOSING **/
		this.addWindowListener(new java.awt.event.WindowAdapter() { 
			public void windowClosing(java.awt.event.WindowEvent e) {
				ObjectAssociationManager.getInstance().addObject(lrg, AvatarParametersManager.KEY, editionPanel.paramList);
				dispose();
				//parent.dispose();
			}
		});
    }
    
    /**
     * Updates the parameters of the simulation panel given the new context
     * @param param the simulation context (parameters) to use to update the fields of the main panel
     */
    public void refresh(AvatarParameters param){
        mainPanel.removeAll();

    	/** LOAD PARAMS **/
        //System.out.println("AA:"+AvatarUtils.toString(states.getDisabledEdition(false)));
        AvatarParametersHelper.unload(param,this);
        //System.out.println("BB:"+AvatarUtils.toString(states.getDisabledEdition(false)));
        
        JPanel rightPanel = new JPanel(new GridBagLayout());
        JPanel topPanel = getTopPanel();
        if(!flexible){
        	rightPanel.setLayout(null);
	        topPanel.setBounds(5,5,430,yAdjustment);
	        rightPanel.add(topPanel);
    	} else {
    		rightPanel.removeAll();
    		rightPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.weightx = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 2;
            gbc.fill = GridBagConstraints.BOTH;
            rightPanel.add(topPanel, gbc);
	    }
    	    	
	    GridBagConstraints gbc = new GridBagConstraints();
		Icon img = new ImageIcon(getClass().getResource("/greyQuestionMark.png"));
        int width=200, widthstates=430, startX=5, startY=10, heightStates=300, shiftX=5;

        /*if(statestore.getInputState().size()==0){
        	this.setResizable(true);
    		//this.setSize(getWidth(),getHeight()-70);
            heightStates=heightStates-70;
        }*/
        JPanel statesPanel = null;
        if(flexible){
        	statesPanel = new TitleToolTipPanel();
        	JLabel title = new JLabel("      State sampling");
        	title.setIcon(img);
        	statesPanel.setToolTipText(statesVar);
        	statesPanel.setBorder(new MyTitledBorder(new LineBorder(blue,2),title, TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
            gbc.gridx = 2;
            gbc.gridy = 2;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 2;
            gbc.fill = GridBagConstraints.BOTH;
        	statesPanel.setLayout(new GridLayout(1,1));
            //states.setPreferredSize(new Dimension(500,200));
            //states.scrollRectToVisible(states.getBounds());
        	states.setMinimumSize(new Dimension(widthstates-10, heightStates-15));
    		//System.out.println("Size:"+param.states.getAllStateList().size()+"<->"+param.states.getAllIStateList().size());
    		statesPanel.add(states);
    		rightPanel.add(statesPanel,gbc);
            gbc.gridheight = 1;
       } else {
        	statesPanel.setLayout(null);
        	statesPanel.setBorder(new TitledBorder(new LineBorder(blue,2),"State sampling     ", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        	statesPanel.setBounds(startX, startY, widthstates, heightStates);
        	states.setBounds(startX, startY+7, widthstates-10, heightStates-22);
    		statesPanel.add(states);
    		rightPanel.add(statesPanel);
    		JLabel statesQuestion = new JLabel("");
    		statesQuestion.setBounds(100,9,15,15);
    		statesQuestion.setIcon(img);
    		statesQuestion.setToolTipText(statesVar);
    		rightPanel.add(statesQuestion);
        }
        
        /** A: select algorithm **/
		JPanel panelAlgo = new JPanel();
		algorithm.setSelectedIndex(param.algorithm);
		algorithm.setVisible(true);		
		quiet.setSelected(param.quiet);
		plots.setSelected(param.plots);
		JPanel panelOutput = null;
		if(flexible){
			panelOutput = new TitleToolTipPanel();
			panelOutput.setLayout(new GridLayout(1,1));
        	panelOutput.setToolTipText(outputVar);
        	JLabel title = new JLabel("      Output");
        	title.setIcon(img);
        	panelOutput.setBorder(new MyTitledBorder(new LineBorder(purple,1),title, TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
    		panelOutput.add(quiet);
		} else {
			panelOutput = new JPanel(null);
			panelOutput.setBorder(new TitledBorder(new LineBorder(purple,2),"Output     ", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
			panelOutput.add(quiet);
			JLabel outputQuestion = new JLabel("");
			outputQuestion.setIcon(img);
			outputQuestion.setToolTipText(outputVar);
			panelOutput.setBounds(startX, startY+65, width, 60);
			outputQuestion.setBounds(49,0,15,15);
			panelOutput.add(outputQuestion);
			quiet.setBounds(shiftX, 18, 107, 20);
			plots.setBounds(shiftX, 35, 107, 20);
		}

		if(flexible){
			panelAlgo = new TitleToolTipPanel();
        	panelAlgo.setLayout(new GridLayout(2,1));
        	panelAlgo.setBorder(new TitledBorder(new LineBorder(purple,2),"Simulation", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        	
			JPanel panelSelAlgo = new TitleToolTipPanel();
			panelSelAlgo.setMinimumSize(new Dimension(0,50));
        	JLabel title = new JLabel("      Algorithm");
        	title.setIcon(img);
        	panelSelAlgo.setToolTipText(algoVar);
        	panelSelAlgo.setBorder(new MyTitledBorder(new LineBorder(purple,1),title, TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        	panelSelAlgo.setLayout(new GridLayout(1,1));
			panelSelAlgo.add(algorithm);
			
			panelAlgo.add(panelSelAlgo);
			panelAlgo.add(panelOutput);
        	//algorithm.setPreferredSize(new Dimension(widthstates-10, heightStates-22));
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.BOTH;
            rightPanel.add(panelAlgo,gbc);
		} else {
			panelAlgo = new JPanel(null);
			panelAlgo.setBorder(new TitledBorder(new LineBorder(purple,2),"Algorithm     ", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
			startY=-125;
			startX+=widthstates+5;
			panelAlgo.setBounds(startX, startY, width, 60);
			algorithm.setBounds(shiftX+5, 25, width-25, 22);
			JLabel algoQuestion = new JLabel("");
			algoQuestion.setIcon(img);
			algoQuestion.setToolTipText(algoVar);
			algoQuestion.setBounds(66,0,15,15);
		    panelAlgo.add(algorithm);
			panelAlgo.add(algoQuestion);
	        rightPanel.add(panelAlgo);
		}
        
		algorithm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
		    	String algo = (String) algorithm.getSelectedItem();
				panelAvatar.setVisible(false);
				panelFF.setVisible(false);
				panelMC.setVisible(false);
		    	if(algo.equals("FireFront")) panelFF.setVisible(true);
		    	else if(algo.equals("MonteCarlo")) panelMC.setVisible(true);
		    	else panelAvatar.setVisible(true);
			}
		});

        /** B: Output **/
		/*panelOutput.add(plots);
        if(flexible){
            gbc.gridy = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.BOTH;
        	rightPanel.add(panelOutput,gbc);
        } else rightPanel.add(panelOutput);*/

        /** D: Side Panels **/
        if(flexible){
        	//gbc = new GridBagConstraints();
            gbc.gridy = 2;
            gbc.gridx = 1;
            gbc.weightx = 0;
            gbc.weighty = 1;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
	        rightPanel.add(panelAvatar,gbc);
			rightPanel.add(panelFF,gbc);
			rightPanel.add(panelMC,gbc);
            gbc.gridy = 3;
            gbc.fill = GridBagConstraints.NORTH;
            /*JButton dynamicUpdate = new JButton("Data-driven Params");
            dynamicUpdate.setMinimumSize(new Dimension(160,20));
			rightPanel.add(dynamicUpdate,gbc);
			dynamicUpdate.addActionListener(new ParamActionListener(param,this,lrg));*/
        } else {
	        panelAvatar.setBounds(startX+207, startY, 241, 230);
			panelFF.setBounds(startX+210, startY, width-40, 120);
			panelMC.setBounds(startX+210, startY, width-40, 66);
	        rightPanel.add(panelAvatar);
			rightPanel.add(panelFF);
			rightPanel.add(panelMC);
        }
        panelAlgo.setVisible(true);
        panelOutput.setVisible(true);
        panelAvatar.setVisible(false);		
        panelFF.setVisible(false);
		panelMC.setVisible(false);
        if(algorithm.getSelectedIndex()==0) panelAvatar.setVisible(true);
        else if(algorithm.getSelectedIndex()==1) panelFF.setVisible(true);
        else panelMC.setVisible(true);

        /** PARAM PANEL **/
	    JSplitPane paramPanel = editionPanel.getEditionPanel();
	    paramPanel.setRightComponent(rightPanel);
	    //paramPanel.setContinuousLayout(true);
	    paramPanel.setEnabled(true);
	    paramPanel.setIgnoreRepaint(false);
	    paramPanel.setOneTouchExpandable(true);
	    paramPanel.setDividerLocation(0.3);
	    //paramPanel.setMinimumSize(new Dimension(300,100));
	    paramPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
	    paramPanel.setBorder(null);
	    
		gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.gridwidth = 1;
	    gbc.weightx = 1;
        gbc.weighty = 1;
	    gbc.fill = GridBagConstraints.BOTH;
	    mainPanel.add(paramPanel,gbc);

		/** G: Progress bar **/
		progressBar = new JPanel();
		progressBar.setBorder(new TitledBorder(new LineBorder(marine,4),"Running progress... ", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		if(flexible){
			progressBar.setLayout(new GridBagLayout());
            gbc.gridy = 3;
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.gridwidth = 3;
            gbc.fill = GridBagConstraints.BOTH;
			progressBar.add(progress,gbc);
            gbc.gridy = 3;
            gbc.gridx = 3;
            gbc.weightx = 0;
            gbc.gridwidth = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.EAST;
			progressBar.add(forceStop,gbc);
            gbc.gridy = 1;
            gbc.gridx = 0;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.BOTH;
			mainPanel.add(progressBar,gbc);
		} else {
			progressBar.setLayout(null);
			progressBar.setBounds(startX+13, 140, 418, 99);
			progress.setBounds(10,5,400,90);
			forceStop.setBounds(318,70,90,20);
			progressBar.add(progress);
			progressBar.add(forceStop);
			mainPanel.add(progressBar);
		}
		forceStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(results!=null) results.kill(true);
			}
		});
		progressBar.setMinimumSize(new Dimension(400,180));
		progressBar.setVisible(false);
		
		mainPanel.repaint();
		mainPanel.validate();
    }	

	@Override
	public void run(LogicalModel _model) {
		
	  brun.setEnabled(false);
	  forceStop.setEnabled(true);
	  progress.setText("Initializing simulation");
	  progressBar.setVisible(true);
		
	  /** ARGUMENTS **/
      StatefulLogicalModel model = null;
	  try {
		  
		/** A: extract selected states */
	    List<byte[]> initialStates = new ArrayList<byte[]>();
    	int nstates = _model.getNodeOrder().size();
		for(NamedState state1 : states.getStateList()){
			if(states.getIStateList().size()==0){
		    	byte[] state = new byte[nstates];
		    	for(int i=0; i<nstates; i++){
		    		NodeInfo node = _model.getNodeOrder().get(i);
		    		List<Integer> values = state1.getMap().get(node);
		    		if(values==null || values.size()>1) state[i]=-1;
		    		else state[i]=(byte)((int)values.get(0));
		    	}
		    	//System.out.println("Final istate="+AvatarUtils.toString(state));
	    		initialStates.add(state);
			} 
			for(NamedState state2 : states.getIStateList()){
		    	byte[] state = new byte[nstates];
		    	for(int i=0; i<nstates; i++){
		    		NodeInfo node = _model.getNodeOrder().get(i);
		    		List<Integer> values = node.isInput() ? state2.getMap().get(node) : state1.getMap().get(node);
		    		if(values==null || values.size()>1) state[i]=-1;
		    		else state[i]=(byte)((int)values.get(0));
		    	}
		    	//System.out.println("Final istate="+AvatarUtils.toString(state));
	    		initialStates.add(state);
			}
		}
    	if(initialStates.size()==0){
    		byte[] state = new byte[nstates];
    		for(int i=0; i<nstates; i++) state[i]=-1;
    		initialStates.add(state);
    	}
    	model = new StatefulLogicalModelImpl(_model,initialStates);
    	
		/** B: extracte selected oracles */
	  	List<List<byte[]>> oracle = new ArrayList<List<byte[]>>();
	  	String name = "";
		for(NamedState state1 : states.getOracleStateList(false)){
			if(state1.getName()!=name){
				oracle.add(new ArrayList<byte[]>());
				name = state1.getName();
			}
			boolean visited = false;
			for(NamedState state2 : states.getOracleStateList(true)){
				if(state1.getName()!=name) continue;
				visited = true;
		    	byte[] state = new byte[nstates];
		    	for(int i=0; i<nstates; i++){
		    		NodeInfo node = _model.getNodeOrder().get(i);
		    		List<Integer> values = node.isInput() ? state2.getMap().get(node) : state1.getMap().get(node);
		    		if(values==null || values.size()>1) state[i]=-1;
		    		else state[i]=(byte)((int)values.get(0));
		    	}
		    	System.out.println("Final oracle="+AvatarUtils.toString(state));
	    		oracle.get(oracle.size()-1).add(state);
			}
			if(!visited){
		    	byte[] state = new byte[nstates];
		    	for(int i=0; i<nstates; i++){
		    		NodeInfo node = _model.getNodeOrder().get(i);
		    		List<Integer> values = state1.getMap().get(node);
		    		if(values==null || values.size()>1) state[i]=-1;
		    		else state[i]=(byte)((int)values.get(0));
		    	}
		    	System.out.println("Final oracle="+AvatarUtils.toString(state));
	    		oracle.get(oracle.size()-1).add(state);
			} 
		}
		for(List<byte[]> o : oracle) System.out.println("Oracle entry:"+AvatarUtils.toString(o));
    	((StatefulLogicalModelImpl)model).setOracles(oracle);
    	
	  } catch(Exception e){
		    progress.setEnabled(false);
		  	brun.setEnabled(true);
			String fileErrorMessage = "Unfortunately we were not able to finish your request.<br><em>Reason:</em> Exception while reading the input states and parsing the model.";
			AvatarResults.errorDisplay(fileErrorMessage,e);
			e.printStackTrace();
			return;
	  }
	  Simulation sim = null; 
	  try {
		/** B: extract algo-specific parameters and run */
		String stg = algorithm.getSelectedItem().toString(); 
		if(stg.equals("FireFront")) sim = ((FirefrontPanel)panelFF).getSimulation(model,plots.isSelected(),quiet.isSelected());
		else if(stg.equals("Avatar")) sim = ((AvatarPanel)panelAvatar).getSimulation(model,plots.isSelected(),quiet.isSelected());
		else sim = ((MonteCarloPanel)panelMC).getSimulation(model,false,quiet.isSelected());
	  } catch (Exception e) {
  		    progress.setEnabled(false);
		  	brun.setEnabled(true);
			String fileErrorMessage = "Unfortunately we were not able to finish your request.<br><em>Reason:</em> Exception while parameterizing the algorithm!";
			AvatarResults.errorDisplay(fileErrorMessage,e);
			e.printStackTrace();
			return;
	  } 
	  results = new AvatarResults(sim,flexible,progress,this,quiet.isSelected(),model,memorizedFile,logFile,brun,forceStop);
	  results.runAvatarResults();
    }
		
	@Override
	public void doClose() {
		if(results!=null) results.kill(false);
		ObjectAssociationManager.getInstance().addObject(lrg, AvatarParametersManager.KEY, editionPanel.paramList);
		dispose();
		//parent.dispose();
	}


	/**
	 * Illustrative testing class to launch directly this component with a given model
	 * @param args to be ignored
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					String dir = "C:\\Users\\Rui\\Documents\\00 Avatar\\Avatar Material\\table-models\\";
					Graph<?,?> graph = GraphManager.getInstance().open(dir+"Bladder_Model_Stateful.zginml");
					if(graph instanceof RegulatoryGraph) System.out.println("YES!");

					/*AvatarImport avaImport = new AvatarImport(new File(filename));
					StatefulLogicalModel _model = avaImport.getModel(); //model.fromNuSMV(filename);
					RegulatoryGraph graph = LogicalModel2RegulatoryGraph.importModel(_model);*/
			        GUIManager.getInstance().newFrame(graph,true);
			        //GraphGUI<RegulatoryGraph, RegulatoryNode, RegulatoryMultiEdge> gui = GUIManager.getInstance().getGraphGUI(graph);
			        
					AvatarConfigFrame frame = new AvatarConfigFrame((RegulatoryGraph) graph,new JFrame());
					frame.setVisible(true);
					
					for(Frame f : Frame.getFrames())
						if(f.toString().contains("MainFrame")) f.dispose();
					//avatar.setSelected(true);
					//frame.run(_model);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void setCurrent(AvatarParameters p) {
		editionPanel.setCurrent(p);
	}
}

/*class ParamActionListener implements ActionListener {
	AvatarParameters param;
	AvatarConfigFrame frame;
	RegulatoryGraph graph;
	public ParamActionListener(AvatarParameters p, AvatarConfigFrame f, RegulatoryGraph lrg){
		param=p;
		frame=f;
		graph=lrg;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		param = AvatarParamDynamicUpdate.complete(param,graph);
		frame.refresh(param); 
	}
}*/
