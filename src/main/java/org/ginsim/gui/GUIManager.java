package org.ginsim.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.OptionStore;
import org.ginsim.common.application.Txt;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.graph.GraphGUIHelper;
import org.ginsim.gui.graph.GraphGUIHelperFactory;
import org.ginsim.gui.graph.canvas.CanvasGraphGUIImpl;
import org.ginsim.gui.shell.AboutDialog;
import org.ginsim.gui.shell.MainFrame;
import org.ginsim.gui.shell.StartupDialog;
import org.ginsim.gui.utils.widgets.Frame;

public class GUIManager {
    
	private static boolean STARTUPDIALOG = true;
	private static GUIManager manager;
	
	private HashMap<Graph,GUIObject> graphToGUIObject = new HashMap<Graph, GUIObject>();
	private StartupDialog startupDialog = null;

	/**
	 * Give access to the manager singleton
	 * 
	 * @return
	 */
	static public GUIManager getInstance(){
		
		if( manager == null){
			manager = new GUIManager();
		}
		
		return manager; 
	}
	
	public void registerGUI( GraphGUI graph_gui, MainFrame frame){
		
		if( graph_gui!= null && frame != null){
			Graph graph = graph_gui.getGraph();
			if( graph != null){
				graphToGUIObject.put( graph, new GUIObject( graph_gui, frame));
			}
		}
	}

	/**
	 * Load a graph from a GINML file and open a new frame for it.
	 * 
	 * @param filename
	 */
	public void loadGINMLfile(String filename) {
		try {
			Graph<?,?> graph = GSGraphManager.getInstance().open(filename);
			newFrame( graph);
		} catch (GsException e) {
			LogManager.error(e);
		}
	}
	
	/**
	 * Create a new graph (of the default type, i.e. a regulatory graph)
	 * and then a new frame for it.
	 * 
	 * @return the new regulatory graph
	 */
	public RegulatoryGraph newFrame() {
		RegulatoryGraph graph = GSGraphManager.getInstance().getNewGraph();
		newFrame(graph);
		return graph;
	}

	
	/**
	 * Build a new frame for the given Graph by creating a suitable GraphGUI and memorize their relationship
	 * 
	 * @param graph the graph for which a Frame is desired
	 * @return a new frame for the given Graph
	 */
	public Frame newFrame( Graph graph){
		
		return newFrame( graph, true);
	}
	
	/**
	 * Build a new frame for the given Graph by creating a suitable GraphGUI and memorize their relationship
	 * 
	 * @param graph the graph for which a Frame is desired
	 * @param can_be_saved a boolean indicating if the graph associated to the frame can be saved or not
	 * @return a new frame for the given Graph
	 */
	public Frame newFrame( Graph graph, boolean can_be_saved) {
		
		GraphGUI graph_gui;
		try {
			graph_gui = createGraphGUI( graph, can_be_saved);
			MainFrame frame = new MainFrame( graph_gui);
			frame.setVisible(true);
			NotificationManager.getManager().registerListener( frame, graph);
			closeStartupDialog();			
			return frame;
		} catch (Exception e) {
			GUIMessageUtils.openErrorDialog( "STR_unableToOpen");
			LogManager.error( "Unable to open graph");
			LogManager.error( e);
			return null;
		}
	}
	
	
	/**
	 * Build a GraphGUI instance for the given graph
	 * @param <V>
	 * @param <E>
	 * 
	 * @param graph the graph for which a GraphGUi is desired
	 * @return the GraphGUI built for the given Graph
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private GraphGUI createGraphGUI( Graph graph, boolean can_be_saved) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		
		// get a graph GUI helper
		GraphGUIHelper<?,?,?> helper = GraphGUIHelperFactory.getFactory().getGraphGUIHelper(graph);
		
		// find the GUI component and show the graph...
		GraphGUI<?,?,?> graphGUI = new CanvasGraphGUIImpl(graph, helper, can_be_saved);
		graphToGUIObject.put( graph, new GUIObject( graphGUI, null));
		return graphGUI;
	}
	
	
	/**
	 * Give access to the GraphGUI corresponding to the given Graph
	 * 
	 * @param graph the graph for which the GraphGUI is desired
	 * @return the GraphGUI corresponding to the given Graph
	 */
	public GraphGUI getGraphGUI( Graph graph){
		
		GUIObject gui_object = graphToGUIObject.get( graph);
		
		if( gui_object != null){
			return gui_object.getGraphGUI();
		}
		
		return null;
	}
	
	
	/**
	 * Give access to the Frame corresponding to the given Graph
	 * 
	 * @param graph the graph for which the GraphGUI is desired
	 * @return the Frame corresponding to the given Graph
	 */
	public Frame getFrame( Graph graph){
		
		GUIObject gui_object = graphToGUIObject.get( graph);
		
		if( gui_object != null){
			return gui_object.getFrame();
		}
		
		return null;
	}
	
	/**
	 * Unmemorize the relationship between a graph and its GraphGUI and Frame
	 * 
	 * @param graph the graph to close
	 */
	public boolean close( Graph graph){
		
		GUIObject o = graphToGUIObject.get(graph);
		if (!o.graphGUI.isSaved()) {
			// FIXME: better name
			String name = GSGraphManager.getInstance().getGraphPath( graph);
			if( name == null){
				name = "NAME_HERE";
			}
			o.frame.toFront();
            int aw = JOptionPane.showConfirmDialog(o.frame, Txt.t("STR_saveQuestion", name),
                    Txt.t("STR_closeConfirm"),
                    JOptionPane.YES_NO_CANCEL_OPTION);
            switch (aw) {
                case JOptionPane.YES_OPTION:
                	if (!o.graphGUI.save()) {
                		return false;
                	}
                case JOptionPane.NO_OPTION:
                	break;
                case JOptionPane.CANCEL_OPTION:
                default:
                	return false;
            }
		}
		
		o.graphGUI.fireGraphClose();
		graphToGUIObject.remove( graph);
		if (o.frame != null) {
			o.frame.setVisible(false);
			o.frame.dispose();
		}
		if (graphToGUIObject.size() == 0) {
			noFrameLeft(false);
		}
		GSGraphManager.getInstance().close(graph);
		return true;
	}
	
	/**
	 * try to close all frames
	 */
	public void quit( ){
		
		// Construction of the List of graph is required to avoid the ConcurrentModificationException
		// a loop on the graphToGUIObject.keySet() will generate due to the graphToGUIObject.remove( graph)
		// call in the close() method
		
		// Ignore the startup dialog setting: we want to quit
		STARTUPDIALOG = false;
		List<Graph> graph_list = new ArrayList<Graph>();
		graph_list.addAll( graphToGUIObject.keySet());
		for ( Graph g: graph_list) {
			if (!close(g)) {
				break;
			}
		}
	}
	
	/**
	 * Close all the windows of graphs that are empty
	 * 
	 */
	public void closeEmptyGraphs(){
		
		// Construction of the List of graph is required to avoid the ConcurrentModificationException
		// a loop on the graphToGUIObject.keySet() will generate due to the graphToGUIObject.remove( graph)
		// call in the close() method
		List<Graph> graph_list = new ArrayList<Graph>();
		graph_list.addAll( graphToGUIObject.keySet());
		for ( Graph g: graph_list) {
			if( g.getNodeCount() == 0){
				if ( !close( g)) {
					break;
				}
			}
		}
	}

	
	
	// ---------------------- METHODS ON BLOCKS MANAGEMENT -----------------------------------
	
	/**
	 * block editing mode for the graph
	 *
	 * @param key
	 */
	public void addBlockEdit( Graph graph, Object key) {
		
		GUIObject obj = graphToGUIObject.get( graph);
		
		if( obj != null){
			obj.addBlockEdit(key);
		}
	}

	/**
	 * Verify if edition is allowed on the graph
	 * 
	 * @return true if edit isn't blocked
	 */
	public boolean isEditAllowed( Graph graph) {
		
		GUIObject obj = graphToGUIObject.get( graph);
		
		if( obj != null){
			return obj.isEditAllowed();
		}
		
		return true;
	}

	/**
	 * Remove a block on Edition
	 * 
	 * @param key
	 */
	public void removeBlockEdit( Graph graph, Object key) {
		
		GUIObject obj = graphToGUIObject.get( graph);
		
		if( obj != null){
			obj.removeBlockEdit(key);
		}
	}
	
	
	/**
	 * Block closing of the graph
	 *
	 * @param key
	 */
	public void addBlockClose( Graph graph, Object key) {
		
		GUIObject obj = graphToGUIObject.get( graph);
		
		if( obj != null){
			obj.addBlockClose(key);
		}
	}
	
    /**
     * Verify if it is allowed to close the graph can be closed
     * 
     * @return true if closing this graph is allowed
     */
    public boolean canClose( Graph graph) {
    	
		GUIObject obj = graphToGUIObject.get( graph);
		
		if( obj != null){
			return obj.isEditAllowed();
		}
		
		return true;
    }

	/**
	 * Remove a block on closing
	 * 
	 * @param key
	 */
	public void removeBlockClose ( Graph graph, Object key) {
		
		GUIObject obj = graphToGUIObject.get( graph);
		
		if( obj != null){
			obj.removeBlockClose(key);
		}
	}

	
	// ---------------------- METHODS LINKED TO THE whatToDoWithGraph FRAME -----------------------------------


	public void whatToDoWithGraph(Graph<?, ?> new_graph) {
		whatToDoWithGraph( new_graph, null);
	}

	/**
	 * Manage the action to execute on the new graph (that was generated
	 * from the parent graph). According to the size of teh graph, a WhatToDoWithGraph frame may be
	 * opened to ask user what he wants to do with the new graph.
	 * 
	 * @param new_graph the graph to manage
	 * @param parent_graph the graph from which the new graph was generated
	 */
	public void whatToDoWithGraph(Graph<?, ?> new_graph, Graph<?,?> parent_graph) {
		
		// If the new graph is null, an error message is displayed
		if( new_graph == null){
			GUIMessageUtils.openErrorDialog( Txt.t("STR_computedNullGraph"));
			return;
		}
		
		// If the graph is below the limit, a new frame is opened
		if( new_graph.getNodeCount() < WhatToDoWithGraph.LITMIT_ASK_QUESTION){
			WhatToDoWithGraph.layoutIfNeeded(new_graph);
			newFrame( new_graph);
			return;
		}
		
		// If the graph is above the limit, a  WhatToDoWithGraph frame is opened to obtain the user chosen action
		new WhatToDoWithGraph( new_graph);
	}
	
	
	/**
	 * Class containing the relationship between a Graph, its GraphGUI the corresponding Frame
	 * 
	 * @author Lionel Spinelli
	 *
	 */
	private class GUIObject{
		
		private GraphGUI graphGUI;
		private Frame frame;
		private List<Object> blockEdit;
		private List<Object> blockClose;
		
		public GUIObject( GraphGUI graph_gui, Frame frame){
			
			this.graphGUI = graph_gui;
			this.frame = frame;
		}
		
		/**
		 * 
		 * @return the frame
		 */
		public Frame getFrame() {
			
			return frame;
		}
		
		/**
		 * 
		 * @return the GraphGUI
		 */
		public GraphGUI getGraphGUI() {
			
			return graphGUI;
		}
		
		/**
		 * block editing mode for the graph
		 *
		 * @param key
		 */
		public void addBlockEdit(Object key) {
			
		    if (key == null) {
		        return;
		    }
		    if (blockEdit == null) {
		        blockEdit = new ArrayList<Object>();
		    }
		    blockEdit.add(key);
		}

		/**
		 * Verify if edition is allowed on the graph
		 * 
		 * @return true if edit isn't blocked
		 */
		public boolean isEditAllowed() {
			
		    return blockEdit == null;
		}

		/**
		 * Remove a block on Edition
		 * 
		 * @param key
		 */
		public void removeBlockEdit (Object key) {
			
		    if (blockEdit == null) {
		        return;
		    }

		    blockEdit.remove(key);
		    if (blockEdit.size() == 0) {
		        blockEdit = null;
		    }
		}
		
		
		/**
		 * Block closing of the graph
		 *
		 * @param key
		 */
		public void addBlockClose(Object key) {
			
		    if (key == null) {
		        return;
		    }
		    if (blockClose == null) {
		        blockClose = new ArrayList<Object>();
		    }
		    blockClose.add(key);
		}

		/**
		 * Remove a block on closing
		 * 
		 * @param key
		 */
		public void removeBlockClose (Object key) {
			
		    if (blockClose == null) {
		        return;
		    }

		    blockClose.remove(key);
		    if (blockClose.size() == 0) {
		        blockClose = null;
		    }
		}

	}


	public void startup( List<String> open) {
		
		if (open.size() > 0) {
			for (String filename: open) {
				Graph graph = null;
				if (filename == null) {
					graph = GSGraphManager.getInstance().getNewGraph();
				} else {
					try {
						graph = GSGraphManager.getInstance().open(filename);
					} catch (GsException e) {
						LogManager.error(e);
						graph = null;
					}
				}
				
				if (graph != null) {
					newFrame(graph);
				}
			}
		}
		
		if (graphToGUIObject.size() == 0) {
			noFrameLeft(true);
		}
	}

	private void noFrameLeft(boolean startup) {
		if (STARTUPDIALOG) {
			startupDialog = new StartupDialog( startup);
			return;
		}
		
		if (startup) {
			newFrame();
			return;
		}
		
		exit();
	}
	
	public void exit() {
		OptionStore.saveOptions();
		System.exit(0);
	}

	public void closeStartupDialog() {
		if (startupDialog == null) {
			return;
		}
		StartupDialog oldDialog = startupDialog;
		startupDialog = null;
		oldDialog.close();
		
		if (graphToGUIObject.size() == 0) {
			exit();
		}
	}

	/**
	 * Show an "about" dialog
	 */
	public void about() {
		new AboutDialog().setVisible(true);
	}
}
