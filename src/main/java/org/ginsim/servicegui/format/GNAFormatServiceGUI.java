package org.ginsim.servicegui.format;

import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.service.FormatSupportServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.service.format.GNAFormatService;
import org.mangosdk.spi.ProviderFor;

/**
 * GUI Action to export model to the GNA (non-xml) format
 * 
 * @author Pedro T. Monteiro
 */
@ProviderFor(ServiceGUI.class)
@GUIFor(GNAFormatService.class)
@ServiceStatus(EStatus.RELEASED)
public class GNAFormatServiceGUI extends
		FormatSupportServiceGUI<GNAFormatService> {

	private static GNAFormatService SERVICE = GSServiceManager.getService(GNAFormatService.class);
	private static FileFormatDescription FORMAT = new FileFormatDescription(
			"GNA", "gna");

	public GNAFormatServiceGUI() {
		super("GNA (non-xml)", SERVICE, FORMAT);
	}

}
