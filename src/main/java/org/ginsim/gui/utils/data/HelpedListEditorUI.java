package org.ginsim.gui.utils.data;

/**
 * helper HelpedListEditorUI  class
 * @author Aurelien Naldi
 */
public class HelpedListEditorUI implements ObjectPropertyEditorUI {

    ListPanel panel = null;

    @Override
    public void refresh(boolean force) {
        if (panel != null) {
            panel.refresh();
        }
    }

    @Override
    public void apply() {

    }

    @Override
    public void setEditedProperty(GenericPropertyInfo pinfo, GenericPropertyHolder holder) {
        if (holder == null) {
            return;
        }

        if (panel == null) {
            HelpedList helped = (HelpedList)pinfo.getRawValue();
            panel = new ListPanel(helped.helper, pinfo.name);
            panel.setList(helped.list);

            holder.addField(panel, pinfo, 0);
        }
    }

    @Override
    public void release() {

    }
}
