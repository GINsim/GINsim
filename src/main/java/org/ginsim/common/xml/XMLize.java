package org.ginsim.common.xml;

import java.io.IOException;

/**
 * Interface for object that can save themselves to XML.
 */
public interface XMLize {
    
    /**
     * write the XML representation of this object to a file
     * 
     * @param out XMLWriter
     * @throws IOException exception
     */
	public void toXML(XMLWriter out) throws IOException;	
}
