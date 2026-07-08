package org.ginsim.core.graph;

import org.ginsim.core.graph.view.style.EdgeStyle;
import org.ginsim.core.graph.view.style.EdgeStyleImpl;
import org.ginsim.core.graph.view.style.NodeStyle;
import org.ginsim.core.graph.view.style.NodeStyleImpl;

/**
 * Base class for Graph Factories.
 *
 * @author Lionel Spinelli
 * @author Aurelien Naldi
 *
 * @param <G> the type of graph created by the factory.
 */
abstract public class AbstractGraphFactory<G extends Graph<?,?>> implements GraphFactory<G> {

	private final Class graphClass;
	private final String graphClassName;

	/**
	 * constructor AbstractGraphFactory
	 * @param graphClass  the graph class
	 * @param graphClassName the graph class name
	 */
	public AbstractGraphFactory(Class graphClass, String graphClassName) {
		this.graphClass = graphClass;
		this.graphClassName = graphClassName;
	}
	
	@Override
	public Class<G> getGraphClass() {
		return graphClass;
	}

	@Override
	public String getGraphType() {
		return graphClassName;
	}

	@Override
	public Class getParser() {
		return null;
	}

	@Override
	public NodeStyle createDefaultNodeStyle(G graph) {
		return new NodeStyleImpl();
	}

	@Override
	public EdgeStyle createDefaultEdgeStyle(G graph) {
		return new EdgeStyleImpl();
	}

}
