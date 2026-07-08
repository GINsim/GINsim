package org.ginsim.gui.graph;

import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;
import org.ginsim.gui.GUIManager;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.common.application.Txt;

public class DeleteAction extends EditAction {

	Graph graph;
	GraphGUI gui = null;
	
	public DeleteAction(Graph<?,?> graph) {
		super( EditMode.DELETE, Txt.t("STR_deleteSelection_descr"), "edit-delete.png");
		this.graph = graph;
		this.gui = GUIManager.getInstance().getGraphGUI(graph);
	}
	
	public void performed(EditActionManager manager) {
		if (gui == null) {
			gui = GUIManager.getInstance().getGraphGUI(graph);
			if (gui == null) {
				LogManager.error("No GUI for the graph");
				return;
			}
		}
		GraphSelection<?, ?> selection = gui.getSelection();
		if (selection == null) {
			LogManager.debug("empty selection");
			return;
		}
		// extend selection for deletion (helps to mark them as damaged)
		selection.extendSelectionToIncomingEdges();
		selection.extendSelectionToOutgoingEdges();
		boolean yes_answer = false;
		if (selection.getSelectedNodes() != null) {
			yes_answer = GUIMessageUtils.openConfirmationDeleteDialog(Txt.t("STR_deleteGraphQuestion" ), Txt.t("STR_deleteComponent"));
			if (!yes_answer){
				return;
			}
			for (Object n: selection.getSelectedNodes()) {
				graph.removeNode(n);
			}
		}
		if (selection.getSelectedEdges() != null) {
			if (!yes_answer) {
				yes_answer = GUIMessageUtils.openConfirmationDeleteDialog(Txt.t("STR_deleteEdgeQuestion"), Txt.t("STR_deleteComponent"));
				if (!yes_answer) {
					return;
				}
			}
			for (Edge<?> e: selection.getSelectedEdges()) {
				graph.removeEdge(e);
			}

		}

		selection.unselectAll();
        graph.updateEvsmap();
        gui.repaint();
	}
}