package org.ginsim.core.graph.regulatorygraph;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.filechooser.FileFilter;

import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.annotation.BiblioManager;
import org.ginsim.core.exception.GsException;
import org.ginsim.core.graph.common.AbstractGraph;
import org.ginsim.core.graph.common.Edge;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.omdd.OMDDNode;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.io.parser.GinmlHelper;
import org.ginsim.core.notification.Notification;


import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.regulatorygraph.mutant.MutantListManager;
import org.ginsim.gui.resource.Translator;
import org.ginsim.gui.shell.GsFileFilter;


/**
 * The regulatory graph
 */
public interface RegulatoryGraph extends Graph<RegulatoryNode, RegulatoryMultiEdge>{
	
    /**
     * Return the node order
     * 
     * @return the node order as a list of RegulatoryNode
     */
    public List<RegulatoryNode> getNodeOrder();
    
    /**
     * add a node from textual parameters (for the parser).
     *
     * @param id
     * @param name
     * @param max
     * @return the new node.
     */
    public RegulatoryNode addNewNode(String id, String name, byte max);
    
    /**
     * 
     * @return
     */
    public RegulatoryNode addNode();
    
    
    /**
     * 
     * @param newId
     * @return True if a node of the graph has the given ID
     */
    public boolean idExists(String newId);
    
    /**
     * add an edge from textual parameters (for the parser).
     * @param from
     * @param to
     * @param minvalue
     * @param maxvalue
     * @param sign
     * @return the new edge.
     */
    public RegulatoryEdge addNewEdge(String from, String to, byte minvalue, byte sign) throws GsException;
    
    /**
     * add an edge from textual parameters (for the parser).
     * @param from
     * @param to
     * @param minvalue
     * @param maxvalue
     * @param sign
     * @return the new edge
     */
    public RegulatoryEdge addNewEdge(String from, String to, byte minvalue, String sign)  throws GsException;
    
    
    /**
     * Add a signed edge
     * 
     * @param source
     * @param target
     * @param sign
     * @return
     */
    public RegulatoryMultiEdge addEdge(RegulatoryNode source, RegulatoryNode target, int sign);
    
    
    /**
     * @param focal if true, leaves are focal points. Otherwise their are directions (-1, 0, +1)
     * @return a tree representation of logical parameters
     */
    public OMDDNode[] getAllTrees(boolean focal);

    /**
     * Computes the tree representing the logical parameters, receiving an optional node ordering
     * (otherwise uses the one already defined in the regulatory graph)
     *  
     * @param focal if true, leaves are focal points. Otherwise their are directions (-1, 0, +1)
     * @return a tree representation of logical parameters
     */
    public OMDDNode[] getAllTrees(List<RegulatoryNode> nodeOrder, boolean focal);

    
    /**
     * 
     * @param focal
     * @return
     */
	public OMDDNode[] getParametersForSimulation(boolean focal);
	
    
    /**
     * 
     * @param node
     * @param newId
     * @throws GsException
     */
    public void changeNodeId(Object node, String newId) throws GsException;
    
    /**
     * 
     * @return
     */
	public List getNodeOrderForSimulation();
	
	
	/**
	 * Set a list of class dependent objects representing the order of node as defined by the model
	 * 
	 * @param list the list of objects representing the order of node as defined by the model
	 */
	public void setNodeOrder( List<RegulatoryNode> list);
	
	
    /**
     * 
     * @param node
     * @param newMax
     * @param l_fixable
     * @param l_conflict
     */
	public void canApplyNewMaxValue(RegulatoryNode node, byte newMax, List l_fixable, List l_conflict);
    
}
