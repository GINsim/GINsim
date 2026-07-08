package org.ginsim.core.graph.hierarchicaltransitiongraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDManagerFactory;
import org.colomoto.mddlib.MDDVariableFactory;
import org.colomoto.mddlib.PathSearcher;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.GraphChangeType;
import org.ginsim.core.graph.GraphEventCascade;
import org.ginsim.core.graph.dynamicgraph.TransitionGraphImpl;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.io.parser.GINMLWriter;
import org.ginsim.service.tool.modelreduction.ReductionConfig;
import org.ginsim.service.tool.reg2dyn.SimulationStrategy;
/**
 * Implementation of the HTG interface
 *
 * @author Duncan Berenguier
 */
public class HierarchicalTransitionGraphImpl extends TransitionGraphImpl<HierarchicalNode, DecisionOnEdge> implements HierarchicalTransitionGraph {

	public static final String GRAPH_ZIP_NAME = "hierarchicalTransitionGraph.ginml";
	public static final String GRAPH_ZIP_NAME_SCC = "sccGraph.ginml";
	
	private List<NodeInfo> nodeOrder = new ArrayList<NodeInfo>();

    private MDDManager ddmanager;
	
	/**
	 * Mode is either SCC or HTG depending if we group the transients component by their atteignability of attractors.
	 */
	private boolean transientCompaction;

	private ReductionConfig reduction = null;

	/**
	 * An array indicating for each node in the nodeOrder their count of children. (ie. their max value)
	 */
	private byte[] childsCount = null;

	private SimulationStrategy _strategy = SimulationStrategy.STG;
	
	
/* **************** CONSTRUCTORS ************/	
	
	
	/**
	 * create a new empty DynamicalHierarchicalGraph.
	 */
	public HierarchicalTransitionGraphImpl() {
		super(HierarchicalTransitionGraphFactory.getInstance());
	}

	/**
	 * create a new DynamicalHierarchicalGraph with a nodeOrder.
	 * @param nodeOrder the node order
	 * @param transientCompaction flag to enable further compaction
	 */
	public HierarchicalTransitionGraphImpl( List<NodeInfo> nodeOrder, boolean transientCompaction) {
		
	    this();
        setNodeOrder(nodeOrder);
	    this.transientCompaction = transientCompaction;
	}

	@Override
	public GraphEventCascade graphChanged(RegulatoryGraph g,
			GraphChangeType type, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * set reduction ReductionConfig if htg is making from reduction
	 */
     public void setReduction(ReductionConfig reduction){
		 this.reduction = reduction;
	 }

	 public ReductionConfig getReduction(){
		 return this.reduction;
	 }

	 /**
	 * Return the node order as a List of NodeInfo
	 * 
	 * @return the node order as a List of NodeInfo
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
	 * Set a list of NodeInfo representing the order of node as defined by the model
	 * 
	 * @param node_order the list of NodeInfo representing the order of node as defined by the model
	 */
    @Override
	public void setNodeOrder( List<NodeInfo> node_order){

        this.nodeOrder.clear();
        MDDVariableFactory vbuilder = new MDDVariableFactory();
        for (NodeInfo ni: node_order) {
            this.nodeOrder.add(ni);
            vbuilder.add(ni, (byte)(ni.getMax()+1));
        }
        ddmanager = MDDManagerFactory.getManager(vbuilder, 10);
	}

	/**
	 * Set the reduction boolean value if this htg is comes from a reduction calculation
	 * @param true or false
	 */
/* **************** EDITION OF VERTEX AND EDGE ************/	

	/**
	 * add an edge between source and target
	 * @param source a HierarchicalNode
	 * @param target a HierarchicalNode
	 * @return the new edge
	 */
	@Override
	public DecisionOnEdge addEdge(HierarchicalNode source, HierarchicalNode target) {
		
		DecisionOnEdge e = getEdge(source, target);
		if (e != null) {
			return e;
		}
		
		// FIXME: creating an empty DecisionOnEdge object: is it even possible?
		DecisionOnEdge edge = new DecisionOnEdge( this, source, target, nodeOrder);
		if (addEdge(edge)) {
			return edge;
		}
		return null;
	}

		
/* **************** SAVE ************/	
		
	@Override
	public String getGraphZipName(){
		if (getSimulationStrategy() == SimulationStrategy.SCCG) {
			return GRAPH_ZIP_NAME_SCC;
		}
		return GRAPH_ZIP_NAME;
	}

	@Override
	protected GINMLWriter getGINMLWriter() {
		return new HierarchicalGINMLWriter(this, transientCompaction, stringNodeOrder());
	}
		
/* **************** NODE SEARCH ************/
	
	@Override
	public List<HierarchicalNode> searchNodes(String regexp) {
		List<HierarchicalNode> v = new ArrayList<HierarchicalNode>();
		
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < regexp.length(); i++) {
			char c = regexp.charAt(i);
			if (c == '\\') {
				s.append(regexp.charAt(++i));
			} else if (c == '*') {
				s.append("[0-9\\*]");
			} else if (c == '0' || (c >= '1' && c <= '9')){
				s.append("["+c+"\\*]");
			} else if (c == ' ' || c == '\t') {
				//pass
			} else {
				s.append(c);
			}
		}
		Pattern pattern = Pattern.compile(s.toString(), Pattern.COMMENTS | Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher("");
		
		for (Iterator<HierarchicalNode> it = this.getNodes().iterator(); it.hasNext();) {
			HierarchicalNode vertex = it.next();
			matcher.reset(vertex.statesToString());
			if (matcher.find()) {
				v.add(vertex);
			}
		}
		return v;
	}
	
	@Override
	public HierarchicalNode getNodeForState(byte[] state) {
		for (HierarchicalNode v: this.getNodes()) {
			if (v.contains(state)) {
				return v;
			}
		}
		return null;
	}
	
		
	
		
/* **************** GETTER AND SETTERS ************/
		
	/**
	 * return an array indicating for each node in the nodeOrder their count of childs. (ie. their max value)
	 */
	@Override
	public byte[] getChildsCount() {
		if (childsCount == null) {
			childsCount = new byte[nodeOrder.size()];
			int i = 0;
			for (NodeInfo v: nodeOrder) {
				childsCount[i++] = (byte) ( v.getMax()+1);
			}			
		}
		return childsCount;
	}
	
	@Override
	public void setChildsCount(byte[] cc) {
		childsCount = cc;
	}
	
	/**
	 * Return a string representation of the nodeOrder
	 *  
	 * ex : <tt>G0:1 G1:2 G2:1 G3:3</tt>
	 * 
	 * @return
	 */
	private String stringNodeOrder() {
		String s = "";
		for (NodeInfo v: nodeOrder) {
			s += v.getNodeID() + ":" + v.getMax() + " ";
		}
		if (s.length() > 0) {
			return s.substring(0, s.length()-1);
		}
		return s;
	}
	
	/**
	 * Return <b>true</b> if the transients are compacted into component by their atteignability of attractors.
	 * @return
	 */
	@Override
	public boolean areTransientCompacted() {
		return transientCompaction;
	}
	
	@Override
	public void setMode(boolean compacted) {
		transientCompaction = compacted;
	}

    /**
     * Indicates if the given graph can be associated to the current one
     * 
     * @param graph the graph to associate to the current one
     * @return true is association is possible, false if not
     */
    @Override
    protected boolean isAssociationValid( Graph<?, ?> graph) {
    	
    	if( graph instanceof RegulatoryGraph){
    		return true;
    	}
    	
    	return false;
    }
    

		
/* **************** UNIMPLEMENTED METHODS ************/


	/**
	 * Not used for this kind of graph: it's not interactively editable
	 */
	@Override
	public Graph getSubgraph(Collection<HierarchicalNode> vertex, Collection<DecisionOnEdge> edges) {
		return null;
	}

    /**
	 * 
	 * not used for this kind of graph: it has no meaning
     */
	@Override
	protected List doMerge( Graph otherGraph) {
        return null;
    }

    public StatesSet createStateSet() {
        return new StatesSet(ddmanager, getChildsCount());
    }


	public void setSimulationStrategy(SimulationStrategy strategy) {
		_strategy = strategy;
	}
	public SimulationStrategy getSimulationStrategy() {
		return _strategy;
	}
	
    public String getExtraValueInterval(byte[] state, int idx) {
		PathSearcher searcher = new PathSearcher(super.ddmanager);
		int[] path = searcher.getPath();
		searcher.setNode(super.extraFunctions[0]);

		int min = Integer.MAX_VALUE, max = -1;
		nextp: for (int l : searcher) { // for each MDD path
			for (int i = 0; i < path.length; i++) { // for each level / component
				if (path[i] != -1 && state[i] != -1 && path[i] != state[i]) {
					// path incompatible with state[]
					continue nextp;
				}
			}
			if (l > max) max = l;
			if (l < min) min = l;
		}
		
		if (min == 0 && max == super.extraNodes.get(idx).getMax()) {
			return "*";
		} else if (min == max) {
			return "" + min;
		} 
		return min + "-" + max;
    }
}
