package org.ginsim.servicegui.format;

import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.service.FormatSupportServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.service.format.TruthTableFormatService;
import org.kohsuke.MetaInfServices;

/**
 * GUI Action to import create a model specified in the TruthTable format
 * 
 * @author Pedro T. Monteiro
 */
@MetaInfServices(ServiceGUI.class)
@GUIFor(TruthTableFormatService.class)
@ServiceStatus(EStatus.RELEASED)
public class TruthTableFormatServiceGUI extends
		FormatSupportServiceGUI<TruthTableFormatService> {

	private static TruthTableFormatService SERVICE = GSServiceManager
			.getService(TruthTableFormatService.class);
	private static FileFormatDescription FORMAT = new FileFormatDescription(
			"TruthTable", "tt");

	public TruthTableFormatServiceGUI() {
		super("Truth table", SERVICE, FORMAT);
	}
	@Override
	public int getInitialWeight() {
		return 5;
	}

}
