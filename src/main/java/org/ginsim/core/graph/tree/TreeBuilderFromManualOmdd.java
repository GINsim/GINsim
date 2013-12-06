package org.ginsim.core.graph.tree;

import java.util.List;


public class TreeBuilderFromManualOmdd extends TreeBuilderFromMDD {

	public static final String PARAM_MANUALOMDD = "p_manual_omdd";

	public void init() {
		nodeOrder = (List)getParameter(PARAM_NODEORDER);
		root = (Integer)getParameter(PARAM_MANUALOMDD);
		initRealDepth(root);
	}

	
	/**
	 * Initialize the <b>realDepth</b> array, and <b>max_terminal</b> from an initial node, assuming regGraph is defined
	 * @param root
	 */
	public void initRealDepth(int root) {
		realDetph = new int[nodeOrder.size()+1]; //+1 for the leafs
		_initRealDepth(root);
		int next_realDepth = 0;
		for (int i = 0; i < realDetph.length; i++) {
			if (realDetph[i] == -1) {
				total_levels++;
				realDetph[i] = next_realDepth++;
			} else realDetph[i] = -2;
		}
	}

}
