package org.ginsim.core.graph.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ginsim.common.application.LogManager;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;


public abstract class TreeBuilder {
	public static final String PARAM_NODEORDER = "p_nodeOrder";
	protected Tree tree;
	protected List<RegulatoryNode> nodeOrder;
	private Map<String, Object> parameters;

	/**
	 * Indicates the maximum count of terminal.
	 */
	protected int max_terminal;
	/**
	 * Indicates for each depth, the sub-total (width) of children or 0 if the depth is not skipped (realDepth == -1).
	 */
	protected int[] widthPerDepth;
	/**
	 * Indicates for each depth, the total (width) of children or 0 if the depth is not skipped (realDepth == -1).
	 */
	protected int[] widthPerDepth_acc;
	/**
	 * As the omdd diagram could not contain all the levels (some could be skipped...), 
	 * we try to reduce the tree width, by assigning a depth to each __used__ level.
	 * 
	 * This can be achieved in two ways, 
	 * 		* by a first pass on the omdd (good for diagram)
	 *      * by computing the incoming vertices from the regulatoryGraph (good for tree)
	 *      
	 * A value of -2, indicates the depth correspond to a skipped level.
	 */
	protected int[] realDepth;
	/**
	 * The total number of levels that are not skipped
	 */
	protected int total_levels;
	/**
	 * The level of the last depth (corresponding to the terminal node in widthPerDepth)
	 */
	protected int max_depth;


	
	/**
	 * Run the parser on the OMDDNode root, and fill the tree.
	 * 
	 * @param treeMode the kind of tree to parse (tree, diagram or diagramWithAllleafs)
	 */
	@SuppressWarnings("unchecked")
	public void run(int treeMode) {
		tree.setMode(treeMode);
		this.nodeOrder = (List<RegulatoryNode>) getParameter(PARAM_NODEORDER);
		
//		LogManager.info("Clearing the tree");
		clearTree();			//Remove all the treeNode from the current tree
//		LogManager.info("Pretreatment");
		init();					//Init the variables needed for the parsing
//		LogManager.info("Parsing the tree");
		parseOmdd();			//Launch the correct parser depending on the mode.
//		LogManager.info("Updating the graphical attributes of the tree, and refreshing the GUI");
		updateNodeLayout();	//Refresh the position and style of the GsTreeNodes
//		LogManager.info("Tree constructed");
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}
	
	/**
	 * This method is called before parsing the tree. Should initialize all the side variables.
	 */
	public abstract void init();

	public abstract void parseOmdd();
	
	
	/**
	 * Return the name of the node at level
	 * @param level the level of the node
	 * @return the name
	 */
	protected abstract String getNodeName(int level);

	
	/**
	 * Compute the layout for each node in this tree
	 */
	public void updateNodeLayout() {
		NodeAttributesReader vreader = tree.getNodeAttributeReader();
		for (TreeNode vertex: tree.getNodes()) {
			updateLayout(vreader, vertex);
		}		
	}
	
	protected void updateLayout(NodeAttributesReader vreader, TreeNode vertex) {
		vreader.setNode(vertex);
		int total_width = getTerminalWidth()*TreeNode.PADDING_HORIZONTAL;
		if (vertex.getType() == TreeNode.TYPE_LEAF) {
			if (vertex.getDepth() != -1) {
	    		vreader.setPos((int)((vertex.getWidth()-0.5)*total_width/getWidthPerDepth_acc(vertex))+100, getTotalLevels()*TreeNode.PADDING_VERTICAL+40);
			} else {
	    		vreader.setPos((int)((vertex.getWidth()+0.5)*total_width/getMaxTerminal())+100, getTotalLevels()*TreeNode.PADDING_VERTICAL+40);
			}
		} else {
			vreader.setPos((int)((vertex.getWidth()-0.5)*total_width/getWidthPerDepth_acc(vertex))+100, (getRealDepth(vertex)+1)*TreeNode.PADDING_VERTICAL-40);
		}
		vreader.refresh();
	}

	protected void computeWidthPerDepthFromRegGraph() {
		widthPerDepth = new int[nodeOrder.size()+1];
		widthPerDepth_acc = new int[nodeOrder.size()+1];

		int last_real = -1;

		Iterator<RegulatoryNode> it = nodeOrder.iterator();
		for (int i = 0 ; it.hasNext() ; i++) {
			RegulatoryNode v = it.next();
			if (realDepth[i] != -2) {
				int max = v.getMaxValue()+1;
				widthPerDepth[i] = max;
				if (last_real != -1) widthPerDepth_acc[i] = widthPerDepth_acc[last_real] * widthPerDepth[last_real];
				else widthPerDepth_acc[i] = 1;
				last_real = i;
			}
		}
		max_depth = last_real+1;
		if (last_real != -1) widthPerDepth_acc[max_depth] = widthPerDepth_acc[last_real] * widthPerDepth[last_real];
	}	

	
	/**
	 * Remove all the vertices from the tree
	 */
	public void clearTree() {
		List<TreeNode> tmp = new ArrayList<TreeNode>( tree.getNodeCount());
		for (TreeNode vertex: tree.getNodes()) {
			tmp.add(vertex);
		}
		for (Iterator<TreeNode> it = tmp.iterator(); it.hasNext();) {
			TreeNode vertex = it.next();
			tree.removeNode(vertex);
		}
	}	
	
	public Object getParameter(String key) {
		Object value = this.parameters.get(key);
		if (value == null) {
            LogManager.debug("expected parameter '" + key + "' not found or null");
		}
		return value;
	}
	
	public void setParameter(String key, Object value) {
		if (parameters == null) {
			parameters = new HashMap<String, Object>();
		}
		parameters.put(key, value);
	}
	
	/**
	 * Little helper function to write a tree in stdout
	 * @param tabs the count of tab to output
	 * @return a string with <b>tab</b> tabs
	 */
	protected String tab(int tabs) {
    	if (tabs <= 0) return "";
    	StringBuffer s = new StringBuffer(tabs);
    	for (int i = 0; i <= tabs; i++) {
			s.append("   ");
		}
		return s.toString();
	}
	
	protected List<TreeNode> addChildren(int j, int mult, List<TreeNode> parents, int childIndex, int[] currentWidthPerDepth, EdgeAttributesReader ereader) {
		List<TreeNode> newParents = new ArrayList<TreeNode>(mult);
		
		while (realDepth[j] == -2 && j < max_depth) j++; //Get the child level
		
		String parentId = getNodeName(j);
		
		for (Iterator<TreeNode> it = parents.iterator(); it.hasNext();) {
			TreeNode o  = (TreeNode) it.next();
			for (int i = 0 ; i < mult ; i++) {
				TreeNode treeNode = new TreeNode(parentId, j, ++currentWidthPerDepth[j], TreeNode.TYPE_BRANCH, TreeNode.SKIPPED);
				newParents.add(treeNode);
				tree.addNode(treeNode);
				linkNode(o, treeNode, childIndex);
			}
		}
		return newParents;
	}
	
	/**
	 * Try to jump over the skipped nodes between lastLevel and maxLevel and fill currentWidthPerDepth accordingly.
	 * @param lastLevel
	 * @param maxLevel
	 * @param currentWidthPerDepth
	 * @return the max width of skipped nodes
	 */
	protected int jump(int lastLevel, int maxLevel, int[] currentWidthPerDepth) {
		int mult = 1;
		for (int j = lastLevel+1 ; j < maxLevel ; j++) { //For all the missing genes
			if (realDepth[j] != -2) {
				currentWidthPerDepth[j] += mult;
				mult *= widthPerDepth[j];
			}
		}
		return mult;
	}

	
	/**
	 * Create an edge from source to target
	 * Also set the right color according to colorIndex, and the dashed line if the target is a leaf.
	 * 
	 * @param source
	 * @param target
	 * @param colorIndex
	 */
	protected void linkNode(TreeNode source, TreeNode target, int colorIndex) {
		Edge<?> e = tree.addEdge(source, target, colorIndex);
	}

	
	public int[] getWidthPerDepth() { return widthPerDepth; }
	public int[] getWidthPerDepth_acc() { return widthPerDepth_acc; }
	public int getMaxTerminal() { return max_terminal; }
	public int getMaxDepth() { return max_depth; }
	public int[] getRealDepth() { return realDepth; }
	public int getTerminalWidth() { return widthPerDepth_acc[max_depth]; }
	public int getTotalLevels() { return total_levels; }
	
	protected int getRealDepth(TreeNode node) {
		if (node.getDepth() == TreeNode.LEAF_DEFAULT_DEPTH) return getMaxDepth();
		return getRealDepth()[node.getDepth()];
	}
	protected int getWidthPerDepth_acc(TreeNode node) {
		if (node.getDepth() == TreeNode.LEAF_DEFAULT_DEPTH) return getWidthPerDepth_acc()[getMaxDepth()];
		return getWidthPerDepth_acc()[node.getDepth()];
	}

	
}