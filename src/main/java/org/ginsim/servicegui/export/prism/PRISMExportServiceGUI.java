package org.ginsim.servicegui.export.prism;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.common.application.GsException;
import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.shell.actions.ExportAction;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.service.StandaloneGUI;
import org.ginsim.service.export.prism.PRISMConfig;
import org.ginsim.service.export.prism.PRISMExportService;
import org.kohsuke.MetaInfServices;

/**
 * GUI Action to export a PRISM model description
 * 
 * @author Pedro T. Monteiro
 */
@MetaInfServices(ServiceGUI.class)
@StandaloneGUI
@ServiceStatus(EStatus.RELEASED)
public class PRISMExportServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<Action>();
			actions.add(new PRISMExportAction((RegulatoryGraph) graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_EXPORT_GENERIC + 2;
	}
}

/**
 * Export to PRISM file Action
 * 
 * @author Pedro T. Monteiro
 */
class PRISMExportAction extends ExportAction<RegulatoryGraph> {

	private static final long serialVersionUID = -3615904375655037276L;
	private static final FileFormatDescription FORMAT = new FileFormatDescription(
			"PRISM", "prism");

	private PRISMConfig config;

	public PRISMExportAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
		super(graph, "STR_PRISM", "STR_PRISM_descr", serviceGUI);
	}

	@Override
	public FileFormatDescription getFileFilter() {
		return FORMAT;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		config = new PRISMConfig(graph);
		new PRISMExportConfigPanel(config, this);
	}

	@Override
	protected void doExport(String filename) throws GsException, IOException {
		if (config == null) {
			throw new GsException(GsException.GRAVITY_ERROR,
					"Nothing to export: PRISMConfig must be specified");
		}
		if (config.getGraph() == null
				|| config.getGraph().getNodes().size() == 0) {
			throw new GsException(GsException.GRAVITY_ERROR,
					"Nothing to export: The graph is empty");
		}

		PRISMExportService service = GSServiceManager.getService(
				PRISMExportService.class);
		if (service == null) {
			throw new GsException(GsException.GRAVITY_ERROR,
					"PRISMExportService service is not available");
		}
		service.run(config, filename);
	}
}
