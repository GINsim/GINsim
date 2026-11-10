package org.ginsim.service.export.documentation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.metadata.Annotator;
import org.colomoto.biolqm.metadata.annotations.URI;
import org.ginsim.common.document.DocumentStyle;
import org.ginsim.common.document.DocumentWriter;
import org.ginsim.common.document.GenericDocumentFormat;
import org.ginsim.core.annotation.Annotation;
import org.ginsim.core.annotation.AnnotationLink;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameter;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeInteractionsModel;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeValue;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.service.export.image.ImageExportService;

/**
 * Export a regulatory graph as a formatted model documentation report.
 */
public class LRGDocumentationWriter {

    private static ImageExportService SRV_IMAGE = GSServiceManager.getService(ImageExportService.class);

	private DocumentWriter doc;

	private final RegulatoryGraph graph;
    public LRGDocumentationWriter(RegulatoryGraph graph) {
		this.graph = graph;
    }

	public void export( GenericDocumentFormat format, String filename) throws Exception {

		this.doc = format.getWriter();

		File file = new File(filename);
		try (OutputStream output = new FileOutputStream(file)) {
			doc.setOutput(file, output);
			setDocumentProperties();
			setDocumentStyles();
			writeDocument();
		}
	}

	private void writeDocument() throws Exception {
		doc.startDocument();
		doc.openHeader(1, "Description of the model \"" + graph.getGraphName() + "\"", null);

        // add model image
        if (graph.getNodeCount() > 0) {
            doc.addImage(SRV_IMAGE.getPNG(graph, 1), "model.png");
        }

        //Graph annotation
		doc.openHeader(2, "Annotation", null);
		writeAnnotation(graph.getAnnotator().onModel());
		if (!graph.getAnnotation().isEmpty()) {
			writeLegacyAnnotation(graph.getAnnotation());
		}

		// all nodes with comment and logical functions
		doc.openHeader(2, "Nodes", null);
		writeLogicalFunctionsTable();

		doc.close();//close the document
	}

	private void writeLogicalFunctionsTable() throws IOException {
		doc.openTable(null, "table", new String[] { "", "", ""});
		doc.openTableRow(null);
		doc.openTableCell("ID", true);
		doc.openTableCell("Val", true);
		doc.openTableCell("Logical function", true);

		for (RegulatoryNode vertex: graph.getNodeOrder()) {
			TreeInteractionsModel lfunc = vertex.getInteractionsModel();
			int nbval = 0;
			Object funcRoot = null;
			List<List<Object>> t_val = new ArrayList<>(
					Collections.nCopies(vertex.getMaxValue() + 1, null));
			int nbrows = 0;
			if (lfunc != null) {
				funcRoot = lfunc.getRoot();
				nbval = lfunc.getChildCount(funcRoot);
				if (nbval == 0) {
					funcRoot = null;
				}
				// put all values from function
				for (int i=0 ; i<nbval ; i++) {
					TreeValue val = (TreeValue)lfunc.getChild(funcRoot, i);
					int v = val.getValue();
					if (lfunc.getChildCount(val) > 0) {
						if (t_val.get(v) == null) {
							t_val.set(v, new ArrayList<>());
							nbrows++;
						}
						t_val.get(v).add(val);
					}
				}
			}
			// and add logical parameters as well
			Iterator<?> it_param = vertex.getV_logicalParameters().iterator(true);
			while (it_param.hasNext()) {
				LogicalParameter param = (LogicalParameter)it_param.next();
				if (!param.isDup()) {
					int v = param.getValue();
					if (t_val.get(v) == null) {
						t_val.set(v, new ArrayList<>());
						nbrows++;
					}
					t_val.get(v).add(param);
				}
			}
			doc.openTableRow(null);
			int nrows = nbrows > 0 ? nbrows : 1;
			doc.openTableCell(1, nrows+1, vertex.getId(), true); //ID
			if (nbrows > 0) {
				boolean first_value = true;
				for ( int currentValue = 0 ; currentValue<t_val.size() ; currentValue++) {
					if (t_val.get(currentValue) != null) {
						if (first_value) {
							first_value = false;
						} else {
							doc.openTableRow(null);
						}
						doWriteParameters(currentValue, t_val.get(currentValue), lfunc);
					}
				}
			} else {
				String text = "No function";
				if (vertex.isInput()) {
					text = "Input node";
				}
				doc.openTableCell(2, 1, text);
			}

			doc.openTableRow();
			doc.openTableCell(2,1, null, false);
			writeAnnotation(graph.getAnnotator().node(vertex.getNodeInfo()));

			boolean hasRegulatorComment = false;
			for (RegulatoryMultiEdge me: graph.getIncomingEdges(vertex)) {
				Annotator<NodeInfo> annot = graph.getAnnotator().edge(
						me.getSource().getNodeInfo(), me.getTarget().getNodeInfo());
				if (!annot.hasData()) {
					continue;
				}
				if (!hasRegulatorComment) {
					doc.openTable("", "", new String[] {"", ""});
					doc.openTableCell("Regulator", true);
					doc.openTableCell("Comment", true);
					hasRegulatorComment = true;
				}
				doc.openTableRow();
				doc.openTableCell(me.getSource().toString(), true);
				doc.openTableCell(null);
				writeAnnotation(annot);
			}
			if (hasRegulatorComment) {
				doc.closeTable();
			}
			doc.closeTableRow();

		}
		doc.closeTable();
	}

	private void doWriteParameters(int value, List<Object> data, TreeInteractionsModel lfunc) throws IOException {
		doc.openTableCell(""+value); //Values
		doc.openTableCell(null); //logical function
		doc.openList("L1");
		for (Object o : data) {
			if (o instanceof TreeValue) {
				int nbfunc = lfunc.getChildCount(o);
				for (int j=0 ; j<nbfunc ; j++) {
					Object func = lfunc.getChild(o, j);
					doc.openListItem(func.toString());
				}
			} else {
				doc.openListItem(o.toString());
			}
		}
		doc.closeList();
		doc.closeTableCell();
	}

	private void writeAnnotation(Annotator<NodeInfo> annotator) throws IOException {
		String notes = annotator.getNotes();
		if (notes != null && !notes.isEmpty()) {
			writeNotes(notes);
		}
		List<org.colomoto.biolqm.metadata.annotations.Annotation> annotations = annotator.annotations();
		if (annotations == null) {
			return;
		}
		for (org.colomoto.biolqm.metadata.annotations.Annotation annotation : annotations) {
			doc.openParagraph(null);
			doc.writeText(annotation.qualifier == null || annotation.qualifier.term == null
					|| annotation.qualifier.term.isEmpty() ? "Annotations" : annotation.qualifier.term);
			doc.closeParagraph();
			doc.openList("L1");
			for (URI uri : annotation.uris) {
				doc.openListItem(null);
				String link = uri.getCollection() == null
						? new AnnotationLink(uri.uri(), null).getLink() : uri.getLink();
				writeLink(link, uri.uri());
				doc.closeListItem();
			}
			for (String tag : annotation.tags) {
				doc.openListItem("Tag: " + tag);
				doc.closeListItem();
			}
			for (Map.Entry<String, String> entry : annotation.keyValues.entrySet()) {
				doc.openListItem(entry.getKey() + ": " + entry.getValue());
				doc.closeListItem();
			}
			doc.closeList();
		}
	}

	private void writeNotes(String notes) throws IOException {
		doc.openParagraph(null);
		String[] lines = notes.split("\\R", -1);
		for (int i = 0; i < lines.length; i++) {
			if (i > 0) {
				doc.writeTextln("");
			}
			doc.writeText(lines[i]);
		}
		doc.closeParagraph();
	}

	private void writeLegacyAnnotation(Annotation annotation) throws IOException {

        writeNotes(annotation.getComment());
        writeLinkList(annotation.getLinkList());
    }

    private void writeLinkList(List<AnnotationLink> links) throws IOException {
        if (links == null || links.size() < 1) {
            return;
        }
        doc.openList("L1");
        for (AnnotationLink lnk: links) {
            doc.openListItem(null);
            writeLink(lnk.getLink(), lnk.toString());
            doc.closeListItem();
		}
        doc.closeList();
	}

	private void writeLink(String link, String label) throws IOException {
		String scheme = link == null ? "" : link.toLowerCase(Locale.ROOT);
		if (scheme.startsWith("http://") || scheme.startsWith("https://")
				|| scheme.startsWith("mailto:") || scheme.startsWith("ftp://")) {
			doc.addLink(link, label);
		} else {
			doc.writeText(label);
		}
	}

	/**
	 * Set the style for the document.
	 */
	private void setDocumentStyles() {
		DocumentStyle styles = doc.getStyles();
		styles.addStyle("L1");
		styles.addProperty(DocumentStyle.LIST_TYPE, "U");
		styles.addStyle("table");
		styles.addProperty(DocumentStyle.TABLE_BORDER, 1);
	}

	/**
	 * Set the properties (meta-information) for the document.
	 */
	private void setDocumentProperties() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//for dc:date
		doc.setDocumentProperties(new String[] {
				//DocumentWriter.META_AUTHOR,
				DocumentWriter.META_DATE, simpleDateFormat.format(new Date()).toString(),
				//DocumentWriter.META_DESCRIPTION,
				DocumentWriter.META_GENERATOR, "GINsim",
				//DocumentWriter.META_KEYWORDS,
				DocumentWriter.META_TITLE, graph.getGraphName()
		});
	}
}