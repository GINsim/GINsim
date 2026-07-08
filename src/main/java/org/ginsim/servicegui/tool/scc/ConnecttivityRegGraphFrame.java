package org.ginsim.servicegui.tool.scc;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.reducedgraph.NodeReducedData;
import org.ginsim.core.graph.view.style.StyleProvider;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.graph.view.style.StyleColorizerCheckbox;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.service.tool.scc.SCCGraphService;

public class ConnecttivityRegGraphFrame extends StackDialog {
    private static final long serialVersionUID = -5576209151262677441L;

    private Graph graph;

    private Container mainPanel;

    private final StyleProvider styleProvider;

    private StyleColorizerCheckbox colorizerCheckbox;

    public ConnecttivityRegGraphFrame(JFrame frame, Graph graph) {
        super(frame, "SSC_Color", 420, 260);
        SCCGraphService service = GSServiceManager.getService(SCCGraphService.class);
        // RegulatoryGraph ngraph = copyGraph(graph);
        List<NodeReducedData> components = service.getComponents(graph);
        this.styleProvider = service.getStyleProvider(components, graph);
        this.graph = graph;
        graph.getStyleManager().setStyleProvider(styleProvider);
        setMainPanel(getMainPanel());
    }

    private Container getMainPanel() {
        if (mainPanel == null) {
            this.setTitle(Txt.t("STR_connectivity"));
            mainPanel = new javax.swing.JPanel();
            mainPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.BOTH;
            c.ipadx = 10;
            mainPanel.add(new JLabel(Txt.t("STR_connectivity_descr")), c);
            c.gridy++;
            c.gridx = 0;
            c.ipadx = 0;
            c.ipady = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.gridy++;
            c.ipady = 20;
            c.weightx = 0;
            c.weighty = 0;
            mainPanel.add(new JLabel(""), c);
            c.gridy++;
            c.ipady = 0;
            c.fill = GridBagConstraints.CENTER;
            colorizerCheckbox = new StyleColorizerCheckbox("SSC Colour.", graph, styleProvider);
            mainPanel.add(colorizerCheckbox, c);
        }
        return mainPanel;
    }

    protected void run() {
        // byte[] state =
        // ((org.ginsim.servicegui.tool.stateinregulatorygraph.TabComponantProvidingAState)tabbedPane.getSelectedComponent()).getState();
        // styleProvider.setState(state);
        graph.getStyleManager().setStyleProvider(styleProvider);
        colorizerCheckbox.refresh();
    }

    public void cancel() {
        colorizerCheckbox.undoColorize();
        super.cancel();
    }

    @Override
    public void doClose() {
        colorizerCheckbox.undoColorize();
        dispose();
    }
}
