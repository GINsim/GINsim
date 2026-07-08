package org.ginsim.service.tool.graphcomparator;

import java.util.HashMap;
import java.util.Map;

import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.style.StyleProvider;

/**
 * Contains the result of the comparison of two graphs graph_1 and graph_2 and
 * the
 * proper methods to handle the display of theses results
 * 
 * @param <V> a vertex V
 * @param <E> a edge E
 * @param <G> a graph g
 */
public class GraphComparatorResult<V, E extends Edge<V>, G extends Graph<V, E>> {
	private final G graph_new, graph_1, graph_2;
	private final StringBuffer log;

	public final Map<V, ComparedItemInfo<V>> comparedNodes = new HashMap<V, ComparedItemInfo<V>>();
	public final Map<E, ComparedItemInfo<E>> comparedEdges = new HashMap<E, ComparedItemInfo<E>>();

	/**
	 * Create a new result
	 */
	public GraphComparatorResult(G g1, G g2, G g_new) {
		this.graph_1 = g1;
		this.graph_2 = g2;
		this.graph_new = g_new;
		this.log = new StringBuffer(2048);
	}

	/**
	 * Return the new graph created for the comparison
	 * 
	 * @return the new graph created for the comparison
	 */
	public Graph getDiffGraph() {
		return graph_new;
	}

	/**
	 * Try to fix the edges routing
	 */
	public void setEdgeAutomatingRouting() {
		for (E e : graph_new.getEdges()) {

			E e1 = graph_1.getEdge(graph_1.getNodeByName(e.getSource().toString()),
					graph_1.getNodeByName(e.getTarget().toString()));
			if (e1 == null) {// The edge is (only or not) in the first graph. So its intermediary point are
								// right.
				EdgeAttributesReader ereader = graph_new.getEdgeAttributeReader();
				ereader.setEdge(e);
				ereader.setPoints(null); // FIXME: not sure this is the right way to do this, needs testing
				ereader.refresh();
			}
		}
	}

	/**
	 * get the content of the log
	 */
	public StringBuffer getLog() {
		return log;
	}

	/**
	 * Get the name of the first graph
	 * 
	 * @return the name of the first graph
	 */
	public String getGraph1Name() {
		if (graph_1.getGraphName().equals("default") && graph_2.getGraphName().equals("default")) {
			return "graph1";
		}
		return graph_1.getGraphName();
	}

	/**
	 * Get the name of the second graph
	 * 
	 * @return the name of the second graph
	 */
	public String getGraph2Name() {
		if (graph_1.getGraphName().equals("default") && graph_2.getGraphName().equals("default")) {
			return "graph2";
		}
		return graph_2.getGraphName();
	}

	public StyleProvider getStyleProvider() {
		return new GraphComparatorStyleProvider(graph_new, this);
	}
}
