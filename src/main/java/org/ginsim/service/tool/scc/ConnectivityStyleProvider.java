package org.ginsim.service.tool.scc;

import org.ginsim.common.utils.ColorPalette;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.reducedgraph.NodeReducedData;
import org.ginsim.core.graph.view.style.*;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Color a graph according to its connected components.
 *
 * @author Aurelien Naldi
 */
public class ConnectivityStyleProvider implements StyleProvider {

    public static final Color TRANSIENT_NODE = Color.white;
    public static final Color TERMINAL_NODE = Color.red.darker();

    public static final Color TRANSIENT_CYCLE = Color.gray;
    public static final Color TERMINAL_CYCLE = Color.orange;

    public static final Color[] TRANSIENT_PALETTE = ColorPalette.createColorPaletteByHue(25, (float) 0.5, (float) 0.3, (float) 0.6, (float) 0.2);
    public static final Color[] TERMINAL_PALETTE  = ColorPalette.createColorPaletteByHue(25, (float)1.0, (float)0.3, (float)0.9, (float)0.1);

    private final SCCNodeStyle nodeStyle;


    public ConnectivityStyleProvider(List<NodeReducedData> components, Graph graph) {
        StyleManager manager = graph.getStyleManager();

        // build a color cache
        Map<Object, Color> m_colors = new HashMap<Object, Color>();
        int transient_count = 0;
        int attractor_count = 0;
        for (NodeReducedData nrd: components) {
            boolean isTransient = nrd.isTransient(graph);
            if (nrd.isTrivial()) {
                if (isTransient) {
                    continue;
                }
                m_colors.put(nrd.getContent().get(0), TERMINAL_NODE);
            } else if (isTransient) {
                Color color = TRANSIENT_PALETTE[transient_count % TRANSIENT_PALETTE.length];
                transient_count += 6;
                for (Object node: nrd.getContent()) {
                    m_colors.put(node, color);
                }
            } else {
                Color color = TERMINAL_PALETTE[attractor_count % TERMINAL_PALETTE.length];
                attractor_count += 6;
                for (Object node: nrd.getContent()) {
                    m_colors.put(node, color);
                }
            }
        }

        nodeStyle = new SCCNodeStyle(manager.getDefaultNodeStyle(), m_colors);

    }

    @Override
    public Map<Object, Color>  getMColorsMap(){
        return nodeStyle.getMColorsMap();
    }
    @Override
    public NodeStyle getNodeStyle(Object node, NodeStyle baseStyle) {
        nodeStyle.setBaseStyle(baseStyle);
        return nodeStyle;
    }

    @Override
    public EdgeStyle getEdgeStyle(Edge edge, EdgeStyle baseStyle) {
        return baseStyle;
    }
}


class SCCNodeStyle extends NodeStyleOverride {

    private final Map<Object, Color> m_colors;

    public SCCNodeStyle(NodeStyle defaultStyle, Map<Object, Color> m_colors) {
        super(defaultStyle);
        this.m_colors = m_colors;
    }

    public Map<Object, Color>  getMColorsMap(){
        return this.m_colors;
    }

    @Override
    public Color getBackground(Object obj) {
        Color color = m_colors.get(obj);
        if (color == null) {
            return ConnectivityStyleProvider.TRANSIENT_NODE;
        }
        return color;
    }

    @Override
    public Color getForeground(Object obj) {
        return Color.BLACK;
    }

    @Override
    public Color getTextColor(Object obj) {
        return Color.BLACK;
    }
}
