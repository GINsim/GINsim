package org.ginsim.gui.graph;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;

/**
 * GUI-side helper for a graph type.
 * 
 * A helper is a singleton dedicated to a specific graph type.
 * It provides GUI components required to visualize and edit a graph.
 * 
 * Implementors must be called with the same pattern: 'GraphClassName' in order to have
 * the GraphGUIHelperFactory able to create their instances through introspective access
 * 
 * Implementations must be annotated "@MetaInfServices( GraphGUIHelper.class)" to be discovered at runtime
 *
 * @author Aurelien Naldi
 *
 * @param <G> the graph
 * @param <V> the vertex
 * @param <E> the edge
 */
public interface GraphGUIHelper<G extends Graph<V,E>, V, E extends Edge<V>> {

	/**
	 * Create an edition panel for the graph itself.
	 * 
	 * @param graph the graph requiring the panel
	 * @return
	 */
	GUIEditor<G> getMainEditionPanel(G graph);
	
	/**
	 * Retrieve the Title used for the selection edition tab
	 * 
	 * @param graph the graph requiring the panel
	 * @return the string editable
	 */
	String getEditingTabLabel(G graph);
	
	/**
	 * Create an edition panel for nodes.
	 * 
	 * @param graph the graph requiring the panel
	 * @return
	 */
	GUIEditor<V> getNodeEditionPanel(G graph);
	
	/**
	 * Create an edition panel for edges.
	 * 
	 * @param graph the graph requiring the panel
	 * @return 
	 */
	GUIEditor<E> getEdgeEditionPanel(G graph);
	
	/**
	 * Create an Information Panel
	 * 
	 * @param graph the graph requiring the panel
	 * @return an Information Panel
	 */
	JPanel getInfoPanel( G graph);
	
	/**
	 * Get the graph class the helper helps to manage
	 * 
	 * @return the graph class the helper helps to manage
	 */
	Class<G> getGraphClass();
	
	/**
	 * @param graph the edited graph
	 * @return the list of actions or null if none are available
	 */
	List<EditAction> getEditActions(G graph);
	
	/**
	 * Provide the file filter to apply to a file chooser
	 * 
	 * @return the file filter to apply to a file chooser
	 */
	FileFilter getFileFilter();
	
	/**
	 * Create a panel containing the option for graph saving 
	 * 
	 * @param graph the edited graph
	 */
	JPanel getSaveOptionPanel( G graph);
	
	/**
	 * Does this graph type support copy/paste actions?
	 * 
	 * @param graph the graph
	 * @return true if copy/paste is supported, false otherwise.
	 */
	boolean canCopyPaste(G graph);
}
