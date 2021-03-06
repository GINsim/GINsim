package org.ginsim.commongui.utils;

import java.io.File;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import org.ginsim.common.utils.FileFormatDescription;

/**
 * FileFilter using a (List of) FileFormatDescription.
 * 
 * @author Aurelien Naldi
 */
public class FileFormatFilter extends FileFilter {

	private final FileFormatDescription format;
	private final List<FileFormatDescription> formats;
	
	public FileFormatFilter(FileFormatDescription format) {
		this.format = format;
		this.formats = null;
	}
	
	public FileFormatFilter(List<FileFormatDescription> formats) {
		this.formats = formats;
		this.format = null;
	}
	
	public String[] getExtensionList(){
		
		String[] result;
		if( formats != null){
			result = new String[ formats.size()];
			int count = 0;
			for( FileFormatDescription format : formats){
				result[ count] = format.extension;
				count++;
			}
		}
		else if( format != null){
			result = new String[1];
			result[0] = format.extension;
		}
		else{
			result = null;
		}
		
		return result;
	}
	
	@Override
	public boolean accept(File pathname) {
		if (pathname == null) {
			return false;
		}
		
		if (pathname.isDirectory()) {
			return pathname.canRead();
		}
		
		String extension = pathname.getAbsolutePath();
		int pos = extension.lastIndexOf('.');
		if (pos < 0) {
			return false;
		}
		extension = extension.substring(pos+1);
		
		if (formats != null) {
			for (FileFormatDescription f: formats) {
				if (f.extension.equals(extension)) {
					return true;
				}
			}
		}

		if (format != null) {
			return format.extension.equals(extension);
		}
		
		return false;
	}

	@Override
	public String getDescription() {
		// TODO: look for translation based on the ID
		return format.id + " (."+format.extension+")";
	}
}
