package org.ginsim.core.graph.objectassociation;

import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.Graph;

/**
 * Implement this interface to save/open objects associated to graph automatically.
 * @param <T>  manager
 * @author Lionel Spinelli
 */
public interface GraphAssociatedObjectManager<T> {

    /**
     * Getter  object name
     * @return the name of the object (for the zip entry)
     */
    String getObjectName();

    /**
     * Get the aliases under which the object used to be saved (zip name in old files)
     * 
     * @return the aliases or null
     */
    String[] getAliases();

    /**
     * Test if saving needed
     * @param graph the graph
     * @return true if this graph has a relevant associated object
     */
    boolean needSaving( Graph graph);

    /**
     * save the object associated with this graph.
     * 
     * @param out OutputStreamWriter object
     * @param graph the graph
     * @throws GsException graph exception
     */
    void doSave(OutputStreamWriter out, Graph graph) throws GsException;

    /**
     * open associated object.
     * 
     * @param is InputStream object
     * @param graph the graph
     * @return T object
     * @throws GsException  graph exception
     */
    T doOpen(InputStream is, Graph graph) throws GsException;

    /**
     * create the associated object for a graph
     * @param graph the graph
     * @return  T object
     */
	T doCreate( Graph graph);
	
	/**
	 * get the existing associated object for a graph
	 * 
	 * @param graph the graph
	 * @return the existing object, or null if not created
	 */
	T getObject( Graph graph);

    /**
     * Test if the key is a valid name for this data manager
     * @param key the string key value
     *
     * @return true if this manager believes it can handle the key
     */
    boolean handles(String key);

    /**
     * Get the type of graph handled by this data manager.
     *
     * @return the class of supported graph
     */
    Class getGraphType();
}
