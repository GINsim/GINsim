package org.ginsim.gui.shell;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.ginsim.exception.NotificationMessage;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.notifications.NotificationPanel;
import org.ginsim.gui.notifications.NotificationSource;

import fr.univmrs.tagc.common.OptionStore;
import fr.univmrs.tagc.common.managerresources.Translator;
import fr.univmrs.tagc.common.widgets.Frame;
import fr.univmrs.tagc.common.widgets.SplitPane;

/**
 * GINsim's main frame shell.
 * 
 * It provides the general layout with menu, toolbar,
 * notifications and reserved space for the view and edit panels.
 * 
 * Main frames are counted, an event is emitted when the last one is closed.
 * 
 * FIXME: refactor in progress...
 */



public class MainFrame extends Frame implements NotificationSource {
	private static final long serialVersionUID = 3002680535567580439L;
	
    private JDialog secondaryFrame = null;
	private JPanel contentPanel = null;
	private JSplitPane mainSplitPane = null;
	private JScrollPane graphScrollPane = null;
	private JPanel graphPanel = null;
	private JSplitPane secondarySplitPanel = null;
	private EditPanel editTabbedPane = null;
	private JPanel miniMapPanel = null;

	private JMenuBar menubar = new JMenuBar();
	private JToolBar toolbar = new JToolBar();
	
    private int mmapDivLocation = ((Integer)OptionStore.getOption("display.minimapSize", new Integer(100))).intValue();

	private NotificationPanel notificationPanel = new NotificationPanel(this);

	private final FrameActionManager actionManager = new MainFrameActionManager();

	private final GraphGUI graphGUI;
	
	private static final boolean alwaysForceClose = false;

	public MainFrame(String id, int w, int h, GraphGUI graph_gui) {
		super(id, w, h);
        setJMenuBar(menubar);
        
		contentPanel = new JPanel();
		contentPanel.setLayout(new GridBagLayout());

        GridBagConstraints cst = new GridBagConstraints();
        cst.gridx = 0;
        cst.gridy = 0;
        cst.weightx = 1;
        cst.weighty = 0;
        cst.fill = GridBagConstraints.HORIZONTAL;
        cst.anchor = GridBagConstraints.WEST;
        toolbar.setFloatable(false);
		contentPanel.add(toolbar, cst);

		cst = new GridBagConstraints();
        cst.gridx = 0;
        cst.gridy = 1;
        cst.weightx = 1; 
        cst.weighty = 1;
        cst.fill = GridBagConstraints.BOTH;
		contentPanel.add(getMainSplitPane(), cst);

        setContentPane(contentPanel);
        
        this.graphGUI = graph_gui;
        buildFrameContent( graph_gui);
        setVisible(true);
	}

	/**
	 * This method initializes jSplitPane
	 *
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getMainSplitPane() {
		if (mainSplitPane == null) {
			mainSplitPane = new SplitPane();
			mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
			mainSplitPane.setTopComponent(getGraphPanel());
			mainSplitPane.setBottomComponent(getSecondarySplitPanel());
			mainSplitPane.setResizeWeight(1.0);
			mainSplitPane.setName("mainFrameSeparator");
			mainSplitPane.setOneTouchExpandable(true);
		}
		return mainSplitPane;
	}
	
	/**
	 * Create a scrollable panel with a notification area on bottom for use as main panel in the frame.
	 *
	 * @return fr.univmrs.tagc.GINsim.gui.GsGraphPanel
	 */
	private JComponent getGraphPanel() {
		if (graphPanel == null) {
			graphPanel = new JPanel();

			graphPanel.setLayout(new GridBagLayout());

			graphScrollPane = new JScrollPane();

			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			graphPanel.add(graphScrollPane, c);

			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 1;
			c.weightx = 1;
			c.weighty = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
			graphPanel.add(notificationPanel, c);
		}
		return graphPanel;
	}
	
	/**
	 * Change the panel used as minimap
	 * @param graphMapPanel
	 */
    private void setMapPanel(JPanel graphMapPanel) {
        secondarySplitPanel.remove(miniMapPanel);
        miniMapPanel = graphMapPanel;
        if (miniMapPanel == null) {
            showMiniMap(false);
        } else {
        	secondarySplitPanel.setRightComponent(miniMapPanel);
        }
	}

    // FIXME: is this needed?
	public synchronized void updateNotificationMessage() {
		notificationPanel.updateNotificationMessage();
	}

	/**
	 * This method initializes jSplitPane1
	 *
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getSecondarySplitPanel() {
		if (secondarySplitPanel == null) {
			secondarySplitPanel = new SplitPane();
			secondarySplitPanel.setLeftComponent(getEditTabbedPane());
			secondarySplitPanel.setRightComponent(getMiniMapPanel());
			secondarySplitPanel.setDividerSize(2);
			secondarySplitPanel.setResizeWeight(0.7);
			secondarySplitPanel.setName("mapSeparator");
		}
		return secondarySplitPanel;
	}

	/**
	 * This method initializes miniMapPanel
	 *
	 * @return fr.univmrs.tagc.GINsim.gui.GsGraphMapPanel
	 */
	private JPanel getMiniMapPanel() {
		if (miniMapPanel == null) {
			miniMapPanel = new JPanel();
		}
		return miniMapPanel;
	}

	/**
	 * This method initializes editTabbedPane
	 *
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getEditTabbedPane() {
	    if (editTabbedPane == null) {
			editTabbedPane = new EditPanel();
			editTabbedPane.setMinimumSize(new Dimension(0, 0));
		}
	    return editTabbedPane;
	}

    /**
     * change the attribute panel position.
     *
     * @param b : if true the panel will be in a separate window
     */
    public void divideWindow(boolean b) {
    	if ((b && secondaryFrame!=null) || (!b && secondaryFrame==null)) {
    		// already in the desired state
    		return;
    	}
		//if it's not divided
		if (b) {
			//create second frame
			secondaryFrame=new JDialog(this);
			secondaryFrame.setTitle(Translator.getString("STR_Tools"));
			//detach component from SplitPane_H
			mainSplitPane.setBottomComponent(null);
			//set tools in ContentPane
			secondaryFrame.setContentPane(secondarySplitPanel);
			secondaryFrame.setSize(800,300);
			secondaryFrame.addWindowListener(new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent evt) {
						divideWindow(false);
					}
				});
			//show
			secondaryFrame.setVisible(true);
			secondaryFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		} else  {
			// re-attach tools
			mainSplitPane.setBottomComponent(secondarySplitPanel);
			//destroy secondary frame
			secondaryFrame.setVisible(false);
			secondaryFrame.dispose();
			secondaryFrame = null;
			this.setSize(this.getSize().width+1,this.getSize().height);
		}
		// FIXME: update menu content
    }
    
    /**
     * show/hide the minimap
     *
     * @param b : if true the miniMap will be shown
     */
    public void showMiniMap(boolean b) {
        if (miniMapPanel != null) {
            if (b) {
                miniMapPanel.setVisible(b);
                secondarySplitPanel.setDividerLocation(secondarySplitPanel.getWidth() - mmapDivLocation);
            } else {
                mmapDivLocation = secondarySplitPanel.getWidth() - secondarySplitPanel.getDividerLocation();
                miniMapPanel.setVisible(b);
            }
        }
    }

    /**
     * close the window without exiting:
     *    ie close if it's not the last window
     */
    public void close() {
    	GUIManager.getInstance().close(graphGUI.getGraph());
    }
    
    /**
     * some things to do before destroying the window
     * TODO: check if this is actually needed
     */
    public void dispose() {
        if (miniMapPanel.isVisible()) {
            mmapDivLocation = secondarySplitPanel.getWidth() - secondarySplitPanel.getDividerLocation();
        }
        OptionStore.setOption("display.minimapsize", new Integer(mmapDivLocation));
        if (secondaryFrame == null) {
            OptionStore.setOption("display.dividersize", new Integer(mainSplitPane.getHeight()-mainSplitPane.getDividerLocation()));
        }
    }

    public boolean confirmCloseGraph() {
    	// FIXME: show close confirmation dialog, save if needed ... 
    	return true;
    }

    
    private void fillGraphPane( Component view) {
    	
    	graphScrollPane.setViewportView( view);
    }

    private void buildFrameContent( GraphGUI<?,?,?> gui) {
    	
    	actionManager.buildActions( gui, menubar, toolbar);
    	fillGraphPane( gui.getGraphComponent());
    	editTabbedPane.buildEditionPanels( gui);
    }
    
	@Override
	public NotificationMessage getTopNotification() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeNotification() {
		// TODO Auto-generated method stub
	}
}
