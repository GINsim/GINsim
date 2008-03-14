package fr.univmrs.tagc.GINsim.graph;

import java.io.InputStream;
import java.io.OutputStreamWriter;

/**
 * Implement this interface to save/open objects associated to graph automatically.
 */
public interface GsGraphAssociatedObjectManager {

    /**
     * @return the name of the object (for the zip entry)
     */
    public String getObjectName();
    /**
     * @param graph
     * @return true if this graph has a relevant associated object
     */
    public boolean needSaving(GsGraph graph);
    /**
     * save the object associated with this graph.
     * 
     * @param out
     * @param graph
     */
    public void doSave(OutputStreamWriter out, GsGraph graph);
    /**
     * open associated object.
     * 
     * @param is
     * @param graph
     */
    public Object doOpen(InputStream is, GsGraph graph);
    /**
     * create the associated object for a graph
     * @param graph
     */
	public Object doCreate(GsGraph graph);
}