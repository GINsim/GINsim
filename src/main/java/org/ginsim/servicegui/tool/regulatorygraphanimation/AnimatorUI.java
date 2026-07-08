package org.ginsim.servicegui.tool.regulatorygraphanimation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.ginsim.common.application.Txt;


/**
 * UI for the animator plugin.
 * it helps you contructing a path on the dynamic graph and to run/export the animation.
 */
public class AnimatorUI extends JDialog {

    private static final long serialVersionUID = -603747997597648387L;
    
    private JList pathList = null;
    private RegulatoryAnimator pathModel;
    private JPanel contentPane = null;
    private JButton bPlay = null;
    private JButton bRewind = null;
    private JButton bClose = null;
    private JScrollPane scrollpane = null;

    private JButton bGP = null;

    /**
     * Constructor
     * @param frame a frame
     * @param animator a RegulatoryAnimator
     */
    public AnimatorUI(JFrame frame, RegulatoryAnimator animator) {
        super(frame);
        pathModel = animator;
        initialize();
    }
    
    private void initialize() {
        this.setSize(200, 200);
        this.setContentPane(getJContentPane());
        this.setVisible(true);
        this.addFocusListener(null);
        this.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
            }
            public void windowClosing(WindowEvent e) {
                close();
            }
            public void windowClosed(WindowEvent e) {
            }
            public void windowIconified(WindowEvent e) {
            }
            public void windowDeiconified(WindowEvent e) {
            }
            public void windowActivated(WindowEvent e) {
            }
            public void windowDeactivated(WindowEvent e) {
            }
        });
    }
    
    private JPanel getJContentPane() {
        if (contentPane == null) {
            contentPane = new JPanel();
            contentPane.setLayout(new GridBagLayout());
            
            GridBagConstraints cons_table = new GridBagConstraints();
            GridBagConstraints cons_rewind = new GridBagConstraints();
            GridBagConstraints cons_play = new GridBagConstraints();
            GridBagConstraints cons_close = new GridBagConstraints();
            GridBagConstraints cons_gp = new GridBagConstraints();
            
            cons_table.gridx = 0;
            cons_table.gridy = 0;
            cons_table.gridheight = 5;
            cons_table.weightx = 1;
            cons_table.weighty = 1;
            cons_table.fill = GridBagConstraints.BOTH;
            
            cons_rewind.gridx = 1;
            cons_rewind.gridy = 0;
            cons_rewind.fill = GridBagConstraints.HORIZONTAL;
            cons_play.gridx = 1;
            cons_play.gridy = 1;
            cons_play.fill = GridBagConstraints.HORIZONTAL;
            cons_close.gridx = 1;
            cons_close.gridy = 2;
            cons_close.fill = GridBagConstraints.HORIZONTAL;
            
            cons_gp.gridx = 1;
            cons_gp.gridy = 3;
            cons_gp.fill = GridBagConstraints.HORIZONTAL;
            
            contentPane.add(getscrollPane(), cons_table);
            contentPane.add(getBRewind(), cons_rewind);
            contentPane.add(getBPlay(), cons_play);
            contentPane.add(getBGP(), cons_gp);
            contentPane.add(getBClose(), cons_close);
        }
        return contentPane;
    }
    
    private JScrollPane getscrollPane() {
        if (scrollpane == null) {
            scrollpane = new JScrollPane();
            scrollpane.setViewportView(getPathList());
        }
        return scrollpane;
    }
    
    private JList getPathList() {
        if (pathList == null) {
            pathList = new JList(pathModel);
        }
        return pathList;
    }
    private JButton getBClose() {
        if (bClose == null) {
            bClose = new JButton("Close");
            bClose.setToolTipText("Close Animate dialog");
            bClose.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    close();
                }
            });
        }
        return bClose;
    }
    private JButton getBGP() {
        if (bGP == null) {
            bGP = new JButton("gnuplot");
            bGP.setToolTipText("Export to gnuplot selected path from STG");
            bGP.setToolTipText(Txt.t("STR_gnuplot_descr"));
            bGP.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    gnuplot();
                }
            });
        }
        return bGP;
    }
    private JButton getBPlay() {
        if (bPlay == null) {
            bPlay = new JButton("|>");
            bPlay.setToolTipText("Animate LRG with selected path from STG");
            bPlay.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    play();
                }
            });
        }
        return bPlay;
    }
    private JButton getBRewind() {
        if (bRewind == null) {
            bRewind = new JButton("<<");
            bRewind.setToolTipText("Prune future states from selected state in path");
            bRewind.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    rewind();
                }
            });
        }
        return bRewind;
    }

    /**
     * Close function
     */
    protected void close() {
        pathModel.endAnim();
        this.setVisible(false);
    }

    /**
     * play function
     */
    protected void play() {
        pathModel.playPath(pathList.getSelectedIndex());
    }

    /**
     * Rewind function
     */
    protected void rewind() {
        pathModel.revertPath(pathList.getSelectedIndex());
    }

    /**
     * gnuplot function
     */
    protected void gnuplot() {
        pathModel.saveGnuPlotPath();
    }
    
    /**
     * choose the selected item in the path list.
     * @param i indice
     */
    public void setSelected(int i) {
       pathList.setSelectedIndex(i);
    }

    /**
     * inform the user that the animator is playing.
     * (with a pause like button: "||")
     */
    public void busyPlaying() {
        bPlay.setText("||");
    }

    /**
     * inform the user that the animator is ready to play.
     * (with a play like button: "|&gt;")
     */
    public void ready2play() {
        bPlay.setText("|>");
    }
}
