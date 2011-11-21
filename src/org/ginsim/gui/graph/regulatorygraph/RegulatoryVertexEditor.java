package org.ginsim.gui.graph.regulatorygraph;

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;

import org.ginsim.annotation.Annotation;
import org.ginsim.annotation.AnnotationPanel;
import org.ginsim.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.graph.regulatorygraph.RegulatoryVertex;
import org.ginsim.gui.graph.regulatorygraph.logicalfunction.LogicalFunctionPanel;
import org.ginsim.gui.graph.regulatorygraph.models.VertexMaxValueSpinModel;

import fr.univmrs.tagc.common.Tools;
import fr.univmrs.tagc.common.datastore.GenericPropertyInfo;
import fr.univmrs.tagc.common.datastore.ObjectEditor;
import fr.univmrs.tagc.common.datastore.gui.GenericPropertyEditorPanel;
import fr.univmrs.tagc.common.datastore.models.SpinModel;
import fr.univmrs.tagc.common.managerresources.Translator;

public class RegulatoryVertexEditor extends ObjectEditor<RegulatoryVertex> {

	public static final int PROP_ID = 0;
	public static final int PROP_NAME = 1;
    public static final int PROP_MAX = 2;
    public static final int PROP_INPUT = 3;
	public static final int PROP_ANNOTATION = 5;
	public static final int PROP_RAW = 10;
	private List l_prop = new ArrayList();
	
	RegulatoryGraph graph;

	static {
		GenericPropertyEditorPanel.addSupportedClass(Annotation.class, AnnotationPanel.class);
		GenericPropertyEditorPanel.addSupportedClass(RegulatoryVertex.class, GsInteractionPanel.class);
		GenericPropertyEditorPanel.addSupportedClass(LogicalFunctionPanel.class, LogicalFunctionPanel.class);
	}
	
	public RegulatoryVertexEditor(RegulatoryGraph graph) {
		this.graph = graph;
		master = graph;
		GenericPropertyInfo pinfo = new GenericPropertyInfo(this, PROP_ID, Translator.getString("STR_id"), String.class);
		l_prop.add(pinfo);
		pinfo = new GenericPropertyInfo(this, PROP_NAME, Translator.getString("STR_name"), String.class);
		l_prop.add(pinfo);
		pinfo = new GenericPropertyInfo(this, PROP_RAW, Translator.getString("STR_max"), SpinModel.class);
		pinfo.data = new VertexMaxValueSpinModel(graph);
		pinfo.addPosition(0,3);
		pinfo.addPosition(1, 3);
		pinfo.addPosition(0, 2);
		pinfo.addPosition(1, 2);
		l_prop.add(pinfo);
        pinfo = new GenericPropertyInfo(this, PROP_INPUT, Translator.getString("STR_Fixed_input"), Boolean.class);
        l_prop.add(pinfo);

		// build the group [note, parameter, function]
		GenericPropertyInfo[] t = new GenericPropertyInfo[3];
		pinfo = new GenericPropertyInfo(this, PROP_ANNOTATION, Translator.getString("STR_notes"), Annotation.class);
		t[0] = pinfo;
		pinfo = new GenericPropertyInfo(this, PROP_RAW, Translator.getString("STR_parameters"), RegulatoryVertex.class);
		pinfo.data = graph;
		t[1] = pinfo;
		pinfo = new GenericPropertyInfo(this, PROP_RAW, Translator.getString("STR_function"), LogicalFunctionPanel.class);
		pinfo.data = graph;
		t[2] = pinfo;
		
		// and add the group
		pinfo = new GenericPropertyInfo(this, -1, null, GenericPropertyInfo[].class);
		pinfo.data = t;
		pinfo.name = Translator.getString("STR_parameters");
		pinfo.addPosition(0, 4, 2, 1, 0, 0, GridBagConstraints.SOUTH);
		pinfo.addPosition(2, 0, 1, 5, 1, 1, GridBagConstraints.SOUTH);
		l_prop.add(pinfo);
	}
	
	public int getIntValue(int prop) {
		switch (prop) {
            case PROP_MAX:
                return o.getMaxValue();
            case PROP_INPUT:
                return o.isInput() ? 1 : 0;
		}
		return 0;
	}

	public List getProperties() {
		return l_prop;
	}

	public String getStringValue(int prop) {
		switch (prop) {
			case PROP_ID:
				return o.getId();
			case PROP_NAME:
				return o.getName();
			case PROP_MAX:
				return ""+o.getMaxValue();
            case PROP_INPUT:
                return ""+o.isInput();
		}
		return null;
	}

	public boolean isValidValue(int prop, String value) {
		try {
			switch (prop) {
				case PROP_ID:
					return Tools.isValidId(value) && !graph.idExists(value);
				case PROP_NAME:
					return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public boolean isValidValue(int prop, int value) {
		switch (prop) {
			case PROP_MAX:
				return value>0 && value<10;
            case PROP_INPUT:
                return value== 0 || value==1;
		}
		return false;
	}

	public boolean setValue(int prop, String value) {
		try {
			switch (prop) {
				case PROP_ID:
					graph.changeVertexId(o, value);
					return true;
				case PROP_NAME:
					o.setName(value);
					return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public boolean setValue(int prop, int value) {
		switch (prop) {
			case PROP_MAX:
				o.setMaxValue((byte)value, graph);
				return o.getMaxValue() == value;
            case PROP_INPUT:
                boolean nv = value != 0;
                o.setInput(nv, graph);
                return o.isInput() == nv;
		}
		return false;
	}

	public Object getRawValue(int prop) {
		switch (prop) {
			case PROP_ANNOTATION:
				return o.getAnnotation();
			case PROP_RAW:
				return o;
            case PROP_INPUT:
                return o.isInput() ? Boolean.TRUE : Boolean.FALSE;
		}
		return null;
	}

	@Override
	public Object[] getArgs() {
		return new Object[] {graph};
	}

}