package org.ginsim.gui.utils.data;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.ginsim.common.utils.ListReorderListener;
import org.ginsim.gui.utils.widgets.EnhancedJTable;
import org.ginsim.gui.utils.widgets.SplitPane;
import org.ginsim.gui.utils.widgets.StockButton;


/**
 * Generic UI to display the content of a list.
 * It offers optional UI to reorder and alter the content of a list.
 * It works with any type of list, relying on a helper to provide
 * capabilities and methods to alter the list.
 * It can be used as part of a ListEditionPanel.
 * @param <T>   panel
 * @param <L>  list exted
 */
public class ListPanel<T, L extends List<T>> extends JPanel
	implements ListSelectionListener {
    
	JScrollPane sp = new JScrollPane();
    protected final ListPanelHelper<T, L> helper;
    protected L list = null;

    private final SimpleListModel<T,L> model;
    private final EnhancedJTable jl;
    private final ListEditionPanel<T,L> editionPanel;
    int autohide = -1;
    
    JButton b_up;
    JButton b_down;
    JButton b_add;
    JButton b_del;
    JLabel l_title = new JLabel();
    
    protected JPanel p_right;
    protected JPanel targetpanel;
    protected CardLayout cards;

	// TODO: fix ugly hack for the size of the action column
    static final int actionSize = 30;
    static final int minColumnSize = 50;

    public ListPanel(ListPanelHelper<T,L> helper, String name) {
        this(helper, name, null);
    }

    public ListPanel(ListPanelHelper<T,L> helper, String name, ListEditionPanel editionPanel) {
    	this.helper = helper;
        this.editionPanel = editionPanel;

        model = new SimpleListModel<T,L>(helper);
        jl = new EnhancedJTable(model);
    	jl.addActionListener(model);
        jl.getSelectionModel().addListSelectionListener(this);
        jl.setMinimumSize(new Dimension(60, 60));

        // Hide column names if none is provided
        if (!helper.hasNamedColumn()) {
            jl.setTableHeader(null);
        }

    	targetpanel = this;
    	if (helper.m_right != null) {
    		setLayout(new GridLayout());
    		JSplitPane split = new SplitPane();
    		targetpanel = new JPanel();
    		p_right = new JPanel();
    		cards = new CardLayout();
    		p_right.setLayout(cards);
    		p_right.add(new JPanel(), "empty");
    		Iterator<Entry<Class<?>, Component>> it = helper.m_right.entrySet().iterator();
    		while (it.hasNext()) {
    			Entry<Class<?>, Component> e = it.next();
    			p_right.add(e.getValue(), e.getKey().toString());
    		}
    		cards.show(p_right, "empty");
    		split.setRightComponent(p_right);
    		split.setLeftComponent(targetpanel);
    		split.setDividerLocation( 150);
    		this.add(split);
    		split.setName(name+".split");
    	}
        targetpanel.setLayout(new GridBagLayout());
        sp.setViewportView(jl);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,5,4, 5);
        targetpanel.add(l_title, c);
		l_title.setVisible(false);
		
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        
        // TODO: use java6 jtable filtering feature
//        t_filter = new StatusTextField("filter", true);
//        t_filter.addKeyListener(this);
//        targetpanel.add(t_filter, c);
		
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.gridheight = 7;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        targetpanel.add(sp, c);
        
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 4;
        b_del = new StockButton("list-remove.png", true);
        b_del.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doRemove();
            }
        });
        targetpanel.add(b_del, c);
        
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        b_add = new StockButton("list-add.png", true);
        b_add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doAdd(e);
            }
        });
        targetpanel.add(b_add, c);
        
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 5;
        b_up = new StockButton("go-up.png", true);
        b_up.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doMoveUp();
            }
        });
        targetpanel.add(b_up, c);
        
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 6;
        b_down = new StockButton("go-down.png", true);
        b_down.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doMoveDown();
            }
        });
        targetpanel.add(b_down, c);
    }

    /**
     * set when to autohide most of this widget.
     * (when the list contains <code>autohide</code> elements or less, 
     * only the "add" button will remain visible.
     * default value is -1, so this will never happen, encouraged values are 0 or 1.
     * @param autohide
     */
    public void setAutoHide(int autohide) {
        this.autohide = autohide;
        refreshHide();
    }
    
    public void setTitle(String title) {
    	if (title == null) {
    		l_title.setVisible(false);
    		return;
    	}
    	l_title.setText(title);
		l_title.setVisible(true);
    }
    
    public int[] getSelection() {
    	return jl.getSelectedRows();
    }

    public T getSelectedItem() {
        int[] selection = getSelection();
        if (selection == null || selection.length < 1) {
            return null;
        }

        int idx = selection[0];
        if (idx < 0) {
            return null;
        }
        return list.get(idx);
    }

    public void selectItem(int idx) {
        jl.setRowSelectionInterval(idx, idx);
    }

    public L getList() {
        return this.list;
    }


    /**
     * set the list to show.
     * @param list
     */
    public void setList(L list) {
        this.list = list;
        if (list == null) {
            b_up.setVisible(false);
            b_down.setVisible(false);
            b_add.setVisible(false);
            b_del.setVisible(false);
            // TODO: hide filter label
            jl.setEnabled(false);
            return;
        }
        model.setList(list, this);
        jl.setEnabled(true);
        
        b_up.setVisible(helper.canOrder);
        b_down.setVisible(helper.canOrder);
        b_add.setVisible(helper.canCreate());
        b_del.setVisible(helper.canRemove());

        if (list.size() > 0) {
            jl.getSelectionModel().setSelectionInterval(0, 0);
        }

        String[] labels = helper.getActionLabels();
        TableColumnModel columnModel = jl.getColumnModel();
        int i=0;
        if (labels != null) {
            for ( ; i<labels.length ; i++) {
                columnModel.getColumn(i).setMaxWidth(actionSize);
            }
        }

        for (ColumnDefinition cdef: helper.getColumns()) {
            if (cdef.fixedSize > 0) {
                columnModel.getColumn(i).setMaxWidth(cdef.fixedSize);
            } else {
                columnModel.getColumn(i).setMinWidth(minColumnSize);
            }
            i++;
        }
    }
    
	protected void doMoveUp() {
        if (list == null || !helper.canOrder) {
            return;
        }
        int[] index = jl.getSelectedRows();
        if (!helper.moveData(list, index, -1)) {
        	return;
        }

        DefaultListSelectionModel selectionModel = (DefaultListSelectionModel)jl.getSelectionModel();
        selectionModel.clearSelection();
        int min, max;
        int i=0;
        while (i<index.length) {
            min = index[i++];
            max = min;
            while (i<index.length) {
                if (index[i] == max+1) {
                    i++;
                    max++;
                } else {
                    break;
                }
            }
            selectionModel.addSelectionInterval(min, max);
        }
    }
	
    private void doMoveDown() {
        if (list == null || !helper.canOrder) {
            return;
        }
        int[] index=jl.getSelectedRows();
        if (!helper.moveData(list, index, 1)) {
        	return;
        }

        DefaultListSelectionModel selectionModel = (DefaultListSelectionModel)jl.getSelectionModel();
        selectionModel.clearSelection();
        int min, max;
        int i=0;
        while (i<index.length) {
            min = index[i++];
            max = min;
            while (i<index.length) {
                if (index[i] == max+1) {
                    i++;
                    max++;
                } else {
                    break;
                }
            }
            selectionModel.addSelectionInterval(min, max);
        }
    }
    
    private void doRemove() {
        if (list == null || !helper.canRemove()) {
            return;
        }
        int[] sel = jl.getSelectedRows();
        
        if (helper.remove(list, sel)) {
        	if (list instanceof ListReorderListener) {
        		((ListReorderListener)list).deleted(sel);
        	}
        	refresh();
        }
    }

    protected void doAdd(ActionEvent e) {
        // Note: here I would love to hide the menu if it is visible...
        // but menu.isVisible() seems to always returns false! Not worth the extra headache!

        Object[] modes = helper.getCreateTypes();
        if (modes == null || modes.length < 1) {
            doCreate(null);
            return;
        }

        if (modes.length == 1) {
            doCreate(modes[0]);
            return;
        }

        // several possibilities, build a menu for them!
        JPopupMenu menu = new JPopupMenu();
        for (Object o: modes) {
            Action action = null;
            if (o instanceof Action) {
                action = (Action)o;
            } else {
                action = new CreateModeAction(this, o);
            }
            menu.add(action);
        }

        Object src = e.getSource();
        if (src instanceof Component) {
            Component dropdown = (Component)src;
            menu.show(dropdown, 0, dropdown.getHeight());
            return;
        }
    }

    protected void doCreate(Object mode) {
        int idx = helper.create(list, mode);
        if (idx > -1) {
            refresh();
            selectItem(idx);
        }
    }

    /**
     * force a refresh of the list
     */
    public void refresh() {
        model.fireTableDataChanged();
        refreshHide();
    }
    
    private void refreshHide() {
        if (list == null) {
            return;
        }
        if (list.size() <= autohide) {
            if (sp.isVisible()) {
                sp.setVisible(false);
                b_del.setVisible(false);
                b_up.setVisible(false);
                b_down.setVisible(false);
                setSize(30, getHeight());
            }
        } else if (!sp.isVisible()) {
            sp.setVisible(true);
            b_del.setVisible(true);
            b_up.setVisible(true);
            b_down.setVisible(true);
            setSize(100, getHeight());
        }
    }

	@Override
	public void valueChanged(ListSelectionEvent e) {
        if (editionPanel != null) {
		    int[] sel = jl.getSelectedRows();
		    editionPanel.listSelectionUpdated(sel);
        }
	}

    public void addButton(JButton button) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 7;
        targetpanel.add(button, c);
    }
}


class CreateModeAction<T, L extends List<T>> extends AbstractAction {
	private final Object mode;
	private final ListPanel<T,L> listPanel;

	public CreateModeAction(ListPanel<T,L> listPanel, Object mode) {
		super(mode.toString());
        this.listPanel = listPanel;
		this.mode = mode;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		listPanel.doCreate(mode);
	}
}

class SimpleListModel<T, L extends List<T>> extends AbstractTableModel implements TableActionListener {
    private static final long serialVersionUID = 886643323547667463L;
    
    private L list;
    private final ListPanelHelper helper;

    private final ColumnDefinition[] columns;
    private final String[] actions;

    private int lastLineInc = 0;
    private Map m_button = new HashMap();


    public SimpleListModel(ListPanelHelper helper) {
        this.helper = helper;
        this.columns = helper.getColumns();
        this.actions = helper.getActionLabels();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex < actions.length) {
            return false;
        }
        return columns[columnIndex-actions.length].editable;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        columnIndex -= actions.length;
        if (columnIndex < 0) {
            return;
        }
        if (rowIndex == list.size() && lastLineInc != 0) {
            helper.addInline(list, (String)aValue);
            fireTableRowsInserted(rowIndex, rowIndex);
        } else if (columns[columnIndex].editable && helper != null) {
            helper.setValue(list, rowIndex, columnIndex, aValue);
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (list == null) {
            return null;
        }
        if (lastLineInc != 0 && row == list.size()) {
        	return "";
        }
        if (col < actions.length) {
        	Object oa = actions[col];
        	if (oa == null || "".equals(oa)) {
        		return "";
        	}
        	if (oa == "#") {
        		oa = "["+(row+1)+"]";
        	}
        	JButton b = (JButton)m_button.get(oa);
        	if (b == null) {
        		if (oa instanceof String) {
        			b = new JButton((String)oa);
        		} else if (oa instanceof Icon) {
        			b = new JButton((Icon)oa);
        		} else {
        			return "";
        		}
        		b.setBorder(BorderFactory.createEmptyBorder());
        		m_button.put(oa, b);
        	}
        	return b;
        }
        
        col -= actions.length;
        T o = list.get(row);
        
        return helper.getValue(list, o, col);
    }

	@Override
	public int getRowCount() {
        if (list == null) {
            return 0;
        }
        return list.size()+lastLineInc;
    }
	@Override
    public int getColumnCount() {
        return actions.length + columns.length;
    }
	@Override
    public Class getColumnClass(int columnIndex) {
    	if (columnIndex < actions.length) {
    		return JButton.class;
    	}

    	return columns[columnIndex-actions.length].type;
	}
    @Override
    public String getColumnName(int column) {
    	if (column < actions.length) {
    		return "";
    	}
        return columns[column-actions.length].title;
    }
    void setList(L list, ListPanel<T,L> panel) {
        this.list = list;
        if (panel.helper.canAddInline()) {
        	lastLineInc = 1;
        } else {
        	lastLineInc = 0;
        }
        fireTableStructureChanged();
    }

    @Override
	public void actionPerformed(int row, int col) {
		helper.runAction(list, row, col);
	}
}
