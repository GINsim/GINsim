package org.ginsim.service.tool.graphcomparator;

import org.colomoto.common.task.AbstractTask;
import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.view.NodeAttributesReader;


/**
 * Compare two Graphs.
 * Create a new graph with all components from the two compared graphs and track
 * components which are specific to one of them, or changes between common components.
 * This abstract class provides the common mechanism, extensions support specific graph types.
 * @param <E>  edge
 * @param <V>  vertex
 * @param <G>  graph
 * @author Duncan Berenguier
 * @author Aurelien Naldi
 * @since January 2009
 */
public abstract class GraphComparator<V, E extends Edge<V>, G extends Graph<V,E>> extends AbstractTask<GraphComparatorResult<V,E,G>> {
	
	public final G graph_new;
	public final G graph_1;
	public final G graph_2;

	protected GraphComparatorResult<V,E,G> result;

	protected GraphComparator(G g1, G g2, G g_new) {
        this.graph_1 = g1;
        this.graph_2 = g2;
        this.graph_new = g_new;

        if (g1 == null || g2 == null || g_new == null) {
            throw new RuntimeException("Invalid parameters for GraphComparator");
        }

		result = new GraphComparatorResult<V, E, G>(g1, g2, g_new);
	}

    /**
     * Build the basic topology for the diff graph (node+edges) by calling others functions
     *  1) addNodesFromGraph on both graphs
     *  2) setNodesColor
     *  3) addEdgesFromGraph on each node on both graphs
     */
    @Override
    protected GraphComparatorResult<V,E,G> performTask() throws Exception {
		log("Comparing graphs : \n");
		setDiffGraphName();
		log("\n");

        NodeAttributesReader<V> nreader = graph_new.getNodeAttributeReader();
        NodeAttributesReader<V> nreader_1 = graph_1.getNodeAttributeReader();
        NodeAttributesReader<V> nreader_2 = graph_2.getNodeAttributeReader();
        for (V v1: graph_1.getNodes()) {
            V v2 = findMatchingNode(v1, graph_2);
            V v = copyNode(graph_1, v1);
            ComparedItemInfo<V> info = new ComparedItemInfo<V>(v, v1, v2);
            copyNodeView(nreader, v, nreader_1, v1);
            result.comparedNodes.put(v, info);
        }
        for (V v2: graph_2.getNodes()) {
            V v1 = findMatchingNode(v2, graph_1);
            if (v1 == null) {
                V v = copyNode(graph_2, v2);
                ComparedItemInfo<V> info = new ComparedItemInfo<V>(v, v1, v2);
                copyNodeView(nreader, v, nreader_2, v2);
                result.comparedNodes.put(v, info);
            }
        }

        for (E e1: graph_1.getEdges()) {
            E e2 = findMatchingEdge(e1, graph_2);
            E e = copyEdge(graph_1, e1);
            ComparedItemInfo<E> info = new ComparedItemInfo<E>(e, e1, e2);
            result.comparedEdges.put(e, info);
            // TODO: copy edge visual settings
        }
        for (E e2: graph_2.getEdges()) {
            E e1 = findMatchingEdge(e2, graph_1);
            if (e1 == null) {
                E e = copyEdge(graph_2, e2);
                ComparedItemInfo<E> info = new ComparedItemInfo<E>(e, e1, e2);
                result.comparedEdges.put(e, info);
                // TODO: copy edge visual settings
            }
        }

		log("\n");

        doSpecialisedComparison();

		return result;
	}

    private E findMatchingEdge(E edge, G g) {
        V src = findMatchingNode(edge.getSource(), g);
        V tgt = findMatchingNode(edge.getTarget(), g);

        if (src == null || tgt == null) {
            return null;
        }

        return g.getEdge(src, tgt);
    }

    private V findMatchingNode(V node, G g) {
        String id = node.toString();
        return g.getNodeByName(id);
    }

    private void copyNodeView(NodeAttributesReader<V> nreader, V item, NodeAttributesReader<V> nreader_source, V nodeSrc) {
        nreader.setNode(item);
        nreader_source.setNode(nodeSrc);
        nreader.setPos(nreader_source.getX(), nreader_source.getY());
        // TODO: what about style?
    }

	/**
	 * define the name of the diff graph
	 */
	private void setDiffGraphName() {
		try {
			String g1name = getG1().getGraphName();
			String g2name = getG2().getGraphName();
			
			log("Generating diff_"+g1name+"_"+g2name+"\n");
			getDiffGraph().setGraphName("diff_"+g1name+"_"+g2name);
		} catch (GsException e) {} //Could not append normally, the g1 and g2 graph name can't be invalid at this point
	}
	

    abstract protected V copyNode(G srcGraph, V node);

    abstract protected E copyEdge(G srcGraph, E edge);

    abstract protected void doSpecialisedComparison();

	/**
	 * Return a merge graph colored to indicates vertices and edges parent graph.
	 * @return the diff graph
	 */
	public G getDiffGraph() {
		return graph_new;
	}

	/**
	 * Return the first graph to compare
	 * @return the graph
	 */
	public G getG1() {
		return graph_1;
	}

	/**
	 * Return the second graph to compare
	 * @return the graph
	 */
	public G getG2() {
		return graph_2;
	}
	
	/**
	 * append the string 's' to the log
	 * @param s
	 */
	public void log(String s) {
		result.getLog().append(s);
	}

	/**
	 * append the long l to the log
	 * @param l
	 */
	public void log(long l) {
		result.getLog().append(l);
	}

	/**
	 * append the int i to the log
	 * @param i
	 */
	public void log(int i) {
		result.getLog().append(i);
	}
	
	/**
	 * append the boolean b to the log
	 * @param b
	 */
	public void log(boolean b) {
		result.getLog().append(b);
	}
	
	/**
	 * append the object o to the log
	 * @param o
	 */
	public void log(Object o) {
		result.getLog().append(o);
	}

}
