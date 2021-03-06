package org.ginsim.service.tool.scc;

import org.colomoto.common.task.Task;
import org.colomoto.common.task.TaskListener;
import org.ginsim.common.application.LogManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.reducedgraph.NodeReducedData;
import org.ginsim.core.graph.reducedgraph.ReducedGraph;
import org.ginsim.core.graph.view.style.StyleProvider;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.ServiceStatus;
import org.kohsuke.MetaInfServices;

import java.util.List;

@MetaInfServices(Service.class)
@Alias("SCC")
@ServiceStatus(EStatus.RELEASED)
public class SCCGraphService implements Service {

    /**
     * Compute the SCC of graph.
     *
     * @param graph the graph to compute the SCC on
     * @return an object containing the resulting SCC graph
     */
    public ReducedGraph getSCCGraph(Graph graph) {
        SCCGraphAlgo algo = new SCCGraphAlgo(graph);
        try {
            return algo.call();
        } catch (Exception e) {
            LogManager.error(e);
            return null;
        }
    }

    /**
     * Compute the SCC graph in background.
     *
     * @param graph
     */
    public Task<ReducedGraph> backgroundSCCGraph(Graph graph, TaskListener listener) {
        SCCGraphAlgo algo = new SCCGraphAlgo(graph);
        algo.background(listener);
        return algo;
    }

    public List<NodeReducedData> getComponents(Graph graph) {
        StronglyConnectedComponentTask task = new StronglyConnectedComponentTask(graph);
        try {
            return task.call();
        } catch (Exception e) {
            LogManager.error(e);
            return null;
        }
    }

    public StyleProvider getStyleProvider(List<NodeReducedData> sccs, Graph graph) {
        return new ConnectivityStyleProvider(sccs, graph);
    }

}
