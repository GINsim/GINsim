package org.ginsim.core.utils.data;

import java.util.List;

/**
 * Group a list of values and a selected item from this list.
 * @param <T>  list extend
 */
public class ValueList<T> {
    private List<T> values;
    private int selected;
    private String s_none;

    /**
     * getter element of first element
     * @param values  list value
     */
    public ValueList(List<T> values) {
    	this(values, 0);
    }

    /**
     * getter of list
     * @param values list values
     * @param selected indice selected
     */
    public ValueList(List<T> values, int selected) {
        this(values, selected, "");
    }
    /**
     * getter of listValues
     * @param values list values
     * @param s_none string 'None' or not
     */
    public ValueList(List<T> values, String s_none) {
        this(values, -1, s_none);
    }
    /**
     * @param values liste values
     * @param selected selected indice
     * @param s_none  string 'None' or not
     */
    public ValueList(List<T> values, int selected, String s_none) {
        this.values = values;
        this.selected = selected;
        this.s_none = s_none;
    }
    /**
     * reset of list Values
     * @param values list values
     * @param selected selcted indice
     * @param s_none  string 'None' or not
     */
    public void reset(List<T> values, int selected, String s_none) {
        this.values = values;
        this.selected = selected;
        this.s_none = s_none;
    }

    public String toString() {
        if (selected == -1) {
            return s_none;
        }
        return values.get(selected).toString();
    }
    /**
     * getter of selected indice
     * @return the index of the selected value, or -1 if none/empty
     */
    public int getSelectedIndex() {
        return selected;
    }
    /**
     * @param index
     */
    public void setSelectedIndex(int index) {
        selected = index;
    }
    /**
     * @return the number of values
     */
    public int size() {
        return values.size();
    }
    /**
     * @param index
     * @return the corresponding value
     */
    public T get(int index) {
        if (index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    public int indexOf(Object anObject) {
        return values.indexOf(anObject);
    }
}
