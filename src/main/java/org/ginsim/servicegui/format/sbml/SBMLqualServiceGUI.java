package org.ginsim.servicegui.format.sbml;

import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.service.FormatSupportServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.shell.actions.ExportAction;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialogHandler;
import org.ginsim.service.format.sbml.SBMLQualConfig;
import org.ginsim.service.format.sbml.SBMLqualService;
import org.kohsuke.MetaInfServices;

@MetaInfServices(ServiceGUI.class)
@GUIFor(SBMLqualService.class)
@ServiceStatus( EStatus.RELEASED)
public class SBMLqualServiceGUI extends FormatSupportServiceGUI<SBMLqualService> {

	private static SBMLqualService SERVICE = GSServiceManager.getService(SBMLqualService.class);
	public static final FileFormatDescription FORMAT = new FileFormatDescription("SBML", "sbml");

	public SBMLqualServiceGUI() {
		super("SBML-qual", SERVICE, FORMAT);
	}
	
	public StackDialogHandler getConfigPanel(ExportAction action, RegulatoryGraph graph) {
		SBMLQualConfig config = new SBMLQualConfig(graph);
		return new SBMLQualExportConfigPanel(config, action);
	}

	@Override
	public int getInitialWeight() {
		return 1;
	}

}
