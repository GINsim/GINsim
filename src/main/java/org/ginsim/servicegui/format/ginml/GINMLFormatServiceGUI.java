package org.ginsim.servicegui.format.ginml;

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
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.service.format.ginml.GINMLFormatConfig;
import org.ginsim.service.format.ginml.GINMLFormatService;
import org.kohsuke.MetaInfServices;

/**
 * GINsim export service GUI for the GINML (model-only) format.
 * 
 * @author Pedro T. Monteiro
 */
@MetaInfServices(ServiceGUI.class)
@GUIFor(GINMLFormatService.class)
@ServiceStatus(EStatus.RELEASED)
public class GINMLFormatServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<Action>();
			actions.add(new GINMLFormatAction((RegulatoryGraph) graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return 4; // W_EXPORT_SPECIFIC
	}
}

/**
 * Export to GINML file Action
 * 
 * @author Pedro T. Monteiro
 */
class GINMLFormatAction extends ExportAction<RegulatoryGraph> {

	private static final long serialVersionUID = -3615904375655037276L;
	private static final FileFormatDescription FORMAT = new FileFormatDescription(
			"GINML", "ginml");

	private GINMLFormatConfig config;

	public GINMLFormatAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
		super(graph, "STR_GINML_Title", "STR_GINML_Descr", serviceGUI);
	}

	@Override
	public FileFormatDescription getFileFilter() {
		return FORMAT;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		config = new GINMLFormatConfig(graph);
		new GINMLFormatConfigPanel(config, this);
	}

	@Override
	protected void doExport(String filename) throws Exception {
		if (config == null) {
			throw new GsException(GsException.GRAVITY_ERROR,
					"Nothing to export: GINMLFormatConfig must be specified");
		}
		if (config.getGraph() == null
				|| config.getGraph().getNodes().size() == 0) {
			throw new GsException(GsException.GRAVITY_ERROR,
					"Nothing to export: The graph is empty");
		}

		GINMLFormatService service = GSServiceManager.getService(GINMLFormatService.class);
		if (service == null) {
			throw new GsException(GsException.GRAVITY_ERROR,
					"GINMLFormatService service is not available");
		}
		service.run(config, filename);
	}
}