package org.ginsim.gui.shell.editpanel;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;

import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.GraphChangeType;
import org.ginsim.gui.annotation.AnnotationTab;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.graph.GraphGUIListener;
import org.ginsim.gui.graph.GraphSelection;
import org.ginsim.gui.graph.view.style.StyleTab;


/**
 * Tabbed panel which follows the current selection.
 * 
 * TODO: should we put back some of the old activation and selection logics?
 * 
 * @author Aurelien Naldi
 */
public class EditPanel extends JTabbedPane implements GraphGUIListener {

    private final GraphGUI<?, ?, ?> gui;
    private final List<EditTab> tabs = new ArrayList<EditTab>();
    
	public EditPanel( GraphGUI<?, ?, ?> gui) {
		this.gui = gui;
		gui.addGraphGUIListener(this);
		addTab(new MainEditTab(gui));
		addTab(new StyleTab(gui));
		EditTab t = AnnotationTab.prepareTab(gui);
		if (t != null) {
			addTab(t);
		}
	}
	
	/**
	 * Add an editing tab.
	 * 
	 * @param tab the tab to add
	 */
    public void addTab(EditTab tab) {
    	String name = tab.getTitle();
    	Component panel = tab.getComponent();
        addTab(name, null, panel, null);
        tabs.add(tab);
        updateTabs(gui.getSelection());
    }

    /**
     * enable/disable tabs depending on the constraint
     * if the selected tab becomes inactive, select another one
     */
    private void updateTabs( GraphSelection<?, ?> selection) {
        for (int i=0 ; i<tabs.size() ; i++) {
        	boolean enabled = tabs.get(i).isActive(selection);
        	setEnabledAt(i, enabled);
        }
    }
    
    /**
     * Remove an editing tab.
     * 
     * @param name
     * @return
     */
    public boolean removeTab(String name) {
        int i = indexOfTab(name);
        if (i != -1) {
            removeTabAt(i);
            tabs.remove(i);
            return true;
        }
        return false;
    }

	@Override
	public void graphSelectionChanged(GraphGUI gui) {
		updateTabs(gui.getSelection());
	}

	@Override
	public void graphGUIClosed(GraphGUI gui) {
		tabs.clear();
		removeAll();
	}

	@Override
	public void graphChanged(Graph g, GraphChangeType type, Object data) {
		// TODO Auto-generated method stub
	}

}
