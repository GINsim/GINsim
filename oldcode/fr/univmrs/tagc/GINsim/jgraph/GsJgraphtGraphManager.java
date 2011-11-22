package fr.univmrs.tagc.GINsim.jgraph;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.ginsim.graph.Graph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphUndoManager;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;

import fr.univmrs.tagc.GINsim.data.GsDirectedEdge;
import fr.univmrs.tagc.GINsim.graph.GsEdgeAttributesReader;

import fr.univmrs.tagc.GINsim.graph.GsGraphManager;
import fr.univmrs.tagc.GINsim.graph.GsSelectedEdgeWithNodeIterator;
import fr.univmrs.tagc.GINsim.graph.GsNodeAttributesReader;
import fr.univmrs.tagc.GINsim.gui.GsMainFrame;

/**
 * Implementation of a graphManager using jgraph/jgrapht.
 * 
 */
public class GsJgraphtGraphManager<V,E extends GsDirectedEdge<V>> extends GsGraphManager<V,E> {

    private ListenableGraph<V,E>    	g 				= null;
    private JGraphModelAdapter<V,E>		m_jgAdapter    	= null;
    private GsJgraph 					jgraph 			= null;
    private Graph<V,E> 					gsGraph		    = null;
    private GsParallelEdgeRouting	 	pedgerouting	= null;
    private GraphUndoManager	    	undoManager		= null;
    
    private boolean visible = false;
    
    private int vertexCount = 0;
    
    private int curX = 10;
    private int curY = 10;
    
    private static final int minX = 10;
    private static final int maxX = 700;
    private static final int incX = 120;
    private static final int incY = 40;
    
    private AttributeMap defaultNodeAttr;
    private AttributeMap defaultEdgeAttr;

    /**
     * 
     * @param gsGraph
     * @param mainFrame
     */
    public GsJgraphtGraphManager( Graph gsGraph, GsMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.gsGraph = gsGraph;
        
        // use a lighter implementation of a directedGraph!
        g = new ListenableDirectedGraph(new GsJGraphtBaseGraph(new GsJgraphtEdgeFactory()) );

        // and keep the standard one not too far away...
        // g = new ListenableDirectedGraph(new DefaultDirectedGraph(new GsJgraphtEdgeFactory()) );

        setMainFrame(mainFrame);
    }
    
    public JComponent getGraphPanel() {
        return jgraph;
    }

    public JPanel getGraphMapPanel(JScrollPane sp) {
        return GPOverviewPanel.createOverviewPanel(this, sp);
    }

    //TODO remove: moved to Graph
    public boolean addNode(V vertex) {
        if (g.addNode(vertex)) {
            if (visible) {
                positionNodeAuto(vertex);
            }
            vertexCount++;
            return true;
        }
        return false;
    }

    /**
     * @param vertex
     */
    private void positionNodeAuto(Object vertex) {
        placeNode(vertex, curX, curY);
        curX += incX;
        if (curX > maxX) {
            curX = minX;
            curY += incY;
        }
    }

    //TODO partially moved to Graph. Move the GUI code to jgraphGUIImpl
    public boolean addEdge(E newedge) {
    	if (newedge == null) {
    		return false;
    	}
    	V source = newedge.getSource();
    	V target = newedge.getTarget();
    	if (!g.addEdge(source, target, newedge)) {
    		return false;
    	}
		
		if (visible) {
			if (source == target) {
				DefaultEdge[] t_cell = {m_jgAdapter.getEdgeCell(newedge)};
				GraphConstants.setRouting( t_cell[0].getAttributes(), pedgerouting);
    			GraphConstants.setLineStyle(t_cell[0].getAttributes(), GraphConstants.STYLE_BEZIER);
    			GraphConstants.setRemoveAttributes(
    					t_cell[0].getAttributes(),
    					new Object[] { GraphConstants.POINTS });
    			
        		m_jgAdapter.cellsChanged(t_cell);
			} else {
				E edge = g.getEdge(target, source);
        		if ( edge != null) {
    				DefaultEdge[] t_cell = {m_jgAdapter.getEdgeCell(edge), m_jgAdapter.getEdgeCell(newedge)};
    				if (t_cell[0] != null) {
    				    GraphConstants.setRouting( t_cell[0].getAttributes(), pedgerouting);
            			GraphConstants.setLineStyle(t_cell[0].getAttributes(), GraphConstants.STYLE_BEZIER);
    				}
    				if (t_cell[1] != null) {
    				    GraphConstants.setRouting( t_cell[1].getAttributes(), pedgerouting);
            			GraphConstants.setLineStyle(t_cell[1].getAttributes(), GraphConstants.STYLE_BEZIER);
    				}
    				m_jgAdapter.cellsChanged(t_cell);
        		}					
    		}
		}
        return true;
    }

    public void placeNode( Object vertex, int x, int y ) {
        if (!visible) {
			return;
		}
        DefaultGraphCell cell   = m_jgAdapter.getNodeCell( vertex );
        AttributeMap     attr   = cell.getAttributes();
        Rectangle2D      bounds = GraphConstants.getBounds( attr );

        Rectangle2D newBounds =
            new Rectangle2D.Double( x, y, bounds.getWidth(),
                bounds.getHeight() );

        GraphConstants.setBounds( attr, newBounds );

        AttributeMap cellAttr = new AttributeMap();
        cellAttr.put( cell, attr );
        m_jgAdapter.edit( cellAttr, null, null, null );
    }

    /**
     * @return the jgraph graph behind this graphManager.
     */
    public GsJgraph getJgraph() {
        return jgraph;
    }

    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void selectAll() {
        jgraph.setSelectionCells(jgraph.getRoots());
    }

    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void select(List l) {
        jgraph.setSelectionCells( new Object[0]);
        addSelection(l);
    }
    
 // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void select(Set s) {
        jgraph.setSelectionCells( new Object[0]);
        addSelection(s);
    }

    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void addSelection(List l) {
        if (l == null) {
            return;
        }
        for (Iterator it = l.iterator(); it.hasNext();) {
			Object o = (Object) it.next();
            if (o instanceof GsDirectedEdge) {
                jgraph.addSelectionCell(m_jgAdapter.getEdgeCell((E)o));
            } else {
                jgraph.addSelectionCell(m_jgAdapter.getNodeCell((V)o));
            }
        }
    }
    
    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void addSelection(Set s) {
        if (s == null) {
            return;
        }
        for (Iterator it = s.iterator(); it.hasNext();) {
			Object o = (Object) it.next();
            if (o instanceof GsDirectedEdge) {
                jgraph.addSelectionCell(m_jgAdapter.getEdgeCell((E)o));
            } else {
                jgraph.addSelectionCell(m_jgAdapter.getNodeCell(o));
            }
        }
    }

    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void vertexToFront(boolean b) {
        if (!visible) {
            return;
        }
        // move all vertex to front;
        Object[] t = getNodeArray();
        for (int i=0 ; i<t.length ; i++) {
            t[i] = m_jgAdapter.getNodeCell(t[i]);
        }
        if (b) {
            m_jgAdapter.toFront(t);
        } else {
            m_jgAdapter.toBack(t);
        }
    }
    
    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void invertSelection() {
		Object[] selects = jgraph.getSelectionCells();
		Object roots[] = jgraph.getRoots();
		int len = roots.length;
		int nbsel = selects.length;
		Vector toselect = new Vector(len - nbsel);
		for (int i=0 ; i<len ; i++) {
			toselect.add(roots[i]);
		}
		
		for (int i=len-1 ; i>=0 ; i--) {
			Object cur = roots[i];
			for (int j=0 ; j<nbsel ; j++) {
				if (selects[j] == cur) {
					toselect.remove(i);
					break;
				}
			}
		}
		jgraph.setSelectionCells(toselect.toArray());

    }

    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void showGrid(boolean b) {
        jgraph.setGridVisible(b);
    }
    
    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void setGridActive(boolean b) {
        jgraph.setGridEnabled(b);
    }

    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void zoomOut() {
        jgraph.setScale(jgraph.getScale()-0.1);
    }

    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void zoomIn() {
        jgraph.setScale(jgraph.getScale()+0.1);
    }

    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void zoomNormal() {
        jgraph.setScale(1);
    }

    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void displayEdgeName(boolean b) {
       jgraph.setEdgeLabelDisplayed(b);
    }

    /**
     * show/hide vertex name: not applicable as they are always visible.
     * @see fr.univmrs.tagc.GINsim.graph.GsGraphManager#displayNodeName(boolean)
     */
    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void displayNodeName(boolean b) {
    		// NOTHING
    }
    
    
    public void undo() {
		if (undoManager.canUndo()) {
			undoManager.undo();
		}
    }

    public void redo() {
    		if (undoManager.canRedo()) {
    			undoManager.redo();
    		}
    }

    public void delete() {
		//if selection
		if (jgraph.getSelectionCount()>0) {
			//get selected cells
			Object[] cells=jgraph.getSelectionCells();
            // just in case: empty the selection before
            jgraph.setSelectionCells(new Object[0]);
			for (int i=0;i<cells.length;i++) {
				//get jgrapht object
				Object obj=((DefaultGraphCell)cells[i]).getUserObject();
				//if it's an edge
				if (obj instanceof Edge) {
				    gsGraph.removeEdge((E)obj);
				//else it's a node
				} else {
				    gsGraph.removeNode((V)obj);
				}
			}
		}
    }

    public void removeNode(V obj) {
        g.removeNode(obj);
        vertexCount--;
        vertexRemoved(obj);
    }

	// TODO remove. moved to Graph
    public E getEdge(V source, V target) {
        return g.getEdge(source, target);
    }

    public GsGraph getGsGraph() {
        return gsGraph;
    }

    public Iterator getNodeIterator() {
        return g.vertexSet().iterator();
    }

    public Iterator getEdgeIterator() {
        return g.edgeSet().iterator();
    }

	public Iterator<E> getFullySelectedEdgeIterator() {
		if (visible) {
			return new GsSelectedEdgeWithNodeIterator(mainFrame.getSelectedNodes(), mainFrame.getSelectedEdges());
		}
        return g.edgeSet().iterator();
	}
	public Iterator<E> getSelectedEdgeIterator() {
		if (visible) {
			return mainFrame.getSelectedEdges().iterator();
		}
        return g.edgeSet().iterator();
	}

	public Iterator<V> getSelectedNodeIterator() {
		if (visible) {
			return mainFrame.getSelectedNodes().iterator();
		}
        return g.vertexSet().iterator();
	}

	// TODO remove. moved to Graph
    public Set<E> getIncomingEdges(V vertex) {
    	if (g instanceof ListenableDirectedGraph) {
    		return ((ListenableDirectedGraph)g).incomingEdgesOf(vertex);
    	}
        return g.edgesOf(vertex);
    }

	// TODO remove. moved to Graph
    public Set<E> getOutgoingEdges(V vertex) {
        if (g instanceof ListenableDirectedGraph) {
            return ((ListenableDirectedGraph)g).outgoingEdgesOf(vertex);
        }
        return g.edgesOf(vertex);
    }

    /**
     * @return the mainFrame in which this graph is opened (may be null)
     */
    public GsMainFrame getMainFrame() {
        return mainFrame;
    }

    public void removeEdge(V source, V target) {
    	E edge = g.getEdge(target, source);
		if (visible) {
			DefaultEdge de = m_jgAdapter.getEdgeCell(edge);
	    		if ( edge != null && GraphConstants.getRouting(de.getAttributes()) == pedgerouting) {
	    			AttributeMap attr = de.getAttributes();
	    		    de.getAttributes().remove(GraphConstants.ROUTING);
			        List l = GraphConstants.getPoints(attr);
                    if (l != null) {
                        while ( l.size() > 2) {
                            l.remove(1);
                        }
                        GraphConstants.setPoints(attr, l);
                    }

	    			m_jgAdapter.cellsChanged(new Object[] {de});
	    		}
		}
        g.removeEdge(source, target);
        edgeRemoved(edge);
    }

    public void ready() {
        if (mainFrame == null) {
            visible = false;
        } else {
	        new GsMarqueeHandler(this);
	        visible = true;
        }        
    }

    public GsNodeAttributesReader getNodeAttributesReader() {
        if (visible) {
            return new GsJgraphNodeAttribute(this);
        }
        return getFallBackVReader();
    }

    public GsEdgeAttributesReader getEdgeAttributesReader() {
        if (visible) {
            return new GsJgraphEdgeAttribute(this);
        }
        return getFallBackEReader();
    }

    /**
     * @param vertex
     * @return the vertex's attributeMap
     */
    public AttributeMap getNodeAttributesMap(Object vertex) {
        return m_jgAdapter.getNodeCell( vertex ).getAttributes();
    }

    public int getNodeCount() {
    		return vertexCount;
    }

	public void setMainFrame(GsMainFrame m) {
		mainFrame = m;
        if (mainFrame != null) {
            defaultNodeAttr = JGraphModelAdapter.createDefaultNodeAttributes();
            defaultEdgeAttr = JGraphModelAdapter.createDefaultEdgeAttributes(g);
            
	        GsJgraphEdgeAttribute.applyDefault(defaultEdgeAttr);
	        GsJgraphNodeAttribute.applyDefault(defaultNodeAttr);
	        pedgerouting = new GsParallelEdgeRouting();
            
            m_jgAdapter = new JGraphModelAdapter(g, defaultNodeAttr, defaultEdgeAttr);
	        jgraph = new GsJgraph( this );
	        visible = true;
            rereadVS();
        }
	}

	/**
	 * @return Returns the defaultEdgeAttr.
	 */
	public AttributeMap getDefaultEdgeAttr() {
		return defaultEdgeAttr;
	}
	/**
	 * @return Returns the defaultNodeAttr.
	 */
	public AttributeMap getDefaultNodeAttr() {
		return defaultNodeAttr;
	}
	/**
	 * @return the jgrapht to jgraph model adapter
	 */
	public JGraphModelAdapter getM_jgAdapter() {
		return m_jgAdapter;
	}
	/**
	 * @return the parallel edge routing
	 */
	public GsParallelEdgeRouting getPedgerouting() {
		return pedgerouting;
	}

	public Object[] getNodeArray() {
		return g.vertexSet().toArray();
	}
	/**
	 * @return the jgrapht graph
	 */
    public ListenableGraph<V, E> getG() {
        return g;
    }

    /**
     * @return the list of strong connected components
     */
    public List getStrongComponent() {
        return new StrongConnectivityInspector((DirectedGraph)g).stronglyConnectedSets();
    }

    /**
     * read existing graphic attributes and put them into the new jgraph.
     */
    private void rereadVS() {
        if (hasFallBackVSData()) {
            Map vsdata = getEdgeVSMap();
            Iterator it = vsdata.keySet().iterator();
            GsEdgeAttributesReader fereader = getFallBackEReader();
            GsEdgeAttributesReader ereader = getEdgeAttributesReader();
            ereader.copyDefaultFrom(fereader);
            while (it.hasNext()) {
                Object o = it.next();
                fereader.setEdge(o);
                ereader.setEdge(o);
                ereader.copyFrom(fereader);
            }
            vsdata = getNodeVSMap();
            it = vsdata.keySet().iterator();
            GsNodeAttributesReader fvreader = getFallBackVReader();
            GsNodeAttributesReader vreader = getNodeAttributesReader();
            
            while (it.hasNext()) {
                Object o = it.next();
                fvreader.setNode(o);
                vreader.setNode(o);
                vreader.copyFrom(fvreader);
            }
        }
    }
    
    
    public List getShortestPath(V source, V target) {
        return DijkstraShortestPath.findPathBetween(g, source, target);
    }

    public boolean containsNode(V vertex) {
        return g.containsNode(vertex);
    }
    
    public boolean containsEdge(V from, V to) {
        return g.containsEdge(from, to);
    }

    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public boolean isGridDisplayed() {
        if (!visible) {
            return false;
        }
        return jgraph.isGridVisible();
    }

    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public boolean isGridActive() {
        if (!visible) {
            return false;
        }
        return jgraph.isGridEnabled();
    }

    // TODO : defined in GraphGUI. Move the code to JgraphGUIImpl
    public void select(Object obj) {
        jgraph.setSelectionCells( new Object[0]);
        if (obj == null) {
            return;
        }
        if (obj instanceof GsDirectedEdge) {
        	if (obj instanceof Edge) {
                jgraph.addSelectionCell(m_jgAdapter.getEdgeCell((E)obj));
			} else {
				E de = (E)obj;
				jgraph.addSelectionCell(m_jgAdapter.getEdgeCell(getEdge(de.getSource(), de.getTarget())));
			}
        } else {
            jgraph.addSelectionCell(m_jgAdapter.getNodeCell(obj));
        }
    }
    
    public BufferedImage getImage() {
    	if (jgraph != null) {
    		return jgraph.getImage(Color.WHITE, 0);
    	}
    	return null;
    }

	// TODO remove. moved to Graph
	public Collection getAllEdges() {
		return g.edgeSet();
	}

	public Collection getAllNode() {
		return g.vertexSet();
	}
}
