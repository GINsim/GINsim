package org.ginsim.common.utils;

/**
 * Define methods required to handle open actions associated to a given "protocol"
 * 
 * @author Aurelien Naldi
 */
public interface OpenHelper {
	
	/**
	 * Open a link.
	 * 
	 * @param proto proto string
	 * @param value string value
	 * @return true if the link could be opened, false otherwise.
	 */
	public boolean open(String proto, String value);
	
	/**
	 * Create a new link.
	 * Most handlers do not need to do anything here,
	 * some may want to populate a database (bibliography for example)
	 * 
	 * @param proto proto string
	 * @param value string value
	 */
	public void add(String proto, String value);
	
	/**
	 * Create a standard link for this protocol-based link.
	 * 
	 * @param proto proto string
	 * @param value string value
	 * @return a link that can be managed with the system "open" command.
	 */
	public String getLink(String proto, String value);
}
