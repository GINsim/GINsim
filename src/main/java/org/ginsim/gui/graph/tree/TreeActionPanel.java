package org.ginsim.gui.graph.tree;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.tree.Tree;
import org.ginsim.core.graph.tree.TreeBuilder;
import org.ginsim.core.graph.tree.TreeBuilderFromCircuit;
import org.ginsim.core.graph.tree.TreeBuilderFromRegulatoryGraph;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.core.graph.regulatorygraph.MDDContext;
import org.ginsim.gui.graph.GUIEditor;
import org.ginsim.service.tool.circuit.FunctionalityContext;


/**
 * This action panel allow the user to select the
 *     *  omdd representation method
 *     *  the gene associated with the logical function to display (in the case of regulatoryGraph)
 *
 */
public class TreeActionPanel extends JPanel implements GUIEditor<Tree> {
	private static final long serialVersionUID = 3342245591953494375L;

	private JComboBox sourceList;
	private JComboBox treeModeList;
	private Tree tree = null;
    private GraphGUI gui = null;
	private boolean hasSelectionChanged = false;
	
	private static List<String> TREEMODES;

	private JLabel labelChooseComboBox;
	static {
		TREEMODES = new ArrayList<String>(3);
		TREEMODES.add(Txt.t("STR_treeviewer_diagram_with_all_leafs"));
		TREEMODES.add(Txt.t("STR_treeviewer_diagram"));
		TREEMODES.add(Txt.t("STR_treeviewer_tree"));
	}
	
	
	public TreeActionPanel() {
		initialize();
	}
	
	@Override
	public void setEditedItem(Tree tree) {
		if (tree == null) {
			return;
		}
		this.tree = tree;
        this.gui = GUIManager.getInstance().getGraphGUI(tree);
		updateComboBox();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setLayout(new GridBagLayout());
        getContentPanel();
        this.setMinimumSize(new Dimension(20,20));
	}

	private void getContentPanel() {
		GridBagConstraints c = new GridBagConstraints();
		
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = 20;
		c.gridx = 0;
		c.gridy = 0;
		add(new JLabel(Txt.t("STR_treeviewer_tree_choose_mode")), c);
		c.gridx++;
		treeModeList = new JComboBox(TREEMODES.toArray());
		treeModeList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectionChange();
			}
		});
		add(treeModeList, c);
		c.gridx = 0;
		c.gridy++;
		labelChooseComboBox = new JLabel("");
		add(labelChooseComboBox, c);
		c.gridx++;
		sourceList = new JComboBox();
		sourceList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectionChange();
			}
		});
		add(sourceList, c);
	}
	
	private void updateComboBox() {
		TreeBuilder parser = tree.getParser();
		sourceList.removeAllItems();
		if (parser instanceof TreeBuilderFromRegulatoryGraph) {
			labelChooseComboBox.setText(Txt.t("STR_treeviewer_tree_choose_gene"));
			List<RegulatoryNode> nodeOrder = (List<RegulatoryNode>) parser.getParameter(TreeBuilder.PARAM_NODEORDER);
			for (RegulatoryNode node : nodeOrder) {
				sourceList.addItem(node);
			}
			if (sourceList.getItemCount() > 0) {
				sourceList.setSelectedIndex(((Integer)parser.getParameter(TreeBuilderFromRegulatoryGraph.PARAM_INITIALVERTEXINDEX)).intValue());				
			}
		} else if (parser instanceof TreeBuilderFromCircuit) {
			labelChooseComboBox.setText(Txt.t("STR_treeviewer_tree_choose_circuit"));
			List contexts = (List) parser.getParameter(TreeBuilderFromCircuit.PARAM_ALLCONTEXTS);
			for (Object context : contexts) {
				sourceList.addItem(context);
			}
			if (sourceList.getItemCount() > 0) {
                FunctionalityContext fc = (FunctionalityContext)tree.getParser().getParameter(TreeBuilderFromCircuit.PARAM_OPENCIRCUITDESC);
                int selidx = 0;
                if (fc != null) {
                    selidx = contexts.indexOf(fc);
                    if (selidx < 0) {
                        selidx = 0;
                    }
                }
                sourceList.setSelectedIndex( selidx );
			}
		}
	}

	protected void selectionChange() {
		if (!hasSelectionChanged) {
			hasSelectionChanged = true;
			return;
		}
		if (tree == null) return;
		int treeMode = treeModeList.getSelectedIndex();
		if (treeMode < 0 || treeMode > 2) treeMode = 0;
		
		if (tree.getParser() instanceof TreeBuilderFromRegulatoryGraph) {
			Integer geneIndex = new Integer(sourceList.getSelectedIndex());
			tree.getParser().setParameter(TreeBuilderFromRegulatoryGraph.PARAM_INITIALVERTEXINDEX, geneIndex);
		} else if (tree.getParser() instanceof TreeBuilderFromCircuit) {
			int contextIndex = sourceList.getSelectedIndex();
			MDDContext fcontext = ((List<MDDContext>)tree.getParser().getParameter(TreeBuilderFromCircuit.PARAM_ALLCONTEXTS)).get(contextIndex);
			tree.getParser().setParameter(TreeBuilderFromCircuit.PARAM_INITIALCIRCUITDESC, fcontext.getContext());
		}
		tree.getParser().run(treeMode);
        if (gui != null) {
            gui.repaint();
        }
	}


	@Override
	public Component getComponent() {
		return this;
	}
}
