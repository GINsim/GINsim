package org.ginsim.common.document;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;

/**
 * <p><b>DocumentWriter</b> is an abstract class designed to help writing documents. It contains all the methods to define the structure of a document such as startDocument, openParagraph...<br>
 * To write a document, you must call <u>startDocument()</u> first. Then build the structure of the document using the openXXX or addXXX methods. End the document with <u>close()</u></p>
 * 
 * <p>The openXXX and closeXXX deals with the tree structure of the document and call the corresponding doXXX methods.<br>
 * The doXXX methods are implemented in the DocumentWriter's subclass and must write the structure in the document.<br>
 * For example, openHeader make sure we are at the "out" level in the document's tree then it calls doOpenHeader which must write the header itself</p>
 * 
 * <b>DocumentProperties :</b>
 * <p>The protected instance variable documentProperties is a Map of properties (meta) that contains some informations about the document such as the title, the authors...<br>
 * There is a list of class constants beginning with META_* for commonly used properties<br>They must be declared before the startDocument()</p>
 * <p>There is 3 methods to manipulate documentProperties : </p>
 * <ul>
 * 	<li>setDocumentProperty(Object name, Object value) to define a new property</li>
 * 	<li>setDocumentProperties(Map map) to define multiple properties</li>
 * 	<li>setDocumentProperties(Object[] table) to define multiple properties [name, value, name, value...]</li>
 * </ul>
 * 
 * <b>DocumentExtras :</b>
 * <p>DocumentExtra is a concept to allow to define content for a specific type of document.<br>
 * A documentExtra is a StringBuffer containing the content.<br>
 * documentExtras is a map of documentExtra with a key representing the id of the extra</p>
 * <p>When you implement a DocumentWriter subclass, you should register for the extra you are able to use</p>
 * <p>When you use a DocumentWriter subclass, if you want to add specific content, first test if the document support the extra, if it does get the documentExtra and append it your content.</p>
 * <p>There is 3 methods to manipulate documentExtras : </p>
 * <ul>
 * 	<li>registerForDocumentExtra(String extra) register the document for an extra</li>
 * 	<li>getDocumentExtra(String extra) to get an extra</li>
 * 	<li>doesDocumentSupportExtra(String extra) to know if the current document support the extra</li>
 * </ul>
 * 
 * <b>DocumentStyles :</b>
 * <p>Allow you to set the style for the document.<br>They must be declared before the startDocument()</p>
 * <p>There is 2 methods to manipulate documentStyles : </p>
 * <ul>
 * 	<li>getStyles() get the styles of the document</li>
 * 	<li>setStyles(DocumentStyle newStyle) define anotherStyle for the document</li>
 * </ul>
 * 
 * <b>New lines</b>
 * <p>To append a generic new line, use the newLine() function.<br>
 * When you subclass DocumentWriter, you can overwrite newLine() or simply set a new value for the NEW_LINE variable.</p>
 * @see org.ginsim.common.document.DocumentStyle
 * 
 * @author Aurelien Naldi
 * @author Duncan Berenguier
 */
public abstract class DocumentWriter {

	public static final int POS_OUT = 0;
	public static final int POS_PARAGRAPH = 1;
	public static final int POS_HEADER = 2;
	public static final int POS_TABLE = 10;
	public static final int POS_TABLE_ROW = 11;
	public static final int POS_TABLE_CELL = 12;
	public static final int POS_LIST = 20;
	public static final int POS_LIST_ITEM = 21;

	public static final String META_TITLE = "title";
	public static final String META_AUTHOR = "author";
	public static final String META_DATE = "date";
	public static final String META_KEYWORDS = "keywords";
	public static final String META_DESCRIPTION = "description";
	public static final String META_GENERATOR = "generator";
	
	public static final String PROP_SUBDOCUMENT = "subdoc";

	public String NEW_LINE = "\n";

	protected DocumentPos pos = new DocumentPos();
	private Map documentExtras; //A list of all the extra (StringBuffer) the document can use.
	protected Map documentProperties; //The metadata of the document
	protected DocumentStyle documentStyles = null; //The style of the document.

	protected OutputStream output = null;
	protected File outputDir = null;
	protected String	dirPrefix = null;
	
	public DocumentWriter() {
		documentProperties = new Hashtable();
		documentExtras = new Hashtable();
	}
	
	public void setOutput(OutputStream output) {
		this.output = output;
		this.outputDir = null;
		this.dirPrefix = null;
	}
	public void setOutput(File file) throws FileNotFoundException {
		setOutput(file, new FileOutputStream(file));
	}

	public void setOutput(File file, OutputStream output) {
		this.output = output;
		this.dirPrefix  = file.getName()+"_FILES";
		this.outputDir = new File(file.getParentFile(), dirPrefix);
	}
	
	public abstract void startDocument() throws IOException;

	/**
	 * Open a paragraph with a style
	 * @param style
	 * @throws IOException
	 */
	public void openParagraph(String style) throws IOException {
		if (pos.pos == POS_PARAGRAPH) {
			closeParagraph();
		}
		doOpenParagraph(style);
		pos = new DocumentPos(pos, POS_PARAGRAPH, style);
	}
	/**
	 * Open a paragraph without a style
	 * @throws IOException
	 */
	public void newParagraph() throws IOException {
		openParagraph(null);
	}
	
	/**
	 * Close a paragraph
	 * @throws IOException
	 */
	public void closeParagraph() throws IOException {
		if (pos.pos != POS_PARAGRAPH) {
			// FIXME: error
			// throw new Exception("incoherent position");
		}
		doCloseParagraph();
		pos = pos.parent;
	}

	/**
	 * Open a new header. Note the header can't be in another element.
	 * @param level The header's level (1 = section, 2 = subsection, 3 = subsubsection...) 
	 * @param content
	 * @param style
	 * @throws IOException
	 */
	public void openHeader(int level, String content, String style) throws IOException {
		closeUntil(POS_OUT);
		doOpenHeader(level, content, style);
	}
	
	/**
	 * add a new link into the document.
	 * @param href the link's location (URI)
	 * @param content the link's description
	 * @throws IOException
	 */
	public void addLink(String href, String content) throws IOException {
		doAddLink(href, content);
	}
	
	/**
	 * add a new anchor into the document.
	 * @param name the anchors location (URI)
	 * @param content the anchors description
	 * @throws IOException
	 */
	public void addAnchor(String name, String content) throws IOException {
		doAddAnchor(name, content);
	}
	
	/**
	 * Open a new table. 
	 * A table is made of table rows
	 * 
	 * @param name
	 * @param style
	 * @param t_colStyle a style for each column of the table. You must set the style, even if you do not use it. (use new String[] {"" , ""...} if you didn't want any style)
	 * @throws IOException
	 */
	public void openTable(String name, String style, String[] t_colStyle) throws IOException {
		while (pos.pos == POS_PARAGRAPH) {
			closeParagraph();
		}
		doOpenTable(name, style, t_colStyle);
		pos = new DocumentPos(pos, POS_TABLE, style);
	}
	
	/**
	 * Open a new row in the current table.
	 * A row is made of table cells
	 * @throws IOException
	 */
	public void openTableRow() throws IOException {
		openTableRow(null);
	}

	/**
	 * Open a new row in the current table.
	 * A row is made of table cells
	 * @param style TODO
	 * @throws IOException
	 */
	public void openTableRow(String style) throws IOException {
		if (pos.pos != POS_TABLE) {
			closeTableRow();
		}
		doOpenTableRow(style);
		pos = new DocumentPos(pos, POS_TABLE_ROW, style);
	}
	/**
	 * Open a new cell in the current table row
	 * @param colspan the number of column to collapse together.
	 * @param rowspan the number of rows to collapse together.
	 * @param content the cell's content
	 * @throws IOException
	 */
	public void openTableCell(int colspan, int rowspan, String content) throws IOException {
		openTableCell(colspan, rowspan, content, null, false);
	}

	/**
	 * Open a new cell in the current table row
	 * @param colspan the number of column to collapse together.
	 * @param rowspan the number of rows to collapse together.
	 * @param content the cell's content
	 * @param header if true the cell is an header
	 * @throws IOException
	 */
    public void openTableCell(int colspan, int rowspan, String content, boolean header) throws IOException {
        openTableCell(colspan, rowspan, content, null, header);
    }

	public void openTableCell(int colspan, int rowspan, String content, String cl, boolean header) throws IOException {
        if (pos.pos == POS_TABLE) {
            openTableRow(null);
        } else {
            closeUntil(POS_TABLE_ROW);
        }
        doOpenTableCell(colspan, rowspan, header, cl);
        pos = new DocumentPos(pos, POS_TABLE_CELL, null);
        if (content != null) {
            writeText(content);
        }
    }
	/**
	 * Open a new cell in the current table row with 1 colspan and rowspan
	 * @param content the cell's content
	 * @throws IOException
	 */	public void openTableCell(String content) throws IOException {
		openTableCell(1,1, content, null, false);
	}
	/**
	 * Open a new table row and new table cells with the content of the array
	 * @param t_content the content for the cells
	 * @throws IOException
	 */
	public void addTableRow(String[] t_content) throws IOException {
		openTableRow(null);
		for (int i=0 ; i<t_content.length ; i++) {
			openTableCell(1, 1, t_content[i], null, false);
		}
	}

	/**
	 * Close a table cell
 	 * @throws IOException
	 */
	public void closeTableCell() throws IOException {
		closeUntil(POS_TABLE_CELL);
		doCloseTableCell();
		pos = pos.parent;
	}

	/**
	 * Close a table row
 	 * @throws IOException
	 */
	public void closeTableRow() throws IOException {
		if (pos.pos != POS_TABLE_ROW) {
			closeTableCell();
		}
		doCloseTableRow();
		pos = pos.parent;
	}

	/**
	 * Close a table
 	 * @throws IOException
	 */
	public void closeTable() throws IOException {
		if (pos.pos != POS_TABLE) {
			closeTableRow();
		}
		doCloseTable();
		pos = pos.parent;
	}	

	/**
	 * Open a list element with a specific style
	 * A list is made of ListItems
	 * 
	 * @param style
	 * @throws IOException
	 */
	public void openList(String style) throws IOException {
		openList(style, false);
	}
	public void openList(String style, boolean isNumbered) throws IOException {
		while (pos.pos == POS_PARAGRAPH) {
			closeParagraph();
		}
		doOpenList(style, isNumbered);
		pos = new DocumentPos(pos, POS_LIST, style);
	}
	
	/**
	 * Open a new list item and write the content in.
	 * You must be in a List to open list items. It close automatically previously opened list item. 
	 * @param content
	 * @throws IOException
	 */
	public void openListItem(String content) throws IOException {
		closeUntil(POS_LIST);
		doOpenListItem();
		pos = new DocumentPos(pos, POS_LIST_ITEM, null);
		if (content != null) {
			writeText(content);
		}
	}
	
	/**
	 * Close a list item.
	 * @throws IOException
	 */
	public void closeListItem() throws IOException {
		doCloseListItem();
		pos = pos.parent;
	}
	
	/**
	 * Close a list.
	 * @throws IOException
	 */
	public void closeList() throws IOException {
		closeUntil(POS_LIST);
		doCloseList();
		pos = pos.parent;
	}

	/**
	 * Write the text into the current opened element.
	 * @param text the text to write
	 * @throws IOException
	 */
	public void writeText(String text) throws IOException {
		doWriteText(text, false);
	}

	/**
	 * Write the text into the current opened element and append a new line at the end.
	 * @param text the text to write
	 * @throws IOException
	 */
	public void writeTextln(String text) throws IOException {
		doWriteText(text, true);
	}

	/**
	 * Close every element open then close the document
	 * @throws IOException
	 */
	public void close() throws IOException {
		closeUntil(POS_OUT);
		doCloseDocument();
	}
	
	public void addImage(BufferedImage img, String name) throws IOException {
		doAddImage(img, name);
	}
	
	protected abstract void doOpenParagraph(String style) throws IOException;
	protected abstract void doCloseParagraph() throws IOException;
	protected abstract void doWriteText(String text, boolean newLine) throws IOException;
	protected abstract void doOpenTable(String name, String style,
			String[] t_colStyle) throws IOException;
	protected abstract void doCloseTable() throws IOException;
	protected abstract void doCloseTableRow() throws IOException;
	protected abstract void doCloseTableCell() throws IOException;
	protected abstract void doOpenTableRow(String style) throws IOException;
	protected abstract void doOpenTableCell(int colspan, int rowspan, boolean header, String style) throws IOException;
	protected abstract void doCloseDocument() throws IOException;
	protected abstract void doOpenHeader(int level, String content, String style) throws IOException;
	protected abstract void doAddLink(String href, String content) throws IOException;
	protected abstract void doAddAnchor(String name, String content) throws IOException;
	protected abstract void doAddImage(BufferedImage img, String name) throws IOException;
	protected abstract void doOpenList(String style, boolean isNumbered) throws IOException;
	protected abstract void doOpenListItem() throws IOException;
	protected abstract void doCloseListItem() throws IOException;
	protected abstract void doCloseList() throws IOException;

	/*
	 * return the code to add a new line.
	 * @return the NEW_LINE constant
	 */
	protected String newLine() {
		return NEW_LINE;
	}
	
	/**
	 * Set a property for the document.
	 * @param name the name of the property (use 'META' constant if possible)
	 * @param value the value of the property
	 */
	public void setDocumentProperty(Object name, Object value) {
		documentProperties.put(name, value);
	}
	/**
	 * Add a map of properties to the document.
	 * @param map the map of properties
	 */
	public void setDocumentProperties(Map map) {
		documentProperties.putAll(map);
	}
	/**
	 * Add an array of properties to the document
	 * @param table A table that alternate the name then the value of properties
	 * @throws ArrayIndexOutOfBoundsException if there is not the same number of name and values.
	 */
	public void setDocumentProperties(Object[] table) throws ArrayIndexOutOfBoundsException {
		if (table.length%2 == 1) {
			throw new ArrayIndexOutOfBoundsException();
		}
    	for (int i=0 ; i< table.length ; i+=2) {
    		documentProperties.put(table[i], table[i+1]);
    	}
	}
	
	/**
	 * Register the current document for extra (initialize the StringBuffer)
	 * @param extra The name of the extra to create
	 */
	protected void registerForDocumentExtra(String extra) {
		documentExtras.put(extra, new StringBuffer());
	}
	/**
	 * Get a documentExtra
	 * @param extra The name of the extra to get
	 * @return the document extra
	 */
	public StringBuffer getDocumentExtra(String extra) {
		return (StringBuffer)documentExtras.get(extra);
	}
	/**
	 * Indicate if the current document support a specific extra
	 * @param extra The name of the extra
	 * @return true if the document support the specified extra
	 */
	public boolean doesDocumentSupportExtra(String extra) {
		return documentExtras.containsKey(extra);
	}

	/**
	 * Get the styles of the document, and initialize them if needed.
	 * @return the document style
	 */
	public DocumentStyle getStyles() {
		if (documentStyles == null) {
			documentStyles = new DocumentStyle();
		}
		return documentStyles;
	}
	/**
	 * Define a new DocumentStyle for the document
	 * @param newStyle the new Style
	 * @return the old style
	 */
	public DocumentStyle setStyles(DocumentStyle newStyle) {
		DocumentStyle oldStyles = documentStyles;
		documentStyles = newStyle;
		return oldStyles;
	}
	
	/**
	 * Open a new paragraph if we are in a paragraph.
	 * @throws IOException
	 */
	protected void ensureParagraph() throws IOException {
		if (pos.pos != POS_PARAGRAPH) {
			openParagraph(null);
		}
	}
	
	/**
	 * Close every opened element until we are in the correct position in the tree
	 * 
	 * @param targetpos the position
	 * @throws IOException
	 */
	protected void closeUntil(int targetpos) throws IOException {
		while (pos != null && pos.pos != POS_OUT && pos.pos != targetpos) {
			switch (pos.pos) {
				case POS_PARAGRAPH:
					closeParagraph();
					break;
				case POS_TABLE:
					closeTable();
					break;
				case POS_TABLE_CELL:
					closeTableCell();
					break;
				case POS_TABLE_ROW:
					closeTableRow();
					break;
				case POS_LIST_ITEM:
					closeListItem();
					break;
				case POS_LIST:
					closeList();
					break;
			}
		}
	}

	public void openTableCell(String content, boolean header) throws IOException {
		openTableCell(1, 1, content, header);
	}
}


/**
 * Represent the document's Tree view (like the DOM in HTML)
 * */
class DocumentPos {
	
	public DocumentPos parent;
	public Object data;
	public String style;
	public int pos;
	
	public DocumentPos() {
		this(null, DocumentWriter.POS_OUT, null, null);
	}
	public DocumentPos(DocumentPos parent, int pos, String style) {
		this(parent, pos, null, null);
	}
    public DocumentPos(DocumentPos parent, int pos, Object data) {
        this(parent, pos, data, null);
    }
	
	public DocumentPos(DocumentPos parent, int pos, Object data, String style) {
		this.parent = parent;
		this.data = data;
		this.pos = pos;
		this.style = style;
	}
}
