package org.ginsim.core.graph.hierarchicaltransitiongraph;

import java.util.List;

import org.colomoto.biolqm.NodeInfo;
import org.ginsim.core.graph.AbstractGraphFactory;
import org.ginsim.core.graph.GraphFactory;
import org.ginsim.core.graph.view.style.NodeStyle;
import org.kohsuke.MetaInfServices;


/**
 * Factory for hierarchical transition graphs.
 *
 * @author Duncan Berenguier
 * @author aurelien Naldi
 */
@MetaInfServices( GraphFactory.class)
public class HierarchicalTransitionGraphFactory extends AbstractGraphFactory<HierarchicalTransitionGraph> {
	
	public static final String KEY = "hierarchical";
    private static HierarchicalTransitionGraphFactory instance = null;
    
    public HierarchicalTransitionGraphFactory(){
    	super(HierarchicalTransitionGraph.class, KEY);
    	if( instance == null){
    		instance = this;
    	}
    }
    
	/**
     * @return an instance of this graphDescriptor.
     */
    public static HierarchicalTransitionGraphFactory getInstance() {
    	
        if (instance == null) {
            instance = new HierarchicalTransitionGraphFactory();
        }
        return instance;
    }

    @Override
	public Class getParser(){
		return HierarchicalTransitionGraphParser.class;
	}
	
    @Override
	public HierarchicalTransitionGraph create(){
		return new HierarchicalTransitionGraphImpl();
	}
	
	public HierarchicalTransitionGraph create( List<NodeInfo> nodeOrder, boolean transientCompaction){
		return new HierarchicalTransitionGraphImpl( nodeOrder, transientCompaction);
	}
    
	@Override
	public NodeStyle<HierarchicalNode> createDefaultNodeStyle(HierarchicalTransitionGraph graph) {
		return new DefaultHTGNodeStyle(graph);
	}

}
