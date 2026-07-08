package org.ginsim.service.tool.avatar.simulation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.StatefulLogicalModel;
import org.colomoto.biolqm.io.avatar.AvatarUtils;
import org.colomoto.biolqm.tool.simulation.multiplesuccessor.AsynchronousUpdater;
import org.ginsim.service.tool.avatar.domain.AbstractStateSet;
import org.ginsim.service.tool.avatar.domain.CompactStateSet;
import org.ginsim.service.tool.avatar.domain.Result;
import org.ginsim.service.tool.avatar.domain.State;
import org.ginsim.service.tool.avatar.domain.StateSet;
import org.ginsim.service.tool.avatar.service.EnumAlgorithm;
import org.ginsim.service.tool.avatar.utils.AvaException;
import org.ginsim.service.tool.avatar.utils.ChartGNUPlot;
import org.ginsim.service.tool.avatar.utils.StateProbComparator;

/**
 * Firefront simulation for the quasi-exact analysis of point attractors
 * 
 * @author Rui Henriques
 * @author Pedro T. Monteiro
 * @author Nuno Mendes
 * @version 1.0
 */
public class FirefrontSimulation extends Simulation {

	public static double DEFAULT_ALPHA = Math.pow(10, -5);

	/** minimum probability of a state to be considered relevant */
	public double alpha = DEFAULT_ALPHA;
	/** minimum residual probability in F to stop simulation */
	public double beta = -1;
	// number of iterations public int maxRuns = -1;
	/** maximum depth */
	public int maxDepth = -1;
	/** maximum number of states to expand per iteration */
	public int maxExpand = -1;

	protected AsynchronousUpdater updater;

	/**
	 * Instantiates a Firefront simulation
	 */
	public FirefrontSimulation() {
	}

	public void addModel(StatefulLogicalModel _model) {
		super.addModel(_model);
		updater = new AsynchronousUpdater(model);
	}

	@Override
	public Result runSim() throws IOException {

		/** A: parameterize/initialize firefront */

		Result result = new Result();
		result.filename = model.getName();
		if (beta == -1) beta = alpha;
		// int nrStates = 1;
		// for(NodeInfo comp : model.getNodeOrder()) nrStates *= comp.getMax()+1;
		// int maxIterations = (int) Math.pow(nrStates,2);
		// if(maxRuns<0) maxRuns = maxSteps;
		List<double[]> pStates = new ArrayList<double[]>(), pProbs = new ArrayList<double[]>();

		if (model.getInitialStates().size() > 1 || !SimulationUtils.isSingleState(model.getInitialStates().get(0)))
			throw new AvaException("FireFront requests a unique initial state, please select one!");
		StateSet F = new StateSet(new State(model.getInitialStates().get(0), 1));
		StateSet N = new StateSet(), A = new StateSet();

		/** B: firefront converging behavior */

		int k = 0, na = 0;
		Map<String, Integer> oscillatorCount = new HashMap<String, Integer>();
		Map<String, Integer> oscillatorDepth = new HashMap<String, Integer>();
		StateSet complexA = new StateSet();
		String revisit = null;

		for (; k <= maxDepth && F.totalProbability() > beta; k++) {
			checkPause();
			// for(State s : F.getStates())
			// toPrint.append(AvatarUtils.toString(s.state)+",");
			// toPrint.append("\n");
			// for(State s : fixedStates)
			// toPrint.append(F.getState(s.key)+"|"+N.getState(s.key)+",");
			// toPrint.append("\n");
			String fid = F.getKeys() + "";
			if (revisit == null) {
				if (oscillatorCount.containsKey(fid)) {
					if (oscillatorCount.get(fid) == 2) {
						revisit = fid;
						complexA.addAll(F);
					} else
						oscillatorCount.put(fid, oscillatorCount.get(fid) + 1);
				} else {
					oscillatorCount.put(fid, 0);
					oscillatorDepth.put(fid, k);
				}
			} else {
				if (revisit.equals(fid)) {
					List<StateSet> complexAttractors = getAttractorsFromSet(complexA);
					for (StateSet c : complexAttractors) {
						result.complexAttractors.put("att_" + na, c);
						result.attractorsDepths.put("att_" + na, Arrays.asList(oscillatorDepth.get(fid)));
						double prob = 0;
						for (State s : F.getStates())
							if (c.contains(s))
								prob += s.probability;
						// result.attractorsUpperBound.put("att_"+na,prob+N.totalProbability());
						result.attractorsLowerBound.put("att_" + (na++), prob);
					}
					F = new StateSet();
					break;
				}
				complexA.addAll(F);
			}

			if (isGUI) {
				publish(" Iteration:" + k + " set cardinals:[F=" + F.size() + ",N=" + N.size() + ",A=" + A.size() + "]"
						+ " set probs:[F=" + AvatarUtils.round(F.totalProbability()) + ",N="
						+ AvatarUtils.round(N.totalProbability()) + ",A=" + AvatarUtils.round(A.totalProbability())
						+ "]");
			}

			pStates.add(new double[] { F.size(), N.size(), A.size() });
			pProbs.add(new double[] { F.totalProbability(), N.totalProbability(), A.totalProbability() });

			// if (!quiet)
			// output("Iteration:" + k + "\n states=[F=" + F.size() + ",N=" + N.size() +
			// ",A=" + A.size() + "]"
			// + "\n probs=[F=" + F.totalProbability() + ",N=" + N.totalProbability() +
			// ",A="
			// + A.totalProbability() + "]" + "\n total prob="
			// + (F.totalProbability() + N.totalProbability() + A.totalProbability()));

			/** B1: states to expand and pass */

			StateSet toExpand = new StateSet(), toPass = new StateSet();
			if (maxExpand >= 0 && F.size() > maxExpand) {
				List<State> states = new ArrayList<State>();
				states.addAll(F.getStates());
				Collections.sort(states, new StateProbComparator(false /* desc */)); // { $b.probability <=>
																						// $a.probability } @states;
				for (int i = 0; i < maxExpand; i++)
					toExpand.add(states.get(i));
				for (int i = maxExpand, l = states.size(); i < l; i++)
					toPass.add(states.get(i));
			} else
				toExpand = F;
			if (!quiet)
				output("  [F=" + F.size() + ",EXPAND=" + toExpand.size() + ",PASS=" + toPass.size() + "]");

			/** B2: for each expanding state generate succ to find attractors */

			// result.memory=(int)Math.max(result.memory,Runtime.getRuntime().totalMemory()/1024);
			// boolean discovery = false;
			for (State s : toExpand.getStates()) {
				List<byte[]> successors = updater.getSuccessors(s.state);
				double prob = s.probability * (1.0 / (double) successors.size());
				StateSet Q = new StateSet(successors, prob); // add successors with correct prob
				if (Q.isEmpty()) {
					// discovery = true;
					A.addCumulative(s);
					if (result.contains(s)) {
						result.increment(s);
					} else {
						result.add(s);
						result.attractorsDepths.get(s.key).add(k);
					}
					if (!quiet)
						output("  Found an attractor:" + s.toString());
				} else {
					boolean complex = false;
					for (AbstractStateSet trans : result.complexAttractors.values()) {
						if (trans.contains(s)) {
							A.addCumulative(s);
							// System.out.println(result.attractorsLowerBound.get(trans.getKey()));
							result.attractorsLowerBound.put(trans.getKey(),
									result.attractorsLowerBound.get(trans.getKey()) + s.probability);
							result.attractorsDepths.get(trans.getKey()).add(k);
							complex = true;
							break;
						}
					}
					if (!complex) {
						for (CompactStateSet trans : oracle) {
							if (trans.contains(s)) {
								A.addCumulative(s);
								result.complexAttractors.put("att_" + na, trans);
								trans.setKey("att_" + na);
								ArrayList<Integer> depths = new ArrayList<Integer>();
								depths.add(k);
								result.attractorsDepths.put("att_" + na, depths);
								result.attractorsLowerBound.put("att_" + na, s.probability);
								na++;
								complex = true;
								if (!quiet)
									output("  Incrementing attractor!");
								break;
							}
						}
					}
					if (!complex) {
						if (!quiet)
							output("  " + Q.size() + " successors\n  Parent state has probability " + s.probability);
						for (State v : Q.getStates()) {
							if (toPass.contains(v))
								toPass.addCumulative(v);
							else {
								if (N.contains(v)) {
									v.probability += N.getProbability(v);
									N.remove(v);
								}
								if (!quiet)
									output("  v => " + v.toString());
								if (v.probability >= alpha)
									toPass.addCumulative(v); // if(!A.contains(v) && !toExpand.contains(v))
								else
									N.add(v);
							}
						}
					}
				}
				/*
				 * depth++; if(maxSteps>0 && depth>=maxSteps){
				 * output("Reached maximum depth: quitting current simulation"); discovery =
				 * true; truncated++; break; }
				 */
			}
			F = toPass;
			// System.out.println(">"+F.getKeys());
		}

		if (!quiet)
			output("Final results:\n  states=[F=" + F.size() + ",N=" + N.size() + ",A=" + A.size() + "]"
					+ "\n  probs=[F=" + F.totalProbability() + ",N=" + N.totalProbability() + ",A="
					+ A.totalProbability() + ",residual=" + (N.totalProbability() + F.totalProbability()) + "]"
					+ "\n  total prob=" + (F.totalProbability() + N.totalProbability() + A.totalProbability()));
		result.residual = N.totalProbability() + F.totalProbability();

		if (!isGUI) {
			outputDir = (outputDir.contains("/") || outputDir.contains("\\")) ? outputDir + "/"
					: new File("").getAbsolutePath() + outputDir + "/";
		}

		// Plots
		
		String title = "Plot: F, N and A cardinal evolutions";
		// pStates.add(new double[]{F.size(),N.size(),A.size()});
		BufferedImage img = ChartGNUPlot.getProgression(pStates, title, "#Iterations", "#states").asImage();
		result.addPlot(title, img);
		if (!isGUI) {
			String filename = outputDir + model.getName() + "_states.png";
			ChartGNUPlot.writePNGFile(img, new File(filename));
		}
		String title2 = "Plot: F, N and A cumulative probability evolutions";
		// pProbs.add(new
		// double[]{F.totalProbability(),N.totalProbability(),A.totalProbability()});
		BufferedImage img2 = ChartGNUPlot.getProgression(pProbs, title2, "#Iterations", "probability").asImage();
		result.addPlot(title2, img2);
		if (!isGUI) {
			String filename = outputDir + model.getName() + "_probs.png";
			ChartGNUPlot.writePNGFile(img2, new File(filename));
		}

		// System.out.println(toPrint.toString());
		// for(double[] v : pStates) System.out.println("s:"+AvatarUtils.toString(v));
		// for(double[] v : pProbs) System.out.println("p:"+AvatarUtils.toString(v));

		if(F.totalProbability() > beta) result.convergence = false;
		for (State a : result.pointAttractors.values())
			result.setBounds(a.key, a.probability, a.probability + F.totalProbability() + N.totalProbability());
		for (String key : result.complexAttractors.keySet())
			result.attractorsUpperBound.put(key,
					result.attractorsLowerBound.get(key) + F.totalProbability() + N.totalProbability());

		// result.runs = maxRuns;
		// result.truncated = truncated;
		// result.performed = performed;
		// output("Total of "+k+" steps from a max of "+maxRuns+" iterations!");
		// System.out.println("Runs:"+maxRuns+" truncated:"+truncated+"
		// performed:"+performed);
		result.strategy = EnumAlgorithm.FIREFRONT;
		result.log = saveOutput();
		return result;
	}

	private boolean partOfComplexAttractor(State s, Result result) {
		// TODO Auto-generated method stub
		return false;
	}

	private List<StateSet> getAttractorsFromSet(StateSet complexA) {
		List<StateSet> result = new ArrayList<StateSet>();
		Collection<String> keys = complexA.getKeys();
		while (!keys.isEmpty()) {
			StateSet att = new StateSet();
			String key = keys.iterator().next();
			State s = complexA.getState(key);
			keys.remove(key);

			List<byte[]> successors = updater.getSuccessors(s.state);
			while (successors.size() > 0) {
				State v = new State(successors.remove(0));
				if (!att.contains(v)) {
					att.add(v);
					keys.remove(v.key);
					successors.addAll(updater.getSuccessors(v.state));
				}
			}
			result.add(att);
		}
		return result;
	}

	@Override
	public void dynamicUpdateValues() {
		List<NodeInfo> components = model.getComponents();
		int allStates = 1;
		for (NodeInfo comp : components)
			allStates *= comp.getMax() + 1;
		alpha = 1.0 / (double) (10 * allStates);
		beta = alpha;
		maxDepth = allStates;
		maxExpand = allStates;
		quiet = true;
	}

	@Override
	public String parametersToString() {
		return "  Alpha=" + alpha + "\n  Beta=" + beta + "\n  Max depth=" + maxDepth + "\n  Max states expanded/Iteration="
				+ maxExpand;
	}

	@Override
	public String getName() {
		return "FireFront";
	}
}
