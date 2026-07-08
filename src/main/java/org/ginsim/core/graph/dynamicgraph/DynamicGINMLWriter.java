package org.ginsim.core.graph.dynamicgraph;

import java.io.IOException;

import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.io.parser.GINMLWriter;

/**
 * Save a STG in the GINML format.
 *
 * @author Aurelien Naldi
 */
public class DynamicGINMLWriter extends GINMLWriter<DynamicGraph, DynamicNode, DynamicEdge> {

	public DynamicGINMLWriter(DynamicGraph graph) {
		super(graph, DynamicGraphFactory.KEY);
	}

	protected void hook_graphAttribute(XMLWriter out) throws IOException {
  		out.addAttr("nodeorder", stringNodeOrder(graph.getNodeOrder()));
	}
	protected void hook_nodeAttribute(XMLWriter out, DynamicNode node) throws IOException {
        out.addAttr("id", node.getId());
        if (node.isStable()) {
            out.addAttr("tags", "stable");
        }
	}
	
	protected void hook_edgeAttribute(XMLWriter out, DynamicEdge edge) throws IOException {
		String source = edge.getSource().getId();
		String target = edge.getTarget().getId();
        out.addAttr("id", source+"_"+target);
        out.addAttr("from", source);
        out.addAttr("to", target);
	}

}
