package org.ginsim.gui.shell.callbacks;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.OptionStore;
import org.ginsim.common.application.Txt;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.shell.FileSelectionHelper;
import org.ginsim.gui.shell.FrameActionManager;
import org.ginsim.gui.shell.GsFileFilter;


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
		JMenu menu = new JMenu( Txt.t("STR_File"));
		
		menu.add(new NewAction());
		menu.add(new OpenAction());
		JMenu recent = new RecentMenu();
		menu.add(recent);
		menu.add(importMenu);
		if (g instanceof RegulatoryGraph) {
			importMenu.add(new JSeparator());
			importMenu.add(new ImportJSONAction(g));
		}

		menu.add(new JSeparator());
		
		menu.add(new SaveAction(g));
		menu.add(new SaveAsAction(g));
		menu.add(exportMenu);
		if (g instanceof RegulatoryGraph) {
			exportMenu.add(new JSeparator());
			exportMenu.add(new ExportJSONAction(g));
		}

        menu.add(new JSeparator());
        menu.add(new CloseAction(g));

		return menu;
	}

    public static JMenu getMainMenu() {
        JMenu menu = new JMenu("GINsim");

        for (Action a: HelpCallBack.getActions()) {
            menu.add(a);
        }
        JMenu support_menu = new JMenu( Txt.t("STR_Help_Support"));
        menu.add( support_menu);
        for (Action a: HelpCallBack.getSupportActions()) {
            support_menu.add(a);
        }
        menu.add(support_menu);

        menu.add(new JSeparator());
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
		super(Txt.t("STR_RecentFiles"));
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
		super( Txt.t("STR_New"));
		putValue( SHORT_DESCRIPTION, Txt.t("STR_New_descr"));
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
		super( Txt.t("STR_Open"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, FrameActionManager.MASK));
		this.filename = null;
	}
	
	public OpenAction(String filename) {
		super("Open");
		this.filename = filename;
		if (filename != null) {
			int idx = filename.lastIndexOf(File.separatorChar);
			String shortName = filename.substring(idx+1);
			if (shortName.length() > 35) {
				shortName = shortName.substring(0, 12) + '\u2026' + shortName.substring(shortName.length()-20);
			}
			this.putValue(Action.NAME, shortName);
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
				// Crée un objet File avec le nom du fichier
				File file = new File(path);
        
				// Vérifie si le fichier existe
				if (file.exists()) {
					// Récupère le répertoire parent du fichier
					String directory = file.getParent();
					FileSelectionHelper.setLastDirectory(directory);
				}
				else {
					// @TODO traiter le cas ou le fichier n'existe pas
				}
			}
			catch (Exception e) {
				GUIMessageUtils.openErrorDialog( "STR_unableToOpen_SeeLogs");
			}

			try {
				Graph g = GSGraphManager.getInstance().open(path);
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
		super( Txt.t("STR_Save"));
		putValue( SHORT_DESCRIPTION,  Txt.t("STR_Save_descr"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, FrameActionManager.MASK));
		this.g = g;
	}
	
	public boolean save() {
		return GUIManager.getInstance().getGraphGUI(g).save();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		save();
		NotificationManager.publishInformation(g, Txt.t("STR_Saved_msg"));
	}
}


class SaveAsAction extends AbstractAction {
	
	private final Graph<?,?> g;
	
	public SaveAsAction(Graph<?,?> g) {
		super( Txt.t("STR_SaveAs"));
		putValue( SHORT_DESCRIPTION,  Txt.t("STR_SaveAs_descr"));
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
		super( Txt.t("STR_Close"));
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

class ImportJSONAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final RegulatoryGraph g;
	
	public ImportJSONAction(Graph<?,?> g) {
		super(Txt.t("STR_ImportAnnotations"));
		this.g = (RegulatoryGraph) g;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
		    JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		    FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "JSON files", "json");
		    chooser.setFileFilter(filter);
		    int returnVal = chooser.showOpenDialog(null);
		    if (returnVal == JFileChooser.APPROVE_OPTION) {
		    	System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());

				this.g.getAnnotator().importMetadata(chooser.getSelectedFile().getAbsolutePath(), this.g.getNodeInfos());
		    }
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

class ExportJSONAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final RegulatoryGraph g;
	
	public ExportJSONAction(Graph<?,?> g) {
		super(Txt.t("STR_ExportAnnotations"));
		this.g = (RegulatoryGraph) g;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
		    JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		    FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "JSON files", "json");
		    chooser.setFileFilter(filter);
		    int returnVal = chooser.showSaveDialog(null);
		    if (returnVal == JFileChooser.APPROVE_OPTION) {
		    	String nameFile = chooser.getSelectedFile().getName();
		    	
		    	if (!nameFile.substring(nameFile.length()-5).equals(".json")) {
		    		nameFile = nameFile + ".json";
		    	}
		    	
		    	File f = chooser.getSelectedFile();
		    	if (f.exists()) {
					int confirm = JOptionPane.showConfirmDialog(null, "The file exists, do you want to overwrite it?");
					if (confirm != JOptionPane.OK_OPTION) {
						return;
					}
				}
		    	
		    	System.out.println("You chose to save the annotations under the name: " + nameFile);
				Writer out = new OutputStreamWriter(Files.newOutputStream(Paths.get(nameFile)), StandardCharsets.UTF_8);
				out.write(this.g.getAnnotator().writeAnnotationsInJSON().toString());
				out.flush();
				out.close();

			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
