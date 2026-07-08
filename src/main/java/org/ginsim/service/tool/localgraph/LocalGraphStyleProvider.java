package org.ginsim.service.tool.localgraph;

import java.util.Map;

import org.ginsim.core.graph.regulatorygraph.ActivityLevel;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.view.style.EdgeStyle;
import org.ginsim.core.graph.view.style.NodeStyle;
import org.ginsim.core.graph.view.style.StyleManager;
import org.ginsim.core.graph.view.style.StyleProvider;

public class LocalGraphStyleProvider implements StyleProvider<RegulatoryNode, RegulatoryMultiEdge> {

	private Map<RegulatoryMultiEdge, LocalGraphCategory> functionalityMap;
	private Map<RegulatoryNode, ActivityLevel> activityMap;
	private final LocalGraphEdgeStyle edgeStyle;
	private final LocalGraphNodeStyle nodeStyle;

	public LocalGraphStyleProvider(StyleManager<RegulatoryNode, RegulatoryMultiEdge> styleManager,
			Map<RegulatoryMultiEdge, LocalGraphCategory> functionalityMap,
			Map<RegulatoryNode, ActivityLevel> activityMap) {

		this.edgeStyle = new LocalGraphEdgeStyle(styleManager.getDefaultEdgeStyle());
		this.nodeStyle = new LocalGraphNodeStyle(styleManager.getDefaultNodeStyle());
		this.functionalityMap = functionalityMap;
		this.activityMap = activityMap;
	}

	@Override
	public NodeStyle<RegulatoryNode> getNodeStyle(RegulatoryNode node, NodeStyle<RegulatoryNode> baseStyle) {
		if (activityMap == null) {
			return baseStyle;
		}

		ActivityLevel level = activityMap.get(node);
		if (level == null) {
			level = ActivityLevel.INACTIVE;
		}
		nodeStyle.setBaseStyle(baseStyle, level);
		return nodeStyle;
	}

	@Override
	public EdgeStyle<RegulatoryNode, RegulatoryMultiEdge> getEdgeStyle(RegulatoryMultiEdge edge,
			EdgeStyle<RegulatoryNode, RegulatoryMultiEdge> baseStyle) {
		LocalGraphCategory category = functionalityMap.get(edge);
		if (category == null) {
			category = LocalGraphCategory.NONFUNCTIONNAL;
		}
		edgeStyle.setBaseStyle(baseStyle, category);
		return edgeStyle;
	}

	public void setMapping(Map<RegulatoryMultiEdge, LocalGraphCategory> functionalityMap,
			Map<RegulatoryNode, ActivityLevel> activityMap) {
		this.functionalityMap = functionalityMap;
		this.activityMap = activityMap;
	}

}
