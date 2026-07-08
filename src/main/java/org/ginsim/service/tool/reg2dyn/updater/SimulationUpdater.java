package org.ginsim.service.tool.reg2dyn.updater;

import java.util.Iterator;


/**
 * This is the part of the simulation in charge for generating the following states.
 *
 * supported methods are:
 * <ul>
 * 	<li>synchronous search: all changes at once, each state has at most one successor</li>
 * 	<li>asynchronous search: changes one by one, each state can have many successors</li>
 * 	<li>by priority class: genes are classed by priority groups.
 *  <ul>
 *      <li>only changes on the higher priority group(s) containing changing genes will be applied.</li>
 *      <li>if this concerns several groups, changes corresponding to each group will be applied asynchronously</li>
 *      <li>each group can be either synchronous or asynchronous, changes will be applied all at once or one by one, depending on the group!</li>
 *      <li>+1 and -1 transitions can be separated to create really fine-tuned groups</li>
 *  </ul></li>
 * </ul>
 */
public interface SimulationUpdater extends Iterator {

	/**
	 * next state getter
	 * @return byte array state
	 */
	byte[] nextState();

	/**
	 * Setter for state
	 * @param state byte array state
	 * @param depth depth value
	 * @param node the node object
	 */
	void setState(byte[] state, int depth, Object node);

	/**
	 * Clone function
	 * @param state byte array state
	 * @return SimulationUpdater
	 */
	SimulationUpdater cloneForState(byte[] state);
	
}
