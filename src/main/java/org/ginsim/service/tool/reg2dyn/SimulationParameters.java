package org.ginsim.service.tool.reg2dyn;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.NodeInfo;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.common.xml.XMLize;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedState;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateStore;
import org.ginsim.core.graph.regulatorygraph.perturbation.Perturbation;
import org.ginsim.core.utils.data.NamedObject;
import org.ginsim.gui.graph.regulatorygraph.initialstate.InitStateTableModel;
import org.ginsim.service.tool.reg2dyn.priorityclass.PriorityClass;
import org.ginsim.service.tool.reg2dyn.priorityclass.PrioritySetDefinition;
import org.ginsim.service.tool.reg2dyn.priorityclass.PrioritySetList;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinition;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinitionAsynchronous;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinitionComplete;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinitionSynchronous;
import org.ginsim.service.tool.reg2dyn.priorityclass.UpdaterDefinitionStore;


/**
 * remember, save and restore a simulation parameter.
 */
public class SimulationParameters implements XMLize, NamedObject, NamedStateStore, UpdaterDefinitionStore {

	public String name = "new_parameter";

    public int maxdepth;
    public int maxnodes;
    public boolean breadthFirst = false;
    public SimulationStrategy strategy = SimulationStrategy.STG;

    public Map m_initState = new HashMap();
    public Map m_input = new HashMap();
    public SimulationParameterList param_list;

    private UpdaterDefinition updaterDefinition;

    /**
     * empty constructor for everyday use.
     * @param param_list
     */
    public SimulationParameters(SimulationParameterList param_list) {
    	this.param_list = param_list;
        setUpdatingMode(param_list.pcmanager.get(0));
    }

    /**
     * empty constructor without parameter list.
     * @param graph
     */
    public SimulationParameters(RegulatoryGraph graph) {
    	this(new SimulationParameterList(graph));
    }

    public String toString() {
        return name;
    }

    /**
     * Describe this parameter.
     * It shows the full content of this simulation parameter, used to add a useful comment to the state transition graph.
     * @return a human readable description of the parameter
     */
    public String getDescr(List<NodeInfo> nodeOrder) {
        String name = param_list.graph.getGraphName();
        String saveName = GSGraphManager.getInstance().getGraphPath( param_list.graph);
        if (saveName != null) {
            int pos = saveName.lastIndexOf(File.separatorChar);
            if (pos != -1) {
                saveName = saveName.substring(pos+1);
            }
            name += " ("+saveName+")";
        }
        String s = "construction parameters:\n";
        s += "    Regulatory graph: " + name + "\n";
        Perturbation perturbation = param_list.getPerturbation(this);
        if (perturbation != null) {
            s += "    Perturbation: "+perturbation+"\n";
        }
        s += "    Simulation strategy: " + strategy + "\n";
		if (updaterDefinition != null) {
			s += "    Updating policy: " + updaterDefinition.summary(nodeOrder);
		}
        s += "    Initial states: ";
        if (m_initState == null || m_initState.size()==0) {
            s += "ALL\n";
        } else {
            Iterator it = m_initState.keySet().iterator();
            while (it.hasNext()) {
                Map m_init = ((NamedState)it.next()).getMap();
                s += "\n      ";
                for (int j=0 ; j<nodeOrder.size() ; j++) {
                    NodeInfo vertex = nodeOrder.get(j);
                    s += "  "+InitStateTableModel.showValue((List)m_init.get(vertex), vertex.getMax());
                }
            }
            s += "\n";
        }
        // FIXME: proper input/initial states reporting
        s += "    Inputs: ";
        if (m_input == null || m_input.size()==0) {
            s += "ALL\n";
        } else {
            Iterator it = m_input.keySet().iterator();
            while (it.hasNext()) {
                NamedState init = (NamedState)it.next();
                if (init != null) {
                    Map m_init = init.getMap();
                    s += "\n      ";
                    for (int j=0 ; j<nodeOrder.size() ; j++) {
                        NodeInfo vertex = nodeOrder.get(j);
                        s += "  "+InitStateTableModel.showValue((List)m_init.get(vertex), vertex.getMax());
                    }
                }
            }
            s += "\n";
        }

        if (maxdepth != 0) {
            s += "    Max depth: "+maxdepth+"\n";
        }
        if (maxnodes != 0) {
            s += "    Max nodes: "+maxnodes+"\n";
        }
        return s;
    }

    @Override
	public void toXML(XMLWriter out) throws IOException {
    	// avoid failure on unspecified priority class definition
    	UpdaterDefinition pcdef = getPriorityClassDefinition();
		out.openTag("parameter");
		out.addAttr("name", name);
		out.addAttr("updating", pcdef.getName());
		out.addAttr("breadthFirst", ""+breadthFirst);
		out.addAttr("maxdepth", ""+maxdepth);
		out.addAttr("maxnodes", ""+maxnodes);
		out.addAttr("simulationStrategy", ""+strategy);

		if (pcdef instanceof PrioritySetDefinition) {
			out.openTag("priorityClass");
			out.addAttr("ref", pcdef.getName());
			out.closeTag();
		} else {
			
		}
		
        if (m_initState != null && m_initState.keySet().size() > 0) {
            out.openTag("initstates");
            Iterator it = m_initState.keySet().iterator();
            while(it.hasNext()) {
                out.openTag("row");
                out.addAttr("name", ((NamedState)it.next()).getName());
                out.closeTag();
            }
            out.closeTag();
        }
        if (m_input != null && m_input.keySet().size() > 0) {
            out.openTag("inputs");
            Iterator it = m_input.keySet().iterator();
            while(it.hasNext()) {
                out.openTag("row");
                out.addAttr("name", ((NamedState)it.next()).getName());
                out.closeTag();
            }
            out.closeTag();
        }

		out.closeTag();
	}

    @Override
    public SimulationParameters clone() {
    	SimulationParameters newp = new SimulationParameters(param_list);
    	newp.strategy = strategy;
    	newp.name = name;
    	newp.maxdepth = maxdepth;
    	newp.maxnodes = maxnodes;
    	// TODO: transfer the perturbation
        Perturbation perturbation = param_list.getPerturbation(this);
    	newp.setUpdatingMode(updaterDefinition);

    	if (m_initState != null) {
    		newp.m_initState = new HashMap();
    		Iterator it = m_initState.keySet().iterator();
    		while (it.hasNext()) {
    			newp.m_initState.put(it.next(), null);
    		}
    	}
    	return newp;
    }

    @Override
	public String getName() {
		return name;
	}

    @Override
	public void setName(String name) {
		ObjectAssociationManager.getInstance().fireUserUpdate(param_list.graph, Reg2DynService.KEY, this.name, name);
		this.name = name;
	}

    @Override
	public Map getInitialState() {
		return m_initState;
	}
    @Override
    public Map getInputState() {
        return m_input;
    }

	public UpdaterDefinition getPriorityClassDefinition() {
        return getUpdatingMode();
	}

    public void copy_to(SimulationParameters other, Map mapping) {
        other.strategy = this.strategy;
        other.breadthFirst = this.breadthFirst;
        other.maxdepth = this.maxdepth;
        other.maxnodes = this.maxnodes;
        other.name = this.name;
        Iterator it = m_initState.keySet().iterator();
        while (it.hasNext()) {
            other.m_initState.put(mapping.get(it.next()), null);
        }
        it = m_input.keySet().iterator();
        while (it.hasNext()) {
            other.m_input.put(mapping.get(it.next()), null);
        }

        // TODO: get the real associated perturbation
        Perturbation perturbation = null;
        Perturbation o_perturbation = (Perturbation)mapping.get(perturbation);
        UpdaterDefinition new_updater = (UpdaterDefinition)mapping.get(updaterDefinition);
        if (new_updater == null) {
            // FIXME: need a cleaner transposition of updating modes
            PrioritySetList new_pcman = (PrioritySetList)mapping.get("");
            if (updaterDefinition instanceof UpdaterDefinitionAsynchronous) {
            	new_updater = UpdaterDefinitionAsynchronous.DEFINITION;
            } else if (updaterDefinition instanceof UpdaterDefinitionSynchronous) {
            	new_updater = UpdaterDefinitionSynchronous.DEFINITION;
            } else if (updaterDefinition instanceof UpdaterDefinitionComplete) {
            	new_updater = UpdaterDefinitionComplete.DEFINITION;
            } else if (updaterDefinition instanceof PrioritySetDefinition) {
            	PrioritySetDefinition pcdef = (PrioritySetDefinition)updaterDefinition;
	            if (pcdef.size() < 2) {
	                PriorityClass pc = pcdef.get(0);
	                if (pc.getMode() == PriorityClass.SYNCHRONOUS) {
	                    new_updater = UpdaterDefinitionSynchronous.DEFINITION;
	                } else {
	                    new_updater = UpdaterDefinitionAsynchronous.DEFINITION;
	                }
	            }
            }
            
            if (new_updater == null) {
                LogManager.error( "[BUG] complex pcdef not transposed in the reduced model");
                new_updater = (UpdaterDefinition)new_pcman.get(0);
            }
        }
        other.setUpdatingMode(new_updater);
    }

    @Override
    public UpdaterDefinition getUpdatingMode() {
        if (updaterDefinition == null) {
            updaterDefinition = param_list.pcmanager.get(0);
        }
        return updaterDefinition;
    }

    @Override
    public void setUpdatingMode(UpdaterDefinition updef) {
        this.updaterDefinition = updef;
    }

}
