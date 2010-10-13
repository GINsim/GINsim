package fr.univmrs.tagc.GINsim.stg2htg;

import javax.swing.JFrame;

import fr.univmrs.tagc.GINsim.css.Selector;
import fr.univmrs.tagc.GINsim.graph.GsActionProvider;
import fr.univmrs.tagc.GINsim.graph.GsGraph;
import fr.univmrs.tagc.GINsim.gui.GsPluggableActionDescriptor;
import fr.univmrs.tagc.GINsim.plugin.GsPlugin;
import fr.univmrs.tagc.common.GsException;

public class STG2HTGPlugin implements GsPlugin, GsActionProvider{

	private GsPluggableActionDescriptor[] t_action = null;

	public void registerPlugin() {
		GsGraph.registerActionProvider(this);
		Selector.registerSelector(STG2HTGSelector.IDENTIFIER, STG2HTGSelector.class);
	}
	
	public GsPluggableActionDescriptor[] getT_action(int actionType, GsGraph graph) {
		if (actionType != ACTION_ACTION) {
			return null;
		}
		if (t_action == null) {
			t_action = new GsPluggableActionDescriptor[1];
			t_action[0] = new GsPluggableActionDescriptor("STG to HTG", "STR_STG2HTG_descr", null, this, ACTION_ACTION, 0);
		}
		return t_action;
	}
	
	public void runAction(int actionType, int ref, GsGraph graph, JFrame frame) throws GsException {
		System.out.println("Runnnn action");
		if (actionType != ACTION_ACTION) {
			return;
		}
		if (ref == 0) {
			Thread thread = new STG2HTG(graph);
			thread.start();
		}
	}

}

