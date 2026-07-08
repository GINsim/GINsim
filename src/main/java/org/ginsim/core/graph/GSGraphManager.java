package org.ginsim.core.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.colomoto.biolqm.service.ExtensionLoader;
import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.core.graph.dynamicgraph.DynamicGraphImpl;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalTransitionGraphImpl;
import org.ginsim.core.graph.objectassociation.GraphAssociatedObjectManager;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.reducedgraph.ReducedGraphImpl;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraphImpl;
import org.ginsim.core.io.parser.GinmlParser;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.service.ServiceClassInfo;


/**
 * Manage registered graph types and handles graph creation (from scratch or through a parser).
 * It loads GraphFactory implementations annotated as services.
 *
 * @author Lionel Spinelli
 * @author Aurelien Naldi
 */
public class GSGraphManager {

    private static GSGraphManager instance = null;
    private HashMap<Class<Graph>, GraphFactory> graphFactories = new HashMap<Class<Graph>, GraphFactory>();
    private HashMap<Graph, GraphInfo<?>> graphFilepath = new HashMap<Graph, GraphInfo<?>>();
    
    public String getGraphType() {
        return "regulatory";
    }
    
    /**
     * The constructor of the manager retrieves the list of available GraphFactory
     */
    private GSGraphManager(){
        for (GraphFactory factory: ExtensionLoader.load_instances(GraphFactory.class)) {
    		graphFactories.put( factory.getGraphClass(), factory);
        }
    }
    
    /**
     * @return an instance of this graphDescriptor.
     */
    public static GSGraphManager getInstance() {
    	
        if (instance == null) {
            instance = new GSGraphManager();
        }
        return instance;
    }
    
    /**
     * Create a new (RegulatoryGraph)
     * 
     * @return a new Regulatory Graph
     */
    public RegulatoryGraph getNewGraph(){
    	
    	GraphFactory factory = graphFactories.get( RegulatoryGraph.class);
    	RegulatoryGraph graph = (RegulatoryGraph) factory.create();
    	registerGraph( graph);
    	
    	return graph;
    }
    
    /**
     * Create a new default graph of the given class
     * 
     * @param graph_class the class of the graph to instantiate
     * @return the new instance of the graph
     */
    public <C extends Graph> C getNewGraph( Class<C> graph_class){

    	GraphFactory factory = graphFactories.get( graph_class);
    	if( factory != null){
    		C graph = (C) factory.create();
    		registerGraph( graph);
    		return graph;
    	}
    	
    	return null;
    }

    public Collection<GraphFactory> getGraphFactories() {
        return graphFactories.values();
    }
    
    /**
     * 
     * @param graph_class
     * @param args
     * @return a new Graph of the specified type, or null if failed
     */
    public <C extends Graph> C getNewGraph( Class<C> graph_class, Object... args){
    	
    	C graph = null;
    	
    	GraphFactory factory = graphFactories.get( graph_class);
    	if( factory == null) {
    		LogManager.error( "No declared factory for graph class : " + graph_class);
    		return null;
    	}
    	
    	Method[] methods = factory.getClass().getMethods();
    	
    	Method found_method = null;
    	for( Method method : methods){
    		if( method.getName().equals( "create")){
    			Class[] method_params = method.getParameterTypes();
    			if( method_params.length == args.length){
    				boolean good_method = true;
    				for( int i = 0; i < method_params.length && good_method; i++){
    					Class param_class = method_params[i];
    					if( param_class.isPrimitive()){
    						String arg_class_name = args[i].getClass().getName();
    						if( !arg_class_name.substring( arg_class_name.lastIndexOf( ".")+1).toLowerCase().startsWith( param_class.getName().toLowerCase())){
    							good_method = false;
    						}
    					}
    					else{
        					try{
	    						param_class.cast( args[i]);
        					}
        					catch( ClassCastException cce){
        						good_method = false;
        					}
    					}
    				}
    				if( good_method){
    					found_method = method;
    					break;
    				}
    			}
    		}
    	}
    	
    	if( found_method != null){
	    	try{
	    		graph =(C) found_method.invoke( factory, args);
	    	}
			catch( InvocationTargetException ite){
				LogManager.error( "Unable to create graph of class " + graph_class);
				LogManager.error( ite);
			}
			catch( IllegalAccessException iae){
				LogManager.error( "Unable to create graph of class " + graph_class);
				LogManager.error( iae);
			}
			
			registerGraph( graph);
    	}
    	
		return graph;
    }

    
    /**
     * Search for the factory managing the given graph type and return the corresponding parser
     * 
     * @param graph_type the type of the graph
     * @return the class of the parser used to read the given graph type
     */
    public Class getParserClass( String graph_type){
    	
    	// workaround for the updated HTG key
    	if ("hierarchicalTransitionGraph".equals(graph_type)) {
    		graph_type = "hirarchical";
    	}
    	
    	for (GraphFactory<?> factory: graphFactories.values()) {
    		if( factory.getGraphType().equals( graph_type)){
    			return factory.getParser();
    		}
			
		}
    	
    	return null;
    }
    
    
    /**
     * register a graph without associated file path
     * 
     * @param graph the graph
     */
    public void registerGraph( Graph graph){
    	
    	registerGraph(graph, null);
    }
    
    
    /**
     * Memorize the link between the graph and the path of the file the graph has been loaded from or saved to
     * 
     * @param graph the graph
     * @param file_path the path of the file the graph has been loaded from or saved to
     */
    public <G extends Graph<?,?>> void registerGraph( G graph, String file_path){
    	GraphInfo info = graphFilepath.get(graph);
    	if (info == null) {
    		info = new GraphInfo<GraphModel<?,?>>();
        	graphFilepath.put( graph, info);
    	}
    	info.path = file_path;
    }
    
    
    /**
     * Return the list of registered graphs
     * 
     * @return the list of all registered graph types
     */
	public Set getAllGraphs() {

		return graphFilepath.keySet();
	}
    
	public <G extends GraphModel<?, ?>> void addGraphListener(G graph, GraphListener<G> listener) {
		GraphInfo<G> info = (GraphInfo<G>)graphFilepath.get(graph);
		if (info != null) {
			info.addListener(listener);
		}
	}
	public <G extends GraphModel<?, ?>> void removeGraphListener(G graph, GraphListener<G> listener) {
		GraphInfo<G> info = (GraphInfo<G>)graphFilepath.get(graph);
		if (info != null) {
			info.removeListener(listener);
		}
	}
	
	public <G extends GraphModel<?, ?>> void fireGraphChange(G graph, GraphChangeType type, Object data) {
		GraphInfo<G> info = (GraphInfo<G>)graphFilepath.get(graph);
		if (info == null || info.listeners == null) {
			return;
		}
		
        List<GraphEventCascade> l_cascade = new ArrayList<GraphEventCascade>();
		for (GraphListener<G> l: info.listeners) {
			GraphEventCascade gec = l.graphChanged(graph, type, data);
            if (gec != null) {
                l_cascade.add(gec);
            }
		}
		
        if (l_cascade.size() > 0) {
        	// TODO: detail in cascade update notification...
        	NotificationManager.publishInformation(this, "Cascade update");
        }

	}
    
    /**
     * Return the path of the file the graph has been loaded from or saved to (if it exists)
     * 
     * @param graph
     * @return the path of the file the graph has been loaded from or saved to if it exsists, null if not.
     */
    public String getGraphPath( Graph graph){
    	
    	GraphInfo<?> info = graphFilepath.get( graph);
    	if( info != null){
    		return info.path;
    	}
    	else{
    		return null;
    	}
    }
    
    /**
     * Return the graph associated to the given file path, if it exists
     * 
     * @param path the file path associated to the desired graph
     * @return the graph associated to the given file path, if it exists, null if not
     */
    public Graph getGraphFromPath( String path){
    	
    	if( path != null){
	    	for (Iterator<Entry<Graph,GraphInfo<?>>> iterator = graphFilepath.entrySet().iterator(); iterator.hasNext();) {
	    		Entry<Graph,GraphInfo<?>> entry = iterator.next();
				if( path.equals( entry.getValue().path)){
					return entry.getKey();
				}
			}
    	}
    	
    	return null;
    }
    

    /**
     * Retrieve the Graph stored in the file at the given file path
     * 
     * @param file_path the file path of the graph file
     * @return the Graph stored in the file at the given file path
     * @throws GsException
     */
    public Graph open( String file_path) throws GsException{
    	
    	File file = new File( file_path);
    	if( !file.exists() || !file.isFile()){
    		throw new GsException( GsException.GRAVITY_ERROR, "No file on path " + file_path);
    	}
    	
    	return open(null, file);
    }
    
    /**
     * 
     * @param file the File containing the graph to open
     * @return a graph of the correct type read from the given file
     */
	public Graph open(File file)  throws GsException{
		
		return open(null, file);
	}
    
    
    /**
     * 
     * @param file the File containing the graph to open
     * @return a graph of the correct type read from the given file
     */
    public Graph open(Set set, File file) throws GsException{
        // FIXME: do not leak implementation classes
        try {
            ZipFile f = new ZipFile(file);
            try {
                GinmlParser parser = new GinmlParser();
                boolean usePrefix = false;
                ZipEntry ze = f.getEntry("ginml");
                if (ze==null) {
                	usePrefix = true;
                	ze = f.getEntry( AbstractGraph.ZIP_PREFIX + RegulatoryGraphImpl.GRAPH_ZIP_NAME);
                	if (ze == null) {
                		ze = f.getEntry( AbstractGraph.ZIP_PREFIX + DynamicGraphImpl.GRAPH_ZIP_NAME);
                    	if (ze == null) {
                    		ze = f.getEntry( AbstractGraph.ZIP_PREFIX + ReducedGraphImpl.GRAPH_ZIP_NAME);
                        	if (ze == null) {
                        		ze = f.getEntry( AbstractGraph.ZIP_PREFIX + HierarchicalTransitionGraphImpl.GRAPH_ZIP_NAME);
								if (ze == null) {
									ze = f.getEntry( AbstractGraph.ZIP_PREFIX + HierarchicalTransitionGraphImpl.GRAPH_ZIP_NAME_SCC);
									if (ze == null) {
										throw new GsException( GsException.GRAVITY_ERROR, "Unable to find a known main zip entry");
									}
								}
                        	}
                    	}
                	}
                }
                
                Graph graph = parser.parse(f.getInputStream(ze), set);
                if (set == null) {
                	// try to restore associated data ONLY if no subgraph is selected
                	// TODO: need to load associated entry with subgraphs
                	List v_omanager = ObjectAssociationManager.getInstance().getObjectManagerList();
	                if (v_omanager != null) {
	                    for (int i=0 ; i<v_omanager.size() ; i++) {
	                        GraphAssociatedObjectManager manager = (GraphAssociatedObjectManager)v_omanager.get(i);
	                        ze = f.getEntry((usePrefix ? AbstractGraph.ZIP_PREFIX:"")+manager.getObjectName());
	                        if (ze != null) {
	                        	try {
	                        		Object o = manager.doOpen(f.getInputStream(ze), graph);
	                        		ObjectAssociationManager.getInstance().addObject( graph, manager.getObjectName(), o);
	                        	} catch (Exception e) {
	                        		LogManager.error(e);
	                        	}
	                        }
	                    }
	                }
	                graph.parseAssociated(f, usePrefix ? AbstractGraph.ZIP_PREFIX:"");
	                v_omanager = ObjectAssociationManager.getInstance().getObjectManagerList( graph.getClass());
	                if (v_omanager != null) {
	                    for (int i=0 ; i<v_omanager.size() ; i++) {
	                        GraphAssociatedObjectManager manager = (GraphAssociatedObjectManager)v_omanager.get(i);
	                        ze = f.getEntry((usePrefix ? AbstractGraph.ZIP_PREFIX:"")+manager.getObjectName());
	                        if (ze != null) {
	                        	try {
	                        		Object o = manager.doOpen(f.getInputStream(ze), graph);
		                            ObjectAssociationManager.getInstance().addObject( graph, manager.getObjectName(), o);
	                        	} catch (Exception e) {
	                        		LogManager.error(e);
	                        	}
	                        }
	                    }
	                }
                }
                registerGraph( graph, file.getAbsolutePath());
                return graph; 
            } catch (Exception e) {
            	LogManager.error( "Error while opening Graph : " + file.getPath());
            	LogManager.error( e);
                return null;
            }
        } catch (Exception e) {// opening as zip failed, try the old method instead
        }

        // not a zip file
        GinmlParser parser = new GinmlParser();
        try {
            Graph graph = parser.parse(new FileInputStream(file), set);
            registerGraph( graph, file.getAbsolutePath());
            return graph;
        } catch (FileNotFoundException e) {
        	LogManager.error( "Error while opening Graph : " + file.getPath());
        	LogManager.error( e);
            throw new GsException(GsException.GRAVITY_ERROR, e);
        }
        catch( GsException gse) {
        	LogManager.error( "Error while opening Graph : " + file.getPath() + " : " + gse.getMessage());
        	LogManager.error( gse);
        	throw gse;
		}
    }
    
	/**
	 * Remove all the server side association to the graph
	 * 
	 * @param graph the graph to close
	 */
	public void close( Graph graph){
		
		graphFilepath.remove( graph);
		
		// Remove the graph associated objects
		ObjectAssociationManager.getInstance().removeAllObjects( graph);
		
		// Remove the references to the graph as associated graph
		for( Graph other_graph : graphFilepath.keySet()){
			if( other_graph instanceof GraphAssociation){
				try{
					Graph associated_graph = ((GraphAssociation) other_graph).getAssociatedGraph();
				    if (graph == associated_graph) {
				    	((GraphAssociation) other_graph).setAssociatedGraphID( getGraphPath( graph));
				    	((GraphAssociation) other_graph).setAssociatedGraph(null);
				    }
				}
				catch( GsException ge){
					LogManager.error( "Unable to verify the associated graph of graph : " + other_graph.getGraphName());
					LogManager.error( ge);
				}
			}
		}
	}

    public ServiceClassInfo[] getGraphsInfo() {
        ServiceClassInfo[] ret = new ServiceClassInfo[graphFactories.size()];
        int idx = 0;
        for (GraphFactory factory: graphFactories.values()) {
            ret[idx++] = new ServiceClassInfo(factory.getClass());
        }
        return ret;
    }
}

class GraphInfo<G extends GraphModel<?, ?>> {
	public String path;
	public List<GraphListener<G>> listeners = null;
	
	public void addListener(GraphListener<G> l) {
		if (listeners == null) {
			listeners = new ArrayList<GraphListener<G>>();
		}
		listeners.add(l);
	}
	public void removeListener(GraphListener<G> l) {
		if (listeners != null) {
			listeners.remove(l);
		}
	}
}