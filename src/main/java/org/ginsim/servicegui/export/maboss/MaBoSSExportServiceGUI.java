package org.ginsim.servicegui.export.maboss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

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
import org.ginsim.service.format.MaBoSSExportService;
import org.kohsuke.MetaInfServices;

/**
 * GUI Action to export model to the MaBoSS format
 * 
 * @author Aurelien Naldi
 */
@MetaInfServices(ServiceGUI.class)
@GUIFor(MaBoSSExportService.class)
@ServiceStatus( EStatus.RELEASED)
public class MaBoSSExportServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<Action>();
			actions.add(new MaBoSSExportAction((RegulatoryGraph) graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return+ 6;
	} //+ 50  W_EXPORT_SPECIFIC
}

/**
 * Export to MaBoSS file Action
 * 
 * @author Aurelien Naldi
 * 
 */
class MaBoSSExportAction extends ExportAction<RegulatoryGraph> {

	private static final FileFormatDescription FORMAT = new FileFormatDescription("MaBoSS", "bnd");

	public MaBoSSExportAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
		super(graph, "Others/MaBoSS", "Export to MaBoSS format", serviceGUI);
	}

	@Override
	protected void doExport(String filename) throws Exception {

		MaBoSSExportService service = GSServiceManager.getService(
				MaBoSSExportService.class);
		service.export(graph.getModel(), filename);
	}

	@Override
	public FileFormatDescription getFileFilter() {
		return FORMAT;
	}
}
