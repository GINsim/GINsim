package fr.univmrs.tagc.GINsim.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * GINMLFilter.java filter the file to chose for saving/opening
 * 
 * 
 */
public class GsFileFilter extends FileFilter {

	private String[] extensionList = null;
	private String extdescr = null;

	/**
	 * @param list
	 * @param descr
	 */
	public void setExtensionList(String[] list, String descr) {
		this.extdescr = descr;
		this.extensionList = list;
	}


	/**
     *Accept all directories and files with the good extension.
	 * @param f
	 * @return true if the file is accepted
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = f.getName().substring(f.getName().lastIndexOf(".")+1,f.getName().length());
        
        if (extensionList == null) {
        		return true;
        }
        for (int i=0 ; i<extensionList.length ; i++) {
	        	if (extension.equalsIgnoreCase(extensionList[i])) {
	        		return true;
	        	}
        }
        return false;
    }

    public String getDescription() {
        return extdescr;
    }
}