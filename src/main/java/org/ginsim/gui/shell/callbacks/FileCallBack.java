package org.ginsim.gui.shell.callbacks;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.OptionStore;
import org.ginsim.common.application.Translator;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.GraphManager;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.shell.FileSelectionHelper;
import org.ginsim.gui.shell.FrameActionManager;
import org.ginsim.gui.shell.GsFileFilter;
import org.ginsim.gui.utils.widgets.Frame;


/**
 * Callbacks for the "File" menu
 */
public class FileCallBack {
	
	/**
	 * Create and populate a File menu.
	 * 
	 * @param g
	 * @param importMenu
	 * @param exportMenu
	 * 
	 * @return a full file menu
	 */
	public static JMenu getFileMenu(Graph<?, ?> g, JMenu importMenu, JMenu exportMenu) {
		JMenu menu = new JMenu( Translator.getString( "STR_File"));
		
		menu.add(new NewAction());
		menu.add(new OpenAction());
		JMenu recent = new RecentMenu();
		menu.add(recent);
		menu.add(importMenu);

		menu.add(new JSeparator());
		
		menu.add(new SaveAction(g));
		menu.add(new SaveAsAction(g));
		menu.add(exportMenu);

		menu.add(new JSeparator());
		
		menu.add(new CloseAction(g));
		menu.add(new QuitAction());
		
		return menu;
	}
	
	public static Action getActionNew() {
		return new NewAction();
	}
	public static Action getActionOpen() {
		return new OpenAction();
	}
	public static List<Action> getActionsRecent() {
		List<Action> actions = new ArrayList<Action>();
		for (String recent: OptionStore.getRecentFiles()) {
			actions.add( new OpenAction(recent));
		}
		return actions;
	}
}
/**
 * Recent menu: update its content when opened.
 * 
 * @author Aurelien Naldi
 */
class RecentMenu extends JMenu {

	public RecentMenu() {
		super(Translator.getString( "STR_RecentFiles"));
	}
	
	@Override
	public void setSelected(boolean b) {

		if (b) {
			// rebuild the recent menu
			removeAll();
			for (String recent: OptionStore.getRecentFiles()) {
				add( new OpenAction(recent));
			}
		}
		super.setSelected(b);
	}
}


class NewAction extends AbstractAction {
	public NewAction() {
		super( Translator.getString( "STR_New"));
		putValue( SHORT_DESCRIPTION, Translator.getString( "STR_New_descr"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, FrameActionManager.MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			GUIManager.getInstance().newFrame();
		} catch (Exception e) {
			LogManager.error(" Error creating the new frame");
			LogManager.error( e);
		}
	}
}

class OpenAction extends AbstractAction {
	
	private final String filename;
	
	public OpenAction() {
		super( Translator.getString(  "STR_Open"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, FrameActionManager.MASK));
		this.filename = null;
	}
	
	public OpenAction(String filename) {
		super("Open");
		this.filename = filename;
		if (filename != null) {
			int idx = filename.lastIndexOf(File.separatorChar);
			this.putValue(Action.NAME, filename.substring(idx+1));
			this.putValue(Action.LONG_DESCRIPTION, "Open "+filename);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String path = filename;
		if (path == null) {
			GsFileFilter ffilter = new GsFileFilter();
			ffilter.setExtensionList(new String[] { "ginml", "zginml" }, "GINsim files");
			path = FileSelectionHelper.selectOpenFilename( null, ffilter);
		}
		if (path != null) {
			try {
				Graph g = GraphManager.getInstance().open(path);
				if( g != null){
					OptionStore.addRecentFile(path);
					GUIManager.getInstance().newFrame( g);
					GraphGUI graph_gui = GUIManager.getInstance().getGraphGUI( g);
					if( graph_gui != null){
						graph_gui.setSaved( true);
					}
					GUIManager.getInstance().closeEmptyGraphs();
				}
				else{
					GUIMessageUtils.openErrorDialog( "STR_unableToOpen_SeeLogs");
				}
				
			} catch (GsException e) {
				GUIMessageUtils.openErrorDialog( "STR_unableToOpen_SeeLogs");
			}
		}
	}
}

class SaveAction extends AbstractAction {
	
	private final Graph<?,?> g;
	
	public SaveAction(Graph<?,?> g) {
		super( Translator.getString( "STR_Save"));
		putValue( SHORT_DESCRIPTION,  Translator.getString( "STR_Save_descr"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, FrameActionManager.MASK));
		this.g = g;
	}
	
	public boolean save() {
		return GUIManager.getInstance().getGraphGUI(g).save();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		save();
	}
}


class SaveAsAction extends AbstractAction {
	
	private final Graph<?,?> g;
	
	public SaveAsAction(Graph<?,?> g) {
		super( Translator.getString( "STR_SaveAs"));
		putValue( SHORT_DESCRIPTION,  Translator.getString( "STR_SaveAs_descr"));
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, FrameActionManager.MASK));
		this.g = g;
	}
	
	public boolean saveAs() {
		return GUIManager.getInstance().getGraphGUI(g).saveAs();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		saveAs();
	}
}

class CloseAction extends AbstractAction {
	
	private final Graph<?,?> g;
	
	public CloseAction(Graph<?,?> g) {
		super( Translator.getString( "STR_Close"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, FrameActionManager.MASK));
		this.g = g;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		GUIManager.getInstance().close(g);
	}
}

class QuitAction extends AbstractAction {
	
	public QuitAction() {
		super("Quit");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, FrameActionManager.MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		GUIManager.getInstance().quit();
	}
}