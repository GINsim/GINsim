package org.ginsim.servicegui.tool.circuit;

import java.util.*;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.colomoto.common.task.*;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.PathSearcher;

import org.ginsim.common.application.Txt;
import org.ginsim.common.callable.ProgressListener;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.perturbation.Perturbation;
import org.ginsim.core.graph.regulatorygraph.perturbation.PerturbationHolder;
import org.ginsim.core.graph.regulatorygraph.perturbation.PerturbationUser;
import org.ginsim.core.graph.tree.Tree;
import org.ginsim.core.graph.tree.TreeBuilder;
import org.ginsim.core.graph.tree.TreeBuilderFromCircuit;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.regulatorygraph.perturbation.PerturbationSelectionPanel;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.gui.utils.widgets.Label;
import org.ginsim.gui.utils.widgets.treetable.*;
import org.ginsim.service.tool.circuit.*;


/**
 * configuration/status frame for circuit search/analyse
 */
public class CircuitFrame extends StackDialog implements ProgressListener<List>, TaskListener {

    private static final long serialVersionUID = 2671795894716799300L;

    private static final CircuitService CIRCUITS = GSServiceManager.getService(CircuitService.class);

    protected RegulatoryGraph graph;

    private CircuitGUIStatus status = CircuitGUIStatus.NONE;

    private List v_circuit = new ArrayList();
    protected JTreeTable tree = null;
    private GsCircuitTreeModel treemodel = null;
    private JPanel configDialog = null;
    private JPanel resultPanel = null;
    private CardLayout cards;
    private JSplitPane splitPane = null;
    private javax.swing.JPanel jContentPane = null;
    private javax.swing.JLabel labelProgression = null;
    private JScrollPane sp = null;
    private JScrollPane sp2 = null;
    private JTextArea jta = null;
    private CircuitSearchStoreConfig config = null;
    private JCheckBox cb_cleanup = null;
    private PerturbationHolder mutantstore;
    private PerturbationSelectionPanel mutantPanel;

	private JButton viewContextButton;

    private CircuitSearcher cSearcher = null;

    private MDDManager ddmanager = null;
    private PathSearcher mddPaths = null;

    /**
     * This is the default constructor
     * 
     * @param frame
     * @param graph
     */
    public CircuitFrame(JFrame frame, Graph<?,?> graph) {
        super(frame, "display.circuit", 500, 300);
        if (graph == null || !(graph instanceof RegulatoryGraph)) {
			GUIMessageUtils.openErrorDialog("no graph", frame);
		}
        this.graph = (RegulatoryGraph) graph;
        mutantstore = new PerturbationUser(this.graph, "circuits_frame");
        initialize();
        updateStatus(CircuitGUIStatus.NONE);
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setMainPanel(getJContentPane());
        this.setTitle(Txt.t("STR_circuit"));
        this.setVisible(true);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancel();
            }
        });
        getJTextArea();
    }
    /**
     * close the circuit search/analyse dialog. stop running algo and close
     * configuration dialog if appropriate.
     */
    protected void cancel() {
    	if (cSearcher != null) {
    		cSearcher.cancel();
    	}
        graph = null;
        super.cancel();
        dispose();
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new javax.swing.JPanel();
            cards = new CardLayout();
            jContentPane.setLayout(cards);
            jContentPane.add(getConfigPanel(), "config");
            jContentPane.add(getResultPanel(), "result");
            cards.show(jContentPane, "config");
        }
        return jContentPane;
    }

    private JPanel getResultPanel() {
        if (resultPanel == null) {
        	resultPanel = new javax.swing.JPanel();
        	resultPanel.setLayout(new GridBagLayout());

        	GridBagConstraints c = new GridBagConstraints();

            // Perturbation selection
        	c.gridx = 0;
        	c.gridy = 0;
        	c.weightx = 1;
        	c.fill = GridBagConstraints.BOTH;
        	mutantPanel = new PerturbationSelectionPanel(this, graph, mutantstore);
        	resultPanel.add(mutantPanel, c);

            // Checkbox for the strict mode
            c.gridx = 0;
            c.gridy = 1;
        	cb_cleanup = new JCheckBox(Txt.t("STR_do_cleanup"));
            cb_cleanup.setSelected(true);
            resultPanel.add(cb_cleanup, c);

            c.gridy++;
        	resultPanel.add(getLabelProgression(), c);
        	
            // Result table
        	c.gridy++;
        	c.weighty = 1;
        	resultPanel.add(getSplitPane(), c);
        	
            // actions button
        	c.gridy++;
        	c.weighty = 0;
        	c.anchor = GridBagConstraints.EAST;
        	c.fill = GridBagConstraints.NONE;
        	c.gridwidth = 1;
            resultPanel.add(getViewContextButton(), c);
        }
        return resultPanel;
    }
    
	private JButton getViewContextButton() {
    	if (viewContextButton == null) {
    		viewContextButton = new JButton(Txt.t("STR_circuit_viewContext"));
    		viewContextButton.setEnabled(false);
    		viewContextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					viewContext();
				}
			});
    	}
		return viewContextButton;
	}

    /**
     * Verify if the specified String is an integer
     * 
     * @param s -
     *            string to be tested
     * @return true if s is an integer, false if it isn't
     */
    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    protected JPanel getConfigPanel() {
        if (configDialog == null) {
            if (config == null) {
                config = new CircuitSearchStoreConfig(graph.getNodeOrder());
            }
            configDialog = new CircuitConfigureSearch(this, config, graph.getNodeOrder());
        }
        return configDialog;
    }

    private JScrollPane getSp() {
        if (sp == null) {
            sp = new JScrollPane();
        }
        return sp;
    }

    private JScrollPane getSp2() {
        if (sp2 == null) {
            sp2 = new JScrollPane();
        }
        return sp2;
    }

    private JSplitPane getSplitPane() {
        if (splitPane == null) {
            splitPane = new JSplitPane();
            splitPane.setTopComponent(getSp());
            splitPane.setBottomComponent(null);
            splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            splitPane.setDividerLocation(20);
            splitPane.setDividerSize(4);
        }
        return splitPane;
    }

    protected void run() {
        switch (status) {
        case NONE:
            cSearcher = CIRCUITS.getCircuitSearcher(graph, config);
            cSearcher.background(this);
            updateStatus(CircuitGUIStatus.SEARCH);
            break;
        case SEARCH:
        	cSearcher.cancel();
            cancel();
            break;
        case CIRCUITS:
        case RESULT:
            if (configDialog != null) {
                configDialog.setVisible(false);
            }
            runAnalyse();
            break;
        }
    }

    @Override
    public void setProgress(int n) {
        setProgress(""+n);
    }

    @Override
    public void setProgress(String text) {
        getLabelProgression().setText(text);
    }

    protected void updateStatus(CircuitGUIStatus status) {
        this.status = status;
        switch (status) {
        case NONE:
            this.status = status;
            setRunText(Txt.t("STR_circuit_search"), Txt.t("STR_circuit_search_descr"));
            break;
        case SEARCH:
            setRunText(Txt.t("STR_cancel"), null);
            break;
        case CIRCUITS:
            this.status = status;
            cards.show(jContentPane, "result");
            setProgress("Number of circuits satisfying the requirements: "+ v_circuit.size());
            setRunText(Txt.t("STR_circuit_analyse"), Txt.t("STR_circuit_analyse_tooltip"));
            break;
        }
    }

    public void setResult(List result) {
        if (result != null) {
            v_circuit = result;
            if (config != null) {
                config.setReady();
            }
            showCircuit();
        }
    }

    private void showCircuit() {
        updateStatus(CircuitGUIStatus.CIRCUITS);
        if (treemodel == null) {
        	treemodel = new GsCircuitTreeModel(v_circuit);
        }
        if (tree == null) {
            tree = new JTreeTable(treemodel);
        	cards.show(jContentPane, "result");
            
            getSp().setViewportView(tree);
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            tree.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
				public void valueChanged(ListSelectionEvent e) {
					showInfo();
				}
			});
        }
    }

    protected void showInfo() {
        viewContextButton.setEnabled(false);
        CircuitDescrInTree cdtree = getSelectedContextFromTreeTable();
        if (cdtree == null || mddPaths == null) {
            jta.setText("no data");
            return;
        }
        
        int index = 0;
        if (cdtree.summary) {
            if (!treemodel.isLeaf(cdtree)) {
                int count = treemodel.getChildCount(cdtree);
                jta.setText("contains "+count+" layered-circuits");
                return;
            }
            switch (cdtree.key) {
                case CircuitDescr.ALL:
                    index = 0;
                    break;
                case CircuitDescr.FUNCTIONAL:
                    index = ((CircuitDescrInTree)cdtree.getCircuit().v_functional.get(0)).key;
                    break;
                case CircuitDescr.POSITIVE:
                    index = ((CircuitDescrInTree)cdtree.getCircuit().v_positive.get(0)).key;
                    break;
                case CircuitDescr.NEGATIVE:
                    index = ((CircuitDescrInTree)cdtree.getCircuit().v_negative.get(0)).key;
                    break;
                case CircuitDescr.DUAL:
                    index = ((CircuitDescrInTree)cdtree.getCircuit().v_dual.get(0)).key;
                    break;
            }
        } else if (cdtree.key >= cdtree.getCircuit().t_context.length) {
            index = 0;
        } else {
            index = cdtree.key;
        }
        
        CircuitDescr circuit = cdtree.getCircuit();
        if (circuit.t_context == null) {
            jta.setText("no data");
            return;
        }

        if (circuit.t_context[index] == 0) {
            jta.setText(CircuitDescr.SIGN_NAME[CircuitDescr.FALSE]);
            return;
        }


        // FIXME: show relevant circuit information!!
        int node = circuit.t_context[index];
        mddPaths.setNode(node);
        int[] path = mddPaths.getPath();
        int[] max = mddPaths.getMax();
        MDDVariable[] variables = ddmanager.getAllVariables();
        StringBuffer sb = new StringBuffer();
        for (int l: mddPaths) {
            if (l == 0) {
                continue;
            }
            if (l == 1) {
                sb.append("+ : ");
            } else if (l == 2) {
                sb.append("- : ");
            } else {
                sb.append("? : ");
            }
            boolean first = true;
            for (int i=0 ; i<path.length ; i++) {
                int cst = path[i];
                if (cst > -1) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(" & ");
                    }
                    int m = max[i];
                    if (m == -1) {
                        sb.append(variables[i].key+">="+cst);
                    } else if (m > cst) {
                        sb.append(variables[i].key+"=["+cst+","+m+"]");
                    } else {
                        sb.append(variables[i].key+"="+cst);
                    }
                }
            }
            sb.append("\n");
        }

        jta.setText(sb.toString());
        viewContextButton.setEnabled(true);
    }

    protected void runAnalyse() {
    	brun.setEnabled(false);
        ddmanager = treemodel.analyse(graph, config, mutantstore.getPerturbation(), cb_cleanup.isSelected());
        mddPaths = new PathSearcher(ddmanager, 1,2, true);
        brun.setEnabled(true);

        if (sp2 == null) {
	        getSp2().setViewportView(getJTextArea());
	        sp2.setSize(sp2.getWidth(), 80);
	        getSplitPane().setBottomComponent(sp2);
	        int h = splitPane.getHeight();
	        splitPane.setDividerLocation(h - 100);
        }
        tree = null;
        treemodel.reload(this);
        showCircuit();
    }

    private JTextArea getJTextArea() {
        if (jta == null) {
            jta = new JTextArea();
            jta.setEditable(false);
        }
        return jta;
    }

    /**
     * This method initializes labelProgression
     * 
     * @return javax.swing.JLabel
     */
    public JLabel getLabelProgression() {
        if (labelProgression == null) {
            labelProgression = new Label("", Label.MESSAGE_NORMAL);
        }
        return labelProgression;
    }
    
    /**
     * Launch the treeViewer to analyse the contexts of functionalities
     * if a context is selected in the treeTable, it will use it, else it will use a "random" context (the first in v_circuit)
     */
	private void viewContext() {
        CircuitDescrInTree dtree = getSelectedContextFromTreeTable();
        if (dtree == null) {
            return;
        }
		
        TreeBuilderFromCircuit parser = new TreeBuilderFromCircuit();
        parser.setParameter(TreeBuilder.PARAM_NODEORDER, graph.getNodeOrder());
        parser.setParameter(TreeBuilderFromCircuit.PARAM_MANAGER, ddmanager);

        // construct the list of circuits (to allow selecting it)
        CircuitDescr cd_sel = dtree.getCircuit();
        List contexts = new ArrayList(v_circuit.size());
        for (Iterator it = v_circuit.iterator(); it.hasNext();) {
            CircuitDescrInTree cdit = (CircuitDescrInTree) it.next();
            CircuitDescr cd = cdit.getCircuit();
            int[] context = cd.getContext();
            for (int i = 0; i < context.length; i++) {
                int o = context[i];
                if (o != 0) {
                    FunctionalityContext fc = new FunctionalityContext(cd, i);
                    contexts.add(fc);
                    if (cd == cd_sel) {
                        parser.setParameter(TreeBuilderFromCircuit.PARAM_OPENCIRCUITDESC, fc);
                        cd_sel = null;
                    }
                }
            }
        }
		parser.setParameter(TreeBuilderFromCircuit.PARAM_ALLCONTEXTS, contexts);

        Tree tree = GSGraphManager.getInstance().getNewGraph( Tree.class, parser);
		GUIManager.getInstance().newFrame(tree,false);
	}
	
	/**
	 * Return the selected context from the treeTable
	 * if none is selected, then return the first circuit in v_circuit
	 */
	private CircuitDescrInTree getSelectedContextFromTreeTable() {
		CircuitDescrInTree circuitDescrInTree  = null;
		for (Iterator it = v_circuit.iterator(); it.hasNext();) {
			circuitDescrInTree = (CircuitDescrInTree) it.next();
			if (circuitDescrInTree.getCircuit().sign != CircuitDescr.FALSE) break;
		}
		Object selected = ((TreeTableModelAdapter)tree.getModel()).nodeForRow(tree.getSelectedRow());
		if (!(selected instanceof CircuitDescrInTree)) {
        	if (selected == null || GsCircuitTreeModel.s_root.equals(selected)) {
    			jta.setText("");
    			return circuitDescrInTree;
			}
			jta.setText("no data");
            int count = treemodel.getChildCount(selected);
            if (count > 0) {
                jta.setText("contains "+count+" circuits");
            }
            return circuitDescrInTree;
        }
		return (CircuitDescrInTree)selected;
	}

	@Override
	public void taskUpdated(Task task) {
        if (task.getStatus() == TaskStatus.CANCELED) {
            cancel();
        }

		if (task == cSearcher) {
            setResult(cSearcher.getResult());
		}
	}

	@Override
	public void milestone(Object data) {
	}
}



class GsCircuitTreeModel extends AbstractTreeTableModel {

    List v_listeners = new ArrayList();
    List v_circuit;
    List v_root = new ArrayList();
    Map<Object, List> m_parent = new HashMap();

    protected static final String s_root = "Circuits";
    static protected Class[]  cTypes = {TreeTableModel.class, String.class, String.class};

    /**
     * @param v_circuit
     */
    public GsCircuitTreeModel(List v_circuit) {
    	super(s_root);
        v_root.add(CircuitDescr.SIGN_NAME[CircuitDescr.ALL]);
        this.v_circuit = v_circuit;
        m_parent.put(CircuitDescr.SIGN_NAME[CircuitDescr.ALL], v_circuit);
        m_parent.put(s_root, v_root);
    }

    protected MDDManager analyse(RegulatoryGraph graph, CircuitSearchStoreConfig config, Perturbation mutant, boolean do_cleanup) {
        CircuitAlgo circuitAlgo = new CircuitAlgo(graph, mutant, do_cleanup);
        List v_functional = new ArrayList();
        List v_positive = new ArrayList();
        List v_negative = new ArrayList();
        List v_dual = new ArrayList();

        // first some cleanups, to allow running it several times in a row, with different mutants
        v_root.clear();
        v_root.add("All");
        m_parent.clear();
        m_parent.put("All", v_circuit);
        m_parent.put(s_root, v_root);
        for (int i = 0; i < v_circuit.size(); i++) {
        	CircuitDescr cdescr = ((CircuitDescrInTree) v_circuit.get(i)).getCircuit();
        	cdescr.clear();
        }
        
        for (int i = 0; i < v_circuit.size(); i++) {
            CircuitDescr cdescr = ((CircuitDescrInTree) v_circuit.get(i)).getCircuit();
            cdescr.check(circuitAlgo, graph.getNodeOrder());
            CircuitDescrInTree cdtree;
            if (cdescr.v_all.size() > 1) {
                m_parent.put(v_circuit.get(i), cdescr.v_all);
            }
            if (cdescr.v_functional != null) {
                cdtree = new CircuitDescrInTree(cdescr, true, CircuitDescr.FUNCTIONAL);
                placeCircuit(v_functional, cdtree);
                if (cdescr.v_functional.size() > 1) {
                    m_parent.put(cdtree, cdescr.v_functional);
                }
                if (cdescr.v_positive != null) {
                    cdtree = new CircuitDescrInTree(cdescr, true, CircuitDescr.POSITIVE);
                    placeCircuit(v_positive, cdtree);
                    if (cdescr.v_positive.size() > 1) {
                        m_parent.put(cdtree, cdescr.v_positive);
                    }
                } 
                if (cdescr.v_negative != null) {
                    cdtree = new CircuitDescrInTree(cdescr, true, CircuitDescr.NEGATIVE);
                    placeCircuit(v_negative, cdtree);
                    if (cdescr.v_negative.size() > 1) {
                        m_parent.put(cdtree, cdescr.v_negative);
                    }
                }
                if (cdescr.v_dual != null) {
                    cdtree = new CircuitDescrInTree(cdescr, true, CircuitDescr.DUAL);
                    placeCircuit(v_dual, cdtree);
                    if (cdescr.v_dual.size() > 1) {
                        m_parent.put(cdtree, cdescr.v_dual);
                    }
                }
            }
        }
        if (v_functional.size() > 0) {
            v_root.add(CircuitDescr.SIGN_NAME[CircuitDescr.FUNCTIONAL]);
            m_parent.put(CircuitDescr.SIGN_NAME[CircuitDescr.FUNCTIONAL], v_functional);
            if (v_positive.size() > 0) {
                v_root.add(CircuitDescr.SIGN_NAME[CircuitDescr.POSITIVE]);
                m_parent.put(CircuitDescr.SIGN_NAME[CircuitDescr.POSITIVE], v_positive);
            }
            if (v_negative.size() > 0) {
                v_root.add(CircuitDescr.SIGN_NAME[CircuitDescr.NEGATIVE]);
                m_parent.put(CircuitDescr.SIGN_NAME[CircuitDescr.NEGATIVE], v_negative);
            }
            if (v_dual.size() > 0) {
                v_root.add(CircuitDescr.SIGN_NAME[CircuitDescr.DUAL]);
                m_parent.put(CircuitDescr.SIGN_NAME[CircuitDescr.DUAL], v_dual);
            }
        }
        // TODO: add a sorting by context!
        reload(this);

        return circuitAlgo.getManager();
    }

    private void placeCircuit(List v, CircuitDescrInTree cdescr) {
        for (int i = 0; i < v.size(); i++) {
            if (cdescr.getScore() < ((CircuitDescrInTree) v.get(i)).getScore()) {
                v.add(i, cdescr);
                return;
            }
        }
        v.add(cdescr);
    }

    public Object getChild(Object parent, int index) {
        List v = m_parent.get(parent);
        if (v != null && v.size() > index) {
            return v.get(index);
        }
        return null;
    }

    public int getChildCount(Object parent) {
        List v = m_parent.get(parent);
        if (v != null) {
            return v.size();
        }
        return 0;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        reload(this);
    }

    public int getIndexOfChild(Object parent, Object child) {
        List v = m_parent.get(parent);
        if (v != null) {
            return v.indexOf(child);
        }
        return -1;
    }

    public void addTreeModelListener(TreeModelListener l) {
        if (!v_listeners.contains(l)) {
            v_listeners.add(l);
        }
    }

    public void removeTreeModelListener(TreeModelListener l) {
        v_listeners.remove(l);
    }

    protected void reload(Object source) {
        for (int i = 0; i < v_listeners.size(); i++) {
            ((TreeModelListener) v_listeners.get(i))
                    .treeStructureChanged(new TreeModelEvent(source,
                            new Object[] { getRoot() }));
        }
    }

	public int getColumnCount() {
		return 2;
	}

	public String getColumnName(int column) {
		switch (column) {
			case 0:
				return "Circuit";
			case 1:
				return "Sign/children";
		}
		return "";
	}

	public Object getValueAt(Object node, int column) {
		switch (column) {
			case 0:
				return node.toString();
			case 1:
				if (!(node instanceof CircuitDescrInTree)) {
					return "";
				}
		        CircuitDescrInTree cdtree = (CircuitDescrInTree)node;
                int count = getChildCount(cdtree);
	            if (count != 0) {
	                return count+" sub-circuits";
	            }
	            
	            int index = 0;
	            if (cdtree.summary) {
	                switch (cdtree.key) {
	                    case CircuitDescr.ALL:
	                        index = 0;
	                        break;
	                    case CircuitDescr.FUNCTIONAL:
	                        index = ((CircuitDescrInTree)cdtree.getCircuit().v_functional.get(0)).key;
	                        break;
	                    case CircuitDescr.POSITIVE:
	                        index = ((CircuitDescrInTree)cdtree.getCircuit().v_positive.get(0)).key;
	                        break;
	                    case CircuitDescr.NEGATIVE:
	                        index = ((CircuitDescrInTree)cdtree.getCircuit().v_negative.get(0)).key;
	                        break;
	                    case CircuitDescr.DUAL:
	                        index = ((CircuitDescrInTree)cdtree.getCircuit().v_dual.get(0)).key;
	                        break;
	                }
	            } else if (cdtree.key >= cdtree.getCircuit().t_context.length) {
	                index = 0;
	            } else {
	                index = cdtree.key;
	            }
	            if (cdtree.getCircuit().t_mark != null 
	            	&& cdtree.getCircuit().t_mark.length > index 
	            	&& cdtree.getCircuit().t_mark[index] != null) {
	            	return CircuitDescr.SIGN_NAME[(int)cdtree.getCircuit().t_mark[index][1]];
	            }
				return "??";
		}
		return "";
	}

	public Class getColumnClass(int column) {
    	return cTypes[column];
    }	
}

