package org.ginsim.service.format;

import org.colomoto.biolqm.io.maboss.MaBoSSFormat;
import org.ginsim.core.service.*;
import org.kohsuke.MetaInfServices;

/**
 * GINsim export service capable of encoding the working model into the MaBoSS format
 * <p>
 * It only writes the variables and 
 * </p>
 * 
 * @author Aurelien Naldi
 */
@MetaInfServices(Service.class)
@Alias("maboss")
@ServiceStatus(EStatus.RELEASED)
public class MaBoSSExportService extends FormatSupportService<MaBoSSFormat> {

	public MaBoSSExportService() {
		super(new MaBoSSFormat());
	}


}
