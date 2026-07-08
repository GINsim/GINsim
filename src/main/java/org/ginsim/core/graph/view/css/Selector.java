package org.ginsim.core.graph.view.css;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.view.AttributesReader;


/**
 * A Selector implementation for GINsim. It intend to be similar to selectors in HTML's CSS.
 * 
 * @author Duncan Berenguier
 */
public abstract class Selector {

	static protected Map selectors = new HashMap();
	protected String identifier;
	private Map m;
	
	protected Selector(String identifier) {
		this.identifier = identifier;
		m = new HashMap();
		resetDefaultStyle();
	}
	
	/**
	 * Register your selector to enable parsing it.
	 * @param identifier the identifier of the selector
	 * @param cla the selector's class
	 */
	public static void registerSelector(String identifier, Class cla) {
		selectors.put(identifier, cla);
	}

	/**
	 * return a new selector corresponding to an identifier
	 * @param identifier  the identifier
	 * @return a new selector with identifier id or null if the class doesn't exists or can't be instantiated.
	 */
	public static Selector getNewSelector(String identifier) {
		try {
			return (Selector)((Class)selectors.get(identifier)).newInstance();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Return the identifier for this selector
	 * @return identifier
	 */
	public final String getIdentifer() {
		return identifier;
	}

	/**
	 * A collection of all the categories this selector respond to.
	 * @return the categories id
	 */
	public final Collection categories() {
		return m.values();
	}

	/**
	 * Get the style for a category id or null if the category doesn't exist. 
	 * @param category the id of the category
	 * @return the default style
	 */
	public CSSStyle getStyle(String category) {
		if (!m.containsKey(category)) {
			return missingCategory(category);
		}
		return (CSSStyle) m.get(category);
	}
	
	/**
	 * Get the style for an object using getCategory to determine its category  or null if the category doesn't exist or has no category
	 * 
	 * If possible call getStyleForNode or getStyleForEdge, when you know the type of object
	 * 
	 * @param obj the Ojject node
	 * @return the default style for this object
	 */	
	public CSSStyle getStyle(Object obj) {
		return getStyle(getCategory(obj));
	}
	
	/**
	 * Get the style for a node using getCategory to determine its category  or null if the category doesn't exist or has no category
	 * @param obj a node
	 * @return the default style for this object
	 */	
	public final CSSStyle getStyleForNode(Object obj) {
		return getStyle(getCategoryForNode(obj));
	}
	
	/**
	 * Get the style for an edge using getCategory to determine its category  or null if the category doesn't exist or has no category
	 * @param obj an edge
	 * @return the default style for this object
	 */	
	public final CSSStyle getStyleForEdge(Object obj) {
		return getStyle(getCategoryForEdge(obj));
	}
	
	/**
	 * Set the style for a category id. 
	 * @param category the id of the category
	 * @param style  the CSSStyle
	 * @return false if the category doesn't exists
	 */
	public final boolean setStyle(String category, CSSStyle style) {
		if (!m.containsKey(category)) {
            return missingCategory(category, style);
        }
		m.put(category, style);
		return true;
	}
	
	/**
	 * Apply the style to an element using an attributesReder for a category id or null if the category doesn't exist.
	 * @param category the id of the category
	 * @param areader   the  AttributesReader reader
	 * @return false if the category doesn't exist
	 */
	private final boolean applyStyle(String category, AttributesReader areader) {
		CSSStyle s = (CSSStyle) m.get(category);
		if (s != null) {
            s.apply(areader);
            areader.refresh();
        } else {
            return missingCategory(category, areader);
        }
		return true;
	}

	/**
	 * Apply the style to a node using an attributesReder for a category id or null if the category doesn't exist.
	 * @param obj a node
	 * @param areader  a AttributesReader    reader
	 * @return false if the category doesn't exist
	 */	
	public final boolean applyStyleForNode(Object obj, AttributesReader areader) {
		return applyStyle(getCategoryForNode(obj), areader);
	}
	
	/**
	 * Apply the style to an edge using an attributesReder for a category id or null if the category doesn't exist.
	 * @param obj an edge
	 * @param areader a  AttributesReader reader
	 * @return false if the category doesn't exist
	 */	
	public final boolean applyStyleForEdge(Object obj, AttributesReader areader) {
		return applyStyle(getCategoryForEdge(obj), areader);
	}
	
	/**
	 * Declare a new category responding to this selector
	 * @param category the identifier for this category
	 * @param defaultStyle its default style
	 */
	protected void addCategory(String category, CSSStyle defaultStyle) {
		m.put(category, defaultStyle);
	}
	
	/**
	 * Reset the categories to their default values.
	 */
	public abstract void resetDefaultStyle();

	/**
	 * Called when the category doesn't exist in the map.
	 * @param category  category style
	 * @return null
	 */
	protected CSSStyle missingCategory(String category) {
		return null;
	}

	/**
	 * Called when the category doesn't exist in the map.
	 * @param category the category string
	 * @param areader the areader to apply the corresponding style on.
	 * @return false
	 */
	protected boolean missingCategory(String category, AttributesReader areader) {
		return false;
	}

	/**
	 * Called when the category doesn't exist in the map.
	 * @param category the category style
	 * @param style the style to save for this category
	 * @return false
	 */
	protected boolean missingCategory(String category, CSSStyle style) {
		return false;
	}
	
	/**
	 * Indicates if the Selector contains nodes styles. True by default, you should override this function if the selector doesn't respond to Nodes.
	 * @return true
	 */
	public boolean respondToNodes() {
		return true;
	}
	
	/**
	 * Indicates if the Selector contains edges styles. True by default, you should override this function if the selector doesn't respond to Nodes.
	 * @return true
	 */
	public boolean respondToEdges() {
		return true;
	}
	
	/**
	 * Return the category corresponding to the object obj or null if it doesn't respond to any.
	 * 
	 * If possible call getCategoryForNode or getCategoryForEdge, when you know the type of object
	 * 
	 * @param obj a edge object
	 * @return the associated category or null
	 */
	public final String getCategory(Object obj) {
		if (obj instanceof Edge) {
            return getCategoryForEdge(obj);
        }
		return getCategoryForNode(obj);
	}

	/**
	 * Return the category corresponding to the object obj or null if it doesn't respond to any.
	 * 
	 * When subclassing Selector you should use a cache system if you can't get the category for
	 * an object in a constant time. And therefore also extend the function flush() to empty this
	 * cache (set to to null).
	 * @param obj a object
     * @return the associated category or null
	 */
	abstract protected String getCategoryForNode(Object obj);
	/**
	 * Return the category corresponding to the object obj or null if it doesn't respond to any.
	 * 
	 * When subclassing Selector you should use a cache system if you can't get the category for
	 * an object in a constant time. And therefore also extend the function flush() to empty this
	 * cache (set to to null).
	 * @param obj the object
     * @return the associated category or null
	 */
	abstract protected String getCategoryForEdge(Object obj);
	
	/**
	 * By default this does nothing but subclasses of Selector could use this functio
	 * n to free the cache of category &lt;=&gt; object.
	 * Should only be called if you are sure you doesn't need to use getCategory on this selector anymore.
	 */
	public void flush() {};
	
	public final String toString() {
		return identifier;
	}
	
	public final String toString(String category) {
		return identifier+"."+category;
	}
	
	public final String toCSS() {
		StringBuffer s = new StringBuffer();
		for (Object category : m.keySet()) {
			s.append(identifier);
			s.append('.');
			s.append(category);
			s.append("{\n");
			s.append(m.get(category));
			s.append("\n}");
		}
		return s.toString();
	}
}
