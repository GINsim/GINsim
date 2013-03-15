package org.ginsim.service.format.sbml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.io.sbml.SBMLFormat;
import org.colomoto.logicalmodel.io.sbml.SBMLQualBundle;
import org.colomoto.logicalmodel.io.sbml.SBMLqualExport;
import org.colomoto.logicalmodel.io.sbml.SBMLqualImport;
import org.ginsim.core.graph.regulatorygraph.LogicalModel2RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.FormatSupportService;
import org.ginsim.core.service.Service;
import org.mangosdk.spi.ProviderFor;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.Position;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;

@ProviderFor( Service.class)
@Alias("SBML")
public class SBMLqualService extends FormatSupportService<SBMLFormat> {

	public SBMLqualService() {
		super(new SBMLFormat());
	}

	/**
	 * Return the graph built from the SBML file at the given path
	 * 
	 * @param filename the path of the SBML file describing the graph
	 * @return the graph built from the SBML file at the given path
	 */
	public RegulatoryGraph importLRG( String filename) {

		try {
			SBMLqualImport simport = new SBMLqualImport(new File(filename));
			LogicalModel model = simport.getModel();
			RegulatoryGraph lrg = LogicalModel2RegulatoryGraph.importModel(model);
			
			SBMLQualBundle qbundle = simport.getQualBundle();
			
			// TODO: add unused interactions and consistency checks
			
			// add layout information
			if (qbundle.lmodel != null) {
				ListOf<Layout> layouts = qbundle.lmodel.getListOfLayouts();
				if (layouts != null && layouts.size() > 0) {
					Layout layout = layouts.get(0);
					NodeAttributesReader nreader = lrg.getNodeAttributeReader();
					List<RegulatoryNode> nodes = lrg.getNodeOrder();
					for (SpeciesGlyph glyph: layout.getListOfSpeciesGlyphs()) {
						String sid = glyph.getSpecies();
						try {
							nreader.setNode( nodes.get( simport.getIndexForName(sid)));
							BoundingBox bb = glyph.getBoundingBox();
							Point pos = bb.getPosition();
							if (pos != null) {
								nreader.setPos((int)pos.getX(), (int)pos.getY());
							}
							Dimensions dim = bb.getDimensions();
							if (dim != null) {
								nreader.setSize((int)dim.getWidth(), (int)dim.getHeight());
							}
						} catch (Exception e) {
							
						}
					}
				}
			}
			
			return lrg;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Convenience method to export without having to configure anything.
	 * 
	 * @param graph
	 * @param filename
	 * @throws IOException
	 */
	public void export( RegulatoryGraph graph, String filename) throws IOException {
		export(graph, new SBMLQualConfig(graph), filename);
	}

	
	/**
	 * Perform the SBML export, using the JSBML-based encoder from LogicalModel.
	 * 
	 * @param graph the graph to export
	 * @param config the configuration structure
	 * @param filename the path to the target file
	 * @throws IOException
	 */
	public void export( RegulatoryGraph graph, SBMLQualConfig config, String filename) throws IOException{
		LogicalModel model = graph.getModel();
		OutputStream out = new FileOutputStream(new File(filename));
		try {
			SBMLqualExport sExport = new SBMLqualExport(model);
			SBMLQualBundle qbundle = sExport.getSBMLBundle();
			
			// add basic layout information
			if (qbundle.lmodel != null) {
				Layout layout = new Layout();
				qbundle.lmodel.addLayout(layout);
				NodeAttributesReader nreader = graph.getNodeAttributeReader();
				for (RegulatoryNode node: graph.getNodeOrder()) {
					nreader.setNode(node);
					SpeciesGlyph glyph = new SpeciesGlyph();
					glyph.setSpecies(sExport.getSpecies(node.getNodeInfo()).getId());
					BoundingBox bb = new BoundingBox();
					Position pos = bb.createPosition();
					pos.setX(nreader.getX());
					pos.setY(nreader.getY());
					Dimensions dim = bb.createDimensions();
					dim.setWidth(nreader.getWidth());
					dim.setHeight(nreader.getHeight());
					glyph.setBoundingBox(bb);
					layout.add(glyph);
				}
			}
			
			sExport.export(out);
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

}