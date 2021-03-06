package org.ginsim.core.graph.hierarchicaltransitiongraph;

import java.util.List;

import org.colomoto.biolqm.NodeInfo;
import org.ginsim.common.application.Txt;
import org.ginsim.common.utils.ToolTipsable;
import org.ginsim.core.graph.Edge;


/**
 * 
 * Store a label that represent the genes that are updated between two states source, and target.
 * In a HTG, to compute the label of an edge, call this function for each couple of states corresponding to the edge.
 *
 * @author Duncan Berenguier
 */
public class DecisionOnEdge extends Edge<HierarchicalNode> implements ToolTipsable {
	
    private static final int CHANGE_NONE = 0;
	private static final int CHANGE_INCREASE = 1;
	private static final int CHANGE_DECREASE = -1;
	private static final int CHANGE_BOTH = 3;
	private int[] genesUpdated = null;
	private List<NodeInfo> nodeOrder;

	public DecisionOnEdge( HierarchicalTransitionGraph g, HierarchicalNode source, HierarchicalNode target, List<NodeInfo> nodeOrder) {
		super(g, source, target);
        this.nodeOrder = nodeOrder;
	}

	/**
	 * Compute the changes of a given edge (source, target)
	 * @param source
	 * @param target
	 */
	public void computeChange(byte[] source, byte[] target) {
        if (this.genesUpdated == null) {
            this.genesUpdated = new int[source.length];
        }
		for (int i = 0; i < source.length; i++) {
			int change = target[i] - source[i];
			if (change != 0) {
				int currentChange = genesUpdated[i];
				if (currentChange != change) {
					if (currentChange == CHANGE_NONE) {
						genesUpdated[i] = change;
					} else if (currentChange != CHANGE_BOTH) {
						genesUpdated[i] = CHANGE_BOTH;
					}
				}
			}
		}
	}
	
	/**
	 * Return a string representation of the label with the name of the genes.
	 */
	public String toString() {
		if (genesUpdated == null) {
			return "";
		}
		StringBuffer s = new StringBuffer();
		s.append(Txt.t("STR_htg_decision_analysis_label"));
		s.append(": ");
		for (int i = 0; i < genesUpdated.length; i++) {
			switch (genesUpdated[i]) {
			case CHANGE_NONE: break;
			case CHANGE_DECREASE: 
				if (s.length() > 0) s.append(' ');
				s.append(nodeOrder.get(i).getNodeID());
				s.append('-');
				break;
			case CHANGE_INCREASE: 
				if (s.length() > 0) s.append(' ');
				s.append(nodeOrder.get(i).getNodeID());
				s.append('+');
				break;
			case CHANGE_BOTH: 
				if (s.length() > 0) s.append(' ');
				s.append(nodeOrder.get(i).getNodeID());
				s.append('\u00B1');
				break;
			}
		}
		return s.toString();
	}

	@Override
	public String toToolTip() {
		return toString();
	}
}
