package fr.univmrs.tagc.GINsim.regulatoryGraph;

import java.awt.GridBagConstraints;
import java.util.ArrayList;

import javax.swing.Action;

import org.ginsim.exception.NotificationMessage;
import org.ginsim.exception.NotificationMessageAction;
import org.ginsim.exception.NotificationMessageHolder;
import org.ginsim.graph.common.Graph;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;

import fr.univmrs.tagc.GINsim.annotation.Annotation;
import fr.univmrs.tagc.common.datastore.GenericList;
import fr.univmrs.tagc.common.datastore.GenericPropertyInfo;
import fr.univmrs.tagc.common.datastore.ObjectEditor;
import fr.univmrs.tagc.common.datastore.gui.GenericPropertyEditorPanel;
import fr.univmrs.tagc.common.managerresources.Translator;

public class RegulatoryEdgeEditor extends ObjectEditor<GsRegulatoryMultiEdge> {

	private final Graph graph;
	private final GraphGUI gui;
	GsRegulatoryEdge edge;
	EdgeList edgeList;
	
	private static final int ANNOTATION = 0; 
	private static final int EDGES = 1; 
	private static final int EDGE = 2; 
	private static final int SOURCE = 11;
	private static final int TARGET = 12;
	
	static {
		GenericPropertyEditorPanel.addSupportedClass(GsRegulatoryEdge.class, RegulatoryEdgeEditPanel.class);
	}
	
	public RegulatoryEdgeEditor(GsRegulatoryGraph graph) {
		this.graph = graph;
		this.gui = GUIManager.getInstance().getGraphGUI(graph);
		master = graph;
		
		// info on top
		GenericPropertyInfo pinfo = new GenericPropertyInfo(this, SOURCE, 
				Translator.getString("STR_from"), Action.class);
		pinfo.addPosition(0, 0);
		pinfo.addPosition(1, 0);
		v_prop.add(pinfo);
		pinfo = new GenericPropertyInfo(this, TARGET, 
				Translator.getString("STR_to"), Action.class);
		pinfo.addPosition(2, 0);
		pinfo.addPosition(3, 0);
		v_prop.add(pinfo);
		
		// edge list
		edgeList = new EdgeList(graph);
		pinfo = new GenericPropertyInfo(this, EDGES, null, GenericList.class);
		pinfo.addPosition(0, 1, 5, 1, 1, 1, GridBagConstraints.SOUTH);
		v_prop.add(pinfo);
		
		// edge edit panel
		pinfo = new GenericPropertyInfo(this, EDGE, null, GsRegulatoryEdge.class);
		pinfo.data = graph;
		pinfo.addPosition(0, 2, 5, 1, 1, 0, GridBagConstraints.SOUTH);
		v_prop.add(pinfo);
		
		// annotation
		pinfo = new GenericPropertyInfo(this, ANNOTATION, null, Annotation.class);
		pinfo.addPosition(5, 0, 1, 3, 4, 1, GridBagConstraints.SOUTH);
		v_prop.add(pinfo);
	}
	
	@Override
	public void setEditedItem(GsRegulatoryMultiEdge o) {
		if (o == null) {
			return;
		}
		this.edge = o.getEdge(0);
		edgeList.setMEdge(o);
		super.setEditedItem(o);
	}


	public Object getRawValue(int prop) {
		switch (prop) {
			case ANNOTATION:
				return edge.annotation;
			case EDGES:
				return edgeList;
			case EDGE:
				return edge;
		}
		return null;
	}

	public boolean setValue(int prop, int value) {
		switch (prop) {
			case EDGES:
				if (value > -1 && value < o.getEdgeCount()) {
					edge = o.getEdge(value);
					
					// FIXME: hack to trigger a refresh and avoid an infinite loop
					// it should be replaced by something cleaner...
					refresh(false);
					return true;
				}
		}
		return false;
	}

	public int getIntValue(int prop) {
		return 0;
	}
	public String getStringValue(int prop) {
		switch (prop) {
			case SOURCE:
				return o.getSource().getId();
			case TARGET:
				return o.getTarget().getId();
		}
		return null;
	}

	public void performAction(int prop) {
		switch (prop) {
			case SOURCE:
				gui.selectVertex(o.getSource());
				break;
			case TARGET:
				gui.selectVertex(o.getTarget());
				break;
		}
	}

	public boolean isValidValue(int prop, String value) {
		return false;
	}

	public boolean isValidValue(int prop, int value) {
		return false;
	}

	public boolean setValue(int prop, String value) {
		return false;
	}
}

class EdgeList extends GenericList {

	GsRegulatoryGraph graph;
	GsRegulatoryMultiEdge medge;
	
	EdgeList(GsRegulatoryGraph graph) {
		this.graph = graph;
		addOptions = new ArrayList();
		canAdd = true;
		canEdit = true;
		canRemove = true;
		t_type = new Class[1];
		t_type[0] = GsRegulatoryEdge.class;
	}
	
	void setMEdge(GsRegulatoryMultiEdge medge) {
		this.medge = medge;
		addOptions.clear();
	    int[] t = medge.getFreeValues();
	    for (int i=0 ; i<t.length ; i++) {
	    	int th = t[i];
	    	if (th != -1) {
	    		addOptions.add(new Integer(t[i]));
	    	}
	    }
		refresh();
	}

    protected void addEdge(int value) {
		int index = medge.addEdge(GsRegulatoryMultiEdge.SIGN_POSITIVE, value, graph);
		if (index != -1) {
			setMEdge(medge);
		}
    }
    
	public int add(int position, int mode) {
		if (mode == -1 || mode >= addOptions.size()) {
			NotificationMessageAction notifAction = new AddEdgeNotificationAction(this);
	    	graph.addNotificationMessage(new NotificationMessage(graph,
	    			Translator.getString("STR_noMoreValueForInteraction"),
	    			notifAction,
	    			medge,
	    			NotificationMessage.NOTIFICATION_WARNING));
	    	return -1;
		}
		this.addEdge(((Integer)addOptions.get(mode)).intValue());
		return 0;
	}

	public Object getElement(String filter, int startIndex, int i) {
		return medge.getEdge(i);
	}

	public int getNbElements(String filter, int startIndex) {
		return medge.getEdgeCount();
	}
	public boolean remove(String filter, int startIndex, int[] t_index) {
		if (medge.getEdgeCount() > 1 && t_index.length == 1) {
			medge.removeEdge(t_index[0], graph);
			setMEdge(medge);
			return true;
		}
		return false;
	}

	public boolean edit(String filter, int startIndex, int i, int col, Object o) {
		return false;
	}
	public boolean move(int[] sel, int diff) {
		return false;
	}
	public void run(String filter, int startIndex, int row, int col) {
	}
}

class AddEdgeNotificationAction implements NotificationMessageAction {
	EdgeList edgeList;
	AddEdgeNotificationAction(EdgeList edgeList) {
		this.edgeList = edgeList;
	}
	public boolean timeout( NotificationMessageHolder graph, Object data) {
		return true;
	}

	public boolean perform( NotificationMessageHolder graph, Object data, int index) {
		if (edgeList.medge == data) {
			GsRegulatoryVertex vertex = ((GsRegulatoryMultiEdge)data).getSource();
			vertex.setMaxValue((byte)(vertex.getMaxValue()+1), (GsRegulatoryGraph)graph);
			edgeList.add();
			return true;
		}
		return false;
	}
	public String[] getActionName() {
		String[] t = {"add value"};
		return t;
	}
}
