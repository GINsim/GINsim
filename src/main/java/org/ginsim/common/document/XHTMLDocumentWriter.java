package org.ginsim.common.document;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.ginsim.common.utils.ColorPalette;
import org.ginsim.common.xml.XMLWriter;


/**
 * XHTMLDocumentWriter is a backend to write xHTML files
 * 
 * @see DocumentWriter
 * @author Duncan Berenguier
 * @author Aurelien Naldi
 */
public class XHTMLDocumentWriter extends DocumentWriter {

	public static final DocumentWriterFactory FACTORY = new XHTMLDocumentWriterFactory();;
	XMLWriter xmlw;
	OutputStreamWriter writer;
	
	Map m_style = new HashMap();
	List<xHTMLTable> v_table = new ArrayList<xHTMLTable>();
	xHTMLTable curTable = null;
	public String NEW_LINE = "<br />";
	
	static Map m_stylesWriters = new HashMap();
	static {
		m_stylesWriters.put(DocumentStyle.COLOR, new ColorStyleWriter("color: ", ";"));
		m_stylesWriters.put(DocumentStyle.FONT_SIZE, new SimpleStyleWriter("font-size: ", "pt;"));
		m_stylesWriters.put(DocumentStyle.HEIGHT, new SimpleStyleWriter("height: ", "px;"));
		m_stylesWriters.put(DocumentStyle.WIDTH, new SimpleStyleWriter("width: ", "px;"));
		m_stylesWriters.put(DocumentStyle.TABLE_BORDER, new BorderStyleWriter());
	}

	public XHTMLDocumentWriter() {
		registerForDocumentExtra("javascript");
		registerForDocumentExtra("css");
	}

	public void startDocument() throws IOException {
		writer = new OutputStreamWriter(output, "UTF-8");
		xmlw = new XMLWriter(writer);
	
        if (documentProperties.get(PROP_SUBDOCUMENT) == null) {

    		xmlw.openTag("html");
    		xmlw.addAttr("xmlns", "http://www.w3.org/1999/xhtml");
    		xmlw.addAttr("xml:lang", "en");
    		
    		xmlw.openTag("head");
    		if (documentProperties.containsKey(META_TITLE)) {
    			xmlw.addTagWithContent("title", (String)documentProperties.get(META_TITLE));
    		}
    		doCreateMeta("author", META_AUTHOR);
    		doCreateMeta("created-at", META_DATE);
    		doCreateMeta("keywords", META_KEYWORDS);
    		doCreateMeta("description", META_DESCRIPTION);
    		doCreateMeta("generator", META_GENERATOR);
    		
            if (documentProperties.containsKey("css")) {
                // <link rel="stylesheet" href="style.css" type="text/css" />
                xmlw.openTag("link");
                xmlw.addAttr("rel", "stylesheet");
                xmlw.addAttr("type", "text/css");
                xmlw.addAttr("href", (String)documentProperties.get("css"));
        		xmlw.closeTag();//link
            }
    		doWriteStyles();
    		
    		StringBuffer javascript = getDocumentExtra("javascript");
    		if (javascript.length() > 0) {
    			xmlw.openTag("script");
    			xmlw.addAttr("type", "text/javascript");
    			xmlw.addComplexContent(javascript.toString(), true);
    			xmlw.closeTag();
    		}
    		
    		StringBuffer css = getDocumentExtra("css");
    		if (javascript.length() > 0) {
    			xmlw.openTag("style");
    			xmlw.addAttr("type", "text/css");
    			xmlw.addFormatedContent(css.toString(), false);
    			xmlw.closeTag();
    		}
    		
    		xmlw.closeTag();//head
    
    		xmlw.openTag("body");
        } else {
            xmlw.openTag("div");
        }
	}
	
	protected void doOpenParagraph(String style) throws IOException {
		xmlw.openTag("p");
		if (style != null) {
			xmlw.addAttr("class", style);
		}
	}
	
	protected void doWriteText(String text, boolean newLine) throws IOException {
		xmlw.addContent(text);
		if (newLine) {
			xmlw.addFormatedContent(newLine(), false);
		}
	}
	
	protected String newLine() {
		return NEW_LINE;
	}

	protected void doOpenTable(String name, String style, String[] t_colStyle) throws IOException {
		curTable = new xHTMLTable(name, style, t_colStyle);
		v_table.add(curTable);
		xmlw.openTag("table");
		if (name != null) {
			xmlw.addAttr("name", name);
		}
		if (style != null) {
			xmlw.addAttr("class", style);
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
		xmlw.openTag("tr");
		curTable.row++;
		curTable.col = 0;
	}
	
	protected void doOpenTableCell(int colspan, int rowspan, boolean header, String style) throws IOException {
		if (curTable.row == 1 && curTable.t_colStyle != null && curTable.t_colStyle[curTable.col] != null) {
			xmlw.addAttr("class", curTable.t_colStyle[curTable.col++]);
		}
		if (header) {
			xmlw.openTag("th");
		} else {
			xmlw.openTag("td");			
		}
		if (colspan > 1) {
			xmlw.addAttr("colspan", ""+colspan);
		}
		if (rowspan > 1) {
			xmlw.addAttr("rowspan", ""+rowspan);
		}
		if (style != null) {
		    xmlw.addAttr("class", style);
		}
	}
	
	protected void doCloseDocument() throws IOException {
        if (documentProperties.get(PROP_SUBDOCUMENT) == null) {
            xmlw.closeTag(); // body
            xmlw.closeTag(); // html
        } else {
            xmlw.closeTag(); // div
        }
		writer.flush();
		writer.close();
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
		if (level > 6) {
			level = 6;
		}
		xmlw.openTag("h"+level);
		if (style != null) {
			xmlw.addAttr("class", style);
		}
		xmlw.addContent(content);
		xmlw.closeTag();
	}
	
	protected void doAddLink(String href, String content) throws IOException {
		xmlw.openTag("a");
		xmlw.addAttr("href", href);
		xmlw.addContent(content);
		xmlw.closeTag();
	}
	
	protected void doAddAnchor(String name, String content) throws IOException {
		xmlw.openTag("a");
		xmlw.addAttr("name", name);
		xmlw.addContent(content);
		xmlw.closeTag();
	}

	protected void doOpenList(String style, boolean numbered) throws IOException {
		if (style != null) {
			Map m_style = documentStyles.getPropertiesForStyle(style);
			if (m_style != null) {
				numbered = "O".equals(m_style.get(DocumentStyle.LIST_TYPE));
			}
		}
		if (numbered) {
			xmlw.openTag("ol");
		} else {
			xmlw.openTag("ul");
		}
		if (style != null) {
			xmlw.addAttr("class", style);
		}
	}
	protected void doOpenListItem() throws IOException {
		xmlw.openTag("li");
	}
	protected void doCloseListItem() throws IOException {
		xmlw.closeTag();
	}
	protected void doCloseList() throws IOException {
		xmlw.closeTag();
	}

	
	protected void doCreateMeta(String meta, String key) throws IOException {
		if (documentProperties.containsKey(key)) {
			xmlw.openTag("meta");
			xmlw.addAttr("name", meta);
			xmlw.addAttr("content", (String)documentProperties.get(key));
			xmlw.closeTag();
		}
	}
	
	protected void doWriteStyles() throws IOException {
		if (documentStyles != null) {
			xmlw.openTag("style");
			xmlw.addAttr("type", "text/css");
			xmlw.addContent("\ntable {border-collapse: collapse;}\n");
			Iterator styleIterator = documentStyles.getStyleIterator();
			while (styleIterator.hasNext()) {
				String style = (String) styleIterator.next();
				StringBuffer buf = new StringBuffer();
				StringBuffer bufHeader = new StringBuffer("\n."+style);
				Map properties = documentStyles.getPropertiesForStyle(style);
				Iterator propertiesIterator = documentStyles.getPropertiesIteratorForStyle(style);
				while (propertiesIterator.hasNext()) {
					String property = (String) propertiesIterator.next();
					StyleWriter sw = (StyleWriter)m_stylesWriters.get(property);
					if (sw != null) {
						buf.append(sw.getCSSStyle(properties.get(property)));
						bufHeader.append(sw.getCSSStyleHeader(style));
					}
				}
				buf.append("}\n");
				xmlw.addContent(bufHeader.toString()+"{"+buf.toString());
			}
			xmlw.closeTag();	
		}
	}

	protected void doAddImage(BufferedImage img, String name) throws IOException {
		// TODO Auto-generated method stub
		if (outputDir != null) {
			Files.createDirectories(outputDir.toPath());
			File fout = new File(outputDir, name);
			if (!ImageIO.write(img, "png", fout)) {
				throw new IOException("No PNG image writer available");
			}
			xmlw.openTag("img");
			try {
				xmlw.addAttr("src", new URI(null, null, "./" + dirPrefix + "/" + name, null).toASCIIString());
			} catch (URISyntaxException e) {
				throw new IOException("Invalid image path", e);
			}
			xmlw.closeTag();
		} else {
			// TODO: add an error message ?
		}
	}
}

interface StyleWriter{
	String getCSSStyle(Object value);
	String getCSSStyleHeader(String style);
}

class SimpleStyleWriter implements StyleWriter {
	final String prefix;
	final String suffix;
	public SimpleStyleWriter(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}
	public String getCSSStyle(Object value) {
		return prefix+value+suffix;
	}
	public String getCSSStyleHeader(String style) {
		return "";
	}
}

class ColorStyleWriter extends SimpleStyleWriter {
	public ColorStyleWriter(String prefix, String suffix) {
		super(prefix, suffix);
	}
	public String getCSSStyle(Object value) {
		return prefix+ColorPalette.getColorCode((Color)value)+suffix;
	}
}

class BorderStyleWriter implements StyleWriter {
	public String getCSSStyle(Object value) {
		return "border: "+value.toString()+"px solid black";
	}

	public String getCSSStyleHeader(String style) {
		return " th, "+style+" td";
	}
}

class xHTMLTable {
	String name;
	String style;
	String[] t_colStyle;
	int row, col;

	public xHTMLTable(String name, String style, String[] t_colStyle) {
		this.name = name;
		this.style = style;
		this.t_colStyle = t_colStyle;
		this.row = 0;
		this.col = 0;
	}
}

class XHTMLDocumentWriterFactory implements DocumentWriterFactory {

	@Override
	public DocumentWriter getDocumentWriter() {
		return new XHTMLDocumentWriter();
	}
}