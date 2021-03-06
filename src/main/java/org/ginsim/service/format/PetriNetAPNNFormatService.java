package org.ginsim.service.format;

import org.colomoto.biolqm.io.petrinet.APNNFormat;
import org.ginsim.core.service.*;
import org.kohsuke.MetaInfServices;

/**
 * GINsim export service for the APNN Petri net format.
 * 
 * @author Aurelien Naldi
 */
@MetaInfServices(Service.class)
@Alias("APNN")
@ServiceStatus(EStatus.RELEASED)
public class PetriNetAPNNFormatService extends FormatSupportService<APNNFormat> {

	public PetriNetAPNNFormatService() {
		super(new APNNFormat());
	}
}
