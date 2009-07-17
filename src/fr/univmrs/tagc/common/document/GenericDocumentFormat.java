package fr.univmrs.tagc.common.document;

import java.util.Iterator;
import java.util.Vector;

import fr.univmrs.tagc.GINsim.graph.GsGraph;
import fr.univmrs.tagc.GINsim.gui.GsPluggableActionDescriptor;

/**
 * This class contain the informations about each kind of document like id, extension, filter and its class.
 */
public class GenericDocumentFormat {
	
	private static Vector formats = new Vector();
	static {
		formats.add(new GenericDocumentFormat(XHTMLDocumentWriter.class, "xHTML", "xHTML files (.html, .xhtml)", "html", new String[] {"html", "xhtml"}));
		formats.add(new GenericDocumentFormat(OOoDocumentWriter.class, "OpenOffice.org", "OpenOffice.org files (.odt)", "odt", new String[] {"odt"}));
        formats.add(new GenericDocumentFormat(WikiDocumentWriter.class, "Wiki", "Text files (.txt)", "txt", new String[] {"txt"}));
        //FIXME: LaTeX export disabled as it is not ready for complex documents with large tables
        // formats.add(new GenericDocumentFormat(LaTeXDocumentWriter.class, "LaTeX", "LaTeX files (.tex)", "tex", new String[] {"tex"})); 			
	}
	
	/**
	 * The identifier of the document (eg. xHTML)
	 */
	public String id;
	/**
	 * The descritpion of the document (eg. xHTML files)
	 */
	public String filterDescr;
	/**
	 * The documentWriter to instanciates to write a document of this type (eg. XHTMLDocumentWriter)
	 */
	public Class documentWriterClass;
	/**
	 * The default file extension without trailing point (eg. html)
	 */
	public String defaultExtension;
		
	/**
	 * The files extensions without trailing point (eg. html)
	 */
	public String[] extensionArray;
		
	/**
	 * Define a new generic document format.
	 * @param documentWriterClass : The DocumentWriter sub-class for the format
	 * @param id : The name of the format (for the dropdown menu)
	 * @param filter : an array of filter for the file extention the format can overwrite
	 * @param fillterDescr : a description
	 * @param extention : the extetion to add to the exported file
	 */
	public GenericDocumentFormat(Class documentWriterClass, String id, String filterDescr, String defaultExtension, String[] extensionArray) {
		this.documentWriterClass = documentWriterClass;
		this.id = id;
		this.filterDescr = filterDescr;
		this.defaultExtension = defaultExtension;
		this.extensionArray = extensionArray;
	}
	
	public static GenericDocumentFormat getFormatById(String id) {
		for (Iterator iterator = formats.iterator(); iterator.hasNext();) {
			GenericDocumentFormat format = (GenericDocumentFormat) iterator.next();
			if (format.id.equals(id)) return format;
		}
		return null;
	}
	
	public static GenericDocumentFormat getFormatByExtention(String defaultExtension) {
		for (Iterator iterator = formats.iterator(); iterator.hasNext();) {
			GenericDocumentFormat format = (GenericDocumentFormat) iterator.next();
			if (format.defaultExtension.equals(defaultExtension)) return format;
		}
		return null;
	}
	
	public static Vector getAllFormats() {
		return formats;
	}
	
	public static String[] getFilterOfAllExtensions() {
		String[] s = new String[formats.size()];
		int i = 0;
		for (Iterator iterator = formats.iterator(); iterator.hasNext();) {
			GenericDocumentFormat format = (GenericDocumentFormat) iterator.next();
			s[i++] = "."+format.defaultExtension;
		}
		return s;
	}
	
	public static Vector getAllExtensionsNames() {
		Vector v = new Vector(formats.size());
		for (Iterator iterator = formats.iterator(); iterator.hasNext();) {
			GenericDocumentFormat format = (GenericDocumentFormat) iterator.next();
			v.add(format.defaultExtension);
		}
		return v;
	}
		
	public GsPluggableActionDescriptor[] getT_action(int actionType, GsGraph graph) {
		return null;
	}
	
}