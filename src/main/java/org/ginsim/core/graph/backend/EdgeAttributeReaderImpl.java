package org.ginsim.core.graph.backend;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.GraphBackend;
import org.ginsim.core.graph.view.Bezier;
import org.ginsim.core.graph.view.EdgeAnchor;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.EdgeEnd;
import org.ginsim.core.graph.view.EdgePattern;
import org.ginsim.core.graph.view.EdgeViewInfo;
import org.ginsim.core.graph.view.MovingEdgeType;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.graph.view.SimpleStroke;
import org.ginsim.core.graph.view.ViewHelper;
import org.ginsim.core.graph.view.style.EdgeStyle;
import org.ginsim.core.graph.view.style.StyleManager;


/**
 * a generic edgeAttributeReader storing data into a dedicated hashmap
 * @param <V> vertex
 * @param <E>  edge
 */
public class EdgeAttributeReaderImpl<V, E extends Edge<V>> implements EdgeAttributesReader<V,E> {

	private static final int MAX_EDGE_SIZE = 12;
	public static final String EDGE_COLOR = "vs.edgecolor";
    protected static Map<String, float[]> m_pattern = null;
	
    
	private final GraphBackend<V, E> graph;
	private final StyleManager<V, E> styleManager;
    private final EdgeStyle<V,E> defaultStyle;
    private final NodeAttributesReader nreader;
    
    
    private E edge;
    private EdgeViewInfo<V, E> viewInfo;
    private EdgeStyle<V, E> style;

    private boolean hasReverseEdge = false;
    private boolean selected = false;
    private boolean hasChanged = false;
    
    private Rectangle cachedBounds = null;
    private Shape cachedPath = null;
    private List<Point> cachedPoints = null;
    private SimpleStroke stroke = new SimpleStroke();
    
    /**
     * @param styleManager
     * @param backend
     * @param nreader
     */
    public EdgeAttributeReaderImpl(StyleManager<V, E> styleManager, GraphBackend<V, E> backend, NodeAttributesReader nreader) {
    	this.graph = backend;
        this.nreader = nreader;
        this.styleManager = styleManager;
        this.defaultStyle = styleManager.getDefaultEdgeStyle();
    }
    
    @Override
    public Rectangle getBounds() {
        if (edge == null) {
            return null;
        }
    	
        if (cachedBounds == null) {
        	Shape path = getPath();
        	Rectangle b = path.getBounds();
        	int arrowmargin = 5;
        	float w = getLineWidth();
        	if (w > 2) {
        		arrowmargin = (int) ((1 + arrowmargin*w) / 2);
        	}
        	cachedBounds = new Rectangle(b.x-arrowmargin, b.y-arrowmargin, b.width+2*arrowmargin, b.height+2*arrowmargin);
        }
        return cachedBounds;
    }
    
    @Override
    public void setEdge(E obj) {
    	setEdge(obj, false);
    }
    @Override
    public void setEdge(E obj, boolean selected) {
    	this.selected = selected;
    	if (obj == this.edge) {
    		return;
    	}
    	this.edge = obj;
    	cachedBounds = null;
    	cachedPath = null;

        if (obj == null) {
            viewInfo = null;
            style = null;
            hasReverseEdge = false;
        } else {
            V src = edge.getSource();
            V tgt = edge.getTarget();
            if (src == tgt) {
                hasReverseEdge = false;
            } else {
                hasReverseEdge = (graph.getEdge(tgt, src) != null);
            }
    	    viewInfo = graph.getEdgeViewInfo(edge);
		    style = styleManager.getViewEdgeStyle(edge);
        }
    	hasChanged = false;
    }

    @Override
    public float getLineWidth() {
    	if (style == null) {
    		return defaultStyle.getWidth(edge);
    	}
		return style.getWidth(edge);
    }

    @Override
    public Color getLineColor() {
    	if (style == null) {
    		return defaultStyle.getColor(edge);
    	}
		return style.getColor(edge);
    }

    @Override
    public void refresh() {
    	if (edge != null && hasChanged) {
    		graph.damage(edge);
    	}
    }
    @Override
    public void damage() {
    	if (edge != null) {
    		graph.damage(edge);
    	}
    }

    @Override
    public EdgeEnd getLineEnd() {
    	if (style == null) {
    		return defaultStyle.getEnding(edge);
    	}
		return style.getEnding(edge);
    }

    @Override
    public List<Point> getPoints() {
        if ( viewInfo == null ) {
            return null;
        }
        return viewInfo.getPoints();
    }

    @Override
    public void setPoints(List<Point> l) {
        if (edge == null) {
            return;
        }
        damage();
        if (viewInfo == null) {
        	viewInfo = graph.ensureEdgeViewInfo(edge);
        }
        viewInfo.setPoints(l);
        hasChanged = true;
        cachedBounds = null;
        cachedPath = null;
        refresh();
    }

    @Override
    public boolean hasReverseEdge() {
        return hasReverseEdge;
    }

    @Override
	public EdgePattern getDash() {
    	if (style == null) {
    		return defaultStyle.getPattern(edge);
    	}
		return style.getPattern(edge);
	}

	@Override
	public void copyFrom(EdgeAttributesReader fereader) {
		setCurve(fereader.isCurve());
		setStyle(fereader.getStyle());
		
		List<Point> oldPoints = fereader.getPoints();
		if (oldPoints != null) {
			List<Point> points = new ArrayList<Point>(oldPoints.size());
			for (Point point : oldPoints) {
				points.add((Point) point.clone());
			}
			setPoints(points);			
		} else {
			setPoints(null);
		}
 }

	@Override
	public EdgeAnchor getAnchor() {
		if (viewInfo != null) {
			EdgeAnchor anchor = viewInfo.getAnchor();
			if (anchor != null) {
				return anchor;
			}
		}

		return EdgeAnchor.NE;
	}

	@Override
	public void setAnchor(EdgeAnchor anchor) {
        if (viewInfo == null) {
        	if (anchor == null) {
        		return;
        	}
        	viewInfo = graph.ensureEdgeViewInfo(edge);
        }
		viewInfo.setAnchor(anchor);
	}

	@Override
	public boolean isCurve() {
		if (viewInfo != null) {
			return viewInfo.isCurve();
		}

		return false;
	}

	@Override
	public void setCurve(boolean curve) {
		if (edge == null) {
			return;
		}
        if (viewInfo == null) {
        	viewInfo = graph.ensureEdgeViewInfo(edge);
        }
        viewInfo.setCurve(curve);
	}

	@Override
	public void render(Graphics2D g) {
		dorender(g, null);
	}
	
	@Override
	public void renderMoving(Graphics2D g, MovingEdgeType type, int movex, int movey) {
		List<Point> points = ViewHelper.getMovingPoints(type, movex, movey, nreader, this, edge);
		dorender(g, points);
	}
	
	@Override
	public void renderMovingPoint(Graphics2D g, int idx, int movex, int movey) {
		List<Point> points = new ArrayList<Point>(getPoints());
		Point pt = points.get(idx);
		points.set(idx, new Point(pt.x+movex, pt.y+movey));
		dorender(g, ViewHelper.getModifiedPoints(nreader, this, edge, points));
	}



	private void dorender(Graphics2D g, List<Point> points) {
		float width = getLineWidth();
		stroke.setWidth(width);
		stroke.setDashPattern(getDash());
		g.setStroke(stroke);
		
		Shape s;
		if (points == null) {
			s = getPath();
			points = cachedPoints;
		} else {
			s = createPath(points, isCurve(), getAnchor());
		}
		if (selected) {
			g.setColor(Color.PINK);
			g.draw(stroke.createStrokedShape(s));
		}
		g.setColor(getLineColor());
		g.fill(stroke.createStrokedShape(s));
		


		// get the angle for the edge end
		int n = points.size()-1;
		Point pt1 = points.get(n);
		Point ptsrc = points.get(n-1);
		double theta = ViewHelper.getRotationAngle(pt1.x-ptsrc.x, pt1.y-ptsrc.y);;

		// draw the arrow end
		g.translate(pt1.x, pt1.y);
		g.rotate(theta);

		Shape lineEnd = getLineEnd().getShape();
		if (width > 2) {
			float scale = width / 2;
			g.scale(scale, scale);
			g.fill(lineEnd);
			scale = 1/scale;
			g.scale(scale, scale);
		} else {
			g.fill(lineEnd);
		}

		g.rotate(-theta);
		g.translate(-pt1.x, -pt1.y);

		if (selected) {
			// draw points
			int maxpoint = points.size()-1;
			for (int i=0 ; i<= maxpoint ; i++) {
				Point pt = points.get(i);
				if (i==0 || i==maxpoint) {
					g.setColor(Color.GRAY);
					g.fillRect(pt.x-2, pt.y-2, 4, 4);
				} else {
					g.setColor(Color.RED);
					g.fillRect(pt.x-3, pt.y-3, 6, 6);
				}
			}
		}
	}
	
	@Override
	public boolean select(Point p) {
		if (edge == null) {
			return false;
		}
		
		if (getBounds().contains(p)) {
			Shape path = stroke.createSimpleStrokedShape(getPath());
			return path.intersects(p.x-2, p.y-2, 5, 5);
		}
		
		return false;
	}

	private Shape createPath(List<Point> points, boolean curve, EdgeAnchor anchor) {

		Path2D path = new Path2D.Float();

		if (curve && points != null && points.size() > 2) {
			Point pt = points.get(0);
			path.moveTo(pt.x, pt.y);

			pt = points.get(1);
			Point2D[] b = new Bezier(points).getPoints();
			path.quadTo((float) b[0].getX(), 	(float) b[0].getY(), (float) pt.getX(), (float) pt.getY());
			int n = points.size();
			for (int i = 2; i < n - 1; i++) {
				Point2D b0 = b[2 * i - 3];
				Point2D b1 = b[2 * i - 2];
				pt = points.get(i);
				path.curveTo(
						(float) b0.getX(), (float) b0	.getY(),
						(float) b1.getX(), (float) b1.getY(),
						(float) pt.getX(), (float) pt.getY());
			}
			pt = points.get(n-1);
			path.quadTo(
					(float) b[b.length - 1].getX(),
					(float) b[b.length - 1].getY(),
					(float) pt.getX(), (float) pt.getY());

		} else {
			// just iterate a path with existing points
			Point pt_prev = null;
			for (Point pt: points) {
				if (pt_prev == null) {
					path.moveTo(pt.x, pt.y);
				} else {
					path.lineTo(pt.x, pt.y);
				}
				pt_prev = pt;
			}
		}
		
		return path;
	}

	
	private Shape getPath() {
		if (cachedPath == null) {
			stroke.setWidth(getLineWidth());
			cachedPoints = ViewHelper.getPoints(nreader, this, edge);
			cachedPath = createPath(cachedPoints, isCurve(), getAnchor());
		}
		return cachedPath;
	}
	
	@Override
	public void move(int dx, int dy) {
		if (viewInfo == null) {
			return;
		}
		List<Point> points = getPoints();
		if (points == null) {
			return;
		}
		for (Point p: points) {
			p.x += dx;
			p.y += dy;
		}
	}

	@Override
	public EdgeStyle<V, E> getDefaultEdgeStyle() {
		return defaultStyle;
	}
	
	@Override
	public void writeGINML(XMLWriter writer) throws IOException {
		if (edge == null) {
			return;
		}
		
		if (style == null && getPoints() == null) {
			return;
		}
		
		// save style information
		writer.openTag("edgevisualsetting");

		if (styleManager.isCompatMode()) {
            // write old attributes for backward compatibility
        	writer.openTag("polyline");
	        
	        writer.addAttr("points", getPointDescr( ViewHelper.getPoints(nreader, this, edge) ));
	        writer.addAttr("line_color", getLineColor());
	        writer.addAttr("line_style", isCurve() ? "curve" : "straight");
	        writer.addAttr("line_width", ""+(int)getLineWidth());
	        writer.addAttr("routage","auto");
	        if (getDash() == EdgePattern.DASH) {
	        	writer.addAttr("pattern","dash");
	        }

	        writer.closeTag();
        } else if (style != null) {
            String points = getPointDescr(getPoints());
            if (points != null) {
                writer.addAttr("points", points);
            }
            if (isCurve()) {
                writer.addAttr("curve", "true");
            }
            EdgeAnchor anchor = getAnchor();
            if (anchor != null) {
                writer.addAttr("anchor", anchor.name());
            }
            writer.addAttr("style", style.getName());
        }

		writer.closeTag();
	}
	
	private String getPointDescr(List<Point> points) {
		if (points == null || points.size() < 1) {
			return null;
		}
		String s_points = "";
		for (Point p: points) {
			s_points += p.x+","+p.y+" ";
		}
		return s_points.trim();
	}

	@Override
	public void setStyle(EdgeStyle style) {
		if (edge == null) {
			return;
		}
		
		if (viewInfo == null && style != null) {
			viewInfo = graph.ensureEdgeViewInfo(edge);
		}
		if (viewInfo == null) {
			return;
		}
		if (style == null) {
			this.style = null;
			viewInfo.setStyle(null);
			return;
		}
		
		List<EdgeStyle<V,E>> styles = styleManager.getEdgeStyles();
		EdgeStyle<V,E> existing = null;
		for (EdgeStyle<V,E> st: styles) {
			if (style.equals(st)) {
				existing = st;
				break;
			}
		}
		if (existing == null) {
			existing = styleManager.addEdgeStyle();
			existing.copy(style);
		}

		viewInfo.setStyle(existing);
	}

	@Override
	public EdgeStyle getStyle() {
		if (edge == null || viewInfo == null) {
			return null;
		}
		
		return viewInfo.getStyle();
	}

}
