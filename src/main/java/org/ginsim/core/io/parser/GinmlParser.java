package org.ginsim.core.io.parser;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.ginsim.common.application.GsException;
import org.ginsim.common.xml.ParsingWarningReport;
import org.ginsim.common.xml.XMLHelper;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.notification.NotificationManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Parser for GINML files.
 * This generic parser starts parsing and will handle the core of the file to a specialised parser,
 * according to the encountered graph type. 
 */
public final class GinmlParser extends XMLHelper {
    
    private Set map;
    private GsXMLHelper realParser = null;
    
    static {
    	String DTD = "/org/ginsim/dtd/GINML_2_1.dtd";
        XMLHelper.addEntity("http://gin.univ-mrs.fr/GINsim/GINML_2_0.dtd", DTD);
        XMLHelper.addEntity("http://gin.univ-mrs.fr/GINsim/GINML_2_1.dtd", DTD);
        XMLHelper.addEntity("GINML_2_0.dtd", DTD);
        XMLHelper.addEntity("GINML_2_1.dtd", DTD);

    	String NEWDTD = "/org/ginsim/dtd/GINML_2_2.dtd";
        XMLHelper.addEntity("https://ginsim.github.io/GINML_2_2.dtd", NEWDTD);
        XMLHelper.addEntity("GINML_2_2.dtd", NEWDTD);
    }
    
    /**
	 * parse from map
     * @param is from where to read
     * @param map Set to parse
     * @return the new graph
     */
    public Graph  parse(InputStream is, Set map)  throws GsException{
    	this.map = map;
		startParsing(is);
		if (realParser == null) {
		    return null;
		}
		Graph g = realParser.getGraph();
		
		// Notification for parsing errors.
		// Note: warnings are collected by the generic parser, not the real one
		ParsingWarningReport warnings = getWarnings();
		if (warnings != null) {
			NotificationManager.publishDetailedWarning(g, warnings.getMessage(), warnings.getDetail());
		}
		return g;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equals("graph")) {
        	try {
	            String s_class = attributes.getValue("class");
	            Class parser_class = GSGraphManager.getInstance().getParserClass( s_class);
	            if( parser_class != null){
		            Class<?> parameter_types[] = new Class[3];
		            parameter_types[0] = Set.class;
		            parameter_types[1] = Attributes.class;
		            parameter_types[2] = String.class;
		            
		            Object[] arg_list = new Object[3];
		            arg_list[0] = map;
		            arg_list[1] = attributes;
		            arg_list[2] = s_dtd;

		            realParser = (GsXMLHelper) parser_class.getConstructor( parameter_types).newInstance( arg_list);
	            	
	            } else {
	                throw new SAXException( new GsException( GsException.GRAVITY_ERROR, "STR_noSuchGraphType"));
	            }
        	}
        	catch( NoSuchMethodException nsme){
        		throw new SAXException( new GsException( "STR_NoAvailableParser", nsme));
        	}
        	catch( InvocationTargetException ite){
        		throw new SAXException( new GsException( "STR_NoAvailableParser", ite));
        	}
        	catch( IllegalAccessException iae){
        		throw new SAXException( new GsException( "STR_NoAvailableParser", iae));
        	}
        	catch( InstantiationException ie){
        		throw new SAXException( new GsException( "STR_NoAvailableParser", ie));
        	}

            xr.setContentHandler(realParser);
        }
    }
    
    public Graph getGraph() {
    	
		if (realParser == null) {
		    return null;
		}
		return realParser.getGraph();
    }
}
