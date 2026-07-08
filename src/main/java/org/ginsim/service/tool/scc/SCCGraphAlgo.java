package org.ginsim.service.tool.scc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.colomoto.common.task.AbstractTask;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.reducedgraph.NodeReducedData;
import org.ginsim.core.graph.reducedgraph.ReducedGraph;



/**
 * Search for strongly connected components, and build the SCC graph.
 *
 * @author Cecile Menahem
 * @author Aurelien Naldi
 */
public class SCCGraphAlgo extends AbstractTask<ReducedGraph> {
	private final Graph graph;

	/**
	 * get ready to run.
	 * 
	 * @param graph
	 */
	public SCCGraphAlgo(Graph graph) {
		this.graph = graph;
	}

    @Override
    protected ReducedGraph performTask() throws Exception {
        StronglyConnectedComponentTask sccTask = new StronglyConnectedComponentTask(graph);
		List<NodeReducedData> components = sccTask.call();
		ReducedGraph reducedGraph = constructGraph(components);
		return reducedGraph;
	}

	/**
	 * Construct a reducedGraph from a list of components
	 * @param components
	 * @return the reducedGraph
	 */
	private ReducedGraph constructGraph(List<NodeReducedData> components) {
		boolean flagIn = false;
		List<NodeReducedData> newcomponents = new ArrayList<NodeReducedData>(components.size());
		Collection<Edge<?>> exists = null;
		ReducedGraph reducedGraph = GSGraphManager.getInstance().getNewGraph( ReducedGraph.class, (Graph)graph);
		HashMap<Object, NodeReducedData> nodeParentSCC = new HashMap<Object, NodeReducedData>(); //Map the a node to its parent SCC
		
		int totalComplexComponents = 0;
		for (NodeReducedData component : components) {		//For each component
			reducedGraph.addNode(component);
			if (!component.isTrivial()) {
				totalComplexComponents++;
			}
			for (Object node : component.getContent()) {	//  for each nodes in the component
				nodeParentSCC.put(node, component);		//     add the node in the map nodeParentSCC as a key, with the current SCC node as value

			}
		}

		for (NodeReducedData component : components) {		//For each component
			for (Object node : component.getContent()) {	//  for each nodes in the component
				Collection<Edge<?>> outgoingEdges = graph.getOutgoingEdges(node);
				for (Edge edge: outgoingEdges) {									//    for each edge outgoing from this node
					Object targetNode = edge.getTarget();
					NodeReducedData targetParent = nodeParentSCC.get(targetNode);
					if (nodeParentSCC.get(targetNode) != component) {            //      if the target of the edge is not in the SCC
						exists = reducedGraph.getEdges();
						flagIn = false;
						for (Edge newedge : exists) {
							if (newedge.getSource().equals(component) && newedge.getTarget().equals(targetParent)) {
								flagIn = true;
								break;
							}
						}
						if (!flagIn) {
							// NodeReducedData newId_source = new NodeReducedData(component.getId().replaceAll("cc-", "ct#-"), component.getContent());
							reducedGraph.addEdge(component, targetParent);
						}
					}
				}
			}
		}
		return reducedGraph;
	}
}
