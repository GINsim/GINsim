package org.ginsim.core.graph;


/**
 * Listen to graph edit events.
 * @param <G>  graph model
 * @author Aurelien Naldi
 */
public interface GraphListener<G extends GraphModel<?,?>> {
	/**
	 * change on graph change
	 * @param g the graph
	 * @param type graph change type
	 * @param data object data to change
	 * @return acacade of event GraphEventCascade
	 */
	GraphEventCascade graphChanged(G g, GraphChangeType type, Object data);
	
}
