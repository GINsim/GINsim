package org.ginsim.core.graph.dynamicgraph;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.GraphChangeType;
import org.ginsim.core.graph.GraphEventCascade;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraphImpl;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.io.parser.GINMLWriter;
import org.ginsim.service.tool.reg2dyn.SimulationStrategy;

/**
 * Implementation of dynamical graphs.
 * This class should not be used directly: instances are created by the associated factory.
 * 
 * @author Aurelien Naldi
 * @author Lionel Spinelli
 */
public final class DynamicGraphImpl	extends TransitionGraphImpl<DynamicNode, DynamicEdge> implements DynamicGraph {

	public static final String GRAPH_ZIP_NAME = "stateTransitionGraph.ginml";
	
	protected List v_stables = null;

    private SimulationStrategy _strategy = SimulationStrategy.STG;

	/**
	 * create a new DynamicGraph.
	 * @param nodeOrder
	 */
	protected DynamicGraphImpl(List<NodeInfo> nodeOrder) {

		this();
		this.nodeOrder = nodeOrder;
	}

	protected DynamicGraphImpl() {
		super( DynamicGraphFactory.getInstance());
	}

	@Override
	public GraphEventCascade graphChanged(RegulatoryGraph g,
			GraphChangeType type, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Return the node order as a list of String
	 * 
	 * @return the node order as a list of String
	 */
	@Override
	public List<NodeInfo> getNodeOrder() {
		
		return nodeOrder;
	}
	
    /**
     * Return the size of the node order
     * 
     * @return the size of the node order
     */
    @Override
	public int getNodeOrderSize(){
		
		if( nodeOrder != null){
			return nodeOrder.size();
		}
		else{
			return 0;
		}
	}
	
    
	/**
	 * Set a list of String representing the order of node as defined by the model
	 * 
	 * @param node_order the list of String representing the order of node as defined by the model
	 */
	@Override
	public void setNodeOrder( List<NodeInfo> node_order){
		
		nodeOrder = node_order;
	}

    
	/**
	 * Return the zip extension for the graph type
	 * 
	 * @return the zip extension for the graph type
	 */
	@Override
	protected String getGraphZipName(){
		return GRAPH_ZIP_NAME;
	}
	
    @Override
	protected GINMLWriter getGINMLWriter() {
    	return new DynamicGINMLWriter(this);
    }

	@Override
	public boolean removeEdge(DynamicEdge obj) {
		return false;
	}

	@Override
	public DynamicEdge addEdge(DynamicNode source, DynamicNode target, boolean multiple) {
		
		DynamicEdge edge = new DynamicEdge(this, source, target);
		if (!addEdge(edge)) {
			return null;
		}
		return edge;
	}

	
	@Override
    protected List doMerge( Graph otherGraph) {

        // first check if this merge is allowed!
        if (!(otherGraph instanceof DynamicGraph)) {
            return null;
        }
        List v_order = ((DynamicGraph)otherGraph).getNodeOrder();
        if (v_order.size() != nodeOrder.size()) {
            return null;
        }
        for (int i=0 ; i<nodeOrder.size() ; i++) {
            if (!nodeOrder.get(i).toString().equals(v_order.get(i).toString())) {
                return null;
            }
        }

        List ret = new ArrayList();
        Iterator it = otherGraph.getNodes().iterator();
        NodeAttributesReader vReader = getNodeAttributeReader();
        NodeAttributesReader cvreader = otherGraph.getNodeAttributeReader();
        while (it.hasNext()) {
            DynamicNode vertex = (DynamicNode)it.next();
            addNode(vertex);
            cvreader.setNode(vertex);
            vReader.setNode(vertex);
            vReader.copyFrom(cvreader);
            vReader.refresh();
            ret.add(vertex);
        }

        for (Edge edge: (Collection<Edge>)otherGraph.getEdges()) {
            DynamicNode from = (DynamicNode)edge.getSource();
            DynamicNode to = (DynamicNode)edge.getTarget();
            int c = 0;
            for ( int i=0 ; i<from.state.length ; i++) {
            	if (from.state[i] != to.state[i]) {
            		c++;
            	}
            }
            ret.add(addEdge(from, to, c>1));
        }

        return ret;
    }
    
	@Override
    public Graph getSubgraph(Collection vertex, Collection edges) {
        // no copy for state transition graphs
        return null;
    }

    /**
     * look for the shortest path between two states.
     * @param source
     * @param target
     * @return the List describing the path or null if none is found
     */
	@Override
    public List shortestPath(byte[] source, byte[] target) {
    	
        DynamicNode n = new DynamicNode(source);
        DynamicNode n2 = new DynamicNode(target);
        if (containsNode(n) && containsNode(n2)) {
            return getShortestPath(n, n2);
        }
        return null;
    }
    /**
     * Indicates if the given graph can be associated to the current one
     * 
     * @param graph the graph to associate to the current one
     * @return true is association is possible, false if not
     */
    @Override
    protected boolean isAssociationValid( Graph<?,?> graph) {
    	
        if (graph == null) {
            return true;
        }
        
        if (!(graph instanceof RegulatoryGraph)) {
            return false;
        }
        return RegulatoryGraphImpl.associationValid((RegulatoryGraph)graph, this);
    }

    public void setSimulationStrategy(SimulationStrategy strategy) {
        _strategy = strategy;
    }
    public SimulationStrategy getSimulationStrategy() {
        return _strategy;
    }
}
