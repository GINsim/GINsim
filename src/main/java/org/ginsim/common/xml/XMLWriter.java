package org.ginsim.common.xml;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.ginsim.common.utils.ColorPalette;

/**
 * A helper to write well formed XML documents.
 */
public class XMLWriter {

    private OutputStreamWriter out = null;
    private List v_stack = new ArrayList();
    private boolean inTag;
    private boolean inContent;
    private boolean indent;
    private StringBuffer buf = null;

	/**
	 * Indicates if the given string is a valid GINsim ID (contains only a-z, A-Z, 0-9, "_" or "-" characters)
	 * 
	 * @param id the string to test
	 * @return true if the given string can be used as ID
	 */
	public static boolean isValidId(String id) {
		return Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$").matcher(id).find();
	}

    public static String deriveValidId(String id) {
        if (isValidId(id)) {
            return id;
        }

        id = id.trim();
        if (id.length() < 1) {
            return "_";
        }

        char c = id.charAt(0);
        if ( !Character.isLetter(c) && c != '_') {
            id = '_' + id;
            if (isValidId(id)) {
                return id;
            }
        }

        char[] a = id.toCharArray();
        for (int i=1 ; i< a.length ; i++) {
            if (a[i] == '_') continue;
            if (Character.isLetterOrDigit(a[i])) continue;
            a[i] = '_';
        }

        return new String(a);
    }

    /**
     * Create a XMLWriter with the path to a file.
     * 
     * @param filename the string filename
     * @throws IOException the io exception
     */
    public XMLWriter(String filename) throws IOException {
        this(filename, null);
    }

    /**
     * Create a XMLWriter with the path to a file.
     *
     * @param filename the string filename
     * @param docType doc type value
     * @throws IOException the io exception
     */
    public XMLWriter(String filename, String docType) throws IOException {
    	this(new FileOutputStream(filename), docType);
	}

    /**
     * Create a XMLWriter with an existing Writer.
     * Warning: the writer should use the right encoding, otherwise we may get into troubles.
     *
     * @param out the OutputStreamWriter as out attribute
     * @throws IOException the io exception
     */
    public XMLWriter(OutputStreamWriter out) throws IOException {
        this(out, null);
    }
    /**
     * Create a XMLWriter with an existing Writer.
     * Warning: the writer should use the right encoding, otherwise we may get into troubles.
     * 
     * @param out the OutputStreamWriter
     * @param docType doc type value
     * @throws IOException the io exception
     */
    public XMLWriter(OutputStreamWriter out, String docType) throws IOException {
        this(out,docType,true);
    }
    /**
     * Create a XMLWriter with an output stream.
     * It will create a Writer for this stream, using an UTF-8 encoding.
     *
     * @param out the OutputStream
     * @throws IOException the io exception
     */
    public XMLWriter(OutputStream out) throws IOException {
        this(new OutputStreamWriter(out, "UTF-8"),null);
    }
    /**
     * Create a XMLWriter with an output stream.
     * It will create a Writer for this stream, using an UTF-8 encoding.
     *
     * @param out the OutputStreamWriter
     * @param docType doc type value
     * @throws IOException the io exception
     */
    private XMLWriter(OutputStream out, String docType) throws IOException {
        this(new OutputStreamWriter(out, "UTF-8"),docType,true);
    }

    /**
     * Create a XMLWriter with an existing Writer.
     * 
     * @param out the OutputStreamWriter as out attribute
     * @param docType doc type value
     * @param indent boolean if indent
     * @throws IOException the io exception
     */
    private XMLWriter(OutputStreamWriter out, String docType, boolean indent) throws IOException {
        this.indent = indent;
        this.out = out;
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        if (docType != null) {
            write("<!DOCTYPE "+docType+">\n");
        }
    }
    
	/**
     * ask to store the next calls into a string buffer.
     * Use <code>getBuffer()</code> to stop it and get the content of the buffer.
     * @throws IOException the io exception
     */
    public void toBuffer()  throws IOException {
        if (inTag) {
            write(">");
            if (indent) {
                write("\n");
            }
            inTag = false;
        }
        buf = new StringBuffer();
    }

    /**
     * If <code>toBuffer</code> has previously been called, stop saving to buffer.
     * 
     * @return the content of the buffer.
     */
    public String getBuffer() {
        if (buf == null) {
            return null;
        }
        String s = buf.toString();
        buf = null;
        return s;
    }
    
    /**
     * Re write string with protected characted
     * @param s the string to write
     * @param isAttVal boolean if Attribute value if \
     * @throws IOException the exception
     */
    public void writeEsc (String s, boolean isAttVal) throws IOException
    {
    	StringCharacterIterator iterator = new StringCharacterIterator(s);
    	char cur = iterator.current();
    	while (cur != CharacterIterator.DONE) {
           switch (cur) {
           case '&':
               write("&amp;");
               break;
           case '<':
               write("&lt;");
               break;
           case '>':
               write("&gt;");
               break;
           case '\"':
                if (isAttVal) {
                    write("&quot;");
                } else {
                    write('\"');
                }
                break;
           default:
        	   write(cur);
           }
           cur = iterator.next();
        }
    }

    /**
     * Write function
     * @param s the string
     * @throws IOException the exception
     */
    public void write(String s) throws IOException {
        if (buf != null) {
            buf.append(s);
            return;
        } 
        out.write(s);
    }
    
    /**
     * Write function
     * @param c the charactere
     * @throws IOException the exception
     */
    public void write(char c) throws IOException {
        if (buf != null) {
            buf.append(c);
            return;
        } 
        out.write(c);
    }
    
    /**
     * open a tag in the XML output file
     * @param name the string name
     * @throws IOException the exception
     */
    public void openTag(String name) throws IOException {
    	if (inTag) {
    		write(">");
    		if (indent) {
    			write("\n");
    		}
    	}
        inTag = true;
        if (indent) {
        	for (int i=0 ; i<v_stack.size() ; i++) {
        		write("  ");
        	}
        }
        write("<"+name);
        v_stack.add(name);
        inContent = false;
    }

    /**
     * add (i.e. open and close) a tag without any attributes and content.
     * @param name the string name
     * @throws IOException the exception
     */
    public void addTag(String name) throws IOException {
    	openTag(name);
    	closeTag();
    }
    /**
     * add (i.e. open and close) a tag with specified attributes and content.
     * @param name the name string
     * @param attributes string array attributes
     * @param content string content
     * @throws IOException the exception
     */
    public void addTag(String name, String[] attributes, String content) throws IOException {
    	openTag(name, attributes);
    	addContent(content);
    	closeTag();
    }
    /**
     * open a tag and add it the specified attributes.
     * @param name the string name
     * @param attributes string array attributes
     * @throws IOException the exception
     */
    public void openTag(String name, String[] attributes) throws IOException {
    	openTag(name);
        if (attributes != null) {
            for (int i=0 ; i<attributes.length ; i+=2) {
                addAttr(attributes[i], attributes[i+1]);
            }
        }
    }
    /**
     * add (i.e. open and close) a tag with specified attributes and no content.
     * @param name the string name
     * @param attributes string array attributes
     * @throws IOException the exception
     */
    public void addTag(String name, String[] attributes) throws IOException {
    	openTag(name, attributes);
    	closeTag();
    }

    /**
     * add (i.e. open and close) a tag with specified content and no attributes.
     * @param tag the tag string
     * @param content the object c content
     * @throws IOException the exception
     */
    public void addTagWithContent(String tag, Object content) throws IOException {
    	addTagWithContent(tag, content.toString());
    }
    /**
     * add (i.e. open and close) a tag with specified content and no attributes.
     * @param tag the tag string
     * @param content the object c content
     * @throws IOException the exception
     */
    public void addTagWithContent(String tag, String content) throws IOException {
    	openTag(tag);
    	addContent(content);
    	closeTag();
    }

    /**
     * close the currently opened tag.
     * depending on context it will use "/&gt; or "&lt;/name&gt;"
     * @throws IOException the exception
     */
    public void closeTag() throws IOException {
        int l = v_stack.size()-1;
        if (inTag) {
            write("/>");
        } else {
            if (!inContent && indent) {
                for (int i=0 ; i<l ; i++) {
                    write("  ");
                }
            }
            write("</"+v_stack.get(l)+">");
        }
        if (indent) {
        	write("\n");
        }
        v_stack.remove(l);
        inTag = false;
        inContent = false;
    }

    /**
     * Add a color attribute to the opened tag.
     * The color will be encoded as #RRGGBB
     * 
     * @param name the string name
     * @param color the Color
     * @throws IOException the exception
     */
    public void addAttr(String name, Color color) throws IOException {
    	addAttr(name, ColorPalette.getColorCode(color));
    }
    
    /**
     * add an attribute to the opened tag.
     * If the tag is no-more really opened it will return silently without writing anything.
     * @param name the string name
     * @param value the string value
     * @throws IOException the exception
     */
    public void addAttr(String name, String value) throws IOException {
        if (!inTag) {
            return;
        }
        write(" "+name+"=\"");
        if (value == null) {
        	writeEsc("", true);
        } else {
        	writeEsc(value, true);
        }
        write("\"");
    }
    
    /**
     * add a "text child"
     * @param s the string to add
     * @throws IOException the exception
     */
    public void addContent(String s) throws IOException {
        if (inTag) {
            write(">");
            inTag = false;
        }
        writeEsc(s, false);
        inContent = true;
    }
    /**
     * add a "text child", already formated: should _NOT_ be escaped
     * @param s the string to add
     * @param b if true, then the file might get indented
     * @throws IOException the exception
     */
    public void addFormatedContent(String s, boolean b) throws IOException {
    	addLongContent(s, b, false);
    }

    /**
     * add a complex "text child", it will be enclosed into CDATA markers
     * @param s the string to add
     * @param b if true, then the file might get indented
     * @throws IOException  the exception
     */
    public void addComplexContent(String s, boolean b) throws IOException {
    	addLongContent(s, b, true);
    }

    /**
     * Common implementation for formatted and complex content.
     * 
     * @param s the string to add
     * @param b boolean to write EOL
     * @param cdata boolean to write CDATA
     * @throws IOException the exception
     */
    private void addLongContent(String s, boolean b, boolean cdata) throws IOException {
        if (inTag) {
            write(">");
            inTag = false;
            if (b && indent) {
                write("\n");
            }
        }
        if (cdata) {
        	write("<![CDATA[");
        }
        write(s);
        if (cdata) {
        	write("]]>");
        }
    }
    
    /**
     * Close the writer: all open tags and the underlying outputstream.
     * 
     * @throws IOException the exception
     */
    public void close() throws IOException {
    	while (v_stack.size() > 0) {
    		closeTag();
    	}
    	out.close();
    }
}
