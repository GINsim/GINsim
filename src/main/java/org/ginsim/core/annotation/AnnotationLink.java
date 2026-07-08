package org.ginsim.core.annotation;

import java.util.HashMap;
import java.util.Map;

import org.ginsim.common.utils.OpenHelper;
import org.ginsim.common.utils.OpenUtils;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;


/**
 * A link in an annotation.
 * It has a "protocol" (database, bibliography, normal link) and a value (ID or real value).
 * 
 * @author Aurelien Naldi
 */
public class AnnotationLink {

	/**
	 * helper as Map
	 */
	protected static Map m_helper = new HashMap();
	static {
		HttpHelper.setup();
		MiriamURNHelper.setup();
	}
	static void addHelperClass( String key, String objectKey) {
		
		m_helper.put(key, objectKey);
	}
	
	private OpenHelper helper = null;
	String proto;
	String value;

	/**
	 * Constructor
	 * @param s string of annotation link
	 * @param graph the graph
	 */
	public AnnotationLink(String s, Graph graph) {
		setText(s, graph);
	}

	/**
	 * Setter for text
	 * @param s string of annotation link
	 * @param graph the graph
	 */
	public void setText(String s, Graph graph){
		String[] ts = s.split(":", 2);
		if (ts.length == 1) {
			this.helper = null;
			this.proto = "";
			this.value = s;
			return;
		}
		proto = ts[0].trim();
		value = ts[1].trim();
		String okey = (String)m_helper.get(proto);
		if (graph!= null && okey != null) {
			helper = (OpenHelper) ObjectAssociationManager.getInstance().getObject(graph, okey, true);
			if (getHelper() != null) {
				getHelper().add(proto, value);
			}
		}
	}

	/**
	 * Open function
	 */
	public void open() {
		if (getHelper() != null) {
			getHelper().open(proto, value);
			return;
		}
		// no helper, use a generic open call
		OpenUtils.open(proto, value);
	}

	/**
	 * To string function
	 * @return a description string
	 */
	public String toString() {
		if (proto == null || proto.equals("")) {
			return value;
		}
		return proto+":"+value;
	}

	/**
	 * Reteur helper
	 * @return OpenHelper threath
	 */
	public OpenHelper getHelper() {
		return helper;
	}

	/**
	 * Proto getter
	 * @return string of proto
	 */
	public String getProto() {
		return proto;
	}

	/**
	 * Getter value
	 * @return value as string
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Link getter
	 * @return link as string
	 */
	public String getLink() {
		if (getHelper() != null) {
			return getHelper().getLink(proto, value);
		}

		return OpenUtils.getLink(proto, value);
	}

	/**
	 * CTest equality of link
	 * @param o object to compare
	 * @return boolean if equal
	 */
	public boolean equals(Object o) {
		return toString().equals(o.toString());
	}
}
