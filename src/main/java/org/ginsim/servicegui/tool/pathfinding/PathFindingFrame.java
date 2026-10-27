package org.ginsim.servicegui.tool.pathfinding;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.ginsim.common.application.Txt;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.commongui.utils.ImageLoader;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.GraphChangeType;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalTransitionGraph;
import org.ginsim.core.graph.reducedgraph.ReducedGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;


public class PathFindingFrame extends StackDialog implements ActionListener, ResultHandler {
	private static final long serialVersionUID = -7430762236435581864L;
	private Graph graph;
	private Container resultsPanel, progressionPanel;
	private JProgressBar progressBar;
	private JLabel progressionLabel;
	private JList pathList;
	private JButton colorizeButton;
	private JButton copyButton;
	private JScrollPane pathListScrollPane;
	private boolean isColorized = false;
	private List path;
	private PathStyleProvider selector;
	private JButton b_selectFromGraph;
	private JButton b_add;
	private JButton b_del;

    private JTable constraintTable;
    private PathFindingTableModel constraintModel;
	private final javax.swing.Timer searchTimer = new javax.swing.Timer(250, e -> startAutomaticSearch());
	private int searchGeneration = 0;
	
	
	public PathFindingFrame( Graph graph) {
		super(GUIManager.getInstance().getFrame(graph), "pathFinding", 420, 260);
		setTitle("Find path in " + getGraphTypeName(graph));
		this.setMinimumSize(new Dimension(300, 300));
		this.graph = graph;
		if (graph instanceof DynamicGraph) {
			constraintModel = new DynamicAnalyserPathModel((DynamicGraph)graph);
		} else {
			constraintModel = new ConstraintSelectionModel(graph);
		}

        JPanel mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(getGetSelectFromGraphButton(), c);
        
        c.gridx++;
        mainPanel.add(getAddButton(), c);
        
        c.gridx++;
        mainPanel.add(getDelButton(), c);
        b_del.setEnabled(false);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 5;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(new JScrollPane(getNodeTable()), c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 5;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

        c.gridx = 0;
        c.gridy++;
        mainPanel.add(getProgressionPanel(), c);

        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(getResultPanel(), c);

        setMainPanel(mainPanel);
		brun.setVisible(false);
		constraintModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				constraintsChanged();
			}
		});
		searchTimer.setRepeats(false);
		constraintsChanged();
	}

	private static String getGraphTypeName(Graph graph) {
		if (graph instanceof DynamicGraph) {
			return "STG";
		}
		if (graph instanceof RegulatoryGraph) {
			return "LRG";
		}
		if (graph instanceof HierarchicalTransitionGraph) {
			return "HTG";
		}
		if (graph instanceof ReducedGraph) {
			return "SCC";
		}
		return "graph";
	}

	private Component getProgressionPanel() {
		if (progressionPanel == null) {
			progressionPanel = new JPanel();
			progressionPanel.setVisible(false);
			GridBagConstraints c = new GridBagConstraints();
			
			c.gridx = 0;
			c.gridy = 0;
			progressionPanel.add(getProgressionLabel(),c);
			c.gridx++;
			progressionPanel.add(getProgressBar(),c);
		}
		return progressionPanel;
	}

	private Component getResultPanel() {
		if (resultsPanel == null) {
			resultsPanel = new JPanel(new GridBagLayout());
			resultsPanel.setVisible(false);
			
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.gridheight = 3;
			c.weighty = 1;
			c.weightx = 1;
			c.fill = GridBagConstraints.BOTH;
			resultsPanel.add(getPathScrollPane(),c);

			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 0;
			resultsPanel.add(getColorizeButton(),c);
			
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			resultsPanel.add(getCopyButton(),c);

		}
		return resultsPanel;
	}

	private Component getGetSelectFromGraphButton() {
		if (b_selectFromGraph == null) {
            b_selectFromGraph = new JButton(ImageLoader.getImageIcon("pick.png"));
            b_selectFromGraph.addActionListener(this);
			b_selectFromGraph.setToolTipText("Use selected graph nodes as path endpoints");
		}
		return b_selectFromGraph;
	}
	
	private Component getAddButton() {
		if (b_add == null) {
			b_add = new JButton(ImageLoader.getImageIcon("list-add.png"));
			b_add.addActionListener(this);
			b_add.setToolTipText("Add an intermediate");
		}
		return b_add;
	}

	private Component getDelButton() {
		if (b_del == null) {
			b_del = new JButton(ImageLoader.getImageIcon("list-remove.png"));
			b_del.addActionListener(this);
			b_del.setToolTipText("Remove an intermediate");
		}
		return b_del;
	}

    private Component getNodeTable() {
        if (constraintTable == null) {
            constraintTable = new JTable(constraintModel);
        }
        return constraintTable;
    }

	private Component getCopyButton() {
		if (copyButton == null) {
			copyButton = new JButton(Txt.t("STR_pathFinding_copy"));
			copyButton.addActionListener(this);
		}
		return copyButton;
	}

	private Component getColorizeButton() {
		if (colorizeButton == null) {
			colorizeButton = new JButton(Txt.t("STR_do_colorize"));
			colorizeButton.addActionListener(this);
		}
		return colorizeButton;
	}

	private Component getPathScrollPane() {
		if (pathList == null) {
			pathList = new JList();
			pathList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			pathList.setLayoutOrientation(JList.VERTICAL);
			pathListScrollPane = new JScrollPane(pathList);
			pathListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		}
		return pathListScrollPane;
	}

	private JLabel getProgressionLabel() {
		if (progressionLabel == null) {
			progressionLabel = new JLabel("");
		}
		return progressionLabel;
	}

	private Component getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar();
		}
		return progressBar;
	}

	protected void run() {
		searchTimer.stop();
		startAutomaticSearch();
	}

	private void constraintsChanged() {
		searchGeneration++;
		if (isColorized) {
			undoColorize();
		}
		path = null;
		pathList.setListData(new Object[0]);
		resultsPanel.setVisible(true);
		getCopyButton().setEnabled(false);
		getColorizeButton().setEnabled(false);
		List nodes = constraintModel.getNodes();
		if (nodes == null) {
			searchTimer.stop();
			progressBar.setIndeterminate(false);
			progressionPanel.setVisible(true);
			setProgressionText("Enter/select valid graph nodes/states to find a path.");
			return;
		}
		progressionPanel.setVisible(true);
		progressBar.setIndeterminate(false);
		setProgressionText("Path will update automatically...");
		searchTimer.restart();
	}

	private void startAutomaticSearch() {
		final int generation = searchGeneration;
		List nodes = constraintModel.getNodes();
		if (nodes == null) {
			return;
		}
		if (isColorized) {
			undoColorize();
		}
		progressionPanel.setVisible(true);
		progressBar.setIndeterminate(true);
		setProgressionText("searching...");
		new PathFinding(result -> SwingUtilities.invokeLater(() -> {
			if (generation == searchGeneration) {
				setPath(result);
			}
		}), graph, new ArrayList(nodes)).start();
	}

	public void setProgressionText(String text) {
		getProgressionLabel().setText(text);
	}
	
	public void setPath(List<Object> path) {
		this.path = path;
		if (path == null) {
			pathList.setListData(new Object[0]);
			getCopyButton().setEnabled(false);
			getColorizeButton().setEnabled(false);
			progressBar.setIndeterminate(false);
			progressionPanel.setVisible(true);
			setProgressionText("No path found for the current constraints.");
			resultsPanel.setVisible(true);
			return;
		}

		progressBar.setIndeterminate(false);
		progressionPanel.setVisible(false);
		setProgressionText("Path found...");
		pathList.setListData(path.toArray(new Object[path.size()]));
		getCopyButton().setEnabled(true);
		getColorizeButton().setEnabled(true);
		resultsPanel.setVisible(true);
		progressionPanel.setVisible(false);
		doColorize();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == colorizeButton) {
			if (isColorized  ) {
				undoColorize();
			} else {
				doColorize();
			}
		} else if (e.getSource() == copyButton) {
			Clipboard clipboard = getToolkit().getSystemClipboard();
			StringSelection data = new StringSelection(getPathAsText());
			clipboard.setContents(data, data);
		} else if (e.getSource() == b_selectFromGraph) {
			getSelectionFromGraph();
		} else if (e.getSource() == b_add) {
			int[] sel = constraintTable.getSelectedRows();
			if (sel != null && sel.length > 0) {
				constraintModel.add(sel[0]);
			} else {
				constraintModel.add( constraintModel.getRowCount()-1);
			}
			b_del.setEnabled( constraintModel.getRowCount() > 2);
			constraintsChanged();
		} else if (e.getSource() == b_del) {
			del();
		}
	}
	
	private void del() {
		int[] sel = constraintTable.getSelectedRows();
		
    	int n = 0;
    	for (int i: sel) {
    		if (constraintModel.del(i-n)) {
    			n++;
    		}
    	}

		b_del.setEnabled( constraintModel.getRowCount() > 2);
		constraintsChanged();
	}
	
	
	
	private void getSelectionFromGraph() {
		GraphGUI<?, ?, ?> gui = GUIManager.getInstance().getGraphGUI(graph);
		Collection<?> selected = gui.getSelection().getSelectedNodes();
		if (selected == null || selected.size() < 1) {
			return;
		}
		List<?> nodes = new ArrayList<>(selected);
		if (nodes.size() >= 2) {
			constraintModel.setNode(nodes.get(0), new int[] {0});
			constraintModel.setNode(nodes.get(nodes.size() - 1), new int[] {constraintModel.getRowCount() - 1});
			return;
		}
		int[] rows = constraintTable.getSelectedRows();
		if (rows.length == 0) {
			rows = new int[] {0};
		}
		constraintModel.setNode(nodes.get(0), rows);
	}

	/**
	 * Transform the path list in a string like [elm0, elm1, elm2, ...]
	 * @return
	 */
	private String getPathAsText() {
		if (path.size() == 0) {
			return "[]";
		}
		StringBuffer s = new StringBuffer("[");
		for (Iterator iterator = path.iterator(); iterator.hasNext();) {
			s.append(iterator.next()+", ");
		}
		return s.substring(0, s.length()-2)+"]";
	}

	/**
	 * Search the node whose id correspond to the text in textField.
	 * If only one node is found, return it.
	 * Else display an error message.
	 * @param textField
	 * @return
	 */
	private Object getNode(JTextField textField) {
		
		String lookup = textField.getText();
		List foundNodes = graph.searchNodes( lookup);
		if (foundNodes == null) {
			GUIMessageUtils.openErrorDialog(Txt.t("STR_pathFinding_no_node")+textField.getText(), this);
			return null;
		} else if (foundNodes.size() == 1) {
			return foundNodes.get(0);
		} else if (foundNodes.size() > 1) {
			// roughly search for a perfect match
			for (Object cur: foundNodes) {
				if (cur.toString().equals( lookup)) {
					return cur;
				}
			}
			GUIMessageUtils.openErrorDialog(Txt.t("STR_pathFinding_too_much_nodes")+textField.getText(), this);
			return null;
		} else {
			GUIMessageUtils.openErrorDialog(Txt.t("STR_pathFinding_no_node")+textField.getText(), this);
			return null;
		}
	}

	private void doColorize() {
		if (path != null) {
			if (selector == null) {
				selector = new PathStyleProvider(graph.getStyleManager());
			}
			
			selector.setPath(path);
			graph.getStyleManager().setStyleProvider(selector);
		
			graph.fireGraphChange(GraphChangeType.GRAPHVIEWCHANGED, null);
			colorizeButton.setText(Txt.t("STR_undo_colorize"));
			isColorized = true;
		}
	}
	
	private void undoColorize() {
		graph.getStyleManager().setStyleProvider(null);
		colorizeButton.setText(Txt.t("STR_do_colorize"));
		isColorized = false;
	}

	public Graph getGraph() {
		
		return graph;
	}
	
	public void cancel() {
		super.cancel();
	}

	@Override
	public void doClose() {
		searchTimer.stop();
		searchGeneration++;
		if (isColorized) {
			undoColorize();
		}
		super.doClose();
	}
}

class ConstraintSelectionModel<N> extends AbstractTableModel implements PathFindingTableModel<N> {

    private final Graph graph;
    private final List<String> constraints = new ArrayList<String>(2);
	private final List<N> resolvedNodes = new ArrayList<>(2);

    @Override
	public String getColumnName(int column) {
		return "Node";
	}

	public ConstraintSelectionModel(Graph graph) {
        this.graph = graph;
        constraints.add("");
        constraints.add("");
		resolvedNodes.add(null);
		resolvedNodes.add(null);
    }

    @Override
    public boolean del(int index) {
		if (getRowCount() < 2) {
			return false;
		}
		constraints.remove(index);
		resolvedNodes.remove(index);
		fireTableDataChanged();
		return true;
	}

	@Override
    public int getRowCount() {
        return constraints.size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= getRowCount()) {
            return null;
        }

        return constraints.get(row);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {
        if (row >= getRowCount()) {
            return;
        }
        
        String lookup = aValue.toString();
        constraints.set(row, lookup);
		resolvedNodes.set(row, findInGraph(lookup));
		fireTableCellUpdated(row, col);

    }
    
    @Override
    public void add(int index) {
    	String s = constraints.get(index);
    	constraints.add(index, s);
		resolvedNodes.add(index, resolvedNodes.get(index));
    	fireTableDataChanged();
    }

    private N findInGraph(String lookup) {
		List<N> exactMatches = new ArrayList<>();
		for (Object node: graph.getNodes()) {
			if (lookup.equals(node.toString())) {
				exactMatches.add((N) node);
			}
		}
		if (exactMatches.size() == 1) {
			return exactMatches.get(0);
		}
		if (exactMatches.size() > 1) {
			return null;
		}

		try {
			List<N> foundNodes = graph.searchNodes(lookup);
			if (foundNodes != null && !foundNodes.isEmpty()) {
				return foundNodes.get(0);
			}
		} catch (IllegalArgumentException e) {
			return null;
        }
		return null;
    }

    @Override
    public List<N> getNodes() {

        List<N> nodes = new ArrayList<N>();
		for (N n:resolvedNodes) {
            if (n == null) {
                return null;
            }
            nodes.add(n);
        }

        if (nodes.size() < 2) {
            return null;
        }

        return nodes;
    }

    @Override
    public void setNode(N node, int[] selectedRows) {
        String txt = node.toString();
        for (int i: selectedRows) {
			if (i >= 0 && i < constraints.size()) {
				constraints.set(i, txt);
				resolvedNodes.set(i, node);
				fireTableRowsUpdated(i, i);
			}
        }
    }
}
