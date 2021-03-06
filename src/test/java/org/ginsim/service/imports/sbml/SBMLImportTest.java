package org.ginsim.service.imports.sbml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Color;
import java.io.File;

import org.ginsim.TestFileUtils;
import org.ginsim.common.application.OptionStore;
import org.ginsim.core.graph.backend.EdgeAttributeReaderImpl;
import org.ginsim.core.graph.backend.NodeAttributeReaderImpl;
import org.ginsim.core.graph.regulatorygraph.BasicRegulatoryGraphTest;
import org.ginsim.core.graph.view.NodeBorder;
import org.ginsim.core.graph.view.NodeShape;
import org.ginsim.service.format.sbml.SBMLqualService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SBMLImportTest {

	private static final String module = "SBMLImport";
	
	
	/**
	 * Initialize the OPtion store and define the test file directory
	 * 
	 */
	@BeforeAll
	public static void beforeAllTests(){
		
		try {
			OptionStore.init( BasicRegulatoryGraphTest.class.getPackage().getName());
	    	OptionStore.getOption( EdgeAttributeReaderImpl.EDGE_COLOR, new Integer(-13395457));
	    	OptionStore.getOption( NodeAttributeReaderImpl.VERTEX_BG, new Integer(-26368));
	    	OptionStore.getOption( NodeAttributeReaderImpl.VERTEX_FG, new Integer(Color.WHITE.getRGB()));
	    	OptionStore.getOption( NodeAttributeReaderImpl.VERTEX_HEIGHT, new Integer(30));
	    	OptionStore.getOption( NodeAttributeReaderImpl.VERTEX_WIDTH, new Integer(55));
	    	OptionStore.getOption( NodeAttributeReaderImpl.VERTEX_SHAPE, NodeShape.RECTANGLE.name());
	    	OptionStore.getOption( NodeAttributeReaderImpl.VERTEX_BORDER, NodeBorder.SIMPLE.name());
		} catch (Exception e) {
			fail( "Initialisation of OptionStore failed : " + e);
		}
		
	}
	
	/**
	 * Test to import a graph from a predefined SBML file 
	 * 
	 */
	@Test
	public void importSBMLGraphTest(){
		
		File file = new File( TestFileUtils.getTestFileDirectory( module), "importGraphTest.sbml");
		
		SBMLqualService srv = new SBMLqualService();
		// FIXME: old test disabled: need some changes in the new SBML import
/*		
		RegulatoryGraph graph = srv.run(file.getPath());
		
		assertNotNull( "Import graph : graph is null", graph);
		assertEquals( "Import graph : Graph node number is not correct", 4, graph.getNodeCount());
		assertEquals( "Import graph : Graph edge number is not correct", 7, graph.getEdges().size());
*/
		
	}

}
