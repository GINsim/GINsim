package org.ginsim.service.tool.reg2dyn.updater;

import java.util.List;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.ginsim.common.xml.XMLize;
import org.ginsim.core.utils.data.NamedObject;

public interface UpdaterDefinition extends NamedObject, XMLize {

	boolean USE_BIOLQM_UPDATERS = true;

	SimulationUpdater getUpdater(LogicalModel model);

	String summary(List<NodeInfo> nodeOrder);
}
