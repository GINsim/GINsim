package org.ginsim.servicegui.export.cadp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JPanel;

import org.colomoto.biolqm.tool.fixpoints.FixpointList;
import org.colomoto.biolqm.tool.fixpoints.FixpointSearcher;
import org.colomoto.biolqm.tool.fixpoints.FixpointTask;
import org.colomoto.mddlib.PathSearcher;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.utils.dialog.stackdialog.AbstractStackDialogHandler;
import org.ginsim.service.export.cadp.CADPExportConfig;
import org.ginsim.service.tool.composition.GenericTopology;
import org.ginsim.service.tool.composition.IntegrationFunction;
import org.ginsim.service.tool.composition.IntegrationFunctionMapping;
import org.ginsim.service.tool.composition.Topology;
import org.ginsim.service.tool.stablestates.StableStatesService;
import org.ginsim.servicegui.tool.composition.AdjacencyMatrixWidget;
import org.ginsim.servicegui.tool.composition.CompositionSpecificationDialog;
import org.ginsim.servicegui.tool.composition.InstanceSelectorWidget;
import org.ginsim.servicegui.tool.composition.IntegrationFunctionWidget;

/**
 * Main dialog for CADP export
 * 
 * @author Nuno D. Mendes
 */

public class CADPExportConfigPanel extends AbstractStackDialogHandler implements
		CompositionSpecificationDialog {

	private static final long serialVersionUID = 7274577689017747224L;

	private final CADPExportConfig config;
	private final CADPExportAction action;

	private InstanceSelectorWidget instanceSelectorPanel = null;
	private AdjacencyMatrixWidget adjacencyMatrixPanel = null;
	private IntegrationFunctionWidget integrationPanel = null;
	private VisibleComponentsWidget visibleComponentsPanel = null;
	private InitialStatesWidget initialStatesPanel = null;

	private int instances = 2;
	private Topology topology = new GenericTopology(this.instances);
	private List<RegulatoryNode> mappedNodes = new ArrayList<RegulatoryNode>();
	private List<byte[]> stableStates = null;
	private List<List<byte[]>> compatibleStableStates = null;

	public CADPExportConfigPanel(CADPExportConfig config,
			CADPExportAction action) {
		this.config = config;
		this.action = action;
	}

	@Override
	public boolean run() {

		config.setTopology(this.topology);
		config.setMapping(integrationPanel.getMapping());
		config.setListVisible(visibleComponentsPanel.getSelectedNodes());
		config.setInitialStates(initialStatesPanel.getInitialStates());
		config.setCompatibleStableStates(getCompatibleStableStates());
		config.setReducedCompatibleStableStates(getReducedCompatibleStableStates());

		action.selectFile();
		return true;
	}

	@Override
	protected void init() {
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		add(getInstanceSelectorPanel(), constraints);

		constraints.gridwidth = 5;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridx = 0;
		constraints.gridy = 2;
		add(getAdjacencyMatrixPanel(), constraints);

		constraints.weightx = 2;
		constraints.gridx = 5;
		constraints.gridy = 2;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		add(getIntegrationPanel(), constraints);

		constraints.gridx = 0;
		constraints.gridy = 7;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		add(getVisibleComponentsPanel(), constraints);

		constraints.gridx = 0;
		constraints.gridy = 9;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridheight = GridBagConstraints.REMAINDER;
		add(getInitialStatesPanel(), constraints);

		setSize(getPreferredSize());

	}

	private JPanel getInstanceSelectorPanel() {
		if (instanceSelectorPanel == null)
			instanceSelectorPanel = new InstanceSelectorWidget(this);
		return instanceSelectorPanel;

	}

	private JPanel getAdjacencyMatrixPanel() {
		if (adjacencyMatrixPanel == null)
			adjacencyMatrixPanel = new AdjacencyMatrixWidget(this);

		return adjacencyMatrixPanel;
	}

	private JPanel getIntegrationPanel() {
		if (integrationPanel == null)
			integrationPanel = new IntegrationFunctionWidget(this);
		return integrationPanel;
	}

	private JPanel getVisibleComponentsPanel() {
		if (visibleComponentsPanel == null)
			visibleComponentsPanel = new VisibleComponentsWidget(this);
		return visibleComponentsPanel;
	}

	private JPanel getInitialStatesPanel() {
		if (initialStatesPanel == null)
			initialStatesPanel = new InitialStatesWidget(this);
		return initialStatesPanel;
	}

	@Override
	public int getNumberInstances() {
		return instances;
	}

	@Override
	public void updateNumberInstances(int instances) {
		this.instances = instances;
		this.topology = new GenericTopology(instances);
		adjacencyMatrixPanel = adjacencyMatrixPanel.reBuild();
		initialStatesPanel = null;
		this.removeAll();
		init();
		this.revalidate();
	}

	public void setAsMapped(RegulatoryNode node) {
		this.mappedNodes.add(node);
		visibleComponentsPanel = null;
		this.removeAll();
		init();
		this.revalidate();
	}

	public void unsetAsMapped(RegulatoryNode node) {
		this.mappedNodes.remove(node);
		visibleComponentsPanel = null;
		this.removeAll();
		init();
		this.revalidate();
	}

	public List<RegulatoryNode> getMappedNodes() {
		return this.mappedNodes;
	}

	@Override
	public RegulatoryGraph getGraph() {
		return config.getGraph();

	}

	@Override
	public void addNeighbour(int m, int n) {
		this.topology.addNeighbour(m, n);

	}

	@Override
	public void removeNeighbour(int m, int n) {
		this.topology.removeNeighbour(m, n);
	}

	@Override
	public boolean hasNeihgbours(int m) {
		return this.topology.hasNeighbours(m);
	}

	@Override
	public IntegrationFunctionMapping getMapping() {
		return this.integrationPanel.getMapping();
	}

	@Override
	public boolean isTrulyMapped(RegulatoryNode node, int m) {
		return (this.getMapping().isMapped(node) && this.topology
				.hasNeighbours(m));

	}

	@Override
	public Collection<Entry<RegulatoryNode, Integer>> getInfluencedModuleInputs(
			RegulatoryNode proper, int moduleIndex) {

		List<Map.Entry<RegulatoryNode, Integer>> influences = new ArrayList<Map.Entry<RegulatoryNode, Integer>>();

		if (proper.isInput()
				|| this.getMapping().getInfluencedInputs(proper).isEmpty())
			return influences;

		for (int i = 0; i < this.getNumberInstances(); i++)
			if (this.areNeighbours(i, moduleIndex))
				for (RegulatoryNode input : this.getMapping()
						.getInfluencedInputs(proper))
					influences
							.add(new AbstractMap.SimpleEntry<RegulatoryNode, Integer>(
									input, new Integer(i)));

		return influences;
	}

	@Override
	public Collection<Entry<RegulatoryNode, Integer>> getMappedToModuleArguments(
			RegulatoryNode input, int moduleIndex) {

		List<Map.Entry<RegulatoryNode, Integer>> arguments = new ArrayList<Map.Entry<RegulatoryNode, Integer>>();

		if (!input.isInput() || !this.getMapping().isMapped(input))
			return arguments;

		for (int i = 0; i < this.getNumberInstances(); i++)
			if (this.areNeighbours(moduleIndex, i))
				for (RegulatoryNode proper : this.getMapping()
						.getProperComponentsForInput(input))
					arguments
							.add(new AbstractMap.SimpleEntry<RegulatoryNode, Integer>(
									proper, new Integer(i)));

		return arguments;

	}

	public List<byte[]> getStableStates() {
		if (stableStates != null)
			return stableStates;

		stableStates = new ArrayList<byte[]>();
		/*
		 * So we need to know the stable states of the model as well as the
		 * initial states specified for each instance as well as the list of
		 * visible states
		 * 
		 * (it would be helpful to also have a function determining whether the
		 * visible components specified are sufficient to distinguish the stable
		 * states from one another.. so maybe the stable states ought to appear
		 * before in the gui)
		 * 
		 * Each property specification has to take into account the initial
		 * value of each visible component, as well as the target value of each
		 * visible component with respect to the combination of stable states at
		 * hand (all combinations need to be tested but only compatible
		 * combinations are preservable).
		 */

		// Determining the Stable States
		// Needs to determine stable states of the individual model
		try {
			FixpointTask fpTask = GSServiceManager.getService(StableStatesService.class).getTask(getGraph().getModel());
			fpTask.pattern = false; // unfold all states
			return fpTask.call();
		} catch (Exception e) {
			System.err.println("Error while computing the fixed points");
		}
		return null;
	}

	/**
	 * No longer used as the FixpointTask above can already unfold patterns of stable states
	 * FIXME: double check and delete
	 * @deprecated
	 */
	private List<byte[]> multiplexStableState(byte[] stableState) {
		List<byte[]> multiplexed = new ArrayList<byte[]>();
		boolean needsMultiplex = true;

		multiplexed.add(stableState);

		while (needsMultiplex) {
			List<byte[]> toRemove = new ArrayList<byte[]>();
			List<byte[]> toAdd = new ArrayList<byte[]>();
			MAINLOOP: for (byte[] state : multiplexed)
				for (int i = 0; i < state.length; i++)
					if (state[i] == -1) {
						toRemove.add(state);
						byte max = getGraph().getModel().getComponents().get(i)
								.getMax();
						for (byte j = 0; j <= max; j++) {
							byte[] newState = state.clone();
							newState[i] = j;
							toAdd.add(newState);
						}

						continue MAINLOOP;
					}

			if (toAdd.size() == 0)
				needsMultiplex = false;

			for (byte[] state : toRemove)
				multiplexed.remove(state);

			for (byte[] state : toAdd)
				multiplexed.add(state);

		}

		return multiplexed;
	}

	public List<List<byte[]>> getCompatibleStableStates() {
		if (compatibleStableStates != null)
			return compatibleStableStates;

		compatibleStableStates = new ArrayList<List<byte[]>>();
		List<byte[]> stableStates = getStableStates();

		for (byte[] stableState : stableStates) {
			List<byte[]> frozen = new ArrayList<byte[]>();
			frozen.add(stableState);
			List<List<byte[]>> globalList = generateCompatibleStableStates(frozen);
			if (globalList != null)
				for (List<byte[]> global : globalList)
					compatibleStableStates.add(global);
		}

		return compatibleStableStates;

	}

	private List<List<byte[]>> generateCompatibleStableStates(
			List<byte[]> frozen) {
		List<byte[]> stableStates = getStableStates();
		List<List<byte[]>> globalStableStates = new ArrayList<List<byte[]>>();

		int populated = frozen.size();
		int total = this.getNumberInstances();

		if (populated != total) // not all instances have had a stableState
								// attributed
			for (byte[] stableState : stableStates) {
				List<byte[]> newFrozen = new ArrayList<byte[]>();
				for (byte[] frozenStableState : frozen)
					newFrozen.add(frozenStableState);
				newFrozen.add(stableState);
				List<List<byte[]>> globalList = generateCompatibleStableStates(newFrozen);
				for (List<byte[]> globalState : globalList)
					globalStableStates.add(globalState);
			}
		else { // all instances have their tentative stableState

			Collection<RegulatoryNode> mappedInputs = getMapping()
					.getMappedInputs();
			boolean isCompatible = true;

			for (int instance = 0; instance < total && isCompatible; instance++) {

				byte[] stableState = frozen.get(instance);
				for (RegulatoryNode input : mappedInputs) {
					if (!isCompatible)
						break;

					IntegrationFunction integrationFunction = getMapping()
							.getIntegrationFunctionForInput(input);
					List<RegulatoryNode> arguments = getMapping()
							.getProperComponentsForInput(input);
					List<Integer> argumentValues = new ArrayList<Integer>();

					for (int neighbour = 0; neighbour < total; neighbour++) {
						if (!this.areNeighbours(instance, neighbour))
							continue;

						byte[] foreignStableState = frozen.get(neighbour);
						for (RegulatoryNode argument : arguments)
							argumentValues
									.add(new Integer(
											foreignStableState[getGraph()
													.getModel().getComponents()
													.indexOf(argument)]));

					}

					if (!argumentValues.isEmpty()) {

						byte actualValue = stableState[getGraph().getModel()
								.getComponents().indexOf(input)];
						byte computedValue = (byte) IntegrationFunction
								.getIntegrationFunctionComputer(
										integrationFunction)
								.compute(argumentValues).intValue();

						if (actualValue != computedValue)
							isCompatible = false;
					}

				}

			}

			if (isCompatible)
				globalStableStates.add(frozen);

		}

		return globalStableStates;

	}

	public boolean areCompatibleStableStatesDiscernible() {
		/*
		 * List<List<byte[]>> reducedCompatibleStableStates =
		 * getReducedCompatibleStableStates(); List<List<byte[]>>
		 * globalReducedCompatibleStableStates =
		 * getReducedSetCompatibleStableStates(getUnmappedComponents());
		 * 
		 * System.err.println("All compatible stable states\n"); for (int i = 0;
		 * i < globalReducedCompatibleStableStates.size(); i++) {
		 * System.err.println("\tStable configuration #" + (i + 1) + "\n"); for
		 * (int j = 1; j <= getNumberInstances(); j++) for (RegulatoryNode node
		 * : getUnmappedComponents()) { System.err.println("\t\t" +
		 * node.getNodeInfo().getNodeID() + "_" + j + ": " +
		 * globalReducedCompatibleStableStates
		 * .get(i).get(j-1)[getUnmappedComponents().indexOf(node)]); } }
		 * 
		 * System.err.println("All compatible visible configurations\n");
		 * System.
		 * err.println(this.visibleComponentsPanel.getSelectedNodes().size() +
		 * " components selected\n"); for (int i = 0; i <
		 * reducedCompatibleStableStates.size(); i++) {
		 * System.err.println("\tStable configuration #" + (i + 1) + "\n"); for
		 * (int j = 1; j <= getNumberInstances(); j++) for (RegulatoryNode node
		 * : this.visibleComponentsPanel.getSelectedNodes()) {
		 * System.err.println("\t\t" + node.getNodeInfo().getNodeID() + "_" + j
		 * + ": " +
		 * reducedCompatibleStableStates.get(i).get(j-1)[this.visibleComponentsPanel
		 * .getSelectedNodes().indexOf(node)]); } }
		 */
		return getReducedCompatibleStableStates().size() == getReducedSetCompatibleStableStates(
				getUnmappedComponents()).size();

	}

	/**
	 * ReducedCompatibleStableStates
	 * @return list of ReducedCompatibleStableStates
	 */
	public List<List<byte[]>> getReducedCompatibleStableStates() {
		return getReducedSetCompatibleStableStates(this.visibleComponentsPanel
				.getSelectedNodes());

	}

	private List<List<byte[]>> getReducedSetCompatibleStableStates(
			List<RegulatoryNode> subSet) {
		List<List<byte[]>> globalStableStates = getCompatibleStableStates();
		List<List<byte[]>> globalReducedStableStates = new ArrayList<List<byte[]>>();
		Set<State> uniqueSet = new HashSet<State>();

		for (List<byte[]> globalState : globalStableStates) {
			List<byte[]> reducedGlobalState = new ArrayList<byte[]>();
			for (byte[] localState : globalState) {
				byte[] reducedLocalState = computeReducedState(localState,
						subSet);
				reducedGlobalState.add(reducedLocalState);
			}

			State stateRepresentation = new State(reducedGlobalState);
			if (!uniqueSet.contains(stateRepresentation)) {
				globalReducedStableStates.add(reducedGlobalState);
				uniqueSet.add(stateRepresentation);
			}
		}

		return globalReducedStableStates;

	}

	private byte[] computeReducedState(byte[] state, List<RegulatoryNode> subSet) {
		byte[] reducedState = new byte[subSet.size()];
		for (RegulatoryNode sub : subSet)
			reducedState[subSet.indexOf(sub)] = state[getGraph().getModel()
					.getComponents().indexOf(sub)];

		return reducedState;
	}

	@Override
	public boolean areNeighbours(int m, int n) {
		return this.topology.areNeighbours(m, n);
	}

	private List<RegulatoryNode> getUnmappedComponents() {
		List<RegulatoryNode> unMappedComponents = new ArrayList<RegulatoryNode>();
		for (RegulatoryNode node : getGraph().getNodeOrder())
			if (!this.mappedNodes.contains(node))
				unMappedComponents.add(node);

		return unMappedComponents;
	}

	private class State {
		private byte[] values;

		public State(byte[] stateByte) {
			values = new byte[stateByte.length];
			for (int i = 0; i < stateByte.length; i++)
				values[i] = stateByte[i];
		}

		public State(List<byte[]> stateList) {
			int size = 0;
			for (byte[] stateByte : stateList) {
				size += stateByte.length;
			}

			values = new byte[size];

			int i = 0;
			for (byte[] stateByte : stateList)
				for (int j = 0; j < stateByte.length; j++)
					values[i++] = stateByte[j];
		}

		public byte get(int i) {
			if (i < values.length)
				return values[i];
			else
				return 0;
		}

		public int size() {
			return values.length;
		}

		@Override
		public boolean equals(Object object) {

			if (!(object instanceof State) || (object == null))
				return false;

			State state = (State) object;

			if (size() != state.size())
				return false;
			else {
				boolean identical = true;
				for (int i = 0; i < size(); i++)
					if (get(i) != state.get(i))
						identical = false;

				return identical;
			}

		}

		@Override
		public int hashCode() {
			String stringRepresentation = "";
			for (int i = 0; i < size(); i++)
				stringRepresentation += get(i);

			return stringRepresentation.hashCode();
		}

	}

	public void fireIntegrationFunctionsChanged() {
		initialStatesPanel.fireInitialStatesUpdate();
	}

	public void fireIntegrationMappingChange() {
		visibleComponentsPanel = visibleComponentsPanel.reBuild();
		initialStatesPanel.fireInitialStatesUpdate();
		compatibleStableStates = null;
		this.removeAll();
		init();
		this.revalidate();
	}

}
