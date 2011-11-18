package org.ginsim.gui.graphhelper.dynamicgraph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import org.ginsim.graph.common.Edge;
import org.ginsim.graph.dynamicgraph.GsDynamicGraph;
import org.ginsim.graph.dynamicgraph.GsDynamicNode;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.EditAction;
import org.ginsim.gui.graph.GUIEditor;
import org.ginsim.gui.graphhelper.GraphGUIHelper;
import org.ginsim.gui.service.tools.dynamicalhierarchicalsimplifier.NodeInfo;
import org.ginsim.gui.service.tools.dynamicanalyser.GsDynamicItemAttributePanel;
import org.ginsim.gui.service.tools.stablestates.StableTableModel;
import org.mangosdk.spi.ProviderFor;

import fr.univmrs.tagc.GINsim.gui.GsFileFilter;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraphOptionPanel;
import fr.univmrs.tagc.GINsim.regulatoryGraph.RegulatoryGraphEditor;
import fr.univmrs.tagc.common.Debugger;
import fr.univmrs.tagc.common.managerresources.Translator;
import fr.univmrs.tagc.common.widgets.EnhancedJTable;
import fr.univmrs.tagc.common.widgets.Frame;

@ProviderFor( GraphGUIHelper.class)
public class DynamicGraphGUIHelper implements GraphGUIHelper<GsDynamicGraph, GsDynamicNode, Edge<GsDynamicNode>> {

	/**
	 * Provide the file filter to apply to a file chooser
	 * 
	 * @return the file filter to apply to a file chooser
	 */
	@Override
	public FileFilter getFileFilter() {
		
		GsFileFilter ffilter = new GsFileFilter();
	    ffilter.setExtensionList(new String[] {"ginml"}, "ginml files");

		return ffilter;
	}

	/**
	 * Create a panel containing the option for graph saving 
	 * 
	 * @param graph the edited graph
	 */
	@Override
	public JPanel getSaveOptionPanel( GsDynamicGraph graph) {
		
		Frame graph_frame = GUIManager.getInstance().getFrame( graph);
		
        Object[] t_mode = { Translator.getString("STR_saveNone"),
                    		Translator.getString("STR_savePosition"),
                    		Translator.getString("STR_saveComplet") };
        JPanel optionPanel = new GsRegulatoryGraphOptionPanel(t_mode, graph_frame != null ? 2 : 0);
		
		return optionPanel ;
	}
	
	@Override
	public GUIEditor<GsDynamicGraph> getMainEditionPanel(GsDynamicGraph graph) {
//		RegulatoryGraphEditor editor = new RegulatoryGraphEditor();
//		editor.setEditedObject(graph);
//		return editor;
		// TODO: rework the main graph panel
		return null;
	}

	@Override
	public String getEditingTabLabel(GsDynamicGraph graph) {
		return "STG";
	}

	@Override
	public GUIEditor<GsDynamicNode> getNodeEditionPanel(GsDynamicGraph graph) {
		return new GsDynamicItemAttributePanel(graph);
	}

	@Override
	public GUIEditor<Edge<GsDynamicNode>> getEdgeEditionPanel(
			GsDynamicGraph graph) {
		return new GsDynamicItemAttributePanel(graph);
	}

    /**
     * browse the graph, looking for stable states
     * @return the list of stable states found
     */
    private List getStableStates( GsDynamicGraph graph) {
    	// TODO: use cache from the graph itself?
    	
    	List<byte[]> stables = new ArrayList<byte[]>();
        for (GsDynamicNode node: graph.getVertices()) {
            if (node.isStable()) {
                stables.add(node.state);
            }
        }
        return stables;
    }
    
	
	/**
	 * Callback for the info panel: open a dialog with the list of stable states
	 */
	protected void viewStable( GsDynamicGraph graph, List<byte[]> stables) {
		List<NodeInfo> nodeOrder = graph.getNodeOrder();
        JFrame frame = new JFrame(Translator.getString("STR_stableStates"));
        frame.setSize(Math.min(30*(nodeOrder.size()+1), 800),
        		Math.min(25*(stables.size()+2), 600));
        JScrollPane scroll = new JScrollPane();
        StableTableModel model = new StableTableModel(nodeOrder);
        try {
	        model.setResult(stables, graph);
	        scroll.setViewportView(new EnhancedJTable(model));
	        frame.setContentPane(scroll);
	        frame.setVisible(true);
        } catch (Exception e) {
        	Debugger.log(e);
        }
	}
	
	@Override
	public JPanel getInfoPanel( GsDynamicGraph graph) {
        JPanel pinfo = new JPanel();
        List<byte[]> stables = getStableStates( graph);

        // just display the number of stable states here and a "show more" button
        if (stables.size() > 0) {
            pinfo.add(new JLabel("nb stable: "+stables.size()));
            JButton b_view = new JButton("view");
            // show all stables: quickly done but, it is "good enough" :)
            b_view.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	
                	// FIXME: restore info panel for the dynamic graph
                	
//                	try{
//                		viewStable(graph);
//                	}
//                	catch( GsException ge){
//                		// TODO : REFACTORING ACTION
//                		// TODO : Launch a message box to the user
//                		Debugger.log( "Unable to get the stable states" + ge);
//                	}
                }
            });
            pinfo.add(b_view);
        } else if (stables.size() > 1) {
            pinfo.add(new JLabel("no stable state."));
        }

        return pinfo;	}

	@Override
	public Class<GsDynamicGraph> getGraphClass() {
		return GsDynamicGraph.class;
	}

	@Override
	public List<EditAction> getEditActions(GsDynamicGraph graph) {
		return null;
	}
}
