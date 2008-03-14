package fr.univmrs.tagc.GINsim.reg2dyn;

import java.util.List;

import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraph;
import fr.univmrs.tagc.common.datastore.SimpleGenericList;


public class PriorityClassManager extends SimpleGenericList {

	List nodeOrder;
	
	public PriorityClassManager(GsRegulatoryGraph graph) {
		this.nodeOrder = graph.getNodeOrder();
		canAdd = true;
		canOrder = true;
		canRemove = true;
		canEdit = true;
		enforceUnique = true;
		prefix = "priorities_";
	}
	
	public Object doCreate(String name) {
		return new PriorityClassDefinition(nodeOrder.iterator(), name);
	}
	public Object doCreate(String name, int pos) {
		return doCreate(name);
	}
}