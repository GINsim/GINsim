package fr.univmrs.tagc.GINsim.reg2dyn;

import java.util.ArrayList;
import java.util.List;

import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraph;
import fr.univmrs.tagc.common.datastore.SimpleGenericList;


public class PriorityClassManager extends SimpleGenericList {

	List nodeOrder;
	public static final String FILTER_NO_SYNCHRONOUS = "[no-synchronous]";
	
	public PriorityClassManager(GsRegulatoryGraph graph) {
		this.nodeOrder = graph.getNodeOrder();
		canAdd = true;
		canOrder = true;
		canRemove = true;
		canEdit = true;
		enforceUnique = true;
		prefix = "priorities_";
		addOptions = new ArrayList();
		addOptions.add("One unique class");
		addOptions.add("One class for each node");
		addOptions.add("Splitting transitions – one unique class");

		// add default priority classes
		int index = add();
		PriorityClassDefinition pcdef = (PriorityClassDefinition)getElement(null, index);
		pcdef.setName("Asynchronous");
		GsReg2dynPriorityClass pc = (GsReg2dynPriorityClass)pcdef.getElement(null, 0);
		pc.setName("all");
		pc.setMode(GsReg2dynPriorityClass.ASYNCHRONOUS);
		pcdef.lock();
		index = add();
		pcdef = (PriorityClassDefinition)getElement(null, index);
		pcdef.setName("Synchronous");
		pc = (GsReg2dynPriorityClass)pcdef.getElement(null, 0);
		pc.setName("all");
		pc.setMode(GsReg2dynPriorityClass.SYNCHRONOUS);
		pcdef.lock();
	}
	
	public Object doCreate(String name, int mode) {
		PriorityClassDefinition pcdef = new PriorityClassDefinition(nodeOrder.iterator(), name);
        Object lastClass = pcdef.v_data.get(0);
        GsReg2dynPriorityClass currentClass;
        switch (mode) {
            case 1:
                // should be equivalent to the old priority system: add one class per node
            	pcdef.v_data.clear();
            	pcdef.m_elt.clear();
                for (int i=0 ; i<nodeOrder.size() ; i++) {
                    currentClass = new GsReg2dynPriorityClass();
                    pcdef.v_data.add(i, currentClass);
                    pcdef.m_elt.put(nodeOrder.get(i), currentClass);
                    currentClass.setName(""+nodeOrder.get(i));
                }
                break;
            case 2:
                for (int i=0 ; i<nodeOrder.size() ; i++) {
                    Object[] t = {lastClass, lastClass};
                    pcdef.m_elt.put(nodeOrder.get(i), t);
                }
                break;
        }
        return pcdef;
    }

	public Object doCreate(String name, int pos, int mode) {
		return doCreate(name, mode);
	}
	public boolean remove(String filter, int[] t_index) {
		if (t_index.length < 1 || getRealIndex(filter, t_index[0]) < 2) {
			return false;
		}
		return super.remove(filter, t_index);
	}
	
    protected void doMoveUp(int[] selection, int diff) {
    	if (selection.length < 1 || selection[0] < 3) {
    		return;
    	}
    	super.doMoveUp(selection, diff);
    }
    protected void doMoveDown(int[] selection, int diff) {
    	if (selection.length < 1 || selection[0] < 2) {
    		return;
    	}
    	super.doMoveDown(selection, diff);
    }
	public boolean match(String filter, Object o) {
		if (filter != null && filter.startsWith(FILTER_NO_SYNCHRONOUS)) {
			PriorityClassDefinition pcdef = (PriorityClassDefinition)o;
			
			int l = pcdef.getNbElements();
			boolean hasSync = false;
			for (int i=0 ; i<l ; i++) {
				GsReg2dynPriorityClass pc = (GsReg2dynPriorityClass)pcdef.getElement(null, i);
				if (pc.getMode() == GsReg2dynPriorityClass.SYNCHRONOUS) {
					hasSync = true;
					break;
				}
			}
			if (hasSync) {
				return false;
			}
			String realfilter = filter.substring(FILTER_NO_SYNCHRONOUS.length()).trim();
			if (realfilter.length() == 0) {
				return true;
			}
			return super.match(realfilter, o);
		}
		return super.match(filter, o);
	}
}
