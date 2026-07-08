package org.ginsim.servicegui.format;

import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.service.FormatSupportServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.service.format.BNetFormatService;
import org.kohsuke.MetaInfServices;

/**
 * GUI Action to export model to the BoolNet format
 * 
 * @author Aurelien Naldi
 */
@MetaInfServices(ServiceGUI.class)
@GUIFor(BNetFormatService.class)
@ServiceStatus( EStatus.RELEASED)
public class BoolNetFormatServiceGUI extends FormatSupportServiceGUI<BNetFormatService> {

	private static BNetFormatService SERVICE = GSServiceManager.getService(BNetFormatService.class);
	private static FileFormatDescription FORMAT = new FileFormatDescription("BoolNet", "bnet");
	
	public BoolNetFormatServiceGUI() {
		super("BoolNet", SERVICE, FORMAT);
	}

	@Override
	public int getInitialWeight() {
		return 2;
	}
	
}
