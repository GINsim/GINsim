package org.ginsim.gui.shell.callbacks;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.graph.GraphSelection;
import org.ginsim.gui.shell.FrameActionManager;

/**
 * Callbacks for the "File" menu
 */
public class EditCallBack {
	
	protected static Graph copiedSubGraph = null;
	
	/**
	 * Create and populate an Edit menu
	 * 
	 * @param menu the menu
	 * @param gui  gui with a graph
	 */
	public static void addEditEntries(JMenu menu, GraphGUI<?, ?, ?> gui) {
		if (gui.canCopyPaste()) {
			Graph<?, ?> g = gui.getGraph();
			menu.add(new CopyAction(g));
			menu.add(new PasteAction(g));
		}
	}
}


class CopyAction extends AbstractAction {
	
	private final Graph<?,?> graph;
	
	public CopyAction(Graph<?,?> graph) {
		super( Txt.t("STR_Copy"));
		putValue(SHORT_DESCRIPTION,  Txt.t("STR_Copy_descr"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, FrameActionManager.MASK));
		this.graph = graph;
	}
	
	public void copy() {
		GraphSelection graphSelection = GUIManager.getInstance().getGraphGUI(graph).getSelection();
		Collection nodes, edges;
		if (graphSelection.getSelectedNodes() == null) {
			nodes = new ArrayList();
		} else {
			nodes = new ArrayList(graphSelection.getSelectedNodes());
		}
		if (graphSelection.getSelectedEdges() == null) {
			edges = new ArrayList();
			EditCallBack.copiedSubGraph = graph.getSubgraph(nodes, edges);
			return;
		}
		edges = new ArrayList(graphSelection.getSelectedEdges());
		Collection selectedEdges = graphSelection.getSelectedEdges();
		int questionHasBeenAsked = -1; //-1 = Not asked, 0 = asked and extend, 1 = asked and restraint
		for (Iterator it = selectedEdges.iterator(); it.hasNext();) {
			Edge edge = (Edge) it.next();
			questionHasBeenAsked = nodeFound(nodes, edges, edge, edge.getSource(), questionHasBeenAsked, graphSelection);
			questionHasBeenAsked = nodeFound(nodes, edges, edge, edge.getTarget(), questionHasBeenAsked, graphSelection);
		}
		
		EditCallBack.copiedSubGraph = graph.getSubgraph(nodes, edges);
	}
	
	private int nodeFound(Collection nodes, Collection edges, Edge edge, Object node, int questionHasBeenAsked, GraphSelection graphSelection) {
		if (! nodes.contains(node)) {
			if (questionHasBeenAsked == -1) {//not asked
				questionHasBeenAsked = JOptionPane.showConfirmDialog(null, Txt.t("STR_Copy_shouldExtend"),"",JOptionPane.YES_NO_OPTION);
			}
			if (questionHasBeenAsked == 0) { //extend
				nodes.add(node);
				List v = new ArrayList();
				v.add(node);
				graphSelection.addNodesToSelection(v);
			} else if (questionHasBeenAsked == 1){ //restraint
				edges.remove(edge);
			}
		}
		return questionHasBeenAsked;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		copy();
	}
}

class PasteAction<V, E extends Edge<V>> extends AbstractAction {
	
	private final Graph<V,E> graph;
	
	public PasteAction(Graph<V,E> graph) {
		super( Txt.t("STR_Paste"));
		putValue(SHORT_DESCRIPTION,  Txt.t("STR_Paste_descr"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, FrameActionManager.MASK));
		this.graph = graph;
	}
	
	public void paste() {
		graph.merge(EditCallBack.copiedSubGraph);
		GraphSelection<V,E> graphSelection = GUIManager.getInstance().getGraphGUI(graph).getSelection();
		Collection<E> edges = new ArrayList<E>();
		Map<V, V> old_to_new_nodes = new HashMap<V, V>();
		for (Object oldNode : EditCallBack.copiedSubGraph.getNodes()) {
			for (Object newNode : graph.getNodes()) {
				if (newNode.toString().startsWith(oldNode.toString())) {
					Object mappedNode = old_to_new_nodes.get(oldNode);
					if (mappedNode == null || mappedNode.toString().compareTo(newNode.toString()) < 0) {
						old_to_new_nodes.put((V) oldNode, (V) newNode);
					}
				}
			}
		}
		Collection<Edge> oldEdges = EditCallBack.copiedSubGraph.getEdges();
		for (Edge edge : oldEdges) {
			edges.add(graph.getEdge(old_to_new_nodes.get(edge.getSource()), old_to_new_nodes.get(edge.getTarget())));
		}
		
		graphSelection.unselectAll();
		graphSelection.setSelectedNodes(old_to_new_nodes.values());
		graphSelection.setSelectedEdges(edges);

	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		paste();
	}
}



