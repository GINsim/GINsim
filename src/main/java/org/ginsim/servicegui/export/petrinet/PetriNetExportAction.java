package org.ginsim.servicegui.export.petrinet;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.io.petrinet.AbstractPNEncoder;
import org.colomoto.biolqm.io.petrinet.PetriNetFormat;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedState;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateStore;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.graph.regulatorygraph.initialstate.InitialStatePanel;
import org.ginsim.gui.shell.actions.ExportAction;
import org.ginsim.gui.utils.dialog.stackdialog.LogicalModelActionDialog;
import org.ginsim.service.format.PetriNetINAFormatService;
import org.ginsim.servicegui.tool.reg2dyn.PrioritySelectionPanel;

public class PetriNetExportAction extends ExportAction<RegulatoryGraph> implements NamedStateStore {

	static final String PNFORMAT = "export.petriNet.defaultFormat";

	private final PetriNetFormat format;
	private LogicalModel model = null;
	Map m_init = null;
	Map m_input = null;

	public PetriNetExportAction(RegulatoryGraph graph, PetriNetFormat format) {
		super(graph, "STR_PetriNet_"+format.getID(), "STR_PetriNet_"+format.getID()+"_descr", null);
		this.format = format;
	}

	protected void doExport( String filename) {
		if (model == null) {
			LogManager.error("Unable to export Petri net: missing logical model");
			return;
		}

		AbstractPNEncoder encoder = format.getExporter(model);

		encoder.setDestination(filename);

		// retrieve the selected initial state if needed
		byte[] initialstate = null;
		Map<NodeInfo,List<Integer>> m_init_values = null;
		if (m_init != null && m_init.size() == 1) {
			NamedState istate = (NamedState)m_init.keySet().iterator().next();
			m_init_values = istate.getMap();
		}
		if (m_input != null && m_input.size() == 1) {
			NamedState istate = (NamedState)m_input.keySet().iterator().next();
			if (m_init_values == null) {
				m_init_values = istate.getMap();
			} else {
				// avoid headaches, merge the two maps
				m_init_values = new HashMap<NodeInfo, List<Integer>>(m_init_values);
				m_init_values.putAll(istate.getMap());
			}
		}
		
		if (m_init_values != null) {
			List<NodeInfo> nodes = model.getComponents();
			initialstate = new byte[nodes.size()];
			int i=0;
			for (NodeInfo ni: nodes) {
				List<Integer> values = m_init_values.get(ni);
				if (values != null && values.size() > 0) {
					initialstate[i] = (byte)values.get(0).intValue();
				}
				i++;
			}
			encoder.setInitialState(initialstate);
		}

		// call the selected export method to do the job
		try {
			encoder.call();
		} catch (Exception e) {
			LogManager.error(e);
		}
	}

	@Override
	protected FileFormatDescription getFileFilter() {
		return new FileFormatDescription(format.getName(), format.getID());
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		new PetriNetExportFrame(null, graph, this);
	}

	public void selectFile(LogicalModel model) {
		this.model = model;
		selectFile();
	}

	@Override
	public Map getInitialState() {
		if (m_init == null) {
			m_init = new HashMap();
		}
		return m_init;
	}

	@Override
	public Map getInputState() {
		if (m_input == null) {
			m_input = new HashMap();
		}
		return m_input;
	}
}

class PetriNetExportFrame extends LogicalModelActionDialog {

	private static final String FORMATOPTIONKEY = "PNformat";
	
	private PetriNetINAFormatService service = GSServiceManager.getService(PetriNetINAFormatService.class);
	private final PetriNetExportAction action;
	private PrioritySelectionPanel priorityPanel = null;
	private InitialStatePanel initStatePanel = null;

	public PetriNetExportFrame(JFrame f, RegulatoryGraph lrg, PetriNetExportAction action) {
		super(lrg, f, "PNGUI", 600, 400);
		this.action = action;
		
    	JPanel mainPanel = new JPanel();
    	mainPanel.setLayout(new GridBagLayout());
    	
    	
    	initStatePanel = new InitialStatePanel(lrg, false);
    	initStatePanel.setParam(action);
    	GridBagConstraints cst = new GridBagConstraints();
    	cst.gridwidth = 2;
    	cst.weightx = 1;
    	cst.weighty = 1;
    	cst.fill = GridBagConstraints.BOTH;
    	mainPanel.add(initStatePanel, cst);

    	cst = new GridBagConstraints();
    	cst.gridx = 0;
    	cst.gridy = 1;
    	cst.fill = GridBagConstraints.HORIZONTAL;
    	mainPanel.add(new JLabel("Petri net format"), cst);

    	
    	cst = new GridBagConstraints();
    	cst.gridx = 1;
    	cst.gridy = 1;
    	cst.weightx = 1;
    	
    	setMainPanel(mainPanel);
	}

	@Override
	public void run(LogicalModel model) {
		action.selectFile(model);
		cancel();
	}

}
