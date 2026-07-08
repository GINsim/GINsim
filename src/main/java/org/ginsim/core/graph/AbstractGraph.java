package org.ginsim.core.graph;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.core.annotation.Annotation;
import org.ginsim.core.graph.backend.EdgeAttributeReaderImpl;
import org.ginsim.core.graph.backend.JgraphtBackendImpl;
import org.ginsim.core.graph.backend.NodeAttributeReaderImpl;
import org.ginsim.core.graph.objectassociation.GraphAssociatedObjectManager;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.graph.view.ViewCopyHelper;
import org.ginsim.core.graph.view.style.StyleManager;
import org.ginsim.core.io.parser.GINMLWriter;

/**
 * Base class for graphs using a storage backend: it provides generic methods and storage abstraction.
 * The actual structure is stored in the graph backend, and classes deriving from this one
 * can provide specialised methods.
 * 
 * @author Lionel Spinelli
 * @author Aurelien Naldi
 *
 * @param <V>   the type of vertices
 * @param <E>   the type of edges
 */
abstract public class AbstractGraph<V, E extends Edge<V>> implements Graph<V, E>, GraphViewListener {
	/**
	 * 	vd manager attribute
 	 */
    private static final List<GraphAssociatedObjectManager> v_OManager = new ArrayList<GraphAssociatedObjectManager>();
	/**
	 * 	ZIP prefix string
 	 */
	public static final String ZIP_PREFIX = "GINsim-data/";
	/**
	 * 	graph backend attribute
 	 */
	private final GraphBackend<V,E> graphBackend;
	/**
	 * 	factory attribute
	 */
	private final GraphFactory factory;

	/**
	 * 	view data
 	 */
	private GraphViewListener listener;

	/**
	 * 	cache attribute readers for internal usage
 	 */
    private EdgeAttributesReader cachedEReader = null;
	/**
	 * 	cache NodeAttributesReader
	 */
	private NodeAttributesReader cachedNReader = null;

	/**
	 * 	The name of the graph
	 */
	protected String graphName = "default_name";

	/**
	 * Attributes Map of string string
 	 */
	protected Map<String,String> attributes = new HashMap<>();

	/**
	 * The annotation associated with the graph
	 */

	protected Annotation graphAnnotation = null;
	/**
	 * 	style manager attribute
	 */
    private StyleManager<V, E> styleManager = null;
    

	/**
	 * Create a new graph with the default back-end.
	 * @param factory the factory from construction
	 */
	public AbstractGraph(GraphFactory factory) {
    	this.factory = factory;
    	this.graphBackend = (GraphBackend<V, E>)JgraphtBackendImpl.getGraphBackend(this);
        graphBackend.setViewListener(this);
	}
	
	
    /**
     * Give access to the name of the graph
     * 
     * @return the name associated with this graph.
     */
    public String getGraphName() {
    	
        return graphName;
    }
    
    
    /**
     * changes (if success) the name associated with this graph.
     * By default only valid xmlid are accepted.
     *
     * @param graph_name the new name.
     * @throws GsException if the name is invalid.
     */
    public void setGraphName( String graph_name) throws GsException {
        // FIX #15
		if (!graph_name.matches("[a-zA-Z_]+[a-zA-Z0-9_-]*")) {
		//if (!graph_name.matches("[a-zA-Z0-9_-]*")) {
		    throw new GsException(GsException.GRAVITY_ERROR, "Invalid name");
		}
        this.graphName = graph_name;
        fireMetaChange();
    }

    @Override
	public String getDisplayName(V node) {
    	return node.toString();
	}

	public Map<String,String> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}


//    //----------------------   GRAPH SAVING MANAGEMENT METHODS -------------------------------
//    
//    /**
//     * Set the mode of saving the graph must used when saved
//     * 
//     * @param save_mode the mode of saving
//     */
//    public void setSaveMode( int save_mode){
//    	
//    	saveMode = save_mode;
//    }
//    
//    /**
//     * Return the mode the graph must used when saved
//     * 
//     * @return the mode the graph must used when saved
//     */
//    public int getSaveMode(){
//    	
//    	return saveMode;
//    }
    
	/**
	 * Return the zip extension for the graph type
	 * 
	 * @return the zip extension for the graph type
	 */
    protected String getGraphZipName() {
    	
    	return "ginml";
    }
    
    
    //----------------------   GRAPH VERTICES AND EDGES MANAGEMENT METHODS -------------------------------

	
	/**
	 * Add a node to this graph structure
	 * 
	 * @param node the node V
	 * @return the created node
	 */
	@Override
	public boolean addNode( V node) {
		
		if (graphBackend.addNodeInBackend(node)) {
			fireGraphChange(GraphChangeType.NODEADDED, node);
			return true;
		}
		
		return false;
	}

	/**
	 * Add an edge to this graph structure.
	 * 
	 * @param edge the edge E
	 * @return the created edge
	 */
	@Override
	public boolean addEdge(E edge) {
		
		if (graphBackend.addEdgeInBackend(edge)) {
			fireGraphChange(GraphChangeType.EDGEADDED, edge);
			return true;
		}
		return false;
	}
	
    /**
     * Remove a node from the graph.
     * 
     * @param node the node
     * @return true if the node was effectively removed
     */ 
	@Override
	public boolean removeNode(V node) {
        graphBackend.damage(node);
        boolean ret = graphBackend.removeNode(node);
        if (ret) {
            fireGraphChange(GraphChangeType.NODEREMOVED, node);
        }
        return ret;
	}

	
    /**
     * Remove an edge from the graph.
     * 
     * @param edge the edge E
     * @return true if the edge was effectively removed
     */
	@Override
	public boolean removeEdge(E edge) {
		
		return graphBackend.removeEdge(edge);
	}

	
	/**
	 * @return the number of node in this graph.
	 */
	@Override
	public int getNodeCount() {
		return graphBackend.getNodeCount();
	}

	
    /**
     * @param source the source edge V
     * @param target the target edge v
     * @return the edge between source and target or null if not found.
     */
	@Override
	public E getEdge(V source, V target) {
		
		return graphBackend.getEdge(source, target);
	}

	
    /**
	 * getter for the collections of edges
     * @return a Collection of the graph edges.
     */
	@Override
	public Collection<E> getEdges() {
		return graphBackend.getEdges();
	}

	
    /**
     * @return a Collection of the graph vertices.
     */
	@Override
	public Collection<V> getNodes() {
		return graphBackend.getNodes();
	}

	/**
	 * Return a list of set of node, each set containing a strongly connected component of the graph
	 * 
	 * @return a list of set of node, each set containing a strongly connected component of the graph
	 */
	@Override
	public List<Set<V>> getStronglyConnectedComponents() {
		return graphBackend.getStronglyConnectedComponents();
	}

	/**
	 * Give access to the node named with the given name
	 * 
	 * @param id name of a node
	 * @return the node corresponding to this unique id or null if not found.
	 */
	@Override
	public V getNodeByName( String id) {
		
		return graphBackend.getNodeByName( id);
	}

	
    /**
     * Return the size of the node order
     * 
     * @return the size of the node order
     */
    @Override
	public abstract int getNodeOrderSize();
	
	
	/**
	 * Search the vertices with ID matching the given regular expression. 
	 * Other kind of graph could overwrite this method. 
	 * 
	 * @param regexp the regular expression node ID must match to be selected
	 * @return a List of vertices
	 */
	public List<V> searchNodes( String regexp) {
		
		List<V> v = new ArrayList<V>();
		
		Pattern pattern = Pattern.compile(regexp, Pattern.COMMENTS | Pattern.CASE_INSENSITIVE);
		
		for (Iterator<V> it = getNodes().iterator(); it.hasNext();) {
			V vertex = (V) it.next();
			Matcher matcher = pattern.matcher(vertex.toString());
			if (matcher.find()) {
				v.add(vertex);
			}
		}
		return v;
	}
	
	@Override
    public boolean containsNode(V node) {
        return graphBackend.containsNode(node);
    }
    
	@Override
	public V getExistingNode(V node) {
		return graphBackend.getExistingNode(node);
	}
	
	@Override
    public boolean containsEdge(V from, V to) {
        return graphBackend.containsEdge(from, to);
    }	
	
	@Override
	public Collection<E> getIncomingEdges(V node) {
		return graphBackend.getIncomingEdges(node);
	}
	
	@Override
	public Collection<E> getOutgoingEdges(V node) {
		return graphBackend.getOutgoingEdges(node);
	}
	
	@Override
	public List<E> getShortestPath( V source, V target){
		
		return graphBackend.getShortestPath( source, target);
	}
	
	@Override
	public void addViewListener(GraphViewListener listener) {
		this.listener = listener;
	}
	
	 /**
	 * Declare an object visual setting change
	 * 
	 * @param o object
	 */
	public void refresh(Object o) {
		if (listener != null) {
			listener.refresh(o);
		} else if(o == null) {
		} else if (o instanceof Edge) {
			fireGraphChange(GraphChangeType.EDGEUPDATED, o);
		} else {
			fireGraphChange(GraphChangeType.NODEUPDATED, o);
		}
	}
    /*
	* repaint function
	*/
	@Override
	public void repaint() {
		if (listener != null) {
			listener.repaint();
		}
	}

	 /**
	 * dammage function
	 * @param o object to dammage
	 */
	public void damage(Object o) {
		if (listener != null) {
			listener.refresh(o);
		} else if (o instanceof Edge) {
			fireGraphChange(GraphChangeType.EDGEDAMAGED, o);
		} else {
			fireGraphChange(GraphChangeType.NODEDAMAGED, o);
		}
	}
	 /**
	 * update Evsmap attribute function
	 *
	 */
    @Override
	public void updateEvsmap(){
		graphBackend.updateEvsmap();
	}
	@Override
	public StyleManager<V, E> getStyleManager() {
		if (styleManager == null) {
			this.styleManager = new StyleManager<V, E>(this, graphBackend, factory);
		}
		return styleManager;
	}

	@Override
	public EdgeAttributesReader getEdgeAttributeReader() {
		return new EdgeAttributeReaderImpl(getStyleManager(), graphBackend, getNodeAttributeReader());
	}
	
	@Override
	public NodeAttributesReader getNodeAttributeReader() {
		return new NodeAttributeReaderImpl(getStyleManager(), this, graphBackend);
	}

	/**
	 * getter of the EdgeAttributesReader
	 * @return the EdgeAttributesReader
	 */
	protected EdgeAttributesReader getCachedEdgeAttributeReader() {
		if (cachedEReader == null) {
			cachedEReader = getEdgeAttributeReader();
		}
		return cachedEReader;
	}

	/**
	 * getter of the NodeAttributesReader
	 * @return the NodeAttributesReader
	 */
	protected NodeAttributesReader getCachedNodeAttributeReader() {
		if (cachedNReader == null) {
			cachedNReader = getNodeAttributeReader();
		}
		return cachedNReader;
	}

	/**
	 * getter of the dimension
	 * @return the deimension
	 */
	public Dimension getDimension() {
    	int width = 0;
    	int height = 0;
		NodeAttributesReader nreader = getNodeAttributeReader();
		for (V node: getNodes()) {
			nreader.setNode(node);
    		int x = nreader.getX() + nreader.getWidth();
    		if (x > width) {
    			width = x;
    		}
    		int y = nreader.getY() + nreader.getHeight();
    		if (y > height) {
    			height = y;
    		}
		}
		
		EdgeAttributesReader ereader = getEdgeAttributeReader();
        for (Edge<V> e: getEdges()) {
            ereader.setEdge(e);
            List<Point> points = ereader.getPoints();
            if (points == null) {
				// Make sure to include self-loops in the bounding box
				if (e.getSource() == e.getTarget()) {
					Rectangle bounds = ereader.getBounds();
					int x = (int)(bounds.getX() + bounds.getWidth());
					if (x > width) {
						width = x;
					}
					int y = (int)(bounds.getY() + bounds.getHeight());
					if (y > height) {
						height = y;
					}
				}
                continue;
            }
            for (Point2D pt: points ) {
                int x = (int)pt.getX();
        		if (x > width) {
        			width = x;
        		}
                int y = (int)pt.getY();
        		if (y > height) {
        			height = y;
        		}
            }
        }

		
		return new Dimension(width+5, height+5);
	}

	
    /**
	 * tes if empty
     * @return true if the graph is empty
     */
    public boolean isEmpty() {
        return getAnnotation().isEmpty() && getNodeCount() == 0;
    }
	
	
    /**
     * Launch the merge method on the specialized graph level, merging the current graph with the given one
     * and fire a graph change event
     * 
     * @param graph the graph to merge with the current one
     * @return  list merged
     */
	@Override
	public List<?> merge( Graph<V, E> graph) {
		
		List<?> v = this.doMerge(graph);
        if (v != null) {
        	fireGraphChange( GraphChangeType.GRAPHMERGED, v);
        	//TODO Move the select on the GUI side
        	//graphManager.select(v);
        }
        
    	return v;
	}
	
	
	/**
	 * Specialized method that execute the merging of the given graph with the current one
	 * Must be override at specialized graph level
	 * 
	 * @param graph the graph
	 * @return the list
	 */
	abstract protected List<?> doMerge( Graph<V, E> graph);
	

	/**
	 * Specialized method that build the sub-graph corresponding to the given lists of vertices and edges
	 * The returned graph contains clones of the given graph objects structured as they are in the current graph
	 * 
	 * @param node the list of node to include in the desired sub-graph
	 * @param edges the list of edges to include in the sub-graph
	 * @return a Graph containing clones of initial vertices and edges structured as they are in the current graph
	 */
	@Override
	public abstract Graph<V, E> getSubgraph(Collection<V> node, Collection<E> edges);
    
    
    //----------------------   ANNOTATION METHODS --------------------------------------------

    
    
	/**
     * Give access to the annotation associated with this graph.
     * 
	 * @return the association associated with this graph
	 */
	public Annotation getAnnotation() {
		if (graphAnnotation == null) {
			graphAnnotation = new Annotation();
		}
		return graphAnnotation;
	}
	
	
	// ----------------------   SAVING METHODS -----------------------------------------------

	/**
	 * save function
	 * @param save_path  the path to save
	 * @throws GsException exception
	 */
	public void save(String save_path) throws GsException {
		save(save_path, null, null);
	}

	/**
	 * save partial function
	 * @param save_path the save path
	 * @param vertices  the vertice to save
	 * @param edges the edge to save
	 * @throws GsException exception
	 */
	public void save(String save_path, Collection<V> vertices, Collection<E> edges) throws GsException {

		if (vertices == null) {
			vertices = getNodes();
			edges = getEdges();
		}
		// TODO: make sure that selected edges don't refer non-selected nodes

		File f;
		if ( save_path != null ){
			f = new File( save_path);
		}
		else {
			String save_filepath = GSGraphManager.getInstance().getGraphPath( this);
			if ( save_filepath != null){
				f = new File( save_filepath);
			}
			else {
				throw new GsException( GsException.GRAVITY_ERROR, "No save path has been set for this file");
			}
		}
		File ftmp = null;
		String fileName = f.getAbsolutePath();;
		if (f.exists()) {
			// create a temporary file to avoid destroying a good file in case save does not work
			try {
				ftmp = File.createTempFile(f.getName(), null, f.getParentFile());
				fileName = ftmp.getAbsolutePath();
			} catch (Exception e) {
				// TODO : REFACTORING ACTION
				// TODO : introduce a clean permission checking
				LogManager.error( "Could not use a tmp file in the same directory");
				LogManager.error( e);
				//       			ftmp = File.createTempFile(f.getName(), null);
				//       			fileName = ftmp.getAbsolutePath();
			}
		}

		try {
			ZipOutputStream zos = new ZipOutputStream( new FileOutputStream(fileName));
			// FIXME: uncompressed Zip require to set the size and CRC by hand!!
			// this must be done for each ZipEntry: save it to a tmpfile,
			// mesure the CRC and the size, then put it in the uncompressed zip...
			zos.putNextEntry(new ZipEntry(ZIP_PREFIX + getGraphZipName()));
			OutputStreamWriter osw = new OutputStreamWriter(zos, "UTF-8");
			
			// TODO: doSave should take the selection as parameter.
			// note: this method should be the only caller and ensures that the selection is defined and consistent
			doSave(osw, vertices, edges);
			osw.flush();
			zos.closeEntry();
			// now save associated objects
			this.saveAssociated(zos, osw);
			if (v_OManager != null) {
				for (GraphAssociatedObjectManager manager: v_OManager) {
					if (manager == null) {
						LogManager.error("Non-existing object manager?!?");
						continue;
					}
					if (manager.needSaving(this)) {
						zos.putNextEntry(new ZipEntry(ZIP_PREFIX+manager.getObjectName()));
						try {
							manager.doSave(osw, this);
						} catch (GsException e) {
							throw e;
						} finally {
							osw.flush();
							zos.closeEntry();
						}
					}
				}
			}
			List<GraphAssociatedObjectManager> v_specManager = ObjectAssociationManager.getInstance().getObjectManagerList( this.getClass());
			if (v_specManager != null) {
				for (GraphAssociatedObjectManager manager: v_specManager) {
					if (manager == null) {
						LogManager.error("Non-existing object manager?!?");
						continue;
					}
					if (manager.needSaving(this)) {
						zos.putNextEntry(new ZipEntry(ZIP_PREFIX+manager.getObjectName()));
						manager.doSave(osw, this);
						osw.flush();
					}
				}
			}
			zos.close();
		} catch (Exception e) {
			throw new GsException(GsException.GRAVITY_ERROR, e);
		}

		if (ftmp != null) {
			// Everything went fine, rename the temporary file
			boolean r = ftmp.renameTo(f);
			if (!r) {
				if (f.exists()) {
					f.delete();
					r = ftmp.renameTo(f);
				}
				if (!r) {
					new GsException( GsException.GRAVITY_ERROR, "Enable to save the file to chosen path : "+ftmp.getAbsolutePath());
				}
			}
		}
	}

	
	@Override
	public void copyView(Graph<V,E> src, ViewCopyHelper<Graph<V,E>,V,E> helper) {

		// copy nodes
		NodeAttributesReader sNReader = src.getNodeAttributeReader();
		NodeAttributesReader dNReader = this.getNodeAttributeReader();

		Dimension offset = helper.getOffset();
		
		for (V destNode : this.getNodes()) {
			V srcNode = helper.getSourceNode(destNode);
			if (srcNode == null) {
				continue;
			}
			dNReader.setNode(destNode);
			sNReader.setNode(srcNode);
			dNReader.copyFrom(sNReader);
			if (offset != null) {
				dNReader.move(offset.width, offset.height);
			}
			dNReader.refresh();
		}

		
		// copy edges
		EdgeAttributesReader sEReader = src.getEdgeAttributeReader();
		EdgeAttributesReader dEReader = this.getEdgeAttributeReader();
		for (E dEdge: this.getEdges()) {
			E sEdge = helper.getSourceEdge(dEdge);
            if (sEdge == null) {
            	continue;
            }
			sEReader.setEdge(sEdge);
			dEReader.setEdge(dEdge);
			dEReader.copyFrom(sEReader);
			dEReader.refresh();
		}

	}

	
	
	// ====================================================================================
	// ====================================================================================
	// METHODS THAT DO NOT APPEAR ON GRAPH INTERFACE 
	// ====================================================================================
	// ====================================================================================
    
	// ----------------------   SAVING METHODS -----------------------------------------------
   

	/**
	 * save implementation for a specific graph type.
	 * 
	 * @param osw		stream writer
	 * @param nodes 	vertices that should be saved (can not be null)
	 * @param edges		edges that should be saved (can not be null)
	 * @throws GsException exception of graph
	 */
	protected final void doSave(OutputStreamWriter osw, Collection<V> nodes, Collection<E> edges) throws GsException {
		GINMLWriter writer = getGINMLWriter();
		if (writer == null) {
			throw new GsException(GsException.GRAVITY_ERROR, "Can not save this graph type");
		}
		try {
			writer.write(osw, nodes, edges);
		} catch (IOException e) {
            throw new GsException( "STR_unableToSave", e);
        }
	}

	/**
	 * getter the  GINMLWriter
	 * @return the  GINMLWriter  coponent
	 */
	protected GINMLWriter getGINMLWriter() {
		return null;
	}

	// -------------------------  EVENT MANAGEMENT METHODS ---------------------------------

	/**
	 * firechange
	 */
	public void fireMetaChange() {
    	
        fireGraphChange(GraphChangeType.METADATACHANGE, null);
    }

	@Override
	public void fireGraphChange(GraphChangeType type, Object data) {
		GSGraphManager.getInstance().fireGraphChange(this, type, data);
	}

}
