package org.ginsim.gui.shell.editpanel;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.JPanel;

import org.ginsim.core.graph.Graph;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GUIEditor;
import org.ginsim.gui.graph.GraphGUI;

/**
 * All parameter panels must extend this abstract class to be updated on selection changes
 * @param <T> the gui editor T
 * @param <G> Graph
 */
public abstract class AbstractParameterPanel<G extends Graph,T> extends JPanel implements GUIEditor<T> {
	private static final long	serialVersionUID	= 8326061792159035277L;
	/**
	 * gui attribute
	 */
	protected final GraphGUI<?, ?, ?> gui;
	/**
	 * graph attribute
	 */
    protected final G graph;
	/**
	 * frame attribut
	 */
    protected final Frame frame;

	/**
	 * constructor
	 * @param gui the gui
	 */
	protected AbstractParameterPanel(GraphGUI<?, ?, ?> gui) {
    	this.gui = gui;
    	this.graph = (G)gui.getGraph();
    	this.frame = GUIManager.getInstance().getFrame(graph);
    }

	/**
	 * Constructor
	 * @param graph the graph
	 */
	protected AbstractParameterPanel(Graph<?,?> graph) {
		this(GUIManager.getInstance().getGraphGUI(graph));
	}

	@Override
	public final Component getComponent() {
		return this;
	}
    
}
