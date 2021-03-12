package org.ginsim.gui.annotation;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.metadata.AnnotationModule;
import org.colomoto.biolqm.metadata.annotations.Metadata;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.gui.annotation.classes.AnnotationsComponent;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.graph.GraphSelection;
import org.ginsim.gui.shell.editpanel.EditTab;

import javax.swing.*;
import java.awt.*;

public class AnnotationTab extends JPanel implements EditTab {
	
	private static final long serialVersionUID = 1L;
	private AnnotationModule annotationModule;
	private GridBagConstraints gbc;

	public AnnotationTab(AnnotationModule newAnnotationModule) {
		this.annotationModule = newAnnotationModule;
		
		this.setLayout(new GridBagLayout());
		
		this.gbc = new GridBagConstraints();
		this.gbc.weightx = 1.0;
		this.gbc.weighty = 1.0;
		this.gbc.fill = GridBagConstraints.BOTH;
		this.gbc.anchor = GridBagConstraints.CENTER;
		this.gbc.gridx = 0;
		this.gbc.gridy = 0;
		
		try {
			this.add(new AnnotationsComponent(this.getMetadataOfModel(), false), this.gbc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public static AnnotationTab prepareTab(GraphGUI<?,?,?> gui) {
        Graph graph = gui.getGraph();

        // TODO: create the panel only of the graph supports annotations
        if (graph instanceof RegulatoryGraph) {
        	AnnotationModule newAnnotationModule = ((RegulatoryGraph) graph).getAnnotationModule();
        	
            return new AnnotationTab(newAnnotationModule);
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
    
    public void updateMetadata(Metadata meta) {
    	this.remove(0);
    	this.add(new AnnotationsComponent(meta, false), this.gbc);
    	this.revalidate();
    	this.repaint();
    }

    @Override
    public boolean isActive(GraphSelection<?, ?> selection) {
        if (selection == null) {
            // No selection: annotate the graph
            return true;
        }
        
        switch (selection.getSelectionType()) {
            case SEL_NONE:
            	Metadata metadataModel = this.getMetadataOfModel();
            	updateMetadata(metadataModel);
            	return true;
            case SEL_NODE:
            	RegulatoryNode interNode = (RegulatoryNode) selection.getSelectedNodes().get(0);
            	NodeInfo node = interNode.getNodeInfo();
            	String nodeId = node.getNodeID();
				try {
					Metadata metadataNode = this.getMetadataOfNode(nodeId);
					updateMetadata(metadataNode);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                return true;
            case SEL_EDGE:
            	break;
            case SEL_MULTIPLE:
                return false;
        }
        return false;
    }
    
	public Metadata getMetadataOfModel() {
		
		return this.annotationModule.modelConstants.getListMetadata().get(this.annotationModule.modelIndex);
	}
	
	public boolean isSetMetadataOfNode(String nodeId) {
		if (this.annotationModule.nodesIndex.containsKey(nodeId)) {
			return true;
		}
		return false;
	}
	
	public Metadata getMetadataOfNode(String nodeId) throws Exception {
		try {
			if (this.annotationModule.nodesIndex.containsKey(nodeId)) {
				return this.annotationModule.modelConstants.getListMetadata().get(this.annotationModule.nodesIndex.get(nodeId));
			}
			else {
				return this.annotationModule.createMetadataOfNode(nodeId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
