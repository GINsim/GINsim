package org.ginsim.core.graph.view.css;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.ginsim.common.utils.ColorPalette;
import org.ginsim.core.graph.view.AttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.graph.view.NodeBorder;
import org.ginsim.core.graph.view.NodeShape;



/**
 * NodeStyle store some graphical attributes of a node
 * Attributes : 
 *     - background : the color for the background = any Color
 *     - foreground : the color for the text and the border = any Color
 *     - border : the style of the border = simple, raised or strong
 *     - shape : the shape = ellipse or rectangle
 */
public class CSSNodeStyle implements CSSStyle {

	public final static String CSS_BACKGROUND		= "background";
	public final static String CSS_FOREGROUND		= "foreground";
	public final static String CSS_TEXTCOLOR		= "text-color";
	public final static String CSS_SHAPE			= "shape";
	public final static String CSS_BORDER			= "border";
	
	public final static String CSS_SHAPE_ELLIPSE	= "ellipse";
	public final static String CSS_SHAPE_RECTANGLE	= "rectangle";
	public final static String CSS_BORDER_SIMPLE	= "simple";
	public final static String CSS_BORDER_RAISED	= "raised";
	public final static String CSS_BORDER_STRONG	= "strong";

	public Color background;
	public Color foreground;
	public Color textcolor;
	public NodeBorder border;
	public NodeShape shape;
	
	static Pattern parserPattern = null;

	/**
	 * A new style from the with all values to NULL
	 */
	public CSSNodeStyle() {
		this(null, null, null, null, null);
	}

	/**
	 * A new style from the scratch
	 * @param background the background color for the node
	 * @param foreground the foreground color for the node
	 * @param border the style for the line 
	 * @param shape the shape for the line 
	 * 
	 * @see NodeAttributesReader
	 */
	public CSSNodeStyle(Color background, Color foreground, Color textcolor, NodeBorder border, NodeShape shape) {
		this.background = background;
		this.foreground = foreground;
		this.textcolor  = textcolor;
		this.border 	= border;
		this.shape 		= shape;
	}

	
	/**
	 * A new style from a GsAttributesReader areader
 	 * @param areader
 	 */
	public CSSNodeStyle(NodeAttributesReader areader) {
		background 	= areader.getBackgroundColor();
		foreground 	= areader.getForegroundColor();
		textcolor 	= areader.getTextColor();
		border 		= areader.getBorder();
		shape 		= areader.getShape();
	}
	
	/**
	 * A new style copied from another
	 * @param s
	 */
	public CSSNodeStyle(CSSNodeStyle s) {
		background 	= s.background;
		foreground 	= s.foreground;
		textcolor 	= s.textcolor;
		border 		= s.border;
		shape 		= s.shape;
	}
	
	public void merge(CSSStyle sa) {
		if (!(sa instanceof CSSNodeStyle)) {
			return;
		}
		
		CSSNodeStyle s = (CSSNodeStyle)sa; 
		if (s.background != null)   background = s.background;
		if (s.foreground != null)   foreground = s.foreground;
		if (s.textcolor != null)    textcolor = s.textcolor;
		if (s.border != null)       border     = s.border;		
		if (s.shape != null)        shape      = s.shape;
	}

	public void apply(AttributesReader areader) {
		if (!(areader instanceof NodeAttributesReader)) {
			return;
		}
		NodeAttributesReader nreader = (NodeAttributesReader)areader;
		
		// FIXME: reimplement CSS styling using styles

		areader.damage();
	}
	
	@Override
	public void setProperty(String property, String value, int i) throws CSSSyntaxException {
		if (property.equals(CSS_BACKGROUND)) {
			try {
				background = ColorPalette.getColorFromCode(value.toUpperCase());
			} catch (NumberFormatException e) {
				throw new CSSSyntaxException("Malformed color code at line "+i+" found "+value+". Must be from 000000 to FFFFFF");
			}
		} else if (property.equals(CSS_FOREGROUND)) {
			try {
				foreground = ColorPalette.getColorFromCode(value.toUpperCase());
			} catch (NumberFormatException e) {
				throw new CSSSyntaxException("Malformed color code at line "+i+" found "+value+". Must be from 000000 to FFFFFF");
			}
		} else if (property.equals(CSS_TEXTCOLOR)) {
			try {
				textcolor = ColorPalette.getColorFromCode(value.toUpperCase());
			} catch (NumberFormatException e) {
				throw new CSSSyntaxException("Malformed color code at line "+i+" found "+value+". Must be from 000000 to FFFFFF");
			}
		} else if (property.equals(CSS_SHAPE)) {
			if 		(value.equals(CSS_SHAPE_ELLIPSE)) 	shape = NodeShape.ELLIPSE;
			else if (value.equals(CSS_SHAPE_RECTANGLE)) shape = NodeShape.RECTANGLE;
			else throw new CSSSyntaxException("Unknown vertex shape at line "+i+" found "+value+". Must be "+CSS_SHAPE_ELLIPSE+" or "+CSS_SHAPE_RECTANGLE);
		} else if (property.equals(CSS_BORDER)) {
			if 		(value.equals(CSS_BORDER_SIMPLE)) 	border = NodeBorder.SIMPLE;
			else if (value.equals(CSS_BORDER_RAISED)) 	border = NodeBorder.RAISED;
			else if (value.equals(CSS_BORDER_STRONG)) 	border = NodeBorder.STRONG;
			else throw new CSSSyntaxException("Unknown vertex border at line "+i+" found "+value+". Must be "+CSS_BORDER_SIMPLE+", "+CSS_BORDER_RAISED+" or "+CSS_BORDER_STRONG);
		} else {
			throw new CSSSyntaxException("Node has no key "+property+" at line "+i+" found "+value+". Must be "+CSS_BACKGROUND+", "+CSS_FOREGROUND+", "+CSS_SHAPE+" or "+CSS_BORDER);
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
	 * @param tabs_count the number of tabulations to append at the begining of each line
	 */
	public String toString(int tabs_count) {
		String s = "", tabs = "\t";
		for (int i = 1; i < tabs_count; i++) {
			tabs += "\t";
		}
		if (background != null) s += tabs+CSS_BACKGROUND+": "+ColorPalette.getColorCode(background)+";\n"; 
		if (foreground != null) s += tabs+CSS_FOREGROUND+": "+ColorPalette.getColorCode(foreground)+";\n";
		if (textcolor != null) s += tabs+CSS_TEXTCOLOR+": "+ColorPalette.getColorCode(textcolor)+";\n";
		if (border != null) {
			s += tabs+CSS_BORDER+": ";
			switch (border) {
			case SIMPLE:
				s += CSS_BORDER_SIMPLE;
				break;
			case RAISED:
				s += CSS_BORDER_RAISED;
				break;
			case STRONG:
				s += CSS_BORDER_STRONG;
				break;
			}
			s += ";\n";
		}
		if (shape != null) s += tabs+CSS_SHAPE+": "+(shape == NodeShape.ELLIPSE?CSS_SHAPE_ELLIPSE:CSS_SHAPE_RECTANGLE)+";\n";
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
		Color background  = null;
		Color foreground  = null;
		Color textColor  = null;
		NodeShape shape   = null;
		NodeBorder border = null;
		
		if (parserPattern == null) parserPattern = Pattern.compile("([a-zA-Z0-9\\-_]+):\\s*#?([a-zA-Z0-9\\-_]+);");
		
		for (int i = 0; i < lines.length; i++) {
			Matcher m = parserPattern.matcher(lines[i].trim());
			String key = m.group(1).trim(), value = m.group(2).trim();
			
			if (m.groupCount() < 2) throw new CSSSyntaxException("Malformed line "+i+" : "+lines[i]+". Must be 'key: value;'");
			if (key.equals(CSS_BACKGROUND)) {
				try {
					background = ColorPalette.getColorFromCode(value);
				} catch (NumberFormatException e) {
					throw new CSSSyntaxException("Malformed color code at line "+i+" : "+lines[i]+". Must be from 000000 to FFFFFF");
				}
			} else if (key.equals(CSS_FOREGROUND)) {
				try {
					foreground = ColorPalette.getColorFromCode(value);
				} catch (NumberFormatException e) {
					throw new CSSSyntaxException("Malformed color code at line "+i+" : "+lines[i]+". Must be from 000000 to FFFFFF");
				}
			} else if (key.equals(CSS_TEXTCOLOR)) {
				try {
					textColor = ColorPalette.getColorFromCode(value);
				} catch (NumberFormatException e) {
					throw new CSSSyntaxException("Malformed color code at line "+i+" : "+lines[i]+". Must be from 000000 to FFFFFF");
				}
			} else if (key.equals(CSS_SHAPE)) {
				if 		(value.equals(CSS_SHAPE_ELLIPSE)) 	shape = NodeShape.ELLIPSE;
				else if (value.equals(CSS_SHAPE_RECTANGLE)) shape = NodeShape.RECTANGLE;
				else throw new CSSSyntaxException("Unknown vertex shape at line "+i+" : "+lines[i]+". Must be "+CSS_SHAPE_ELLIPSE+" or "+CSS_SHAPE_RECTANGLE);
			} else if (key.equals(CSS_BORDER)) {
				if 		(value.equals(CSS_BORDER_SIMPLE)) 	border = NodeBorder.SIMPLE;
				else if (value.equals(CSS_BORDER_RAISED)) 	border = NodeBorder.RAISED;
				else if (value.equals(CSS_BORDER_STRONG)) 	border = NodeBorder.STRONG;
				else throw new CSSSyntaxException("Unknown vertex border at line "+i+" : "+lines[i]+". Must be "+CSS_BORDER_SIMPLE+", "+CSS_BORDER_RAISED+" or "+CSS_BORDER_STRONG);
			} else {
				throw new CSSSyntaxException("Node has no key "+key+" at line "+i+" : "+lines[i]+". Must be "+CSS_BACKGROUND+", "+CSS_FOREGROUND+", "+CSS_SHAPE+" or "+CSS_BORDER);
			}
		}
		return new CSSNodeStyle(background, foreground, textColor, border, shape);
	}
	public Object clone() {
		return new CSSNodeStyle(this);
	}
}
