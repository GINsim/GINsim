package org.ginsim.service.tool.reg2dyn;

import java.util.Collection;

import org.ginsim.core.graph.GraphManager;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.core.graph.common.GraphChangeType;
import org.ginsim.core.graph.common.GraphEventCascade;
import org.ginsim.core.graph.common.GraphListener;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.initialstate.GsInitialStateList;
import org.ginsim.core.graph.regulatorygraph.initialstate.InitialStateManager;
import org.ginsim.core.graph.regulatorygraph.mutant.MutantListManager;
import org.ginsim.core.graph.regulatorygraph.mutant.Perturbation;
import org.ginsim.core.graph.regulatorygraph.mutant.RegulatoryMutants;
import org.ginsim.core.utils.data.GenericListListener;
import org.ginsim.core.utils.data.SimpleGenericList;
import org.ginsim.service.tool.reg2dyn.priorityclass.PriorityClassDefinition;
import org.ginsim.service.tool.reg2dyn.priorityclass.PriorityClassManager;


/**
 * store all simulation parameters and offer a mean to access them.
 * Also deals with updating them when the graph is changed
 */
public class SimulationParameterList extends SimpleGenericList<SimulationParameters> 
	implements GraphListener<RegulatoryGraph>, GenericListListener {

    public final RegulatoryGraph graph;
    public final GsInitialStateList imanager;
    public final PriorityClassManager pcmanager;

    /**
     * @param graph
     */
    public SimulationParameterList( Graph graph) {
    	
    	this(graph, null);
    }

    public SimulationParameterList( Graph<RegulatoryNode,RegulatoryMultiEdge> graph, SimulationParameters param) {
    	
        this.graph = (RegulatoryGraph) graph;
        imanager = (GsInitialStateList) ObjectAssociationManager.getInstance().getObject(graph, InitialStateManager.KEY, true);
        imanager.getInitialStates().addListListener(this);
        imanager.getInputConfigs().addListListener(this);
        pcmanager = new PriorityClassManager(this.graph);
    	prefix = "parameter_";
    	canAdd = true;
    	canEdit = true;
    	canRemove = true;
    	canOrder = true;
        GraphManager.getInstance().addGraphListener( this.graph, this);
        RegulatoryMutants mutants = (RegulatoryMutants)  ObjectAssociationManager.getInstance().getObject( graph, MutantListManager.KEY, true);
        mutants.addListListener(this);
        if (param == null) {
        	add();
        } else {
        	add(param,0);
        }
    }

    @Override
	public GraphEventCascade graphChanged(RegulatoryGraph g, GraphChangeType type, Object data) {
		switch (type) {
		case NODEADDED:
			nodeAdded(data);
			break;
		case NODEREMOVED:
	        // remove it from priority classes
	        for (int i=0 ; i<pcmanager.getNbElements(null) ; i++) {
	        	PriorityClassDefinition pcdef = (PriorityClassDefinition)pcmanager.getElement(null, i);
	    		if (pcdef.m_elt != null) {
	    			pcdef.m_elt.remove(data);
	    		}
	        }
			break;
		case GRAPHMERGED:
			Collection<?> nodes = (Collection<?>)data;
			for (Object v: nodes) {
				nodeAdded(v);
			}
			break;

		}
		return null;
	}
	
	private void nodeAdded(Object data) {
        // if needed, add it to the default priority class!
        for (int i=0 ; i<pcmanager.getNbElements(null) ; i++) {
        	PriorityClassDefinition pcdef = (PriorityClassDefinition)pcmanager.getElement(null, i);
    		if (pcdef.m_elt != null) {
    			pcdef.m_elt.put((RegulatoryNode)data, pcdef.getElement(null, 0));
    		}
        }
	}

    public int add(SimulationParameters param, int index) {
    	v_data.add(index, param);
    	return v_data.indexOf(param);
    }
    public int add(SimulationParameters param) {
    	return add(param, v_data.size());
    }

	protected SimulationParameters doCreate(String name, int mode) {
        SimulationParameters parameter = new SimulationParameters(this);
        parameter.name = name;
		return parameter;
	}

	public void itemAdded(Object item, int pos) {
	}

	public void itemRemoved(Object item, int pos) {
		if (item instanceof Perturbation) {
	        for (int i=0 ; i< v_data.size() ; i++) {
	            SimulationParameters param = (SimulationParameters)v_data.get(i);
	            if (param.store.getObject(SimulationParameters.MUTANT) == item) {
	                param.store.setObject(SimulationParameters.MUTANT, null);
	            }
	        }
	        return;
		}
		
		for (int i=0 ; i<v_data.size() ; i++) {
			SimulationParameters param = (SimulationParameters)v_data.get(i);
			if (param.m_initState != null) {
				param.m_initState.remove(item);
			}
		}
	}

	public void contentChanged() {
	}
	public void structureChanged() {
	}
}