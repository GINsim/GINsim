package org.ginsim.core.graph.regulatorygraph.perturbation;

import java.io.IOException;
import java.util.Map;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.modifier.perturbation.InteractionPerturbation;
import org.ginsim.common.xml.XMLWriter;

public class PerturbationRegulator extends InteractionPerturbation implements Perturbation {

	public PerturbationRegulator(NodeInfo regulator, NodeInfo target, int value) {
		super(regulator, target, value);
	}

	@Override
	public void toXML(XMLWriter out) throws IOException {
        out.openTag("regulatorChange");
        out.addAttr("regulator", regulator.getNodeID());
        out.addAttr("target", target.getNodeID());
        out.addAttr("value", ""+regValue);
        out.closeTag();
	}

	@Override
	public Perturbation clone(ListOfPerturbations manager, Map<NodeInfo, NodeInfo> m_nodes, Map<Perturbation, Perturbation> m_perturbations) {
		NodeInfo newTarget = m_nodes.get(target);
		NodeInfo newRegulator = m_nodes.get(regulator);
		if (newTarget != null && newRegulator != null) {
			return manager.addRegulatorPerturbation(newRegulator, newTarget, regValue);
		}
		return null;
	}

	@Override
	public String getDescription() {
		return "The component "+ target.getNodeID() + " will behave as if its regulator " + regulator +" was fixed at value " + regValue;
	}
}
