package org.ginsim.service.tool.scc;

import org.colomoto.common.task.AbstractTask;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.reducedgraph.NodeReducedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Search for strongly connected components.
 *
 * @author Cecile Menahem
 * @author Aurelien Naldi
 */
public class StronglyConnectedComponentTask extends AbstractTask<List<NodeReducedData>> {
	private final Graph graph;

	/**
	 * get ready to run.
	 *
	 * @param graph the graph
	 */
	public StronglyConnectedComponentTask(Graph graph) {
		this.graph = graph;
	}

    @Override
    protected List<NodeReducedData> performTask() {
		List<Collection<?>> jcp = graph.getStronglyConnectedComponents();
		List<NodeReducedData> components = new ArrayList<NodeReducedData>(jcp.size());
		int id = 0;
		for (Collection<?> set: jcp) {
			String sid;
			if (set.size() == 1) {
				sid = null;
			} else {
				sid = "cc-"+id++;
			}
			NodeReducedData node = new NodeReducedData(sid, set);
			components.add(node);
		}
		return components;
	}
}
