package org.ginsim.servicegui.tool.regulatorygraphanimation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.Txt;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.dynamicgraph.DynamicNode;
import org.ginsim.gui.shell.FileSelectionHelper;



/**
 * Export a path in the state transition graph to gnuplot scripts.
 * This class offers static methods to do the job, and an UI to configure it
 */
public class AReg2GPConfig extends JDialog {

	private static final long serialVersionUID = -7398674287463858306L;
	private JTable blockTable;

	private List nodeOrder;
	private boolean[] t_selected;
	private JPanel contentPane;
	private JScrollPane scrollPane;
	
	private JButton but_export;
	private JButton but_cancel;
	private JComboBox combo_choice;
	private List path;
	
	/**
     * Constructor
	 * @param frame the frame
	 * @param path the path
	 * @param nodeOrder node order list
	 */
	public AReg2GPConfig(JFrame frame, List path, List nodeOrder) {
	    super(frame);
		this.nodeOrder = nodeOrder;
        this.path = path;
        t_selected = new boolean[nodeOrder.size()];
        for (int i=0 ; i<t_selected.length ; i++) {
            t_selected[i] = true;
        }
		initialize();
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				close();
			}
		});
	}

	private void initialize() {
		this.setSize(180, 250);
		this.setTitle(Txt.t("STR_pathExport"));
		this.setContentPane(getContentPanel());
		setVisible(true);
	}
	
	private JTable getBlockTable() {
		if (blockTable == null) {
			blockTable = new JTable();
			blockTable.setModel(new AReg2GPModel(nodeOrder, t_selected));
		}
		return blockTable;
	}
	
	private JPanel getContentPanel() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(new GridBagLayout());
			
			GridBagConstraints c_scroll = new GridBagConstraints();
			GridBagConstraints c_choice = new GridBagConstraints();
			GridBagConstraints c_export = new GridBagConstraints();
			GridBagConstraints c_cancel = new GridBagConstraints();
			
			c_scroll.gridx = 0;
			c_scroll.gridy = 0;
			c_scroll.gridwidth = 4;
			c_scroll.weightx = 1;
			c_scroll.weighty = 1;
			c_scroll.fill = GridBagConstraints.BOTH;
			
			c_choice.gridx = 1;
			c_choice.gridy = 1;
			c_cancel.gridx = 1;
			c_cancel.gridy = 2;
			c_export.gridx = 2;
			c_export.gridy = 2;
			
			contentPane.add(getScrollPanel(), c_scroll);
			contentPane.add(getCombo_choice(), c_choice);
			contentPane.add(getBut_export(), c_export);
			contentPane.add(getBut_cancel(), c_cancel);
		}
		return contentPane;
	}
	
	private JScrollPane getScrollPanel() {
		if (scrollPane == null) {
		    scrollPane = new JScrollPane();
		    scrollPane.setViewportView(getBlockTable());
		}
		return scrollPane;
	}
	
	/**
	 * close the config window.
	 */
	protected void close() {
	    dispose();
	}
	
	/**
	 * do the export.
     * @throws  GsException graph exception
	 */
	protected void export() throws GsException{
		if (combo_choice.getSelectedIndex() == 2) {
			doSavePathAsPython(path);
			return;
		}
	    String gnuplotsFilename;
        
        gnuplotsFilename  = FileSelectionHelper.selectSaveFilename( null, new String[]{"gnuplot"}, "Gnuplot files");
	    if (gnuplotsFilename == null) {
	        return;
	    }
        File scriptfile = new File(gnuplotsFilename);
        File datafile = new File(gnuplotsFilename.substring(0, gnuplotsFilename.length()-7)+"data");
	    switch (combo_choice.getSelectedIndex()) {
	    	case 0:
	    	    doSaveGnuPlotArrowPath(scriptfile, datafile, path, nodeOrder, t_selected);
	    	    break;
	    	case 1:
	    	    doSaveGnuPlotMultiPath(scriptfile, datafile, path, nodeOrder, t_selected);
	    	    break;
	    }
	    dispose();
	}
	
    private void doSavePathAsPython(List path) {
		String s = "";
		Iterator it = path.iterator();
		while (it.hasNext()) {
			DynamicNode state = (DynamicNode)it.next();
			s += "        (";
			for (int i=0 ; i<state.state.length ; i++) {
				s += state.state[i]+",";
			}
			s += "),\n";
		}
		JFrame f = new JFrame("path in python");
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(new JTextArea(s));
		f.add(sp);
		f.setSize(400, 300);
		f.setVisible(true);
	}


	/**
     * Save Plot
	 * @param scriptfile file script
     * @param datafile file
     * @param path the path to follow
     * @param nodeOrder the node order
     * @param selected as boolean array
     * @throws    GsException graph exception
     */
    public static void doSaveGnuPlotArrowPath(File scriptfile, File datafile, List path, List nodeOrder, boolean[] selected) throws GsException{        
        int[] exported = new int[selected.length];
        int nbExported = 0;
        for (int i=0 ; i<selected.length ; i++) {
            if (selected[i]) {
                exported[nbExported++] = i;
            }
        }
        if (nbExported == 0) {
            return;
        }
        
        try {
            FileWriter out;
            int i;
            int maxValue = 0;
            
            out = new FileWriter(datafile);
            // write data to feed the gnuplot script
            byte[] prevState = ((DynamicNode)path.get(0)).state;
            for (i=1 ; i<path.size() ; i++) {
            	byte[] state = ((DynamicNode)path.get(i)).state;
                out.write(i+"\t");
                for (int j=0 ; j<nbExported ; j++) {
                    int index = exported[j];
                    if (prevState[index] > maxValue) {
                        maxValue = prevState[index];
                    }
                    out.write(prevState[index]+"  "+(state[index]-prevState[index])+"\t");
                }
                out.write("\n");
                prevState = state;
            }
            out.close();

            
            // write the gnuplot script!
            out = new FileWriter(scriptfile);
            String s_plot = "     '"+datafile.getName()+"' using ";

            // first define arrow
            for (i=1 ; i<nbExported+1 ; i++) {
                out.write("set style arrow "+i+" filled linetype "+i+" size screen 0.025,30.000,45.000\n");
            }
            out.write("\n");
            out.write("set size 1,1\n");
            out.write("set origin 0,0 \n");
            out.write("set xrange [ 0 : "+(path.size()+1)+" ]\n");
            out.write("set yrange [ 0 : "+(maxValue+0.2)+" ]\n");
            out.write("set ytics 0,1,"+maxValue+"\n");
            out.write("set xtics 0,1,"+path.size()+"\n");
            out.write ("\nplot ");
            // then add plots
            for (i=0 ; i<nbExported-1 ; i++) {
                out.write(s_plot + "1:"+ (2*i+2)+":(0):"+(2*i+3)+" with vectors arrowstyle "+(i+1)+" title '"+nodeOrder.get(exported[i])+"',\\\n");
            }
            out.write(s_plot + "1:"+ (2*i+2)+":(0):"+(2*i+3)+" with vectors arrowstyle "+(i+1)+" title '"+nodeOrder.get(exported[i])+"'\n");
            out.close();

        } catch (IOException e) {
            throw new GsException(GsException.GRAVITY_ERROR, e.getLocalizedMessage());
        }
    }
    /**
     * sve plot from multi path
     * @param scriptfile script file
     * @param datafile  data file
     * @param path list of path
     * @param nodeOrder node order list
     * @param selected boolean list
     * @throws GsException graph exception
     */
    public static void doSaveGnuPlotMultiPath(File scriptfile, File datafile, List path, List nodeOrder, boolean[] selected) throws GsException{        
        int[] exported = new int[selected.length];
        int nbExported = 0;
        for (int i=0 ; i<selected.length ; i++) {
            if (selected[i]) {
                exported[nbExported++] = i;
            }
        }
        if (nbExported == 0) {
            return;
        }
        
        try {
            FileWriter out;
            int i;
            int maxValue = 0;

            // write data to feed the gnuplot script
            out = new FileWriter(datafile);
            for (i=0 ; i<path.size() ; i++) {
            	byte[] state = ((DynamicNode)path.get(i)).state;
                String s = "";
                for (int j=0 ; j<nbExported ; j++) {
                    int value = state[exported[j]];
                    if (value > maxValue) {
                        maxValue = value;
                    }
                    s += value+"\t";
                }
                out.write(i+"\t"+s+"\n");
                out.write(i+1+"\t"+s+"\n");
            }
            out.close();
            
            // write the gnuplot script!
            out = new FileWriter(scriptfile);
            String s_plot = "plot '"+datafile.getName()+"' using 1:";
            out.write ("set multiplot\n");
            double yscale = 1.0/nbExported;
            out.write ("set size 1, "+yscale+"\n");
            out.write("set yrange [ 0 : "+(maxValue+0.2)+" ]\n");
            out.write("set ytics 1,1,"+maxValue+"\n");
            out.write("set xtics 0,1,"+path.size()+"\n");

            out.write("set xlabel \"time\"\n");
            out.write("set bmargin 0\n");
            out.write("set tmargin 0\n");
            // the first (bottom) plot
            out.write("set origin 0, 0; "+s_plot + "2 title '"+nodeOrder.get(exported[0])+"' with lines\n");
            //change parameters for following plots
            out.write("set xtics -1,1,-1\n"); // set noxtics doesn't seems to work, work arround it..
            out.write("set xlabel \"\"\n");

            // then add other plots
            for (i=1 ; i<nbExported ; i++) {
                out.write("set origin 0, "+i*yscale+"; "+s_plot + (i+2) +" title '"+nodeOrder.get(exported[i])+"' with lines\n");
            }
            out.write ("set nomultiplot\n");
            out.close();

        } catch (IOException e) {
            throw new GsException(GsException.GRAVITY_ERROR, e.getLocalizedMessage());
        }
    }

    private JButton getBut_cancel() {
        if (but_cancel == null) {
            but_cancel = new JButton(Txt.t("STR_cancel"));
            but_cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    close();
                }
            });
        }
        return but_cancel;
    }
    private JButton getBut_export() {
        if (but_export == null) {
            but_export = new JButton(Txt.t("STR_Export"));
            but_export.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	try{
                		export();
                	}
                	catch( GsException ge){
                		GUIMessageUtils.openErrorDialog( "Unable to execute the export");
                		LogManager.error( "Unable to execute the Export");
                		LogManager.error( ge);
                	}
                }
            });
        }
        return but_export;
    }
    private JComboBox getCombo_choice() {
        if (combo_choice == null) {
            combo_choice = new JComboBox();
            combo_choice.addItem("arrow");
            combo_choice.addItem("multi");
            combo_choice.addItem("python");
        }
        return combo_choice;
    }
}
