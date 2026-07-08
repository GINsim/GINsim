package org.ginsim.service.export.prism;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.mddlib.PathSearcher;
import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedState;

/**
 * Exports a Regulatory graph into a PRISM model description.
 * 
 * @author Pedro T. Monteiro
 */
public class PRISMEncoder {

	// http://www.prismmodelchecker.org/manual/ThePRISMLanguage/ModulesAndVariables
	private static String[] prismReserved = { "A", "bool", "clock", "const",
			"ctmc", "C", "double", "dtmc", "E", "endinit", "endinvariant",
			"endmodule", "endrewards", "endsystem", "false", "formula",
			"filter", "func", "F", "global", "G", "init", "invariant", "I",
			"int", "label", "max", "mdp", "min", "module", "X",
			"nondeterministic", "Pmax", "Pmin", "P", "probabilistic", "prob",
			"pta", "rate", "rewards", "Rmax", "Rmin", "R", "S", "stochastic",
			"system", "true", "U", "W" };

	private String avoidPRISMNames(String keyword) {
		if (keyword == null) {
			return keyword;
		}
		if (keyword.length() == 1) {
			return "_" + keyword;
		}

		for (String reserved : prismReserved) {
			if (keyword.compareToIgnoreCase(reserved) == 0) {
				return "_" + keyword;
			}
		}
		// PRISM does not like "-" on keywords
		return keyword.replace("-", "_");
	}

	/**
	 * Export the graph to a PRISM model description
	 * 
	 * @param config the user defined parameters with the model
	 * @param out the writer receiving the encoded model description
	 */
	public void write(PRISMConfig config, Writer out) throws IOException,
			GsException {

		LogicalModel model = config.getModel();
		List<NodeInfo> coreNodes = model.getComponents();
		// Nodes actual logical rules
		int[] kMDDs = model.getLogicalFunctions();

		out.write("dtmc\n");
		for (int i = 0; i < coreNodes.size(); i++) {
			NodeInfo node = coreNodes.get(i);
			String nodeID = this.avoidPRISMNames(node.getNodeID());
			out.write("\nmodule M_" + nodeID);
			if (node.isInput())
				out.write(" // Input variable");
			out.write("\n");
			out.write("  " + nodeID + " : [0.." + node.getMax() + "];\n");
			out.write("\n");
			out.write("  [] " + nodeID + "_focal>" + nodeID + " & " + nodeID
					+ "<" + +node.getMax() + " -> (" + nodeID + "'=" + nodeID
					+ "+1);\n");
			out.write("  [] " + nodeID + "_focal<" + nodeID + " & " + nodeID
					+ ">0 -> (" + nodeID + "'=" + nodeID + "-1);\n");
			out.write("endmodule\n");
			nodeRules2PRISM(out, model, kMDDs[i], coreNodes, node);
		}

		// Write selected initial state
		Set<NamedState> sInputState = config.getInputState().keySet();
		Set<NamedState> sInitialState = config.getInitialState().keySet();
		if (sInputState != null && sInputState.iterator().hasNext()
				|| sInitialState != null && sInitialState.iterator().hasNext()) {
			boolean first = true;
			out.write("\n// Initial states\ninit");
			first = this.writeInitialConds(out, sInputState, first);
			first = this.writeInitialConds(out, sInitialState, first);
			out.write("\nendinit\n");
		}
		out.write("\n");

		// Write all defined states as labels
		this.writeLabels(out, sInputState.iterator());
		this.writeLabels(out, sInitialState.iterator());
	}

	private void writeLabels(Writer out, Iterator<NamedState> iter)
			throws IOException {
		while (iter.hasNext()) {
			NamedState iState = iter.next();
			Map<NodeInfo, List<Integer>> m_states = iState.getMap();
			String s_init = "";

			for (NodeInfo node : m_states.keySet()) {
				List<Integer> v = m_states.get(node);
				if (v != null && v.size() > 0) {
					if (!s_init.isEmpty())
						s_init += " & ";
					s_init += "(";
					for (int j = 0; j < v.size(); j++) {
						if (j > 0)
							s_init += " | ";
						s_init += avoidPRISMNames(node.getNodeID()) + "="
								+ v.get(j);
					}
					s_init += ")";
				}
			}
			out.write("label \"" + avoidPRISMNames(iState.getName()) + "\" = "
					+ s_init + ";\n");
		}
	}

	private boolean writeInitialConds(Writer out, Set<NamedState> sNamedState,
			boolean first) throws IOException {
		if (sNamedState != null && sNamedState.iterator().hasNext()) {
			Map<NodeInfo, List<Integer>> mStateComp = sNamedState.iterator()
					.next().getMap();
			for (NodeInfo node : mStateComp.keySet()) {
				if (!first)
					out.write(" &");
				List<Integer> vList = mStateComp.get(node);
				if (vList != null && vList.size() > 0) {
					out.write("\n  (");
					for (int j = 0; j < vList.size(); j++) {
						if (j > 0)
							out.write(" | ");
						out.write(this.avoidPRISMNames(node.getNodeID()) + "="
								+ vList.get(j));
					}
					out.write(")");
					first = false;
				}
			}
		}
		return first;
	}

	private void nodeRules2PRISM(Writer out, LogicalModel model, int nodeMDD,
			List<NodeInfo> coreNodeOrder, NodeInfo node) throws IOException {
		PathSearcher searcher = new PathSearcher(model.getMDDManager(), 1,
				node.getMax());
		int[] path = searcher.getPath();
		searcher.setNode(nodeMDD);

		int leafValue = 0;
		out.write("formula " + avoidPRISMNames(node.getNodeID())
				+ "_focal = \n");
		String s = "";
		for (int l : searcher) {
			boolean bWrite = false;
			for (int i = 0; i < path.length; i++) {
				if (path[i] != -1) {
					if (!bWrite)
						s += "  (";
					if (bWrite)
						s += " & ";
					s += avoidPRISMNames(coreNodeOrder.get(i).getNodeID())
							+ "=" + path[i];
					bWrite = true;
				}
			}
			if (s.isEmpty()) {
				s += "  (true";
				leafValue = l;
			}
			s += ")? " + l + " : \n";
		}
		out.write(s);
		out.write("  " + leafValue + ";\n");
	}
}