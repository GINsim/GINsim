package org.ginsim.core.graph.backend;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

import org.ginsim.common.utils.ArraySet;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.view.NodeViewInfo;
import org.ginsim.core.graph.view.style.NodeStyle;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultGraphType;

/**
 * a "simple" jgrapht implementation using hashmap.
 * it aims at "low" memory consumption without getting too slow.
 * @param <E> edge
 * @param <V> vertex
 */
public class GsJGraphtBaseGraph<V,E extends Edge<V>> extends AbstractGraph<V, E> implements Graph<V,E>, EdgeFactory<V, E> {

    public static final GraphType TYPE;

    static {
        TYPE = DefaultGraphType.directedSimple();
    }

    protected Map<V, VInfo<V,E>> m_vertices = new HashMap<V, VInfo<V,E>>(10);
    private Set<E> edgeSet = null;
    private Set<V> vertexSet = null;
    private int edgeCount = 0;
    
    private NodeStyle<V> defaultNodeStyle = null;
    
    private static final Set emptySet = new HashSet();
    
    @Override
    public Set<E> getAllEdges(V sourceNode, V targetNode) {
        // no multiple edges here
        return null;
    }

    @Override
    public E getEdge(V sourceNode, V targetNode) {
        VInfo<V,E> vinfo = m_vertices.get(sourceNode);
        if (vinfo == null) {
            return null;
        }
        return vinfo.getOutgoing(targetNode);
    }

    @Override
    public EdgeFactory<V,E> getEdgeFactory() {
        return this;
    }

    @Override
    public Supplier<V> getVertexSupplier() {
        return null;
    }

    @Override
    public Supplier<E> getEdgeSupplier() {
        return null;
    }

    @Override
    public E addEdge(V sourceNode, V targetNode) {
        // at the same time, check if vertices exists in graph AND replace them if necessary
        VInfo<V,E> vinfo = m_vertices.get(sourceNode);
        if (vinfo == null) {
            return null;
        }
        
        V src = vinfo.self;
        vinfo = m_vertices.get(targetNode);
        if (vinfo == null) {
            return null;
        }
        // really create/add the edge
        E e = createEdge( src, vinfo.self );
        if (addEdge(sourceNode, targetNode, e)) {
            return e;
        }
        return null;
    }

	@Override
	public boolean addEdge(V sourceNode, V targetNode, E e) {
        VInfo<V,E> vinfo = m_vertices.get(sourceNode);
        if (vinfo.addOutgoing(e)) {
            vinfo = m_vertices.get(targetNode);
            vinfo.addIncoming(e);
            edgeCount++;
            return true;
        }
        return false;
    }

    @Override
    public V addVertex() {
        return null;
    }

    @Override
	public E createEdge(V source, V target) {
		return (E)new Edge<V>(null, getVertex(source), getVertex(target));
	}
	
    @Override
    public boolean addVertex(V v) {
        if (m_vertices.containsKey(v)) {
            return false;
        }
        m_vertices.put(v, new VInfo<V,E>( v, defaultNodeStyle ));
        return true;
    }

    public boolean updateNodeColorStyleVertex(V v, NodeStyle nodeStyle) {
        if (!m_vertices.containsKey(v)) {
            return false;
        }
        m_vertices.replace(v, new VInfo<V,E>( v, nodeStyle));
        return true;
    }

    /**
     * Get an existing vertex.
     * @param v
     * @return the vertex
     */
    public V getVertex(V v) {
    	VInfo<V, E> info = m_vertices.get(v);
        if (info == null) {
        	return null;
        }
        return info.self;
    }

    @Override
    public boolean containsEdge(E e) {
        VInfo<V,E> vinfo = m_vertices.get(e.getSource());
        if (vinfo == null) {
            return false;
        }
        return vinfo.containsOutgoing(e);
    }

    @Override
    public boolean containsVertex(V v) {
        return m_vertices.containsKey(v);
    }

    @Override
    public Set<E> edgeSet() {
        if (edgeSet == null) {
            edgeSet = new EdgeSet(this);
        }
        return edgeSet;
    }

    @Override
    public int degreeOf(V v) {
        return inDegreeOf(v) + outDegreeOf(v);
    }

    @Override
    public Set<E> edgesOf(V vertex) {
        Set<E> l1 = incomingEdgesOf(vertex);
        Set<E> l2 = outgoingEdgesOf(vertex);
        if (l1 == null) {
            return l2;
        } else if (l2 == null) {
            return l1;
        } else {
            Set<E> l = new HashSet<E>(l1);
            l.addAll(l2);
            return l;
        }
    }

    @Override
    public E removeEdge(V sourceNode, V targetNode) {
        VInfo<V,E> vinfo = m_vertices.get(sourceNode);
        if (vinfo == null) {
            return null;
        }
        E e = vinfo.getOutgoing(targetNode);
        if (e != null) {
            vinfo.removeOutgoing(e);
            m_vertices.get(targetNode).removeIncoming(e);
            edgeCount--;
        }
        return e;
    }

    @Override
    public boolean removeEdge(E e) {
        VInfo<V,E> vinfo = m_vertices.get(e.getSource());
        if (vinfo == null) {
            return false;
        }
        if (!vinfo.containsOutgoing(e)) {
            return false;
        }
        vinfo.removeOutgoing(e);
        m_vertices.get(e.getTarget()).removeIncoming(e);
        edgeCount--;
        return true;
    }

    @Override
    public boolean removeVertex(V v) {
        VInfo<V,E> vinfo = m_vertices.get(v);
        if (vinfo == null) {
            return false;
        }
        vinfo.cleanup(m_vertices);
        m_vertices.remove(v);
        return true;
    }

    @Override
    public Set<V> vertexSet() {
        if (vertexSet == null) {
            vertexSet = Collections.unmodifiableSet(m_vertices.keySet());
        }
        return vertexSet;
    }

    @Override
    public int inDegreeOf(V vertex) {
        return incomingEdgesOf(vertex).size();
    }

    @Override
    public Set<E> incomingEdgesOf(V vertex) {
        Set<E> l = m_vertices.get(vertex).l_incoming;
        if (l == null) {
            return emptySet;
        }
        return l;
    }

    @Override
    public int outDegreeOf(V vertex) {
        return outgoingEdgesOf(vertex).size();
    }

    @Override
    public Set<E> outgoingEdgesOf(V vertex) {
        // BUG FIX ISSUE #18
        VInfo<V, E> localVertex = m_vertices.get(vertex);
        Set<E> l = null;
        if(localVertex != null) {
             l = localVertex.l_outgoing;
        }
        if (l == null) {
            return emptySet;
        }
        return l;
    }
    
    public NodeViewInfo getNodeViewInfo(V vertex) {
    	return m_vertices.get(vertex);
    }
    
	@Override
	public V getEdgeSource(E e) {
		return e.getSource();
	}

	@Override
	public V getEdgeTarget(E e) {
		return e.getTarget();
	}

    @Override
    public GraphType getType() {
        return TYPE;
    }

    @Override
	public double getEdgeWeight(E e) {
		return 0;
	}

    @Override
    public void setEdgeWeight(E e, double v) {
        throw new UnsupportedOperationException("No weight in this graph");
    }

    protected int getEdgesCount() {
		return edgeCount;
	}
	protected Collection<VInfo<V, E>> getVInfoSet() {
		return m_vertices.values();
	}
}

/**
 * Store information about a node and its incoming and outgoing edges.
 * 
 * @author Aurelien Naldi
 *
 * @param <V>
 * @param <E>
 */
class VInfo<V,E extends Edge<V>> implements NodeViewInfo {
    V self;
    Set<E> l_incoming;
    Set<E> l_outgoing;
    
    int x = 0;
    int y = 0;
    NodeStyle<V> style = null;
    
    protected VInfo( V o, NodeStyle<V> style ) {
        self = o;
        this.style = style;
    }
    
	protected boolean addIncoming(E e ) {
        if (l_incoming == null) {
            l_incoming = new ArraySet<E>();
            l_incoming.add(e);
            return true;
        }
        if (l_incoming.contains(e)) {
            return false;
        }
        l_incoming.add(e);
        return true;
    }
    protected boolean addOutgoing(E e ) {
        if (l_outgoing == null) {
            l_outgoing = new ArraySet<E>();
            l_outgoing.add(e);
            return true;
        }
        if (l_outgoing.contains(e)) {
            return false;
        }
        l_outgoing.add(e);
        return true;
    }
    
    protected boolean containsOutgoing(E e) {
        if (l_outgoing == null) {
            return false;
        }
        return l_outgoing.contains(e);
    }
    
    protected void removeIncoming(E e) {
        if (l_incoming != null) {
            l_incoming.remove(e);
        }
    }
    protected void removeOutgoing(E e) {
        if (l_outgoing != null) {
            l_outgoing.remove(e);
        }
    }
    protected E getOutgoing(V target) {
        if (l_outgoing == null) {
            return null;
        }
        for (E e: l_outgoing) {
            if (e.getTarget().equals(target)) {
                return e;
            }
        }
        return null;
    }
    
    protected void cleanup( Map<V,VInfo<V, E>> m_vertices ) {
        if (l_incoming != null) {
            for (E e: l_incoming) {
                m_vertices.get(e.getSource()).removeOutgoing(e);
            }
        }
        if (l_outgoing != null) {
            for (E e: l_outgoing) {
                m_vertices.get(e.getTarget()).removeIncoming(e);
            }
        }
    }

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public NodeStyle<V> getStyle() {
		return style;
	}

	@Override
	public boolean setPosition(int x, int y) {
		if (x<0 || y < 0) {
			return false;
		}
		this.x = x;
		this.y = y;
		
		return true;
	}

	@Override
	public boolean setStyle(NodeStyle style) {
		if (this.style == style) {
			return false;
		}
		this.style = style;
		return true;
	}
}

class EdgeSet<V,E extends Edge<V>> implements Set<E> {
	
    private final GsJGraphtBaseGraph<V,E> g;
    
    public EdgeSet(GsJGraphtBaseGraph<V, E> graphManager) {
		this.g = graphManager;
	}

    @Override
	public int size() {
        return g.getEdgesCount();
    }

    @Override
    public boolean isEmpty() {
        return (size() == 0);
    }

    public boolean contains(E edge) {
        return g.containsEdge(edge);
    }

    @Override
    public Iterator<E> iterator() {
        return new EdgeIterator<V,E>(g.getVInfoSet());
    }

    @Override
    public Object[] toArray() {
    	Object[] array = new Object[this.size()];
    	int i = 0;
    	for (E edge : this) {
			array[i++] = edge;
		}
        return array;
    }

    @Override
    public boolean add(E edge) {
        return false;
    }
    @Override
    public void clear() {
    }

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return false;
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof Edge) {
			return contains((Edge)o);
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
        for (Object o: c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		if (a.length < size() ) {
			Array.newInstance(a.getClass(), size());
		}
		int idx = 0;
		for (E e: this) {
			a[idx++] = (T)e;
		}
		return a;
	}
}

class EdgeIterator<V,E extends Edge<V>> implements Iterator<E> {

    E next;
    Iterator<VInfo<V, E>> i_vertices;
    Iterator<E> i_edges;
    
    public boolean hasNext() {
        return (next != null);
    }

    public E next() {
    	if (next == null) {
    		throw new NoSuchElementException();
    	}
        E ret = next;
        selectNext();
        return ret;
    }

    public void remove() {
        // unsupported
    }
    
    private void selectNext() {
        next = null;
        if (i_edges != null && i_edges.hasNext()) {
            next = i_edges.next();
        } else while (i_vertices.hasNext()) {
            VInfo<V,E> vinfo = i_vertices.next();
            if (vinfo.l_outgoing != null) {
                i_edges = vinfo.l_outgoing.iterator();
                if (i_edges.hasNext()) {
                    next = i_edges.next();
                    break;
                }
            }
        }
    }

    protected EdgeIterator(Collection<VInfo<V, E>> infoSet) {
        this.i_vertices = infoSet.iterator();
        selectNext();
    }
}
