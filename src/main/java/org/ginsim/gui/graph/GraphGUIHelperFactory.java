package org.ginsim.gui.graph;

import java.util.HashMap;

import org.colomoto.biolqm.service.ExtensionLoader;
import org.ginsim.core.graph.Graph;

/**
 * This factory provides access to the GraphGUIHelper corresponding to a specific graph class
 * The factory itself is managed as a singleton.
 * The instances of GraphGUIHelper are managed through a map establishing the correspondence 
 * 	between graph class and GraphGUIHelper instance. Theses instances are generated using 
 *  the Service Provider interface implying each GraphGUIHelper extension have to declare 
 *  the annotation "@MetaInfServices(GraphGUIHelper)"
 * 
 * A user may access statically to the factory singleton through the getFactory method and then call the
 * 	getGraphGUIHelper method to obtain the helper instance
 * 
 * 
 * @author Lionel Spinelli
 */
public class GraphGUIHelperFactory {
	
	// The factory singleton
	private static GraphGUIHelperFactory factory = null;
	
	// The map establishing the correspondence between graph class and GraphGUIHelper instance
	private HashMap<Class<?>, GraphGUIHelper<?,?,?>> guiGraphHelpers = new HashMap<Class<?>, GraphGUIHelper<?,?,?>>();
	
	
	/**
	 * Factory creator. Instantiate the factory and ask the ServiceLoader to load the GraphGUIHelper list.
	 * 
	 */
	private GraphGUIHelperFactory(){
		
        for (GraphGUIHelper<?,?,?> helper: ExtensionLoader.load_instances(GraphGUIHelper.class)) {
        	Class<?> graph_class = helper.getGraphClass();
            if( graph_class != null) {
            	guiGraphHelpers.put( graph_class, helper);
            }
        }
	}
	
	
	/**
	 * Method providing access to the factory instance
	 * 
	 * @return the GraphGUIHelperFactory singleton 
	 */
	public static GraphGUIHelperFactory getFactory(){
		
		if( factory == null){
			factory = new GraphGUIHelperFactory();
		}
		
		return factory;
	}
	
	
	/**
	 * Give access to the GraphGUIHelper corresponding to the given graph class
	 * 
	 * @param graph_class the name of the graph class name for which the GUI helper is required
	 * @return the instance of GraphGUIHelper corresponding to the graph class
	 */
	public GraphGUIHelper<?,?,?> getGraphGUIHelper( Class<?> graph_class) throws ClassNotFoundException, IllegalAccessException, InstantiationException{
		
		GraphGUIHelper<?,?,?> helper = guiGraphHelpers.get( graph_class);
		if (helper != null) {
			return helper;
		}

		// no helper for the concrete class, lookup on interfaces
		Class<?>[] interfaces = graph_class.getInterfaces();
		for (Class<?> i: interfaces) {
			helper = guiGraphHelpers.get(i);
			if (helper != null) {
				return helper;
			}
		}
		throw new ClassNotFoundException( "GraphGUIhelperFactory.getGraphGUIHelper : No GraphGUIHelper found for graph name : " + graph_class);
	}
	
	
	/**
	 * Give access to the GraphGUIHelper corresponding to the given graph
	 * 
	 * @param graph the instance of the graph for which the GUI helper is required
	 * @return the instance of GraphGUIHelper corresponding to the graph class
	 */
	public GraphGUIHelper<?,?,?> getGraphGUIHelper( Graph graph) throws ClassNotFoundException, IllegalAccessException, InstantiationException{
		
		if( graph != null){
			return getGraphGUIHelper( graph.getClass());
		}
		else{
			throw new ClassNotFoundException( "GraphGUIhelperFactory.getGraphGUIHelper : the provided graph is null");
		}
	}
	
	
}
