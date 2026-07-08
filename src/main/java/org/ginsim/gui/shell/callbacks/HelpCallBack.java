package org.ginsim.gui.shell.callbacks;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.Txt;
import org.ginsim.common.utils.OpenUtils;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.shell.FileSelectionHelper;



/**
 * Here are the (few) callback for entry in the "help" menu
 *
 * @author Lionel Spinelli
 */
public class HelpCallBack {
	
	public static List<Action> getActions() {
		List<Action> actions = new ArrayList<Action>();
		actions.add(new HelpAction());
		actions.add(new AboutAction());
		
		return actions;
	}
	
	public static List<Action> getSupportActions(){
		
		List<Action> actions = new ArrayList<Action>();
		actions.add( new ProvideLogAction());
		actions.add( new ToggleTraceAction());
		
		return actions;
	}
}

class AboutAction extends AbstractAction {
	public AboutAction() {
		super( Txt.t("STR_About"));
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		GUIManager.getInstance().about();
	}
}

class HelpAction extends AbstractAction {
	public HelpAction() {
		super("Help");
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		OpenUtils.openURI("https://ginsim.github.io/documentation");
	}
}

/**
 * This Action concerns the export of log files into zip file that could be easily sent to support
 * 
 * @author Lionel Spinelli
 *
 */
class ProvideLogAction extends AbstractAction {
	
	public ProvideLogAction() {
		super(Txt.t("STR_Help_ExportLogs"));
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		Path log_zip_path = LogManager.deliverLogs();
		if( log_zip_path != null){
			File zip_file = log_zip_path.toFile();
			String save_path = FileSelectionHelper.selectSaveFilename( null, new String[]{"zip"}, "Zip files");
			if( save_path != null){
				try {
					Files.move(log_zip_path, new File(save_path).toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					GUIMessageUtils.openErrorDialog( "Unable to save the log ZIP file to selected path.");
				}
			}
		}
		else{
			GUIMessageUtils.openErrorDialog( "Unable to build the log ZIP file. See logs for details.");
		}
	}
}

/**
 * This action permit to activate/unactivate the trace/info log levels
 * 
 * @author Lionel Spinelli
 *
 */
class ToggleTraceAction extends AbstractAction{
	
	public ToggleTraceAction() {
		
		super( LogManager.getVerboseLevel() ==0? Txt.t("STR_Help_EnableTraces"): Txt.t("STR_Help_DisableTraces"));
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		if( LogManager.getVerboseLevel() == 0){
			LogManager.setVerbose( 2);
			putValue( Action.NAME, Txt.t("STR_Help_DisableTraces"));
		}
		else{
			LogManager.setVerbose( 0);
			putValue( Action.NAME, Txt.t("STR_Help_EnableTraces"));
		}
	}
}

