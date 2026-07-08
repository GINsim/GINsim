package org.ginsim.gui.graph;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JPanel;

import org.ginsim.commongui.SavingGUI;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;

/**
 * Deal with the GUI view of a graph: get a component, provide access to selected items and view options.
 * 
 * @author Aurelien Naldi
 *
 * @param <V> the vertex V
 * @param <E> the edge E
 * @param <G> the graph
 */
public interface GraphGUI<G extends Graph<V,E>, V, E extends Edge<V>> extends SavingGUI {

	/**
	 * graph Getter
	 *
	 * @return the underlying graph
	 */
	Graph<V, E> getGraph();
	
	/**
	 * getter graph component
	 * @return the widget showing the graph
	 */
	Component getGraphComponent();

	/**
	 * Fill the view menu with actions available on this graph GUI
	 * @param layoutMenu  Jmenu input
	 * @return JMenu
	 */
	JMenu getViewMenu(JMenu layoutMenu);

	/**
	 * Main Edit panel getter
	 * @return GUIEditor of G
	 */
	GUIEditor<G> getMainEditionPanel();

	/**
	 * Getter for tab label
	 * @return tab label edit
	 */
	String getEditingTabLabel();

	/**
	 * Node Edition panel getter
	 * @return GUIEditor of V
	 */
	GUIEditor<V> getNodeEditionPanel();

	/**
	 * GuiEditor Getter
	 * @return GUIEditor
	 */
	GUIEditor<E> getEdgeEditionPanel();

	/**
	 * Info Panel getter
	 * @return info Jpanel
	 */
	JPanel getInfoPanel();

	/**
	 * getter of EditActionManager
	 * @return the EditActionManager
	 */
	EditActionManager getEditActionManager();

	/**
	 * Does this graph type support copy/paste actions?
	 * 
	 * @return true if copy/paste is supported, false otherwise.
	 */
	boolean canCopyPaste();

	/**
	 * Returns true if this graph has not been modified since the last change
	 * 
	 * @return true if this graph has not been modified since the last change
	 */
	boolean isSaved();

	/**
	 * Set the graph has been saved or has not been modified since opened
	 * 
	 * @param isSaved boolean true if isvaved
	 */
	void setSaved( boolean isSaved);
	
	/**
	 * Indicates if the type of graph associated to the GraphGUI can be saved or not
	 * 
	 * @return true if the type of graph associated to the GraphGUI can be saved, false if not
	 */
	boolean canBeSaved();
	
	/**
	 * Pick a destination and save the graph.
	 * @return  boolean if saved
	 */
	boolean saveAs();

	/**
	 * Warn the listeners that the graph will be closed
	 */
	void fireGraphClose();

	/**
	 * Register a GraphGUIListener
	 * 
	 * @param listener graph listener
	 */
	void addGraphGUIListener(GraphGUIListener<G, V, E> listener);

	/**
	 * Remove a listener.
	 * 
	 * @param listener graph listener
	 */
	void removeGraphGUIListener(GraphGUIListener<G, V, E> listener);
	
	/**
	 * Get an object to interact with the selection
	 * 
	 * @return a graph selection
	 */
	GraphSelection<V, E> getSelection();

	/**
	 * test if edit allowed
	 * @return boolean if edit allowed
	 */
	boolean isEditAllowed();
	
	/**
	 * Update selection to reflect the internal selection object
	 */
	void selectionChanged();
	
	/**
	 * Force a repaint (should be used only by layout actions)
	 */
	void repaint();

	/**
	 * Zoom level getter
	 * @return zoom level as double value
	 */
	double getZoomLevel();
}
