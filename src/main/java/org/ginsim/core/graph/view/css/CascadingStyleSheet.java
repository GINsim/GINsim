package org.ginsim.core.graph.view.css;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ginsim.common.application.LogManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;

/**
 * This class represent the CSS object itself
 * 
 * @author Duncan Berenguier
 *
 */
public class CascadingStyleSheet {
	
	protected List<StyleSheet> cascade;
	protected Map<String, Selector> affectedSelectors;

	/**
	 * Create and initialize the collections of the CSS
	 */
	public CascadingStyleSheet() {
		this.cascade = new ArrayList<StyleSheet>();
		this.affectedSelectors = new HashMap<String, Selector>();
	}

	/**
	 * Apply this css on a graph using a CascadingStyleSheetManager
	 * Note this css needs to be compiled before
	 * @param graph
	 * @param cs
	 */
	public void apply(Graph graph, CascadingStyleSheetManager cs) {
		apply(graph.getNodes(), graph.getNodeAttributeReader(), graph.getEdges(), graph.getEdgeAttributeReader(), cs);		
	}
	protected void apply(Collection nodes, NodeAttributesReader vreader, Collection edges, EdgeAttributesReader ereader, CascadingStyleSheetManager cs) {
		System.out.println("CascadingStyleSheet.apply()"+affectedSelectors+" "+affectedSelectors.size());
		for (Selector sel : affectedSelectors.values()) {
			System.out.println("CascadingStyleSheet.apply()"+sel);
			cs.applySelectorOnNodes(sel, nodes, vreader);
			cs.applySelectorOnEdges(sel, edges, ereader);
		}
		
	}
	
	/**
	 * Compile the CSS, allowing it to be applied.
	 */
	public void compile() {
		System.out.println("CascadingStyleSheet.compile()"+ cascade+" "+cascade.size());
		for (StyleSheet styleSheet : cascade) {
			System.out.println("CascadingStyleSheet.compile()"+styleSheet);
			Selector old_sel = affectedSelectors.get(styleSheet.selID);
			if (old_sel == null) {
				Selector sel = Selector.getNewSelector(styleSheet.selID);
				affectedSelectors.put(styleSheet.selID, sel);
				sel.addCategory(styleSheet.category, styleSheet.style);
			} else {
				CSSStyle style = old_sel.getStyle(styleSheet.category);
				if (style == null) {
					old_sel.addCategory(styleSheet.category, styleSheet.style);
				} else {
					style.merge(styleSheet.style);
				}
				
			}
		}
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (StyleSheet styleSheet : cascade) {
			s.append(styleSheet.toString());
		}
		return s.toString();
	}

	/**
	 * Return a new CSS object from a text.
	 * @param text
	 * @return a new CSS object from a text
	 * @throws CSSSyntaxException
	 */
	public static CascadingStyleSheet newFromText(String text) throws CSSSyntaxException {
		CascadingStyleSheet css = new CascadingStyleSheet();
		CSSParser.parse(css, text);
		return css;
	}
	
	/**
	 * Return a new CSS object from a text.
	 * @param text
	 * @throws CSSSyntaxException
	 */
	public void parse(String text) throws CSSSyntaxException {
		this.cascade = new ArrayList<StyleSheet>();
		this.affectedSelectors = new HashMap<String, Selector>();
		CSSParser.parse(this, text);
	}

	/**
	 * Return a new CSS object from a Reader.
	 * @return a new CSS object from a text
	 * @throws CSSSyntaxException
	 */
	public String readFile(Reader reader) throws CSSSyntaxException {
		StringBuffer contents = new StringBuffer();
        try {
        	BufferedReader buffreader = new BufferedReader(reader);
            String line = null;

            // repeat until all lines is read
            while ((line = buffreader.readLine()) != null) {
                contents.append(line)
                        .append(System.getProperty(
                                "line.separator"));
            }
        } catch (FileNotFoundException e) {
        	LogManager.error("Unable to open the file, file not found : "+e.getMessage());
        	return null;
        } catch (IOException e) {
        	LogManager.error("Unable to open the file, IOException while reading the file : "+e.getMessage());
        	return null;
        }
		return contents.toString();
	}

	protected void addStyleSheet(StyleSheet styleSheet) {
		this.cascade.add(styleSheet);
	}

}

class StyleSheet {
	protected String selID;
	protected String category;
	protected CSSStyle style;
	
	public StyleSheet(String sel, String category, CSSStyle style) {
		this.selID = sel;
		this.category = category;
		this.style = style;
	}

	
	public StyleSheet() {
	}
	


	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(selID);
		s.append('.');
		s.append(category);
		s.append(" {\n");
		if (style != null) s.append(style);
		s.append("}\n");
		return s.toString();
	}
}

class CSSParser {
	/**
	 * Parse a css
	 * @param css the css text
	 * @param text  corresponding text
	 * @throws CSSParseException
	 */
	protected static void parse(CascadingStyleSheet css, String text) throws CSSSyntaxException {
		int i = 0;
		
		while (i < text.length()) {
			char c = text.charAt(i);
			if (Character.isLetter(c)) {
				StyleSheet styleSheet = new StyleSheet();
				i = parseSelectorID(text, i, styleSheet);
				css.addStyleSheet(styleSheet);
			} else if (Character.isWhitespace(c)){
				i++;
			} else {
				throw new CSSSyntaxException("Unexpected character before the begining of a selector", text, i);
			}
		}
	}

	private static int parseSelectorID(String text, int i, StyleSheet styleSheet)  throws CSSSyntaxException {
		StringBuffer selID = new StringBuffer();
		
		while (i < text.length()) {
			char c = text.charAt(i++);
			if (c == '.') {
				styleSheet.selID = selID.toString();
				if (styleSheet.selID == null) {
					throw new CSSSyntaxException("Error while parsing a selector identifier, the selector "+selID.toString()+" cannot be loaded");
				}
				return parseCategory(text, i, styleSheet);
			} else if (Character.isLetter(c) || Character.isDigit(c)) {
				selID.append(c);
			} else {
				throw new CSSSyntaxException("Error while parsing a selector identifier at position ", text, i);
			}
		}
		throw new CSSSyntaxException("Unexpected end of file while parsing a selector identifier at position ", text, i);
	}

	private static int parseCategory(String text, int i, StyleSheet styleSheet) throws CSSSyntaxException {
		StringBuffer catID = new StringBuffer();
		
		while (i < text.length()) {
			char c = text.charAt(i++);
			if (c == '{') {
				styleSheet.category = catID.toString();
				return parseStyle(text, i, styleSheet);
			} else if (Character.isWhitespace(c)) {	
				//pass
			} else if (Character.isLetterOrDigit(c)) {
				catID.append(c);
			} else {
				throw new CSSSyntaxException("Error while parsing a category at position ", text, i);
			}
		}
		throw new CSSSyntaxException("Unexpected end of file while parsing a category at position ", text, i);
	}

	private static int parseStyle(String text, int i, StyleSheet styleSheet) throws CSSSyntaxException {
		StringBuffer property = new StringBuffer();
		StringBuffer value = new StringBuffer();
		
		CSSStyle style = null ;
		
		
		while (i < text.length()) {
			char c = text.charAt(i++);
			if (c == '}') {
				styleSheet.style = style;
				return i;
			} else if (Character.isWhitespace(c)) {	
				//pass
			} else if (Character.isLetterOrDigit(c)) {
				i = parsePropertyAndValue(text, i-1, property, value);
				if (style == null) {
					String p = property.toString();
					if (p.equals(CSSEdgeStyle.CSS_LINECOLOR) 
						|| p.equals(CSSEdgeStyle.CSS_LINEEND) 
						|| p.equals(CSSEdgeStyle.CSS_BORDER) 
						|| p.equals(CSSEdgeStyle.CSS_SHAPE)) {
						style = new CSSEdgeStyle();
					} else {
						style = new CSSNodeStyle();
					}
				}
				style.setProperty(property.toString(), value.toString(), i);
				property.delete(0, property.length());
				value.delete(0, value.length());
			} else {
				throw new CSSSyntaxException("Error while parsing a style at position ", text, i);
			}
		}
		throw new CSSSyntaxException("Unexpected end of file while parsing a style at position ", text, i);
	}

	private static int parsePropertyAndValue(String text, int i, StringBuffer property, StringBuffer value) throws CSSSyntaxException {
		return parseValue(text, parseProperty(text, i, property), value);
	}

	private static int parseProperty(String text, int i, StringBuffer property) throws CSSSyntaxException {
		while (i < text.length()) {
			char c = text.charAt(i++);
			if (c == ':') {
				return i;
			} else if (Character.isWhitespace(c)) {	
				//pass
			} else if (Character.isLetterOrDigit(c) || c == '-') {
				property.append(c);
			} else {
				throw new CSSSyntaxException("Error while parsing a property at position ", text, i);
			}
		}
		throw new CSSSyntaxException("Unexpected end of file while parsing a property at position ", text, i);
	}
	
	private static int parseValue(String text, int i, StringBuffer value) throws CSSSyntaxException {
		while (i < text.length()) {
			char c = text.charAt(i++);
			if (c == ';') {
				return i;
			} else if (Character.isWhitespace(c)) {	
				//pass
			} else if (Character.isLetterOrDigit(c) || c == '#') {
				value.append(c);
			} else {
				throw new CSSSyntaxException("Error while parsing a value at position ", text, i);
			}
		}
		throw new CSSSyntaxException("Unexpected end of file while parsing a value at position ", text, i);
	}
	
}