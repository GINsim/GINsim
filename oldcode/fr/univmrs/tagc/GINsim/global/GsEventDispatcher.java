package fr.univmrs.tagc.GINsim.global;

import java.util.Vector;

import org.ginsim.graph.Graph;

import fr.univmrs.tagc.GINsim.graph.GraphChangeListener;
import fr.univmrs.tagc.GINsim.graph.GsGraphSelectionChangeEvent;
import fr.univmrs.tagc.GINsim.graph.GsNewGraphEvent;
import fr.univmrs.tagc.GINsim.gui.GsMainFrame;

/**
 * dispatch all main events:
 * - graph change
 * - selection change.
 *
 * it can be attached to a graph or to a frame, if attached to a frame it will just
 * use the graph's dispather as backend.
 */
public class GsEventDispatcher implements GraphChangeListener {

	/** listeners for "graphChanged" event */
	private Vector graphChangeListeners = new Vector();
    private GsEventDispatcher dispatcher = null;
    private boolean useDelagate = false;

    /**
     * create a new event dispatcher.
     * if <code>useDelegate</code> is true, it won't do anything by itself.
     *
     * @param useDelegate
     */
    public GsEventDispatcher(boolean useDelegate) {
        this.useDelagate = useDelegate;
    }

	/**
	 * add a new listener for graph events.
	 * the listener will receive events about new graph and selection change.
	 * @param gcl
	 */
	public void addGraphChangedListener(GraphChangeListener gcl) {
		graphChangeListeners.add(gcl);
	}
	/**
	 * remove a graphChangeListener.
	 * @param gcl
	 */
	public void removeGraphChangeListener(GraphChangeListener gcl) {
	    graphChangeListeners.remove(gcl);
	}
	/**
	 * annonce a new graph.
	 *
	 * @param source
	 * @param oldGraph the old graph
	 * @param newGraph
	 * @param association
	 */
	public void fireGraphChange(Object source, Graph oldGraph, Graph newGraph, boolean association) {
        if (useDelagate) {
            if (dispatcher != null) {
                dispatcher.removeGraphChangeListener(this);
                dispatcher = null;
            }
            if (newGraph != null) {
                dispatcher = newGraph.getGraphManager().getEventDispatcher();
                dispatcher.addGraphChangedListener(this);
            }
        }
        GsNewGraphEvent evt = new GsNewGraphEvent(source, oldGraph, newGraph, association);
        graphChanged(evt);
	}

	/**
	 * annonce a change in the set of selected objects.
	 * @param evt the NEW selection.
	 */
	public void fireGraphSelectionChanged(GsGraphSelectionChangeEvent evt) {
        if (useDelagate) {
            if (dispatcher != null) {
                dispatcher.fireGraphSelectionChanged(evt);
            }
        } else {
            graphSelectionChanged(evt);
        }
	}
    /**
     * @param graph the closed graph.
     */
    public void fireGraphClose( Graph graph) {
        if (useDelagate) {
            if (dispatcher != null) {
                dispatcher.fireGraphClose(graph);
                dispatcher.removeGraphChangeListener(this);
                dispatcher = null;
            }
        } else {
            graphClosed(graph);
        }
    }
    /**
     * @param graph
     * @param main
     */
    public static void associateGraphWithFrame( Graph graph, GsMainFrame main) {
        if (main != null) {
            main.getEventDispatcher().fireGraphChange(null, main.getGraph(), graph, false);
        }
    }

    public void graphChanged(GsNewGraphEvent event) {
        for (int i=graphChangeListeners.size()-1 ; i>=0 ; i--) {
            ((GraphChangeListener)graphChangeListeners.get(i)).graphChanged(event);
        }
    }
    public void graphSelectionChanged(GsGraphSelectionChangeEvent event) {
        for (int i=graphChangeListeners.size()-1 ; i>=0 ; i--) {
            ((GraphChangeListener)graphChangeListeners.get(i)).graphSelectionChanged(event);
        }
    }
    public void graphClosed( Graph graph) {
        for (int i=graphChangeListeners.size()-1 ; i>=0 ; i--) {
            ((GraphChangeListener)graphChangeListeners.get(i)).graphClosed(graph);
        }
    }

	public void updateGraphNotificationMessage( Graph graph) {
        for (int i=graphChangeListeners.size()-1 ; i>=0 ; i--) {
            ((GraphChangeListener)graphChangeListeners.get(i)).updateGraphNotificationMessage(graph);
        }
	}
}