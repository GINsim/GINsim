package org.ginsim.gui;

import java.awt.Frame;
import java.util.Map;

import org.ginsim.graph.common.Graph;

import fr.univmrs.tagc.GINsim.gui.GsFileFilter;
import fr.univmrs.tagc.common.Debugger;

public class FileSelectionHelper {

	// TODO: remember previous path
	// and store it in the option file
	
	public static Graph<?,?> open(String path) {
		Debugger.log("TODO: open files");
		return null;
	}
	public static Graph<?,?> open(String path, Map filter) {
		Debugger.log("TODO: open filtered files");
		return null;
	}
	
	
	public static String selectSaveFilename( Frame parent) {
		return selectSaveFilename(parent, null);
	}
	public static String selectSaveFilename( Frame parent, String extension) {
		Debugger.log("TODO: save filechooser");
		return null;
	}
	
	public static String selectOpenFilename( Frame parent) {
		return selectOpenFilename(parent, null);
	}
	public static String selectOpenFilename( Frame parent, String extension) {
		Debugger.log("TODO: open filechooser");
		return null;
	}
}