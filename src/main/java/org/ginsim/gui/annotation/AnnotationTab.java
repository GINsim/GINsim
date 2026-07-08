package org.ginsim.gui.annotation;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.metadata.Annotator;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.gui.annotation.classes.AnnotationsComponent;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.graph.GraphSelection;
import org.ginsim.gui.shell.editpanel.EditTab;

import javax.swing.*;
import java.awt.*;

/**
 * class AnnotationTab extends JPanel
 */
public class AnnotationTab extends JPanel implements EditTab {
	
	private static final long serialVersionUID = 1L;
	private AnnotationsComponent<NodeInfo> annotCpt;
	private GridBagConstraints gbc;


	public AnnotationTab(Annotator<NodeInfo> annotator) {

		this.setLayout(new GridBagLayout());
		
		this.gbc = new GridBagConstraints();
		this.gbc.weightx = 1.0;
		this.gbc.weighty = 1.0;
		this.gbc.fill = GridBagConstraints.BOTH;
		this.gbc.anchor = GridBagConstraints.CENTER;
		this.gbc.gridx = 0;
		this.gbc.gridy = 0;

		this.annotCpt = new AnnotationsComponent(annotator);
		this.add(annotCpt, this.gbc);
	}

    /**
     * init function
     * @param gui  the gui
     * @return a new AnnotationTab
     */
    public static AnnotationTab prepareTab(GraphGUI<?,?,?> gui) {
        Graph graph = gui.getGraph();
        // TODO: enable the annotation panel for other graph types?
        if (graph instanceof RegulatoryGraph) {
        	Annotator<NodeInfo> annotator = ((RegulatoryGraph) graph).getAnnotator();
            return new AnnotationTab(annotator);
        }
        return null;
    }

    @Override
    public String getTitle() {
        return "Annotation";
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public boolean isActive(GraphSelection<?, ?> selection) {
        if (selection == null) {
			this.annotCpt.onModel();
            return true;
        }
        
        switch (selection.getSelectionType()) {
            case SEL_NONE:
				this.annotCpt.onModel();
            	return true;
            case SEL_NODE:
            	RegulatoryNode interNode = (RegulatoryNode) selection.getSelectedNodes().get(0);
            	NodeInfo node = interNode.getNodeInfo();
            	this.annotCpt.onNode(node);
                return true;
            case SEL_EDGE:
            	RegulatoryMultiEdge interEdge = (RegulatoryMultiEdge) selection.getSelectedEdges().get(0);
				NodeInfo node1 = interEdge.getSource().getNodeInfo();
				NodeInfo node2 = interEdge.getTarget().getNodeInfo();
				this.annotCpt.onEdge(node1, node2);
                return true;
            case SEL_MULTIPLE:
                return false;
        }
        return false;
    }
}
