package org.ginsim.core.graph.view.css;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;


public class CascadingStyleSheetManager {
	private Map old_nodes, old_edges;
	public boolean shouldStoreOldStyle;

	public CascadingStyleSheetManager(boolean shouldStoreOldStyle) {
		old_nodes = new HashMap();
		old_edges = new HashMap();
		this.shouldStoreOldStyle = shouldStoreOldStyle;
	}

	/**
	 * Apply a selector on an edge using its attribute reader.
	 * If shouldStoreOldStyle is true it save the old style too.
	 * @param sel selector
	 * @param edge  a edge
	 * @param areader a reader EdgeAttributesReader
	 */
	public void applyOnEdge(Selector sel, Object edge, EdgeAttributesReader areader) {
		if (shouldStoreOldStyle) old_edges.put(edge, new CSSEdgeStyle(areader));
		sel.applyStyleForEdge(edge, areader);
	}

	/**
	 * Apply a selector on a node using its attribute reader.
	 * If shouldStoreOldStyle is true it save the old style too.
	 * @param sel a selector
	 * @param node a object node
	 * @param areader NodeAttributesReader reader
	 */
	public void applyOnNode(Selector sel, Object node, NodeAttributesReader areader) {
		if (shouldStoreOldStyle) old_nodes.put(node, new CSSNodeStyle(areader));
		sel.applyStyleForNode(node, areader);
	}

	/**
	 * Apply a style on an edge using its attribute reader.
	 * If shouldStoreOldStyle is true it save the old style too.
	 * @param style  css style
	 * @param edge a object edge
	 * @param areader EdgeAttributesReader reader
	 */
	public void applyOnEdge(CSSEdgeStyle style, Object edge, EdgeAttributesReader areader) {
		if (shouldStoreOldStyle) old_edges.put(edge, new CSSEdgeStyle(areader));
		style.apply(areader);
	}

	/**
	 * Apply a style on an edge using its attribute reader.
	 * If shouldStoreOldStyle is true it save the old style too.
	 * @param style  the style
	 * @param node the node
	 * @param areader a NodeAttributesReader  reader
	 */
	public void applyOnNode(CSSNodeStyle style, Object node, NodeAttributesReader areader) {
		if (shouldStoreOldStyle) old_nodes.put(node, new CSSNodeStyle(areader));
		style.apply(areader);
	}


	/**
	 * Apply all the selectors in cascade on each edges
	 *
	 * If you want to apply only one selector, use applySelectorOnEdges instead.
	 *
	 * @param selectors an ordered list of selector to apply on each edges
	 * @param edges a collection of edges to apply the selectors on
	 * @param ereader an edge AttributesReader
	 */
	public void applySelectorsOnEdges(List selectors, Collection edges, EdgeAttributesReader ereader) {
		for (Iterator it_edges = edges.iterator(); it_edges.hasNext();) {			//For each edges
			Object edge = it_edges.next();
			ereader.setEdge((Edge)edge);
			CSSEdgeStyle style = new CSSEdgeStyle(ereader);								//  get the style
			if (shouldStoreOldStyle) old_edges.put(edge, style.clone());					//  save a copy if needed
			for (Iterator it_sel = selectors.iterator(); it_sel.hasNext();) {		//  For each selector
				Selector sel = (Selector) it_sel.next();
				style.merge(sel.getStyleForEdge(edge));								//     update the style using the selector
			}
			style.apply(ereader);													//  apply the style to the edge.
		}
	}

	/**
	 * Apply all the selectors in cascade on each nodes
	 *
	 * If you want to apply only one selector, use applySelectorOnNodes instead.
	 *
	 * @param selectors an ordered list of selector to apply on each nodes
	 * @param nodes a collection of nodes to apply the selectors on
	 * @param vreader a node AttributesReader
	 */
	public void applySelectorsOnNodes(List selectors, Collection nodes, NodeAttributesReader vreader) {
		for (Iterator it_nodes = nodes.iterator(); it_nodes.hasNext();) {			//For each nodes
			Object node = it_nodes.next();
			vreader.setNode(node);
			CSSNodeStyle style = new CSSNodeStyle(vreader);							//  get the style
			if (shouldStoreOldStyle) old_nodes.put(node, style.clone());					//  save a copy if needed
			for (Iterator it_sel = selectors.iterator(); it_sel.hasNext();) {		//  For each selector
				Selector sel = (Selector) it_sel.next();
				style.merge(sel.getStyleForNode(node));								//     update the style using the selector
			}
			style.apply(vreader);													//  apply the style to the node.
		}
	}

	/**
	 * Apply one selector on each edges from a collection
	 *
	 * If you want to apply several selector, use applySelectorsOnEdges instead.
	 *
	 * @param sel the selector to apply on each edges
	 * @param edges a collection of edges to apply the selectors on
	 * @param ereader an edge AttributesReader
	 */
	public void applySelectorOnEdges(Selector sel, Collection edges, EdgeAttributesReader ereader) {
		for (Iterator it_edges = edges.iterator(); it_edges.hasNext();) {			//For each edge
			Object edge = it_edges.next();
			ereader.setEdge((Edge)edge);
			if (shouldStoreOldStyle) {
				old_edges.put(edge, new CSSEdgeStyle(ereader));					//  save a copy if needed
			}
			CSSEdgeStyle style = (CSSEdgeStyle)sel.getStyleForEdge(edge);
			if (style != null) {
				style.apply(ereader);		//  apply the style to the edge.
			}
		}
		ereader.refresh();
	}

	/**
	 * Apply one selector on each nodes from a collection
	 *
	 * If you want to apply several selector, use applySelectorsOnNodes instead.
	 *
	 * @param sel the selector to apply on each nodes
	 * @param nodes  a collection of nodes to apply the selectors on
	 * @param vreader an node AttributesReader
	 */
	public void applySelectorOnNodes(Selector sel, Collection nodes, NodeAttributesReader vreader) {
		for (Iterator it_nodes = nodes.iterator(); it_nodes.hasNext();) {			//For each node
			Object node = it_nodes.next();
			vreader.setNode(node);
			if (shouldStoreOldStyle) {
				old_nodes.put(node, new CSSNodeStyle(vreader));					//  save a copy if needed
			}
			CSSNodeStyle style = (CSSNodeStyle)sel.getStyleForNode(node);
			if (style != null) {
				style.apply(vreader);	//  apply the style to the node.
			}
		}
		vreader.refresh();
	}

	/**
	 * Restore an edge if it has been previously saved.
	 * @param edge
	 * @param areader a edge attributesReader (must be set to the right edge)
	 */
	public void restoreEdge(Object edge, EdgeAttributesReader areader) {
		((CSSStyle)old_edges.get(edge)).apply(areader);
		areader.refresh();
	}

	/**
	 * Restore a node if it has been previously saved.
	 * @param node
	 * @param areader a node attributesReader (must be set to the right vertex)
	 */
	public void restoreNode(Object node, NodeAttributesReader areader) {
		((CSSStyle)old_nodes.get(node)).apply(areader);
		areader.refresh();
	}

	/**
	 * Restore all the edges previously saved.
	 * @param areader an edge attributesReader
	 */
	public void restoreAllEdges(EdgeAttributesReader areader) {
		for (Iterator it_edges = old_edges.keySet().iterator(); it_edges.hasNext();) {
			Object edge = it_edges.next();
			areader.setEdge((Edge)edge);
			((CSSStyle)old_edges.get(edge)).apply(areader);
		}
		areader.refresh();
	}

	/**
	 * Restore all the nodes previously saved.
	 * @param areader a node attributesReader
	 */
	public void restoreAllNodes(NodeAttributesReader areader) {
		for (Iterator it_nodes = old_nodes.keySet().iterator(); it_nodes.hasNext();) {
			Object node = it_nodes.next();
			areader.setNode(node);
			((CSSStyle)old_nodes.get(node)).apply(areader);
		}
		areader.refresh();
	}

	/**
	 * Restore all the edges from the collection edges.
	 * @param edges a collection of edges to restore
	 * @param areader a edge attributesReader
	 */
	public void restoreAllEdges(Collection edges, EdgeAttributesReader areader) {
		for (Iterator it_edges = edges.iterator(); it_edges.hasNext();) {
			Object edge = it_edges.next();
			areader.setEdge((Edge)edge);
			CSSStyle style = (CSSStyle)old_edges.get(edge);
			if (style != null) style.apply(areader);
		}
		areader.refresh();
	}

	/**
	 * Restore all the nodes from the collection nodes.
	 * @param nodes a collection of nodes to restore
	 * @param areader a node attributesReader
	 */
	public void restoreAllNodes(Collection nodes, NodeAttributesReader areader) {
		for (Iterator it_nodes = nodes.iterator(); it_nodes.hasNext();) {
			Object node = it_nodes.next();
			areader.setNode(node);
			CSSStyle style = (CSSStyle)old_nodes.get(node);
			if (style != null) style.apply(areader);
		}
		areader.refresh();
	}

	/**
	 * store all the nodes from the collection nodes.
	 * @param nodes a collection of nodes to store
	 * @param areader a node attributesReader
	 */
	public void storeAllNodes(Collection nodes, NodeAttributesReader areader) {
		for (Iterator it_nodes = nodes.iterator(); it_nodes.hasNext();) {
			Object node = it_nodes.next();
			areader.setNode(node);
			CSSNodeStyle style = new CSSNodeStyle(areader);
			old_nodes.put(node, style);
		}
	}

	/**
	 * store all the nodes from the collection nodes.
	 * @param edges a collection of nodes to store
	 * @param areader a node attributesReader
	 */
	public void storeAllEdges(Collection edges, EdgeAttributesReader areader) {
		for (Iterator it_edges = edges.iterator(); it_edges.hasNext();) {
			Object edge = it_edges.next();
			areader.setEdge((Edge)edge);
			CSSEdgeStyle style = new CSSEdgeStyle(areader);
			old_edges.put(edge, style);
		}
	}

	public Map getOldNodes() {
		return old_nodes;
	}
	public Map getOldEdges() {
		return old_edges;
	}
}
