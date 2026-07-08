package org.ginsim.gui.shell.editpanel;

import java.awt.CardLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ginsim.gui.graph.GUIEditor;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.graph.GraphSelection;

public class MainEditTab extends JPanel implements EditTab {

	private static final String GRAPH_NAME="Graph", NODE_NAME="Node", EDGE_NAME="Edge";
	
	private final String title;
	private final GUIEditor mainPanel, nodePanel, edgePanel;
    private final CardLayout cards;
    
	public MainEditTab( GraphGUI gui) {
		cards = new CardLayout();
		setLayout(cards);
		
		this.title = gui.getEditingTabLabel();
		GUIEditor editor = gui.getMainEditionPanel();
		this.mainPanel = editor == null ? new DefaultGUIEditor("Empty panel editor") : editor;
		editor = gui.getNodeEditionPanel();
		this.nodePanel = editor == null ? new DefaultGUIEditor("Empty node editor") : editor;
		editor = gui.getEdgeEditionPanel();
		this.edgePanel = editor == null ? new DefaultGUIEditor("Empty edge editor") : editor;
		
		add(mainPanel.getComponent(), GRAPH_NAME);
		add(nodePanel.getComponent(), NODE_NAME);
		add(edgePanel.getComponent(), EDGE_NAME);
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public boolean isActive(GraphSelection<?, ?> selection) {
		
		String activePanel = null;
		switch (selection.getSelectionType()) {
		case SEL_NONE:
			activePanel = GRAPH_NAME;
			mainPanel.setEditedItem(selection.getSelectedNodes());
			break;
		case SEL_NODE:
			activePanel = NODE_NAME;
			nodePanel.setEditedItem(selection.getSelectedNodes().get(0));
			break;
		case SEL_EDGE:
			activePanel = EDGE_NAME;
			edgePanel.setEditedItem(selection.getSelectedEdges().get(0));
			break;
		default:
			return false;
		}
		
		cards.show(this, activePanel);
		// TODO: provide the selection to the active tab
		
		return true;
	}
}

class DefaultGUIEditor implements GUIEditor<Object> {

	private final JLabel label; 
	
	public DefaultGUIEditor(String text) {
		this.label = new JLabel(text);
	}
	
	@Override
	public void setEditedItem(Object item) {
		
	}

	@Override
	public Component getComponent() {
		return label;
	}
}