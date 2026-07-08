package org.ginsim.core.graph.view.css;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.ginsim.common.utils.ColorPalette;
import org.ginsim.core.graph.view.AttributesReader;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.EdgeEnd;




/**
 * EdgeStyle store some graphical attributes of an edge
 * Attributes : 
 *     - lineColor : the line color = any Color
 *     - shape : the style of the edge = straight or curve
 *     - lineEnd : the line end = positive, negative, double or unknown
 */
public class CSSEdgeStyle implements CSSStyle {
	public final static float NULL_BORDER = -99;
	public final static int NULL_CURVE = -99;

	public final static String CSS_LINECOLOR		= "line-color";
	public final static String CSS_SHAPE			= "shape";
	public final static String CSS_SHAPE_STRAIGHT	= "straight";
	public final static String CSS_SHAPE_CURVE		= "curve";
	public final static String CSS_LINEEND			= "line-end";
	public final static String CSS_LINEEND_POSITIVE	= "positive";
	public final static String CSS_LINEEND_NEGATIVE	= "negative";
	public final static String CSS_LINEEND_DUAL		= "double";
	public final static String CSS_LINEEND_UNKNOWN	= "unknown";
	public final static String CSS_BORDER			= "border";
	
	public Color lineColor;
	public int curve;
	public EdgeEnd lineEnd;
	public float border;
	
	
	static Pattern parserPattern = null;

	/**
	 * A new style from the with all values to NULL
	 */
	public CSSEdgeStyle() {
		this(null, null, NULL_CURVE, NULL_BORDER);
	}

	public CSSEdgeStyle(Color lineColor) {
		this(lineColor, null, NULL_CURVE, NULL_BORDER);
	}

	
	/**
	 * A new style from the scratch
	 * @param lineColor the lineColor color for the node
	 * @param  curve the shape for the line
	 * @param lineEnd the style for the end of the line
	 * @param border  border length
	 * 
	 * @see EdgeAttributesReader
	 */
	public CSSEdgeStyle(Color lineColor, EdgeEnd lineEnd, int curve, float border) {
		this.lineColor	= lineColor;
		this.lineEnd 	= lineEnd;
		this.curve 		= curve;
		this.border 	= border;
	}

	/**
	 * A new style from a GsAttributesReader areader
 	 * @param areader
 	 */
	public CSSEdgeStyle(EdgeAttributesReader areader) {
		lineColor = areader.getLineColor();
		curve     = areader.isCurve() ? 1 : 0;
		lineEnd   = areader.getLineEnd();
		border    = areader.getLineWidth();
	}
	
	/**
	 * A new style copied from another
	 * @param s
	 */
	public CSSEdgeStyle(CSSEdgeStyle s) {
		lineColor 	= s.lineColor;
		curve 		= s.curve;
		lineEnd 	= s.lineEnd;
		border	 	= s.border;
	}
	
	public void merge(CSSStyle st) {
		if (!(st instanceof CSSEdgeStyle)) {
			return;
		}
		CSSEdgeStyle s = (CSSEdgeStyle) st;
		if (s.lineColor != null)     lineColor  = s.lineColor;
		if (s.lineEnd != null)       lineEnd    = s.lineEnd;		
		if (s.border != NULL_BORDER) border     = s.border;
		if (s.curve != NULL_CURVE)   curve      = s.curve;
	}

	public void apply(AttributesReader areader) {
		if (!(areader instanceof EdgeAttributesReader)) {
			return;
		}
		EdgeAttributesReader ereader = (EdgeAttributesReader)areader;
		
		// FIXME: reimplement CSS styling using styles
		
		ereader.damage();
	}

	@Override
	public void setProperty(String property, String value, int i) throws CSSSyntaxException {
		if (property.equals(CSS_LINECOLOR)) {
			try {
				lineColor = ColorPalette.getColorFromCode(value.toUpperCase());
			} catch (NumberFormatException e) {
				throw new CSSSyntaxException("Malformed color code at line "+i+" found "+value+". Must be from 000000 to FFFFFF");
			}
		} else if (property.equals(CSS_SHAPE)) {
			if 		(value.equals(CSS_SHAPE_CURVE)) 	curve = 1;
			else if (value.equals(CSS_SHAPE_STRAIGHT)) 	curve = 0;
			else throw new CSSSyntaxException("Unknown edge shape at line "+i+" found "+value+". Must be "+CSS_SHAPE_CURVE+" or "+CSS_SHAPE_STRAIGHT);
		} else if (property.equals(CSS_BORDER)) {
			try {
				border = Float.parseFloat(value);
			} catch (NumberFormatException e) {
				throw new CSSSyntaxException("Malformed border value at line "+i+" found "+value+". Must be a float");
			}
			
		} else if (property.equals(CSS_LINEEND)) {
			if 		(value.equals(CSS_LINEEND_POSITIVE)) 	lineEnd = EdgeEnd.POSITIVE;
			else if (value.equals(CSS_LINEEND_NEGATIVE)) 	lineEnd = EdgeEnd.NEGATIVE;
			else if (value.equals(CSS_LINEEND_DUAL))	 	lineEnd = EdgeEnd.DUAL;
			else if (value.equals(CSS_LINEEND_UNKNOWN)) 	lineEnd = EdgeEnd.UNKNOWN;
			else throw new CSSSyntaxException("Unknown edge lineEnd at line "+i+" found "+value+". Must be "+CSS_LINEEND_POSITIVE+", "+CSS_LINEEND_NEGATIVE+", "+CSS_LINEEND_DUAL+" or "+CSS_LINEEND_UNKNOWN);
		} else {
			throw new CSSSyntaxException("Edge has no key "+value+" at line "+i+" found "+value+". Must be "+CSS_LINECOLOR+", "+CSS_SHAPE+", "+CSS_BORDER+" or "+CSS_LINEEND);
		}
		
	}

	
	/**
	 * a css string representation of this style.
	 */
	public String toString() {
		return toString(0);
	}
	
	/**
	 * a css string representation of this style.
	 * @param tabs_count the number of tabulations to append at the beginning of each line
	 */
	public String toString(int tabs_count) {
		String s = "", tabs = "\t";
		for (int i = 1; i < tabs_count; i++) {
			tabs += "\t";
		}
		if (lineColor!= null) s += tabs+CSS_LINECOLOR+": "+ColorPalette.getColorCode(lineColor)+";\n";
		if (curve != NULL_CURVE) s += tabs+CSS_SHAPE+": "+(curve==1 ? CSS_SHAPE_CURVE:CSS_SHAPE_STRAIGHT)+";\n";
		if (border != NULL_BORDER) s += tabs+CSS_BORDER+": "+border+";\n";
		if (lineEnd != null) {
			s += tabs+CSS_LINEEND+": ";
			switch (lineEnd) {
			case POSITIVE:
				s += CSS_LINEEND_POSITIVE;
				break;
			case NEGATIVE:
				s += CSS_LINEEND_NEGATIVE;
				break;
			case DUAL:
				s += CSS_LINEEND_DUAL;
				break;
			case UNKNOWN:
				s += CSS_LINEEND_UNKNOWN;
				break;
			}
			s += ";\n";
		}
		return s;
	}
	
	/**
	 * Create a new style from an array of strings
	 * 
	 * @param lines
	 * @return the new style
	 * @throws PatternSyntaxException
	 * @throws CSSSyntaxException if there is an error in the syntax
	 */
	public static CSSStyle fromString(String []lines) throws PatternSyntaxException, CSSSyntaxException {
		Color lineColor = null;
		int curve = NULL_CURVE;
		EdgeEnd lineEnd = null;
		float border = NULL_BORDER;
		
		if (parserPattern == null) parserPattern = Pattern.compile("([a-zA-Z0-9\\-_]+):\\s*#?([a-zA-Z0-9\\-_]+);");
		
		for (int i = 0; i < lines.length; i++) {
			Matcher m = parserPattern.matcher(lines[i].trim());
			String key = m.group(1).trim(), value = m.group(2).trim();
			
			if (m.groupCount() < 2) throw new CSSSyntaxException("Malformed line "+i+" : "+lines[i]+". Must be 'key: value;'");
			if (key.equals(CSS_LINECOLOR)) {
				try {
					lineColor = ColorPalette.getColorFromCode(value);
				} catch (NumberFormatException e) {
					throw new CSSSyntaxException("Malformed color code at line "+i+" : "+lines[i]+". Must be from 000000 to FFFFFF");
				}
			} else if (key.equals(CSS_SHAPE)) {
				if 		(value.equals(CSS_SHAPE_CURVE)) 	curve = 1;
				else if (value.equals(CSS_SHAPE_STRAIGHT)) 	curve = 0;
				else throw new CSSSyntaxException("Unknown edge shape at line "+i+" : "+lines[i]+". Must be "+CSS_SHAPE_CURVE+" or "+CSS_SHAPE_STRAIGHT);
			} else if (key.equals(CSS_LINEEND)) {
				if 		(value.equals(CSS_LINEEND_POSITIVE)) 	lineEnd = EdgeEnd.POSITIVE;
				else if (value.equals(CSS_LINEEND_NEGATIVE)) 	lineEnd = EdgeEnd.NEGATIVE;
				else if (value.equals(CSS_LINEEND_DUAL))	 	lineEnd = EdgeEnd.DUAL;
				else if (value.equals(CSS_LINEEND_UNKNOWN)) 	lineEnd = EdgeEnd.UNKNOWN;
				else throw new CSSSyntaxException("Unknown edge lineEnd at line "+i+" : "+lines[i]+". Must be "+CSS_LINEEND_POSITIVE+", "+CSS_LINEEND_NEGATIVE+", "+CSS_LINEEND_DUAL+" or "+CSS_LINEEND_UNKNOWN);
			} else {
				throw new CSSSyntaxException("Edge has no key "+key+" at line "+i+" : "+lines[i]+". Must be "+CSS_LINECOLOR+", "+CSS_SHAPE+" or "+CSS_LINEEND);
			}
		}
		return new CSSEdgeStyle(lineColor, lineEnd, curve, border);
	}

	public Object clone() {
		return new CSSEdgeStyle(this);
	}

}
