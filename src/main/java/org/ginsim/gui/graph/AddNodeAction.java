package org.ginsim.gui.graph;

import org.ginsim.core.graph.view.NodeAttributesReader;

/**
 * Class AddNodeAction
 * @param <V> vertex v
 */
public abstract class AddNodeAction<V> extends EditAction {

	private final NodeAttributesReader reader;

	/**
	 * Constructor AddNodeAction
	 * @param name the name
	 * @param reader the NodeAttributesReader
	 */
	public AddNodeAction(String name, NodeAttributesReader reader) {
		this(name, reader, null);
	}

	/**
	 * Constructor AddNodeAction
	 * @param name the name
	 * @param reader the NodeAttributesReader
	 * @param icon the icon string
	 */
	public AddNodeAction(String name, NodeAttributesReader reader, String icon) {
		super(EditMode.NODE, name, icon);
		this.reader = reader;
	}

	/**
	 * Add node function
	 * @param manager  a EditActionManager
	 * @param x  position x
	 * @param y position y
	 */
	public void addNode( EditActionManager manager, int x, int y) {
		V vertex = getNewNode();
		reader.setNode(vertex);
		reader.setPos(x, y);
		manager.select(vertex);
		manager.actionPerformed(this);
	}

	/**
	 * Getter a new Node
	 * @return a vertex
	 */
	abstract protected V getNewNode();
}
