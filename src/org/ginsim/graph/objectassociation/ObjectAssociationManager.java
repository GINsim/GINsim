package org.ginsim.graph.objectassociation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.ginsim.graph.common.Graph;


public class ObjectAssociationManager {

	private static ObjectAssociationManager instance;
	
	private List<GsGraphAssociatedObjectManager> objectManagers = null;
	private HashMap<Class,List<GsGraphAssociatedObjectManager>> specializedObjectManagers = null;
	
    // The map linking objects associated to the Graph with their representative key
    private HashMap<Graph,Map<Object,Object>> objectsOfGraph;
	
	private ObjectAssociationManager(){
		
		objectManagers = new Vector<GsGraphAssociatedObjectManager>();
		specializedObjectManagers = new HashMap<Class, List<GsGraphAssociatedObjectManager>>();
		objectsOfGraph = new HashMap<Graph, Map<Object,Object>>();
		
	}
	
	static public ObjectAssociationManager getInstance(){
		
		if( instance == null){
			instance = new ObjectAssociationManager();
		}
		
		return instance;
	}
	
	
	/**
     * Register an object manager not associated with a graph class
     *
     * @param manager
     */
    public void registerObjectManager( GsGraphAssociatedObjectManager manager) {
    	
    	objectManagers.add( manager);
    }
    
    /**
     * Register an object manager associated to a graph class.
     * 
     * @param manager
     */
    public void registerObjectManager( Class graph_class, GsGraphAssociatedObjectManager manager) {
    	
    	Class interface_class = getGraphInterface( graph_class);
    	
    	if( interface_class != null){
    	
	    	List<GsGraphAssociatedObjectManager> specialized_managers =  specializedObjectManagers.get( interface_class);
	    	
	    	if( specialized_managers != null){
		    	for (int i=0 ; i<specialized_managers.size(); i++) {
		    		if (((GsGraphAssociatedObjectManager)specialized_managers.get(i)).getObjectName().equals(manager.getObjectName())) {
		    			return;
		    		}            
		    	}
	    	}
	    	else{
	    		specialized_managers = new Vector<GsGraphAssociatedObjectManager>();
	    		specializedObjectManagers.put( interface_class, specialized_managers);
	    	}
	    	
	    	specialized_managers.add( manager);
    	}
    }
    
    
    /**
     * Give access to the list of registered object managers that are not associated with a graph class
     * 
     * @return the list of registered object managers
     */
    public List<GsGraphAssociatedObjectManager> getObjectManagerList() {
    	
        return objectManagers;
    }
    
    /**
     * Give access to the list of registered object managers for the given graph class
     * 
     * @return the list of registered object managers
     */
    public List<GsGraphAssociatedObjectManager> getObjectManagerList( Class graph_class) {
    	
    	Class interface_class = getGraphInterface( graph_class);
    	
        return specializedObjectManagers.get( interface_class);
    }
    

    /**
     * Give access to the Object manager in charge of the given object
     * 
     * @return the Object manager in charge of the given object, null if no Manager is defined for this object
     */
    public GsGraphAssociatedObjectManager getObjectManager( Object key) {
    	
    	if (objectManagers == null) {
    		return null;
    	}
        for (int i=0 ; i < objectManagers.size() ; i++) {
        	GsGraphAssociatedObjectManager manager = (GsGraphAssociatedObjectManager) objectManagers.get(i);
        	if (manager.getObjectName().equals( key)) {
        		return manager;
        	}
        }
        return null;
    }
    
    
    /**
     * Give access to the Object manager in charge of the given object
     * 
     * @return the Object manager in charge of the given object, null if no Manager is defined for this object
     */
    public GsGraphAssociatedObjectManager getObjectManager( Class graph_class, Object key) {
    	
    	Class interface_class = getGraphInterface( graph_class);
    	
    	List<GsGraphAssociatedObjectManager> specialized_managers =  specializedObjectManagers.get( interface_class);
    	
    	if (specialized_managers == null) {
    		return null;
    	}
    	
        for (int i=0 ; i < specialized_managers.size() ; i++) {
        	GsGraphAssociatedObjectManager manager = (GsGraphAssociatedObjectManager) specialized_managers.get(i);
        	if (manager.getObjectName().equals( key)) {
        		return manager;
        	}
        }
        return null;
    }
    
    
    /**
     * Allow to associate objects with a graph to retrieve them later.
     * this (and <code>addObject(key, obj)</code>) makes it easy.
     *
     * @see #addObject(Object, Object)
     * @param key
     * @param create if true, a non-defined object will be created
     * @return the associated object
     */
    public Object getObject( Graph graph, Object key, boolean create) {
    	
    	Map<Object, Object> m_objects = objectsOfGraph.get( graph);
    	
        if (m_objects == null) {
        	if ( create) {
        		m_objects = new HashMap<Object,Object>();
        	} else {
        		return null;
        	}
        }
        Object ret = m_objects.get( key);
        if (create && ret == null) {
        	GsGraphAssociatedObjectManager manager = getObjectManager( key);
        	if (manager == null) {
        		manager = getObjectManager( graph.getClass(), key);
        	}
        	if (manager != null) {
        		ret = manager.doCreate( graph);
        		addObject(graph, key, ret);
        	}
        }
        return ret;
    }

    /**
     * Allow to associate objects with a graph to retrieve them later.
     *
     * @see #getObject(Object)
     * @see #removeObject(Object)
     * @param key
     * @param obj
     */
    public void addObject(Graph graph, Object key, Object obj) {
    	
    	Map<Object, Object> m_objects = objectsOfGraph.get( graph);
    	
    	if (m_objects == null) {
            m_objects = new HashMap<Object,Object>();
            objectsOfGraph.put( graph, m_objects);
        }
        m_objects.put(key, obj);
    }

    /**
     * remove an object previously associated to a graph with <code>addObject(Object, Object)</code>.
     *
     * @see #getObject(Object)
     * @see #addObject(Object, Object)
     * @param key
     */
    public void removeObject(Graph graph, Object key) {
    	
    	Map<Object, Object> m_objects = objectsOfGraph.get( graph);
    	
        if (m_objects != null) {
        	m_objects.remove(key);
        }
        
    }
    
    /**
     * Remove all references from associated objects
     * 
     * @param graph
     */
    public void removeAllObjects( Graph graph){
    	
    	Map<Object, Object> m_objects = objectsOfGraph.get( graph);
    	
        if (m_objects != null) {
            for( Object key : m_objects.keySet()){
            	Object obj = m_objects.get( key);
            	obj = null;
            }
            m_objects.clear();
            m_objects = null;
        }
    }
    
    /**
     * 
     * @param key
     * @return true if a manager with this key already exists
     */
    public boolean isObjectManagerRegistred( Class graph_class, String key) {
    	
    	Class interface_class = getGraphInterface( graph_class);
    	
    	List<GsGraphAssociatedObjectManager> specialized_managers =  specializedObjectManagers.get( interface_class);
    	
        if (specialized_managers == null) {
            return false;
        }
        for (int i=0 ; i<specialized_managers.size() ; i++) {
            if (((GsGraphAssociatedObjectManager)specialized_managers.get(i)).getObjectName().equals(key)) {
                return true;
            }
        }
        return false;
    }
   	
	/**
	 * Return the deeper interface that inherits from Graph interface
	 * 
	 * @param classe
	 * @return
	 */
    private Class getGraphInterface( Class classe) {

        Class[] interfaces = classe.getInterfaces();
        if( interfaces.length != 0){
	        for (int i = 0; i < interfaces.length; i++) {
	            List<Class> all_interfaces = new ArrayList<Class>();
	            all_interfaces.add( interfaces[i]);
	            all_interfaces.addAll( getSuperInterfaces( interfaces[i]));
	            if( all_interfaces.contains( Graph.class)){
	            	return interfaces[i];
	            }
	        }
        }

    	Class super_class =  classe.getSuperclass();
    	if( super_class != null){
    		return getGraphInterface( super_class);
    	}


        return null;
    }
    
    /**
     * Return the list of all the interfaces (recursively) the given class implements
     * 
     * @param classe
     * @return
     */
    private List<Class> getSuperInterfaces( Class classe) {

        List<Class> allInterfaces = new ArrayList<Class>();

        Class[] interfaces = classe.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            allInterfaces.add( interfaces[i]);
            allInterfaces.addAll( getSuperInterfaces( interfaces[i]));
        }

        return allInterfaces;
    }
}