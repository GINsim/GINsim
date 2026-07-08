package org.ginsim.gui.graph.regulatorygraph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.ginsim.core.graph.AbstractGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameter;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameterList;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeElement;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeInteractionsModel;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.param2function.FunctionsCreator;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.gui.shell.editpanel.AbstractParameterPanel;
import org.ginsim.gui.utils.data.GenericPropertyHolder;
import org.ginsim.gui.utils.data.GenericPropertyInfo;
import org.ginsim.gui.utils.data.ObjectPropertyEditorUI;
import org.ginsim.gui.utils.widgets.EnhancedJTable;
import org.ginsim.gui.utils.widgets.SplitPane;
import org.ginsim.gui.utils.widgets.StockButton;


/**
 * Panel to edit interaction of a gene
 */
public class InteractionPanel extends AbstractParameterPanel
	implements ObjectPropertyEditorUI {

	private static final long serialVersionUID = 8583991719735516132L;

	private RegulatoryNode currentNode = null;

	protected JTable jTable = null;
	private JScrollPane jScrollPane = null;
	private JList jList = null;
	private JScrollPane jScrollPane1 = null;
    private JButton but_remove = null;
    private JButton but_addParameter = null;
    private JButton upButton = null;
    private JButton downButton = null;

	private IncomingEdgeListModel edgeList = null;
	private TableInteractionsModel interactionList = null;
	private RegulatoryGraph graph;
	private LogicalParameterCellRenderer cellRenderer;

	private JSplitPane jSplitPane = null;
	private JPanel jPanel = null;

	private JButton makeOneFunctionButton, makeNFunctionsButton;

	private GenericPropertyInfo	pinfo;

	/**
	 * This method initializes
	 * @param graph
	 *
	 */
    public InteractionPanel(RegulatoryGraph graph) {
		super(graph);
        this.graph = graph;
		initialize();
	}
	/**
	 * This method initializes this
	 */
	private void initialize() {
        setLayout(new GridBagLayout());

        GridBagConstraints c_split = new GridBagConstraints();
        c_split.gridx = 0;
        c_split.gridy = 0;
        c_split.gridwidth = 3;
        c_split.fill = GridBagConstraints.BOTH;
        c_split.weightx = 1;
        c_split.weighty = 1;
        add(getJSplitPane(), c_split);
        edgeList = new IncomingEdgeListModel();
        jList.setModel(edgeList);
	}

    /**
	 * @return the jSplitPane
	 */
	private Component getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new SplitPane();
			jSplitPane.setResizeWeight(1);
			jSplitPane.setLeftComponent(getJPanel());
			jSplitPane.setRightComponent(getJScrollPane1());
			jSplitPane.setDividerLocation( 350);
			jSplitPane.setName("logicalParametersPanel");
		}
		return jSplitPane;
	}
	/**
	 * @return the jPanel
	 */
	private Component getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel(new GridBagLayout());
			jPanel.add(getJScrollPane(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 0), 0, 0));
			
			JPanel buttonsPanel = new JPanel(new GridBagLayout());
			buttonsPanel.add(getButAddParameter(), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(2, 0, 0, 2), 0, 0));
			buttonsPanel.add(getButRemove(), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(2, 0, 0, 2), 0, 0));
			buttonsPanel.add(getUpButton(), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(2, 0, 0, 2), 0, 0));
			buttonsPanel.add(getDownButton(), new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(2, 0, 0, 2), 0, 0));
			buttonsPanel.add(getMakeOneFuctionButton(), new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(10, 0, 0, 2), 0, 0));
			buttonsPanel.add(getMakeNFuctionsButton(), new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(2, 0, 2, 2), 0, 0));
			
			jPanel.add(buttonsPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 3, 5, 0), 0, 0));
			
			//GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			//GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
      //      GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
      //      GridBagConstraints c_up = new GridBagConstraints();
      //      GridBagConstraints c_down = new GridBagConstraints();

      //      jPanel = new JPanel();
			//jPanel.setLayout(new GridBagLayout());
			//gridBagConstraints7.weightx = 1.0;
			//gridBagConstraints7.weighty = 4.0D;
			//gridBagConstraints7.fill = java.awt.GridBagConstraints.BOTH;
			//gridBagConstraints8.fill = java.awt.GridBagConstraints.NONE;
			//gridBagConstraints10.fill = java.awt.GridBagConstraints.NONE;
			//gridBagConstraints7.gridx = 0;
			//gridBagConstraints7.gridy = 0;
      //      gridBagConstraints7.gridheight = 6;
      //      gridBagConstraints7.gridwidth = 2;
			//gridBagConstraints7.weightx = 1;
			//gridBagConstraints7.weighty = 1;
			//gridBagConstraints8.gridx = 2;
			//gridBagConstraints8.gridy = 0;
			//gridBagConstraints8.weightx = 0;
			//gridBagConstraints8.weighty = 0;
			//gridBagConstraints10.gridx = 2;
			//gridBagConstraints10.gridy = 2;
			//gridBagConstraints10.weightx = 0;
			//gridBagConstraints10.weighty = 0;

      //      Insets insets = new Insets(0, 5, 3, 5);
            //gridBagConstraints7.insets = insets;
      //      gridBagConstraints8.insets = insets;
      //      gridBagConstraints10.insets = insets;

      //      c_up.gridx = 2;
      //      c_up.gridy = 3;
      //      c_down.gridx = 2;
      //      c_down.gridy = 4;

			//jPanel.add(getJScrollPane(), gridBagConstraints7);
      //      jPanel.add(getButAddParameter(), gridBagConstraints8);
      //      jPanel.add(getButRemove(), gridBagConstraints10);
      //      jPanel.add(getUpButton(), c_up);
      //      jPanel.add(getDownButton(), c_down);


      //      GridBagConstraints c_oneFunction = new GridBagConstraints();
      //      c_oneFunction.gridx = 2;
      //      c_oneFunction.gridy = 5;
      //      c_oneFunction.insets = new Insets(0, 5, 3, 5);
      //      jPanel.add(getMakeOneFuctionButton(), c_oneFunction);
            
      //      GridBagConstraints c_nFunctions = new GridBagConstraints();
     //       c_nFunctions.gridx = 2;
     //       c_nFunctions.gridy = 6;
     //       c_nFunctions.insets = new Insets(0, 5, 3, 5);
     //       jPanel.add(getMakeNFuctionsButton(), c_nFunctions);
		}
		return jPanel;
	}

    /**
     * @see org.ginsim.gui.shell.editpanel.AbstractParameterPanel#setEditedItem(java.lang.Object)
     */
    public void setEditedItem(Object obj) {
			  if (currentNode != null) {
            // apply pending changes
        }
        if (obj != null && obj instanceof RegulatoryNode) {
            currentNode = (RegulatoryNode)obj;
			edgeList.setEdge(graph.getIncomingEdges(currentNode));
            interactionList.setNode(currentNode);
             cellRenderer.setNode(currentNode);
            if (jTable.getSelectedRow() == -1) {
                int i = interactionList.getRowCount();
                jTable.getSelectionModel().setSelectionInterval(i - 1, i - 1);
            }
        }
    }

	/**
	 * This method initializes jTable
	 *
	 * @return javax.swing.JTable
	 */
	private JTable getJTable() {
		if (jTable == null) {
            List v_ok = new ArrayList();
	        interactionList = new TableInteractionsModel(graph, v_ok);
			jTable = new EnhancedJTable(interactionList);
            jTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            jTable.getColumn(jTable.getColumnName(0)).setMaxWidth(50);
            jTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            this.cellRenderer = new LogicalParameterCellRenderer(graph, interactionList);
            jTable.setDefaultRenderer(Object.class, cellRenderer);
            jTable.setDefaultRenderer(Integer.class, cellRenderer);
            jTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    selectLeft2Right();
                }
            });
		}
		return jTable;
	}
	/**
	 * This method initializes jScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());
			jScrollPane.setName("jScrollPane");
		}
		return jScrollPane;
	}
	/**
	 * This method initializes jScrollPane1
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getJList());
			jScrollPane1.setName("jScrollPane1");
			jList.setMinimumSize(new java.awt.Dimension(120,50));
			jList.setSize(new java.awt.Dimension(120,50));
		}
		return jScrollPane1;
	}
	/**
	 * This method initializes jList
	 *
	 * @return javax.swing.JList
	 */
	private JList getJList() {
		if (jList == null) {
			jList = new JList();
			jList.setModel(new IncomingEdgeListModel());
			jList.setName("jList");
		}
		return jList;
	}
	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getButRemove() {
		if (but_remove == null) {
			but_remove = new StockButton("list-remove.png", true);
			but_remove.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					deleteParameter();
				}
			});
		}
		return but_remove;
	}
	/**
	 * This method initializes jButton2
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getButAddParameter() {
		if (but_addParameter == null) {
			but_addParameter = new StockButton("go-previous.png", true);
			but_addParameter.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					insertRight2Left();
				}
			});
		}
		return but_addParameter;
	}

    /**
     * This method initializes upButton
     *
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getUpButton() {
        if(upButton == null) {
			upButton = new StockButton("go-up.png", true);
            upButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    moveUp();
                }
            });
        }
        return upButton;
    }
    /**
     * move the selected items up
     */
    protected void moveUp() {
        int[] index=jTable.getSelectedRows();
        TableInteractionsModel model = (TableInteractionsModel)jTable.getModel();
        for (int i=0;i<index.length;i++) {
            int a = index[i];
            if (a>0) {
                model.moveElementAt(a, a-1);
                index[i]=a-1;
            } else {
				return;
			}
        }
        DefaultListSelectionModel selectionModel = (DefaultListSelectionModel)jTable.getSelectionModel();
        selectionModel.clearSelection();
        int min, max;
        int i=0;
        while (i<index.length) {
            min = index[i++];
            max = min;
            while (i<index.length) {
                if (index[i] == max+1) {
                    i++;
                    max++;
                } else {
                    break;
                }
            }
            selectionModel.addSelectionInterval(min, max);
        }
        ((AbstractGraph) graph).fireMetaChange();
    }
    /**
     * This method initializes downButton
     *
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getDownButton() {
        if(downButton == null) {
			downButton = new StockButton("go-down.png", true);
            downButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    moveDown();
                }
            });
        }
        return downButton;
    }
    /**
     * move the selected items down
     */
    protected void moveDown() {
        int[] index=jTable.getSelectedRows();
        TableInteractionsModel model = (TableInteractionsModel)jTable.getModel();
        for (int i=index.length-1;i>=0;i--) {
            int a = index[i];
            if (a<model.getInteractions().getManualSize()-1) {
                model.moveElementAt(a, a+1);
                index[i]=a+1;
            } else {
				return;
			}
        }
        DefaultListSelectionModel selectionModel = (DefaultListSelectionModel)jTable.getSelectionModel();
        selectionModel.clearSelection();
        int min, max;
        int i=0;
        while (i<index.length) {
            min = index[i++];
            max = min;
            while (i<index.length) {
                if (index[i] == max+1) {
                    i++;
                    max++;
                } else {
                    break;
                }
            }
            selectionModel.addSelectionInterval(min, max);
        }
        ((AbstractGraph) graph).fireMetaChange();
    }


	/**
	 * Get the selection in the left table (jTable),
	 * and select the corresponding rows in the right list (jList)
	 */
	protected void selectLeft2Right() {
		//int sepIndex = interactionList.getInteractions().getManualSize();
        int[]t_selection = jTable.getSelectedRows();
        boolean add = t_selection != null && t_selection.length == 1 ;
        boolean edit = t_selection != null;
		for (int i = 0; edit && i < t_selection.length ; i++) {
			edit = interactionList.getInteractions().isManual(t_selection[i]);
		}
		
    	getButRemove().setEnabled(edit);
    	getUpButton().setEnabled(edit);
    	getDownButton().setEnabled(edit);
    	getMakeOneFuctionButton().setEnabled(edit || t_selection.length == 0);
    	getMakeNFuctionsButton().setEnabled(edit || t_selection.length == 0);
			if (add) {
            getButAddParameter().setEnabled(true);
            jList.setEnabled(true);
    		List edges = interactionList.getActivesEdges(t_selection[0]);
    		if (edges != null) {
    			int[] indices = new int[edges.size()];
    			for (int i=0 ; i<edges.size() ; i++) {
    				indices[i] = edgeList.getIndex(((RegulatoryEdge)edges.get(i)));
    			}
    			jList.setSelectedIndices(indices);
    		}
    	} else {
            getButAddParameter().setEnabled(false);
            jList.setEnabled(false);
    	}
	}

	/**
	 * Get the selected element in the right list (jL_IncomEdge),
	 * and insert them in the selected row in the left table (jSP_Table_Value)
	 */
	protected void insertRight2Left() {
	    if (!gui.isEditAllowed()) {
	        return;
	    }
	    if (currentNode.isInput()) {
	    	NotificationManager.getManager().publishWarning(graph, "Cannot add parameters to input nodes");
	    	return;
	    }
		if (jTable.getSelectedRowCount()<=1) {
			int selectedrow = jTable.getSelectedRow();
            if (selectedrow ==  jTable.getRowCount()-1 ||
                interactionList.getInteractions().isFunction(selectedrow)) {
                selectedrow = -1;
            }
			int[] indices=jList.getSelectedIndices();
			List edgeindex = new ArrayList();
			Object[] objs = jList.getSelectedValues();
			for (int i=0;i<indices.length;i++) {
				// Bug issue #18
				if (objs != null && i < objs.length && objs[i] != null) {
                    edgeindex.add(objs[i]);
                }
			}
			((AbstractGraph) graph).fireMetaChange();
            jTable.clearSelection();
			if (selectedrow>=0) {
                interactionList.setActivesEdges(selectedrow,edgeindex);
                jTable.getSelectionModel().addSelectionInterval(selectedrow, selectedrow);
            }
			else {
                interactionList.setActivesEdges(interactionList.getRowCount()-1, edgeindex);
                selectedrow = interactionList.getRowCount()-1;
                jTable.getSelectionModel().addSelectionInterval(selectedrow, selectedrow);
            }
		}
	}

	/**
	 * delete the selected row in the table
	 */
	protected void deleteParameter() {
	    if (!gui.isEditAllowed()) {
	        return;
	    }
		int[] selectedrows = jTable.getSelectedRows();
        if (selectedrows.length != 0) {
        	interactionList.removeInteractions(selectedrows);
        	((AbstractGraph) graph).fireMetaChange();
        	int i = selectedrows[0];
        	if (i >= jTable.getRowCount()) {
        		i = jTable.getRowCount()-1;
        	}
        	jTable.setRowSelectionInterval(i, i);
        }
	}

	private javax.swing.JButton getMakeOneFuctionButton() {
      if(makeOneFunctionButton == null) {
      	makeOneFunctionButton = new StockButton("makeOneFunction.png", true);
      	makeOneFunctionButton.setName("makeOneFunctionButton");
      	makeOneFunctionButton.setToolTipText("Make one logical formula");
      	makeOneFunctionButton.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
						doChaos(jTable.getSelectedRows().length == 0, true);
          }
        });
      }
      return makeOneFunctionButton;
    }

	private javax.swing.JButton getMakeNFuctionsButton() {
    if(makeNFunctionsButton == null) {
    	makeNFunctionsButton = new StockButton("makeNFunctions.png", true);
    	makeNFunctionsButton.setName("makeNFunctionsButton");
    	makeNFunctionsButton.setToolTipText("Make several logical formulae");
    	makeNFunctionsButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
					doChaos(jTable.getSelectedRows().length == 0, false);
        }
      });
    }
    return makeNFunctionsButton;
  }

	protected void doChaos(boolean comp, boolean one) {
      FunctionsCreator c = null;
      List v = new ArrayList();
      List interactions = ((TableInteractionsModel)jTable.getModel()).getInteractions();

      TreeInteractionsModel interactionsModel = currentNode.getInteractionsModel();
			if (!comp) {
				int[] sel = jTable.getSelectedRows();
				for (int i = 0; i < sel.length; i++) v.add(interactions.get(sel[i]));
				c = new FunctionsCreator(graph, v, currentNode);
			}
			else
				c = new FunctionsCreator(graph, interactions, currentNode);

      Map h = c.doIt(comp);

      Iterator enu = h.keySet().iterator();
      Integer key;
      String s;

      while (enu.hasNext()) {
        key = (Integer)enu.next();
        v = (List)h.get(key);
        Iterator enu2 = v.iterator();
        if (one) {
        	s = "(" + (String)enu2.next() + ")";
        	while (enu2.hasNext()) s = s + " | " + "(" + (String)enu2.next() + ")";
        	try {
        		interactionsModel.addExpression(null, key.byteValue(), currentNode, s);
        	}
        	catch (Exception ex) {
        		ex.printStackTrace();
        	}
        }
        else {
        	while (enu2.hasNext()) {
        		s = (String)enu2.next();
        		try {
        			interactionsModel.addExpression(null, key.byteValue(), currentNode, s);
        		}
        		catch (Exception ex) {
        			ex.printStackTrace();
        		}
        	}
        }
      }
      interactionsModel.setRootInfos();
      interactionsModel.fireTreeStructureChanged((TreeElement)interactionsModel.getRoot());
    }

	public void apply() {
	}
	public void refresh(boolean force) {
		setEditedItem(pinfo.getRawValue());
	}
	public void setEditedProperty(GenericPropertyInfo pinfo,
			GenericPropertyHolder panel) {
		this.pinfo = pinfo;
		this.graph = (RegulatoryGraph)pinfo.data;
		initialize();
		panel.addField(this, pinfo, 0);
	}
	@Override
	public void release() {
	}
}

class LogicalParameterCellRenderer extends DefaultTableCellRenderer {
	private static final long	serialVersionUID	= -1799999900862412151L;

	private TableInteractionsModel model;
	private RegulatoryGraph graph;
	private RegulatoryNode	vertex;

	public LogicalParameterCellRenderer(RegulatoryGraph graph, TableInteractionsModel model) {
		this.model = model;
		this.graph = graph;
	}

	public void setNode(RegulatoryNode vertex) {
		this.vertex = vertex;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel cmp = new JLabel();
		if (value != null) {
			cmp.setText(value.toString());
		}
		LogicalParameter param = model.getParameter(row);
		Color bgColor = isSelected ? table.getSelectionBackground() : table.getBackground();
		Color fgColor = isSelected ? table.getSelectionForeground() : table.getForeground();
		Font font = table.getFont();
		if (param != null) {
			LogicalParameterList lpl = model.getInteractions();
			boolean manual = lpl.isManual(row);
			boolean function = lpl.isFunction(row);
			if (!param.activable(graph, vertex))
				bgColor = Color.ORANGE;
			else if (param.isDup()) {
				fgColor = Color.magenta;
				font = font.deriveFont(Font.ITALIC | Font.BOLD);
			}
			else if (param.hasConflict()) {
				fgColor = Color.red;
				font = font.deriveFont(Font.ITALIC | Font.BOLD);
			}
			if (manual && function) {
				if (column == 0) bgColor = (isSelected ? Color.GRAY : Color.LIGHT_GRAY);
			}
			else if (function)
				bgColor = (isSelected ? Color.GRAY : Color.LIGHT_GRAY);
		}
		cmp.setOpaque(true);
		cmp.setFont(font);
		cmp.setForeground(fgColor);
		cmp.setBackground(bgColor);
		return cmp;
	}
}
