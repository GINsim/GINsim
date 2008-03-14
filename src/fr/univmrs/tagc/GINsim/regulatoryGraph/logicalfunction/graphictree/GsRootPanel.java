package fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryVertex;
import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeElement;
import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeValue;
import fr.univmrs.tagc.common.widgets.Button;

public class GsRootPanel extends GsBooleanFunctionTreePanel implements ActionListener {
  private static final long serialVersionUID = -1866485315946504210L;
  private JLabel label;
  private JButton button;

  public GsRootPanel(GsTreeElement value, JTree tree, boolean sel, int width) {
    super(value, tree, sel, width);
    label = new JLabel(value.toString());
    label.setFont(defaultFont);
    label.setPreferredSize(new Dimension(width, charHeight));
    button = new Button("add.png");
    button.addActionListener(this);
    buttonPanel.add(button);
    if (sel) {
      label.setBackground(Color.yellow);
      setBackground(Color.yellow);
    }
    if (!((Boolean)treeElement.getProperty("add")).booleanValue()) {
		button.setEnabled(false);
	}
    add(buttonPanel, BorderLayout.WEST);
    add(label, BorderLayout.CENTER);
  }
  public void actionPerformed(ActionEvent e) {
    GsRegulatoryVertex vertex;
    boolean ok = true, dis = true;
    short i;

    if (e.getSource() == button) {
      try {
        vertex = ((GsTreeInteractionsModel)tree.getModel()).getVertex();
        Enumeration enu = tree.getExpandedDescendants(tree.getPathForRow(0));
        for (i=0 ; i <= vertex.getMaxValue(); i++) {
          ok = true;
          for (int k = 0; k < treeElement.getChildCount(); k++) {
			if (((GsTreeValue)treeElement.getChild(k)).getValue() == i) {
              ok = false;
              break;
            }
		}
          if (ok) {
            for (int p = i + 1; p <= vertex.getMaxValue(); p++) {
              dis = false;
              for (int k = 0; k < treeElement.getChildCount(); k++) {
				if (((GsTreeValue)treeElement.getChild(k)).getValue() == p) {
                  dis = true;
                  break;
                }
			}
              if (!dis) {
				break;
			}
            }
            break;
          }
        }
        if (dis) {
			treeElement.setProperty("add", new Boolean(false));
		}
        if (ok) {
          ((GsTreeInteractionsModel)tree.getModel()).addValue(i);
          ((GsTreeInteractionsModel)tree.getModel()).addEmptyExpression(i, vertex);
          ((GsTreeInteractionsModel)tree.getModel()).fireTreeStructureChanged((GsTreeElement)tree.getModel().getRoot());
          if (enu != null) {
			while (enu.hasMoreElements()) {
				tree.expandPath((TreePath)enu.nextElement());
			}
		}
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}