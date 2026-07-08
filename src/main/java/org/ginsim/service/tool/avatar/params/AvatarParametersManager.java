package org.ginsim.service.tool.avatar.params;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.io.avatar.AvatarUtils;
import org.ginsim.common.application.GsException;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.objectassociation.BasicGraphAssociatedManager;
import org.ginsim.core.graph.objectassociation.GraphAssociatedObjectManager;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.kohsuke.MetaInfServices;

/**
 * Saves and opens the context of a simulation
 * 
 * @author Rui Henriques
 * @version 1.0
 */
@MetaInfServices(GraphAssociatedObjectManager.class)
public class AvatarParametersManager extends BasicGraphAssociatedManager<AvatarParameterList> {

	/** Manager identifier */
	public static final String KEY = "avatar_parameters";

	/**
	 * Instantiates a manager to save and load the context of simulations
	 */
	public AvatarParametersManager() {
		super(KEY, null, RegulatoryGraph.class);
	}

	@Override
	public AvatarParameterList doCreate(Graph graph) {
		return new AvatarParameterList(graph, null);
	}

	@Override
	public void doSave(OutputStreamWriter os, Graph graph) throws GsException {
		AvatarParameterList paramList = (AvatarParameterList) ObjectAssociationManager.getInstance().getObject(graph,
				KEY, false);
		// for(AvatarParameters pi : paramList) System.out.println(pi.toFullString());
		// System.out.println("do save");

		List nodeOrder = ((RegulatoryGraph) graph).getNodeOrder();
		try {
			XMLWriter out = new XMLWriter(os);
			out.openTag("avatarParameters");
			String s_nodeOrder = nodeOrder.get(0).toString();
			for (int i = 1; i < nodeOrder.size(); i++)
				s_nodeOrder += " " + nodeOrder.get(i);
			out.addAttr("nodeOrder", s_nodeOrder);
			for (AvatarParameters sparam : paramList) {
				sparam.toXML(out);
			}
			out.closeTag();
			// out.close();
			// os.close();
		} catch (IOException e) {
			throw new GsException(GsException.GRAVITY_ERROR, e.getLocalizedMessage());
		}
	}

	@Override
	public AvatarParameterList doOpen(InputStream is, Graph graph) throws GsException {
		AvatarParameterList paramList = new AvatarParameterList((RegulatoryGraph) graph, null);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder out = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null)
				out.append(line + ";");
			reader.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			String[] params = out.toString().split("avatarparameters=");
			for (int i = 1, l = params.length; i < l; i++) {
				line = params[i].substring(1, params[i].indexOf(">") - 2);
				String[] args = line.split(";");
				AvatarParameters p = new AvatarParameters();
				p.name = getStringValue("name", args);
				p.algorithm = Integer.valueOf(getStringValue("algorithm", args));
				p.quiet = Boolean.valueOf(getStringValue("quiet", args));

				int k = 0;
				Map<String, Integer> nodes = new HashMap<String, Integer>();
				for (RegulatoryNode node : ((RegulatoryGraph) graph).getNodeOrder())
					nodes.put(node.getId(), k++);
				List<byte[]> states = getStatesList(getStringValue("states", args), nodes);
				List<byte[]> istates = getStatesList(getStringValue("istates", args), nodes);
				List<byte[]> ostates = getStatesList(getStringValue("ostates", args), nodes);
				String names = getStringValue("namestates", args), inames = getStringValue("inamestates", args),
						onames = getStringValue("onamestates", args);
				String[] namestates = names.substring(1, names.length() - 1).split(",");
				String[] inamestates = inames.substring(1, inames.length() - 1).split(",");
				String[] onamestates = onames.substring(1, onames.length() - 1).split(",");
				p.statestore = new AvatarStateStore(states, namestates, istates, inamestates, ostates, onamestates,
						(RegulatoryGraph) graph);
				p.statesSelected = getBoolVector(getStringValue("statesselection", args));
				p.istatesSelected = getBoolVector(getStringValue("istatesselection", args));

				// p.ostatesSelected = getBoolVector(getStringValue("ostatesselection",args));
				/*
				 * p.oraclesSelected = getBoolVector(getStringValue("oracleselection",args));
				 * p.ioraclesSelected = getBoolVector(getStringValue("ioracleselection",args));
				 * p.enabled = getBoolVector(getStringValue("enabled",args)); p.ienabled =
				 * getBoolVector(getStringValue("ienabled",args));
				 */

				p.avaRuns = getStringValue("avaRuns", args);
				p.avaTau = getStringValue("avaTau", args);
				p.avaDepth = getStringValue("avaDepth", args);
				p.avaAproxDepth = getStringValue("avaAproxDepth", args);
				p.avaMinTran = getStringValue("avaMinTran", args);
				p.avaMinCycle = getStringValue("avaMinCycle", args);
				p.avaMaxPSize = getStringValue("avaMaxPSize", args);
				p.avaKeepTrans = Boolean.valueOf(getStringValue("avaKeepTrans", args));
				p.avaStrategy = Integer.valueOf(getStringValue("avaStrategy", args));
				p.avaMaxRewiringSize = getStringValue("avaMaxRewiringSize", args);
				p.ffMaxExpand = getStringValue("ffMaxExpand", args);
				p.ffDepth = getStringValue("ffDepth", args);
				p.ffAlpha = getStringValue("ffAlpha", args);
				p.ffBeta = getStringValue("ffBeta", args);
				p.mcDepth = getStringValue("mcDepth", args);
				p.mcRuns = getStringValue("mcRuns", args);
				/*
				 * if(line.contains("<reduction>")) {
				 * p.reduction = new ReductionConfig();
				 * p.reduction.setName(getStringValue("simplificationConfig name", args));
				 * p.reduction.strict = Boolean.valueOf(getStringValue("strict", args));
				 * //p.reduction.setSelected(node, true); = getStringValue("removeList", args);
				 * }
				 */
				paramList.add(p);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return paramList;
	}

	private List<byte[]> getStatesList(String value, Map<String, Integer> nodes) {
		value = value.replace('-', '_');
		List<byte[]> result = new ArrayList<byte[]>();
		List<Map<String, Byte>> states = getStateMap(value);
		for (Map<String, Byte> state : states) {
			byte[] s = AvatarUtils.getFreeState(nodes.size());
			for (String key : state.keySet()) {
				s[nodes.get(key)] = state.get(key);
			}
			result.add(s);
		}
		return result;
	}

	private boolean[] getBoolVector(String str) {
		String[] sel = str.substring(1, str.length() - 1).split(",");
		boolean[] selection = new boolean[sel.length];
		for (int i = 0, l = sel.length; i < l; i++)
			selection[i] = Boolean.valueOf(sel[i]);
		return selection;
	}

	private List<Map<String, Byte>> getStateMap(String str1) {
		List<Map<String, Byte>> states = new ArrayList<Map<String, Byte>>();
		if (str1.length() <= 2)
			return states;
		String[] statesStr = str1.substring(1, str1.length() - 1).split("\\},\\{");
		for (int i = 0, l1 = statesStr.length; i < l1; i++) {
			Map<String, Byte> mstate = new HashMap<String, Byte>();
			String[] state = statesStr[i].substring(0, statesStr[i].length() - 1).split("\\], |=\\[");
			try {
				for (int j = 0, l2 = state.length; j < l2; j = j + 2)
					mstate.put(state[j], Byte.valueOf(state[j + 1]));
			} catch (Exception e) {
				System.out.println("EXCEPTION:\n" + AvatarUtils.toString(state));
			}
			states.add(mstate);
		}
		return states;
	}

	private static String getStringValue(String param, String[] args) {
		for (int i = 0, l = args.length; i < l; i++)
			if (args[i].startsWith(param))
				return args[i].substring(args[i].indexOf("=") + 1);
		return null;
	}

}
