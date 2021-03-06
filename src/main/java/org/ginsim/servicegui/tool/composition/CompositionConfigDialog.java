package org.ginsim.servicegui.tool.composition;

import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.service.tool.composition.CompositionService;

/**
 * The composition dialog
 * 
 * @author Nuno D. Mendes
 */

public class CompositionConfigDialog extends StackDialog {

	// TODO: Replace all strings by token in messages.properties

	private static final long serialVersionUID = 8046844091168372569L;
	RegulatoryGraph graph = null;
	CompositionPanel dialog = null;
	boolean isRunning = false;

	CompositionConfigDialog(RegulatoryGraph graph) {
		super(graph, "modelComposer", 700, 300);
		this.graph = graph;
		setTitle("Specify Composition parameters");

		CompositionPanel panel = new CompositionPanel(graph);
		dialog = panel;
		brun.setText("Compose instances");
		brun.setToolTipText("Compose");
		setMainPanel(panel.getMainPanel());
		setVisible(true);
		setSize(getPreferredSize());

	}

	protected void run() throws GsException {
		setRunning(true);
		brun.setEnabled(false);

		CompositionService service = GSServiceManager.getService(CompositionService.class);

        try {
		    RegulatoryGraph composedGraph = service.run(graph, dialog.getConfig());
		    GUIManager.getInstance().whatToDoWithGraph(composedGraph);
        } catch (Exception e) {
            throw new GsException("Error during the composition", e);
        }

		cancel();
	}

}
