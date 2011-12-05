package org.ginsim.core.graph.tree;

import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

import org.ginsim.core.graph.common.AbstractGraph;
import org.ginsim.core.graph.common.Edge;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.omdd.OMDDNode;
import org.ginsim.servicegui.tool.regulatorytreefunction.TreeActionPanel;


public class TreeImpl  extends AbstractGraph<TreeNode, Edge<TreeNode>> 
	implements Tree{
	
	public final static int MODE_DIAGRAM_WITH_MULTIPLE_LEAFS = 0;
	public final static int MODE_DIAGRAM = 1;
	public final static int MODE_TREE = 2;

	/**
	 * The tree pendant to OMDDNode.TERMINALS
	 */
	public static TreeNode[] leafs;

	static {
		leafs = new TreeNode[OMDDNode.TERMINALS.length];
		
		for (byte i = 0; i < OMDDNode.TERMINALS.length; i++) {
			leafs[i] = new TreeNode(""+i,-1, i, TreeNode.TYPE_LEAF, i);
		}
	}
	
	
	private RegulatoryGraph regGraph = null;
	
	private int mode;
	public TreeNode root = null;
	private JPanel graphEditor = null;
	private TreeParser parser;
	
	/**
	 * Create a new Tree and link it to the parser.
	 * @param parser the parser that will fill the tree.
	 */
	public TreeImpl(TreeParser parser) {
		super();
		this.parser = parser;
		parser.setTree(this);
	}

	/**
     * Indicates if the tree contains a node
     * @param node
     * @return true if the tree contains the node
     */
	@Override
    public boolean containsNode(TreeNode node) {
		return getNodes().contains(node);
	}
    

    // TODO : REFACTORING ACTION
	// TODO : Does this method has to be moved to GUI side?
	public JPanel getGraphParameterPanel() {
        if (graphEditor == null) {
            graphEditor = new TreeActionPanel(this, parser);
		}
		return graphEditor;	
	}
	
	/* adding edge and node */
//	/**
//	 * add a node to this graph.
//	 * @param node
//	 */
	// TODO REMOVE since it duplicates a method existing in AbstractGraphFrontend
//	public boolean addNode(TreeNode node) {
//		
//		return graphManager.addNode(node);
//	}
	/**
	 * add an edge between source and target
	 * @param source
	 * @param target
	 * @return the new edge
	 */
	@Override
	public Edge<TreeNode> addEdge(TreeNode source, TreeNode target) {
		
		Edge<TreeNode> edge = getEdge(source, target);
		if (edge == null) {
			edge = new Edge<TreeNode>(source, target);
			if (!addEdge(edge)) {
				return null;
			}
		}
		return edge;
	}
	
		
	/* Getters/ Setters */


	@Override
	public void setRoot( TreeNode root) {
		this.root = root;
	}

	@Override
	public int getMode() { 
		return mode; 
	}


	@Override
	public void setMode(int treeMode) { 
		this.mode = treeMode; 
	}
	
	@Override
	public TreeParser getParser() {
		return parser; 
	}

	
    /**
     * Return the size of the node order
     * 
     * @return the size of the node order
     */
    @Override
	public int getNodeOrderSize(){
    	return regGraph.getNodeOrderSize();
	}

	@Override
	public void doSave(OutputStreamWriter osw, Collection<TreeNode> vertices,
			Collection<Edge<TreeNode>> edges, int saveMode) {
	}

	@Override
	protected List<?> doMerge(Graph<TreeNode, Edge<TreeNode>> graph) {
		return null;
	}

	@Override
	public Graph<TreeNode, Edge<TreeNode>> getSubgraph(
			Collection<TreeNode> vertex, Collection<Edge<TreeNode>> edges) {
		return null;
	}

}