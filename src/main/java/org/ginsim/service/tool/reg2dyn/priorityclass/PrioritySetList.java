package org.ginsim.service.tool.reg2dyn.priorityclass;

import java.util.ArrayList;
import java.util.List;

import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.utils.data.NamedList;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinition;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinitionAsynchronous;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinitionComplete;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinitionSequential;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinitionSynchronous;

/**
 * The list of all available priority set definitions.
 *
 * @author Aurelien Naldi
 */
public class PrioritySetList extends NamedList<UpdaterDefinition> {

	public static final String SYNCHRONOUS = "synchronous", ASYNCHRONOUS = "asynchronous"; 
	
	public final List<RegulatoryNode> nodeOrder;
	
	private final int lockedIndex;

	public PrioritySetList(RegulatoryGraph graph) {
		this.nodeOrder = graph.getNodeOrder();

		// add default updaters
		add(UpdaterDefinitionAsynchronous.DEFINITION);
		add(UpdaterDefinitionSynchronous.DEFINITION);
		add(UpdaterDefinitionComplete.DEFINITION);
		//add(new UpdaterDefinitionSequential());
		
		this.lockedIndex = size();
	}

    public int addDefinition(Object mode) {
        PrioritySetAddMode addMode = PrioritySetAddMode.SIMPLE;
        if (mode instanceof PrioritySetAddMode) {
            addMode = (PrioritySetAddMode)mode;
        }
        String name = findUniqueName("Priorities ");

		PrioritySetDefinition pcdef = new PrioritySetDefinition(nodeOrder, name);
        PriorityClass lastClass = pcdef.get(0);
        PriorityClass currentClass;
        switch (addMode) {
            case SPLIT:
                // should be equivalent to the old priority system: add one class per node
            	pcdef.clear();
            	pcdef.m_elt.clear();
                for (RegulatoryNode node: nodeOrder) {
                    currentClass = pcdef.get(pcdef.add());
                    pcdef.associate(node, currentClass);
                    currentClass.setName(node.getId());
                }
                break;
            case FINE_GRAINED:
                for (RegulatoryNode node: nodeOrder) {
                    pcdef.associate(node, lastClass, lastClass);
                }
                break;
        }

        add(pcdef);
        return size()-1;
    }

    public boolean canMoveItems(int[] selection) {
    	if (selection.length < 1) {
    		return false;
    	}

        // check that the default updaters are not selected
        for (int i: selection) {
            if (i<lockedIndex) {
                return false;
            }
        }
        return true;
    }

    public boolean removeSelection(int[] sel) {
        List<UpdaterDefinition> toRemove = new ArrayList<UpdaterDefinition>();
        for (int i: sel) {
            if (i < lockedIndex) {
                return false;
            }
            toRemove.add(get(i));
        }
        return removeAll(toRemove);
    }
}
