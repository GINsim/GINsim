package org.ginsim.core.graph.tree;

import java.util.List;

import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;

public class TreeBuilderFromRegulatoryGraph extends TreeBuilderFromMDD {
	
	public static final String PARAM_REGGRAPH = "pfrg_regGraph";
	public static final String PARAM_INITIALVERTEXINDEX = "pfrg_initialNode";
	
	protected RegulatoryGraph regGraph;


	public void init() {
		int initial_gene_id = ((Integer)getParameter(PARAM_INITIALVERTEXINDEX)).intValue();
		nodeOrder = (List<RegulatoryNode>)getParameter(PARAM_NODEORDER);
		regGraph = (RegulatoryGraph)getParameter(PARAM_REGGRAPH);

        this.ddmanager = regGraph.getMDDFactory();
		RegulatoryNode initialNode = nodeOrder.get(initial_gene_id);
		
		this.root = initialNode.getMDD(regGraph, ddmanager);
		widthPerDepth = widthPerDepth_acc = realDepth = null;
		total_levels = max_depth = 0;
		max_terminal = initialNode.getMaxValue()+1;
		initRealDepth(root);
	}
	/**
	 * Initialize the <b>realDepth</b> array, and <b>max_terminal</b> from an initial node, assuming regGraph is defined
	 * @param root
	 */
	public void initRealDepth(int root) {
		realDepth = new int[nodeOrder.size()+1]; //+1 for the leafs
		_initRealDepth(root);
		int next_realDepth = 0;
		for (int i = 0; i < realDepth.length; i++) {
			if (realDepth[i] == -1) {
				total_levels++;
				realDepth[i] = next_realDepth++;
			} else realDepth[i] = -2;
		}
	}

}