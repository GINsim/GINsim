package org.ginsim.service.export.cadp;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;

/**
 * 
 * Class generating the EXP file which specifies the synchronization vectors
 * 
 * @author Nuno D. Mendes
 * 
 */
public class CADPExpWriter extends CADPWriter {

	public CADPExpWriter(CADPExportConfig config) {
		super(config);
	}

	public String toString() {
		String out = "";
		List<String> lines = new ArrayList<String>();

		int regularProcesses = getNumberInstances();
		int mappedInputs = getMappedInputs().size();
		int integrationProcesses = 0;

		for (int i = 1; i <= getNumberInstances(); i++) {
			if (hasNeighbours(i))
				integrationProcesses += mappedInputs;
		}

		int totalProcesses = regularProcesses + integrationProcesses;

		Collection<RegulatoryNode> visibleList = getListVisible();
		List<String> visibleGates = new ArrayList<String>();

		for (RegulatoryNode node : visibleList)
			for (int i = 0; i < regularProcesses; i++)
				visibleGates.add(node.getNodeInfo().getNodeID().toUpperCase()
						+ "_" + (i + 1));

		visibleGates.add(getStableActionName());

		out += "hide all but ";
		int index = 0;
		for (String visibleGate : visibleGates) {
			if (index++ > 0)
				out += ", ";
			out += visibleGate;
		}

		out += " in \n\tlabel par\n";

		String stableLine[] = getNewLine(totalProcesses);
		for (int i = 0; i < regularProcesses; i++)
			stableLine[i] = "STABLE";
		stableLine[stableLine.length - 1] = "STABLE";

		lines.add(syncVec(stableLine));

		int p = 0;
		Map<Map.Entry<RegulatoryNode, Integer>, Integer> orderMapped = new HashMap<Map.Entry<RegulatoryNode, Integer>, Integer>();
		for (RegulatoryNode input : getMappedInputs())
			for (int i = 1; i <= getNumberInstances(); i++)
				if (hasNeighbours(i))
					orderMapped
							.put(new AbstractMap.SimpleEntry<RegulatoryNode, Integer>(
									input, new Integer(i)), new Integer(
									regularProcesses + p++));

		for (RegulatoryNode node : getAllComponents()) {
			if (!node.isInput()) {

				for (int j = 1; j <= getNumberInstances(); j++) {

					List<Map.Entry<RegulatoryNode, Integer>> influences = new ArrayList<Map.Entry<RegulatoryNode, Integer>>();

					for (int i = 1; i <= getNumberInstances(); i++) {

						if (areNeighbours(i, j))
							for (RegulatoryNode input : getInfluencedInputs(node))
								influences
										.add(new AbstractMap.SimpleEntry<RegulatoryNode, Integer>(
												input, new Integer(i)));
					}

					for (int v = 0; v <= node.getMaxValue(); v++) {

						List<String[]> mlines = new ArrayList<String[]>();
						String line[] = getNewLine(totalProcesses);
						line[j - 1] = node2GateWithOffer(node, j, v);
						line[line.length - 1] = node2GateWithOffer(node, j, v);

						mlines.add(line);
						for (String[] localLine : multiPlex(
								mlines,
								new AbstractMap.SimpleEntry<RegulatoryNode, Integer>(
										node, new Integer(j)), v, influences,
								orderMapped)) {
							lines.add(syncVec(localLine));
						}

					}

				}
			} else { // node is input

				for (int i = 1; i <= getNumberInstances(); i++)
					if (!hasNeighbours(i))
						for (int v = 0; v <= node.getMaxValue(); v++) {
							String line[] = getNewLine(totalProcesses);
							line[i - 1] = node2GateWithOffer(node, i, v);
							line[line.length - 1] = node2GateWithOffer(node, i,
									v);
							lines.add(syncVec(line));
						}

			}

		}

		index = 0;
		for (String line : lines) {
			if (index++ > 0)
				out += ",\n";
			out += line;
		}

		out += "\n";

		out += "\tin\n";

		List<String> processNames = new ArrayList<String>();
		for (int i = 0; i < totalProcesses; i++)
			processNames.add("");

		for (int i = 0; i < regularProcesses; i++)
			processNames.set(i, getBCGModelFileName(i + 1));

		for (Map.Entry<RegulatoryNode, Integer> entry : orderMapped.keySet()) {
			processNames.set(
					orderMapped.get(entry).intValue(),
					getBCGIntegrationFileName(entry.getKey(), entry.getValue()
							.intValue()));
		}

		index = 0;
		for (String processName : processNames) {
			if (index++ > 0)
				out += " || ";
			out += "\"" + processName + "\"";
		}

		out += "\n\tend par\nend hide\n\n";

		return out;
	}

	/**
	 * 
	 * Gets a line of a given size, with an extra slot for the resulting actions
	 * 
	 * @param size
	 * @return a array of Strings representing the line columns
	 */
	private String[] getNewLine(int size) {
		String line[] = new String[size + 1];

		for (int i = 0; i < line.length; i++)
			line[i] = "_";

		return line;
	}

	/**
	 * Clones a line
	 * 
	 * @param line
	 * @return the cloned line
	 */
	private String[] cloneLine(String[] line) {
		String newLine[] = new String[line.length];
		for (int i = 0; i < line.length; i++)
			newLine[i] = line[i];

		return newLine;
	}

	/**
	 * 
	 * Takes a line a produces the synchronization vector
	 * 
	 * @param line
	 *            a array of columns
	 * @return a String with the corresponding synchronization vector
	 */
	private String syncVec(String[] line) {
		String out = "";
		for (int index = 0; index < line.length; index++) {
			if (index > 0 && index < line.length - 1)
				out += " * ";
			if (index == line.length - 1)
				out += " -> ";
			out += line[index].equals("_") ? line[index] : "\"" + line[index]
					+ "\""; // labels always need enclosing "" except _, which
							// is not a label
		}

		return out;
	}

	// TODO: Relocate this to CADPWriter
	/**
	 * 
	 * Generates the name of an action in a regular process
	 * 
	 * @param node
	 *            a regulatory node
	 * @param index
	 *            the corresponding module index
	 * @param value
	 *            the value associated with the action (the offer)
	 * @return a String representation of the update action of the regulatory
	 *         node at the given module
	 */
	private String node2GateWithOffer(RegulatoryNode node, int index, int value) {
		return node.getNodeInfo().getNodeID().toUpperCase() + "_" + index
				+ " !" + value;
	}

	// TODO: Relocate this to CADPWriter
	/**
	 * 
	 * Generates the name of the action of an integration process
	 * 
	 * @param input
	 *            the mapped input component
	 * @param proper
	 *            a proper component influencing the value of the integration
	 *            function
	 * @param indexInput
	 *            the index of the module of the input component
	 * @param indexProper
	 *            the index of the module of the proper component
	 * @param valueProper
	 *            the value to which the proper component updates to
	 * @param valueIntegration
	 *            the value to which the integration functions updates to
	 * @return a String representation of the update action of the integration
	 *         process
	 */
	private String integrationWithOffer(RegulatoryNode input,
			RegulatoryNode proper, int indexInput, int indexProper,
			int valueProper, int valueIntegration) {
		return "I_" + input.getNodeInfo().getNodeID().toUpperCase() + "_"
				+ indexInput + "_"
				+ proper.getNodeInfo().getNodeID().toUpperCase() + "_"
				+ indexProper + " !" + valueProper + " !" + valueIntegration;
	}

	// TODO: Relocate this to CADPWriter
	/**
	 * 
	 * Generates the name of the action of an integration process
	 * 
	 * @param input
	 *            the mapped input component
	 * @param proper
	 *            a proper component influencing the value of the integration
	 *            function
	 * @param indexInput
	 *            the index of the module of the input component
	 * @param indexProper
	 *            the index of the module of the proper component
	 * @param valueProper
	 *            the value to which the proper component updates to
	 * @return a String representation of the update action of the integration
	 *         process
	 */
	private String integrationWithOffer(RegulatoryNode input,
			RegulatoryNode proper, int indexInput, int indexProper,
			int valueProper) {
		return "I_" + input.getNodeInfo().getNodeID().toUpperCase() + "_"
				+ indexInput + "_"
				+ proper.getNodeInfo().getNodeID().toUpperCase() + "_"
				+ indexProper + " !" + valueProper;

	}

	/**
	 * 
	 * Generates as many sync vector lines as necessary to reflect the
	 * synchronization between the several processes
	 * 
	 * @param lines
	 *            an initial list of lines that need to be populate with
	 *            synchronizations
	 * @param mainGate
	 *            the original gate with which all other require synchronization
	 * @param value
	 *            the update value for the main gate
	 * @param influences
	 *            the input component our main gate influences
	 * @param orderMapped
	 *            the position in the sync vector of the integration processes
	 *            for each mapped input
	 * @return a list of lines
	 */

	private List<String[]> multiPlex(List<String[]> lines,
			Map.Entry<RegulatoryNode, Integer> mainGate, int value,
			List<Map.Entry<RegulatoryNode, Integer>> influences,
			Map<Map.Entry<RegulatoryNode, Integer>, Integer> orderMapped) {

		List<String[]> multiLines = new ArrayList<String[]>();
		List<Map.Entry<RegulatoryNode, Integer>> localInfluences = new ArrayList<Map.Entry<RegulatoryNode, Integer>>();
		for (Map.Entry<RegulatoryNode, Integer> influence : influences)
			localInfluences.add(influence);

		if (localInfluences.isEmpty())
			return lines;

		Map.Entry<RegulatoryNode, Integer> entry = localInfluences.remove(0);
		RegulatoryNode currentInfluence = entry.getKey();
		int currentTargetModule = entry.getValue().intValue();

		for (String[] line : lines) {

			// no change in the integration function
			{
				String[] newLine = cloneLine(line);
				newLine[orderMapped.get(entry).intValue()] = integrationWithOffer(
						currentInfluence, mainGate.getKey(),
						currentTargetModule, mainGate.getValue(), value);

				multiLines.add(newLine);
			}

			// change in the value of the integration function
			for (int v = 0; v <= currentInfluence.getMaxValue(); v++) {
				String[] newLine = cloneLine(line);
				newLine[orderMapped.get(entry).intValue()] = integrationWithOffer(
						currentInfluence, mainGate.getKey(),
						currentTargetModule, mainGate.getValue(), value, v);
				newLine[currentTargetModule - 1] = node2GateWithOffer(
						currentInfluence, currentTargetModule, v);
				multiLines.add(newLine);
			}

		}

		return multiPlex(multiLines, mainGate, value, localInfluences,
				orderMapped);

	}
}
