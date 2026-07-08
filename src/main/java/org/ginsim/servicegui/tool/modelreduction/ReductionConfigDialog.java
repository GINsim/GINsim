package org.ginsim.servicegui.tool.modelreduction;

import java.util.AbstractList;
import java.util.List;

import javax.swing.JOptionPane;

import org.colomoto.common.task.Task;
import org.colomoto.common.task.TaskListener;
import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.service.tool.modelreduction.ReductionConfig;
import org.ginsim.service.tool.modelreduction.ReductionTask;
import org.ginsim.service.tool.modelreduction.ReconstructionTask;
import org.ginsim.service.tool.modelreduction.ReductionLauncher;

public class ReductionConfigDialog extends StackDialog implements ReductionLauncher, TaskListener {
    private static final long serialVersionUID = 3618855894072951620L;

    private final RegulatoryGraph graph;
    private final ReductionConfigurationPanel lp;

    ReductionTask simplifier = null;
    ReconstructionTask reconstructionTask = null;
    ReductionConfig config = null;

    private boolean isRunning = false;

    ReductionConfigDialog(RegulatoryGraph graph) {
        super(graph, "modelSimplifier", 600, 500);
        this.graph = graph;
        setTitle("select nodes to remove");

        lp = new ReductionConfigurationPanel(graph);

        setMainPanel(lp);
        setVisible(true);
    }

    protected void run() {
        config = lp.getSelectedItem();
        if (!isRunning && config != null) {
            simplifier = new ReductionTask(graph, config, this);
            simplifier.background(this);
            isRunning = true;
            brun.setEnabled(false);
        }
    }

    @Override
    public void taskUpdated(Task task) {
        if (task == null) {
            return;
        }

        if (task == simplifier) {
            LogicalModel model = simplifier.getResult();
            simplifier = null;
            reconstructionTask = new ReconstructionTask(model, graph, config);
            reconstructionTask.background(this);
            return;
        }

        if (task == reconstructionTask) {
            isRunning = false;
            RegulatoryGraph newGraph = reconstructionTask.getResult();
            if (newGraph != null) {
                GUIManager.getInstance().whatToDoWithGraph(newGraph, this.graph);
            }
            cancel();
        }
    }

    @Override
    public boolean showPartialReduction(List<NodeInfo> l_todo) {

        int choice = JOptionPane.showConfirmDialog(this,
                "Due to autoregulations,\nthe following nodes could not be removed:\n" + l_todo
                        + "\nShow result of partial reduction?",
                "Reduction failed",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.NO_OPTION) {
            isRunning = false;
            cancel();
            return false;
        }
        return true;
    }
}

class ReductionConfigContentList extends AbstractList<NodeInfo> {

    private final List<RegulatoryNode> nodeOrder;
    private ReductionConfig config;

    ReductionConfigContentList(List<RegulatoryNode> nodeOrder) {
        this.nodeOrder = nodeOrder;
    }

    public void setConfig(ReductionConfig config) {
        this.config = config;
    }

    @Override
    public NodeInfo get(int i) {
        return nodeOrder.get(i).getNodeInfo();
    }

    @Override
    public int size() {
        return nodeOrder.size();
    }

    public boolean isSelected(NodeInfo node) {
        if (config == null) {
            return false;
        }
        return config.isSelected(node);
    }

    public boolean setSelected(NodeInfo node, boolean selected) {
        if (config == null) {
            return false;
        }

        if (config.isSelected(node) == selected) {
            return false;
        }

        config.setSelected(node, selected);
        return true;
    }
}
