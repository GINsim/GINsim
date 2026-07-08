package org.ginsim.servicegui.tool.graphcomparator;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.OptionStore;
import org.ginsim.common.application.Txt;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.shell.GsFileFilter;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.gui.utils.widgets.SeparatorComboBox;
import org.ginsim.service.tool.graphcomparator.GraphComparatorResult;
import org.ginsim.service.tool.graphcomparator.GraphComparatorService;

public class GraphComparatorFrame extends StackDialog implements ActionListener {

	private JFrame frame;
	private Graph frame_graph;

	private JComboBox g1_modelComboBox, g2_modelComboBox;
	private JTextField g1_filepath, g2_filepath;
	private JButton g1_chooseFileButton, g2_chooseFileButton;
	private JFileChooser jfc = null;
	private JCheckBox displayGraphCheckBox;
	private JPanel mainPanel;

	private List graphList = null;// List Of GsGrahp
	private boolean opt_display_graph;

	public GraphComparatorFrame(JFrame frame, Graph graph) {
		super(frame, Txt.t("STR_gcmp"), 200, 200);
		this.frame = frame;
		this.frame_graph = graph;
		initialize();
	}

	public void initialize() {
		setMainPanel(getMainPanel());
		pack();
		setLocationRelativeTo(frame);
	}

	private Container getMainPanel() {
		if (mainPanel == null) {
			this.setTitle(Txt.t("STR_gcmp"));
			mainPanel = new javax.swing.JPanel();

			mainPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			// Label
			c.gridx = 0;
			c.gridy = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = 4;
			c.ipadx = 10;
			mainPanel.add(new JLabel(Txt.t("STR_gcmp_ask")), c);

			c.gridy++;
			c.ipadx = 0;
			c.gridwidth = 1;

			// First graph selector
			c.gridx = 0;
			mainPanel.add(new JLabel("Graph 1 : "), c);

			c.gridx = 1;
			g1_modelComboBox = initComboBox();
			mainPanel.add(g1_modelComboBox, c);

			c.gridx = 2;
			g1_filepath = new JTextField(20);
			mainPanel.add(g1_filepath, c);

			c.gridx = 3;
			g1_chooseFileButton = new JButton(Txt.t("STR_select_a_file") + "...");
			mainPanel.add(g1_chooseFileButton, c);
			g1_chooseFileButton.addActionListener(this);

			// Second graph selector
			c.gridy++;
			c.gridx = 0;
			mainPanel.add(new JLabel("Graph 2 : "), c);

			c.gridx = 1;
			g2_modelComboBox = initComboBox();
			mainPanel.add(g2_modelComboBox, c);

			c.gridx = 2;
			g2_filepath = new JTextField(20);
			mainPanel.add(g2_filepath, c);

			c.gridx = 3;
			g2_chooseFileButton = new JButton(Txt.t("STR_select_a_file") + "...");
			mainPanel.add(g2_chooseFileButton, c);
			g2_chooseFileButton.addActionListener(this);

			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 3;
			mainPanel.add(new JLabel(Txt.t("STR_gcmp_masterGraph")), c);

			c.gridy++;
			displayGraphCheckBox = new JCheckBox(Txt.t("STR_gcmp_displayGraph"));
			displayGraphCheckBox.setSelected(true);
			mainPanel.add(displayGraphCheckBox, c);
		}
		return mainPanel;
	}

	private JComboBox initComboBox() {
		SeparatorComboBox comboBox = new SeparatorComboBox();
		comboBox.addItem(Txt.t("STR_gcmp_from_file") + " :");
		comboBox.addItem(new JSeparator());
		graphList = new ArrayList(GSGraphManager.getInstance().getAllGraphs().size());
		HashMap graphNames = new HashMap();
		int indexToSelect = 0;
		int i = 2;

		for (Iterator it = GSGraphManager.getInstance().getAllGraphs().iterator(); it.hasNext();) {

			Graph graph = (Graph) it.next();
			String name = graph.getGraphName();
			if (graph == this.frame_graph)
				indexToSelect = i;
			comboBox.addItem(name);
			graphList.add(graph);
			graphNames.put(name, graph);
			if (graphNames.containsKey(graph)) {
				GUIMessageUtils.openErrorDialog(
						new GsException(GsException.GRAVITY_INFO, Txt.t("STR_gcmp_graphWithSameName")) + " : " + name,
						this.frame);
			}

		}
		comboBox.setSelectedIndex(indexToSelect);
		return comboBox;

	}

	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource() == g1_chooseFileButton) {
				chooseFile(g1_filepath);
				g1_modelComboBox.setSelectedIndex(0);
			} else if (e.getSource() == g2_chooseFileButton) {
				chooseFile(g2_filepath);
				g2_modelComboBox.setSelectedIndex(0);
			}
		} catch (Exception ex) {
		}
	}

	@Override
	public void run() {

		Graph g1 = getGraph(g1_modelComboBox, g1_filepath);
		Graph g2 = getGraph(g2_modelComboBox, g2_filepath);
		opt_display_graph = displayGraphCheckBox.getSelectedObjects() != null;// display the graph if
																				// displayGraphCheckBox is checked

		GraphComparatorService gcService = GSServiceManager.getService(GraphComparatorService.class);
		GraphComparatorResult gcResult = gcService.run(frame_graph, g1, g2);
		if (gcResult == null) {
			cancel();
			return;
		}

		if (opt_display_graph) {
			GUIManager.getInstance().newFrame(gcResult.getDiffGraph());
			new GraphComparatorCaptionFrame(gcResult);
		} else {
			GUIManager.getInstance().whatToDoWithGraph(gcResult.getDiffGraph());
		}

	}

	private Graph getGraph(JComboBox comboBox, JTextField filepath) {

		Graph g = null;
		int index = comboBox.getSelectedIndex();
		if (index == 0) {
			try {
				g = GSGraphManager.getInstance().open(new File(filepath.getText()));
			} catch (GsException ge) {
				GUIMessageUtils.openErrorDialog(new GsException(GsException.GRAVITY_INFO, Txt.t("STR_GraphNotOpened")),
						this.frame);
			}
		} else if (index == 1) { // has choose blank element
			GUIMessageUtils.openErrorDialog(new GsException(GsException.GRAVITY_INFO, Txt.t("STR_gcmp_blankComboBox")),
					this.frame);
		} else {
			g = (Graph) graphList.get(index - 2);
		}
		return g;
	}

	private void chooseFile(JTextField textField) throws IOException {
		int returnVal = getJfc().showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = jfc.getSelectedFile();
			OptionStore.setOption("currentDirectory", file.getParent());
			textField.setText(file.getCanonicalPath().toString());
		}
	}

	private JFileChooser getJfc() {
		File curDir = null;
		if (jfc != null) {
			curDir = jfc.getCurrentDirectory();
		} else {
			String path = (String) OptionStore.getOption("currentDirectory");
			if (path != null) {
				curDir = new File(path);
			}
		}
		if (curDir != null && !curDir.exists()) {
			curDir = null;
		}
		jfc = new JFileChooser(curDir);
		GsFileFilter ffilter = new GsFileFilter();
		ffilter.setExtensionList(new String[] { "ginml", "zginml" }, "(z)ginml files");
		jfc.setFileFilter(ffilter);
		return jfc;
	}
}
