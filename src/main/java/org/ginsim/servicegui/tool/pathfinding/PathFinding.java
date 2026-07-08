package org.ginsim.servicegui.tool.pathfinding;

import java.util.ArrayList;
import java.util.List;

import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;

/**
 * A class to find a path in any graph
 */
public class PathFinding extends Thread {

	private List<Object> search;
	private ResultHandler resultHandler;
	private Graph graph;

	/**
	 * Create a new thread that will search a path between start and end
	 * The graphManager is used to find the outgoingEdges from a node.
	 * The resultHandler is informed of the progression during the run() and of the results when the run() is finish.
	 * 
	 * @param resultHandler  the result handler
	 * @param graph the graph
	 * @param search  a node
	 */
	public PathFinding(ResultHandler resultHandler, Graph graph, List<Object> search) {
		this.resultHandler = resultHandler;
		this.search = search;
		this.graph = graph;
	}

	public void run() {
		
		List<Object> nodes = null;
		Object prev = null;
		for (Object o: search) {
			if (prev == null) {
				prev = o;
				continue;
			}
			List<Edge> path = graph.getShortestPath(prev, o);
			
			if (path == null) {
				resultHandler.setPath(null);
				return;
			}
			
			if (nodes == null) {
				nodes = new ArrayList<Object>(path.size()+1);
				nodes.add(prev);
			}
			for (Edge e: path) {
				nodes.add(e.getTarget());
			}
			prev = o;
		}
		resultHandler.setPath(nodes);
	}
}
