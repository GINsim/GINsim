package org.ginsim.service.tool.modelreduction;

import java.util.List;

import org.colomoto.logicalmodel.NodeInfo;

public interface ReductionLauncher {

	boolean showPartialReduction(List<NodeInfo> l_todo);
}