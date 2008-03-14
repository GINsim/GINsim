package fr.univmrs.tagc.GINsim.regulatoryGraph.mutant;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import fr.univmrs.tagc.GINsim.graph.GsGraph;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsMutantListManager;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraph;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryVertex;
import fr.univmrs.tagc.common.xml.XMLHelper;

/**
 * parser for simulation parameters file
 */
public class GsRegulatoryMutantParser extends XMLHelper {

    public GsGraph getGraph() {
        // doesn't create a graph!
        return null;
    }
    public String getFallBackDTD() {
        // doesn't use a DTD either
        return null;
    }

    GsRegulatoryMutants mutantList = null;
    GsRegulatoryGraph graph;
    List nodeOrder;
    String[] t_order;
    GsRegulatoryMutantDef mutant;
    
    /**
     * @param graph expected node order
     */
    public GsRegulatoryMutantParser(GsRegulatoryGraph graph) {
    	this.graph = graph;
        this.nodeOrder = graph.getNodeOrder();
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        
        if (qName.equals("mutantList")) {
            if (mutantList != null) {
                // TODO: report error, already listed!!
                return;
            }
            mutantList = (GsRegulatoryMutants)graph.getObject(GsMutantListManager.key, true);
        } else if (qName.equals("mutant")) {
            if (mutantList == null) {
                // TODO: report error, malformed list
                return;
            }
            mutant = new GsRegulatoryMutantDef();
            mutant.name = attributes.getValue("name");
            for (int i=0 ; i<mutantList.getNbElements(null) ; i++) {
                if (mutantList.getElement(null, i).toString().equals(mutant.name)) {
                    // TODO: report error: duplicate ID entry
                    return;
                }
            }
            mutantList.v_data.add(mutant);
        } else if (qName.equals("change")) {
            if (mutant == null) {
                // TODO: report error: malformed file
                return;
            }
            String s_vertex = attributes.getValue("target");
            for (int i=0 ; i<nodeOrder.size() ; i++) {
                GsRegulatoryVertex vertex = (GsRegulatoryVertex)nodeOrder.get(i);
                if (vertex.getId().equals(s_vertex)) {
                    GsRegulatoryMutantChange change = new GsRegulatoryMutantChange(vertex);
                    change.setMin((short)Integer.parseInt(attributes.getValue("min")));
                    change.setMax((short)Integer.parseInt(attributes.getValue("max")));
                    mutant.v_changes.add(change);
                    break;
                }
            }
        } else if (qName.equals("link")) {
            if (mutant == null) {
                // TODO: report error
                return;
            }
            mutant.annotation.addLink(attributes.getValue("xlink:href"), null);
        } else if (qName.equals("comment")) {
            curval = "";
        }
    }

    
    public void endElement (String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (qName.equals("comment")) {
            mutant.annotation.setComment(curval);
            curval = null;
        }
    }
    /**
     * @return the list of parameters read by this parser.
     */
	public Object getParameters() {
		return mutantList;
	}
}