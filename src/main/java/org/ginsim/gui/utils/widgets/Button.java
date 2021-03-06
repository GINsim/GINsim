package org.ginsim.gui.utils.widgets;

import java.awt.Insets;

import javax.swing.JButton;

import org.ginsim.commongui.utils.ImageLoader;


public class Button extends JButton {
  private static final long serialVersionUID = -6823613455585788403L;
  private Insets insets;

  public Button(String iconName) {
    super(ImageLoader.getImageIcon(iconName));
    insets = new Insets(2, 2, 2, 2);
  }
  public Button(String iconName, int left, int right, int top, int bottom) {
    super(ImageLoader.getImageIcon(iconName));
    insets = new Insets(top, left, bottom, right);
  }
  public Insets getInsets() {
    return insets;
  }
}
