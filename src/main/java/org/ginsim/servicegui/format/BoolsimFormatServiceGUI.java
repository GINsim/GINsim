package org.ginsim.servicegui.format;

import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.service.FormatSupportServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.service.format.BoolsimFormatService;
import org.kohsuke.MetaInfServices;

/**
 * GUI Action to export model to the boolsim format
 * 
 * @author Aurelien Naldi
 */
@MetaInfServices(ServiceGUI.class)
@GUIFor(BoolsimFormatService.class)
@ServiceStatus( EStatus.RELEASED)
public class BoolsimFormatServiceGUI extends FormatSupportServiceGUI<BoolsimFormatService> {

	private static BoolsimFormatService SERVICE = GSServiceManager.getService(BoolsimFormatService.class);
	private static FileFormatDescription FORMAT = new FileFormatDescription("Boolsim", "net");
	
	public BoolsimFormatServiceGUI() {
		super("Boolsim", SERVICE, FORMAT);
	}

	@Override
	public int getInitialWeight() {
		return 3;
	}

	
}
