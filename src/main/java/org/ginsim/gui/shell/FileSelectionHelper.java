package org.ginsim.gui.shell;

import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.ginsim.common.application.OptionStore;
import org.ginsim.commongui.utils.FileFormatFilter;


public class FileSelectionHelper {
	
	private static final String DIRKEY = "gui.last_working_directory";
	private static String lastDirectory = System.getProperty("user.dir");
	
	static {
		lastDirectory = OptionStore.getOption(DIRKEY, lastDirectory);
	}
	
	public static String selectSaveFilename( Frame parent) {
		return selectSaveFilename(parent, (GsFileFilter) null);
	}
	
	public static String selectSaveFilename( Frame parent, String[] extensions, String description){
		
		GsFileFilter file_filter = new GsFileFilter(extensions, description);
		return selectSaveFilename( parent, file_filter);
	}

	public static void setLastDirectory( String dir)
	{		
		lastDirectory = dir;
		OptionStore.setOption(DIRKEY, dir);
	}
	
	public static String selectSaveFilename( Frame parent, FileFilter file_filter) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory( new File(lastDirectory));
		if( file_filter != null){
			chooser.setFileFilter( file_filter);
		}
		chooser.showSaveDialog(parent);
		// Coose the file
		File f = chooser.getSelectedFile();
		if (f == null) {
			return null;
		}
		
		// change the remembered directory
		lastDirectory = chooser.getCurrentDirectory().getAbsolutePath();
		setLastDirectory(lastDirectory);
		
		// List the available extensions in the provided File Filter
		String[] extensions;
		if( file_filter instanceof GsFileFilter){
			extensions = ((GsFileFilter) file_filter).getExtensionList();
		}
		else if( file_filter instanceof FileFormatFilter){
			extensions =  ((FileFormatFilter) file_filter).getExtensionList();
		}
		else{
			extensions = null;
		}
		// If some extensions are provided, detect if the file has one of them and
		// add the first extension if it is not the case
		if( extensions != null && extensions.length > 0){
			boolean authorized = false;
			int dot_index = f.getName().lastIndexOf( ".");
			if( dot_index >= 0 && dot_index < f.getName().length()-1){
				String file_ext = f.getName().substring( dot_index + 1);
				for( String extension : extensions){
					if( extension != null && extension.equals( file_ext)){
						authorized = true;
						break;
					}
				}
			}
			if( !authorized){
				f = new File( f.getParent(), f.getName() + "." + extensions[0]);
			}
		}

		if (f.exists()) {
			int confirm = JOptionPane.showConfirmDialog(parent, "The file exists, do you want to overwrite it?");
			if (confirm != JOptionPane.OK_OPTION) {
				return null;

			}
		}
		
		return f.getAbsolutePath();
	}
	
	public static String selectOpenFilename( Frame parent) {
		return selectOpenFilename(parent, (GsFileFilter) null);
	}
	
	public static String selectOpenFilename( Frame parent, String[] extensions, String description){
		
		GsFileFilter file_filter = new GsFileFilter( extensions, description);
		return selectOpenFilename( parent, file_filter);
	}
	
	public static String selectOpenFilename( Frame parent, FileFilter file_filter) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory( new File(lastDirectory));
		if( file_filter != null){
			chooser.setFileFilter( file_filter);
		}
		chooser.showOpenDialog(parent);
		File f = chooser.getSelectedFile();
		if (f == null) {
			return null;
		}
		
		// change the remembered directory
		lastDirectory = chooser.getCurrentDirectory().getAbsolutePath();
		OptionStore.setOption(DIRKEY, lastDirectory);
		
		return f.getAbsolutePath();
	}
}
