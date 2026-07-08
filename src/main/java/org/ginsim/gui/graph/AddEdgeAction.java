package org.ginsim.gui.graph;

import org.ginsim.core.graph.Edge;

/**
 * class AddEdgeAction
 * @param <V>  vertex V
 * @param <E>  edge E
 */
public abstract class AddEdgeAction<V, E extends Edge<V>> extends EditAction {
	/**
	 * constructor
	 * @param name yhe name
	 * @param icon yhe icon
	 */
	public AddEdgeAction(String name, String icon) {
		super(EditMode.EDGE, name, icon);
	}

	/**
	 * constructor
	 * @param name the name
	 */
	public AddEdgeAction(String name) {
		this(name, null);
	}

	/**
	 * add Edge
	 * @param manager  edit action manager
	 * @param from  vertex from
	 * @param to  vetex to
	 */
	public void addEdge( EditActionManager manager, V from, V to) {
		E edge = getNewEdge(from, to);
        if (edge != null) {
            manager.select(edge);
            manager.actionPerformed(this);
        }
	}

	/**
	 * Edge getter
	 * @param from vertex from
	 * @param to vertex to
	 * @return the edge
	 */
	abstract protected E getNewEdge(V from, V to);

}
