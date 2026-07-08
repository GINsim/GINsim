package org.ginsim.service.tool.avatar.params;

import java.io.IOException;

import org.colomoto.biolqm.io.avatar.AvatarUtils;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.common.xml.XMLize;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateList;
import org.ginsim.core.graph.regulatorygraph.perturbation.ListOfPerturbations;
import org.ginsim.core.graph.regulatorygraph.perturbation.Perturbation;
import org.ginsim.core.utils.data.NamedObject;
import org.ginsim.service.tool.avatar.simulation.AvatarSimulation;
import org.ginsim.service.tool.avatar.simulation.AvatarSimulation.AvatarStrategy;
import org.ginsim.service.tool.modelreduction.ListOfReductionConfigs;
import org.ginsim.service.tool.modelreduction.ReductionConfig;
import org.ginsim.service.tool.avatar.simulation.FirefrontSimulation;
import org.ginsim.service.tool.avatar.simulation.MonteCarloSimulation;
import org.ginsim.service.tool.avatar.simulation.Simulation;

/**
 * Serializable context of a simulation (parameters, oracles and initial states)
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public class AvatarParameters implements XMLize, NamedObject {

	/**
	 * String name
	 */
	public String name = "new_parameter";
	/**
	 * AvatarStateStore statestore;
	 */
	public AvatarStateStore statestore;
	/**
	 * boolean array statesSelected,
	 */
	public boolean[] statesSelected,
			/**
			 * boolean array istatesSelected;
			 */
			istatesSelected; // oraclesSelected, ioraclesSelected, enabled, ienabled;
	/**
	 * boolean quiet,
	 */
	public boolean quiet,
			/**
			 * boolean avaKeepTrans
			 */
			avaKeepTrans;
	/**
	 * the int algorithm
	 */
	public int algorithm,
			/**
			 * int avaStrategy
			 */
			avaStrategy;
	/**
	 * String avaRuns
	 */
	public String avaRuns,
			/**
			 * String avaTau
			 */
			avaTau,
			/**
			 * String avaDeath
			 */
			avaDepth,
			/**
			 * String avaAproxDepth
			 */
			avaAproxDepth,
			/**
			 * string avaMinTran
			 */
			avaMinTran,
			/**
			 * String avaMinCycle
			 */
			avaMinCycle,
			/**
			 * String avaMaxPSize
			 */
			avaMaxPSize,
			/**
			 * String avaMaxRewiringSize
			 */
			avaMaxRewiringSize;
	/**
	 * String ffMaxExpand
	 */
	public String ffMaxExpand,
			/**
			 * String ffDepth,
			 */
			ffDepth,
			/**
			 * String ffAlpha,
			 */
			ffAlpha,
			/**
			 * String ffBeta
			 */
			ffBeta,
			/**
			 * String mcDepth,
			 */
			mcDepth,
			/**
			 * String mcRuns
			 */
			mcRuns;
	/**
	 * ListOfPerturbations perturbations;
	 */
	public ListOfPerturbations perturbations;
	/**
	 * ListOfReductionConfigs reductions;
	 */
	public ListOfReductionConfigs reductions;
	/**
	 * Perturbation perturbation;
	 */
	public Perturbation perturbation;
	/**
	 * ReductionConfig reduction;
	 */
	public ReductionConfig reduction;

	/**
	 * Creates an empty context
	 */
	public AvatarParameters() {
		this.algorithm = 0;
		this.quiet = true;
		this.avaRuns = "1000";
		this.avaTau = "3";
		this.avaDepth = "1E6";
		this.avaAproxDepth = "7";
		this.avaMinTran = "200";
		this.avaMinCycle = "4";
		this.avaMaxPSize = "1E4";
		this.avaMaxRewiringSize = "1E3";
		this.avaKeepTrans = true;
		this.avaStrategy = 0;
		this.ffMaxExpand = "1E3";
		this.ffDepth = "1E4";
		this.ffAlpha = "1E-5";
		this.ffBeta = "1E-5";
		this.mcDepth = "1000";
		this.mcRuns = "1000";
	}

	/**
	 * Updates the context with the parameters of a given simulation
	 * 
	 * @param sim simulation whose parameters are to be stored
	 */
	public void complete(Simulation sim) {
		if (sim instanceof AvatarSimulation) {
			AvatarSimulation avasim = (AvatarSimulation) sim;
			algorithm = 0;
			quiet = avasim.quiet;
			avaRuns = "" + avasim.runs;
			avaTau = "" + avasim.tauInit;
			avaDepth = "" + avasim.maxSteps;
			avaAproxDepth = "" + avasim.approxDepth;
			avaMinTran = "" + avasim.minTransientSize;
			avaMinCycle = "" + avasim.minCSize;
			avaMaxPSize = "" + avasim.maxPSize;
			avaMaxRewiringSize = "" + avasim.maxRewiringSize;
			avaKeepTrans = avasim.keepTransients;// avasim.keepOracle;
			AvatarStrategy[] stgs = AvatarStrategy.values();
			for (int i = 0, l = stgs.length; i < l; i++)
				if (stgs[i].equals(avasim.strategy))
					avaStrategy = i;
		} else if (sim instanceof FirefrontSimulation) {
			FirefrontSimulation ffsim = (FirefrontSimulation) sim;
			algorithm = 1;
			ffMaxExpand = "" + ffsim.maxExpand;
			ffDepth = "" + ffsim.maxDepth;
			ffAlpha = "" + ffsim.alpha;
			ffBeta = "" + ffsim.beta;
			quiet = ffsim.quiet;
		} else { // MonteCarlo
			MonteCarloSimulation mcsim = (MonteCarloSimulation) sim;
			algorithm = 2;
			mcDepth = "" + mcsim.maxSteps;
			mcRuns = "" + mcsim.runs;
		}
	}

	/**
	 * Serializes the AvatarParameters into a string
	 * 
	 * @return the string representing the context of a simulation
	 */
	public String toFullString() {
		return "name=" + name + "\n" + statesToString() + "\nquiet=" + quiet + "\navaKeepTrans=" + avaKeepTrans
				+ "\nalgorithm=" + algorithm + "\navaStrategy=" + avaStrategy + "\navaRuns=" + avaRuns + "\navaTau="
				+ avaTau + "\navaDepth=" + avaDepth + "\navaAproxDepth=" + avaAproxDepth + "\navaMinTran=" + avaMinTran
				+ "\navaMinCycle=" + avaMinCycle + "\navaMaxPSize=" + avaMaxPSize + "\navaMaxRewiringSize="
				+ avaMaxRewiringSize + "\nffMaxExpand=" + ffMaxExpand + "\nffDepth=" + ffDepth + "\nffAlpha=" + ffAlpha
				+ "\nffBeta=" + ffBeta + "\nmcDepth=" + mcDepth + "\nmcRuns=" + mcRuns;
	}

	private String statesToString() {
		int l1 = statestore.nstates.size(), l2 = statestore.instates.size(), l3 = statestore.oracles.size();
		String result = "states=";
		for (int i = 0; i < l1; i++)
			result += statestore.nstates.get(i).getMap().toString() + ",";
		if (l1 > 0)
			result = result.substring(0, result.length() - 1);
		result += "\nnamestates=[";
		for (int i = 0; i < l1; i++)
			result += statestore.nstates.get(i).getName() + ",";
		if (l1 > 0)
			result = result.substring(0, result.length() - 1);
		result += "]\nistates=";
		for (int i = 0; i < l2; i++)
			result += statestore.instates.get(i).getMap().toString() + ",";
		if (l2 > 0)
			result = result.substring(0, result.length() - 1);
		result += "\ninamestates=[";
		for (int i = 0; i < l2; i++)
			result += statestore.instates.get(i).getName() + ",";
		if (l2 > 0)
			result = result.substring(0, result.length() - 1);
		result += "]\nostates=";
		for (int i = 0; i < l3; i++)
			result += statestore.oracles.get(i).getMap().toString() + ",";
		if (l3 > 0)
			result = result.substring(0, result.length() - 1);
		result += "\nonamestates=[";
		for (int i = 0; i < l3; i++)
			result += statestore.oracles.get(i).getName() + ",";
		if (l3 > 0)
			result = result.substring(0, result.length() - 1);
		return result + "]\nstatesselection=" + AvatarUtils.toString(statesSelected) + "\nistatesselection="
				+ AvatarUtils.toString(istatesSelected);
		// result
		// +="\noracleselection="+AvatarUtils.toString(oraclesSelected)+"\nioracleselection="+AvatarUtils.toString(ioraclesSelected);
		// result+"\nenabled="+AvatarUtils.toString(enabled)+"\nienabled="+AvatarUtils.toString(ienabled);
	}

	private String getStates(NamedStateList stateList) {
		String result = "";
		for (int i = 0; i < stateList.size(); i++) {
			if (!result.isEmpty())
				result += ",";
			result += stateList.get(i).getMap().toString();
		}
		return result;
	}

	private String getStateNames(NamedStateList stateList) {
		String result = "";
		for (int i = 0; i < stateList.size(); i++) {
			if (!result.isEmpty())
				result += ",";
			result += stateList.get(i).getName();
		}
		return result;
	}

	@Override
	public void toXML(XMLWriter out) throws IOException {
		out.openTag("parameter");
		out.addAttr("name", name);
		out.addAttr("avatarparameters", toFullString());
		// System.out.println(toFullString());
		// State List
		if (reduction != null) {
			out.openTag("reduction");
			reduction.toXML(out);
			out.closeTag();
		}
		if (perturbation != null) {
			out.openTag("perturbation");
			perturbation.toXML(out);
			out.closeTag();
		}
		// System.out.println("after perturbations");

		out.openTag("stateList");
		out.addAttr("states", this.getStates(statestore.nstates));
		out.addAttr("namestates", this.getStates(statestore.nstates));
		out.addAttr("statesselection", AvatarUtils.toString(statesSelected));
		out.addAttr("istates", this.getStates(statestore.instates));
		out.addAttr("inamestates", this.getStates(statestore.instates));
		out.addAttr("istatesselection", AvatarUtils.toString(istatesSelected));
		out.addAttr("ostates", this.getStates(statestore.oracles));
		out.addAttr("onamestates", this.getStates(statestore.oracles));
		out.closeTag();
		//
		out.closeTag();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String _name) {
		// ObjectAssociationManager.getInstance().fireUserUpdate(null, this.name, name);
		this.name = _name;
	}

	@Override
	public String toString() {
		return name;
	}
}
