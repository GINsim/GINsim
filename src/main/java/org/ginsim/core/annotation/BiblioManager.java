package org.ginsim.core.annotation;

import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import org.ginsim.common.application.GsException;
import org.ginsim.common.xml.XMLHelper;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.objectassociation.BasicGraphAssociatedManager;
import org.ginsim.core.graph.objectassociation.GraphAssociatedObjectManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.kohsuke.MetaInfServices;
import org.xml.sax.Attributes;

/**
 * Associated data manager to save and restore bibliographies.
 * 
 * @author Aurelien Naldi
 */
@MetaInfServices(GraphAssociatedObjectManager.class)
public class BiblioManager extends BasicGraphAssociatedManager<BiblioList> {

	/**
	 * static final String KEY = "biblio"
	 */
	public static final String KEY = "biblio";

	/**
	 * Constructor
	 */
	public BiblioManager() {
		super(KEY, null, RegulatoryGraph.class);
		AnnotationLink.addHelperClass("ref", KEY);
	}

	@Override
	public BiblioList doCreate( Graph graph) {
		return new BiblioList( graph, false);
	}

	@Override
	public BiblioList doOpen(InputStream is, Graph graph) throws GsException{
		BiblioList bibList = (BiblioList) getObject(graph);
		BiblioParser parser = new BiblioParser(bibList);
		parser.startParsing(is, false);
		return bibList;
	}
}

class BiblioParser extends XMLHelper {

	BiblioList bibList;
	
	static final int FILE = 0;
	static final int REF = 1;
	static final int LINK = 2;

	static Map CALLMAP = new TreeMap();
	static {
		addCall("file", FILE, CALLMAP, STARTONLY, false);
		addCall("ref", REF, CALLMAP, STARTONLY, true);
		addCall("link", LINK, CALLMAP, STARTONLY, true);
	}
	
    public BiblioParser(BiblioList bibList) {
    	this.bibList = bibList;
    	this.m_call = CALLMAP;
    }
    
	protected void startElement(int id, Attributes attributes) {
		switch (id) {
			case FILE:
				bibList.addFile(attributes.getValue("filename"));
				break;
			case REF:
				bibList.addRef(attributes.getValue("key"));
				break;
			case LINK:
				bibList.addLinkToCurRef(attributes.getValue("key"), attributes.getValue("value"));
				break;
		}
	}
}
