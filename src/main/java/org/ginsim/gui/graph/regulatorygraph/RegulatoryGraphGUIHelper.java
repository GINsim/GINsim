package org.ginsim.gui.graph.regulatorygraph;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.regulatorygraph.RegulatoryEdgeSign;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.gui.graph.AddEdgeAction;
import org.ginsim.gui.graph.AddNodeAction;
import org.ginsim.gui.graph.DeleteAction;
import org.ginsim.gui.graph.EditAction;
import org.ginsim.gui.graph.GUIEditor;
import org.ginsim.gui.graph.GraphGUIHelper;
import org.ginsim.gui.shell.GsFileFilter;
import org.kohsuke.MetaInfServices;


/**
 * GUI helper for the regulatory graph.
 * 
 * @author Aurelien Naldi
 */
@MetaInfServices( GraphGUIHelper.class)
public class RegulatoryGraphGUIHelper implements GraphGUIHelper<RegulatoryGraph, RegulatoryNode, RegulatoryMultiEdge> {

	@Override
	public Class getGraphClass() {
		return RegulatoryGraph.class;
	}

	@Override
	public List<EditAction> getEditActions(RegulatoryGraph graph) {
		List<EditAction> actions = new ArrayList<EditAction>();
		NodeAttributesReader reader = graph.getNodeAttributeReader();
		actions.add(new AddRegulatoryNodeAction(graph, "Add components", reader));
		actions.add(new AddRegulatoryEdgeAction(graph, "Add positive regulations", RegulatoryEdgeSign.POSITIVE));
		actions.add(new AddRegulatoryEdgeAction(graph, "Add negative regulations", RegulatoryEdgeSign.NEGATIVE));
		actions.add(new AddRegulatoryEdgeAction(graph, "Add dual regulations", RegulatoryEdgeSign.DUAL));
		actions.add(new AddRegulatoryEdgeAction(graph, "Add unknown regulations", RegulatoryEdgeSign.UNKNOWN));
		actions.add( null); // Add a separator
		actions.add(new DeleteAction(graph));
		actions.add( null); // Add a separator
		return actions;
	}

	@Override
	public GUIEditor<RegulatoryGraph> getMainEditionPanel( RegulatoryGraph graph) {
		RegulatoryGraphEditor editor = new RegulatoryGraphEditor(graph);
		return editor;
	}

	@Override
	public String getEditingTabLabel( RegulatoryGraph graph) {
		return Txt.t("STR_modelAttribute");
	}

	@Override
	public GUIEditor<RegulatoryNode> getNodeEditionPanel( RegulatoryGraph graph) {
		return new RegulatoryNodeEditor(graph);
	}

	@Override
	public GUIEditor<RegulatoryMultiEdge> getEdgeEditionPanel( RegulatoryGraph graph) {
		return new RegulatoryEdgeEditor(graph);
	}

	@Override
	public JPanel getInfoPanel(RegulatoryGraph graph) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public FileFilter getFileFilter() {
	    
		GsFileFilter ffilter = new GsFileFilter();
		ffilter.setExtensionList(new String[] {"ginml", "zginml"}, "(z)ginml files");

		return ffilter;
	}

	@Override
	public JPanel getSaveOptionPanel(RegulatoryGraph graph) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canCopyPaste(RegulatoryGraph graph) {
		return true;
	}
	
	// FIXME: option panel?
//	private JPanel getOptionPanel() {
//		RegulatoryGraphOptionPanel optionPanel;
//        Object[] t_mode = { Txt.t("STR_saveNone"),
//                Txt.t("STR_savePosition"),
//                Txt.t("STR_saveComplet") };
//		optionPanel = new RegulatoryGraphOptionPanel(t_mode, this.saveMode);
//		return optionPanel;
//	}

}

class AddRegulatoryNodeAction extends AddNodeAction<RegulatoryNode> {

	private final RegulatoryGraph graph;
	public AddRegulatoryNodeAction(RegulatoryGraph graph, String name, NodeAttributesReader reader) {
		super(name, reader, "insertsquare.gif");
		this.graph = graph;
	}

	@Override
	protected RegulatoryNode getNewNode() {
		return graph.addNode();
	}
}

class AddRegulatoryEdgeAction extends AddEdgeAction<RegulatoryNode, RegulatoryMultiEdge> {

	private final RegulatoryGraph graph;
	private final RegulatoryEdgeSign sign;

	private static String getIcon(RegulatoryEdgeSign sign) {
		String sRet;
		switch (sign) {
		case POSITIVE:
			sRet = "insertpositiveedge.gif"; break;
		case NEGATIVE:
			sRet = "insertnegativeedge.gif"; break;
		case DUAL:
			sRet = "insertdualedge.gif"; break;
		default:
			sRet = "insertunknownedge.gif";
		}
		return sRet;
	}
	
	public AddRegulatoryEdgeAction(RegulatoryGraph graph, String name, RegulatoryEdgeSign sign) {
		super(name, getIcon(sign));
		this.graph = graph;
		this.sign = sign;
	}

	@Override
	protected RegulatoryMultiEdge getNewEdge(RegulatoryNode source, RegulatoryNode target) {
		if (target.isInput()) {
    		NotificationManager.getManager().publishWarning(graph, "Can not add a regulator to an input node");
			return null;
		}
		return graph.addEdge(source, target, sign);
	}
}