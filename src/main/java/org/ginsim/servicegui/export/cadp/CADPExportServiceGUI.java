package org.ginsim.servicegui.export.cadp;

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
import org.ginsim.gui.utils.dialog.stackdialog.StackDialogHandler;
import org.ginsim.service.export.cadp.CADPExportConfig;
import org.ginsim.service.export.cadp.CADPExportService;
import org.kohsuke.MetaInfServices;

/**
 * CADP Export Service
 * 
 * @author Nuno D. Mendes
 */

@MetaInfServices(ServiceGUI.class)
@StandaloneGUI
@ServiceStatus(EStatus.RELEASED)
public class CADPExportServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<Action>();
			actions.add(new CADPExportAction((RegulatoryGraph) graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_EXPORT_GENERIC + 4;// + 40
	}
}

class CADPExportAction extends ExportAction<RegulatoryGraph> {
	private static final long serialVersionUID = -8586197112178912230L;
	private static final FileFormatDescription FORMAT = new FileFormatDescription(
			"CADP", "bundle");

	private CADPExportConfig config;

	public CADPExportAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
		super(graph, "STR_CADP", "STR_CADP_descr", serviceGUI);
	}

	@Override
	protected FileFormatDescription getFileFilter() {
		return FORMAT;
	}

	@Override
	public StackDialogHandler getConfigPanel() {
		config = new CADPExportConfig(graph);
		return new CADPExportConfigPanel(config, this);

	}

	@Override
	protected void doExport(String filename) throws GsException, IOException {
		if (config == null) {
			throw new GsException(GsException.GRAVITY_ERROR,
					"Nothing to export: CADPConfig must be specified");
		}
		if (config.getGraph() == null
				|| config.getGraph().getNodes().size() == 0) {
			throw new GsException(GsException.GRAVITY_ERROR,
					"Nothing to export: The graph is empty");
		}

		CADPExportService service = GSServiceManager.getService(CADPExportService.class);
		if (service == null) {
			throw new GsException(GsException.GRAVITY_ERROR,
					"CADPExportService service is not available");
		}
		service.run(config, filename);

	}
}
