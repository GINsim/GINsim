package org.ginsim.gui.utils.dialog.stackdialog;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.Txt;
import org.ginsim.commongui.dialog.DefaultDialogSize;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.commongui.dialog.SimpleDialog;
import org.ginsim.core.graph.Graph;
import org.ginsim.gui.GUIManager;



/**
 * a generic stackable dialog.
 * It offers run/cancel buttons, a message area and a stackable main part.
 * All this is accessible via a simple API to set the main dialog and add
 * secondary dialogs or messages.
 */
abstract public class StackDialog extends SimpleDialog {
    private static final long serialVersionUID = -6696566567870168910L;
    
    private static final String s_mainkey = "_main";
    
    JLabel messageLabel = new JLabel();
    CardLayout cards;
    JPanel mainPanel;
    JPanel bottomPanel;
    Component tmpPanel = null;
    protected JButton brun;
    protected JButton bcancel;
    protected JButton bclose;
    
    /**
     * Constructor
     * @param graph a graph
     * @param id an id to store the windows size (will be "id.width" and "id.height")
     * @param w width value
     * @param h height value
     */
    public StackDialog(Graph<?,?> graph, String id, int w, int h) {
    	this(GUIManager.getInstance().getFrame(graph), id, w, h);
    }
    public StackDialog(Frame parent, DefaultDialogSize defaults) {
    	this(parent, defaults.ID, defaults.width, defaults.height);
    }
    /**
     * Constructor
     * @param parent parent frame
     * @param id an id to store the windows size (will be "id.width" and "id.height")
     * @param w value widths
     * @param h value height
     */
    public StackDialog(Frame parent, String id, int w, int h) {
        super(parent, id, w, h);
        JPanel contentPane = (JPanel)getContentPane();
        mainPanel = new JPanel();
        cards = new CardLayout();
        mainPanel.setLayout(cards);
        
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 1;
        contentPane.add(mainPanel, c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        contentPane.add(getBottomPanel(), c);
    }
    
    /**
     * set the progress level, to give the user some feedback
     * @param message the message string
     */
    public void setMessage(String message) {
    	setMessage(message, 0);
    }

    public void setMessage(String message, int timeout) {
        if (messageLabel != null) {
        	messageLabel.setText(message);
        }
        if (timeout > 0) {
            // TODO: implement timeout!
        }
    }

    public void setRunText(String text, String tooltip) {
    	brun.setText(text);
    	brun.setToolTipText(tooltip);
    }

    public void setMainPanel(Component panel) {
        mainPanel.add(panel, s_mainkey);
        cards.show(mainPanel, s_mainkey);
        setVisible(true);
    }
    
    public void addSecondaryPanel(Component panel, String name) {
        mainPanel.add(panel, name);
    }

    public void addTempPanel(Component panel) {
    	tmpPanel = panel;
        mainPanel.add(tmpPanel, "_tmp");
        setVisiblePanel("_tmp");
    }
    
    public void setVisiblePanel(String name) {
    	if (!"_tmp".equals(name) && tmpPanel != null) {
    		mainPanel.remove(tmpPanel);
    		tmpPanel = null;
    	}
        if (name == null) {
            // show main panel
            cards.show(mainPanel, s_mainkey);
            bclose.setVisible(false);
            brun.setVisible(true);
            bcancel.setVisible(true);
            mainPanel.grabFocus();
            return;
        }
        cards.show(mainPanel, name);
        mainPanel.grabFocus();
        bclose.setVisible(true);
        brun.setVisible(false);
        bcancel.setVisible(false);
    }
    
    protected void escape() {
    	if (bclose.isVisible()) {
    		setVisiblePanel(null);
    	} else {
    		cancel();
    	}
    }
    
    /**
     * This method initializes buttonCancel
     * 
     * @return javax.swing.JButton
     */
    private JButton getBcancel() {
        if(bcancel == null) {
        	bcancel = new javax.swing.JButton(Txt.t("STR_close"));
        	bcancel.setToolTipText(Txt.t("STR_closedialog_descr"));
        	// TODO: get some nice default mnemonics everywhere 
        	bcancel.getModel().setMnemonic(KeyEvent.VK_N);
        	bcancel.addActionListener(new java.awt.event.ActionListener() { 
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    cancel();
                }
            });
        }
        return bcancel;
    }
    private JButton getBclose() {
        if(bclose == null) {
        	bclose = new javax.swing.JButton(Txt.t("STR_back"));
        	bclose.setToolTipText(Txt.t("STR_back_descr"));
        	bclose.getModel().setMnemonic(KeyEvent.VK_E);
        	bclose.addActionListener(new java.awt.event.ActionListener() { 
                public void actionPerformed(java.awt.event.ActionEvent e) {
                	refreshMain();
                    setVisiblePanel(null);
                }
            });
        	bclose.setVisible(false);
        }
        return bclose;
    }
    /**
     * This method initializes buttonRun
     * 
     * @return javax.swing.JButton
     */
    private JButton getBrun() {
        if(brun == null) {
        	brun = new javax.swing.JButton(Txt.t("STR_run"));
        	brun.getModel().setMnemonic(KeyEvent.VK_R);
        	brun.setActionCommand("run");
        	brun.addActionListener(new java.awt.event.ActionListener() { 
                public void actionPerformed(java.awt.event.ActionEvent e)  {
                	try{
                		run();
                	}
                	catch( GsException ge){
                		GUIMessageUtils.openErrorDialog( "Unable to execute the action");
                		LogManager.error( "Unable to execute the action");
                		LogManager.error( ge);
                	}
                }
            });
        }
        return brun;
    }
    private JPanel getBottomPanel() {
        if (bottomPanel == null) {
            bottomPanel = new JPanel();
            bottomPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 3;
            c.gridy = 1;
            c.anchor = GridBagConstraints.EAST;
            bottomPanel.add(getBrun(), c);
            c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 1;
            c.anchor = GridBagConstraints.EAST;
            bottomPanel.add(getBcancel(), c);
            bottomPanel.add(getBclose(), c);
            c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 1;
            c.weightx = 1;
            c.anchor = GridBagConstraints.WEST;
            bottomPanel.add(messageLabel, c);
        }
        return bottomPanel;
    }
    
    abstract protected void run() throws GsException;
    /**
     * called when the main panel takes the focus back
     */
    protected void refreshMain() {
    }
    
    public void doClose() {
    	dispose();
    }

    protected void cancel() {
        if (doCancel()) {
            closeEvent();
        }
    }

    protected boolean doCancel() {
        return true;
    }

    protected void setRunning(boolean b) {
    	brun.setEnabled(b==false);
    }
}
