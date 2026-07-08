package org.ginsim.servicegui.export.nusmv;

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
import org.ginsim.service.export.nusmv.NuSMVConfig;
import org.ginsim.service.export.nusmv.NuSMVExportService;
import org.kohsuke.MetaInfServices;

/**
 * GUI Action to export a NuSMV model description
 * 
 * @author Pedro T. Monteiro
 */
@MetaInfServices(ServiceGUI.class)
@StandaloneGUI
@ServiceStatus(EStatus.RELEASED)
public class NuSMVExportServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<Action>();
			actions.add(new NuSMVExportAction((RegulatoryGraph) graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_EXPORT_GENERIC + 1;
	}
}

/**
 * Export to NuSMV file Action
 * 
 * @author Pedro T. Monteiro
 */
class NuSMVExportAction extends ExportAction<RegulatoryGraph> {

	private static final long serialVersionUID = -3615904375655037276L;
	private static final FileFormatDescription FORMAT = new FileFormatDescription(
			"NuSMV", "smv");

	private NuSMVConfig config;

	public NuSMVExportAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
		super(graph, "STR_NuSMV", "STR_NuSMV_descr", serviceGUI);
	}

	@Override
	public FileFormatDescription getFileFilter() {
		return FORMAT;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		config = new NuSMVConfig(graph.getModel());
		new NuSMVExportConfigPanel(this.graph, config, this);
	}

	@Override
	protected void doExport(String filename) throws GsException, IOException {
		if (config == null) {
			throw new GsException(GsException.GRAVITY_ERROR,
					"Nothing to export: NuSMVConfig must be specified");
		}
		if (config.getModel() == null
				|| config.getModel().getComponents().size() == 0) {
			throw new GsException(GsException.GRAVITY_ERROR,
					"Nothing to export: The graph is empty");
		}

		NuSMVExportService service = GSServiceManager.getService(NuSMVExportService.class);
		if (service == null) {
			throw new GsException(GsException.GRAVITY_ERROR,
					"NuSMVExportService service is not available");
		}
		service.run(config, filename);
	}
}
