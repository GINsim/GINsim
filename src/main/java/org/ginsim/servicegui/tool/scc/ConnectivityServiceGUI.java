package org.ginsim.servicegui.tool.scc;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;

import javax.swing.*;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.common.task.Task;
import org.colomoto.common.task.TaskListener;
import org.colomoto.common.task.TaskStatus;
import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.reducedgraph.NodeReducedData;
import org.ginsim.core.graph.reducedgraph.ReducedGraph;
import org.ginsim.core.graph.view.style.StyleProvider;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.graph.GraphSelection;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.gui.shell.actions.GenericGraphAction;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.service.tool.scc.SCCGraphService;
import org.kohsuke.MetaInfServices;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.LogicalModel2RegulatoryGraph;
import org.ginsim.service.tool.modelbooleanizer.ModelBooleanizerService;

/**
 * register the scc service
 */
@MetaInfServices(ServiceGUI.class)
@GUIFor(SCCGraphService.class)
@ServiceStatus(EStatus.RELEASED)
public class ConnectivityServiceGUI extends AbstractServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		List<Action> actions = new ArrayList<Action>();
		if (graph instanceof ReducedGraph) {
			actions.add(new ExtractFromSCCGraphAction((ReducedGraph) graph, this));
		} else {
			actions.add(new ConnectivityColorizeGraphAction(graph, this));
			actions.add(new SCCGraphAction(graph, this));
		}
		return actions;
	}

	@Override
	public int getInitialWeight() {
		return W_GRAPH_COLORIZE + 50;
	}
}

class SCCGraphAction extends GenericGraphAction implements TaskListener {
	private static final long serialVersionUID = 8294301473668672512L;
	private final Graph graph;
	private Task<ReducedGraph> task = null;

	protected SCCGraphAction(Graph graph, ServiceGUI serviceGUI) {
		super(graph, "STR_constructReducedGraph", null, "STR_constructReducedGraph_descr", null, serviceGUI);
		// super(graph, "STR_connectivity", null, "STR_connectivity_descr", null,
		// serviceGUI);

		this.graph = graph;
	}

	@Override
	public synchronized void actionPerformed(ActionEvent arg0) {
		if (task != null && task.getStatus() == TaskStatus.RUNNING) {
			return;
		}
		setEnabled(false);
		SCCGraphService service = GSServiceManager.getService(SCCGraphService.class);
		task = service.backgroundSCCGraph(graph, this);
	}

	@Override
	public void taskUpdated(Task task) {
		if (task != this.task) {
			return;
		}
		ReducedGraph result = this.task.getResult();

		GUIManager.getInstance().whatToDoWithGraph(result);
		this.task = null;
		setEnabled(true);
	}
}

class ConnectivityColorizeGraphAction extends GenericGraphAction {
	private static final long serialVersionUID = 8294301473668672512L;
	private StyleProvider styler;
	private StyleProvider oldstyle;

	protected ConnectivityColorizeGraphAction(Graph graph, ServiceGUI serviceGUI) {
		super(graph, "STR_connectivity", null, "STR_connectivity_descr", null, serviceGUI);

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		new ConnecttivityRegGraphFrame(GUIManager.getInstance().getFrame(graph), graph);
		if (GUIManager.getInstance().getFrame(graph) == null) {
			GUIManager.getInstance().whatToDoWithGraph(graph);
		}
	}

	private void doclose() {
		// graph.getStyleManager().setStyleProvider(oldstyle);
		;
	}

	private RegulatoryGraph copyGraph(Graph graph) {
		RegulatoryGraph graph1 = (RegulatoryGraph) graph;
		ArrayList<NodeInfo> to_remove = new ArrayList<NodeInfo>();
		RegulatoryGraph simplifiedGraph = LogicalModel2RegulatoryGraph.importModel(graph1.getModel(), to_remove);
		new ModelBooleanizerService().copyNodeStyles(graph1, simplifiedGraph);
		return simplifiedGraph;
	}

}

class ExtractFromSCCGraphAction extends GenericGraphAction {
	private static final long serialVersionUID = 8294301473668672512L;

	protected ExtractFromSCCGraphAction(ReducedGraph<?, ?, ?> graph, ServiceGUI serviceGUI) {
		super(graph, "STR_connectivity_extract", null, "STR_connectivity_extract_descr", null, serviceGUI);

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		ReducedGraph<?, ?, ?> g = (ReducedGraph<?, ?, ?>) graph;
		GraphGUI<ReducedGraph<?, ?, ?>, NodeReducedData, ?> gui = GUIManager.getInstance().getGraphGUI(g);
		GraphSelection<NodeReducedData, ?> selection = gui.getSelection();

		if (selection == null || selection.getSelectedNodes() == null || selection.getSelectedNodes().size() < 1) {
			LogManager.debug("Select some nodes");
			GUIMessageUtils.openErrorDialog("STR_unableToOpen_SeeLogs");
			return;
		}

		String s_ag = null;
		try {
			s_ag = g.getAssociatedGraphID();

		} catch (Exception e) {
		}

		if (s_ag == null) {
			try {
				Graph ag = g.getAssociatedGraph();
				if (ag != null) {
					boolean save = GUIMessageUtils.openConfirmationDialog(
							"This requires to save the original graph. Save it now?", "Save associated graph?");
					if (save) {
						GraphGUI graph_gui = GUIManager.getInstance().getGraphGUI(ag);
						graph_gui.saveAs();
						s_ag = g.getAssociatedGraphID();
					}
				}
			} catch (GsException e1) {
			}
		}

		if (s_ag == null) {
			LogManager.debug("Missing associated Graph");
			GUIMessageUtils.openErrorDialog("STR_unableToOpen_SeeLogs");
			return;
		}

		File f = new File(s_ag);
		if (!f.exists()) {
			LogManager.debug("Missing associated Graph file: " + s_ag);
			GUIMessageUtils.openErrorDialog("STR_unableToOpen_SeeLogs");
			return;
		}

		Graph subgraph = null;
		try {
			Set<?> set = g.getSelectedSet(selection.getSelectedNodes());
			subgraph = GSGraphManager.getInstance().open(set, f);
		} catch (GsException e) {
			e.printStackTrace();
		}

		if (subgraph == null) {
			LogManager.debug("SCC extraction led to a null graph");
			return;
		}

		GUIManager.getInstance().whatToDoWithGraph(subgraph, graph);
	}
}
