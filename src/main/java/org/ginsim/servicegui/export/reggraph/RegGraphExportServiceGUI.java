package org.ginsim.servicegui.export.reggraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.shell.actions.ExportAction;
import org.ginsim.service.export.reggraph.RegGraphExportService;
import org.kohsuke.MetaInfServices;

import static org.ginsim.gui.service.ServiceGUI.W_EXPORT_GRAPH;

/**
 * GUI Action to export the regulatory graph interactions and corresponding
 * signs.
 * 
 * @author Pedro T. Monteiro
 */
@MetaInfServices(ServiceGUI.class)
@GUIFor(RegGraphExportService.class)
@ServiceStatus(EStatus.RELEASED)
public class RegGraphExportServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<Action>();
			actions.add(new RegGraphExportAction((RegulatoryGraph) graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_EXPORT_GRAPH + 4;
	}
}

/**
 * Export to regulatory graph file Action
 * 
 * @author Pedro T. Monteiro
 */
class RegGraphExportAction extends ExportAction<RegulatoryGraph> {

	private static final long serialVersionUID = -3615904375655037276L;
	private static final FileFormatDescription FORMAT = new FileFormatDescription(
			"Regulatory graph", "reggraph");

	public RegGraphExportAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
		super(graph, "STR_RegGraph", "STR_RegGraph_descr", serviceGUI);
	}

	@Override
	public FileFormatDescription getFileFilter() {
		return FORMAT;
	}

	@Override
	protected void doExport(String filename) throws IOException {

		RegGraphExportService service = GSServiceManager.getService(
				RegGraphExportService.class);
		service.export(graph, filename);
	}
}
