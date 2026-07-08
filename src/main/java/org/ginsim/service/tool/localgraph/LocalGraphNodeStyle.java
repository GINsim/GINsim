package org.ginsim.service.tool.localgraph;

import java.awt.Color;

import org.ginsim.core.graph.regulatorygraph.ActivityLevel;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.view.style.NodeStyle;
import org.ginsim.core.graph.view.style.NodeStyleOverride;

class LocalGraphNodeStyle extends NodeStyleOverride<RegulatoryNode> {

	private final Color C_INACTIVE, C_MIDLEVEL, C_ACTIVE, C_FOREGROUND, C_FREE;
	private ActivityLevel level = ActivityLevel.INACTIVE;

	public LocalGraphNodeStyle(NodeStyle<RegulatoryNode> style) {
		super(style);
		C_INACTIVE = new Color(255, 255, 255);
		C_MIDLEVEL = new Color(50, 150, 200);
		C_ACTIVE = new Color(50, 50, 150);
		C_FREE = new Color(255, 220, 150);
		C_FOREGROUND = Color.BLACK;
	}

	public void setBaseStyle(NodeStyle<RegulatoryNode> style, ActivityLevel level) {
		super.setBaseStyle(style);
		this.level = level;
	}

	@Override
	public Color getBackground(RegulatoryNode obj) {
		switch (this.level) {
			case INACTIVE:
				return C_INACTIVE;
			case MIDLEVEL:
				return C_MIDLEVEL;
			case ACTIVE:
				return C_ACTIVE;
		}
		return C_FREE;
	}

	@Override
	public Color getForeground(RegulatoryNode obj) {
		return C_FOREGROUND;
	}

	@Override
	public Color getTextColor(RegulatoryNode obj) {
		return C_FOREGROUND;
	}

}