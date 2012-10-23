package org.ginsim.core.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.TreeSet;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.io.LogicalModelFormat;
import org.colomoto.logicalmodel.services.ServiceManager;

/**
 * Generic service to support format implemented in LogicalModel.
 * This provides an easy way to use the core capabilities provides by LogicalModel formats.
 * A static method allows to add automatic wrappers for all available formats.
 * 
 * @author Aurelien Naldi
 */
public class FormatSupportService<F extends LogicalModelFormat> implements Service {

	private static final Set<LogicalModelFormat> knownFormats = new TreeSet<LogicalModelFormat>();

	/**
	 * register a format when creating an instance of this class.
	 * 
	 * @param f
	 */
	private static void registerFormat(LogicalModelFormat f) {
		knownFormats.add(f);
	}

	/**
	 * Blacklist a format to prevent the creation of an automatic wrapper for it.
	 * 
	 * @param f
	 */
	public static void blacklistFormat(LogicalModelFormat f) {
		knownFormats.add(f);
	}

	/**
	 * Trigger the creation of automatic wrappers for all available formats.
	 * Formats that have a custom service or have been blacklisted will be skipped.
	 */
	public static void addMissingFormats() {
		ServiceManager manager = ServiceManager.getManager();
		for (LogicalModelFormat format: manager.getFormats()) {
			if (!knownFormats.contains(format)) {
				System.out.println("Should create wrapper format for: "+format);
			}
		}
	}
	
	private final F format;
	
	public FormatSupportService(F format) {
		this.format = format;
		registerFormat(format);
	}
	
	public void export(LogicalModel model, String filename) throws IOException {
		OutputStream out = new FileOutputStream(filename);
		export(model, out);
	}
	
	public void export(LogicalModel model, OutputStream out) throws IOException {
		format.export(model, out);
	}
	
	public LogicalModel importFile(File f) throws IOException {
		return format.importFile(f);
	}
	
	public LogicalModel importFile(String filename) throws IOException {
		return format.importFile(new File(filename));
	}
	
	public boolean canExport() {
		return format.canExport();
	}
	
	public boolean canImport() {
		return format.canImport();
	}
}
