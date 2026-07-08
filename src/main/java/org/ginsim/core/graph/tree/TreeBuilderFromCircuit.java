package org.ginsim.core.graph.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.view.EdgeAttributesReader;


/**
 * A treeParser for the context of functionality
 *
 */
public class TreeBuilderFromCircuit extends TreeBuilder {
	protected final static int debug = 1;
	
	private MDDManager ddmanager;
	private int root;

    public static final String PARAM_INITIALCIRCUITDESC = "pcirc_initialCircuitDescr";
    public static final String PARAM_OPENCIRCUITDESC = "pcirc_openCircuitDescr";
    public static final String PARAM_ALLCONTEXTS = "pcirc_allCircuits";
    public static final String PARAM_MANAGER = "pcirc_manager";

	@SuppressWarnings("unchecked")
	public void init() {
        ddmanager = (MDDManager)getParameter(PARAM_MANAGER);
		root = (Integer) getParameter(PARAM_INITIALCIRCUITDESC);
		nodeOrder = (List<RegulatoryNode>)getParameter(PARAM_NODEORDER);

		widthPerDepth = widthPerDepth_acc = realDepth = null;
		total_levels = max_depth = 0;
		max_terminal = 3;
		initRealDepth(root);
	}

	
	/**
	 * Initialize the <b>realDepth</b> array, and <b>max_terminal</b> from an initial node, assuming regGraph is defined
	 * @param root
	 */
	public void initRealDepth(int root) {
		realDepth = new int[nodeOrder.size()+1]; //+1 for the leafs
		_initRealDepth(root);
		int next_realDepth = 0;
		for (int i = 0; i < realDepth.length; i++) {
			if (realDepth[i] == -1) {
				total_levels++;
				realDepth[i] = next_realDepth++;
			} else realDepth[i] = -2;
		}
	}

    public void _initRealDepth(int o) {
        if (ddmanager.isleaf(o)) {
            return ;
        }
        MDDVariable var = ddmanager.getNodeVariable(o);
        realDepth[var.order] = -1;
        for (int i = 0 ; i < var.nbval ; i++) {
            _initRealDepth(ddmanager.getChild(o,i));
        }
    }

	

    @Override
	protected String getNodeName(int level) {
		return nodeOrder.get(level).getId();
	}
	
    @Override
	public void parseOmdd() {
		if (tree.getMode() == TreeImpl.MODE_TREE) {
			createTreeFromOmdd(root);
		} else {
			createDiagramFromOmdd(root);
		}	
	}
	
	
	public void createDiagramFromOmdd(int root) {
		computeWidthPerDepthFromRegGraph();
		int[] currentWidthPerDepth = new int[widthPerDepth.length];
		tree.setRoot( _createDiagramFromOmdd(root, 0, currentWidthPerDepth, tree.getEdgeAttributeReader()));
	}
	private TreeNode _createDiagramFromOmdd(int o, int lastLevel, int[] currentWidthPerDepth, EdgeAttributesReader ereader) {
		TreeNode treeNode;
		int mult;
		if (ddmanager.isleaf(o)) {
			mult = jump(lastLevel, max_depth, currentWidthPerDepth);
		
			if (tree.getMode() == TreeImpl.MODE_DIAGRAM_WITH_MULTIPLE_LEAFS) {
				treeNode = new TreeNode(""+o, max_depth, ++currentWidthPerDepth[max_depth], TreeNode.TYPE_LEAF, (byte)o);
				if (mult > 1) currentWidthPerDepth[max_depth] += mult-1;
				tree.addNode(treeNode);
			} else { // if (mode == MODE_DIAGRAM) {
				if (o == -1) treeNode =  TreeImpl.MINUS_ONE_NODE;
				else treeNode = TreeImpl.leafs[o];
				if (!tree.containsNode(treeNode)) {
					tree.addNode(treeNode);
				}
			}
			return treeNode;
		}

        MDDVariable var = ddmanager.getNodeVariable(o);
		mult = jump(lastLevel, var.order, currentWidthPerDepth);

		treeNode = new TreeNode(getNodeName(var.order), var.order, ++currentWidthPerDepth[var.order], TreeNode.TYPE_BRANCH);
		tree.addNode(treeNode);
	
		for (int i = 0 ; i < var.nbval ; i++) { //For all the children
	    	TreeNode child = _createDiagramFromOmdd(ddmanager.getChild(o, i), var.order, currentWidthPerDepth, ereader);
	    	linkNode(treeNode, child, i);
	    }
		
		if (mult > 1) {
			mult = jump(var.order, max_depth, currentWidthPerDepth);
			if (mult > 1) {
				currentWidthPerDepth[max_depth] += mult;
			}
		}
	    return treeNode;
	}
	
	public void createTreeFromOmdd(int root) {
		computeWidthPerDepthFromRegGraph();
		int[] currentWidthPerDepth = new int[widthPerDepth.length];
		tree.setRoot (_createTreeFromOmdd(root, 0, null, 0, currentWidthPerDepth, tree.getEdgeAttributeReader()).get(0));
	}
	private List<TreeNode> _createTreeFromOmdd(int o, int lastLevel, TreeNode parent, int childIndex, int[] currentWidthPerDepth, EdgeAttributesReader ereader) {
		TreeNode treeNode = null;
		List<TreeNode> parents = new ArrayList<TreeNode>();
		parents.add(parent);
		int mult, last_real;
		if (ddmanager.isleaf(o)) {
			mult = 1;

			last_real = 0;
			for (int j = lastLevel+1 ; j < max_depth ; j++) { //For all the missing genes
				if (realDepth[j] != -2) {
					parents = addChildren(j, mult, parents, childIndex, currentWidthPerDepth, ereader);
					mult = widthPerDepth[j];
					last_real = j;
				}
			}
			for (Iterator<TreeNode> it = parents.iterator(); it.hasNext();) {
				TreeNode p = it.next();
				if (mult > 1) {
					for (int i = 0; i < widthPerDepth[last_real]; i++) {
						treeNode = new TreeNode(""+o, max_depth, ++currentWidthPerDepth[max_depth], TreeNode.TYPE_LEAF, (byte)o);
						tree.addNode(treeNode);
				    	linkNode(p, treeNode, i);
					}
				} else {
					treeNode = new TreeNode(""+o, max_depth, ++currentWidthPerDepth[max_depth], TreeNode.TYPE_LEAF, (byte)o);
					tree.addNode(treeNode);
			    	linkNode(p, treeNode, childIndex);	
				}
			}
			return null;
		}

        MDDVariable var = ddmanager.getNodeVariable(o);
		mult = 1;
		last_real = 0;
		List<TreeNode> skippedParents = parents;
		for (int j = lastLevel+1 ; j < var.order ; j++) { //For all the missing genes
			if (realDepth[j] != -2) {
				skippedParents = addChildren(j, mult, skippedParents, childIndex, currentWidthPerDepth, ereader);
				mult = widthPerDepth[j];
				last_real = j;
			}
		}
		List<TreeNode> currentNodes = new ArrayList<TreeNode>();
		int nodeCountToCreate = 1;
		if (mult > 1) nodeCountToCreate = widthPerDepth[last_real];
		for (int k = 0; k < skippedParents.size(); k++) {
			for (int i = 0; i < nodeCountToCreate; i++) {
				treeNode = new TreeNode(getNodeName(var.order), var.order, ++currentWidthPerDepth[var.order], TreeNode.TYPE_BRANCH);
				tree.addNode(treeNode);
				currentNodes.add(treeNode);
		
				for (int j = 0 ; j < var.nbval ; j++) { //For all the children
                    int jchild = ddmanager.getChild(o, j);
					if (jchild > -1) {
				    	List<TreeNode> childs = _createTreeFromOmdd(jchild, var.order, treeNode, j, currentWidthPerDepth, ereader);
						if (childs != null) {
							for (Iterator<TreeNode> it2 = childs.iterator(); it2 .hasNext();) {
								TreeNode child = it2.next();
								linkNode(treeNode, child, j);
								
							}
						}
					}
			    }
			}
		}

		if (skippedParents.equals(parents)) {
		    return currentNodes;
		} else {
			int max = currentNodes.size()/skippedParents.size();
			for (int i = 0; i < skippedParents.size(); i++) {
				TreeNode p = skippedParents.get(i);
				for (int j = 0; j < max; j++) {
					TreeNode child = currentNodes.get(j+i*max);
					linkNode(p, child, j);
				}
			}
			return null;
		}
	}

	

}
