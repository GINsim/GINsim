package org.ginsim.gui.graph.regulatorygraph.logicalfunction.graphictree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTree;

import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeElement;


/**
 * abstract class BooleanFunctionTreePanel
 */
public abstract class BooleanFunctionTreePanel extends JPanel {
  private static final long serialVersionUID = -1420693226899522868L;
  /**
   * static Font defaultFont
   */
  protected static Font defaultFont = new Font("monospaced", Font.BOLD, 12);
  /**
   * TreeElement treeElement
   */
  protected TreeElement treeElement;
  /**
   *  JTree tree
   */
  protected JTree tree = null;
  /**
   * static int charWidth = 7
   */
  protected static int charWidth = 7,
  /**
   * static int   charHeight = 12
   */
  charHeight = 12;
  /**
   * int width = 0
   */
  protected int width = 0;
  /**
   * boolean selected
   */
  protected boolean selected;
  /**
   * String tex
   */
  protected String text;
  /**
   * MouseListener mouseListener
   */
  protected MouseListener mouseListener;
  /**
   * MouseMotionListener mouseMotionListener
   */
  protected MouseMotionListener mouseMotionListener;
  /**
   * JPanel buttonPanel
   */
  protected JPanel buttonPanel;

  /**
   * Constructor
   * @param value object value
   * @param tree a  JTree
   * @param sel boolean selection
   * @param w wigth int
   */
  public BooleanFunctionTreePanel(Object value, JTree tree, boolean sel, int w) {
    super();
    setBackground(Color.white);
    treeElement = (TreeElement)value;
    treeElement.setEditable(false);
    this.tree = tree;
    selected = sel;
    width = w;
    text = treeElement.toString();
    buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
    buttonPanel.setBackground(Color.white);
    buttonPanel.setOpaque(false);
    setLayout(new BorderLayout(2, 2));
  }

  /**
   * Paint function
   * @param g  the <code>Graphics</code> context in which to paint
   */
  public void paint(Graphics g) {
    charWidth = g.getFontMetrics(defaultFont).charWidth('A');
    charHeight = g.getFontMetrics(defaultFont).getHeight();
    super.paint(g);
    if (selected) {
      g.setColor(Color.blue);
      g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }
    if (treeElement.isDropable()) {
      g.setColor(Color.red);
      g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }
  }

  /**
   * Setter of MouseListener
   * @param ml the MouseListener
   */
  public void setMouseListener(MouseListener ml) {
    mouseListener = ml;
  }

  /**
   * Setter of MouseMotionListener
   * @param mml a MouseMotionListener
   */
  public void setMouseMotionListener(MouseMotionListener mml) {
    mouseMotionListener = mml;
  }

  /**
   * MouseListener getter
   * @return the MouseListener
   */
  public MouseListener getMouseListener() {
    return mouseListener;
  }

  /**
   * Getter MouseMotionListener
   * @return the MouseMotionListener
   */
  public MouseMotionListener getMouseMotionListener() {
    return mouseMotionListener;
  }

  /**
   * Getter Insets
   * @return the Insets
   */
  public Insets getInsets() {
    return new Insets(2, 2, 2, 2);
  }

  /**
   * Update Size
   */
  public void updateSize() {

  }
}
