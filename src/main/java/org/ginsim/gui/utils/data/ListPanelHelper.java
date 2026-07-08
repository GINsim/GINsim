package org.ginsim.gui.utils.data;

import org.ginsim.common.utils.ListTools;
import org.ginsim.core.utils.data.NamedList;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;

import java.awt.Component;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provide configuration and helper methods for a list panel.
 * The helper defines capability switches (reordering, adding, removing),
 * GUI hints such as available actions, and methods to create new items.
 * 
 * @author Aurelien Naldi
 * @param <T> the type of objects in the list
 * @param <L> list to extend
 */
public class ListPanelHelper<T, L extends List<T>> {
    /**
     * static final String SEL_SINGLE="SEL_SINGLE",
     */
    public static final String SEL_EMPTY="SEL_EMPTY",
    /**
     * tatic final String SEL_SINGLE="SEL_SINGLE",
     */
    SEL_SINGLE="SEL_SINGLE",
    /**
     * static final String SEL_MULTIPLE="SEL_MULTIPLE";
     */
    SEL_MULTIPLE="SEL_MULTIPLE";
    private static final String NAME_CREATE = "doCreate";
    private static final String NAME_ADDINLINE = "addInline";
    private static final String NAME_REMOVE = "doRemove";
    private static final String NAME_COLUMN = "getColumnName";
    private static final ColumnDefinition[] DEFAULT_COLUMN = new ColumnDefinition[] {ColumnDefinition.SINGLE};

    /**
     * Mapo f Component  m_right
     */
    public Map<Class<?>, Component> m_right = null;

    /**
     * oolean canFilter
     */
    public boolean canFilter = true;
    /**
     *  boolean canOrder
     */
    public boolean canOrder = true;

    // Auto-detected capabilities
    private final boolean hasNamedColumn;
	private final boolean canAdd;
    private final boolean canAddInline;
    private final boolean canRemove;

	List<String> addOptions = null;

    /**
     * Constructor ListPanelHelper()
     */
    public ListPanelHelper() {
        // Detect capabilities based on overridden methods
        boolean canAdd = false;
        boolean canAddInline = false;
        boolean canRemove = false;
        boolean hasNamedColumn = false;
        Class cl = getClass();
        for (Method m: cl.getMethods()) {
            if (m.getDeclaringClass() != cl) {
                continue;
            }
            if (m.getName() == NAME_CREATE) {
                canAdd = true;
            } else if (m.getName() == NAME_ADDINLINE) {
                canAddInline = true;
            } else if (m.getName() == NAME_REMOVE) {
                canRemove = true;
            } else if (m.getName() == NAME_COLUMN) {
                hasNamedColumn = true;
            }
        }
        // save capabilities
        this.canAdd = canAdd;
        this.canAddInline = canAddInline;
        this.canRemove = canRemove;
        this.hasNamedColumn = hasNamedColumn;
    }

    /**
     * Create function
     * @param list the list
     * @param arg object argument
     * @return indice created
     */
    public int create(L list, Object arg) {
        return doCreate(list, arg);
    }

    /**
     * test remove function
     * @param list the list
     * @param sel the selection
     * @return boolean if remove
     */
    public boolean remove(L list, int[] sel) {
		if (sel == null || sel.length < 1 || !canRemove) {
			return false;
		}
		return doRemove(list, sel);
	}

    /**
     * ListPanel getter
     * @param list the list
     * @return ListPanel object
     */
    public ListPanel<T,L> getListPanel(L list) {
        return new ListPanel<T, L>(this, "");
    }

    /**
     * Panel getter
     * @param list a list
     * @param dialog a  StackDialog
     * @return ListEditionPanel object
     */
    public ListEditionPanel<T,L> getEditPanel(L list, StackDialog dialog) {
        return new ListEditionPanel<T, L>(this, list, "", dialog, null);
    }

    /* *************************************************************************
     *                       DECLARE CAPABILITIES
     *****************************************************************************/

    /**
     * Test can create
     * @return boolean if creat
     */
    public final boolean canCreate() {
        return canAdd;
    }
    /**
     * Test if can add
     * @return boolean if can add
     */
    public final boolean canAddInline() {
        return canAddInline;
    }
    /**
     * Test if ca remove function
     * @return boolean if can remove
     */
    public final boolean canRemove() {
        return canRemove;
    }

    /**
     * Test if  has a column name
     * @return boolean if has a column name
     */
    public final boolean hasNamedColumn() {
        return hasNamedColumn;
    }

    /* ***************************************************************************
     *     The following methods should be overridden to extend capabilities
     *****************************************************************************/

    /**
     * Column getter
     * @return DEFAULT_COLUMN
     */
    public ColumnDefinition[] getColumns() {
        return DEFAULT_COLUMN;
    }

    /**
     * Labels getter
     * @return array of string for labels
     */
    public String[] getActionLabels() {
        return new String[0];
    }

    /**
     * Return types
     * @return array of object type
     */
    public Object[] getCreateTypes() {
        return null;
    }

    /**
     * Value Getter
     * @param list the list
     * @param o the T object
     * @param column the column number
     * @return object getting
     */
    public Object getValue(L list, T o, int column) {
        return o;
    }

    /**
     * Value Setter
     * @param list the list
     * @param row the row number
     * @param column the column number
     * @param value object value
     * @return boolean if done
     */
    public boolean setValue(L list, int row, int column, Object value) {
        if (column == 0 && list instanceof NamedList) {
            return ((NamedList) list).rename(row, value.toString());
        }
        return false;
    }

    /**
     * Create function
     * @param list the list
     * @param arg object argument
     * @return -1 in any case
     */
    public int doCreate(L list, Object arg) {
        return -1;
    }

    /**
     * addInline function
     * @param list the list
     * @param s the string
     * @return return -1 in any case
     */
    public int addInline(L list, String s) {
        return -1;
    }

    /**
     * Getter
     * @param editPanel the ListEditionPanel
     * @return the ListPanelCompanion
     */
    public ListPanelCompanion getCompanion(ListEditionPanel<T,L> editPanel) {
        return null;
    }

    /**
     * Run function
     * @param list the list
     * @param row the row number
     * @param col the co4lumn number
     */
    public void runAction(L list, int row, int col) {}

    /**
     * Doremove test
     * @param list the list
     * @param sel the selection
     * @return boolean if to do
     */
    public boolean doRemove(L list, int[] sel) {
        return false;
    }

    /**
     * Remove function
     * @param list the list
     * @param sel the selection
     * @return boolean if removed
     */
    protected final boolean removeItems(L list, int[] sel) {
        if (sel == null || sel.length < 1) {
            return false;
        }

        if (sel.length == 1) {
            list.remove(sel[0]);
            return true;
        }

        List<T> toRemove = new ArrayList<T>();
        for (int idx: sel) {
            toRemove.add(list.get(idx));
        }
        list.removeAll(toRemove);

        return true;
    }

    /**
     * Move function
     * @param list the list
     * @param sel the selection
     * @param diff the int diffrenece
     * @return boolean if move data
     */
    public boolean moveData(L list, int[] sel, int diff) {
        return ListTools.moveItems(list, sel, diff);
    }

}
