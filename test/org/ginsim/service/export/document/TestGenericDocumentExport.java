package org.ginsim.service.export.document;

import java.io.File;
import java.io.IOException;

import org.ginsim.TestFileUtils;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.ServiceManager;
import org.ginsim.service.export.documentation.DocumentExportConfig;
import org.ginsim.service.export.documentation.LRGDocumentationService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGenericDocumentExport {
	static RegulatoryGraph graph;
	static File tmpDir = TestFileUtils.getTempTestFileDirectory("DocumentationExport");
	
	@BeforeClass
	public static void init() {
		File file = new File(TestFileUtils.getTestFileDir(), "graph.ginml");
		graph = TestFileUtils.loadGraph(file);
	}
	
	@Test
	public void testGenericDocument() {
		LRGDocumentationService export =  ServiceManager.get(LRGDocumentationService.class);
		String filename = tmpDir.getAbsolutePath()+File.separator+"graph.html";
		
		DocumentExportConfig config = new DocumentExportConfig();
		config.exportInitStates = false;
		config.exportMutants = false;
		try {
			export.run(graph, config, filename);
		} catch (IOException e) {
			Assert.fail("Error during export: "+e.getMessage());
		}
	}
}
