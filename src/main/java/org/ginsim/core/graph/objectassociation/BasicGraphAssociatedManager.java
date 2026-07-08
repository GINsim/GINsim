package org.ginsim.core.graph.objectassociation;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.ginsim.common.application.GsException;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.common.xml.XMLize;
import org.ginsim.core.graph.Graph;


/**
 * Base class to create managers for associated objects.
 * @param <T>  manager
 * @author Lionel Spinelli
 * @author Aurelien Naldi
 */
public abstract class BasicGraphAssociatedManager<T> implements GraphAssociatedObjectManager<T> {

	private final String key;
	private final String[] aliases;
    private final Class typeClass;

	/**
	 * Constructor
	 * @param key the key string
	 * @param aliases string array for alias
	 * @param typeClass the class
	 */
	public BasicGraphAssociatedManager(String key, String[] aliases, Class typeClass) {
		this.key = key;
		this.aliases = aliases;
        this.typeClass = typeClass;
	}

	/**
	 * Save function
	 * @param os OutputStreamWriter
	 * @param graph the graph
	 * @throws GsException the exception
	 */
	public void doSave(OutputStreamWriter os, Graph graph) throws GsException{
		
        Object o =  getObject(graph);
        if (o != null && o instanceof XMLize) {
        	try{
        		XMLWriter out = new XMLWriter(os);
        		((XMLize)o).toXML(out);
        	}
        	catch( IOException ioe){
        		throw new GsException( GsException.GRAVITY_ERROR, ioe);
        	}
		} 
	}

	/**
	 * Name getter
	 * @return string name
	 */
	public String getObjectName() {
		return key;
	}

	/**
	 * Aliases getter
	 * @return string array  for alias
	 */
	public String[] getAliases() {
		return aliases;
	}

	/**
	 * Test to be saved
	 * @param graph the graph
	 * @return boolean if need to be saved
	 */
	public boolean needSaving( Graph graph) {
		return getObject(graph) != null;
	}

	/**
	 * Object getter
	 * @param graph the graph
	 * @return T ObjectAssociationManager.
	 */
	public T getObject( Graph graph) {
		return (T)ObjectAssociationManager.getInstance().getObject(graph, key, false);
	}

    @Override
    public boolean handles(String key) {
        if (key == null) {
            return false;
        }

        if (key.equals(this.key)) {
            return true;
        }

        if (aliases != null) {
            for (String alias: aliases) {
                if (alias.equals(key)) {
                    return true;
                }
            }
        }

        return false;
    }

	/**
	 * Class type getter
	 * @return class type
	 */
	public Class getGraphType() {
        return typeClass;
    }
}
