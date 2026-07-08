package org.ginsim.core.graph.regulatorygraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.NodeInfoHolder;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.operators.MDDBaseOperators;
import org.ginsim.common.application.Txt;
import org.ginsim.common.utils.ToolTipsable;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.annotation.Annotation;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.GraphChangeType;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameter;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameterList;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeInteractionsModel;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeElement;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeExpression;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeString;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeValue;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.notification.resolvable.NotificationResolution;


/**
 * A node in the Regulatory graph, representing a regulatory component.
 * A component has:
 * <ul>
 *		<li> a maxValue</li>
 *		<li> an annotation (comment and URLs)</li>
 *		<li> a dynamical behaviour defined by logical parameters or functions.</li>
 * </ul>
 *
 * @author Aurelien Naldi.
 */
public class RegulatoryNode implements ToolTipsable, NodeInfoHolder {

	private final NodeInfo nodeInfo;

	private boolean isOutput = true;
	private final LogicalParameterList v_logicalParameters = new LogicalParameterList();

	private TreeInteractionsModel interactionsModel;

    private static final String S_ID   = "STR_id";
    private static final String S_NAME = "STR_name";
    private static final String S_MAX  = "STR_max";

    public static final int MAXVALUE = 9;

	/**
	 * Constructs an empty vector and set the baseValue (0) and the maxValue (1)
	 * @param id
	 */
	public RegulatoryNode(String id, RegulatoryGraph graph) {
		this.nodeInfo = new NodeInfo(id, (byte)1);
		interactionsModel = new TreeInteractionsModel(graph);
	}

	/**
	 * @param num number of the gene.
	 */
	public RegulatoryNode(int num, RegulatoryGraph graph) {
		this("G"+num, graph);
	}

    public boolean isInput() {
        return nodeInfo.isInput();
    }
    public void setInput(boolean input, Graph graph) {
        if (input != isInput()) {
            nodeInfo.setInput(input);
            graph.fireGraphChange( GraphChangeType.NODEUPDATED, this);
        }
    }

    public boolean isOutput() {
        return !isInput() && isOutput;
    }
    public void setOutput(boolean output, Graph graph) {
        if (output != this.isOutput) {
            this.isOutput = output;
            graph.fireGraphChange( GraphChangeType.NODEUPDATED, this);
        }
    }

	/**
	 * @return the max value of the node
	 */
	public byte getMaxValue() {
		return nodeInfo.getMax();
	}

	/**
	 * Sets the max value to the node
	 * @param max the new max value
	 * @param graph the graph (to propagate changes if needed)
	 */
	public void setMaxValue(byte max, RegulatoryGraph graph) {
    	byte oldmax = nodeInfo.getMax();
	    if (max>0 && max<= MAXVALUE && max != oldmax) {
	    	if (oldmax > max) {
	    		List l_fixable = new ArrayList();
	    		List l_conflict = new ArrayList();
	    		List l_parameters = new ArrayList();
			    if (!getInteractionsModel().isMaxCompatible(max)) {
			    	NotificationManager.publishError( this, "Max value (" + max + ") is inconsistent with some boolean function value.");
			    	return;
			    }
	    		graph.canApplyNewMaxValue(this, max, l_fixable, l_conflict);
	    		v_logicalParameters.applyNewMaxValue(max, graph, l_parameters);
	    		if (l_fixable.size() > 0 || l_conflict.size() > 0 || l_parameters.size() > 0) {

	    			// Define the options of the Notification Resolution
	    			String[] option_names;
	    			if (l_conflict.size() == 0) {
	    				option_names = new String[2];
	    				option_names[1] = "Fix";
	    			} else {
	    				option_names = new String[1];
	    			}
	    			option_names[0] = "Detail";
	    			
	    			// Create the notification resolution
	    			NotificationResolution resolution = new NotificationResolution( option_names){
	    				
	    				public boolean perform( Graph graph, Object[] data, int index){
	    					RegulatoryNode node = (RegulatoryNode) data[0];
	    					byte max = ((Integer) data[1]).byteValue();
	    					List conflict = (List) data[2];
	    					List fixable = (List) data[3];
	    					List parameters = (List) data[4];
	    					
	    					if (index == 1) {
	    						if (conflict.size() > 0) {
	    							return false;
	    						}
	    						Iterator it = fixable.iterator();
	    						while (it.hasNext()) {
	    							((RegulatoryMultiEdge)it.next()).applyNewMaxValue(max);
	    						}
	    						it = parameters.iterator();
	    						while (it.hasNext()) {
	    							((LogicalParameter)it.next()).setValue( max, graph);
	    						}
	    						node.setMaxValue(max, (RegulatoryGraph) graph);
	    						return true;
	    					}
	    					return true;
	    				}
	    			};
	    			
	    			// Create the Notification
	    			Object[] data = new Object[5];
	    			data[0] = this;
	    			data[1] = Integer.valueOf( max);
					data[2] = l_conflict;
	    			data[3] = l_fixable;
	    			data[4] = l_parameters;
	    			
	    			NotificationManager.publishResolvableWarning( this, "max value decrease is blocked", graph, data, resolution);
	    			
	    			return;
	    		}
	    	}
	    	nodeInfo.setMax(max);
    		graph.fireGraphChange( GraphChangeType.NODEUPDATED, this);
    		getInteractionsModel().refreshNode();
	    }
	}

	/**
	 * @return the id of this node.
	 */
	public String getId() {
	    return nodeInfo.getNodeID();
	}

	/**
	 * change the id of this node.
	 * @param id the new id.
	 */
	public boolean setId(String id, RegulatoryGraph graph) {
		if (XMLWriter.isValidId(id)) {
			nodeInfo.setNodeID(id);
			graph.fireGraphChange(GraphChangeType.NODEUPDATED, this);
			return true;
		}
		return false;
	}
	/**
	 * Adds the specified interaction I to the interactions of the node
	 * @param I
	 * @return true if the logical parameter has been added
	 */
	public boolean addLogicalParameter (LogicalParameter I, boolean manual) {
		return v_logicalParameters.addLogicalParameter(I, manual);
	}

	/**
	 * Removes the specified interaction to the interactions of the node
	 * @param I
	 */
	public void removeInteraction (LogicalParameter I) {
		v_logicalParameters.remove(I);
	}

	/**
	 * Returns the interaction at the specified position index
	 * @param index
	 * @return LogicalParameter
	 */
	public LogicalParameter getInteraction(int index) {
		try
		{
			return v_logicalParameters.get(index);
		}
		catch (java.lang.ArrayIndexOutOfBoundsException e)
		{
			return null;
		}
	}

	/**
	 * change an interaction's list of edges: we have to check that the new list of edges
	 * is not in an existing interaction.
	 *
	 * @param index
	 * @param edges
	 */
	public void updateInteraction(int index, List edges) {
		v_logicalParameters.updateInteraction(index, edges);
	}

	/**
	 * Returns the number of the interaction in the node.
	 * @return the number of user defined interactions on this node
	 */
	public int interactionCount() {
		return v_logicalParameters.getRealSize();
	}

	/**
	 * get the long name of the node.
	 * @return the name
	 */
	public String getName() {
		return nodeInfo.getName();
	}

	/**
	 * set the name of the gene.
	 * @param name the new name
	 */
	public void setName(String name, RegulatoryGraph graph) {
		nodeInfo.setName(name);
		graph.fireGraphChange(GraphChangeType.NODEUPDATED, this);
	}

	/**
	 * @return the list of all interactions on this gene.
	 */
	public LogicalParameterList getV_logicalParameters() {
		return v_logicalParameters;
	}
	/**
	 * Experimental: Get the MDD for this node function using the new MDD toolkit.
	 * 
	 * @param factory
	 * @return
	 */
	public int getMDD(RegulatoryGraph graph, MDDManager factory) {
        if (isInput()) {
        	MDDVariable level = factory.getVariableForKey(getNodeInfo());
        	if (getMaxValue() == 1) {
        		return level.getNode(0, 1);
        	}
        	int[] children = new int[getMaxValue()+1];
            for (int i=0 ; i<children.length ; i++) {
                children[i] = i;
            }
    		return level.getNode(children);
        }

        if (v_logicalParameters.size() == 0) {
        	return 0;
        }
        int[] parameters = new int[v_logicalParameters.size()];
        int i = 0;
        for (LogicalParameter gsi: v_logicalParameters) {
            parameters[i++] = gsi.getMDD(graph, this, factory);
        }
        int result = MDDBaseOperators.OR.combine(factory, parameters);
		// free intermediate results
		for (int n: parameters) {
			factory.free(n);
		}
		return result;
	}

	public String toToolTip() {
		return    Txt.t(S_ID) + ":" + getId() + "|"
				+ Txt.t(S_NAME) + ":" + getName() + "|"
                + Txt.t(S_MAX) + ":" + getMaxValue()
                + (isOutput() ? "(output)" : "");
	}

	public void toXML(XMLWriter out) throws IOException {

	    	out.addAttr("id", getId());
	    	String name = getName();
			if (name != null && name.length() > 0) {
			    out.addAttr("name", name);
			}
		    out.addAttr("maxvalue", ""+getMaxValue());
		    
		    if (isInput()) {
	            out.addAttr("input", ""+isInput());
		    }	    
		    // TODO: at some point stop saving logical parameters
		 	LogicalParameterList lpl = this.getV_logicalParameters();
    	    for (LogicalParameter lp: lpl) {
		 		if(lpl.isManual(lp))
		 			 lp.toXML(out);		 			    
		 	} 
		    // save logical function
		 	saveInteractionsModel(out);
	}

	public RegulatoryNode clone(RegulatoryGraph graph) {
		RegulatoryNode clone = new RegulatoryNode(nodeInfo.getNodeID(), graph);
		clone.nodeInfo.setMax(nodeInfo.getMax());
		clone.setName(getName(), graph);
		clone.setInput(isInput(), graph);
		return clone;
	}
	
	/**
	 * update logical parameters when an interaction is deleted.
	 */
	public void cleanupInteraction() {
		v_logicalParameters.cleanupInteraction();
	}


	/**
	 * when an incoming edge is deleted, we don't want to see it appear in interactions anymore.
	 * This will remove a subedge.
	 * NOTE: ALL interaction involving the deleted subedge will be suppressed.
	 * @param multiEdge
	 * @param index
	 */
	public void removeEdgeFromInteraction(RegulatoryMultiEdge multiEdge, int index) {
		cleanupInteraction();
		interactionsModel.removeEdge(multiEdge, index);
	}

	/**
	 * when an incoming edge is deleted, we don't want to see it appear in interactions anymore.
	 * This will remove all subedges of a multiedge.
	 * NOTE: ALL interaction involving the deleted multiedge will be suppressed.
	 * @param multiEdge
	 */
	public void removeEdgeFromInteraction(RegulatoryMultiEdge multiEdge) {
		cleanupInteraction();
		interactionsModel.removeEdge(multiEdge);
	}

    public String toString() {
        return getId();
    }

    public String getDisplayName() {
		return nodeInfo.getDisplayName();
	}

    /**
     * @param copyMap
     */
    public void cleanupInteractionForNewGraph(Map copyMap) {
        RegulatoryNode myClone = (RegulatoryNode) copyMap.get(this);
        for (LogicalParameter lp: v_logicalParameters) {
            lp.applyNewGraph(myClone, copyMap);
            // TODO: copy the logical functions as well
            // if pasted into a new graph, the "interactionModel" should be
            // recreated/updated for the new graph
        }
    }

    public void setInteractionsModel(TreeInteractionsModel model) {
      interactionsModel = model;
      v_logicalParameters.setFunctionParameters(interactionsModel.getLogicalParameters());
    }

    public TreeInteractionsModel getInteractionsModel() {
      return interactionsModel;
    }

    public void saveInteractionsModel(XMLWriter out) throws IOException {
      TreeString root = (TreeString)interactionsModel.getRoot();
      TreeValue val;
      TreeElement exp;

      for (int i = 0; i < root.getChildCount(); i++) {
        val = (TreeValue)root.getChild(i);
        out.openTag("value");
        out.addAttr("val", ""+val.getValue());
        for (int j = 0; j < val.getChildCount(); j++) {
          exp = val.getChild(j);
          if (exp instanceof TreeExpression) {
            out.openTag("exp");
            ((TreeExpression)exp).refreshRoot();
            out.addAttr("str", exp.toString());
            out.closeTag();
          }
        }
        out.closeTag();
      }
    }
    public void incomingEdgeAdded(RegulatoryMultiEdge me) {
      interactionsModel.addEdge(me);
    }
    
	/**
	 * Compare the object to the given one, using the internal NodeInfo.
	 */
	@Override
	public boolean equals( Object obj) {
		return nodeInfo.equals(obj);
	}

	@Override
	public int hashCode() {
		return nodeInfo.hashCode();
	}

	@Override
	public NodeInfo getNodeInfo() {
		return nodeInfo;
	}

}

