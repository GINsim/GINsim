package org.ginsim.commongui.dialog;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;

import org.ginsim.common.application.OptionStore;
import org.ginsim.commongui.SavingGUI;
import org.ginsim.commongui.utils.GUIInfo;

/**
 * Base dialog which can remember its default size.
 * To restore its size, it must be named, and relies on the OptionStore.
 * 
 * This dialog handles close events. The close callback is also triggered by pressing "escape".
 * 
 * @author Aurelien Naldi
 */
public abstract class SimpleDialog extends JDialog {
	private static final long	serialVersionUID	= -460464845250055098L;

	protected String id;

    private SavingGUI gui = null;

    Action actionListener = new AbstractAction() {
        private static final long serialVersionUID = 448859746054492959L;
        public void actionPerformed(ActionEvent actionEvent) {
            closeEvent();
        }
    };

    Action saveAction = new AbstractAction() {
        public void actionPerformed(ActionEvent actionEvent) {
            saveEvent();
        }
    };

    /**
     * Construction  SimpleDialog
     * @param parent the parent frame
     * @param id an id to store the windows size (will be "id.width" and "id.height")
     * @param w number w width
     * @param h number int height
     */
	public SimpleDialog(Frame parent, String id, int w, int h) {
		super(parent);
		this.id = id;
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeEvent();
			}
		});
		
		setLocationByPlatform(true);
		this.setSize(OptionStore.getOption(id+".width", new Integer(w)).intValue(),
        		OptionStore.getOption(id+".height", new Integer(h)).intValue());
		
	    JPanel content = (JPanel) getContentPane();
        InputMap im = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put(KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
        content.getActionMap().put("ESCAPE", actionListener);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, GUIInfo.MASK), "save");
        content.getActionMap().put("save", saveAction);
	}

	public void closeEvent() {
		OptionStore.setOption(id+".width", new Integer(getWidth()));
		OptionStore.setOption(id+".height", new Integer(getHeight()));
		doClose();
	}

    /**
     * Doing the close
     */
	abstract public void doClose();

    public void saveEvent() {
        if (gui != null) {
            gui.save();
        } else {
            //System.out.println("no GUI to save!!");
        }
    }

    public void setAssociatedGUI(SavingGUI gui) {
        this.gui = gui;
    }
}
