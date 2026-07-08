package org.ginsim.gui.graph.regulatorygraph.logicalfunction.graphictree;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeInteractionsModel;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeElement;

/**
 * class BooleanFunctionTreeRenderer
 */
public class BooleanFunctionTreeRenderer extends DefaultTreeCellRenderer implements ComponentListener {
private static final long serialVersionUID = 3456841880209526024L;
private int width;
  private JTree tree = null;
  private static final int leftmargin = 90;
  private PanelFactory panelFactory;

  /**
   * Constructor BooleanFunctionTreeRenderer
   * @param totalWidth the total wigth
   * @param pf the PanelFactory
   */
  public BooleanFunctionTreeRenderer(int totalWidth, PanelFactory pf) {
    super();
    width = totalWidth - leftmargin;
		panelFactory = pf;
  }

  /**
   * width getter
   * @return teh width int
   */
  public int getWidth() {
    return width;
  }

  /**
   * Getter for Customize Toolbar...
   * @param tree      the receiver is being configured for
   * @param value     the value to render
   * @param sel  whether node is selected
   * @param expanded  whether node is expanded
   * @param leaf      whether node is a lead node
   * @param row       row index
   * @param hasFocus  whether node has focus
   * @return the Component
   */
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                boolean leaf, int row, boolean hasFocus) {
    this.tree = tree;
    ((TreeElement)value).setSelected(sel);
    ((TreeElement)value).setEdited(false);
    BooleanFunctionTreePanel p = panelFactory.getPanel((TreeElement)value, tree, sel, width, false);
    p.updateSize();
    return p;
  }

  /**
   * componentHidden
   * @param e the event to be processed
   */
  public void componentHidden(ComponentEvent e) {}

  /**
   * componentMoved
   * @param e the event to be processed
   */
  public void componentMoved(ComponentEvent e) {}

  /**
   *  componentResized
   * @param e the event to be processed
   */
  public void componentResized(ComponentEvent e) {
    width = e.getComponent().getWidth() - leftmargin;
    if (tree != null) {
      tree.stopEditing();
      int[] sr = tree.getSelectionRows();
      Enumeration enu = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
      ((TreeInteractionsModel)tree.getModel()).fireTreeStructureChanged((TreeElement)tree.getModel().getRoot());
      if (enu != null) {
        while (enu.hasMoreElements()) {
          tree.expandPath((TreePath)enu.nextElement());
        }
      }
      tree.setSelectionRows(sr);
    }
  }

  /**
   *  componentShown
   * @param e the event to be processed
   */
  public void componentShown(ComponentEvent e) {}

  /**
   * PanelFactory getter
   * @return the PanelFactory
   */
  public PanelFactory getPanelFactory() {
		return panelFactory;
	}
}
