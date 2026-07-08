package org.ginsim.core.graph.view.style;

import java.awt.Color;

import org.ginsim.common.utils.ColorPalette;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.view.EdgeEnd;
import org.ginsim.core.graph.view.EdgePattern;

/**
 * Simple implementation for EdgeStyle.
 * It can be used to override a parent style (defining some properties)
 * or as default style (defining all properties).
 * 
 * @author Aurelien Naldi
 *
 * @param <V>  the vertex
 * @param <E> the edge
 */
public class EdgeStyleImpl<V, E extends Edge<V>> extends BaseStyle<EdgeStyle<V, E>> implements EdgeStyle<V, E> {

	private static int DEFAULT_WIDTH = 1;
	private static Color DEFAULT_COLOR = Color.BLACK;
	private static EdgeEnd DEFAULT_ENDING = EdgeEnd.POSITIVE;
	private static EdgePattern DEFAULT_PATTERN = EdgePattern.SIMPLE;
	
	protected static final StyleProperty[] DEFAULT_PROPERTIES = {
		StyleProperty.COLOR,
		StyleProperty.PATTERN,
		StyleProperty.LINEWIDTH,
	};
	
	private Color color = null;
	private EdgePattern pattern = null;
	private EdgeEnd ending = null;
	private int width = -1;
	
	public EdgeStyleImpl() {
		this(null, null);
	}
	
	public EdgeStyleImpl(String name, EdgeStyle<V, E> parent) {
		super(parent, name);
		
		if (parent == null) {
			color = DEFAULT_COLOR;
			pattern = DEFAULT_PATTERN;
			ending = DEFAULT_ENDING;
			width = DEFAULT_WIDTH;
		}
	}
	
	@Override
	public Color getColor(E edge) {
		if (color == null) {
			if (parent == null) {
				return DEFAULT_COLOR;
			}
			return parent.getColor(edge);
		}
		
		if (parent != null && parent.enforceColor()) {
			return parent.getColor(edge);
		}
		return color;
	}

	@Override
	public int getWidth(E edge) {
		if (width <0) {
			if (parent == null) {
				return DEFAULT_WIDTH;
			}
			return parent.getWidth(edge);
		}
		
		if (parent != null && parent.enforceWidth()) {
			return parent.getWidth(edge);
		}
		return width;
	}
	@Override
	public EdgePattern getPattern(E edge) {
		if (pattern == null) {
			if (parent == null) {
				return DEFAULT_PATTERN;
			}
			return parent.getPattern(edge);
		}
		
		if (parent != null && parent.enforcePattern()) {
			return parent.getPattern(edge);
		}
		return pattern;
	}

	@Override
	public EdgeEnd getEnding(E edge) {
		if (ending == null) {
			if (parent == null) {
				return DEFAULT_ENDING;
			}
			
			if (parent != null && parent.enforceEnding()) {
				return parent.getEnding(edge);
			}
			return parent.getEnding(edge);
		}
		return ending;
	}

	@Override
	public boolean enforceColor() {
		if (parent != null) {
			return parent.enforceColor();
		}
		return false;
	}

	@Override
	public boolean enforceEnding() {
		if (parent != null) {
			return parent.enforceEnding();
		}
		return false;
	}

	@Override
	public boolean enforcePattern() {
		if (parent != null) {
			return parent.enforcePattern();
		}
		return false;
	}

	@Override
	public boolean enforceWidth() {
		if (parent != null) {
			return parent.enforceWidth();
		}
		return false;
	}

	@Override
	public StyleProperty[] getProperties() {
		return DEFAULT_PROPERTIES;
	}

	public String toString() {
		if (name == null) {
			return "Default edge style";
		}
		return name;
	}
	
	@Override
	public Object getProperty(StyleProperty prop) {
		if (prop == StyleProperty.COLOR) {
			return color;
		}
		if (prop == StyleProperty.ENDING) {
			return ending;
		}
		
		if (prop == StyleProperty.PATTERN) {
			return pattern;
		}
		
		if (prop == StyleProperty.LINEWIDTH) {
			if (width < 1) {
				return null;
			}
			return width;
		}
		
		return getCustomProperty(prop);
	}
	
	@Override
	public void setProperty(StyleProperty prop, Object value) {
		if (prop == StyleProperty.COLOR) {
			this.color = (Color)value;
		} else if (prop == StyleProperty.ENDING) {
			this.ending = (EdgeEnd)value;
		} else if (prop == StyleProperty.PATTERN) {
			this.pattern = (EdgePattern)value;
		} else if (prop == StyleProperty.LINEWIDTH) {
			if (value == null) {
				this.width = -1;
			} else {
				this.width = (Integer)value;
			}
		} else {
			setCustomProperty(prop, value);
		}
	}
	
	protected Object getCustomProperty(StyleProperty prop) {
		return null;
	}
	protected void setCustomProperty(StyleProperty prop, Object value) {
	}

	@Override
	public boolean matches(Color color, EdgePattern pattern, int width) {
		return	NodeStyleImpl.equals(color, this.color) &&
				pattern == this.pattern &&
				width == this.width;
	}

    @Override
    public String getCSS() {
        StringBuffer sb = new StringBuffer();
        String s_class = ".edge"+getCSSNameSuffix();

        sb.append(s_class+" {\n");
        if (color != null) {
            sb.append("stroke: "+ ColorPalette.getColorCode(color)+";\n");
            sb.append("fill: none;\n");
        }

        if (width > -1) {
            sb.append("stroke-width: "+width+";\n");
        }

        if (pattern != null) {
            float[] dashPattern = pattern.getPattern();
            if (dashPattern != null && dashPattern.length > 0) {
                sb.append("stroke-dasharray:"+dashPattern[0]);
                    for (int i=1 ; i<dashPattern.length ; i++) {
                        sb.append(","+dashPattern[i]);
                    }
                sb.append(";\n");
            }
        }
        sb.append("}\n");

        return sb.toString();
    }

    @Override
    public String getCSSClass(E edge) {
        if (parent == null) {
            return "edge";
        }

        return parent.getCSSClass(edge) + " edge" +getCSSNameSuffix();
    }
}
