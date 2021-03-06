package org.ginsim.service.tool.reg2dyn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.ginsim.common.application.GsException;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.common.xml.XMLize;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.objectassociation.BasicGraphAssociatedManager;
import org.ginsim.core.graph.objectassociation.GraphAssociatedObjectManager;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.kohsuke.MetaInfServices;


/**
 * Save/open simulation parameters along with the model.
 */
@MetaInfServices(GraphAssociatedObjectManager.class)
public class SimulationParametersManager extends BasicGraphAssociatedManager<SimulationParameterList> {

	public static final String KEY = "reg2dyn_parameters";
	
	public SimulationParametersManager() {
		super(KEY, null, RegulatoryGraph.class);
	}

	@Override
	public SimulationParameterList doOpen(InputStream is, Graph graph)  throws GsException{
        SimulationParametersParser parser = new SimulationParametersParser((RegulatoryGraph)graph);
        parser.startParsing(is, false);
        return parser.getParameters();
    }

    @Override
    public void doSave(OutputStreamWriter os, Graph graph) throws GsException{
        SimulationParameterList paramList = (SimulationParameterList) ObjectAssociationManager.getInstance().getObject( graph, KEY, false);
        List nodeOrder = ((RegulatoryGraph)graph).getNodeOrder();
        try {
            XMLWriter out = new XMLWriter(os);
            out.openTag("simulationParameters");
            String s_nodeOrder = nodeOrder.get(0).toString();
            for (int i=1 ; i<nodeOrder.size() ; i++) {
                s_nodeOrder += " "+nodeOrder.get(i);
            }
            out.addAttr("nodeOrder", s_nodeOrder);
            // add priority class definition
            if (paramList.pcmanager != null && paramList.pcmanager.size() > 0) {
                for (int i=2 ; i<paramList.pcmanager.size() ; i++) {
                	((XMLize)paramList.pcmanager.get(i)).toXML(out);
                }
            }
            // and the real parameters
            for (SimulationParameters sparam: paramList) {
                sparam.toXML(out);
            }
            out.closeTag();
        } catch (IOException e) {
            throw new GsException(GsException.GRAVITY_ERROR, e.getLocalizedMessage());
        }
    }

    @Override
	public SimulationParameterList doCreate( Graph graph) {
		return new SimulationParameterList( graph);
	}
}
