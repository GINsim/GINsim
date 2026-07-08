package org.ginsim.servicegui.tool.reg2dyn;

import java.awt.Frame;

import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.gui.utils.dialog.stackdialog.LogicalModelActionDialog;


/**
 * abstract class BaseSimulationFrame
 */
public abstract class BaseSimulationFrame extends LogicalModelActionDialog {
    private static final long serialVersionUID = 8275117764047606650L;

    /**
     * Constructor
     * @param lrg the lrg graph
     * @param parent the parent frame
     * @param id the id
     * @param w the width
     * @param h the hheight
     */
    public BaseSimulationFrame(RegulatoryGraph lrg, Frame parent, String id, int w, int h) {
        super(lrg, parent, id, w, h);
    }

    /**
     * Progess Setting
     * @param n number int
     */
    public void setProgress(int n) {
    	setMessage(""+n);
    }

    /**
     * Progress Setting
     * @param s string of text
     */
    public void setProgress(String s) {
    	setMessage(s);
  }
}