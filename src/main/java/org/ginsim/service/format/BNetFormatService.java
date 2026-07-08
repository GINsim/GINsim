package org.ginsim.service.format;

import org.colomoto.biolqm.io.bnet.BNetFormat;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.FormatSupportService;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.ServiceStatus;
import org.kohsuke.MetaInfServices;

/**
 * GINsim export service for the BoolNet format.
 * 
 * @author Aurelien Naldi
 */
@MetaInfServices(Service.class)
@Alias("bnet")
@ServiceStatus(EStatus.DEVELOPMENT)
public class BNetFormatService extends FormatSupportService<BNetFormat> {

	/**
	 * Constructor
	 */
	public BNetFormatService() {
		super(new BNetFormat());
	}
}
