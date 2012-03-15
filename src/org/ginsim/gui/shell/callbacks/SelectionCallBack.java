package org.ginsim.gui.shell.callbacks;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

import org.ginsim.common.utils.Translator;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.shell.FrameActionManager;
import org.ginsim.gui.shell.SearchFrame;

public class SelectionCallBack {

	public static void fillMenu(JMenu menu, Graph graph) {
		JMenu smenu = new JMenu(Translator.getString( "STR_SelectAll"));
		smenu.add(new SelectAllAction(graph));
		smenu.add(new SelectAllNodesAction(graph));
		smenu.add(new SelectAllEdgesAction(graph));
		menu.add(smenu);
		
		smenu = new JMenu(Translator.getString( "STR_InvertSelection"));
		smenu.add(new InvertSelectionAction(graph));
		smenu.add(new InvertNodesSelectionAction(graph));
		smenu.add(new InvertEdgesSelectionAction(graph));
		menu.add(smenu);
		
		smenu = new JMenu(Translator.getString( "STR_ExtendSelection"));
		smenu.add(new ExtendSelectionToOutgoingNodesAction(graph));
		smenu.add(new ExtendSelectionToOutgoingEdgesAction(graph));
		smenu.add(new ExtendSelectionToIncomingNodesAction(graph));
		smenu.add(new ExtendSelectionToIncomingEdgesAction(graph));
		menu.add(smenu);

		menu.add(new SearchNodeAction(graph));
	}	
}


class SearchNodeAction extends AbstractAction {
	
	private final Graph<?,?> graph;
	
	public SearchNodeAction(Graph<?,?> graph) {
		super( Translator.getString( "STR_SearchNode"));
		putValue(SHORT_DESCRIPTION,  Translator.getString( "STR_SearchNode_descr"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, FrameActionManager.MASK));
		this.graph = graph;
	}
	
	public void searchNode() {
		new SearchFrame(GUIManager.getInstance().getGraphGUI(graph));
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		searchNode();
	}
}

class SelectAllAction extends AbstractAction {
	
	private final Graph<?,?> graph;
	
	public SelectAllAction(Graph<?,?> graph) {
		super( Translator.getString( "STR_SelectAll"));
		putValue(SHORT_DESCRIPTION,  Translator.getString( "STR_SelectAll_descr"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, FrameActionManager.MASK));
		this.graph = graph;
	}
	
	public void selectAll() {
		GUIManager.getInstance().getGraphGUI(graph).getSelection().selectAll();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		selectAll();
	}
}

class SelectAllNodesAction extends AbstractAction {
	
	private final Graph<?,?> graph;
	
	public SelectAllNodesAction(Graph<?,?> graph) {
		super( Translator.getString( "STR_SelectAllNodes"));
		putValue(SHORT_DESCRIPTION,  Translator.getString( "STR_SelectAllNodes_descr"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, FrameActionManager.MASK | KeyEvent.SHIFT_MASK));
		this.graph = graph;
	}
	
	public void selectAllNodes() {
		GUIManager.getInstance().getGraphGUI(graph).getSelection().selectAllNodes();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		selectAllNodes();
	}
}

class SelectAllEdgesAction extends AbstractAction {
	
	private final Graph<?,?> graph;
	
	public SelectAllEdgesAction(Graph<?,?> graph) {
		super( Translator.getString( "STR_SelectAllEdges"));
		putValue(SHORT_DESCRIPTION,  Translator.getString( "STR_SelectAllEdges_descr"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, FrameActionManager.MASK | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK));
		this.graph = graph;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		GUIManager.getInstance().getGraphGUI(graph).getSelection().selectAllEdges();
	}
}

class InvertSelectionAction extends AbstractAction {
	
	private final Graph<?,?> graph;
	
	public InvertSelectionAction(Graph<?,?> graph) {
		super( Translator.getString( "STR_InvertSelection"));
		putValue(SHORT_DESCRIPTION,  Translator.getString( "STR_InvertSelection_descr"));
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, FrameActionManager.MASK | KeyEvent.SHIFT_MASK));
		this.graph = graph;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		GUIManager.getInstance().getGraphGUI(graph).getSelection().invertSelection();
	}
}

class InvertNodesSelectionAction extends AbstractAction {
	
	private final Graph<?,?> graph;
	
	public InvertNodesSelectionAction(Graph<?,?> graph) {
		super( Translator.getString( "STR_InvertNodesSelection"));
		putValue(SHORT_DESCRIPTION,  Translator.getString( "STR_InvertNodesSelection_descr"));
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, FrameActionManager.MASK | KeyEvent.SHIFT_MASK));
		this.graph = graph;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		GUIManager.getInstance().getGraphGUI(graph).getSelection().invertNodesSelection();
	}
}

class InvertEdgesSelectionAction extends AbstractAction {
	
	private final Graph<?,?> graph;
	
	public InvertEdgesSelectionAction(Graph<?,?> graph) {
		super( Translator.getString( "STR_InvertEdgesSelection"));
		putValue(SHORT_DESCRIPTION,  Translator.getString( "STR_InvertEdgesSelection_descr"));
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, FrameActionManager.MASK | KeyEvent.SHIFT_MASK));
		this.graph = graph;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		GUIManager.getInstance().getGraphGUI(graph).getSelection().invertEdgesSelection();
	}
}

class ExtendSelectionToOutgoingNodesAction extends AbstractAction {
	
	private final Graph<?,?> graph;
	
	public ExtendSelectionToOutgoingNodesAction(Graph<?,?> graph) {
		super( Translator.getString( "STR_ExtendSelectionToOutgoingNodes"));
		putValue(SHORT_DESCRIPTION,  Translator.getString( "STR_ExtendSelectionToOutgoingNodes_descr"));
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, FrameActionManager.MASK | KeyEvent.SHIFT_MASK));
		this.graph = graph;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		GUIManager.getInstance().getGraphGUI(graph).getSelection().extendSelectionToOutgoingNodes();
	}
}

class ExtendSelectionToOutgoingEdgesAction extends AbstractAction {
	
	private final Graph<?,?> graph;
	
	public ExtendSelectionToOutgoingEdgesAction(Graph<?,?> graph) {
		super( Translator.getString( "STR_ExtendSelectionToOutgoingEdges"));
		putValue(SHORT_DESCRIPTION,  Translator.getString( "STR_ExtendSelectionToOutgoingEdges_descr"));
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, FrameActionManager.MASK | KeyEvent.SHIFT_MASK));
		this.graph = graph;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		GUIManager.getInstance().getGraphGUI(graph).getSelection().extendSelectionToOutgoingEdges();
	}
}

class ExtendSelectionToIncomingNodesAction extends AbstractAction {
	
	private final Graph<?,?> graph;
	
	public ExtendSelectionToIncomingNodesAction(Graph<?,?> graph) {
		super( Translator.getString( "STR_ExtendSelectionToIncomingNodes"));
		putValue(SHORT_DESCRIPTION,  Translator.getString( "STR_ExtendSelectionToIncomingNodes_descr"));
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, FrameActionManager.MASK | KeyEvent.SHIFT_MASK));
		this.graph = graph;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		GUIManager.getInstance().getGraphGUI(graph).getSelection().extendSelectionToIncomingNodes();
	}
}

class ExtendSelectionToIncomingEdgesAction extends AbstractAction {
	
	private final Graph<?,?> graph;
	
	public ExtendSelectionToIncomingEdgesAction(Graph<?,?> graph) {
		super( Translator.getString( "STR_ExtendSelectionToIncomingEdges"));
		putValue(SHORT_DESCRIPTION,  Translator.getString( "STR_ExtendSelectionToIncomingEdges_descr"));
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, FrameActionManager.MASK | KeyEvent.SHIFT_MASK));
		this.graph = graph;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		GUIManager.getInstance().getGraphGUI(graph).getSelection().extendSelectionToIncomingEdges();
	}
}