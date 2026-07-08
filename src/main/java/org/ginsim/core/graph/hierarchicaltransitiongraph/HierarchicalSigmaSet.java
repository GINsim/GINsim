package org.ginsim.core.graph.hierarchicaltransitiongraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ginsim.common.application.LogManager;

/**
 * class HierarchicalSigmaSet
 * @author Duncan Berenguier
 */
public class HierarchicalSigmaSet {

	/**
	 * The parent node
	 */
	private HierarchicalSigmaSet parent;
	
	/**
	 * Map&lt;HierarchicalNode, HierarchicalSigmaSet&gt;
	 */
	private Map<HierarchicalNode, HierarchicalSigmaSet> children;
	
	private HierarchicalNode label;
	
	private HierarchicalNode unrecoverable;
	
 	public HierarchicalSigmaSet(HierarchicalNode label, HierarchicalSigmaSet parent) {
 		this.label = label;
 		this.parent = parent;
 		this.children = null;
 		this.unrecoverable = null;
 	}
 	
 	public HierarchicalSigmaSet getChild(HierarchicalNode label) {
 		if (children == null) return null;
 		return (HierarchicalSigmaSet) children.get(label);
 	}
 	
 	public HierarchicalSigmaSet addChild(HierarchicalNode label) {
 		if (children == null) {
 			children = new HashMap<HierarchicalNode, HierarchicalSigmaSet>();
 		}
 		HierarchicalSigmaSet child = (HierarchicalSigmaSet) children.get(label);
 		if (child == null) {
 			child = new HierarchicalSigmaSet(label, this);
 			this.children.put(label, child);
 		}
 		return child;
 	}

	public boolean hasChildren() {
		return children != null;
	}

	/**
	 * @param unrecoverable the unrecoverable component associated to this SigmaSet to set
	 * @param nodeSet
	 * @param htg 
	 */
	public void setUnrecoverable(HierarchicalNode unrecoverable, Collection<HierarchicalNode> nodeSet, HierarchicalSigmaSetFactory sigmaSetFactory, HierarchicalTransitionGraph htg) {
		if (this.unrecoverable != null) {
			this.unrecoverable.merge(unrecoverable, nodeSet, sigmaSetFactory, htg);
		} else {
			this.unrecoverable = unrecoverable;			
		}
	}

	/**
	 * @return the unrecoverable component associated to this SigmaSet
	 */
	public HierarchicalNode getUnrecoverable() {
		return unrecoverable;
	}
	
	public List<HierarchicalNode> getSigmaImage() {
		LinkedList<HierarchicalNode> path = new LinkedList<HierarchicalNode>();
		if (this.parent == null) {
			LogManager.error( this + " appel sur la root ?");
			return path;
		}		
		HierarchicalSigmaSet current = this;
		do {
			path.addFirst(current.label);
			current = current.parent;
		} while (current.parent != null);
		return path;
	}

	/**
	 * Indicates if the SigmaSet is a singleton (contains only one element), that is, if this HierarchicalSigmaSet's parent is not the root of the factory, ie. it has a parent
	 * The root is always empty.
	 * @return
	 */
	public boolean isSingleton() {
		return parent == null || parent.parent == null;
	}
	
	public String pathToString() {
		StringBuffer sb = new StringBuffer();
		HierarchicalSigmaSet current = this;
		if (current.parent == null) {
			sb.append("<root>");
			return sb.toString();
		}
		do {
			sb.append(current+",");
			current = current.parent;
		} while (current.parent != null);
		return sb.toString();
	}
	public String toString() {
		return "<"+label+","+unrecoverable+">";
	}
	
}
