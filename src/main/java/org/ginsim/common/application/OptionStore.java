package org.ginsim.common.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.ginsim.common.xml.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Handle options: remember them during the session, restore them when first called
 * and save them when exiting.
 * 
 * @author Aurelien Naldi
 */
public class OptionStore extends DefaultHandler {
	
    // some static stuff
    private static Map<String, Object> m_option = new TreeMap();
    private static String optionFile = null;
	private static List<String> recentFiles = new ArrayList<String>();
    
	/**
	 * Initialise the option store: this should be called by the launcher once and only once.
	 * 
	 * @param name
	 * @throws Exception
	 */
    public static void init(String name) throws Exception {
    	
    	String home = System.getProperty("user.home");
    	String prefix = home;
    	switch (CurrentOS.CURRENT_OS) {
		case MACOSX:
			prefix = home+"/Library/Preferences/";
			break;
		case LINUX:
			prefix = home+"/.config/";
			break;
		case WINDOWS:
			prefix = home+"\\Application Data\\";
			break;
		}
    	
    	File f = new File(prefix);
    	if (!f.exists()) {
    		f.mkdirs();
    	}
    	if (!f.isDirectory()) {
    		throw new Exception("Could not find the base directory for option file: "+prefix);
    	}
    	optionFile = prefix + name + ".xml";
    	
        File f_option = new File( optionFile);
        if (f_option.exists()) {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            OptionStore options = new OptionStore();
            XMLReader xr;
            try {
                SAXParser sp = spf.newSAXParser();
                xr = sp.getXMLReader();
                xr.setContentHandler(options);
                xr.setEntityResolver(options);
                xr.setErrorHandler(options);
                FileReader r = new FileReader(f_option);
                xr.parse(new InputSource(r));
            } catch (Exception e) { 
            	throw new Exception( "Error in the configuration file: "+optionFile+"\n"+ e.getLocalizedMessage());
            }
        }
    }
    
    /**
     * Add file path to recent file list, at the end of the list.
     * This is only used internally to restore the list of recent files at startup.
     * 
     * @param path the path to add to the list
     */
	private static void appendRecentFile(String path) {
		recentFiles.remove(path);
		int last = recentFiles.size();
		if (last < 10) {
			recentFiles.add(path);
		}
	}
    /**
     * Add file path to recent file list.
     * The file will take the first position in the list.
     * 
     * @param path the path to add to the list
     */
	public static void addRecentFile(String path) {
		// add this file on top of the list
		if (!new File(path).exists()) {
			return;
		}
		recentFiles.remove(path);
		recentFiles.add(0, path);
		
		// trim the list size
		while (recentFiles.size() > 10) {
			recentFiles.remove(10);
		}
        saveOptions();
	}
	
	/**
	 * Get the list of recent files.
	 * Note: the recent menu will be managed by this class directly,
	 * this method should only be used to save the list of recent files before closing GINsim
	 * 
	 * @return the list of recent files
	 */
	public static List<String> getRecentFiles() {
		List<String> filteredRecentFiles = new ArrayList<String>();
		for (String filename: recentFiles) {
			File f = new File(filename);
			if (f.exists()) {
				filteredRecentFiles.add(filename);
			}
		}
		recentFiles = filteredRecentFiles;
		return recentFiles;
	}
    
    /**
     * save the value of an option. accepted types: Boolean, Integer, String
     * 
     * @param name unique name (key) of the option
     * @param value an object giving the value of this option
     */
    public static void setOption(String name, Object value) {
        if (value instanceof Boolean ||
                value instanceof Integer ||
                value instanceof String) {
            
            m_option.put(name, value);
        }
    }
    /**
     * get the saved value of an option
     * 
     * @param name uniq name (key) of the option
     * @return an object representing the value of this option (null if not defined)
     */
    public static Object getOption(String name) {
        return m_option.get(name);
    }
    /**
     * get the saved value of an option with a fallback value.
     * If the option was not defined or if types mismatch, it will be set to the default value.
     * @param name unique name (key) of the option
     * @param defValue value to return if this option is not defined
     * @return an object representing the value of this option
     */
    public static <T> T getOption(String name, T defValue) {
        Object o = m_option.get(name);
        
        if (o != null && o.getClass().equals(defValue.getClass())) {
            return (T)o;
        }
        m_option.put(name, defValue);
        return defValue;
    }
    /**
     * remove a saved option.
     * @param name unique name (key) of the option
     */
    public static void removeOption(String name) {
        m_option.remove(name);
    }
    /**
     * save all options in a file, to restore them at next run.
     */
    public static void saveOptions() {
        
        List<String> recents = OptionStore.getRecentFiles();
        if (recents.size() == 0 && m_option.size() == 0) {
            return;
        }
        try {
            OutputStreamWriter fos = new OutputStreamWriter(new FileOutputStream(optionFile), "UTF-8");
            XMLWriter out = new XMLWriter(fos);
            out.openTag("gsconfig");
            for (String recent: recents) {
                out.openTag("recent");
                out.addAttr("filename", recent);
                out.closeTag();
            }
            Iterator it = m_option.keySet().iterator();
            for (String k: m_option.keySet()) {
                Object v = m_option.get(k);
                String t;
                if (v instanceof Boolean) {
					t = "boolean";
				} else if (v instanceof Integer) {
					t = "integer";
				} else {
					t = "string";
				}
                out.openTag("option");
                out.addAttr("key", k.toString());
                out.addAttr("type", t);
                out.addAttr("value", v.toString());
                out.closeTag();
            }
            out.closeTag();
            fos.close();
        } catch (Exception e) {}
    }

    
    /* ********************************
     *      parse options 
     **********************************/
    /*
     * exemple of config file:
     * 
     * <gsconfig>
     *  <recent filename="/path/to/file.ginml"/>
     *  <recent filename="/anotherpath/to/file.ginml"/>
     *  <option key="cle" type="string" value="valeur"/>
     *  <option key="autrecle" type="integer" value="123"/>
     * </gsconfig>
     * 
     * 
     * all that is needed is an handler for startElement
     */
    /**
     * Handler to parse the saved file.
     */
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (qName.equals("recent")) {
        	OptionStore.appendRecentFile(attributes.getValue("filename"));
        } else if (qName.equals("option")) {
            String k = attributes.getValue("key");
            String sv = attributes.getValue("value");
            String t = attributes.getValue("type");
            Object v;
            if ("boolean".equals(t)) {
				v = new Boolean(sv);
			} else if ("integer".equals(t)) {
				v = new Integer(sv);
			} else {
				v = sv;
			}

            setOption(k, v);
        }
    }
    
}
