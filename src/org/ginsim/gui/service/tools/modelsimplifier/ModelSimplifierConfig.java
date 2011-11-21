package org.ginsim.gui.service.tools.modelsimplifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ginsim.annotation.Annotation;
import org.ginsim.graph.regulatorygraph.RegulatoryVertex;

import fr.univmrs.tagc.common.datastore.MultiColHelper;
import fr.univmrs.tagc.common.datastore.NamedObject;
import fr.univmrs.tagc.common.xml.XMLWriter;
import fr.univmrs.tagc.common.xml.XMLize;


public class ModelSimplifierConfig implements NamedObject, XMLize, MultiColHelper<RegulatoryVertex> {
	String name;
	Annotation note = new Annotation();
	Map<RegulatoryVertex, List<RegulatoryVertex>> m_removed = new HashMap<RegulatoryVertex, List<RegulatoryVertex>>();
	boolean	strict = true;

	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public void toXML(XMLWriter out, Object param, int mode)
			throws IOException {
		if (m_removed.size() < 1) {
			return;
		}
		String s_removed = "";
		for (RegulatoryVertex v: m_removed.keySet()) {
			s_removed += " "+v;
		}
		out.openTag("simplificationConfig");
		out.addAttr("name", this.name);
		out.addAttr("strict", ""+this.strict);
		out.addAttr("removeList", s_removed.substring(1));
		note.toXML(out, param, mode);
		out.closeTag();
	}
	
	@Override
	public Object getVal(RegulatoryVertex o, int index) {
		if (index == 1) {
			return m_removed.containsKey(o) ? Boolean.TRUE : Boolean.FALSE;
		}
		return o;
	}
	@Override
	public boolean setVal(RegulatoryVertex vertex, int index, Object value) {
		if (index == 1) {
			if (value.equals(Boolean.TRUE)) {
				m_removed.put(vertex, null);
			} else {
				m_removed.remove(vertex);
			}
			return true;
		}
		return false;
	}
}
