package org.ginsim.servicegui.export.sat;

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
import org.ginsim.service.export.sat.SATConfig;
import org.ginsim.service.export.sat.SATExportService;
import org.kohsuke.MetaInfServices;

/**
 * GINsim export service capable of encoding the model properties into a SAT
 * specification in CNF.
 * 
 * @author Pedro T. Monteiro
 */
@MetaInfServices(ServiceGUI.class)
@StandaloneGUI
@ServiceStatus(EStatus.RELEASED)
public class SATExportServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<Action>();
			actions.add(new SATExportAction((RegulatoryGraph) graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_EXPORT_GENERIC + 3;
	}
}

/**
 * Export to SAT file Action
 * 
 * @author Pedro T. Monteiro
 */
class SATExportAction extends ExportAction<RegulatoryGraph> {

	private static final long serialVersionUID = -3615904375655037276L;
	private static final FileFormatDescription FORMAT = new FileFormatDescription("SAT", "cnf");

	private SATConfig config;

	public SATExportAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
		super(graph, "STR_SAT", "STR_SAT_descr", serviceGUI);
	}

	@Override
	public FileFormatDescription getFileFilter() {
		return FORMAT;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		config = new SATConfig(graph);
		new SATExportConfigPanel(config, this);
	}

	@Override
	protected void doExport(String filename) throws Exception {
		if (config == null) {
			throw new GsException(GsException.GRAVITY_ERROR, "Nothing to export: SATConfig must be specified");
		}
		if (config.getGraph() == null || config.getGraph().getNodes().size() == 0) {
			throw new GsException(GsException.GRAVITY_ERROR, "Nothing to export: The graph is empty");
		}

		SATExportService service = GSServiceManager.getService(SATExportService.class);
		if (service == null) {
			throw new GsException(GsException.GRAVITY_ERROR, "SATExportService service is not available");
		}
		service.run(config, filename);
	}
}
