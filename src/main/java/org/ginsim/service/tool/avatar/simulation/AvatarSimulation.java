package org.ginsim.service.tool.avatar.simulation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.StatefulLogicalModel;
import org.colomoto.biolqm.io.avatar.AvatarUtils;
import org.colomoto.biolqm.tool.simulation.multiplesuccessor.AsynchronousUpdater;
import org.ejml.simple.SimpleMatrix;
import org.ginsim.service.tool.avatar.domain.AbstractStateSet;
import org.ginsim.service.tool.avatar.domain.ApproximateFinalPaths;
import org.ginsim.service.tool.avatar.domain.CompactStateSet;
import org.ginsim.service.tool.avatar.domain.CycleGraph;
import org.ginsim.service.tool.avatar.domain.ExhaustiveFinalPaths;
import org.ginsim.service.tool.avatar.domain.FinalPaths;
import org.ginsim.service.tool.avatar.domain.Result;
import org.ginsim.service.tool.avatar.domain.State;
import org.ginsim.service.tool.avatar.domain.StateSet;
import org.ginsim.service.tool.avatar.service.EnumAlgorithm;
import org.ginsim.service.tool.avatar.utils.AvaException;
import org.ginsim.service.tool.avatar.utils.AvaMath;
import org.ginsim.service.tool.avatar.utils.ChartGNUPlot;
import org.ginsim.service.tool.avatar.utils.StateSetComparator;


/**
 * Avatar simulation for the discovery of point and complex attractors.<br>
 * Class providing all the functionalities to explore STGs, extend and rewire
 * cycles.
 * 
 * @author Rui Henriques
 * @author Pedro T. Monteiro
 * @author Nuno Mendes
 * @version 1.0
 */
public class AvatarSimulation extends Simulation {

	/** enum specifying the different behavioral strategies of Avatar */
	public enum AvatarStrategy {
		/**
		 * MatrixInversion
		 */
		MatrixInversion,
		/**
		 * RandomExit
		 */
		RandomExit
	}

	/****************/
	/** PARAMETERS **/
	/****************/

	/** cycle expansion rate */
	public int tauInit;
	/** number of iterations */
	public int runs;
	/** maximum depth */
	public int maxSteps;
	/**
	 * maximum number of visited transitions to stop expansion approximated by
	 * #states within the growing cycle times #exits
	 */
	public int maxPSize;
	/** maximum number of states within a cycle to be rewired */
	public int maxRewiringSize;
	/** minimum cycle size for rewiring */
	public int minCSize;
	/** size of small state spaces for the inflationary mode */
	public int smallStateSpace;
	/** minimum size of a transient cycles to be stored */
	public int minTransientSize;
	/** whether transient cycles should be kept between iterations */
	public boolean keepTransients;
	/** whether oracles should be kept between iterations */
	public boolean keepOracle;
	/** Avatar rewiring strategy */
	public AvatarStrategy strategy;
	/** maximum depth for the approximate rewiring strategy */
	public int approxDepth = -1;

	/**
	 * AsynchronousUpdater exhaustiveUpdater
	 */
	protected AsynchronousUpdater exhaustiveUpdater;
	// public AvatarUpdater sequentialUpdater;

	/**
	 * Instantiates an Avatar simulation
	 */
	public AvatarSimulation() {
	}

	public void addModel(StatefulLogicalModel _model) {
		super.addModel(_model);
		exhaustiveUpdater = new AsynchronousUpdater(model);
	}

	/***************/
	/** MAIN CODE **/
	/***************/

	public Result runSim() throws Exception {

		if (!quiet)
			output("Strategy: " + strategy.toString());

		/** I: Initializations **/
		Result result = new Result();
		result.filename = model.getName();
		int performed = 0, truncated = 0, avgSteps = 0, minSteps = 0;
		int stateSpaceSize = 1, space = Math.max(runs / 100, 1), psize = 0;
		int maxExits = (minTransientSize >= 300) ? minTransientSize : 300;
		for (NodeInfo comp : model.getComponents())
			stateSpaceSize *= comp.getMax() + 1;
		int largestFoundTransient = -1;
		int limitTransients = 10;
		Map<String, List<Double>> plotProbs = new HashMap<String, List<Double>>();
		PriorityQueue<StateSet> savedTransients = new PriorityQueue<StateSet>(new StateSetComparator());
		
		if (!quiet)
			output("Quiet=" + quiet + "\nNode order: " + model.getComponents() + "\nPSize=" + psize + ",maxPSize="
					+ maxPSize);

		/** II: Simulation **/

		for (int sn = 1, time = 0, tau = tauInit; sn <= runs; sn++, time = 0, avgSteps = 0, minSteps = 0, psize = 0, tau = tauInit) {
			checkPause();
		    if(keepTransients && savedTransients.size() > limitTransients) {
		    	PriorityQueue<StateSet> pqnew = new PriorityQueue<StateSet>(new StateSetComparator());
		    	while(pqnew.size() < 10) pqnew.add(savedTransients.poll());
		    	savedTransients = pqnew;
			}

			List<StateSet> temporaryTransients = new ArrayList<StateSet>();
			State istate = SimulationUtils.getRandomState(model, model.getInitialStates(), false);
			Map<String, Integer> discoveryTime = new HashMap<String, Integer>();
			output("Iteration " + sn + "/" + runs + " state=" + istate.toShortString());
			
			/*System.out.println("Iteration " + sn + "/" + runs + " state=" + istate.toShortString());
			System.out.print("Temporary:");
			for (AbstractStateSet itrans : temporaryTransients) System.out.print(itrans.size()+",");
			System.out.print("\nSaved:");
			for (AbstractStateSet itrans : savedTransients) System.out.print(itrans.size()+",");
			System.out.println();*/
			
			/** A: Initialize Simulation **/

			FinalPaths exitProbs = strategy.equals(AvatarStrategy.RandomExit) ? new ApproximateFinalPaths() : new ExhaustiveFinalPaths();
			StateSet D = new StateSet(), F = new StateSet(istate), exitStates = new StateSet();
			// boolean inflation = stateSpaceSize<=smallStateSpace; //constitutive inflationary mode for small state spaces

			/** B: Do Reincarnations **/

			StateSet localTransient = null;
			while (!F.isEmpty()) {
				checkPause();
				State s = F.getProbableRandomState();
				//System.out.println("new state: "+s.toShortString());
				if (Thread.currentThread().isInterrupted()) {
					// System.out.println("Simulation stoped by user !");
					return null;
				}
				if (!quiet)
					output("  Popped state=" + s + " Sim=" + sn + ", Reincarnation=" + time + ", #F="
							+ F.size() + ", #D=" + D.size() + ", #A=" + result.attractorsCount.keySet().size());

				/** C: Check whether state belongs a transient or terminal cycle **/

				// State nv = null; #1024
				for (AbstractStateSet itrans : temporaryTransients) {
					StateSet trans = (StateSet) itrans;
					if (Thread.currentThread().isInterrupted()) {
						System.out.println("Simulation interrompue !");
						return null;
					}
					if (trans.contains(s)) {
						//System.out.println("tempTransient #"+trans.size()+" "+trans.getKey()+" P"+trans.hasPaths()+" E"+trans.hasExits());
						if (strategy.equals(AvatarStrategy.RandomExit))
							s = trans.getExitStateSet().getProbableRandomState();
						else {
							//System.out.println("lala0");
							if(!trans.hasPaths() || !trans.hasExits()) {
								//System.out.println("lala1");
								continue;
							}
							//System.out.println("lala2");
							s = ((StateSet) trans).getProbableExitState(s);
						}
						minSteps++;
						avgSteps += Math.ceil((double) trans.size() / 2.0);
						localTransient = trans;
						if (!quiet)
							output("  Identified transient and getting out of it through state = " + s);
						break;
					}
				}
				if(s == null) System.out.println("state is null");

				if (keepTransients) {
					for (AbstractStateSet itrans : savedTransients) {
						StateSet trans = (StateSet) itrans;
						if (Thread.currentThread().isInterrupted()) {
							System.out.println("Simulation interrompue !");
							return null;
						}
						if (trans.contains(s)) {
							//System.out.println("tempTransient #"+trans.size()+" "+trans.getKey()+" P"+trans.hasPaths()+" E"+trans.hasExits());
							minSteps++;
							avgSteps += Math.ceil((double) trans.size() / 2.0);
							if (strategy.equals(AvatarStrategy.RandomExit))
								s = trans.getExitStateSet().getProbableRandomState();
							else {
								if(!trans.hasPaths() || !trans.hasExits()) continue;
								s = ((StateSet) trans).getProbableExitState(s);
							}
							localTransient = trans;
							if (!quiet)
								output("  Identified transient and getting out of it through state = " + s);
							break;
						}
					}
				}

				if (keepOracle) {
					boolean complex = false;
					for (AbstractStateSet trans : result.complexAttractors.values()) {
						//System.out.println("attractor #"+trans.size()+" "+trans.getKey());
						if (trans.contains(s)) {
							result.incrementComplexAttractor(trans.getKey(), avgSteps);
							complex = true;
							if (!quiet)
								output("  Incrementing attractor!");
							break;
						}
					}
					if (!complex) {
						for (CompactStateSet trans : oracle) {
							if (trans.contains(s)) {
								result.add(trans, avgSteps);
								complex = true;
								if (!quiet)
									output("  Incrementing attractor!");
								break;
							}
						}
					}
					if (complex)
						break;
				}

				/** D: Cycle to Rewire **/

				if (D.contains(s)) {
					StateSet Ct = new StateSet();
					time = discoveryTime.get(s.key);
					for (State ds : D.getStates())
						if (discoveryTime.get(ds.key) >= time)
							Ct.add(ds); // && dsTime<=i

					boolean extending = localTransient != null && localTransient.contains(s);
					if (extending) { 
						for (State ds : localTransient.getStates())	Ct.add(ds); 
					}

					/** D1: Extend Cycle: check whether a larger cycle can be identified **/

					int prev_cycle_size = 0;
					double exitRatio = 0;
					tau = (tauInit > 0) ? tauInit : 2;

					StateSet cycleToRewire = new StateSet(Ct), exitStatesRewiring = new StateSet();
					exitStates = null;
					//System.out.println("growing");
					do {
						checkPause();
						if (Thread.currentThread().isInterrupted()) {
							System.out.println("Simulation interrompue !");
							return null;
						}
						prev_cycle_size = Ct.size();
						if (!quiet)
							output("  Tau updated from " + tau + " to " + (tau * 2) + " (prev cycle=#" + prev_cycle_size
									+ ")");
						if (prev_cycle_size > 0 && !quiet)
							output("  Trying another round of cycle extension..");

						StateSet newstates = new StateSet();
						// if(exitStates==null || exitStates.size()<maxExits){
						extendCycle(null, Ct, exitStates, newstates, 0, tau, new HashMap<String, Integer>(), 0);
						if (exitStates != null) {
							for (State v : newstates.getStates())
								if (exitStates.contains(v))
									exitStates.remove(v);
						}
						/*
						 * } else { //StateSet myExits = new StateSet(); while(exitStates.size()>0){
						 * StateSet exits = new StateSet(); int k=0, k2=(exitStates.size()/maxExits);
						 * for(State v : exitStates.getStates()) if(k++%k2==0) exits.add(v); for(State v
						 * : exits.getStates()) exitStates.remove(v);
						 * //System.out.println(">>"+exits.size()+"|"+exitStates.size());
						 * extendCycle(null, Ct, exits, newstates, 0, tau, new
						 * HashMap<String,Integer>(), 0); //myExits.addAll(exits); //for(State v :
						 * exits.getStates()) //if(!newstates.contains(v)) myExits.add(v);
						 * if(newstates.size()>0){ for(State v : newstates.getStates())
						 * if(!exits.contains(v)) exitStates.add(v); //exitStates = exits; break; } }
						 * //exitStates = myExits; }
						 */
						tau = (tau > 40) ? tau : tau * 2;
						Ct.addAll(newstates);

						Collection<State> expand = (exitStates == null) ? Ct.getStates() : newstates.getStates();
						if (exitStates == null)
							exitStates = new StateSet();
						for (State v : expand) {
							for (State successor : generateSuccessors(v, exitProbs.getPaths(v.key), exitStates, Ct).getStates())
								if (!Ct.contains(successor))
									exitStates.add(successor);
						}
						if (!quiet)
							output("  Cycle extended from #" + prev_cycle_size + " to #" + Ct.size() + "states (#"
								+ exitStates.size() + " exits)");
						exitRatio = ((double) exitStates.size()) / (double) Ct.size();

						if (Ct.size() < maxRewiringSize) {
							cycleToRewire = new StateSet(Ct);
							exitStatesRewiring = new StateSet(exitStates);
							// System.out.println("DO:"+cycleToRewire.size());
						} // else System.out.println("SKIP:"+cycleToRewire.size());

						// memory=(int)Math.max(memory,Runtime.getRuntime().totalMemory()/1024);

					} while (exitRatio > 0 && prev_cycle_size < Ct.size() && Ct.size() < maxPSize);
					//System.out.println("grown");

					if (!quiet)
						publish("Extended cycle with #" + Ct.size() + " states and exitRatio=" + exitRatio);

					/** D2: Rewire Graph **/
					// C.add(Ct);
					if (exitStates.isEmpty()) {
						if (!quiet)
							output("  Identified an attractor!");
						if (temporaryTransients.size() == 0)
							result.add(new StateSet(Ct));
						else
							result.add(calculateComplexAttractor(Ct, temporaryTransients, savedTransients)); 
						break;
					}

					avgSteps += Math.ceil((double) cycleToRewire.size() / 2.0);
					minSteps++;
					largestFoundTransient = Math.max(largestFoundTransient, Ct.size());
					Ct = cycleToRewire;
					//System.out.println(exitStates.size()+"|"+exitStatesRewiring.size());
					F = new StateSet(exitStates);
					//F = generateSuccessors(s, exitProbs.getPaths(s.key), exitStates, Ct);
					//System.out.println("B>"+F.size());
					exitStates = exitStatesRewiring;
					if (Ct.size() > minCSize) {
						if (!quiet)
							output("  Rewiring cycle  with #" + Ct.size() + " states");
						//System.out.println("To rewire");
						rewriteGraph(Ct, exitStates, exitProbs);
						//System.out.println("Rewired");
						if (!quiet)
							output("  Cycle rewired");
						if (Ct.size() > minTransientSize) {
							StateSet transi = new StateSet(Ct);
							transi.setExitStates(new StateSet(exitStates));
							transi.setProbPaths(exitProbs);
							temporaryTransients.add(transi);
						}
					}
					// StateSet transi = calculateComplexAttractor(C,t);
					for (State ds : Ct.getStates())
						discoveryTime.put(ds.key, time);
					time++;
					D.addAll(Ct);
					if (!quiet)
						output("  Successors of " + s.toString() + " => " + F.toString());
					// if(F.isEmpty()) throw new AvaException("F is empty after re-writing a cycle
					// with successors: Unknown error!");
					// D = new StateSet();

				} else { // D does not contain s: new state never seen before

					/** E: Non-Cycle: Keep On **/

					D.add(s);
					discoveryTime.put(s.key, time++);
					F = generateSuccessors(s, exitProbs.getPaths(s.key), exitStates, new StateSet());
					//System.out.println("C>"+F.size());
					
					if (F.isEmpty()) {
						result.add(s, avgSteps);
						for (int k=temporaryTransients.size()-1; k>=0; k--) {
							StateSet transi = temporaryTransients.remove(k);
							if (!quiet) output("  Saving transient (#" + transi.size() + ")");
							//if (!strategy.equals(AvatarStrategy.RandomExit)) transi.setProbPaths(exitProbs);
							savedTransients.add(transi);
						}
							/*
							 * if(t>0){ StateSet transi = calculateComplexAttractor(C,t-1); if(!quiet)
							 * output("  Identified transient:"+transi); if(transi.size()>minTransientSize){
							 * if(!quiet) output("  Saving transient (#"+transi.size()+")");
							 * transi.setExitStates(exitStates);
							 * if(!strategy.equals(AvatarStrategy.RandomExit))
							 * transi.setProbPaths(exitProbs); savedTransients.add(transi); } }
							 */
					}
				}
				

				/** F: Finish Iteration **/

				minSteps++;
				avgSteps++;
				if (maxSteps > 0 && avgSteps >= maxSteps) {
					if (!quiet)
						output("  Reached maximum depth: quitting current simulation");
					truncated++;
					break; // last;
				}
			}

			/** G: Out of Reincarnation **/

			if ((sn + 1) % space == 0) {
				Set<String> allkeys = new HashSet<String>();
				allkeys.addAll(result.complexAttractors.keySet());
				allkeys.addAll(result.pointAttractors.keySet());
				for (String key : allkeys) {
					if (!plotProbs.containsKey(key))
						plotProbs.put(key, new ArrayList<Double>());
					plotProbs.get(key).add((double) result.attractorsCount.get(key));
				}
			}
			if (!quiet)
				output("  Out of iteration!");
			performed++;
		}

		/** H: plots **/

		if (plotProbs.size() > 0) {
//			if (!quiet)
				output("Plotting charts");
//			if (isGUI)
//				publish("Plotting charts");
			int max = 0;
			for (String key : plotProbs.keySet()) {
				max = Math.max(max, plotProbs.get(key).size());
			}
			double[][] dataset = new double[plotProbs.size()][max];
			List<String> names = new ArrayList<String>();
			List<String> namesSSs = new ArrayList<String>(result.pointAttractors.keySet());
			Collections.sort(namesSSs);
			int i = 0;
			for (String key : plotProbs.keySet()) {
				for (int k = 0; k < plotProbs.get(key).size(); k++ ) {
					dataset[i][k] = plotProbs.get(key).get(k);
				}
				if (namesSSs.contains(key)) {
					names.add("SS" + (namesSSs.indexOf(key)+1));
				} else {
					names.add(key);
				}
				i++;
			}
			
			
//			for (List<Double> vec : plotProbs.values()) {
//				for (int k = 0, j = max - vec.size(), l = vec.size(); k < l; j++, k++)
//					dataset[i][j] = vec.get(k);
//				i++;
//			}
			String title = "Plot: convergence of probability estimates";
			BufferedImage img = ChartGNUPlot.getConvergence(AvaMath.normalizeColumns(dataset), names, space, title,
					"#Iterations", "Probability").asImage();
			result.addPlot(title, img);
			if (!isGUI) {
				outputDir = (outputDir.startsWith("/")) ? new File("").getAbsolutePath() + outputDir + "/"
						: outputDir + "\\";
				String filename = outputDir + "avatar_" + model.getName() + "_convergence.png";
				ChartGNUPlot.writePNGFile(img, new File(filename));
			}
			List<String> depthRemovals = new ArrayList<String>();
			for (String key : result.attractorsDepths.keySet())
				if (result.attractorsDepths.get(key).size() == 0)
					depthRemovals.add(key);
			for (String key : depthRemovals)
				result.attractorsDepths.remove(key);
		}

		/** I: update results **/

		if (isGUI) {
			publish("Creating compact patterns of the found attractors");
		}
		result.strategy = EnumAlgorithm.AVATAR;
		result.transientMinSize = minTransientSize;
		result.maxTransientSize = largestFoundTransient;
		result.performed = performed;
		result.truncated = truncated;
		result.memory = memory;
		result.runs = runs;
		if (!quiet)
			output("Simulations asked: " + runs + ", performed: " + performed + ", truncated: " + truncated);
		result.log = "AVATAR\nModel: " + model.getName() + "\n" + saveOutput();
		return result;
	}

	/***********************/
	/** AUXILIARY METHODS **/
	/***********************/

	/**
	 * Method for generating the successor states of a given state (comprising
	 * knowledge of rewirings)
	 * 
	 * @param s
	 *            the state to be expanded
	 * @param exitProbs
	 *            knowledge of exit transitions
	 * @param exitStates
	 *            knowledge of exit states
	 * @param intraNull
	 *            knowledge of cycle that the state is possibly in
	 * @return the correct successor states
	 */
	private StateSet generateSuccessors(State s, Map<String, Double> exitProbs, StateSet exitStates,
			StateSet intraNull) {
		List<byte[]> successors = exhaustiveUpdater.getSuccessors(s.state);
		// for(byte[] succ : successors) if(succ[succ.length-2]==0)
		// System.out.println(">>"+s+"|"+new State(succ));

		double prob = s.probability * (1.0 / (double) successors.size());
		if (exitProbs == null)
			return new StateSet(successors, prob);
		else if (!quiet)
			output("  Exits of " + s + " => " + exitProbs);

		StateSet succSet = new StateSet();
		for (byte[] succ : successors) {
			State u = new State(succ, prob);
			if (intraNull.contains(u))
				continue;
			else if (exitProbs.containsKey(u.key))
				u.probability = exitProbs.get(u.key);
			succSet.add(u);
		}
		for (String sKey : exitProbs.keySet()) {
			if (!succSet.contains(sKey) && exitStates.contains(sKey)) {
				State u = exitStates.getState(sKey);
				u.probability = exitProbs.get(sKey);
				succSet.add(u);
			}
		}
		return succSet;
	}

	/**
	 * Calculates a complex attractor based on the knowledge of previous
	 * incarnations
	 * @param savedTransients PriorityQueue saved
	 * 
	 * @param Ct terminal cycles from all incarnations
	 * @param temporaryTransients the current time
	 * @return the revised complex attractor
	 */
	public StateSet calculateComplexAttractor(StateSet Ct, List<StateSet> temporaryTransients, PriorityQueue<StateSet> savedTransients) {
		StateSet Cstar = new StateSet();
		StateSet L = new StateSet(Ct);
		if (!quiet)
			output("  Visiting all reincarnations to discover the master attractor ...");
		while (!L.isEmpty()) {
			State s = L.getFirstState();
			L.remove(s);
			Cstar.add(s);
			for (int k = temporaryTransients.size()-1; k >= 0; k--) {
				StateSet transi = temporaryTransients.remove(k);
				if (transi.contains(s)) {
					for (State v : transi.getStates()) {
						if (Cstar.contains(v))
							continue;
						if (!quiet)
							output("  Adding state " + v.toString() + " from reincarnation " + k);
						L.add(v);
					}
				} else savedTransients.add(transi);
			}
		}
		return Cstar;
	}

	/**
	 * Method for rewiring a cycle
	 * 
	 * @param cycle the cycle (state-set) to be rewired
	 * @param out the exit states
	 * @param pi the transitions between cycle and exit states whose probability is
	 *            to be adjusted
	 * @throws Exception an exception
	 */
	public void rewriteGraph(StateSet cycle, StateSet out, FinalPaths pi) throws Exception {

		List<String> cycleL = new ArrayList<String>(cycle.getKeys());
		List<String> outL = new ArrayList<String>(out.getKeys());

		if (!strategy.equals(AvatarStrategy.RandomExit)) {

			/** I: MATRIX INVERSION **/

			if (strategy.equals(AvatarStrategy.MatrixInversion)) {

				final double[][] qMatrix, rMatrix;
				/** A: Computing q and r **/
				try {
					qMatrix = new double[cycle.size()][cycle.size()];
					rMatrix = new double[cycle.size()][out.size()];
					// memory=(int)Math.max(memory,Runtime.getRuntime().totalMemory()/1024);
				} catch (OutOfMemoryError e) {
					throw new AvaException(
							"[error] out-of-memory exception since cycle is too large to rewrite: please either select 'Uniform Exits' or decrease the maximum number of states for rewriting operations!");
				}

				for (State s : cycle.getStates()) {
					StateSet set = generateSuccessors(s, pi.getPaths(s.key), out, cycle);
					double p = 1.0 / (double) set.size();
					int index = cycleL.indexOf(s.key);
					for (State v : set.getStates()) {
						if (cycle.contains(v.key))
							qMatrix[index][cycleL.indexOf(v.key)] = -p; // pi(s,v)
						else if (out.contains(v.key))
							rMatrix[index][outL.indexOf(v.key)] = p; // pi(s,v)
						else {
							if (!quiet)
								output(v + " is not in cycle and not in cycle successors");
							// throw new AvaException("Unclear situation of state "+v);
						}
					}
				}
				if (!quiet)
					output("QMatrix\n" + AvatarUtils.toString(qMatrix));
				if (!quiet)
					output("RMatrix\n" + AvatarUtils.toString(rMatrix));

				/** B: Computing (I-q)^-1 * r **/

				for (int i = 0, l = cycle.size(); i < l; i++)
					qMatrix[i][i] += 1;

				// System.out.println(">>" + qMatrix.length);
				// System.out.println(">>" + rMatrix[0].length);
				final double[][] rewrittenMatrix = new double[qMatrix.length][rMatrix[0].length];
				
				boolean ejml = true;
				if (ejml) {
					SimpleMatrix RMatrix = new SimpleMatrix(rMatrix);
					
					try {
						RMatrix = new SimpleMatrix(qMatrix).invert().mult(RMatrix);
					} catch (OutOfMemoryError e) {
						throw new Exception(e);
					}
					
					for (int i = 0, l1 = RMatrix.numRows(); i < l1; i++)
						for (int j = 0, l2 = RMatrix.numCols(); j < l2; j++)
							rewrittenMatrix[i][j] = RMatrix.get(i, j);
					if (!quiet)
						output("Final Matrix EJML:\n" + AvatarUtils.toString(rewrittenMatrix));
				} else {
					
					/*System.out.println("First");
					org.ujmp.core.Matrix RM1 = DenseMatrix.Factory.importFromArray(rMatrix);
					org.ujmp.core.Matrix QM1 = SparseMatrix.Factory.importFromArray(qMatrix);
					org.ujmp.core.Matrix resMatrixA = QM1.inv().mtimes(RM1);
					RM1 = null;
					QM1 = null;

					System.out.println("Second");
					Matrix RM2 = new Matrix(rMatrix);
					Matrix QM2 = new Matrix(qMatrix);
					Matrix resMatrixB = QM2.inverse().times(RM2);
					RM2 = null;
					QM2 = null;
					
					System.out.println("Third");
					//double[][] resMatrix = MatrixUtils.blockInverse(QM, qMatrix.length/2).getData();
					System.out.println("Fourth");
					double[][] resMatrix = MatrixUtils.inverse(QM, qMatrix.length/2).getData();
					System.out.println("Fifth");*/
					
					RealMatrix RM = MatrixUtils.createRealMatrix(rMatrix), QM = MatrixUtils.createRealMatrix(qMatrix);
					double[][] resMatrix = new LUDecomposition(QM).getSolver().getInverse().multiply(RM).getData();
					for (int i = 0, l1 = rewrittenMatrix.length; i < l1; i++)
						for (int j = 0, l2 = rewrittenMatrix[0].length; j < l2; j++)
							rewrittenMatrix[i][j] = resMatrix[i][j];
					if (!quiet)
						output("Final Matrix Commons:\n" + AvatarUtils.toString(rewrittenMatrix));
				}

				/*
				 * CycleGraph digraph = new CycleGraph(outL); for(State s : cycle.getStates())
				 * digraph.add(s.key,generateSuccessors(s,pi.getPaths(s.key),out,cycle));
				 * double[][] probs = new double[cycle.size()][]; int i=0; for(String s :
				 * cycleL) probs[i++] = digraph.getExitProbs(s);
				 * System.out.println("cycle:"+cycleL); System.out.println("out:"+outL);
				 * System.out.println("graph:"+digraph.toString());
				 * System.out.println("===\n"+AvatarUtils.toString(probs));
				 * System.out.println(">>>\n"+AvatarUtils.toString(rewrittenMatrix));
				 */

				/** C: Adjusting Probabilities **/
				pi.addOutputPaths(cycleL, outL, rewrittenMatrix);
				// psize+=cycleL.size()*outL.size();

			} else {

				/** II: QUASI-OPTIMAL SOLUTION **/

				CycleGraph digraph = new CycleGraph(outL);
				for (State s : cycle.getStates())
					digraph.add(s.key, generateSuccessors(s, pi.getPaths(s.key), out, cycle));
				double[][] probs = new double[cycle.size()][];
				int i = 0;
				for (String s : cycleL)
					probs[i++] = digraph.getExitProbs(s, approxDepth);

				/*
				 * System.out.println("cycle:"+cycleL); System.out.println("out:"+outL);
				 * System.out.println("graph:"+digraph.toString());
				 * System.out.println("===\n"+AvatarUtils.toString(probs));
				 */
				pi.addOutputPaths(cycleL, outL, probs);
				// psize+=cycleL.size()*outL.size();
			}
		} else {

			/** III: APPROXIMATE SOLUTION **/

			double prob = 1.0 / out.size();
			// psize+=cycle.size()*out.size();
			pi.addOutputPaths(cycleL, outL, prob);
		}
		if (!quiet)
			output("  Cycle pivot has " + out.size() + " exists");
	}

	/**
	 * Method for extending cycles before rewiring
	 * 
	 * @param v the state being expanded (null at the start)
	 * @param cycle the state-set representing the initial cycle
	 * @param exits the extended cycle
	 * @param i the current time
	 * @param tau the expansion rate
	 * @param time structure maintaining the time/depth of the included states
	 * @param originalTime the original time
	 * @param newstates the new state
	 */
	public void extendCycle(State v, StateSet cycle, StateSet exits, StateSet newstates, int i, int tau,
			Map<String, Integer> time, int originalTime) {
		if (!quiet)
			output("    Extending tau=" + tau + " cycle=" + cycle.getKeys());
		StateSet Q = new StateSet();
		if (v == null) {
			if (exits != null)
				Q = exits;
			for (State u : cycle.getStates()) {
				time.put(u.key, i);
				if (exits == null) {
					for (State s : new StateSet(exhaustiveUpdater.getSuccessors(u.state)).getStates())
						if (!cycle.contains(s))
							Q.add(s);
				}
			}
		} else {
			if (!quiet)
				output("(" + i + ")V=" + v.key);
			time.put(v.key, i);
			newstates.add(v);
			Q.addAll(new StateSet(exhaustiveUpdater.getSuccessors(v.state)));
		}
		i++;
		StateSet additions = new StateSet();
		if (tau > 0) {
			if (!quiet)
				output("Tau>0 and Q=" + Q.getKeys());
			for (State w : Q.getStates()) {
				if (!time.containsKey(w.key)) {
					extendCycle(w, cycle, null, newstates, i, tau - 1, time, originalTime);
					if (v != null)
						time.put(v.key, Math.min(time.get(v.key), time.get(w.key)));
				} else if (v != null)
					time.put(v.key, Math.min(time.get(v.key), time.get(w.key)));
				// if(result.contains(w) && v!=null){
				// output("  w:"+w.key+" d:"+d.get(w.key)+" l:"+lambda.get(w.key));
				// if(v!=null) output("    v:"+v.key+" d:"+d.get(v.key)+"
				// l:"+lambda.get(v.key));
			}
		}
		if (v != null && time.get(v.key) > originalTime) {
			if (!quiet)
				output("V:" + v.key + "(" + time.get(v.key) + ")=>remove at i=" + i);
			newstates.remove(v);
		} else
			newstates.addAll(additions);
		// if(v!=null) output("V:"+v.key+"("+lambda.get(v.key)+"|"+d.get(v.key)+")=>add
		// at i="+i);
	}

	@Override
	public void dynamicUpdateValues() {
		List<NodeInfo> components = model.getComponents();
		int allStates = 1;
		for (NodeInfo comp : components)
			allStates *= comp.getMax() + 1;

		// non-fixed
		runs = Math.max(10000, allStates);
		maxSteps = allStates;
		approxDepth = components.size();
		maxPSize = allStates;

		int sumStates = 0;
		for (NodeInfo comp : components)
			sumStates += comp.getMax() + 1;
		minTransientSize = (int) Math.pow(sumStates, 1.5);

		// fixed
		quiet = true;
		tauInit = 3;
		strategy = AvatarStrategy.MatrixInversion;
		keepTransients = true;
		keepOracle = true;
		minCSize = 4;
	}

	@Override
	public String parametersToString() {
		return "  Runs=" + runs + "\n  Expansion #states limit=" + maxPSize + "\n  Rewiring #states limit="
				+ maxRewiringSize + "\n  Keep transients=" + keepTransients + "\n  Min transient size="
				+ minTransientSize + "\n  Keep oracles=" + keepOracle + "\n  Tau=" + tauInit
				+ "\n  Min #states SCC to rewire=" + minCSize + "\n  Max depth=" + maxSteps;
	}

	@Override
	public String getName() {
		return "Avatar " + (strategy.toString().startsWith("Matrix") ? "(exact exit probs)" : "(uniform exit probs)");
	}
}