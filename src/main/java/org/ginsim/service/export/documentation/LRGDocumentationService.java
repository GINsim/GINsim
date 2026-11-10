package org.ginsim.service.export.documentation;

import org.ginsim.common.document.GenericDocumentFormat;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.ServiceStatus;
import org.kohsuke.MetaInfServices;

@MetaInfServices( Service.class)
@Alias("documentation")
@ServiceStatus(EStatus.RELEASED)
public class LRGDocumentationService implements Service {

	public void run(RegulatoryGraph graph, GenericDocumentFormat format, String filename) throws Exception {
		LRGDocumentationWriter writer = new LRGDocumentationWriter(graph);
		writer.export(format, filename);
	}
	
	public void export(RegulatoryGraph graph, String filename) throws Exception {
		JSONDocumentationWriter writer = new JSONDocumentationWriter(graph, filename);
		writer.call();
	}

}
