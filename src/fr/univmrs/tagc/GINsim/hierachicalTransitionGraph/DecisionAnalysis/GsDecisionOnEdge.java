package fr.univmrs.tagc.GINsim.hierachicalTransitionGraph.DecisionAnalysis;

import java.util.List;

import fr.univmrs.tagc.GINsim.data.GsDirectedEdge;
import fr.univmrs.tagc.GINsim.hierachicalTransitionGraph.GsHierarchicalNode;

/**
 * 
 * Store a label that represent the genes that are updated between two states source, and target.
 * In a HTG, to compute the label of an edge, call this function for each couple of states corresponding to the edge.
 */
public class GsDecisionOnEdge extends GsDirectedEdge<GsHierarchicalNode> {
	
    private static final int CHANGE_NONE = 0;
	private static final int CHANGE_INCREASE = 1;
	private static final int CHANGE_DECREASE = -1;
	private static final int CHANGE_BOTH = 3;
	private int[] genesUpdated = null;

	public GsDecisionOnEdge(GsHierarchicalNode source, GsHierarchicalNode target) {
		super(source, target);
		// FIXME: new constructor for en empty decision, see init method below
	}

	/**
	 * Initialize a new label
	 * @param geneCount the count of genes in the LRG
	 */
	public void init(int geneCount) {
		if (this.genesUpdated == null) {
			this.genesUpdated = new int[geneCount];
		}
    }

	/**
	 * Compute the changes of a given edge (source, target)
	 * @param source
	 * @param target
	 */
	public void computeChange(byte[] source, byte[] target) {
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
	 * Return a string representation of the label with the names of the genes.
	 * @param nodeOrder the node order of the LRG 
	 */
	public String labelToString(List nodeOrder) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < genesUpdated.length; i++) {
			switch (genesUpdated[i]) {
			case CHANGE_NONE: break;
			case CHANGE_DECREASE: 
				if (s.length() > 0) s.append(' ');
				s.append(nodeOrder.get(i));
				s.append('-');
				break;
			case CHANGE_INCREASE: 
				if (s.length() > 0) s.append(' ');
				s.append(nodeOrder.get(i));
				s.append('+');
				break;
			case CHANGE_BOTH: 
				if (s.length() > 0) s.append(' ');
				s.append(nodeOrder.get(i));
				s.append('x');
				break;
			}
		}
		return s.toString();
	}


	/**
	 * Return a string representation of the label with the id of the genes.
	 * Use labelToString(List nodeOrder) to display their names.
	 */
	public String toString() {
		if (genesUpdated == null) {
			return "empty decision";
		}
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < genesUpdated.length; i++) {
			switch (genesUpdated[i]) {
			case CHANGE_NONE: break;
			case CHANGE_DECREASE: 
				if (s.length() > 0) s.append(' ');
				s.append(i);
				s.append('-');
				break;
			case CHANGE_INCREASE: 
				if (s.length() > 0) s.append(' ');
				s.append(i);
				s.append('+');
				break;
			case CHANGE_BOTH: 
				if (s.length() > 0) s.append(' ');
				s.append(i);
				s.append('x');
				break;
			}
		}
		return s.toString();
	}
}