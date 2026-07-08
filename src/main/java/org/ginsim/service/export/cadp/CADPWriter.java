package org.ginsim.service.export.cadp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.LogicalModel;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.service.tool.composition.IntegrationFunction;

/**
 * 
 * Class providing methods supporting all writers of CADP specifications
 * 
 * @author Nuno D. Mendes
 * 
 */
public abstract class CADPWriter {

	private CADPExportConfig config = null;

	public CADPWriter(CADPExportConfig config) {
		this.config = config;
	}

	/**
	 * @return ordered list of components of the original LRM
	 */
	public List<RegulatoryNode> getAllComponents() {
		return this.config.getGraph().getNodeOrder();
	}

	/**
	 * @return name of stable action tau
	 */
	public static String getStableActionName() {
		return "STABLE";
	}

	public static String getCommonModuleName () {
		return "common";
	}
	
	
	/**
	 * node2Gate string setter
	 * @param node
	 *            the regulatory node
	 * @param moduleId
	 *            the identification of the instance
	 * @return the name of the corresponding gate
	 */
	public static String node2Gate(RegulatoryNode node, int moduleId) {
		return node.getNodeInfo().getNodeID().toUpperCase() + "_" + moduleId;
	}

	/**
	 * node2Gate
	 * @param node the regulatory node
	 * @return the name of the corresponding gate
	 */
	public static String node2Gate(RegulatoryNode node) {
		return node.getNodeInfo().getNodeID().toUpperCase();
	}

	/**
	 * node2StateVar
	 * @param node the regulatory node
	 * @return the name of the corresponding state variable
	 */
	public static String node2StateVar(RegulatoryNode node) {
		return node.getNodeInfo().getNodeID().toLowerCase();
	}

	/**
	 * 
	 * @param input the regulatory input node
	 * @param inputModuleIndex the identification of the instance where the input node is
	 * @param proper the regulatory proper node
	 * @param properModuleIndex the identification of the instance where the proper node is
	 * @return the name of the action updating the value of the proper component
	 *         and the mapped input node influenced by the proper componentn
	 */
	public static String node2SyncAction(RegulatoryNode input,
			int inputModuleIndex, RegulatoryNode proper, int properModuleIndex) {
		return "I_" + node2Gate(input, inputModuleIndex) + "_"
				+ node2Gate(proper, properModuleIndex);
	}

	/**
	 * NumberInstances getter
	 * @return the number of instances of the composition
	 */
	public int getNumberInstances() {
		return this.config.getTopology().getNumberInstances();
	}

	/**
	 * ModelName getter
	 * @return the name of the original LRM
	 */
	public String getModelName() {
		return this.config.getModelName();
	}

	/**
	 * MappedInputs getter
	 * @return a collection of mapped input nodes in the original LRM
	 */
	public Collection<RegulatoryNode> getMappedInputs() {
		return this.config.getMapping().getMappedInputs();
	}

	/**
	 * ProperComponentsForInput getter
	 * @param input a regulatory input node
	 * @return a collection of proper components influencing the value of the
	 *         input
	 */
	public Collection<RegulatoryNode> getProperComponentsForInput(
			RegulatoryNode input) {
		return this.config.getMapping().getProperComponentsForInput(input);
	}

	public Collection<RegulatoryNode> getInfluencedInputs(RegulatoryNode proper) {
		return this.config.getMapping().getInfluencedInputs(proper);
	}

	/**
	 * IntegrationFunctionForInput getter
	 * @param input a regulatory input node
	 * @return the integration function associated to the input node
	 */
	public IntegrationFunction getIntegrationFunctionForInput(
			RegulatoryNode input) {
		return this.config.getMapping().getIntegrationFunctionForInput(input);
	}

	/**
	 * ListVisible getter
	 * @return the collection of visible components
	 */
	public List<RegulatoryNode> getListVisible() {
		return this.config.getListVisible();
	}

	/**
	 * Method determines whether modules Mi and Mj are neighbours (this relation
	 * is not necessarily symmetrical
	 * 
	 * @param i the index of Module i (starting in 1)
	 * @param j the index of Module j (starting in 1)
	 * @return TRUE if they are neighbours, FALSE otherwise
	 */
	public boolean areNeighbours(int i, int j) {
		// Topology used indices starting in 0, here we start in 1
		return this.config.getTopology().areNeighbours(i - 1, j - 1);
	}

	public boolean hasNeighbours(int i) {
		// Topology used indices starting in 0, here we start in 1
		return this.config.getTopology().hasNeighbours(i - 1);
	}

	public InitialStateWriter getInitialStateWriter() {
		return new InitialStateWriter(this.config.getInitialStates(),
				this.getAllComponents());
	}

	public InitialStateWriter getIntegrationInitialStateWriter(
			List<Map.Entry<RegulatoryNode, Integer>> externalComponents) {
		return new InitialStateWriter(this.config.getInitialStates(),
				externalComponents, true);
	}

	public GateWriter getGateWriter() {
		return new GateWriter(this.getAllComponents());
	}

	public StateVarWriter getStateVarWriter() {
		return new StateVarWriter(this.getAllComponents());
	}

	public String concreteProcessName(int moduleId) {
		return "openLRG_"
				+ this.getInitialStateWriter().typedStateConcat(moduleId);
	}

	public String formalProcessName(String index) {
		return "LogicModel" + index;
	}

	public String concreteIntegrationProcessName(RegulatoryNode input,
			int inputModuleIndex) {
		return "Integration" + CADPWriter.node2Gate(input) + "_"
				+ inputModuleIndex;
	}

	public String formalIntegrationProcessName(RegulatoryNode input,
			int numberArguments, IntegrationFunction integrationFunction) {
		return "Integration" + (input.getMaxValue() > 1 ? "M" : "B")
				+ numberArguments + integrationFunction.name();
	}

	public String formalIntegrationFunctionName(RegulatoryNode input,
			int numberArguments, IntegrationFunction integrationFunction) {
		return "lif" + (input.getMaxValue() > 1 ? "M" : "B") + numberArguments
				+ integrationFunction.name();
	}

	public LogicalModel getModel() {
		return this.config.getGraph().getModel();
	}

	public String getBCGModelFileName(int moduleId) {
		return config.getBCGModelFilename(moduleId);
	}

	public String getLNTModelFileName() {
		return config.getLNTModelFilename();
	}

	public String getBCGIntegrationFileName(RegulatoryNode input,
			int moduleIndex) {
		return config.getBCGIntegrationFilename(input, moduleIndex);
	}

	public String getLNTIntegrationFileName() {
		return config.getLNTIntegrationFilename();
	}

	public String getExpFileName() {
		return config.getExpFilename();
	}

	public String getMCLPropertyFileName(List<byte[]> globalStableState) {
		return config.getMCLPropertyFileName(globalStableState);
	}

	protected List<byte[]> getInitialState(){
		return config.getInitialStates();
	}
	
	protected static String makeCommaList(List<String> list) {
		return makeCommaList(list, ",");
	}

	protected static String makeCommaList(List<String> list, String connective) {
		String out = "";
		for (String name : list) {
			if (!out.isEmpty())
				out += connective;
			out += name;
		}
		return out;
	}

	protected static String makeUpdatedCommaList(List<String> original,
			List<String> updated, String toUpdate) {
		List<String> modified = new ArrayList<String>();
		for (String name : original) {
			if (!name.equals(toUpdate))
				modified.add(name);
			else
				modified.add(updated.get(original.indexOf(toUpdate)));

		}
		return makeCommaList(modified);
	}

	public class InitialStateWriter {
		private List<byte[]> initialStates = null;
		private List<RegulatoryNode> listNodes = null;
		private List<Map.Entry<RegulatoryNode, Integer>> listExternal = null;
		private boolean isMixed = false;

		public InitialStateWriter(List<byte[]> initialStates,
				List<RegulatoryNode> listNodes) {
			this.initialStates = initialStates;
			this.listNodes = listNodes;
			sanityCheck();
		}

		public InitialStateWriter(List<byte[]> initialStates,
				List<Map.Entry<RegulatoryNode, Integer>> listExternal,
				boolean dummy) {
			this.initialStates = initialStates;
			this.listExternal = listExternal;
			this.isMixed = true;
			sanityCheck();
		}

		/**
		 * Makes sure that initial states have been specified. If this is not
		 * the case if falls back to defining an initial state with all
		 * components at 0
		 */
		private void sanityCheck() {
			if (this.initialStates == null) {
				this.initialStates = new ArrayList<byte[]>();
				if (this.isMixed == true) {

					byte[] mixedState = new byte[listExternal.size()];
					for (int p = 0; p < mixedState.length; p++)
						mixedState[p] = 0;
					this.initialStates.add(mixedState);

				} else {
					for (int i = 1; i <= config.getTopology()
							.getNumberInstances(); i++) {
						byte[] initialState = new byte[listNodes.size()];
						for (int p = 0; p < initialState.length; p++)
							initialState[p] = 0;
						this.initialStates.add(initialState);

					}
				}
			} else {
				// Do nothing
			}
		}

		public String typedSimpleList(int moduleId) {
			if (this.isMixed == true)
				return "";

			byte[] initialState = initialStates.get(moduleId - 1);
			List<String> listStates = new ArrayList<String>();

			for (int i = 0; i < initialState.length; i++) {
				RegulatoryNode node = listNodes.get(i);
				String modifier = node.getMaxValue() > 1 ? "M" : "B";
				listStates.add(initialState[i] + modifier);
			}

			return CADPWriter.makeCommaList(listStates);
		}

		public String typedStateConcat(int moduleId) {
			if (this.isMixed == true)
				return "";

			byte[] initialState = initialStates.get(moduleId - 1);
			List<String> listStates = new ArrayList<String>();

			for (int i = 0; i < initialState.length; i++) {
				RegulatoryNode node = listNodes.get(i);
				String modifier = node.getMaxValue() > 1 ? "M" : "B";
				listStates.add(initialState[i] + modifier);
			}

			return CADPWriter.makeCommaList(listStates, "");

		}

		public String typedMixedList() {
			if (this.isMixed == false)
				return "";

			List<String> listStates = new ArrayList<String>();

			for (Map.Entry<RegulatoryNode, Integer> entry : listExternal) {
				RegulatoryNode node = entry.getKey();
				int moduleId = entry.getValue().intValue();
				String modifier = node.getMaxValue() > 1 ? "M" : "B";
				listStates
						.add(initialStates.get(moduleId - 1)[getAllComponents()
								.indexOf(node)] + modifier);
			}

			return CADPWriter.makeCommaList(listStates);

		}

	}

	public static class GateWriter {
		List<RegulatoryNode> listNodes = null;

		public GateWriter(List<RegulatoryNode> listNodes) {
			this.listNodes = listNodes;

		}

		public String simpleList() {
			String out = CADPWriter.getStableActionName();
			for (RegulatoryNode node : listNodes) {
				out += "," + CADPWriter.node2Gate(node);
			}

			return out;
		}

		public String simpleListWithModuleId(int moduleId) {
			String out = CADPWriter.getStableActionName();
			for (RegulatoryNode node : listNodes) {
				out += "," + CADPWriter.node2Gate(node, moduleId);
			}

			return out;
		}

		public String simpleDecoratedList(String decoration) {
			String out = CADPWriter.getStableActionName();
			for (RegulatoryNode node : listNodes) {
				out = "," + CADPWriter.node2Gate(node) + decoration;
			}

			return out;
		}

		public String typedList() {
			String out = CADPWriter.getStableActionName() + ":None";
			for (RegulatoryNode node : listNodes) {
				String type = node.getMaxValue() > 1 ? "Multi" : "Binary";
				out += "," + CADPWriter.node2Gate(node) + ":" + type;

			}

			return out;

		}
	}

	public static class StateVarWriter {
		List<RegulatoryNode> listNodes = null;

		public StateVarWriter(List<RegulatoryNode> listNodes) {
			this.listNodes = listNodes;
		}

		public String simpleList() {
			String out = "";
			for (RegulatoryNode node : listNodes) {

				if (!out.isEmpty())
					out += ",";

				out += CADPWriter.node2StateVar(node);
			}

			return out;
		}

		public String typedList() {
			String out = "";
			for (RegulatoryNode node : listNodes) {

				if (!out.isEmpty())
					out += ",";

				out += CADPWriter.node2StateVar(node) + ":"
						+ (node.getMaxValue() > 1 ? "Multi" : "Binary");

			}

			return out;
		}

	}

}
