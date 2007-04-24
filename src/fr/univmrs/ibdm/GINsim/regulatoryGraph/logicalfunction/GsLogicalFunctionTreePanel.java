package fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction;

import javax.swing.*;
import java.util.Vector;
import java.awt.BorderLayout;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsRegulatoryGraph;
import java.util.Iterator;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsEdgeIndex;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsRegulatoryVertex;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.parser.TBooleanTreeNode;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.GsTreeInteractionsModel;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.GsTreeInteractionsCellRenderer;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.GsTreeInteractionsCellEditor;
import fr.univmrs.ibdm.GINsim.gui.GsParameterPanel;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.GsTreeElement;
import javax.swing.tree.TreePath;

public class GsLogicalFunctionTreePanel extends GsParameterPanel {
  private JTree tree;
  private GsTreeInteractionsModel interactionList = null;
  private GsRegulatoryGraph graph;

  public GsLogicalFunctionTreePanel(GsRegulatoryGraph graph) {
    super();
    setLayout(new BorderLayout());
    add(new JScrollPane(getJTree(graph)), BorderLayout.CENTER);
    this.graph = graph;
  }
  public void setEditedObject(Object obj) {
    GsRegulatoryVertex vertex = (GsRegulatoryVertex)obj;
    interactionList = vertex.getInteractionsModel();
    tree.setModel(interactionList);
    tree.repaint();
  }
  private JTree getJTree(GsRegulatoryGraph graph) {
    if (tree == null) {
      interactionList = new GsTreeInteractionsModel(graph);
      tree = new JTree(interactionList);
      tree.setShowsRootHandles(true);
      tree.setCellRenderer(new GsTreeInteractionsCellRenderer());
      tree.setCellEditor(new GsTreeInteractionsCellEditor(tree, tree.getCellRenderer()));
      tree.setEditable(true);
    }
    return tree;
  }
  public void addFunctionList(GsLogicalFunctionList list, short val, GsRegulatoryVertex currentVertex, GsBooleanParser parser) {
    Vector params = parser.getParams((Vector)list.getData());
    Iterator it = params.iterator(), it2;
    Vector v;
    GsEdgeIndex edgeIndex;
    GsLogicalFunctionListElement element;
    TBooleanTreeNode root = parser.getRoot();

    interactionList.setNode(currentVertex);
    interactionList.addValue(val);
    interactionList.addExpression(val, root);
    while (it.hasNext()) {
      it2 = ((Vector)it.next()).iterator();
      v = new Vector();
      while (it2.hasNext()) {
        element = (GsLogicalFunctionListElement)it2.next();
        edgeIndex = new GsEdgeIndex(element.getEdge(), element.getIndex());
        v.addElement(edgeIndex);
      }
      if (v.size() > 0) interactionList.setActivesEdges(v, val);
      interactionList.addFunction(val, root.toString(), v);
    }
    interactionList.parseFunctions();
    ((GsTreeInteractionsModel)tree.getModel()).fireTreeStructureChanged((GsTreeElement)tree.getModel().getRoot());
    currentVertex.setInteractionsModel(interactionList);
    tree.expandPath(interactionList.getPath(val, root.toString()));
    tree.scrollPathToVisible(interactionList.getPath(val, root.toString()));
  }
}
