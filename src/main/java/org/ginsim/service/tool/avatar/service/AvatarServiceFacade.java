package org.ginsim.service.tool.avatar.service;

import java.io.File;
import java.io.IOException;
import org.colomoto.biolqm.StatefulLogicalModel;
import org.colomoto.biolqm.StatefulLogicalModelImpl;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesHandler;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesManager;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.service.tool.avatar.domain.Result;
import org.ginsim.service.tool.avatar.simulation.AvatarSimulation;
import org.ginsim.service.tool.avatar.simulation.AvatarSimulation.AvatarStrategy;
import org.ginsim.service.tool.avatar.simulation.MDDUtils;
import org.ginsim.service.tool.avatar.simulation.Simulation;
import org.ginsim.service.tool.avatar.simulation.SimulationUtils;
import org.ginsim.service.tool.avatar.utils.AvaException;
import org.ginsim.service.tool.avatar.utils.AvaOptions;
import org.kohsuke.MetaInfServices;

/**
 * Facade of an Avatar simulation.<br>
 * Instantiates an Avatar simulation from arguments within a command line,
 * provides soundness checks, and outputs statistics and help tips.
 * 
 * @author Pedro T. Monteiro
 * @author Rui Henriques
 */
@MetaInfServices(Service.class)
@Alias(value = "avatarservice")
@ServiceStatus(EStatus.DEVELOPMENT)
public class AvatarServiceFacade implements Service {

	static int DEFAULT_TAU = 2;
	static int DEFAULT_RUNS = 100;
	static int DEFAULT_MAX_STEPS = 1000;
	static int DEFAULT_MIN_CSIZE = 4;
	static int SMALL_STATE_SPACE = (int) Math.pow(2, 10);
	static int DEFAULT_MAX_PSIZE = (int) Math.pow(2, 15);
	static int MIN_TRANSIENT_SIZE = 32;

	/**
	 * Runs an avatar simulation
	 * 
	 * @param sim
	 *            the parameterized avatar simulation
	 * @return the discovered attractors, their reachability, and remaining
	 *         contextual information
	 * @throws Exception
	 *             thrown due to errors while reading and writing files, conflicting
	 *             parameters and unexpected behavior of the simulation
	 */
	public static Result run(AvatarSimulation sim) throws Exception {
		Result result = sim.runSimulation();
		// System.out.println(">> Results <<\n"+result.toString());
		return result;
	}

	/**
	 * Runs an avatar simulation from a given set of arguments
	 * 
	 * @param args
	 *            textual arguments specifying the input model and the parameters of
	 *            the avatar simulation
	 * @return the discovered attractors, their reachability, and remaining
	 *         contextual information
	 * @throws Exception
	 *             thrown due to errors while reading and writing files, conflicting
	 *             parameters and unexpected behavior of the simulation
	 */
	public static Result run(String[] args) throws Exception {
		Simulation sim = getSimulation(args);
		return run((AvatarSimulation) sim);
	}

	/**
	 * Creates an avatar simulation from a string command
	 * 
	 * @param args
	 *            command specifying the input model and the parameters of the
	 *            avatar simulation
	 * @return the avatar simulation to be executed
	 * @throws Exception
	 *             thrown due to errors while reading and writing files, conflicting
	 *             parameters and unexpected behavior of the simulation
	 */
	public static Simulation getSimulation(String args) throws Exception {
		return getSimulation(args.split("( --)|=|--"));
	}

	/**
	 * Creates an avatar simulation from a given set of arguments
	 * 
	 * @param args
	 *            textual arguments specifying the input model and the parameters of
	 *            the avatar simulation
	 * @return the avatar simulation to be executed
	 * @throws Exception
	 *             thrown due to errors while reading and writing files, conflicting
	 *             parameters and unexpected behavior of the simulation
	 */
	public static Simulation getSimulation(String[] args) throws Exception {
		AvatarSimulation sim = new AvatarSimulation();
		sim.runs = AvaOptions.getIntValue("runs", args);
		sim.tauInit = AvaOptions.getIntValue("tau", args);
		sim.maxSteps = AvaOptions.getIntValue("maxDepth", args);
		sim.minCSize = AvaOptions.getIntValue("minCycleSize", args);
		sim.maxPSize = AvaOptions.getIntValue("maxGrowthSize", args);
		sim.maxRewiringSize = AvaOptions.getIntValue("maxRewiringSize", args);
		sim.minTransientSize = AvaOptions.getIntValue("minTransientSize", args);
		sim.smallStateSpace = SMALL_STATE_SPACE;
		sim.keepTransients = AvaOptions.getBoolValue("keepTransients", args);
		sim.keepOracle = true; // AvaOptions.getBoolValue("keepAttractors",args);
		sim.quiet = AvaOptions.getBoolValue("quiet", args);
		String strategy = AvaOptions.getStringValue("strategy", args);
		if (strategy.contains("Matrix"))
			sim.strategy = AvatarStrategy.MatrixInversion;
		else
			sim.strategy = AvatarStrategy.RandomExit;
		if (AvaOptions.getBoolValue("no-extension", args)) {
			if (sim.minCSize > 2) {
				System.out.println("Minimum cycle size for rewrite was reset to 2");
				sim.minCSize = 2;
			}
		}
		sim.outputDir = AvaOptions.getStringValue("outputDir", args);
		// sim.init = AvaOptions.getStringValue("state",args);
		// System.out.println("AVATAR\nModel:
		// "+model.getName()+"\n"+parametersToString(sim));
		String filename = AvaOptions.getStringValue("input", args);
		if (filename != null)
			sim = (AvatarSimulation) addModel(sim, filename);
		return sim;
	}

	/**
	 * Add model to simulation
	 * @param sim the simulation
	 * @param filename string for filename
	 * @return the  Simulation
	 * @throws Exception the exception
	 */
	public static Simulation addModel(Simulation sim, String filename) throws Exception {
		StatefulLogicalModel model = null;
		if (filename.contains(".avatar")) {
			// AvatarImport avaImport = new AvatarImport(new File(filename));
			// model = avaImport.getModel(); //model.fromNuSMV(filename);
		} else {
			RegulatoryGraph graph = (RegulatoryGraph) GSGraphManager.getInstance().open(filename);
			NamedStatesHandler nstatesHandler = (NamedStatesHandler) ObjectAssociationManager.getInstance()
					.getObject(graph, NamedStatesManager.KEY, true);
			model = new StatefulLogicalModelImpl(graph.getModel(),
					MDDUtils.getStates(nstatesHandler, graph.getNodeInfos()), graph.getGraphName());
		}
		sim.addModel(model);
		return sim;
	}

	private static String parametersToString(AvatarSimulation sim) {
		return sim.parametersToString();
	}

	/**
	 * Provides help on the parameters of avatar simulations
	 * 
	 * @return description of the parameters of avatar simulations
	 */
	public String getHelp() {
		return "avatar - Stochastic Exploration of the Dynamics of Asynchronous Logical Models\n\n"
				+ "avatar [options] model_file\n\n" + "Options:\n\n"
				+ "\t--runs=NUMBER\t\tSpecifies the number of simulations to perform (default: 100)\n"
				+ "\t--sampling\t\tIndicates whether random initial states should be selected in each run.\n"
				+ "\t\tRandom states are select honoring initial values specified for components, as well as any specified initial oracles\n"
				+ "\t--max-steps=NUMBER\t\tSpecifies the maximum number of exploration steps in each run\n"
				+ "\t--tau=NUMBER\t\tIndicates the initial value of the cycle extension phase parameter (default: 2)\n"
				+ "\t--no-extension\t\tSuppresses the cycle extension phase\n"
				+ "\t--min-cycle-size=NUMBER\t\tSpecifies the minimum number of elements in a cycle required to trigger a graph re-write operation (default: 4)\n"
				+ "\t--max-psize=NUMBER\t\tSpecifies the maximum number of explicit state transition representations spawned\n"
				+ "\t\tby re-write operations before inflationary mode is activated (unbounded expansion of all transient cycles) (default: 2^15)\n"
				+ "\t--expand-all-transients\t\tSpecifies unbounded expansion of all transient cycles (enabled by default for state spaces with 1024 states or less)\n"
				+ "\t--min-transient-size=NUMBER\t\tSpecifies the mininum size of a transient cycle for it to be kept for subsequent simulation runs (default: 32)\n"
				+ "\t--plots\t\tEnables the generation of probability estimation plots and trajectory length distribution\n"
				+ "\t--output-dir=PATH\t\tIndicates the output directory for plot generation\n"
				+ "\t--quiet\t\tSuppresses all output except for results and fatal errors\n\n\n";
	}

}
