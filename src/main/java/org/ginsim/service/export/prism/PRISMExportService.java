package org.ginsim.service.export.prism;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.ginsim.common.application.GsException;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.ServiceStatus;
import org.kohsuke.MetaInfServices;

@MetaInfServices(Service.class)
@Alias("PRISM")
@ServiceStatus(EStatus.DEVELOPMENT)
public class PRISMExportService implements Service {

	public void run(PRISMConfig config, String filename) throws IOException,
			GsException {
		File f = new File(filename);
		export(config, f);
	}

	public void export(PRISMConfig config, File file) throws IOException,
			GsException {
		FileWriter writer = new FileWriter(file);

		PRISMEncoder encoder = new PRISMEncoder();
		encoder.write(config, writer);

		writer.close();
	}
}
