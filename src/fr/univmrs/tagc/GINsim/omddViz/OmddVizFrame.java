package fr.univmrs.tagc.GINsim.omddViz;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.univmrs.tagc.GINsim.global.GsEnv;
import fr.univmrs.tagc.GINsim.graph.GsGraph;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraph;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryVertex;
import fr.univmrs.tagc.GINsim.regulatoryGraph.OmddNode;
import fr.univmrs.tagc.GINsim.treeViewer.GsTree;
import fr.univmrs.tagc.GINsim.treeViewer.GsTreeParser;
import fr.univmrs.tagc.GINsim.treeViewer.GsTreeParserFromManualOmdd;
import fr.univmrs.tagc.common.widgets.StackDialog;

public class OmddVizFrame extends StackDialog implements ActionListener {
	private static final long serialVersionUID = -7619253564236142617L;
	private JFrame frame;
	private GsGraph graph;
	private Container mainPanel;
	
	private JPanel calcPanel, resPanel;
	private JComboBox  leftOperandCB, rightOperandCB, operatorCB;
	private JTextField  resultTextField;
	private JButton displayTreeButton;
	private OmddNode leftOmdd, rightOmdd, resOmdd;
	
	private String[] operationsOptions = new String[] {"or", "and", "constraint or", "constraint and", "max"};
	
	
	public OmddVizFrame(JFrame parent, String id, int w, int h) {
		super(parent, id, w, h);
		this.frame = parent;
	}

	public OmddVizFrame(JFrame frame, GsGraph graph) {
		super(frame, "OMDD Vizualizer", 475, 260);
		this.frame = frame;
		this.graph = graph;
        initialize();
    }

	public void initialize() {
		setMainPanel(getMainPanel());
	}
	
	private Container getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new javax.swing.JPanel();
			mainPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
		
			c.gridx = 0;
			c.gridy = 0;
			mainPanel.add(getCalcPanel(), c);
			c.gridy++;
			mainPanel.add(getResPanel(), c);
			
			updateLogicalFunctionList();
		}
		return mainPanel;
	}


	private Container getCalcPanel() {
		if (calcPanel == null) {
			calcPanel = new javax.swing.JPanel();
			calcPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			calcPanel.setBorder(BorderFactory.createTitledBorder("Operation"));
		
			c.gridx = 0;
			c.gridy = 0;
			leftOperandCB = new JComboBox();
			leftOperandCB.addActionListener(this);
			calcPanel.add(leftOperandCB, c);
			c.gridx++;
			operatorCB = new JComboBox(operationsOptions);
			calcPanel.add(operatorCB, c);
			c.gridx++;
			rightOperandCB = new JComboBox();
			rightOperandCB.addActionListener(this);
			calcPanel.add(rightOperandCB, c);
		}
		return calcPanel;
	}

	private Container getResPanel() {
		if (resPanel == null) {
			resPanel = new javax.swing.JPanel();
			resPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			resPanel.setBorder(BorderFactory.createTitledBorder("Results"));
		
			c.gridx = 0;
			c.gridy = 0;
			resultTextField = new JTextField("", 25);
			resPanel.add(resultTextField, c);
			c.gridy++;
			displayTreeButton = new JButton("display tree view");
			displayTreeButton.addActionListener(this);
			resPanel.add(displayTreeButton, c);
		}
		return resPanel;
	}

	protected void run() {
		if (leftOmdd != null && rightOmdd != null) {
			resOmdd = leftOmdd.merge(rightOmdd, operatorCB.getSelectedIndex());
			resultTextField.setText(resOmdd.write().toString());			
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == leftOperandCB) {
			GsRegulatoryVertex item = (GsRegulatoryVertex) leftOperandCB.getSelectedItem();
			if (item != null) {
				System.out.println("left = "+item);
				leftOmdd = item.getTreeParameters((GsRegulatoryGraph) graph);
			}
		} else if (e.getSource() == rightOperandCB) {
			GsRegulatoryVertex item = (GsRegulatoryVertex) rightOperandCB.getSelectedItem();
			if (item != null) {
				System.out.println("right = "+item);
				rightOmdd = item.getTreeParameters((GsRegulatoryGraph) graph);
			}
		} else if (e.getSource() == displayTreeButton) {
			run();
			GsTreeParser parser = new GsTreeParserFromManualOmdd();
			GsTree tree = new GsTree(parser);
				
			parser.setParameter(GsTreeParserFromManualOmdd.PARAM_MANUALOMDD, resOmdd);
			parser.setParameter(GsTreeParserFromManualOmdd.PARAM_NODEORDER, graph.getNodeOrder());
			parser.run(GsTree.MODE_DIAGRAM_WITH_MULTIPLE_LEAFS);
			GsEnv.newMainFrame(tree);

		}
	}

	private void updateLogicalFunctionList() {
		List nodeOrder = graph.getNodeOrder();
		for (Iterator it = nodeOrder.iterator(); it.hasNext();) {
			Object node = (Object) it.next();
			leftOperandCB.addItem(node);
			rightOperandCB.addItem(node);
		}
	}
	
	public JFrame getFrame() {
		return frame;
	}

	
}