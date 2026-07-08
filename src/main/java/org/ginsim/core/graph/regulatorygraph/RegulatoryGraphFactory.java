package org.ginsim.core.graph.regulatorygraph;

import org.ginsim.core.graph.AbstractGraphFactory;
import org.ginsim.core.graph.GraphFactory;
import org.ginsim.core.graph.view.style.EdgeStyle;
import org.ginsim.core.graph.view.style.NodeStyle;
import org.ginsim.core.graph.view.style.NodeStyleImpl;
import org.kohsuke.MetaInfServices;


/**
 * Factory for regulatory graphs.
 *
 * @author Aurelien Naldi
 */
@MetaInfServices( GraphFactory.class)
public class RegulatoryGraphFactory extends AbstractGraphFactory<RegulatoryGraph> {

	public static final String KEY = "regulatory";
    private static RegulatoryGraphFactory instance = null;
    
    public RegulatoryGraphFactory() {
    	super(RegulatoryGraph.class, KEY);
    	if( instance == null){
    		instance = this;
    	}
    }

    
    /**
     * @return an instance of this graphDescriptor.
     */
    public static RegulatoryGraphFactory getInstance() {
        if (instance == null) {
            instance = new RegulatoryGraphFactory();
        }
        return instance;
    }
    
	@Override
    public RegulatoryGraph create() {
    	RegulatoryGraph graph = new RegulatoryGraphImpl();
        return graph;
    }

	
	@Override
    public Class getParser() {
    	return RegulatoryParser.class;
    }

	@Override
	public NodeStyle<RegulatoryNode> createDefaultNodeStyle(RegulatoryGraph graph) {
		return new DefaultRegulatoryNodeStyle();
	}
	
	@Override
	public EdgeStyle<RegulatoryNode, RegulatoryMultiEdge> createDefaultEdgeStyle(RegulatoryGraph graph) {
		return new DefaultRegulatoryEdgeStyle();
	}

}
