package org.ginsim.service.export.document;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.swing.SwingUtilities;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.metadata.Annotator;
import org.colomoto.biolqm.metadata.annotations.Annotation;
import org.colomoto.biolqm.metadata.annotations.URI;
import org.ginsim.TestFileUtils;
import org.ginsim.common.document.GenericDocumentFormat;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryEdgeSign;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.service.export.documentation.LRGDocumentationService;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.annotation.classes.AnnotationsComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

class TestGenericDocumentExport {
	@TempDir
	Path outputDir;

	private RegulatoryGraph loadGraph() {
		return TestFileUtils.loadGraph(Path.of("src/test/resources/DocumentationExport/graph.ginml").toFile());
	}

	private static void annotate(Annotator<NodeInfo> annotator, String prefix) {
		annotator.setNotes(prefix + " notes & <text>\nsecond line");
		Annotation annotation = annotator.openBlock("description").getSelectedAnnotation();
		annotation.uris.add(new URI(null, "https://example.org/" + prefix));
		assertTrue(annotator.annotate("pubmed:12345"));
		annotation.tags.add(prefix + "Tag");
		annotation.keyValues.put(prefix + "Key", prefix + "Value");
	}

	@Test
	void exportsStructuredAnnotationsInBothFormats() throws Exception {
		RegulatoryGraph graph = loadGraph();
		RegulatoryNode source = graph.getNodeByName("G0");
		RegulatoryNode target = graph.getNodeByName("G1");
		RegulatoryMultiEdge edge = graph.getEdge(source, target);
		assertNotNull(edge);
		annotate(graph.getAnnotator().onModel(), "model");
		annotate(graph.getAnnotator().node(source.getNodeInfo()), "node");
		annotate(graph.getAnnotator().edge(source.getNodeInfo(), target.getNodeInfo()), "edge");
		Annotation extra = graph.getAnnotator().onModel().openBlock("source").getSelectedAnnotation();
		extra.keyValues.put("key with spaces", "value & details");
		graph.addEdge(source, source, RegulatoryEdgeSign.POSITIVE);
		annotate(graph.getAnnotator().edge(source.getNodeInfo(), source.getNodeInfo()), "self");

		Path saved = outputDir.resolve("structured.zginml");
		graph.save(saved.toString());
		RegulatoryGraph reopened = TestFileUtils.loadGraph(saved.toFile());
		List<URI> uris = reopened.getAnnotator().onModel().annotations().get(0).uris;
		assertEquals("https://example.org/model", uris.get(0).uri());
		assertEquals("pubmed:12345", uris.get(1).uri());

		for (RegulatoryGraph candidate : new RegulatoryGraph[] {graph, reopened}) {
			for (GenericDocumentFormat format : GenericDocumentFormat.getAllFormats()) {
				Path output = outputDir.resolve("model." + format.extension);
				LRGDocumentationService service = GSServiceManager.getService(LRGDocumentationService.class);
				assertNotNull(service);
				service.run(candidate, format, output.toString());
				assertTrue(Files.size(output) > 0);
				Document document = readDocument(output, format);
				String text = document.getDocumentElement().getTextContent();
				for (String prefix : new String[] {"model", "node", "edge", "self"}) {
					assertTrue(text.contains(prefix + " notes & <text>"), prefix + " notes");
					assertTrue(text.contains("https://example.org/" + prefix), prefix + " URI");
					assertTrue(text.contains(prefix + "Tag"), prefix + " tag");
					assertTrue(text.contains(prefix + "Key: " + prefix + "Value"), prefix + " key/value");
					assertLink(document, "https://example.org/" + prefix);
				}
				assertTrue(text.contains("description"));
				assertTrue(text.contains("source"));
				assertTrue(text.contains("key with spaces: value & details"));
				assertTrue(text.contains("pubmed:12345"));
				assertTrue(text.contains("G0"));
				assertTrue(text.contains("Logical function"));
			}
		}
	}

	@Test
	void exportsLegacyModelAnnotations() throws Exception {
		RegulatoryGraph graph = TestFileUtils.loadGraph(
				Path.of("data/p53Mdm2_tutorial_5march2018.zginml").toFile());
		for (GenericDocumentFormat format : GenericDocumentFormat.getAllFormats()) {
			Path output = outputDir.resolve("legacy." + format.extension);
			new LRGDocumentationService().run(graph, format, output.toString());
			String text = readDocument(output, format).getDocumentElement().getTextContent();
			assertTrue(text.contains("Ternary component"), "legacy node notes");
			assertTrue(text.contains("16455486"), "legacy first node link");
			assertTrue(text.contains("9153395"), "legacy second node link");
			assertTrue(text.contains("Translocation of Mdm2"), "legacy edge notes");
			assertLink(readDocument(output, format), "http://identifiers.org/pubmed/16455486");
		}
	}

	@Test
	void retainsLegacyLinksAfterSavingAndReopening() throws Exception {
		String oldModel = Files.readString(Path.of("src/test/resources/DocumentationExport/graph.ginml"))
				.replace("<node id=\"G0\" maxvalue=\"1\">",
						"<node id=\"G0\" maxvalue=\"1\"><annotation><linklist>"
						+ "<link xlink:href=\"unknown:one\"/><link xlink:href=\"unknown:two\"/>"
						+ "<link xlink:href=\"relative/path\"/><link xlink:href=\"hugo:6973\"/>"
						+ "</linklist><comment>legacy node</comment></annotation>")
				.replace("<edge id=\"G0:G1\" from=\"G0\" to=\"G1\" minvalue=\"1\" sign=\"positive\">",
						"<edge id=\"G0:G1\" from=\"G0\" to=\"G1\" minvalue=\"1\" sign=\"positive\">"
						+ "<annotation><linklist><link xlink:href=\"edge:reference\"/></linklist>"
						+ "<comment>legacy edge</comment></annotation>")
				.replace("</graph>", "<annotation><linklist><link xlink:href=\"model:reference\"/>"
						+ "</linklist><comment>legacy model</comment></annotation></graph>");
		Path input = outputDir.resolve("old.ginml");
		Files.writeString(input, oldModel);
		RegulatoryGraph graph = TestFileUtils.loadGraph(input.toFile());
		Annotator<NodeInfo> nodeAnnotations = graph.getAnnotator().node(graph.getNodeByName("G0").getNodeInfo());
		SwingUtilities.invokeAndWait(() -> new AnnotationsComponent<>(nodeAnnotations, true));
		assertTrue(nodeAnnotations.annotations().get(0).uris.stream()
				.anyMatch(uri -> uri.uri().endsWith(":6973")), "GUI must retain legacy identifiers");
		Path saved = outputDir.resolve("saved.zginml");
		graph.save(saved.toString());
		RegulatoryGraph reopened = TestFileUtils.loadGraph(saved.toFile());
		for (RegulatoryGraph candidate : new RegulatoryGraph[] {graph, reopened}) {
			for (GenericDocumentFormat format : GenericDocumentFormat.getAllFormats()) {
				Path output = outputDir.resolve("legacy-roundtrip." + format.extension);
				new LRGDocumentationService().run(candidate, format, output.toString());
				String text = readDocument(output, format).getDocumentElement().getTextContent();
				for (String value : new String[] {"legacy node", "legacy edge", "legacy model",
						"unknown:one", "unknown:two", "relative/path", "edge:reference", "model:reference"}) {
					assertTrue(text.contains(value), value);
				}
			}
		}
	}

	@Test
	void reportsFileCreationErrors() {
		RegulatoryGraph graph = loadGraph();
		Path output = outputDir.resolve("missing/model.xhtml");
		assertThrows(java.io.IOException.class, () -> new LRGDocumentationService()
				.run(graph, GenericDocumentFormat.XHTMLDocumentFormat, output.toString()));
	}

	@Test
	void reportsImageCreationErrors() throws Exception {
		Path output = outputDir.resolve("model.xhtml");
		Files.writeString(outputDir.resolve("model.xhtml_FILES"), "not a directory");
		assertThrows(java.io.IOException.class, () -> new LRGDocumentationService()
				.run(loadGraph(), GenericDocumentFormat.XHTMLDocumentFormat, output.toString()));
	}

	@Test
	void escapesImagePathsInHtml() throws Exception {
		Path output = outputDir.resolve("model #1?.xhtml");
		new LRGDocumentationService().run(loadGraph(), GenericDocumentFormat.XHTMLDocumentFormat, output.toString());
		Document document = readDocument(output, GenericDocumentFormat.XHTMLDocumentFormat);
		var image = (org.w3c.dom.Element) document.getElementsByTagNameNS("*", "img").item(0);
		java.net.URI source = new java.net.URI(image.getAttribute("src"));
		assertNull(source.getFragment());
		assertNull(source.getQuery());
		assertTrue(Files.exists(outputDir.resolve(source.getPath())));
	}

	@Test
	void exportsAnEmptyModel() throws Exception {
		loadGraph();
		RegulatoryGraph graph = GSGraphManager.getInstance().getNewGraph(RegulatoryGraph.class);
		for (GenericDocumentFormat format : GenericDocumentFormat.getAllFormats()) {
			Path output = outputDir.resolve("empty." + format.extension);
			new LRGDocumentationService().run(graph, format, output.toString());
			assertTrue(Files.size(output) > 0);
		}
	}

	private static void assertLink(Document document, String expected) {
		var links = document.getElementsByTagNameNS("*", "a");
		for (int i = 0; i < links.getLength(); i++) {
			var element = (org.w3c.dom.Element) links.item(i);
			if (expected.equals(element.getAttribute("href"))
					|| expected.equals(element.getAttributeNS("http://www.w3.org/1999/xlink", "href"))) {
				return;
			}
		}
		fail("Missing hyperlink: " + expected);
	}

	private static Document readDocument(Path path, GenericDocumentFormat format) throws Exception {
		byte[] xml;
		if (format == GenericDocumentFormat.OOoDocumentFormat) {
			try (ZipFile zip = new ZipFile(path.toFile());
				 InputStream stream = zip.getInputStream(zip.getEntry("content.xml"))) {
				assertNotNull(zip.getEntry("Pictures/model.png"));
				assertEquals("mimetype", zip.entries().nextElement().getName());
				assertEquals(java.util.zip.ZipEntry.STORED, zip.getEntry("mimetype").getMethod());
				try (InputStream manifest = zip.getInputStream(zip.getEntry("META-INF/manifest.xml"))) {
					String contents = new String(manifest.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
					assertTrue(contents.contains("Pictures/model.png"));
					assertTrue(contents.contains("image/png"));
				}
				xml = stream.readAllBytes();
			}
		} else {
			assertTrue(Files.exists(path.resolveSibling(path.getFileName() + "_FILES/model.png")));
			xml = Files.readAllBytes(path);
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
		if (format == GenericDocumentFormat.OOoDocumentFormat) {
			var frames = document.getElementsByTagNameNS(
					"urn:oasis:names:tc:opendocument:xmlns:drawing:1.0", "frame");
			assertEquals(1, frames.getLength());
			var frame = (org.w3c.dom.Element) frames.item(0);
			assertFalse(frame.getAttributeNS(
					"urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0", "width").isEmpty());
		}
		return document;
	}
}
