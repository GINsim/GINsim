package org.ginsim.core.graph.regulatorygraph;

import java.util.List;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.metadata.AnnotationModule;
import org.colomoto.biolqm.metadata.Annotator;
import org.colomoto.mddlib.MDDManager;
import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.Graph;


/**
 * The regulatory graph
 *
 * @author Aurelien Naldi
 */
public interface RegulatoryGraph extends Graph<RegulatoryNode, RegulatoryMultiEdge>{
	
    /**
     * Return the node order
     * 
     * @return the node order as a list of RegulatoryNode
     */
    List<RegulatoryNode> getNodeOrder();
    
    List<NodeInfo> getNodeInfos();
    
    /**
     * add a node from textual parameters (for the parser).
     *
     * @param id
     * @param name
     * @param max
     * @return the new node.
     */
    RegulatoryNode addNewNode(String id, String name, byte max);
    
    /**
     * 
     * @return
     */
    RegulatoryNode addNode();
    
    
    /**
     * 
     * @param newId
     * @return True if a node of the graph has the given ID
     */
    boolean idExists(String newId);
    
    /**
     * add an edge from textual parameters (for the parser).
     * @param from
     * @param to
     * @param minvalue
     * @param sign
     * @return the new edge.
     */
    RegulatoryEdge addNewEdge(String from, String to, byte minvalue, RegulatoryEdgeSign sign) throws GsException;
    
    /**
     * add an edge from textual parameters (for the parser).
     * @param from
     * @param to
     * @param minvalue
     * @param sign
     * @return the new edge
     */
    RegulatoryEdge addNewEdge(String from, String to, byte minvalue, String sign)  throws GsException;
    
    
    /**
     * Add a signed edge
     * 
     * @param source
     * @param target
     * @param sign
     * @return
     */
    RegulatoryMultiEdge addEdge(RegulatoryNode source, RegulatoryNode target, RegulatoryEdgeSign sign);
    
    
    /**
     * Construct MDD representation of the logical functions of the nodes of this graph.
     * 
     * @param factory a MDDFactory associated with the nodes of this graph 
     * @return an array containing references to the MDD roots in the factory
     */
    int[] getMDDs(MDDManager factory);

    /**
     * Construct a MDDFactory associated to the nodes of this graph
     * @return a new MDDFactory.
     */
    MDDManager getMDDFactory();


	 /**
     * 
     * @param node
     * @param newId
     * @throws GsException
     **/
    void changeNodeId(Object node, String newId) throws GsException;

	List<RegulatoryNode> getNodeOrderForSimulation();


	/**
	 * Set a list of class dependent objects representing the order of node as defined by the model
	 * 
	 * @param list the list of objects representing the order of node as defined by the model
	 */
	void setNodeOrder( List<RegulatoryNode> list);
	
	
    /**
     * 
     * @param node
     * @param newMax
     * @param l_fixable
     * @param l_conflict
     */
	void canApplyNewMaxValue(RegulatoryNode node, byte newMax, List l_fixable, List l_conflict);

	/**
	 * Get a ready-to-be-used model (list of nodes and functions, no graph structure)
	 * 
	 * @return a model matching this RegulatoryGraph
	 */
	LogicalModel getModel();

	/**
	 * Get a ready-to-be-used model (list of nodes and functions, no graph structure)
	 * 
	 * @param orderer helper providing the desired node order
	 * 
	 * @return a model matching this RegulatoryGraph
	 */
	LogicalModel getModel(NodeOrderer orderer);

	/**
	 * Get a ready-to-be-used model (list of nodes and functions, no graph structure)
	 *
	 * @param orderer helper providing the desired node order
	 * @param withLayout flag to add layout information
	 * @param withAnnotations flag to add model metadata
	 *
	 * @return a model matching this RegulatoryGraph
	 */
	LogicalModel getModel(NodeOrderer orderer, boolean withLayout, boolean withAnnotations);
	
	void setAnnotationModule(AnnotationModule newAnnotationModule);
	
	Annotator<NodeInfo> getAnnotator();


	/***********************/
	/*** STATEFUL GRAPHS ***/
	/***********************/
	
	/**
	 * Checks whether the graph has states associated
	 * @return true if the graph maintains a set of initial states
	 */
	boolean isStateful();
	
	/**
	 * Checks whether the graph has oracles associated
	 * @return true if the graph has oracles defined
	 */
	boolean hasOracles();
	
    /**
     * Accesses the initial states associated with the graph
     * @return a list of states
     */
    List<byte[]> getStates();
    
    /**
     * Accesses the oracles associated with the graph
     * @return a list of oracles
     */
    List<List<byte[]>> getOracles();
    
	/**
	 * Associates a set of initial states to the graph
	 * @param list the list of states
	 */
	void setStates(List<byte[]> list);
	
	/**
	 * Associates a set of oracles to the graph
	 * @param list the list of oracles
	 */
	void setOracles(List<List<byte[]>> list);

}
