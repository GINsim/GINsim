package org.ginsim.core.graph.regulatorygraph;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.colomoto.biolqm.*;
import org.colomoto.biolqm.metadata.AnnotationModule;
import org.colomoto.biolqm.metadata.Annotator;
import org.colomoto.biolqm.metadata.annotations.JsonReader;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDManagerFactory;
import org.colomoto.mddlib.MDDVariableFactory;
import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.core.graph.AbstractGraph;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.GraphChangeType;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.io.parser.GINMLWriter;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.notification.resolvable.NotificationResolution;
import org.json.JSONObject;

/**
 * Implementation of the RegulatoryGraph interface.
 * 
 * @author Aurelien Naldi
 */
public final class RegulatoryGraphImpl  extends AbstractGraph<RegulatoryNode, RegulatoryMultiEdge> 
	implements RegulatoryGraph {

	public static final String GRAPH_ZIP_NAME = "regulatoryGraph.ginml";
	
	private int nextid=0;

	private List<RegulatoryNode> nodeOrder = new ArrayList<RegulatoryNode>();
    private List<byte[]> initialStates = null;
    private List<List<byte[]>> oracles = null;

    private boolean use_name = false;
    
    private AnnotationModule annotationModule;

    /**
     * Create a new Regulatory graph
     */
    public RegulatoryGraphImpl() {
        super( RegulatoryGraphFactory.getInstance());
        
        try {
			this.annotationModule = new AnnotationModule();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override
    public List<RegulatoryNode> getNodeOrder() {
    	return nodeOrder;
    }
    
	@Override
	public List<NodeInfo> getNodeInfos() {
		return getNodeInfos(nodeOrder);
	}

    @Override
	public int getNodeOrderSize(){
		
		if( nodeOrder != null){
			return nodeOrder.size();
		}
		else{
			return 0;
		}
	}
	
    @Override
    public void setNodeOrder( List<RegulatoryNode> nodeOrder) {
		this.nodeOrder = nodeOrder;
	}
    
    @Override
    public RegulatoryNode addNode() {

        while ( getNodeByName("G" + nextid) != null) {
        		nextid++;
        }
        RegulatoryNode obj = new RegulatoryNode(nextid++, this);
        if (addNode(obj)) {
    		return obj;
        }
        return null;
    }
    
    @Override
	public boolean addNode( RegulatoryNode node){
		
        if (node != null && super.addNode( node)) {
    		if (!nodeOrder.contains(node)) {
        		nodeOrder.add( node);
    		} else {
    			System.err.println("The new node " + node + "was added to the node order behind my back");
    		}
    		return true;
        }
        
        return false;
	}
    
    @Override
    public RegulatoryMultiEdge addEdge(RegulatoryNode source, RegulatoryNode target, RegulatoryEdgeSign sign) {
    	RegulatoryMultiEdge obj = getEdge(source, target);
    	if (obj != null) {

    		// TODO: restore this action without requiring to know the GUIManager?
    		NotificationResolution resolution = null;
//    		NotificationResolution resolution = new NotificationResolution(){
//    			
//    			public boolean perform( Graph graph, Object[] data, int index) {
//    				
//    				GUIManager.getInstance().getGraphGUI(graph).selectEdge((Edge<?>)data[0]);
//    				return true;
//    			}
//    			
//    			public String[] getOptionsName(){
//    				
//    				String[] t_option = {"Go"};
//    				return t_option;
//    			}
//    		};
    		
    		NotificationManager.publishResolvableWarning( this, "STR_usePanelToAddMoreEdges", this, new Object[] {obj}, resolution);
    		
    		return obj;
    	}
    	obj = new RegulatoryMultiEdge(this, source, target, sign);
    	addEdge(obj);
    	obj.rescanSign( this);
    	target.incomingEdgeAdded(obj);
    	return obj;
    }

    @Override
    public String getDisplayName(RegulatoryNode node) {
        if (use_name) {
            return node.getDisplayName();
        }
        return node.getId();
    }

    @Override
    public Map<String,String> getAttributes() {
        if (use_name) {
            attributes.put("display.node", "name");
        } else {
            attributes.remove("display.node");
        }

        return attributes;
    }

    @Override
    public void setAttribute(String name, String value) {
        super.setAttribute(name, value);

        if ("display.node".equalsIgnoreCase(name)) {
            use_name = "name".equalsIgnoreCase(value);
        }
    }

    @Override
	protected String getGraphZipName(){
		return GRAPH_ZIP_NAME;
	}
    
    @Override
	protected GINMLWriter getGINMLWriter() {
    	return new RegulatoryGINMLWriter(this);
    }

    @Override
    public boolean idExists(String newId) {
        Iterator it = getNodes().iterator();
        while (it.hasNext()) {
            if (newId.equals(((RegulatoryNode)it.next()).getId())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void changeNodeId(Object node, String newId) throws GsException {
        RegulatoryNode rvertex = (RegulatoryNode)node;
        if (newId.equals(rvertex.getId())) {
            return;
        }
        if (idExists(newId)) {
        	throw  new GsException(GsException.GRAVITY_ERROR, "id already exists");
        }
        if (!rvertex.setId(newId, this)) {
        	throw  new GsException(GsException.GRAVITY_ERROR, "invalid id");
        }
    }

    @Override
    public boolean removeEdge(RegulatoryMultiEdge edge) {
       edge.markRemoved();
       super.removeEdge(edge);
       RegulatoryNode src = edge.getSource();
       Collection<RegulatoryMultiEdge> targets = getOutgoingEdges(src);
       if (targets == null || targets.size() == 0) {
    	   src.setOutput(true, this);
       }
       edge.getTarget().removeEdgeFromInteraction(edge);
       fireGraphChange(GraphChangeType.EDGEREMOVED, edge);
       return true;
    }

    @Override
    public boolean removeNode(RegulatoryNode obj) {
    	List<RegulatoryMultiEdge> outedges = new ArrayList<RegulatoryMultiEdge>();
        for (RegulatoryMultiEdge me: getOutgoingEdges(obj)) {
        	outedges.add(me);
        }
        for (RegulatoryMultiEdge me: outedges) {
            removeEdge(me);
        }
        super.removeNode( obj);
        nodeOrder.remove(obj);
        fireGraphChange(GraphChangeType.NODEREMOVED, obj);
        return true;
    }

    @Override
    public RegulatoryNode addNewNode(String id, String name, byte max) {
    	RegulatoryNode existing = getNodeByName(id);
    	if (existing != null) {
    		throw new RuntimeException("A node with id \""+id+"\" already exists");
    	}
        RegulatoryNode vertex = new RegulatoryNode(id, this);
        if (name != null) {
            vertex.setName(name, this);
        }
        vertex.setMaxValue(max, this);
        addNode(vertex);

        return vertex;
    }

    @Override
    public RegulatoryEdge addNewEdge(String from, String to, byte minvalue, String sign)  throws GsException{
    	RegulatoryEdgeSign vsign = RegulatoryEdgeSign.UNKNOWN;
    	for (RegulatoryEdgeSign s : RegulatoryEdgeSign.values()) {
    		if (s.getLongDesc().equals(sign)) {
    			vsign = s;
    			break;
    		}
    	}
    	return addNewEdge(from, to, minvalue, vsign);
    }
    
    @Override
    public RegulatoryEdge addNewEdge(String from, String to, byte minvalue, RegulatoryEdgeSign sign) throws GsException {
        RegulatoryNode source = null;
        RegulatoryNode target = null;

        source = (RegulatoryNode) getNodeByName(from);
        if (from.equals(to)) {
            target = source;
        } else {
            target = (RegulatoryNode) getNodeByName(to);
        }

        if (source == null || target == null) {
            throw new GsException( GsException.GRAVITY_ERROR, "STR_noSuchNode");
        }
        RegulatoryMultiEdge me = getEdge(source, target);
        int index = 0;
        if (me == null) {
            me = new RegulatoryMultiEdge(this, source, target, sign, minvalue);
            addEdge(me);
        } else {
            index = me.addEdge(sign, minvalue, this);
        }
        return me.getEdge(index);
    }

    @Override
	public void canApplyNewMaxValue(RegulatoryNode node, byte newMax, List l_fixable, List l_conflict) {
		for (RegulatoryMultiEdge me: getOutgoingEdges(node)) {
			me.canApplyNewMaxValue(newMax, l_fixable, l_conflict);
		}
	}

	private String stringNodeOrder() {
		String s = "";
		for (int i=0 ; i<nodeOrder.size() ; i++) {
			s += nodeOrder.get(i)+" ";
		}
		if (s.length() > 0) {
			return s.substring(0, s.length()-1);
		}
		return s;
	}

    @Override
    protected List doMerge( Graph<RegulatoryNode, RegulatoryMultiEdge> otherGraph) {
        if (!(otherGraph instanceof RegulatoryGraph)) {
            return null;
        }
        List ret = new ArrayList();
        HashMap copyMap = new HashMap();
        NodeAttributesReader vReader = getNodeAttributeReader();
        NodeAttributesReader cvreader = otherGraph.getNodeAttributeReader();
        for (RegulatoryNode vertexOri: otherGraph.getNodes()) {
            RegulatoryNode vertex = vertexOri.clone(this);
            addNodeWithNewId(vertex);
            cvreader.setNode(vertexOri);
            vReader.setNode(vertex);
            vReader.copyFrom(cvreader);
            vReader.refresh();
            copyMap.put(vertexOri, vertex);
            ret.add(vertex);
        }

        Iterator<RegulatoryMultiEdge> it2 = otherGraph.getEdges().iterator();
        EdgeAttributesReader eReader = getEdgeAttributeReader();
        EdgeAttributesReader cereader = otherGraph.getEdgeAttributeReader();
        while (it2.hasNext()) {
        	RegulatoryMultiEdge deOri = it2.next();
        	RegulatoryMultiEdge edge = addEdge((RegulatoryNode)copyMap.get(deOri.getSource()), (RegulatoryNode)copyMap.get(deOri.getTarget()), RegulatoryEdgeSign.POSITIVE);
            edge.copyFrom(deOri);
            cereader.setEdge(deOri);
            eReader.setEdge(edge);
            eReader.copyFrom(cereader);
            eReader.refresh();
            copyMap.put(deOri, edge);
            ret.add(edge);
        }

        for (RegulatoryNode node: otherGraph.getNodes()) {
            node.cleanupInteractionForNewGraph(copyMap);
        }
        return ret;
    }
    
    
    /**
     * @param node
     */
    private void addNodeWithNewId(RegulatoryNode node) {
        String id = node.getId();
        if (getNodeByName(id) == null) {
            addNode(node);
            return;
        }
        int addon = 1;
        while ( getNodeByName(id+"_"+addon) != null) {
            addon++;
        }
        node.setId(id+"_"+addon, this);
        addNode(node);
    }

    @Override
    public Graph getSubgraph(Collection<RegulatoryNode> v_vertex, Collection<RegulatoryMultiEdge> v_edges) {

        RegulatoryGraph copiedGraph = GSGraphManager.getInstance().getNewGraph();
        NodeAttributesReader vReader = getNodeAttributeReader();
        NodeAttributesReader cvreader = copiedGraph.getNodeAttributeReader();
        HashMap copyMap = new HashMap();
        if (v_vertex != null) {
            for (RegulatoryNode vertexOri: v_vertex) {
                RegulatoryNode vertex = vertexOri.clone(this);
                ((RegulatoryGraphImpl)copiedGraph).addNodeWithNewId(vertex);
                vReader.setNode(vertexOri);
                cvreader.setNode(vertex);
                cvreader.copyFrom(vReader);
                copyMap.put( vertexOri, vertex);
            }
        }

        if (v_edges != null) {
        	EdgeAttributesReader eReader = getEdgeAttributeReader();
            EdgeAttributesReader cereader = copiedGraph.getEdgeAttributeReader();
	        for (RegulatoryMultiEdge edgeOri: v_edges) {
	        	RegulatoryMultiEdge edge = copiedGraph.addEdge((RegulatoryNode)copyMap.get(edgeOri.getSource()), (RegulatoryNode)copyMap.get(edgeOri.getTarget()), RegulatoryEdgeSign.POSITIVE);
	            edge.copyFrom(edgeOri);
	            copyMap.put(edgeOri, edge);
                eReader.setEdge(edgeOri);
                cereader.setEdge(edge);
                cereader.copyFrom(eReader);
	        }
        }

        if (v_vertex != null) {
            for (RegulatoryNode v: v_vertex) {
                v.cleanupInteractionForNewGraph(copyMap);
            }
        }

        return copiedGraph;
    }

    /**
     * Test if an association between a regulatory graph and a state transition graph is valid:
     * all what we can do is checking the node-order to see if they obviously differ (by size of node's name).
     *
     * if this function returns true, it's _NOT_ a garanty that both graphs are compatibles,
     * for exemple the state transition graph could have higher max value for one of the node.
     *
     * @param regGraph
     * @param dynGraph
     * @return true if the two graph can be associated
     */
    public static boolean associationValid(RegulatoryGraph regGraph, DynamicGraph dynGraph) {
        if (regGraph == null || dynGraph == null) {
            return false;
        }

        List regOrder = regGraph.getNodeOrder();
        List dynOrder = dynGraph.getNodeOrder();
        if (regOrder == null || dynOrder == null || regOrder.size() != dynOrder.size()) {
            return false;
        }

        for (int i=0 ; i<regOrder.size() ; i++) {
            if (!dynOrder.get(i).equals( regOrder.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public MDDManager getMDDFactory() {
    	return  getMDDFactory(null);
    }

    /**
     * Build a MDDManager with a custom variable order
     * 
     * @param order the desired order or null to use the default order
     * 
     * @return a new MDDManager with the required order
     */
    private MDDManager getMDDFactory(List<NodeInfo> order) {
    	if (order == null) {
    		// default to the node order
    		order = getNodeInfoOrder();
    	}
    	MDDVariableFactory vbuilder = new MDDVariableFactory();
    	for (NodeInfo ni: order) {
    		vbuilder.add(ni, (byte)(ni.getMax()+1));
    	}
    	MDDManager factory = MDDManagerFactory.getManager(vbuilder, 10);
    	return factory;
    	
    }
    
    private List<NodeInfo> getNodeInfoOrder() {
		List<NodeInfo> order = new ArrayList<NodeInfo>();
		for (RegulatoryNode node: getNodeOrder()) {
			order.add(node.getNodeInfo());
		}
		return order;
    }
    
    @Override
    public int[] getMDDs(MDDManager factory) {
    	int[] mdds = new int[getNodeCount()];
    	int i=0;
    	for (RegulatoryNode node: getNodeOrder()) {
    		mdds[i++] = node.getMDD(this, factory);
    	}
    	return mdds;
    }
    
    @Override
	public List<RegulatoryNode> getNodeOrderForSimulation() {
		return getNodeOrder();
	}
	
	@Override
	public LogicalModel getModel() {
		return getModel(null);
	}

    @Override
    public LogicalModel getModel(NodeOrderer orderer) {
        return getModel(orderer, false, false);
    }
    @Override
    public LogicalModel getModel(NodeOrderer orderer, boolean withLayout, boolean withAnnotations) {
        //System.out.println(">>I3:"+AvatarUtils.toString(getState()));
		List<NodeInfo> order = null;
		if (orderer == null) {
			order = new ArrayList<>();
			for (RegulatoryNode node: getNodeOrder()) {
				order.add(node.getNodeInfo());
			}
		} else {
			order = orderer.getOrder(this);
		}

		MDDManager factory = getMDDFactory(order);
		int[] functions = getMDDs(factory);
		if(isStateful()) return new StatefulLogicalModelImpl(order, factory, functions, initialStates, graphName);

		LogicalModel model = new LogicalModelImpl(order, factory, functions);

		// Include layout information if needed
		if (withLayout) {
            ModelLayout mlayout = model.getLayout();
            NodeAttributesReader nreader = this.getNodeAttributeReader();
            for (RegulatoryNode node: this.getNodeOrder()) {
                nreader.setNode(node);
                int x = nreader.getX();
                int y = nreader.getY();

                ModelLayout.LayoutInfo li = mlayout.setPosition(node.getNodeInfo(), x,y);
                li.width = nreader.getWidth();
                li.height = nreader.getHeight();
            }
        }

        if (withAnnotations) {
            // FIXME: transfer metadata to the LogicalModel
//            model.setAnnotationModule(annotationModule);
        }

		return model;
	}

	private List<NodeInfo> getNodeInfos(List<RegulatoryNode> order) {
		List<NodeInfo> n_info = new ArrayList<NodeInfo>(order.size());
		for (RegulatoryNode node: order) {
			n_info.add(node.getNodeInfo());
		}
		return n_info;
	}
	
	@Override
	public List<RegulatoryNode> searchNodes( String regexp) {
		if (!regexp.startsWith("::")) {
			return super.searchNodes(regexp);
		}
		
		regexp = regexp.substring(2);
		List<RegulatoryNode> v = new ArrayList<RegulatoryNode>();
		
		Pattern pattern = Pattern.compile(regexp, Pattern.COMMENTS | Pattern.CASE_INSENSITIVE);
		
		for (RegulatoryNode vertex: getNodes()) {
			Matcher matcher = pattern.matcher(vertex.toString());
			if (matcher.find()) {
				v.add(vertex);
				continue;
			}

			matcher = pattern.matcher(vertex.getName());
			if (matcher.find()) {
				v.add(vertex);
				continue;
			}

            // FIXME: search in annotations
		}
		return v;
	}

    @Override
    public Collection<RegulatoryNode> getNodes() {
        return nodeOrder;
    }
    
	@Override
	public void setAnnotationModule(AnnotationModule oldAnnotationModule) {
		this.annotationModule = oldAnnotationModule;
//
//		Map<NodeInfo, Index> oldNodesIndex = oldAnnotationModule.getNodesIndex();
//		Map<NodeInfoPair, Index> oldEdgesIndex = oldAnnotationModule.getEdgesIndex();
//
//		Map<NodeInfo, Index> newNodesIndex = new HashMap<>();
//		Map<NodeInfoPair, Index> newEdgesIndex = new HashMap<>();
//
//		Map<String, NodeInfo> oldIdsNodes = new HashMap<>();
//		Map<ArrayList<String>, NodeInfoPair> oldIdsEdges = new HashMap<>();
//
//		// update nodesIndex with the nodes of the GINsim model
//		for (Entry<NodeInfo, Index> oldEntry: oldAnnotationModule.getNodesIndex().entrySet()) {
//			NodeInfo node = oldEntry.getKey();
//			oldIdsNodes.put(node.getNodeID(), node);
//		}
//
//		for (NodeInfo newNodeInfo: this.getNodeInfos()) {
//			String newId = newNodeInfo.getNodeID();
//
//			if (oldIdsNodes.containsKey(newId)) {
//    			Index newIndex = oldNodesIndex.get(oldIdsNodes.get(newId));
//    			newNodesIndex.put(newNodeInfo, newIndex);
//    		}
//		}
//
//		// update edgesIndex with the nodes of the GINsim model
//		for (Entry<NodeInfoPair, Index> oldEntry: oldAnnotationModule.getEdgesIndex().entrySet()) {
//			NodeInfoPair pair = oldEntry.getKey();
//
//			String id1 = pair.getNode1().getNodeID();
//			String id2 = pair.getNode2().getNodeID();
//			ArrayList<String> ids = new ArrayList<>();
//			ids.add(id1);
//			ids.add(id2);
//
//			oldIdsEdges.put(ids, pair);
//		}
//
//		for (RegulatoryMultiEdge newEdge: this.getEdges()) {
//			RegulatoryNode interNode1 = newEdge.getSource();
//        	NodeInfo newNodeInfo1 = interNode1.getNodeInfo();
//        	String newId1 = newNodeInfo1.getNodeID();
//
//        	RegulatoryNode interNode2 = newEdge.getTarget();
//        	NodeInfo newNodeInfo2 = interNode2.getNodeInfo();
//			String newId2 = newNodeInfo2.getNodeID();
//
//			ArrayList<String> newIds = new ArrayList<>();
//			newIds.add(newId1);
//			newIds.add(newId2);
//
//			if (oldIdsEdges.containsKey(newIds)) {
//    			Index newIndex = oldEdgesIndex.get(oldIdsEdges.get(newIds));
//    			newEdgesIndex.put(new NodeInfoPair(newNodeInfo1, newNodeInfo2), newIndex);
//    		}
//		}
//
//		this.annotationModule.setNodesIndex(newNodesIndex);
//		this.annotationModule.setEdgesIndex(newEdgesIndex);
	}
    
	@Override
	public Annotator<NodeInfo> getAnnotator() {
		return new Annotator<>(this.annotationModule);
	}

    public void saveAssociated(ZipOutputStream zos, OutputStreamWriter osw) throws IOException {
        zos.putNextEntry(new ZipEntry(ZIP_PREFIX+"annotations.json"));
        
        // write JSON to osw
        JSONObject json = this.getAnnotator().writeAnnotationsInJSON();
        osw.write(json.toString());
        
        osw.flush();
        zos.closeEntry();
    }

    public void parseAssociated(ZipFile f, String prefix) throws GsException {
        ZipEntry ze = f.getEntry(prefix+"annotations.json");
        if (ze != null) {
            try {
                InputStream is = f.getInputStream(ze);

                // parse JSON from is
                JSONObject json = JsonReader.readInputStream(is);

                // we reinitialize the annotation module to get rid of the old annotations extracted from the GINML file
                this.setAnnotationModule(new AnnotationModule());

                // then we put the annotations from the JSON file in the new annotation module
                HashMap<String, NodeInfo> name2node = new HashMap<>();
                for (RegulatoryNode node: this.getNodes()) {
                    name2node.put(node.getId(), node.getNodeInfo());
                }
            	this.getAnnotator().readAnnotationsFromJSON(json, name2node);
                
                is.close();
            } catch (Exception e) {
                LogManager.error(e);
            }
        }
    }
    
	/* *****  STATEFUL GRAPH  ***** */

    @Override
	public List<byte[]> getStates() {
		return initialStates;
	}
	
    @Override
	public void setStates(List<byte[]> states) {
		initialStates = states;
	}
    
    @Override
	public boolean isStateful() {
		return initialStates != null;
	}
    
    @Override
	public List<List<byte[]>> getOracles() {
		return oracles;
	}
    
    @Override
	public void setOracles(List<List<byte[]>> _oracles) {
		oracles = _oracles;
	}
    
    @Override
	public boolean hasOracles() {
		return oracles != null;
	}
}
