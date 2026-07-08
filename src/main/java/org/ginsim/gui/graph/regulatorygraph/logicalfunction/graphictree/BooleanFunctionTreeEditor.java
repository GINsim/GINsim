package org.ginsim.gui.graph.regulatorygraph.logicalfunction.graphictree;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeElement;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeExpression;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeValue;

/**
 * class BooleanFunctionTreeEditor
 */
public class BooleanFunctionTreeEditor extends DefaultTreeCellEditor {
  private BooleanFunctionTreePanel p = null;
  private PanelFactory panelFactory;

	/**
	 * Constructor
	 * @param tree a JTree
	 * @param renderer a  DefaultTreeCellRenderer
	 */
	public BooleanFunctionTreeEditor(JTree tree, DefaultTreeCellRenderer renderer) {
    super(tree, renderer);
		panelFactory = ((BooleanFunctionTreeRenderer)renderer).getPanelFactory();
  }
  public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
                                              boolean expanded, boolean leaf, int row) {
    ((TreeElement)value).setSelected(true);
    ((TreeElement)value).setEdited(true);
    return p;
  }
  protected boolean canEditImmediately(EventObject event) {
    if (event instanceof MouseEvent && SwingUtilities.isLeftMouseButton((MouseEvent)event)) {
			MouseEvent me = (MouseEvent)event;
			TreePath tp = tree.getPathForLocation(me.getX(), me.getY());
			if (tp == null) {
				return false;
			}
			TreeElement treeElement = (TreeElement)tp.getLastPathComponent();
			if (treeElement == null) {
				return false;
			} else if (treeElement.isEdited()) {
				return true;
			}

			p = panelFactory.getPanel(treeElement, tree, true, ((BooleanFunctionTreeRenderer)super.renderer).getWidth(), true);
			if (treeElement.isLeaf()) { // parametres
				if (inHitRegion(me.getX(), me.getY()) && me.getClickCount() > 0) {
					return true;
				}
			}
			else if (tp.getParentPath() == tree.getPathForRow(0)) { // valeurs
				if (inHitRegion(me.getX(), me.getY()) && me.getClickCount() > 0) {
					return true;
				}
			}
			else if (tp == tree.getPathForRow(0)) { // racine
				if (inHitRegion(me.getX(), me.getY()) && me.getClickCount() > 0) {
					return true;
				}
			}
			else { // fonctions
				if (inHitRegion(me.getX(), me.getY()) && me.getClickCount() > 0) {
					return true;
				}
			}
		}
		return false;
	}
	protected boolean inHitRegion(int x, int y) {
		TreePath tp = tree.getPathForLocation(x, y);
    if (tp == null)
			return false;
		else if (lastRow != -1) {
			Rectangle bounds = tree.getPathBounds(tp);
			TreeElement treeElement = (TreeElement)tp.getLastPathComponent();
			boolean leaf = treeElement.isLeaf();

			// Ajout pour compatibilite avec Java 1.4
			offset = 20;

			if (bounds != null) {
				if (!leaf) {
					if (treeElement.getParent() == null) {
						if (x > bounds.x + offset - 16 && x < bounds.x + offset - 3) return true;
						return false;
					}
					else if (treeElement instanceof TreeValue) {
						if (x > bounds.x + offset - 16 && x < bounds.x + offset + 31)	return true;
						return false;
					}
					else if (treeElement instanceof TreeExpression) {
						if (y < bounds.y + 16) {
							if (x > bounds.x + offset - 16 && x < bounds.x + offset - 3)
								return true;
							else if (p instanceof ParamPanel)
								return true;
						}
						return false;
					}
				}
				else if (leaf && treeElement.getParent() instanceof TreeExpression && (x <= bounds.x + offset - 16 || x > bounds.x + offset + 3))
					return false;
			}
		}
		return true;
	}
}
