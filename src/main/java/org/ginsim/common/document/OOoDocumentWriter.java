package org.ginsim.common.document;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Locale;
import java.util.TreeMap;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.ginsim.common.utils.ColorPalette;
import org.ginsim.common.xml.XMLWriter;


/**
 * OOoDocumentWriter is a backend to write OpenDocument text files.
 * Such files are supported by OpenOffice/LibreOffice and recent versions of MS Office.
 * 
 * @see DocumentWriter
 * @author Aurelien Naldi
 */
public class OOoDocumentWriter extends DocumentWriter {

	public static final DocumentWriterFactory FACTORY = new OOoDocumentWriterFactory();
	
	XMLWriter xmlw;
	ZipOutputStream zo;
	OutputStreamWriter writer;
	
	List<OOoTable> v_table = new ArrayList();
	OOoTable curTable = null;
	
	Map<String, BufferedImage> m_files = new TreeMap<>();
	
	public OOoDocumentWriter() {
		NEW_LINE = "<text:line-break/>";
	}
		
	public void startDocument() throws ZipException, IOException {
		zo = new ZipOutputStream(output);
		writer = new OutputStreamWriter(zo, "UTF-8");
		
		byte[] mime = "application/vnd.oasis.opendocument.text".getBytes(StandardCharsets.US_ASCII);
		ZipEntry mimeEntry = new ZipEntry("mimetype");
		mimeEntry.setMethod(ZipEntry.STORED);
		mimeEntry.setSize(mime.length);
		CRC32 crc = new CRC32();
		crc.update(mime);
		mimeEntry.setCrc(crc.getValue());
		zo.putNextEntry(mimeEntry);
		zo.write(mime);
		zo.closeEntry();

		zo.putNextEntry(new ZipEntry("content.xml"));
		xmlw = new XMLWriter(writer);
		xmlw.openTag("office:document-content");
		xmlw.addAttr("xmlns:office", "urn:oasis:names:tc:opendocument:xmlns:office:1.0");
		xmlw.addAttr("xmlns:style","urn:oasis:names:tc:opendocument:xmlns:style:1.0");
		xmlw.addAttr("xmlns:text","urn:oasis:names:tc:opendocument:xmlns:text:1.0");
		xmlw.addAttr("xmlns:table","urn:oasis:names:tc:opendocument:xmlns:table:1.0");
		xmlw.addAttr("xmlns:draw","urn:oasis:names:tc:opendocument:xmlns:drawing:1.0" );
		xmlw.addAttr("xmlns:fo","urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0");
		xmlw.addAttr("xmlns:xlink","http://www.w3.org/1999/xlink");
		xmlw.addAttr("xmlns:dc","http://purl.org/dc/elements/1.1/");
		xmlw.addAttr("xmlns:meta","urn:oasis:names:tc:opendocument:xmlns:meta:1.0");
		xmlw.addAttr("xmlns:number","urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0");
		xmlw.addAttr("xmlns:svg","urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0");
		xmlw.addAttr("xmlns:chart","urn:oasis:names:tc:opendocument:xmlns:chart:1.0");
		xmlw.addAttr("xmlns:dr3d","urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0");
		xmlw.addAttr("xmlns:math","http://www.w3.org/1998/Math/MathML");
		xmlw.addAttr("xmlns:form","urn:oasis:names:tc:opendocument:xmlns:form:1.0");
		xmlw.addAttr("xmlns:script","urn:oasis:names:tc:opendocument:xmlns:script:1.0");
		xmlw.addAttr("xmlns:ooo","http://openoffice.org/2004/office");
		xmlw.addAttr("xmlns:ooow","http://openoffice.org/2004/writer");
		xmlw.addAttr("xmlns:oooc","http://openoffice.org/2004/calc");
		xmlw.addAttr("xmlns:dom","http://www.w3.org/2001/xml-events");
		xmlw.addAttr("xmlns:xforms","http://www.w3.org/2002/xforms");
		xmlw.addAttr("xmlns:xsd","http://www.w3.org/2001/XMLSchema");
		xmlw.addAttr("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		xmlw.addAttr("office:version","1.0");
				
		xmlw.openTag("office:scripts");
		xmlw.closeTag();
		
		xmlw.openTag("office:font-face-decls");
		xmlw.closeTag();
		
		doWriteStyles();

		xmlw.openTag("office:body");
		xmlw.openTag("office:text");
		
		xmlw.openTag("office:forms");
		xmlw.addAttr("form:automatic-focus","false");
		xmlw.addAttr("form:apply-design-mode","false");
		xmlw.closeTag();
		
		xmlw.openTag("text:sequence-decls");
		xmlw.closeTag();
	}
	
	protected void doWriteStyles() throws IOException {
		if (documentStyles != null) {
			xmlw.openTag("office:automatic-styles");
			
			Iterator styleIterator = documentStyles.getStyleIterator();
			while (styleIterator.hasNext()) {
				String style = (String)styleIterator.next();
				Map m_style = documentStyles.getPropertiesForStyle(style);
				
				Object listtype = m_style.get(DocumentStyle.LIST_TYPE);
				if (listtype != null) {
					xmlw.openTag("text:list-style");
					xmlw.addAttr("style:name", style);
					double spacing = 0.5;
					for (int i=0 ; i<10 ; i++) {
						xmlw.openTag("text:list-level-style-bullet");
						xmlw.addAttr("text:level", ""+i);
						xmlw.addAttr("text:bullet-char", "\u2022");
						xmlw.openTag("style:list-level-properties");
						xmlw.addAttr("text:space-before", (i-1)*spacing+"cm");
						xmlw.addAttr("text:min-label-width", spacing+"cm");
						xmlw.closeTag();
						xmlw.closeTag();
					}
					xmlw.closeTag();	
				} else {
					xmlw.openTag("style:style");
					xmlw.addAttr("style:name", style);
					xmlw.addAttr("style:family", "paragraph"); // FIXME:
				
	//				StringBuffer buf = new StringBuffer(style);
	//				buf.append('{');
	//				Map properties = styles.getPropertiesForStyle(style);
	//				Iterator propertiesIterator = styles.getPropertiesIteratorForStyle(style);
	//				while (propertiesIterator.hasNext()) {
	//					String property = (String) propertiesIterator.next();
	//					buf.append(property);
	//					buf.append(':');
	//					buf.append(getStyleValue(property, properties.get(property)));
	//					buf.append(';');
	//				}
	//				buf.append('}');
	//				xmlw.addContent(buf.toString());
					xmlw.closeTag();	
				}
			}
			xmlw.closeTag();
		}
	}

	private String getStyleValue(String property, Object value) {
		if (property.equals(DocumentStyle.COLOR)) {
			return ColorPalette.getColorCode((Color)value);
		} else if (property.equals(DocumentStyle.FONT_SIZE)) {
			return value.toString()+"pt";
		} else if (property.equals(DocumentStyle.HEIGHT)) {
			return value.toString()+"px";
		} else if (property.equals(DocumentStyle.WIDTH)) {
			return value.toString()+"px";
		}
		return property.toString();
	}

	
	protected void doOpenParagraph(String style) throws IOException {
		xmlw.openTag("text:p");
		xmlw.addAttr("text:style-name", style==null ? "Standard" : style);
	}
	
	protected void doWriteText(String text, boolean newLine) throws IOException {
		ensureParagraph();
		xmlw.addContent(text);
		if (newLine) {
			xmlw.addFormatedContent(newLine(), false);
		}
	}
	
	protected void doOpenTable(String name, String style, String[] t_colStyle) throws IOException {
		curTable = new OOoTable();
		curTable.name = name;
		curTable.style = style;
		curTable.t_colStyle = t_colStyle;
		v_table.add(curTable);
		xmlw.openTag("table:table");
		xmlw.addAttr("table:name", name);
		if (style != null) {
			xmlw.addAttr("table:style-name", style);
		}
		if (t_colStyle != null) {
			for (int i=0 ; i<t_colStyle.length ; i++) {
				xmlw.openTag("table:table-column");
				xmlw.addAttr("table:style-name", t_colStyle[i]);
				xmlw.closeTag();
			}
		}
	}
	
	protected void doCloseTable() throws IOException {
		xmlw.closeTag();
		v_table.remove(curTable);
		if (v_table.size() > 0) {
			curTable = v_table.get(v_table.size()-1);
		} else {
			curTable = null;
		}
	}
	
	protected void doOpenTableRow(String style) throws IOException {
		xmlw.openTag("table:table-row");
	}
	
	protected void doOpenTableCell(int colspan, int rowspan, boolean header, String style) throws IOException {
		xmlw.openTag("table:table-cell");
		if (colspan > 1) {
			xmlw.addAttr("table:number-columns-spanned", ""+colspan);
		}
		if (rowspan > 1) {
			xmlw.addAttr("table:number-rows-spanned", ""+rowspan);
		}
	}
	
	protected void doCloseDocument() throws IOException {
		xmlw.closeTag(); // "office:text"
		xmlw.closeTag(); // "office:body"
		xmlw.closeTag(); // "office:document-content"
		writer.flush();
		zo.closeEntry();
		
		for (Entry<String, BufferedImage> entry : m_files.entrySet()) {
			zo.putNextEntry(new ZipEntry("Pictures/" + entry.getKey()));
			if (!ImageIO.write(entry.getValue(), "png", zo)) {
				throw new IOException("No PNG image writer available");
			}
			zo.closeEntry();
		}

		zo.putNextEntry(new ZipEntry("META-INF/manifest.xml"));
		XMLWriter manifest = new XMLWriter(writer);
		manifest.openTag("manifest:manifest");
		manifest.addAttr("xmlns:manifest", "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0");
		addManifestEntry(manifest, "/", "application/vnd.oasis.opendocument.text");
		addManifestEntry(manifest, "content.xml", "text/xml");
		for (String name : m_files.keySet()) {
			addManifestEntry(manifest, "Pictures/" + name, "image/png");
		}
		manifest.closeTag();
		writer.flush();
		zo.closeEntry();
		zo.close();
	}

	private void addManifestEntry(XMLWriter manifest, String path, String mediaType) throws IOException {
		manifest.openTag("manifest:file-entry");
		manifest.addAttr("manifest:media-type", mediaType);
		manifest.addAttr("manifest:full-path", path);
		manifest.closeTag();
	}

	protected void doCloseParagraph() throws IOException {
		xmlw.closeTag();
	}

	protected void doCloseTableCell() throws IOException {
		xmlw.closeTag();
	}

	protected void doCloseTableRow() throws IOException {
		xmlw.closeTag();
	}

	protected void doOpenHeader(int level, String content, String style) throws IOException {
		xmlw.openTag("text:h");
		xmlw.addAttr("text:outline-level", ""+level);
		if (style != null) {
			xmlw.addAttr("text:style-name", style);
//		} else {
//			xmlw.addAttr("text:style-name", "Heading_20_"+level);
		}
		xmlw.addContent(content);
		xmlw.closeTag();
	}
	protected void doAddLink(String href, String content) throws IOException {
		ensureParagraph();
		xmlw.openTag("text:a");
		xmlw.addAttr("xlink:type", "simple");
		xmlw.addAttr("xlink:href", href);
		xmlw.addContent(content);
		xmlw.closeTag();
	}
	protected void doAddAnchor(String name, String content) throws IOException {
		// TODO
	}
	protected void doOpenList(String style, boolean numbered) throws IOException {
		// FIXME: support numbered list
		xmlw.openTag("text:list");
		if (style != null) {
			xmlw.addAttr("text:style-name", style);
		}
	}
	protected void doOpenListItem() throws IOException {
		xmlw.openTag("text:list-item");
	}
	protected void doCloseListItem() throws IOException {
		xmlw.closeTag();
	}
	protected void doCloseList() throws IOException {
		xmlw.closeTag();
	}

	protected void doAddImage(BufferedImage img, String name) throws IOException {
		ensureParagraph();
		double scale = Math.min(1.0, Math.min(16.0 * 96 / 2.54 / img.getWidth(),
				20.0 * 96 / 2.54 / img.getHeight()));
		xmlw.openTag("draw:frame");
		xmlw.addAttr("text:anchor-type", "as-char");
		xmlw.addAttr("svg:width", String.format(Locale.ROOT, "%.3fcm", img.getWidth() * scale * 2.54 / 96));
		xmlw.addAttr("svg:height", String.format(Locale.ROOT, "%.3fcm", img.getHeight() * scale * 2.54 / 96));
		xmlw.openTag("draw:image");
		xmlw.addAttr("xlink:href", "Pictures/"+name);
		xmlw.addAttr("xlink:type", "simple");
		xmlw.addAttr("xlink:show", "embed");
		xmlw.addAttr("xlink:actuate", "onLoad");
		xmlw.closeTag();
		xmlw.closeTag();
		m_files.put(name, img);
	}
}

class OOoTable {
	String name;
	String style;
	String[] t_colStyle;
}

class OOoDocumentWriterFactory implements DocumentWriterFactory {

	@Override
	public DocumentWriter getDocumentWriter() {
		return new OOoDocumentWriter();
	}
}
