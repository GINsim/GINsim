package org.ginsim.service.export.cadp;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.service.tool.composition.IntegrationFunction;

/**
 *  class CADPIntegrationWriter
 */
public class CADPIntegrationWriter extends CADPWriter {
	private Map<Map.Entry<RegulatoryNode, Integer>, IntegrationProcessWriter> association = new HashMap<Map.Entry<RegulatoryNode, Integer>, IntegrationProcessWriter>();
	private Collection<String> integrationProcessSignature = new HashSet<String>();
	private Collection<String> integrationFunctionSignature = new HashSet<String>();

	/**
	 * Constructor
	 * @param config the CADPExportConfig c
	 * @throws GsException the exception graph
	 */
	public CADPIntegrationWriter(CADPExportConfig config) throws GsException {
		super(config);
		init();
	}

	/**
	 * Init function
	 * @throws GsException the graph exception
	 */
	public void init() throws GsException {

		int numberInstances = this.getNumberInstances();
		Collection<RegulatoryNode> mappedInputs = this.getMappedInputs();

		for (RegulatoryNode input : mappedInputs) {

			Collection<RegulatoryNode> properComponents = this
					.getProperComponentsForInput(input);
			IntegrationFunction integrationFunction = this
					.getIntegrationFunctionForInput(input);

			for (int instance = 1; instance <= numberInstances; instance++) {
				List<Map.Entry<RegulatoryNode, Integer>> listExternal = new ArrayList<Map.Entry<RegulatoryNode, Integer>>();
				Map.Entry<RegulatoryNode, Integer> inputFromModule = new AbstractMap.SimpleEntry<RegulatoryNode, Integer>(
						input, new Integer(instance));
				for (int neighbour = 1; neighbour <= numberInstances; neighbour++) {
					if (!areNeighbours(instance, neighbour))
						continue;
					for (RegulatoryNode proper : properComponents)
						listExternal
								.add(new AbstractMap.SimpleEntry<RegulatoryNode, Integer>(
										proper, new Integer(neighbour)));
				}

				IntegrationProcessWriter integrationProcessWriter = new IntegrationProcessWriter(
						inputFromModule, listExternal, integrationFunction);

				association.put(inputFromModule, integrationProcessWriter);

			}

		}

	}

	/**
	 * To string function
	 * @return the representation as string
	 */
	public String toString() {
		String out = "";

		out += "module integration_" + this.getModelName() + "("
				+ CADPWriter.getCommonModuleName() + ") is\n";

		for (Map.Entry<RegulatoryNode, Integer> key : association.keySet()) {
			IntegrationProcessWriter integrationProcessWriter = association
					.get(key);

			out += integrationProcessWriter.concreteIntegrationProcess();
			String formalIntegrationProcess = integrationProcessWriter
					.formalIntegrationProcess();
			String formalIntegrationFunction = integrationProcessWriter
					.formalIntegrationFunction();

			if (!integrationProcessSignature.contains(formalIntegrationProcess)) {
				integrationProcessSignature.add(formalIntegrationProcess);
				out += formalIntegrationProcess;
			}

			if (!integrationFunctionSignature
					.contains(formalIntegrationFunction)) {
				integrationFunctionSignature.add(formalIntegrationFunction);
				out += formalIntegrationFunction;
			}
		}

		out += "\nend module\n";

		return out;

	}

	/**
	 * class IntegrationProcessWriter
	 */
	public class IntegrationProcessWriter {
		private RegulatoryNode input = null;
		private int inputModuleIndex;
		private List<Map.Entry<RegulatoryNode, Integer>> listExternal = null;
		private IntegrationFunction integrationFunction;
		private int localMaxValue = 0; // simplifies, assuming that all Multi
										// variables have the same maxValue
										// this may not be true, giving rise to
										// transitions that never take place
										// in the actual Module, making the
										// Integration process excessively
										// complicated
		private int numberArguments = 0;
		private String gateType = "";
		private String concreteProcessName = "";
		private String formalProcessName = "";
		private String formalFunctionName = "";
		private String formalParametersType = "";
		private String formalFunctionReturnType = "";
		private String formalStateVarModifier = "";
		private List<String> formalGates = new ArrayList<String>();
		private List<String> formalStateVars = new ArrayList<String>();
		private List<String> formalUpdateVars = new ArrayList<String>();
		private String formalParameterBaseName = "g";
		private boolean toWrite = true;

		/**
		 * IntegrationProcessWriter constructor
		 * @param inputFromModule map of RegulatoryNode
		 * @param listExternal list of map with RegulatoryNode
		 * @param integrationFunction the integration function
		 * @throws GsException the graph exception
		 */
		public IntegrationProcessWriter(
				Map.Entry<RegulatoryNode, Integer> inputFromModule,
				List<Map.Entry<RegulatoryNode, Integer>> listExternal,
				IntegrationFunction integrationFunction) throws GsException {
			this.listExternal = listExternal;
			this.input = inputFromModule.getKey();
			this.inputModuleIndex = inputFromModule.getValue().intValue();
			this.integrationFunction = integrationFunction;

			System.err.println("Creating IntegrationProcessWriter with " + listExternal.size() + " arguments and integrationFunction = " + integrationFunction);
			
			boolean consistent = true;
			for (Map.Entry<RegulatoryNode, Integer> entry : listExternal) {
				if (entry.getKey().getMaxValue() > this.localMaxValue)
					this.localMaxValue = entry.getKey().getMaxValue();

				String componentType = entry.getKey().getMaxValue() > 1 ? "Multi"
						: "Binary";
				if (this.formalParametersType.isEmpty())
					this.formalParametersType = componentType;
				else if (this.formalParametersType != componentType)
					consistent = false;
			}

			if (!consistent)
				throw new GsException(GsException.GRAVITY_ERROR,
						"Formal parameters cannot be of different types");

			this.numberArguments = listExternal.size();
			if (this.numberArguments == 0) {
				this.toWrite = false;
				return;
			}

			if (input.getMaxValue() > 1) {
				this.gateType = "MultiIntegration";
				this.formalFunctionReturnType = "Multi";
			} else {
				this.gateType = "BinaryIntegration";
				this.formalFunctionReturnType = "Binary";
			}

			this.concreteProcessName = concreteIntegrationProcessName(input,
					inputModuleIndex);
			this.formalProcessName = formalIntegrationProcessName(input,
					numberArguments, integrationFunction);
			this.formalFunctionName = formalIntegrationFunctionName(input,
					numberArguments, integrationFunction);

			this.formalStateVarModifier = this.formalParametersType
					.equals("Multi") ? "M" : "B";

			for (int i = 0; i < this.numberArguments; i++) {
				this.formalGates.add(this.formalParameterBaseName.toUpperCase()
						+ i);
				this.formalStateVars.add(this.formalParameterBaseName
						.toLowerCase() + i);
				this.formalUpdateVars.add("new_"
						+ this.formalParameterBaseName.toLowerCase() + i);
			}
		}

		/**
		 * retuern concreteIntegrationProces string
		 * @return a string of concreteIntegrationProces
		 */
		public String concreteIntegrationProcess() {
			if (!this.toWrite)
				return "";

			List<String> concreteGateList = new ArrayList<String>();
			for (Map.Entry<RegulatoryNode, Integer> entry : listExternal)
				concreteGateList.add(CADPWriter.node2SyncAction(this.input,
						this.inputModuleIndex, entry.getKey(), entry.getValue()
								.intValue()));

			String concreteGateCommaList = makeCommaList(concreteGateList);
			String concreteGateSignature = concreteGateCommaList + " : "
					+ gateType;

			InitialStateWriter initialStateWriter = getIntegrationInitialStateWriter(listExternal);
			String initialExternalState = initialStateWriter.typedMixedList();

			String out = "";
			out += "process " + concreteProcessName + "["
					+ concreteGateSignature + "] is\n";
			out += "\t" + formalProcessName + "[" + concreteGateCommaList
					+ "](" + initialExternalState + ")\n";
			out += "end process\n\n";

			return out;
		}

		public String formalIntegrationProcess() {
			if (!this.toWrite)
				return "";

			String formalGateCommaList = makeCommaList(this.formalGates);
			String formalGateSignature = formalGateCommaList + " : "
					+ this.gateType;
			String formalStateVarsCommaList = makeCommaList(this.formalStateVars);
			String formalStateVarsSignature = formalStateVarsCommaList + " : "
					+ this.formalParametersType;
			String formalUpdateVarsCommaList = makeCommaList(this.formalUpdateVars);
			String formalUpdateVarsSignature = formalUpdateVarsCommaList
					+ " : " + this.formalParametersType;

			String out = "";

			out += "process " + this.formalProcessName + " ["
					+ formalGateSignature + "](" + formalStateVarsSignature
					+ ") is\n";
			out += "\tvar " + formalUpdateVarsSignature + " in\n";
			out += "\t\tloop\n";
			out += "\t\t\tselect\n";
			boolean isFirst = true;
			for (String stateVar : this.formalStateVars) {
				if (isFirst)
					isFirst = false;
				else
					out += "\t\t\t[]\n";

				String maxValue = (this.formalStateVarModifier.equals("B") ? 1
						: this.localMaxValue) + this.formalStateVarModifier;
				String minValue = 0 + this.formalStateVarModifier;

				String updateVar = this.formalUpdateVars
						.get(this.formalStateVars.indexOf(stateVar));

				String gateName = this.formalGates.get(this.formalStateVars
						.indexOf(stateVar));

				out += "\t\t\t\tselect\n";
				out += "\t\t\t\t\tif " + stateVar + " != " + maxValue
						+ " then " + updateVar + " := " + stateVar
						+ " + 1 else stop end if\n";
				out += "\t\t\t\t\t[]\n";
				out += "\t\t\t\t\tif " + stateVar + " != " + minValue
						+ " then " + updateVar + " := " + stateVar
						+ " - 1 else stop end if\n";
				out += "\t\t\t\tend select;\n";
				out += "\t\t\t\tif "
						+ this.formalFunctionName
						+ "("
						+ makeUpdatedCommaList(this.formalStateVars,
								this.formalUpdateVars, stateVar) + ") != "
						+ this.formalFunctionName + "("
						+ makeCommaList(this.formalStateVars) + ") then\n";
				out += "\t\t\t\t\t"
						+ gateName
						+ "("
						+ updateVar
						+ ","
						+ this.formalFunctionName
						+ "("
						+ makeUpdatedCommaList(this.formalStateVars,
								this.formalUpdateVars, stateVar) + "))\n";
				out += "\t\t\t\telse\n";
				out += "\t\t\t\t\t" + gateName + "(" + updateVar + ")\n";
				out += "\t\t\t\tend if;\n";
				out += "\t\t\t\t" + stateVar + " := " + updateVar + "\n";

			}
			out += "\t\t\tend select\n";
			out += "\t\tend loop\n";
			out += "\tend var\n";
			out += "end process\n\n";

			return out;
		}

		public String formalIntegrationFunction() {
			if (!this.toWrite)
				return "";

			String out = "";

			out += "function " + this.formalFunctionName + "("
					+ makeCommaList(this.formalStateVars) + " : "
					+ this.formalParametersType + ") : "
					+ this.formalFunctionReturnType + " is\n";

			String value = "";
			String connective = "";
			String returnValue = "";
			String elseReturnValue = "";
			String operator = " == ";

			boolean isSpecial = false;
			boolean reverseValues = false;

			switch (this.integrationFunction) {
			case OR:
				value = 0 + this.formalStateVarModifier;
				connective = " and ";
				returnValue = 0 + this.formalStateVarModifier;
				elseReturnValue = 1 + this.formalStateVarModifier;
				break;
			case AND:
				value = 1 + this.formalStateVarModifier;
				connective = " and ";
				returnValue = 1 + this.formalStateVarModifier;
				elseReturnValue = 0 + this.formalStateVarModifier;
				break;
			case THRESHOLD2:
				value = this.localMaxValue + this.formalStateVarModifier;
				connective = " or ";
				returnValue = 1 + this.formalStateVarModifier;
				elseReturnValue = 0 + this.formalStateVarModifier;
				break;
			case MAX:
				isSpecial = true;
				operator = " >= ";
				connective = " and ";
				reverseValues = false;
				break;
			case MIN:
				isSpecial = true;
				operator = " <= ";
				connective = " and ";
				reverseValues = true;
				break;
			case MAX_LEFT:
			case MAX_RIGHT:
				break;

			}

			if (!isSpecial) {
				List<String> terms = new ArrayList<String>();

				for (String stateVar : this.formalStateVars) {
					terms.add("(" + stateVar + operator + value + ")");
				}

				String ifCondition = makeCommaList(terms, connective);
				out += "\tif " + ifCondition + " then\n";
				out += "\t\treturn (" + returnValue + ")\n";
				out += "\telse\n";
				out += "\t\treturn(" + elseReturnValue + ")\n";
				out += "\tend if\n";

			} else {
				List<Integer> valueList = new ArrayList<Integer>();
				for (int v = 0; v <= this.localMaxValue; v++)
					valueList.add(new Integer(v));

				if (reverseValues)
					Collections.reverse(valueList);

				boolean isFirst = true;
				boolean isLast = false;
				String ifStatement = "if ";
				for (Integer curValue : valueList) {
					List<String> terms = new ArrayList<String>();
					for (String stateVar : this.formalStateVars)
						terms.add(stateVar + operator + value);

					isLast = valueList.size() == valueList.indexOf(curValue) + 1;

					if (isLast) {
						out += "\telse\n";
						out += "\t\treturn (" + curValue
								+ this.formalStateVarModifier + ")\n";
						out += "\tend if\n";
					} else {
						if (isFirst)
							isFirst = false;
						else
							ifStatement = "elsif ";

						out += "\t" + ifStatement
								+ makeCommaList(terms, connective) + " then\n";
						out += "\t\treturn (" + curValue
								+ this.formalStateVarModifier + ")\n";

					}

				}

			}

			out += "end function\n\n";

			return out;
		}

	}

}
