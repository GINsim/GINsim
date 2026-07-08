package org.ginsim.core.graph.view.style;

/**
 * public class BaseStyle
 * @param <S> style
 */
abstract public class BaseStyle<S extends Style> implements Style {

	/**
	 * String name
	 */
	protected String name;
	/**
	 * final S parent
	 */
	protected final S parent;

	/**
	 * Constructor
	 * @param parent style parent
	 * @param name name string style
	 */
	public BaseStyle(S parent, String name) {
		this.parent = parent;
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Object getParentProperty(StyleProperty property) {
		Object value = getProperty(property);
		if (value == null && parent != null) {
			return parent.getParentProperty(property);
		}
		return value;
	}

	/**
	 * Getter of SNameSuffix
	 * @return string of SNameSuffix
	 */
	protected String getCSSNameSuffix() {
        if (name == null) {
            return "";
        }
        return "_"+name.trim().toLowerCase().replaceAll(" ", "_");
    }
    
    @Override
    public Style getParent() {
    	return parent;
    }
    
    @Override
    public void copy(Style source) {
    	setName(source.getName());
    	for (StyleProperty prop: getProperties()) {
    		setProperty(prop, source.getProperty(prop));
    	}
    }

	/**
	 * Test if equal
	 * @param other object
	 * @return boolean if equal
	 */
	public boolean equals(Object other) {
    	if (other == null || other.getClass() != getClass()) {
    		return false;
    	}
    	
		Style style = (Style)other;
		if (style.getProperties() != getProperties()) {
			return false;
		}
		if (style.getName() != getName()) {
			return false;
		}
		
		return true;
    }
}
