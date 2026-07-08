package org.ginsim.service.tool.modelreduction;

import org.ginsim.core.graph.regulatorygraph.namedstates.PatternHolder;

/**
 * An interface for configuration objects which store a selected reduction.
 * 
 * @author Aurelien Naldi
 *
 */
public interface ReductionHolder extends PatternHolder {

	/**
	 * Retrieve the stored reduction.
	 * 
	 * @return the selected reduction
	 */
	ReductionConfig getReduction();
	
	/**
	 * Select a reduction.
	 * @param reduction reduction config
	 */
	void setReduction(ReductionConfig reduction);
}
