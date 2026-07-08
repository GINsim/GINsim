package org.ginsim.core.graph.view.css;

/**
 * 
 * A simple selector with two categories to select all the nodes or all the edges.
 * 
 * selectors : *.node and *.edges
 *
 */
public class AllSelector extends Selector {
	/**
	 * Define String IDENTIFIER
	 */
	public static final String IDENTIFIER = "all";
	/**
	 * Define String CAT_NODES
	 */
	public static final String CAT_NODES = "nodes";
	/**
	 * Define String CAT_EDGES
	 */
	public static final String CAT_EDGES = "edges";
	/**
	 * define style CSSNodeStyle STYLE_NODES
	 */
	public static final CSSNodeStyle STYLE_NODES = new CSSNodeStyle();
	/**
	 * define CSSEdgeStyle   STYLE_EDGES
	 */
	public static final CSSEdgeStyle   STYLE_EDGES = new CSSEdgeStyle();
	/**
	 * constructor
	 */
	public AllSelector() {
		super(IDENTIFIER);
	}

	/**
	 * Reset function
	 */
	public void resetDefaultStyle() {
		addCategory(CAT_NODES, (CSSStyle)STYLE_NODES.clone());
		addCategory(CAT_EDGES, (CSSStyle)STYLE_EDGES.clone());		
	}

	/**
	 * Getter Edge category
	 * @param obj object not used
	 * @return value string CAT_EDGES
	 */
	public String getCategoryForEdge(Object obj) {
		return CAT_EDGES;
	}

	/**
	 * Getter node category
	 * @param obj object not used
	 * @return value string CAT_NODE
	 */
	public String getCategoryForNode(Object obj) {
		return CAT_NODES;
	}

}
