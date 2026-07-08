package org.ginsim.servicegui.tool.reg2dyn;

import java.awt.Frame;

import org.colomoto.biolqm.LogicalModel;
import org.ginsim.common.application.OptionStore;
import org.ginsim.common.application.Txt;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.utils.data.*;
import org.ginsim.service.tool.reg2dyn.Reg2DynService;
import org.ginsim.service.tool.reg2dyn.Simulation;
import org.ginsim.service.tool.reg2dyn.SimulationParameterList;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;


/**
 * The frame displayed to the user when he want to run a simulation
 */
public class SingleSimulationFrame extends BaseSimulationFrame {
	private static final long serialVersionUID = 8687415239702718705L;
	
/* *************** SIMULATION RELATED PARAMETERS **********************/
	/**
	 * The regulatoryGraph frame
	 */
	private Frame regGraphFrame;
	private SimulationParameterList paramList;
	private Simulation sim;
	boolean isrunning = false;

/* *************** GUI RELATED PARAMETERS **********************/
	/* ****** PANELS **********/
    private final ListEditionPanel spane;

	public SingleSimulationFrame(Frame regGraphFrame, SimulationParameterList paramList) {
		super(paramList.graph, regGraphFrame, "display.simulation", 800, 400);
		setUserID("reg2dyn");
		this.regGraphFrame = regGraphFrame;
		this.paramList = paramList;
        this.spane = new ListEditionPanel(ListOfSimulationParametersHelper.HELPER, paramList, Txt.t("STR_simulationSettings"), this, null);
		GUIManager.getInstance().addBlockEdit( paramList.graph, this);
        spane.setName("display.configSimulation");
        setMainPanel(spane);
		this.setTitle(Txt.t("STR_reg2dynRunningTitle"));
		this.addWindowListener(new java.awt.event.WindowAdapter() { 
			public void windowClosing(java.awt.event.WindowEvent e) {
				cancel();
			}
		});
	}

/* *************** RUN AND SIMULATION **********************/

	@Override
	public void setResult( Object graph) {
		isrunning = false;
		if (null == graph) {
			GUIMessageUtils.openErrorDialog("no graph generated", regGraphFrame);
		} else {
			GUIManager.getInstance().whatToDoWithGraph( (Graph)graph);
		}
		cancel();
	}

	@Override
	protected void cancel() {
		if (isrunning) {
			sim.interrupt();	
		}
		GUIManager.getInstance().removeBlockEdit( paramList.graph, this);
		OptionStore.setOption(id+".width", new Integer(getWidth()));
		OptionStore.setOption(id+".height", new Integer(getHeight()));
		super.cancel();
	}
	@Override
	public void run(LogicalModel model) {
        SimulationParameters currentParameter = (SimulationParameters)spane.getSelectedItem();
		if (currentParameter == null) {
			return;
		}
		setMessage(Txt.t("STR_wait_msg"));
		bcancel.setText(Txt.t("STR_abort"));
		//FIXME: nearly everything should be disabled
//		radioBreadthFirst.setEnabled(false);
//		radioDephtFirst.setEnabled(false);
//		selectPriorityClass.setEnabled(false);
//
//		initStatePanel.setEnabled(false);
//		initStatePanel.setEnabled(false);
//		textMaxDepth.setEnabled(false);
//		textMaxNodes.setEnabled(false);
        brun.setEnabled(false);
		isrunning = true;
		Reg2DynService service = GSServiceManager.getService( Reg2DynService.class);
		sim = service.get( model, this, currentParameter, this.realReduction);
		new Thread(sim).start();
	}


	@Override
	public void milestone(Object item) {
	}
}
