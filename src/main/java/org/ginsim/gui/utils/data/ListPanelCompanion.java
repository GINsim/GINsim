package org.ginsim.gui.utils.data;

import java.util.List;

/**
 * Complete a ListPanel by providing additional panels reacting to the selection
 * @param <L>  list extend
 * @param <T>  companion
 */
public interface ListPanelCompanion<T, L extends List<T>> {

    /**
     * Setter of parent list
     * @param list the list to set
     */
    void setParentList(L list);

    /**
     * Update selection function
     * @param selection selection int array
     */
    void selectionUpdated(int[] selection);
}
