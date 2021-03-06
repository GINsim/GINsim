package org.ginsim.gui.graph.reducedgraph;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.reducedgraph.NodeReducedData;
import org.ginsim.core.graph.reducedgraph.ReducedGraph;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.EditAction;
import org.ginsim.gui.graph.GUIEditor;
import org.ginsim.gui.graph.GraphGUIHelper;
import org.ginsim.gui.graph.regulatorygraph.RegulatoryGraphOptionPanel;
import org.ginsim.gui.utils.widgets.Frame;
import org.kohsuke.MetaInfServices;


@MetaInfServices( GraphGUIHelper.class)
public class ReducedGraphGUIHelper implements GraphGUIHelper<ReducedGraph<?,?,?>, NodeReducedData, Edge<NodeReducedData>> {

	
	/**
	 * Provide the file filter to apply to a file chooser
	 * 
	 * @return the file filter to apply to a file chooser
	 */
	@Override
	public FileFilter getFileFilter() {
		
		return null;
	}

	/**
	 * Create a panel containing the option for graph saving 
	 * 
	 * @param graph the edited graph
	 */
	@Override
	public JPanel getSaveOptionPanel(ReducedGraph graph) {
		
		Frame graph_frame = GUIManager.getInstance().getFrame( graph);
		
		Object[] t_mode = { Txt.t("STR_saveNone"),
                    Txt.t("STR_savePosition"),
                    Txt.t("STR_saveComplet") };
        JPanel optionPanel = new RegulatoryGraphOptionPanel(t_mode, graph_frame != null ? 2 : 0);
        
		return optionPanel;
	}

	@Override
	public GUIEditor<ReducedGraph<?,?,?>> getMainEditionPanel(ReducedGraph graph) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEditingTabLabel(ReducedGraph graph) {
		return "SCC";
	}

	@Override
	public GUIEditor<NodeReducedData> getNodeEditionPanel(ReducedGraph graph) {
		return new ReducedParameterPanel(graph);
	}

	@Override
	public GUIEditor<Edge<NodeReducedData>> getEdgeEditionPanel(ReducedGraph graph) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPanel getInfoPanel(ReducedGraph graph) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<ReducedGraph<?,?,?>> getGraphClass() {
		// don't ask me why we have to cast...
		return (Class)ReducedGraph.class;
	}

	@Override
	public List<EditAction> getEditActions(ReducedGraph graph) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean canCopyPaste(ReducedGraph graph) {
		return false;
	}

}
