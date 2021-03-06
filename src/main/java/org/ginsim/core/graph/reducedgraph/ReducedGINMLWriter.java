package org.ginsim.core.graph.reducedgraph;

import java.io.IOException;

import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.io.parser.GINMLWriter;

/**
 * Save the SCC graph in GINML
 *
 * @author Aurelien Naldi
 */
public class ReducedGINMLWriter extends GINMLWriter<ReducedGraph<?,?,?>, NodeReducedData, Edge<NodeReducedData>> {

	
	private long saveEdgeID = 1;
	
	public ReducedGINMLWriter(ReducedGraph graph) {
		super(graph, ReducedGraphFactory.KEY);
	}

	@Override
	public void hook_nodeAttribute(XMLWriter out, NodeReducedData node) throws IOException {
		out.addAttr("id", ""+node);
		addAttributeTag(out, "content", node.getContentString());
	}

	@Override
	public void hook_edgeAttribute(XMLWriter out, Edge<NodeReducedData> edge) throws IOException {
		NodeReducedData source = edge.getSource();
		NodeReducedData target = edge.getTarget();

		out.addAttr("id", "e"+saveEdgeID++);
		out.addAttr("from", ""+source);
		out.addAttr("to", ""+target);
	}

}
