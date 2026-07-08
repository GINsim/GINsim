package org.ginsim.service.export.sat;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.modifier.booleanize.BooleanizeModifier;
import org.colomoto.biolqm.modifier.booleanize.BooleanizeService;
import org.colomoto.biolqm.tool.fixpoints.FixpointSearcher;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.PathSearcher;
import org.colomoto.mddlib.operators.MDDBaseOperators;
import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedState;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.service.tool.stablestates.StableStatesService;

/**
 * Exports a Regulatory graph into a SAT model description.
 * 
 * @author Pedro T. Monteiro
 */
public class SATEncoder {

	/**
	 * Export the graph to a SAT model description
	 * 
	 * @param config the user defined parameters with the model
	 * @param out the writer receiving the encoded model description
	 */
	public void write(SATConfig config, Writer out) throws Exception {

		LogicalModel multiValueModel = config.getModel();
		LogicalModel model = new BooleanizeModifier(multiValueModel).call();
		List<NodeInfo> coreNodes = model.getComponents();
		StringBuffer sb = new StringBuffer();
		int nSATrules = 0;
		int iNonInputs = 0;

		if (config.getExportType() == SATExportType.STABLE_STATE) {
			try {
				for (int i = 0; i < coreNodes.size(); i++) {
					NodeInfo node = coreNodes.get(i);
					this.printNodeComment2CNF(sb, node, i + 1);
				}
				FixpointSearcher stableStateSearcher = GSServiceManager.get(
						StableStatesService.class)
						.getSearcher(model);
				int root = stableStateSearcher.call();
				MDDManager manager = stableStateSearcher.getMDDManager();
				PathSearcher searcher = new PathSearcher(manager, 1);
				int[] path = searcher.getPath();
				nSATrules += this.writeDNFBDD2CNF(config, sb, manager,
						searcher, path, root, 0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			// Nodes actual logical rules
			int[] kMDDs = model.getLogicalFunctions();
			MDDManager manager = model.getMDDManager();
			MDDVariable[] MDDVars = manager.getAllVariables();

			for (int i = 0; i < coreNodes.size(); i++) {
				NodeInfo node = coreNodes.get(i);
				this.printNodeComment2CNF(sb, node, i + 1);
				if (node.isInput())
					continue;
				else
					iNonInputs++;

				int varTrue = MDDVars[i].getNode(0, 1);
				int varFalse = MDDVars[i].getNode(1, 0);
				int kVtrue = kMDDs[i];
				int kVfalse = manager.not(kVtrue);

				int mdd = MDDBaseOperators.OR.combine(manager,
						MDDBaseOperators.AND
								.combine(manager, varFalse, kVfalse),
						MDDBaseOperators.AND.combine(manager, varTrue, kVtrue));

				PathSearcher searcher = new PathSearcher(manager, 1, 1);
				int[] path = searcher.getPath();
				nSATrules += this.writeDNFBDD2CNF(config, sb, manager,
						searcher, path, mdd, coreNodes.size() + iNonInputs);
			}
		}

		// Write individual variable restrictions
		if (!config.getInputState().keySet().isEmpty()
				|| !config.getInitialState().isEmpty()) {
			sb.append("c user variable restriction\n");
			nSATrules += varRestr2SAT(model.getComponents(), config
					.getInputState().keySet().iterator(), sb);
			nSATrules += varRestr2SAT(model.getComponents(), config
					.getInitialState().keySet().iterator(), sb);
		}

		out.write("c CNF representation of the "
				+ config.getExportType().toString() + "\n");
		out.write("c of a logical model exported by GINsim\n");
		// for Intervention exports, add the number of "core" variables
		out.write("p cnf "
				+ (coreNodes.size() + (config.getExportType() == SATExportType.INTERVENTION ? iNonInputs
						: 0)) + " " + nSATrules + "\n");
		out.write(sb.toString());
	}

	private void printNodeComment2CNF(StringBuffer sb, NodeInfo node,
			int varIndex) {
		sb.append("c " + (node.isInput() ? "input" : "core") + " var["
				+ varIndex + "] " + node.getNodeID() + "\n");
	}

	private int varRestr2SAT(List<NodeInfo> nodeOrder,
			Iterator<NamedState> iter, StringBuffer sb) {
		int nSATrules = 0;
		if (!iter.hasNext())
			return nSATrules;
		// Assumes only ONE selected state
		NamedState iState = iter.next();
		Map<NodeInfo, List<Integer>> m_states = iState.getMap();
		for (int i = 0; i < nodeOrder.size(); i++) {
			List<Integer> v = m_states.get(nodeOrder.get(i));
			if (v != null && v.size() > 0) {
				// Assumes that all models have been Booleanized !
				sb.append((v.get(0) > 0 ? "" : "-") + (i + 1) + " 0\n");
				nSATrules++;
			}
		}
		return nSATrules;
	}

	/*
	 * DNF 2 CNF implemented is described in
	 * http://mathforum.org/library/drmath/view/51857.html
	 */
	private int writeDNFBDD2CNF(SATConfig config, StringBuffer sb,
			MDDManager manager, PathSearcher searcher, int[] path, int bdd,
			int intervVar) throws IOException {
		// Negate the whole DNF MDD
		searcher.setNode(manager.not(bdd));
		int nSATrules = 0;
		for (@SuppressWarnings("unused")
		int l : searcher) {
			boolean bWrite = false;
			String s = config.getExportType() == SATExportType.INTERVENTION ? intervVar
					+ " "
					: "";
			for (int i = 0; i < path.length; i++) {
				if (path[i] != -1) {
					if (bWrite)
						s += " ";
					// Invert 0s to 1s and vice-versa
					s += ((path[i] == 1) ? "-" : "") + (i + 1);
					bWrite = true;
				}
			}
			nSATrules++;
			sb.append(s + " 0\n");
		}
		return nSATrules;
	}
}