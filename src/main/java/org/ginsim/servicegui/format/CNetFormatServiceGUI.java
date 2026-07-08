package org.ginsim.servicegui.format;

import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.service.FormatSupportServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.service.format.CNetFormatService;
import org.kohsuke.MetaInfServices;

/**
 * GUI Action to export model to the CNET format
 * 
 * @author Aurelien Naldi
 */
@MetaInfServices(ServiceGUI.class)
@GUIFor(CNetFormatService.class)
@ServiceStatus( EStatus.RELEASED)
public class CNetFormatServiceGUI extends FormatSupportServiceGUI<CNetFormatService> {

	private static CNetFormatService SERVICE = GSServiceManager.getService(CNetFormatService.class);
	private static FileFormatDescription FORMAT = new FileFormatDescription("CNET (bns tool)", "cnet");
	
	public CNetFormatServiceGUI() {
		super("Others/CNET (bns tool)", SERVICE, FORMAT);
	}

	@Override
	public int getInitialWeight() {
		return 7;//W_EXPORT_SPECIFIC + 40
	}
	
}
