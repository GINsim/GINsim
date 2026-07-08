package org.ginsim.core.io.parser;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.ginsim.common.application.LogManager;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.view.EdgeAnchor;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.EdgePattern;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.graph.view.ViewHelper;
import org.ginsim.core.graph.view.style.EdgeStyle;
import org.ginsim.core.graph.view.style.NodeStyle;
import org.ginsim.core.graph.view.style.StyleManager;
import org.xml.sax.Attributes;



/**
 * some help function to parse/write ginml files.
 * it is mainly here to share visual settings's code
 */
public class GinmlHelper {
	
    /** default URL of the DTD */
    public static final String DEFAULT_URL_DTD_FILE = "https://ginsim.github.io/GINML_2_2.dtd";
    public static final String DOCTYPE = "gxl SYSTEM \""+DEFAULT_URL_DTD_FILE+"\"";

    /**
     * we are reading node visual settings from a ginml file, apply them on
     * the current node.
     * 
     * @param vareader
     * @param qName
     * @param attributes
     * 1 if byte VS, 2 otherwise
     */
	public static void applyNodeVisualSettings(NodeAttributesReader vareader, StyleManager stylemanager, String qName, Attributes attributes) {
        vareader.setPos(Integer.parseInt(attributes.getValue("x")),Integer.parseInt(attributes.getValue("y")));
        NodeStyle style = stylemanager.guessNodeStyle(qName, attributes);
        vareader.setStyle(style);
	}

	/**
     * we are reading edge visual settings from a ginml file, apply them on
     * the current edge.
     * 
	 * @param ereader
	 * @param qName
	 * @param attributes
	 */
	public static void applyEdgeVisualSettings(Edge<?> edge, StyleManager styleManager, EdgeAttributesReader ereader, NodeAttributesReader nreader, String qName, Attributes attributes) {
		if (!qName.equals("polyline")) {
			return;
		}
		
		EdgeStyle style = styleManager.guessEdgeStyle(qName, attributes);
		ereader.setStyle(style);
		
		String s = attributes.getValue("line_style");
		if (s.equals("curve") || s.equals("13") || s.equals("12") || s.equals("bezier") || s.equals("spline")) {
		    ereader.setCurve(true);
		}

        s = attributes.getValue("points");
        List<Point> l = loadPoints(s);
        if (l != null) {
			ViewHelper.trimPoints(edge, l, nreader, ereader);
			ereader.setPoints(l);
		}

		ereader.refresh();
	}

	/**
	 * Load style and intermediate points from GINML.
	 * 
	 * @param styleManager
	 * @param ereader
	 * @param attributes
	 * @return true if no data was found and the old visual settings must be loaded
	 */
	public static boolean loadEdgeStyle(StyleManager styleManager, EdgeAttributesReader ereader, Attributes attributes) {
    	boolean loadOldVS = true;

    	String value = attributes.getValue("points");
    	if (value != null) {
    		loadOldVS = false;
    		ereader.setPoints( GinmlHelper.loadPoints(value) );
    	}

        value = attributes.getValue("curve");
        if (value!= null && !value.equalsIgnoreCase("false")) {
            ereader.setCurve(true);
        }
        
        value = attributes.getValue("anchor");
        if (value != null) {
        	EdgeAnchor anchor = EdgeAnchor.valueOf(value);
            ereader.setAnchor(anchor);
        }
        
        value = attributes.getValue("style");
    	if (value != null) {
    		loadOldVS = false;
    		ereader.setStyle( styleManager.getEdgeStyle(value) );
    	}
    	
    	return loadOldVS;
	}

	/**
	 * Load node position and style from GINML
	 * 
	 * @param styleManager
	 * @param vreader
	 * @param attributes
	 * @return true if no data was found and the old visual settings must be loaded
	 */
	public static boolean loadNodeStyle(StyleManager styleManager, NodeAttributesReader vreader, Attributes attributes) {
    	boolean loadOldVS = true;
    	String sx = attributes.getValue("x");
    	String sy = attributes.getValue("y");
    	if (sx != null && sy != null) {
    		loadOldVS = false;
    		int x = Integer.parseInt(sx);
    		int y = Integer.parseInt(sy);
    		vreader.setPos(x, y);
    	}

    	sx =  attributes.getValue("style");
    	if (sx != null) {
    		loadOldVS = false;
    		vreader.setStyle( styleManager.getNodeStyle(sx) );
    	}
    	return loadOldVS;
	}

	/**
	 * Parse a string representing a list of points
	 * 
	 * @param s the string to parse
	 * @return a proper list of points
	 */
	private static List<Point> loadPoints(String s) {
		if (s == null) {
			return null;
		}
		s = s.trim();
		if (s.length() == 0) {
			return null;
		}
		String[] ts = s.split(" ");
	    try {
			List l = new ArrayList();
			for (int j=0 ; j<ts.length ; j++) {
			    String[] t_point = ts[j].split(",");
		        l.add(new Point(Integer.parseInt(t_point[0]),Integer.parseInt(t_point[1])));
			}
			return l;
	    } catch (Exception e) {
	        LogManager.error("invalid points");
	    }
	    return null;
	}
}
