package org.ginsim.servicegui.tool.avatar.algopanels;

import javax.swing.JPanel;

import org.colomoto.biolqm.StatefulLogicalModel;
import org.ginsim.service.tool.avatar.params.AvatarParameters;
import org.ginsim.service.tool.avatar.simulation.Simulation;

/**
 * Class for managing the panel with the parameters of a simulation (e.g.
 * Avatar, Firefront and MonteCarlo panels).<br>
 * Panels for alternative simulations can be easily added.
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public abstract class SimulationPanel extends JPanel {
	private static final long serialVersionUID = -8563253212299485519L;

	/**
	 * Given contextual information, returns a simulation adequately parameterized
	 * according to the panel
	 * 
	 * @param model the stateful logical model possibly defining a set of initial
	 *            states and oracles
     *            whether charts should be created and plotted
	 * @param quiet
	 *            whether detailed logs of the behavior of the simulation are to be
	 *            printed (default: true)
	 * @return the parameterized simulation ready to be executed
	 * @throws Exception the exception
	 */
	public abstract Simulation getSimulation(StatefulLogicalModel model, boolean quiet) throws Exception;

	/**
	 * Saves the parameters within the fields of the panel
	 * 
	 * @param param
	 *            the set of parameters to be read for populating the panel
	 */
	public abstract void unload(AvatarParameters param);

	/**
	 * Reads the fields of the panel and exports them into a set of parameters
	 * 
	 * @param param
	 *            the set of parameters to be populated
	 */
	public abstract void load(AvatarParameters param);
}
