package org.ginsim.gui.graph.hierarchicaltransitiongraph;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.hierarchicaltransitiongraph.DecisionOnEdge;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNode;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalTransitionGraph;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.EditAction;
import org.ginsim.gui.graph.GUIEditor;
import org.ginsim.gui.graph.GraphGUIHelper;
import org.ginsim.gui.graph.regulatorygraph.RegulatoryGraphOptionPanel;
import org.ginsim.gui.shell.GsFileFilter;
import org.ginsim.gui.utils.widgets.Frame;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;
import org.ginsim.service.tool.reg2dyn.SimulationStrategy;
import org.kohsuke.MetaInfServices;


/**
 * class helper HierarchicalTransitionGraphGUIHelper
 *
 * @author Lionel Spinelli
 *
 */
@MetaInfServices( GraphGUIHelper.class)
public class HierarchicalTransitionGraphGUIHelper implements GraphGUIHelper<HierarchicalTransitionGraph, HierarchicalNode, DecisionOnEdge> {

	
	/**
	 * Provide the file filter to apply to a file chooser
	 * 
	 * @return the file filter to apply to a file chooser
	 */
	@Override
	public FileFilter getFileFilter() {
		
	    GsFileFilter ffilter = new GsFileFilter();
	    ffilter.setExtensionList(new String[] {"ginml", "zginml"}, "(z)ginml files");

		return ffilter;
	}
	
	/**
	 * Create a panel containing the option for graph saving 
	 *
	 * @param graph the edited graph
	 * @return    a JPanel
	 */
	@Override
	public JPanel getSaveOptionPanel( HierarchicalTransitionGraph graph) {
		
		Frame graph_frame = GUIManager.getInstance().getFrame( graph);
		
		Object[] t_mode = { Txt.t("STR_saveNone"),
							Txt.t("STR_savePosition"),
							Txt.t("STR_saveComplet") };
		JPanel optionPanel = new RegulatoryGraphOptionPanel(t_mode, graph_frame != null ? 2 : 0);
		
		return optionPanel ;
	}


	@Override
	public GUIEditor<HierarchicalTransitionGraph> getMainEditionPanel( HierarchicalTransitionGraph graph) {
		return new HierarchicalGraphEditor(graph);
	}

	@Override
	public String getEditingTabLabel(HierarchicalTransitionGraph graph) {
		return graph.getSimulationStrategy().getAcronym();
	}

	@Override
	public GUIEditor<HierarchicalNode> getNodeEditionPanel( HierarchicalTransitionGraph graph) {
		return new HierarchicalNodeParameterPanel(graph);
	}

	@Override
	public GUIEditor<DecisionOnEdge> getEdgeEditionPanel( HierarchicalTransitionGraph graph) {
		return new HierarchicalEdgeParameterPanel(graph);
	}

	@Override
	public JPanel getInfoPanel(HierarchicalTransitionGraph graph) {
		// TODO: info panel for HTG?
		return null;
	}

	@Override
	public Class<HierarchicalTransitionGraph> getGraphClass() {
		
		return HierarchicalTransitionGraph.class;
	}

	@Override
	public List<EditAction> getEditActions(HierarchicalTransitionGraph graph) {
		return null;
	}

	@Override
	public boolean canCopyPaste(HierarchicalTransitionGraph graph) {
		return false;
	}
}
