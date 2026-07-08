package org.ginsim.service.tool.avatar.params;

import java.util.Collection;

import org.ginsim.core.graph.*;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesHandler;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesManager;
import org.ginsim.core.utils.data.GenericListListener;
import org.ginsim.core.utils.data.NamedList;
import org.ginsim.service.tool.reg2dyn.priorityclass.PriorityClass;
import org.ginsim.service.tool.reg2dyn.priorityclass.PrioritySetDefinition;
import org.ginsim.service.tool.reg2dyn.priorityclass.PrioritySetList;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinition;

/**
 * Stores the context of simulations (parameters and changes to the input model)
 * @author Rui Henriques
 * @version 1.0
 */
public class AvatarParameterList extends NamedList<AvatarParameters> implements GraphListener<RegulatoryGraph>, GenericListListener {
	
	private static final long serialVersionUID = 1L;
	private final RegulatoryGraph graph;
    private final PrioritySetList pcmanager;
    private final NamedStatesHandler imanager;

    /**
	 * Constructor
     * @param graph a graph
	 * @param param  parameter AvatarParameters
     */
    public AvatarParameterList(Graph<RegulatoryNode,RegulatoryMultiEdge> graph, AvatarParameters param) {
        this.graph = (RegulatoryGraph) graph;
        imanager = (NamedStatesHandler) ObjectAssociationManager.getInstance().getObject(graph, NamedStatesManager.KEY, true);
        imanager.getInitialStates().addListListener(this);
        imanager.getInputConfigs().addListListener(this);
        pcmanager = new PrioritySetList(this.graph);
        GSGraphManager.getInstance().addGraphListener(this.graph, this);
        if(param!=null) add(param);
    }
	
	@Override
	public String toString(){
		String result = "";
		for(AvatarParameters param : this)
			result += param.toFullString()+"\n";
		return result;
	}

    /***********************
     **** GRAPH CHANGES ****
     ***********************/
    
    /* (non-Javadoc)
     * @see org.ginsim.core.graph.GraphListener#graphChanged(org.ginsim.core.graph.GraphModel, org.ginsim.core.graph.GraphChangeType, java.lang.Object)
     */
    @Override
	public GraphEventCascade graphChanged(RegulatoryGraph g, GraphChangeType type, Object data) {
		switch (type) {
		case NODEADDED: nodeAdded((RegulatoryNode)data); break;
		case NODEREMOVED: nodeRemoved((RegulatoryNode)data); break;
		case GRAPHMERGED:
			Collection<?> items = (Collection<?>)data;
			for (Object item: items) {
				if(item instanceof RegulatoryNode) {
					nodeAdded((RegulatoryNode)item);
				}
			}
			break;
		}
		return null;
	}
	
    private void nodeRemoved(RegulatoryNode data) {
    	pcmanager.nodeOrder.remove(data); // remove it from priority classes
        for (int i=0 ; i<pcmanager.size() ; i++) {
        	UpdaterDefinition updater = pcmanager.get(i);
        	// TODO: future types of updaters may need some treatment as well
        	if (updater instanceof PrioritySetDefinition) {
        		PrioritySetDefinition pcdef = (PrioritySetDefinition)updater;
        		pcdef.removeNode(data);
        	}
        }
    }
    
	private void nodeAdded(RegulatoryNode node) {
		if (pcmanager.nodeOrder.contains(node)) {
			return;
		}
		pcmanager.nodeOrder.add(node);
        for (int i=0 ; i<pcmanager.size() ; i++) {
        	UpdaterDefinition updater = pcmanager.get(i);
        	// TODO: future types of updaters may need some treatment as well
        	if (updater instanceof PrioritySetDefinition) {
        		PrioritySetDefinition pcdef = (PrioritySetDefinition)updater;
    			PriorityClass cl = pcdef.get(0);
    			pcdef.associate(node, cl);
        	}
        }
	}

	@Override
	public void itemAdded(Object item, int pos) {}
	@Override
	public void itemRemoved(Object item, int pos) {}
	@Override
	public void contentChanged() {}
	@Override
	public void structureChanged() {}
}
