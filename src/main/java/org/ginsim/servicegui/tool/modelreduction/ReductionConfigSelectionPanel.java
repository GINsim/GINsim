package org.ginsim.servicegui.tool.modelreduction;

import org.ginsim.core.graph.GraphChangeType;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.graph.GraphGUIListener;
import org.ginsim.gui.utils.data.ListSelectionPanel;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.service.tool.modelreduction.ListOfReductionConfigs;
import org.ginsim.service.tool.modelreduction.ReductionConfig;
import org.ginsim.service.tool.modelreduction.ReductionConfigManager;
import org.ginsim.service.tool.modelreduction.ReductionHolder;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class ReductionConfigSelectionPanel extends ListSelectionPanel<ReductionConfig> implements GraphGUIListener<RegulatoryGraph, RegulatoryNode, RegulatoryMultiEdge> {
	private static final long serialVersionUID = 1213902700181873169L;

	private final RegulatoryGraph graph;
    private final ReductionHolder holder;

	//private JCheckBox cb_simplify = null;

	public ReductionConfigSelectionPanel(StackDialog dialog, RegulatoryGraph graph, ReductionHolder holder) {
		super(dialog, "Select a reduction");
		
		this.graph = graph;
		this.holder = holder;
		
        GraphGUI<RegulatoryGraph, RegulatoryNode, RegulatoryMultiEdge> gui = GUIManager.getInstance().getGraphGUI(graph);
        gui.addGraphGUIListener(this);

        initialize("Select a reduction", true);
	}

	//public void setCBSimplify(JCheckBox cb_simplify) {
	//	this.cb_simplify = cb_simplify;}
	
	@Override
	public void configure() {
		Component panel = ReductionGUIHelper.getReductionPanel(getPerturbationsObject(true));
        dialog.addTempPanel(panel);
	}

	public ReductionConfig getSelected() {
		return holder.getReduction();
	}
	
	public void setSelected(ReductionConfig r) {
		holder.setReduction(r);
       // if (r != null){
		//	if (this.cb_simplify != null) {
		//		this.cb_simplify.setSelected(false);
		//		this.cb_simplify.repaint();
		//	}
		//}
		 dialog.repaint();
	}

	@Override
	public void graphSelectionChanged(GraphGUI<RegulatoryGraph, RegulatoryNode, RegulatoryMultiEdge> gui) {
	}

	@Override
	public void graphGUIClosed(GraphGUI<RegulatoryGraph, RegulatoryNode, RegulatoryMultiEdge> gui) {
	}

	@Override
	public void graphChanged(RegulatoryGraph g, GraphChangeType type, Object data) {
		if (type == GraphChangeType.ASSOCIATEDADDED || type == GraphChangeType.ASSOCIATEDUPDATED) {
			refresh();
		}
	}

	private ListOfReductionConfigs getPerturbationsObject(boolean force) {
		return (ListOfReductionConfigs) ObjectAssociationManager.getInstance().getObject( graph, ReductionConfigManager.KEY, force);
	}
	
	@Override
	protected List<ReductionConfig> getList() {
        return getPerturbationsObject(false);
	}
}
