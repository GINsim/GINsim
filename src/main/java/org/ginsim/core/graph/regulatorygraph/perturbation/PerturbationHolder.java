package org.ginsim.core.graph.regulatorygraph.perturbation;

/**
 * An interface for configuration objects who store a selected perturbation.
 * 
 * @author Aurelien Naldi
 *
 */
public interface PerturbationHolder {

	/**
	 * Retrieve the stored perturbation.
	 * 
	 * @return the selected perturbation
	 */
	Perturbation getPerturbation();
	
	/**
	 * Select a perturbation.
	 * @param perturbation the perturbation
	 */
	void setPerturbation(Perturbation perturbation);
}
