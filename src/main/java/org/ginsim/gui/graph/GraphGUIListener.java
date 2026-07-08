package org.ginsim.gui.graph;

import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.GraphChangeType;

/**
 * Interface for objects which listen for Graph GUI events.
 * 
 * @author Aurelien Naldi
 *
 * @param <G>  the graph
 * @param <V>  the vertex
 * @param <E>  the edge
 */
public interface GraphGUIListener<G extends Graph<V, E>, V, E extends Edge<V>> {

	/**
	 * The selection has changed.
	 * @param gui the gui
	 */
	void graphSelectionChanged(GraphGUI<G, V, E> gui);
	
	/**
	 * The GUI was closed.
	 * 
	 * @param gui
	 */
	void graphGUIClosed(GraphGUI<G, V, E> gui);
	
	/**
	 * The graph data was changed.
	 * 
	 * This method is used to forward change events (see GraphListener) to the GUI.
	 * Listeners should react by refreshing the GUI when needed but should not change the graph.
	 * 
	 * @param g the graph
	 * @param type the type
	 * @param data  object data
	 */
	void graphChanged(G g, GraphChangeType type, Object data);

}
