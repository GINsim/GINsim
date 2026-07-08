package org.ginsim.servicegui.tool.regulatorygraphanimation;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.ginsim.common.utils.ColorPalette;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.view.style.EdgeStyle;
import org.ginsim.core.graph.view.style.EdgeStyleOverride;
import org.ginsim.core.graph.view.style.NodeStyle;
import org.ginsim.core.graph.view.style.NodeStyleOverride;
import org.ginsim.core.graph.view.style.StyleManager;
import org.ginsim.core.graph.view.style.StyleProvider;

public class LRGStateStyleProvider implements StyleProvider<RegulatoryNode, RegulatoryMultiEdge> {

	private final StateColoredNodeStyle nodeStyle;
	private final StateColoredEdgeStyle edgeStyle;

	private final Map<RegulatoryNode, Integer> nodeIndex = new HashMap<RegulatoryNode, Integer>();

	private byte[] state = null;

	public LRGStateStyleProvider(RegulatoryGraph lrg) {

		// build node index
		int i = 0;
		for (RegulatoryNode node : lrg.getNodeOrder()) {
			nodeIndex.put(node, i);
			i++;
		}

		StyleManager<RegulatoryNode, RegulatoryMultiEdge> styleManager = lrg.getStyleManager();
		nodeStyle = new StateColoredNodeStyle(styleManager.getDefaultNodeStyle());
		edgeStyle = new StateColoredEdgeStyle(styleManager.getDefaultEdgeStyle());
	}

	public void setState(byte[] state) {
		this.state = state;
	}

	@Override
	public NodeStyle<RegulatoryNode> getNodeStyle(RegulatoryNode node, NodeStyle<RegulatoryNode> baseStyle) {
		if (state == null) {
			return baseStyle;
		}

		Integer i = nodeIndex.get(node);
		ActivityType activity;
		if (i == null || state[i] < 0) {
			activity = ActivityType.UNDEFINED;
		} else if (state[i] == 0) {
			activity = ActivityType.INACTIVE;
		} else if (state[i] == node.getMaxValue()) {
			activity = ActivityType.ACTIVE;
		} else {
			activity = ActivityType.PARTIAL;
		}

		nodeStyle.setBaseStyle(baseStyle, activity);
		return nodeStyle;
	}

	@Override
	public EdgeStyle<RegulatoryNode, RegulatoryMultiEdge> getEdgeStyle(RegulatoryMultiEdge edge,
			EdgeStyle<RegulatoryNode, RegulatoryMultiEdge> baseStyle) {
		if (state == null) {
			return baseStyle;
		}

		Integer i = nodeIndex.get(edge.getSource());
		boolean isActive = (i != null && state[i] >= edge.getMin(0));
		edgeStyle.setBaseStyle(baseStyle, isActive);
		return edgeStyle;
	}

	@Override
	public String getCSS() {
		StringBuffer sb = new StringBuffer();
		StateColoredNodeStyle.getCSS(sb);
		StateColoredEdgeStyle.getCSS(sb);
		return sb.toString();
	}
}

enum ActivityType {
	UNDEFINED, INACTIVE, ACTIVE, PARTIAL;
}

class StateColoredNodeStyle extends NodeStyleOverride<RegulatoryNode> {

	private ActivityType activity = ActivityType.INACTIVE;

	private static Color FREE = new Color(255, 220, 150);
	private static Color INACTIVE = new Color(255, 255, 255);
	private static Color ACTIVE = new Color(50, 50, 150);
	private static Color INTERMEDIATE = new Color(50, 150, 200);

	public StateColoredNodeStyle(NodeStyle<RegulatoryNode> style) {
		super(style);
	}

	static void getCSS(StringBuffer sb) {
		sb.append(".state_free .shape { fill: " + ColorPalette.getColorCode(FREE) + "; }\n");
		sb.append(".state_active .shape { fill: " + ColorPalette.getColorCode(ACTIVE) + "; }\n");
		sb.append(".state_active text { fill: " + ColorPalette.getColorCode(Color.WHITE) + "; }\n");
		sb.append(".state_inactive .shape { fill: " + ColorPalette.getColorCode(INACTIVE) + "; }\n");
		sb.append(".state_partial .shape { fill: " + ColorPalette.getColorCode(INTERMEDIATE) + "; }\n");
	}

	public void setBaseStyle(NodeStyle<RegulatoryNode> style, ActivityType activity) {
		super.setBaseStyle(style);
		this.activity = activity;
	}

	@Override
	public Color getBackground(RegulatoryNode obj) {
		switch (activity) {
			case UNDEFINED:
				return FREE;
			case ACTIVE:
				return ACTIVE;
			case PARTIAL:
				return INTERMEDIATE;
			default:
				return INACTIVE;
		}
	}

	@Override
	public Color getForeground(RegulatoryNode obj) {
		return Color.BLACK;
	}

	@Override
	public Color getTextColor(RegulatoryNode obj) {
		switch (activity) {
			case ACTIVE:
				return Color.WHITE;
			default:
				return Color.BLACK;
		}
	}

	@Override
	public String getCSSClass(RegulatoryNode node) {
		return "node " + getCSSClass(activity);
	}

	static String getCSSClass(ActivityType activity) {
		switch (activity) {
			case UNDEFINED:
				return "state_free";
			case ACTIVE:
				return "state_active";
			case INACTIVE:
				return "state_inactive";
			case PARTIAL:
				return "state_partial";
			default:
				return "";
		}
	}
}

class StateColoredEdgeStyle extends EdgeStyleOverride<RegulatoryNode, RegulatoryMultiEdge> {

	private boolean isActive = false;

	static Color ACTIVE = Color.BLUE.brighter();
	static Color INACTIVE = Color.GRAY;

	public StateColoredEdgeStyle(EdgeStyle<RegulatoryNode, RegulatoryMultiEdge> style) {
		super(style);
	}

	static void getCSS(StringBuffer sb) {
		sb.append(".edge_active { stroke: " + ColorPalette.getColorCode(ACTIVE) + "; }\n");
		sb.append(".edge_inactive { stroke: " + ColorPalette.getColorCode(INACTIVE) + "; }\n");
	}

	public void setBaseStyle(EdgeStyle<RegulatoryNode, RegulatoryMultiEdge> style, boolean isActive) {
		super.setBaseStyle(style);
		this.isActive = isActive;
	}

	@Override
	public Color getColor(RegulatoryMultiEdge obj) {
		if (isActive) {
			switch (obj.getSign()) {
				case POSITIVE:
					return Color.green;
				case NEGATIVE:
					return Color.red;
				default:
					return ACTIVE;
			}
		}
		return INACTIVE;
	}

	@Override
	public String getCSSClass(RegulatoryMultiEdge edge) {
		if (isActive) {
			return "edge edge_active";
		}
		return "edge edge_inactive";
	}

	@Override
	public int getWidth(RegulatoryMultiEdge edge) {
		if (isActive) {
			return 4;
		}
		return 1;
	}
}