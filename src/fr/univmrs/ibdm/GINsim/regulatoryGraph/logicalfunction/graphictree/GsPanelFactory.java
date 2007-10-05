package fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree;

import javax.swing.JTree;

import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeElement;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeExpression;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeManual;
import java.awt.Point;

public class GsPanelFactory {
  public GsPanelFactory() {
    super();
  }
  public static GsBooleanFunctionTreePanel getPanel(GsTreeElement value, JTree tree, boolean sel, int width, boolean edit) {
    GsBooleanFunctionTreePanel panel = null;
    switch (value.getDepth()) {
      case 0 :
        panel = new GsRootPanel(value, tree, sel, width);
        break;
      case 1 :
        panel = new GsValuePanel(value, tree, sel, width, edit);
        break;
      case 2 :
        if (value instanceof GsTreeExpression) {
          panel = new GsFunctionPanel(value, tree, sel, width, edit);
          Point p =((GsTreeExpression)value).getSelection();
          if (p != null) {
            ((GsFunctionPanel)panel).selectText(p, ((GsTreeExpression)value).isNormal());
          }
        }
        else if (value instanceof GsTreeManual)
          panel = new GsManualPanel(value, tree, sel, width);
        break;
      case 3 :
        if (value.getParent() instanceof GsTreeExpression)
          panel = new GsParamPanel(value, tree, sel, width);
        else if (value.getParent() instanceof GsTreeManual)
          panel = new GsManualParamPanel(value, tree, sel, width);
        break;
    }
    return panel;
  }
}
