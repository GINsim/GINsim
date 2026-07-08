package org.ginsim.gui.graph;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.OptionStore;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.GraphViewListener;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.GraphChangeType;
import org.ginsim.core.graph.GraphEventCascade;
import org.ginsim.core.graph.GraphListener;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.shell.FileSelectionHelper;
import org.ginsim.gui.shell.FrameActionManager;
import org.ginsim.gui.shell.GsFileFilter;
import org.ginsim.gui.utils.widgets.Frame;

/**
 * Abstract class providing generic parts of a Graph GUI.
 * 
 * @author Aurelien Naldi
 *
 * @param <G> the graph
 * @param <V> the vertex
 * @param <E> the edge
 */
public abstract class BaseGraphGUI<G extends Graph<V,E>, V, E extends Edge<V>>
       implements GraphGUI<G, V, E>, GraphViewListener, GraphListener<G> {

	private final GraphGUIHelper<G,V,E> helper;
	private final EditActionManager editActionManager;
    private final List<GraphGUIListener<G, V, E>> listeners = new ArrayList<GraphGUIListener<G,V,E>>();

	/**
	 * final G graph
	 */
	protected final G graph;
	/**
	 * final GraphSelection
	 */
	protected final GraphSelection<V, E> selection = new GraphSelection<V, E>(this);
    private boolean isSaved = true;
    private boolean canBeSaved = true;

	/**
	 * Constructor
	 * @param g the graph G
	 * @param helper  GraphGUIHelpe helper object
	 * @param can_be_saved boolean
	 */
	public BaseGraphGUI(G g, GraphGUIHelper<G,V,E> helper, boolean can_be_saved) {
		
		this.graph = g;
		this.canBeSaved = can_be_saved;
		this.helper = helper;
		
		editActionManager = new EditActionManager(this, helper.getEditActions(graph));
		GSGraphManager.getInstance().addGraphListener(g, this);
		g.addViewListener(this);
	}

	@Override
	public Graph<V, E> getGraph() {
		return graph;
	}

	@Override
	public GUIEditor<G> getMainEditionPanel() {
		return helper.getMainEditionPanel(graph);
	}

	@Override
	public String getEditingTabLabel() {
		return helper.getEditingTabLabel(graph);
	}

	@Override
	public GUIEditor<V> getNodeEditionPanel() {
		return helper.getNodeEditionPanel(graph);
	}

	@Override
	public GUIEditor<E> getEdgeEditionPanel() {
		return helper.getEdgeEditionPanel( graph);
	}

	@Override
	public JPanel getInfoPanel() {
		return helper.getInfoPanel( graph);
	}

	@Override
	public EditActionManager getEditActionManager() {
		return editActionManager;
	}
	
	@Override
	public JMenu getViewMenu(JMenu layout) {
		JMenu menu = new JMenu("View");
		
		menu.add(layout);

		for (ZoomEffect effect: ZoomEffect.values()) {
			menu.add(getZoomAction(this, effect));
		}

		return menu;
	}

	@Override
	public boolean isSaved() {
		return isSaved;
	}

	@Override
	public boolean canBeSaved(){
		return canBeSaved;
	}

	@Override
	public void setSaved( boolean isSaved) {
		
		this.isSaved = isSaved;
		Frame main_frame = GUIManager.getInstance().getFrame( graph);
		if( main_frame != null){
			main_frame.setFrameTitle( graph, isSaved);
		}
	}
	
	@Override
	public boolean save() {
		
		if( ! canBeSaved){
			GUIMessageUtils.openErrorDialog( "STR_graphTypeCannotBeSaved");
			return false;
		}
		
		String savePath = GSGraphManager.getInstance().getGraphPath( graph);
		
		if (savePath == null) {
			isSaved = false;
			saveAs();
			return isSaved();
		}
		
		try {
			graph.save( savePath);
			graphChanged(graph, GraphChangeType.GRAPHSAVED, null);
			OptionStore.addRecentFile(savePath);
			isSaved = true;
			return true;
		} catch (Exception e) {
			GUIMessageUtils.openErrorDialog( "STR_unableToOpen_SeeLogs");
			LogManager.error( "Unable to save file : " + savePath);
			LogManager.error( e);
		}
		return false;
		
	}

	@Override
	public boolean saveAs() {
		
		if( ! canBeSaved){
			GUIMessageUtils.openErrorDialog( "STR_graphTypeCannotBeSaved");
			return false;
		}
		
		Frame frame = GUIManager.getInstance().getFrame(graph);
		GsFileFilter ffilter = new GsFileFilter();
		ffilter.setExtensionList(new String[] { "zginml" }, "GINsim files");
		String filename = FileSelectionHelper.selectSaveFilename(frame, ffilter);
		if (filename != null) {
			String graph_name = (new File( filename)).getName();
			int dot_index = graph_name.lastIndexOf( ".");
			if( dot_index > 0){
				graph_name = graph_name.substring( 0, dot_index);
			}
			try {
				graph.setGraphName( graph_name);
				GSGraphManager.getInstance().registerGraph( graph, filename);
				return save();
			} catch (GsException gse) {
				GUIMessageUtils.openWarningDialog( "Unable to set graph name: " + graph_name);
				LogManager.info( "Unable to set graph name: " + graph_name);
			}

		}
		
		return false;
	}

	@Override
	public void fireGraphClose() {
		for (GraphGUIListener<G, V, E> listener: listeners) {
			listener.graphGUIClosed(this);
		}
	}

	@Override
	public void addGraphGUIListener(GraphGUIListener<G, V, E> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeGraphGUIListener(GraphGUIListener<G, V, E> listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean isEditAllowed() {
		return GUIManager.getInstance().isEditAllowed( graph);
	}

	@Override
	public boolean canCopyPaste() {
		return helper.canCopyPaste(graph);
	}
	@Override
	public GraphSelection<V, E> getSelection() {
		return selection;
	}

	/**
	 * Selection update function
	 * @param nodes nodes list
	 * @param edges edges list
	 */
	protected void updateSelection(List<V> nodes, List<E> edges) {
		selection.backendSelectionUpdated(nodes, edges);
		fireSelectionChange();
	}

	/**
	 * Update function on change
	 */
	protected void fireSelectionChange() {
		for (GraphGUIListener<G, V, E> listener: listeners) {
			listener.graphSelectionChanged(this);
		}
	}

	
	@Override
	public GraphEventCascade graphChanged(G g, GraphChangeType type, Object data) {
		for (GraphGUIListener<G, V, E> listener: listeners) {
			listener.graphChanged(g, type, data);
		}
		return null;
	}

	/**
	 * Set the zoom effet
	 * @param effect zoom effect
	 */
	protected abstract void setZoomLevel(ZoomEffect effect);

	/**
	 * Getter for Action
	 * @param gui the qui
	 * @param effect the zoom effect
	 * @return a Action
	 */
	protected Action getZoomAction(BaseGraphGUI<?, ?, ?> gui, ZoomEffect effect) {
    	return new ZoomAction(gui, effect);
    }
}

class ZoomAction extends AbstractAction {
	private static final long serialVersionUID = 8027606268716590825L;
	
	private final BaseGraphGUI<?, ?, ?> gui;
	private final ZoomEffect effect;
	
	public ZoomAction(BaseGraphGUI<?, ?, ?> gui, ZoomEffect effect) {
		this.gui = gui;
		this.effect = effect;

		putValue(NAME, effect.name);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(effect.key, FrameActionManager.MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		gui.setZoomLevel(effect);
	}
}
