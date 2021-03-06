package org.ginsim.servicegui.tool.regulatorytreefunction;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;

import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.tree.Tree;
import org.ginsim.core.graph.tree.TreeBuilder;
import org.ginsim.core.graph.tree.TreeBuilderFromRegulatoryGraph;
import org.ginsim.core.service.EStatus;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.service.StandaloneGUI;
import org.ginsim.gui.shell.actions.ToolkitAction;
import org.kohsuke.MetaInfServices;


@MetaInfServices( ServiceGUI.class)
@StandaloneGUI
@ServiceStatus( EStatus.DEVELOPMENT)
public class RegulatoryFunctionTreeServiceGUI extends AbstractServiceGUI {

	private static final Integer ZERO = 0;

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<>();
			actions.add(new RegulatoryFunctionTreeAction( (RegulatoryGraph)graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_TOOLKITS_MAIN + 10;
	}

	class RegulatoryFunctionTreeAction extends ToolkitAction {


		private final RegulatoryGraph graph;
		private final GraphGUI<?, ?, ?> gui;

		private RegulatoryFunctionTreeAction( RegulatoryGraph graph, ServiceGUI serviceGUI) {

			super( "STR_treeViewer_regulatoryPlugin", "STR_treeViewer_regulatoryPlugin_descr", serviceGUI);
			this.graph = graph;
			this.gui = GUIManager.getInstance().getGraphGUI(graph);
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			TreeBuilder parser = new TreeBuilderFromRegulatoryGraph();
			Tree tree = GSGraphManager.getInstance().getNewGraph( Tree.class, parser);

			parser.setParameter(TreeBuilderFromRegulatoryGraph.PARAM_NODEORDER, graph.getNodeOrder());
			parser.setParameter(TreeBuilderFromRegulatoryGraph.PARAM_REGGRAPH, graph);
			parser.setParameter(TreeBuilderFromRegulatoryGraph.PARAM_INITIALVERTEXINDEX, getSelectedNode( graph));
			GUIManager.getInstance().newFrame(tree, false);
		}

		/**
		 * Return the index of the first selected gene in the regulatory graph, or 0 if none are selected.
		 * @param regGraph
		 * @return
		 */
		private Integer getSelectedNode(RegulatoryGraph regGraph) {

			Collection<?> vertices = gui.getSelection().getSelectedNodes();
			if (vertices == null || vertices.size() < 1) {
				return ZERO;
			}
			RegulatoryNode selectedNode = (RegulatoryNode) vertices.iterator().next();
			int i = 0;
			for (RegulatoryNode v: regGraph.getNodeOrder()) {
				if (v.equals(selectedNode)) {
					return i;
				}
			}
			return ZERO;
		}
	}

}
