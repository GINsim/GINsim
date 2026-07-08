package org.ginsim.gui.graph.dynamicgraph;

import java.awt.*;
import java.util.List;

import org.colomoto.biolqm.NodeInfo;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.GraphChangeType;
import org.ginsim.core.graph.GraphEventCascade;
import org.ginsim.core.graph.GraphListener;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.gui.annotation.AnnotationPanel;
import org.ginsim.gui.graph.GUIEditor;
import org.ginsim.gui.utils.data.ListPanel;
import org.ginsim.gui.utils.data.ListPanelHelper;
import org.ginsim.gui.utils.data.TextProperty;
import org.ginsim.gui.utils.widgets.StatusTextField;

import javax.swing.*;


public class StateTransitionGraphEditor extends JPanel implements TextProperty, GUIEditor<DynamicGraph>, GraphListener<DynamicGraph> {

	private final DynamicGraph graph;
	private final List<NodeInfo> nodeList;

    private final StatusTextField nameField;
    private final AnnotationPanel annotationPanel;
    private final ListPanel nodePanel;

	public StateTransitionGraphEditor(DynamicGraph graph) {
        super(new GridBagLayout());

        this.graph = graph;
        this.nodeList = graph.getNodeOrder();

        GridBagConstraints cst = new GridBagConstraints();
        cst.gridx = 1;
        cst.gridy = 1;
        cst.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Name"), cst);
        cst.gridx++;
        nameField = new StatusTextField();
        nameField.setProperty(this);
        add(nameField, cst);

        cst.gridx = 1;
        cst.gridy = 2;
        cst.gridwidth = 2;
        cst.weighty = 1;
        cst.fill = GridBagConstraints.BOTH;
        nodePanel = new ListPanel(STGNodeListHelper.HELPER, "");
        nodePanel.setList(nodeList);
        add(nodePanel, cst);

        cst.gridy = 1;
        cst.gridx = 3;
        cst.gridheight = 2;
        cst.weightx = 1;
        annotationPanel = new AnnotationPanel();
        annotationPanel.setAnnotation(graph.getAnnotation());
        add(annotationPanel, cst);

        GSGraphManager.getInstance().addGraphListener( this.graph, this);
	}

    @Override
	public void setEditedItem(DynamicGraph g) {
        // should not be needed
	}

    public void refresh() {
        nameField.refresh(true);
        nodePanel.refresh();
        annotationPanel.refresh(true);
    }
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public GraphEventCascade graphChanged(DynamicGraph g, GraphChangeType type, Object data) {
		switch (type) {
		case NODEADDED:
            refresh();
		case NODEREMOVED:
            refresh();
		case NODEUPDATED:
			refresh();
		}
		return null;
	}

    @Override
    public String getValue() {
        return graph.getGraphName();
    }

    @Override
    public void setValue(String value) {
        try {
            graph.setGraphName( value);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean isValidValue(String value) {
        return XMLWriter.isValidId(value);
    }
}

class STGNodeListHelper extends ListPanelHelper {

    public static final STGNodeListHelper HELPER = new STGNodeListHelper();

    private STGNodeListHelper() {
        canOrder = false;
    }
}
