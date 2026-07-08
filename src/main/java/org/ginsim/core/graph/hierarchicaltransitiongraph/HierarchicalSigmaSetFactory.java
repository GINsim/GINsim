package org.ginsim.core.graph.hierarchicaltransitiongraph;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * HierarchicalSigmaSetFactory class
 * @author Duncan Berenguier
 */
public class HierarchicalSigmaSetFactory {

	private HierarchicalSigmaSet root;

	private SortedSet<HierarchicalNode> tmpSet;

	public HierarchicalSigmaSetFactory() {
		this.root = new HierarchicalSigmaSet(null, null);
		this.tmpSet = new TreeSet<HierarchicalNode>();
	}

	public void beginNewSigma() {
		this.tmpSet.clear();
	}

	public void addToNewSigma(HierarchicalNode label) {
		tmpSet.add(label);
	}

	public void addAllToNewSigma(Collection<HierarchicalNode> labels) {
		tmpSet.addAll(labels);
	}

	public HierarchicalSigmaSet endNewSigma() {
		HierarchicalSigmaSet parent = root;
		for (HierarchicalNode label : tmpSet) {
			parent = parent.addChild(label);
		}
		return parent;
	}

	public boolean isEmpty() {
		return root.hasChildren();
	}

	public void merge(HierarchicalNode master, HierarchicalNode slaveNode) {
		beginNewSigma();
		addAllToNewSigma(master.getSigma().getSigmaImage());
		addAllToNewSigma(slaveNode.getSigma().getSigmaImage());
		master.setSigma(endNewSigma());
	}
}
