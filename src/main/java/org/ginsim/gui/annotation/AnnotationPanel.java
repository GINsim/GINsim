package org.ginsim.gui.annotation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.ginsim.common.application.LogManager;
import org.ginsim.core.annotation.Annotation;
import org.ginsim.core.annotation.AnnotationLink;
import org.ginsim.core.graph.AbstractGraph;
import org.ginsim.core.graph.Graph;
import org.ginsim.gui.utils.data.*;

/**
 * Panel to edit annotations
 */
public class AnnotationPanel extends JPanel
	implements ObjectPropertyEditorUI, TableModelListener {

	private static final long serialVersionUID = -8542547209276966234L;

	private Graph graph = null;
	private Annotation currentNote = null;
    private boolean listenChanges = true;
    private List linkList = null;
    private LinkListHelper helper = null;
	private ListPanel linkListPanel = null;
	private JTextArea jTextArea = null;

	private GenericPropertyInfo	pinfo;
	/**
	 * This method initializes 
	 * 
	 */
	public AnnotationPanel() {
        super(new GridBagLayout());

        setPreferredSize(new java.awt.Dimension(800, 60));

        GridBagConstraints cst = new GridBagConstraints();
        cst.gridx = 0;
        cst.gridy = 0;
        cst.weightx = 1.0;
        cst.weighty = 1.0;
        cst.fill = GridBagConstraints.BOTH;
        helper = new LinkListHelper(graph);
        linkListPanel = new ListPanel(helper, "Annotations");
        linkListPanel.setList(linkList);
        this.add(linkListPanel, cst);

        cst.gridx++;
        jTextArea = new JTextArea();
        jTextArea.setLineWrap(true);
        jTextArea.setWrapStyleWord(true);
        jTextArea.getDocument().addDocumentListener(new DocumentListener() {
            public void removeUpdate(DocumentEvent e) {
                applyComment();
            }
            public void insertUpdate(DocumentEvent e) {
                applyComment();
            }
            public void changedUpdate(DocumentEvent e) {
                applyComment();
            }
        });

        JScrollPane sp = new JScrollPane();
        sp.setViewportView(jTextArea);
        this.add(sp, cst);
	}

    /**
     * Edited object setter
     * @param obj edited object
     */
    public void setEditedObject(Object obj) {
        if (obj instanceof Annotation) {
            setAnnotation((Annotation)obj);
        }
    }

    /**
     * Annotation setter
     * @param annotation  the annotation object
     */
    public void setAnnotation(Annotation annotation) {
        listenChanges = false;
        currentNote = annotation;
        linkList = currentNote.getLinkList();
        linkListPanel.setList(linkList);
        jTextArea.setText(currentNote.getComment());
        listenChanges = true;
    }

    /**
     * apply comment function
      */
    protected void applyComment() {
    	if (currentNote == null) {
    		return;
    	}
    	String new_text = jTextArea.getText();
        if (!currentNote.getComment().equals(new_text)) {
        		currentNote.setComment(new_text);
            if (listenChanges && graph != null) {
                ((AbstractGraph) graph).fireMetaChange();
            }
        }
    }
    
    public void tableChanged(TableModelEvent e) {
        if (listenChanges && graph != null) {
        	((AbstractGraph) graph).fireMetaChange();
        }
    }
	public void apply() {
	}
	public void refresh(boolean force) {
		setEditedObject(pinfo.getRawValue());
	}
	public void setEditedProperty(GenericPropertyInfo pinfo, GenericPropertyHolder panel) {
		this.pinfo = pinfo;
        Graph graph = (Graph)pinfo.editor.getMasterObject();
        if (graph != this.graph) {
            this.graph = graph;
            helper.graph = graph;
            LogManager.error("Different graph in the annotation panel!!");
        }
		panel.addField(this, pinfo, 0);
	}
	
	@Override
	public void release() {
	}
}

class LinkListHelper extends ListPanelHelper<AnnotationLink, List<AnnotationLink>> {

    private static final ColumnDefinition[] COLUMNS = new ColumnDefinition[] {ColumnDefinition.EDITME};
    Graph graph;

    public LinkListHelper( Graph graph) {
		this.graph = graph;
	}

    @Override
    public int addInline(List<AnnotationLink> list, String s) {
        AnnotationLink link = new AnnotationLink(s, graph);
        int idx = list.size();
        if (list.add(link)) {
            return idx;
        }
        return -1;
    }

    @Override
    public boolean setValue(List<AnnotationLink> list, int row, int column, Object value) {
        if (row <0 || row >= list.size() || column != 0) {
            return false;
        }
        AnnotationLink link = list.get(row);
        link.setText(value.toString(), graph);
        return true;
    }

    @Override
    public void runAction(List<AnnotationLink> list, int row, int col) {
        list.get(row).open();
    }

    @Override
    public boolean doRemove(List<AnnotationLink> list, int[] sel) {
        boolean result = removeItems(list, sel);
        if (result) {
        	// FIXME: force text refresh
        }
        return result;
    }

    @Override
    public ColumnDefinition[] getColumns() {
        return COLUMNS;
    }

    @Override
    public String[] getActionLabels() {
        return new String[] {"#"};
    }
}
