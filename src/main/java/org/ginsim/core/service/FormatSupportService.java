package org.ginsim.core.service;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.io.LogicalModelFormat;
import org.colomoto.biolqm.io.StreamProvider;
import org.colomoto.biolqm.modifier.booleanize.BooleanizeModifier;
import org.colomoto.biolqm.service.LQMServiceManager;
import org.colomoto.biolqm.service.MultivaluedSupport;
import org.ginsim.core.graph.regulatorygraph.LogicalModel2RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Generic service to support format implemented in LogicalModel.
 * This provides an easy way to use the core capabilities provides by LogicalModel formats.
 * A static method allows to add automatic wrappers for all available formats.
 * @param <F>  LogicalModelFormat
 * @author Aurelien Naldi
 */
public class FormatSupportService<F extends LogicalModelFormat> implements Service {

	private static final Set<LogicalModelFormat> knownFormats = new HashSet<LogicalModelFormat>();

	/**
	 * register a format when creating an instance of this class.
	 * 
	 * @param f the LogicalModelFormat
	 */
	private static void registerFormat(LogicalModelFormat f) {
		knownFormats.add(f);
	}

	/**
	 * Blacklist a format to prevent the creation of an automatic wrapper for it.
	 * 
	 * @param f the LogicalModelFormat
	 */
	public static void blacklistFormat(LogicalModelFormat f) {
		knownFormats.add(f);
	}

	/**
	 * Trigger the creation of automatic wrappers for all available formats.
	 * Formats that have a custom service or have been blacklisted will be skipped.
	 */
	public static void addMissingFormats() {
		for (LogicalModelFormat format: LQMServiceManager.getFormats()) {
			if (!knownFormats.contains(format)) {
				System.out.println("Should create wrapper format for: "+format);
			}
		}
	}

	/**
	 * final F format;
	 */
	public final F format;

	/**
	 * Constructor
	 * @param format the F format
	 */
	public FormatSupportService(F format) {
		this.format = format;
		registerFormat(format);
	}

	/**
	 * Export as string
	 * @param graph the RegulatoryGraph graph
	 * @param filename the string filename
	 * @return the String
	 * @throws Exception the exception
	 */
	public String export(RegulatoryGraph graph, String filename) throws Exception {
		return export(graph.getModel(), filename);
	}

	/**
	 * Export as string
	 * @param model the LogicalModel
	 * @param filename the string filename
	 * @return a string
	 * @throws Exception the exception
	 */
	public String export(LogicalModel model, String filename) throws Exception {
		return export(model, StreamProvider.create(filename));
	}

	/**
	 * Export as string
	 * @param model the LogicalModel model
	 * @param out the StreamProvider
	 * @return a String
	 * @throws Exception the exception
	 */
	public String export(LogicalModel model, StreamProvider out) throws Exception {
		String message = null;
		if (!model.isBoolean() && format.getMultivaluedSupport() == MultivaluedSupport.BOOLEANIZED) {
			model = new BooleanizeModifier(model).call();
			message = "Multivalued model was converted to Boolean";
		}
		format.export(model, out);
		return message;
	}

	/**
	 * Load file
	 * @param f File
	 * @return the LogicalModel
	 * @throws Exception the exception
	 */
	public LogicalModel importFile(File f) throws Exception {
		return format.load( f);
	}

	/**
	 * Load File
	 * @param filename the string filename
	 * @return the LogicalModel
	 * @throws Exception the exception
	 */
	public LogicalModel importFile(String filename) throws Exception {
		return importFile( new File(filename));
	}

	/**
	 * load File
	 * @param filename the string filename
	 * @return the RegulatoryGraph graph
	 * @throws Exception the exception
	 */
	public RegulatoryGraph importLRG(String filename) throws Exception {
		LogicalModel model = importFile(filename);
		return LogicalModel2RegulatoryGraph.importModel(model);
	}

	/**
	 * Test if can export
	 * @return boolean if can export
	 */
	public boolean canExport() {
		return format.canExport();
	}

	/**
	 * Test if can import
	 * @return boolean if can import
	 */
	public boolean canImport() {
		return format.canLoad();
	}

	/**
	 * test if can export model
	 * @param graph the RegulatoryGraph  graph
	 * @return boolean if can export model
	 */
	public boolean canExportModel(RegulatoryGraph graph) {
        if (!canExport()) {
            return false;
        }

		MultivaluedSupport mvs = format.getMultivaluedSupport();
        if (mvs == MultivaluedSupport.BOOLEAN_STRICT) {
            // check that the model is Boolean
            for (NodeInfo ni: graph.getNodeInfos()) {
                if (ni.getMax() > 1) {
                    return false;
                }
            }
        }

        return true;
    }

}
