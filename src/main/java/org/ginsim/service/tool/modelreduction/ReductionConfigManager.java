package org.ginsim.service.tool.modelreduction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.ginsim.common.application.GsException;
import org.ginsim.common.xml.XMLHelper;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.objectassociation.BasicGraphAssociatedManager;
import org.ginsim.core.graph.objectassociation.GraphAssociatedObjectManager;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.kohsuke.MetaInfServices;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Save/open simulation parameters along with the model.
 */
@MetaInfServices(GraphAssociatedObjectManager.class)
public class ReductionConfigManager extends BasicGraphAssociatedManager<ListOfReductionConfigs> {

	public static final String KEY = "modelSimplifier";
	
	public ReductionConfigManager() {
		super(KEY, null, RegulatoryGraph.class);
	}
	
    public ListOfReductionConfigs doOpen(InputStream is, Graph graph) throws GsException {
    	
        ReductionConfigParser parser = new ReductionConfigParser((RegulatoryGraph)graph);
        parser.startParsing(is, false);
        return parser.getParameters();
    }

    public void doSave(OutputStreamWriter os, Graph graph) {
        ListOfReductionConfigs paramList = (ListOfReductionConfigs) getObject(graph);
        try {
            XMLWriter out = new XMLWriter(os);
            out.openTag("modelModifierConfig");
            out.openTag("modelSimplifications");
            // add the available configurations
            for (ReductionConfig cfg: paramList) {
                cfg.toXML(out);
            }
            out.closeTag();
            
            out.openTag("listOfUsers");
            //for (String key: paramList.getOutputStrippingUsers()) {
            //    out.openTag("stripOutput");
            //    out.addAttr("key", key);
            //    out.closeTag();
            //}
            for (String key: paramList.getFixedPropagationUsers()) {
                out.openTag("propagateFixed");
                out.addAttr("key", key);
                out.closeTag();
            }
            out.closeTag();
            
            out.closeTag();
        } catch (IOException e) {
            new GsException( "STR_unableToSave", e);
        }
    }

	public ListOfReductionConfigs doCreate( Graph graph) {
		return new ListOfReductionConfigs( (RegulatoryGraph)graph);
	}
}

/**
 * parser for simulation parameters file
 */
class ReductionConfigParser extends XMLHelper {

    List<RegulatoryNode> nodeOrder;
    ListOfReductionConfigs paramList;
    
    /**
     * @param graph expected node order
     */
    public ReductionConfigParser(RegulatoryGraph graph) {
    	this.nodeOrder = graph.getNodeOrder();
        this.paramList = (ListOfReductionConfigs) ObjectAssociationManager.getInstance().getObject( graph, ReductionConfigManager.KEY, true);
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (qName.equals("simplificationConfig")) {
        	ReductionConfig cfg = paramList.get(paramList.create(false));
        	cfg.setName(attributes.getValue("name"));
        	String s_strict = attributes.getValue("strict");
        	if (s_strict != null) {
        		cfg.strict = "true".equals(s_strict);
        	} else {
        		cfg.strict = true;
        	}
        	String[] t_remove = attributes.getValue("removeList").split(" ");
        	for (int i=0 ; i<t_remove.length ; i++) {
        		for (RegulatoryNode vertex: nodeOrder) {
        			if (vertex.getId().equals(t_remove[i])) {
        				cfg.remove(vertex);
        			}
        		}
        	}
        } //else if (qName.equals("stripOutput")) {
         //   String key = attributes.getValue("key");
         //   paramList.setStrippingOutput(key, true);
       // }
        else if (qName.equals("propagateFixed")) {
            String key = attributes.getValue("key");
            paramList.setPropagateFixed(key, true);
        }
    }
    
    public void endElement (String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

    /**
     * @return the list of reduction parameters read by this parser.
     */
	public ListOfReductionConfigs getParameters() {
		return paramList;
	}
}