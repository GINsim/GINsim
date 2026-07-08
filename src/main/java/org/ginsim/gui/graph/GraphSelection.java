package org.ginsim.gui.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;
import org.ginsim.gui.shell.editpanel.SelectionType;

/**
 * Manage the selection for a GraphGUI
 * 
 * @author Aurelien Naldi
 * @author Duncan Berenguier
 *
 * @param <V>  vertex
 * @param <E> edge
 */
public class GraphSelection<V, E extends Edge<V>> {

	private SelectionType type = SelectionType.SEL_NONE;
	private int nodeCount;
	private int edgeCount;

	private final GraphGUI<Graph<V,E>, V, E> gui;
	private List<V> nodes = null;
	private List<E> edges = null;

	public GraphSelection(GraphGUI gui) {
		this.gui = gui;
		updateType(true);
	}

	public SelectionType getSelectionType() {
		return type;
	}

	public List<E> getSelectedEdges() {
		return edges;
	}
	public List<V> getSelectedNodes() {
		return nodes;
	}


	/**
	 * Set the selection.
	 * This method should only be called by the GraphGUI to propagate interactive selection changes
	 *  
	 * @param nodes
	 * @param edges
	 */
	public void backendSelectionUpdated(List<V> nodes, List<E> edges) {
		this.nodes = nodes;
		this.edges = edges;

		updateType(true);
	}

	private void updateType(boolean fromBackend) {
		if (edges == null || edges.size() == 0) {
			edges = null;
			edgeCount = 0;
		} else {
			edgeCount = edges.size();
		}

		if (nodes == null || nodes.size() == 0) {
			nodes = null;
			nodeCount = 0;
		} else {
			nodeCount = nodes.size();
		}

		type = findType();
		
		if (!fromBackend) {
			gui.selectionChanged();
		}
	}

	/**
	 * @return the current type of selection
	 */
	private SelectionType findType() {
		if (edgeCount == 0) {
			if (nodeCount == 0) {
				return SelectionType.SEL_NONE;
			}
			if (nodeCount == 1) {
				return SelectionType.SEL_NODE;
			} 
			return SelectionType.SEL_MULTIPLE;
		} 

		if (edgeCount == 1) {
			if (nodeCount == 0) {
				return SelectionType.SEL_EDGE;
			}
			return SelectionType.SEL_MULTIPLE;
		}
		return SelectionType.SEL_MULTIPLE;
	}

	/**
	 * Select a single node.
	 * 
	 * @param node
	 */
	public void selectNode(V node) {
		nodes = new ArrayList<V>();
		edges = null;
		nodes.add(node);
		updateType(false);
	}

	/**
	 * Select a single edge
	 * @param edge
	 */
	public void selectEdge(E edge) {
		nodes = null;
		edges = new ArrayList<E>();
		edges.add(edge);
		updateType(false);
	}


	/**
	 * Select all the edges and 
	 */
	public void selectAll() {
		nodes = new ArrayList<V>(gui.getGraph().getNodes());
		edges = new ArrayList<E>(gui.getGraph().getEdges());
		
		updateType(false);
	}
	
	/**
	 * Select all the nodes
	 */
	public void selectAllNodes() {
		nodes = new ArrayList<V>(gui.getGraph().getNodes());
		edges = null;
		updateType(false);
	}

	/**
	 * Select all the edges
	 */
	public void selectAllEdges() {
		nodes = null;
		edges = new ArrayList<E>(gui.getGraph().getEdges());
		updateType(false);
	}
	
	/**
	 * Unselect all the edges and nodes
	 */
	public void unselectAll() {
		nodes = null;
		edges = null;
		updateType(false);
	}

	/**
	 * Unselect all the nodes
	 */
	public void unselectAllNodes() {
		nodes = null;
		updateType(false);
	}

	/**
	 * Unselect all the edges
	 */
	public void unselectAllEdges() {
		edges = null;
		updateType(false);
	}

	public void unselectNode(V node) {
		if (nodes != null) {
			nodes.remove(node);
			updateType(false);
		}
	}
	
	public void unselectEdge(E edge) {
		if (edges != null) {
			edges.remove(edge);
			updateType(false);
		}
	}
	
	public void addNodeToSelection(V node) {
		if (nodes == null) {
			nodes = new ArrayList<V>();
		}
		nodes.add(node);
		updateType(false);
	}

	public void addEdgeToSelection(E edge) {
		if (edges == null) {
			edges = new ArrayList<E>();
		}
		edges.add(edge);
		updateType(false);
	}

	/**
	 * Add the nodes in l to the list of selected nodes
	 * @param l the list of nodes to select
	 */
	public void addNodesToSelection(Collection<V> l) {
		if (nodes == null) {
			nodes = new ArrayList<V>(l);
		} else {
			nodes.addAll(l);
		}
		updateType(false);
	}

	/**
	 * Add the edges in l to the list of selected edges
	 * @param l the list of edges to select
	 */
	public void addEdgesToSelection(Collection<E> l) {
		if (edges == null) {
			edges = new ArrayList<E>(l);
		} else {
			edges.addAll(l);
		}
		updateType(false);
	}

	
	/**
	 * Set the list of selected nodes to the nodes in the list l
	 * @param l the list of nodes to select
	 */
	public void setSelectedNodes(Collection<V> l) {
		nodes = new ArrayList<V>(l);
		updateType(false);
	}
	
	/**
	 * Set the list of selected edges to the edges in the list l
	 * @param l the list of edges to select
	 */
	public void setSelectedEdges(Collection<E> l) {
		edges = new ArrayList<E>(l);
		updateType(false);
	}

	/**
	 * Set the selection
	 * 
	 * @param nodes
	 * @param edges
	 */
	public void setSelection(Collection<V> nodes, Collection<E> edges) {
		this.nodes = new ArrayList<V>(nodes);
		this.edges = new ArrayList<E>(edges);
		updateType(false);
	}


	
	/**
	 * Add all the incoming nodes of the selected nodes to the list of selected nodes
	 */
	public void extendSelectionToIncomingNodes() {

		if (nodes == null) {
			return;
		}
		ArrayList<V> new_nodes = new ArrayList<V>(nodes);
		for (V node : new_nodes) {
			for (E edge : gui.getGraph().getIncomingEdges(node)) {
				nodes.add(edge.getSource());
			}
		}
		updateType(false);
	}

	/**
	 * Add all the incoming edges of the selected nodes to the list of selected edges
	 */
	public void extendSelectionToIncomingEdges() {
		if (nodes == null) {
			return;
		}
		for (V node : nodes) {
			addEdgesToSelection(gui.getGraph().getIncomingEdges(node));
		}
		updateType(false);
	}

	/**
	 * Add all the edges between the selected nodes to the list of selected edges
	 */
	public void extendSelectionToInternalEdges() {
		if (nodes == null) {
			return;
		}
		for (V node : nodes) {
			for (E edge: gui.getGraph().getIncomingEdges(node)) {
				if (nodes.contains( edge.getSource())) {
					addEdgeToSelection(edge);
				}
			}
		}
		updateType(false);
	}

	/**
	 * Add all the outgoing nodes of the selected nodes to the list of selected nodes
	 */
	public void extendSelectionToOutgoingNodes() {
		
		if (nodes == null) {
			return;
		}
		List<V> new_nodes = new ArrayList<V>(nodes);
		for (V node : new_nodes) {
			for (E edge : gui.getGraph().getOutgoingEdges(node)) {
				nodes.add(edge.getTarget());
			}
		}
		updateType(false);
	}

	/**
	 * Add all the outgoing edges of the selected nodes to the list of selected edges
	 */
	public void extendSelectionToOutgoingEdges() {
		if (nodes == null) {
			return;
		}
		for (V node : nodes) {
			addEdgesToSelection(gui.getGraph().getOutgoingEdges(node));
		}
		updateType(false);
	}

	/**
	 * Invert the selection of nodes and edges
	 */
	public void invertSelection() {
		invertNodesSelection();
		invertEdgesSelection();
		updateType(false);
	}
		
	/**
	 * Invert the selection of nodes
	 */
	public void invertNodesSelection() {
		ArrayList<V> new_nodes = new ArrayList<V>(gui.getGraph().getNodes());
		if (nodes!= null) {
			new_nodes.removeAll(nodes);
		}
		nodes = new_nodes;
		updateType(false);
	}

	/**
	 * Invert the selection of edges
	 */
	public void invertEdgesSelection() {
		ArrayList<E> new_edges = new ArrayList<E>(gui.getGraph().getEdges());
		if (edges != null) {
			new_edges.removeAll(edges);
		}
		edges = new_edges;
		updateType(false);
	}

}
