package org.ginsim.service.format;

import org.colomoto.biolqm.io.boolsim.BoolSimFormat;
import org.ginsim.core.service.*;
import org.kohsuke.MetaInfServices;

/**
 * GINsim export service for the boolsim format.
 * 
 * @author Aurelien Naldi
 */
@MetaInfServices(Service.class)
@Alias("boolsim")
@ServiceStatus(EStatus.DEVELOPMENT)
public class BoolsimFormatService extends FormatSupportService<BoolSimFormat> {

	public BoolsimFormatService() {
		super(new BoolSimFormat());
	}

}
