package org.ginsim.service.export.nusmv;

import java.util.HashMap;
import java.util.Map;

import org.colomoto.biolqm.LogicalModel;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedState;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateStore;
import org.ginsim.service.tool.reg2dyn.priorityclass.PrioritySetDefinition;
import org.ginsim.service.tool.reg2dyn.priorityclass.UpdaterDefinitionStore;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinition;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinitionAsynchronous;
import org.ginsim.service.tool.reg2dyn.updater.UpdaterDefinitionSynchronous;

public class NuSMVConfig implements NamedStateStore, UpdaterDefinitionStore {

	public static final int CFG_SYNC = 0;
	public static final int CFG_ASYNC = 1;
	public static final int CFG_PCLASS = 2;
	public static final int CFG_COMPLETE = 3;

	private LogicalModel model;
	private Map<NamedState, Object> m_initStates;
	private Map<NamedState, Object> m_input;

	private UpdaterDefinition updater;
	private int updatePolicy;
	private boolean bFixedInputs;

	/**
	 * @param model
	 */
	public NuSMVConfig(LogicalModel model) {
		this.m_initStates = new HashMap<NamedState, Object>();
		this.m_input = new HashMap<NamedState, Object>();
		this.model = model;
		this.updatePolicy = CFG_ASYNC; // Default update policy
		this.bFixedInputs = true;
	}

	public void setUpdatePolicy() {
		if (updater == null || updater == UpdaterDefinitionAsynchronous.DEFINITION) {
			updatePolicy = CFG_ASYNC;
		} else if (updater == UpdaterDefinitionSynchronous.DEFINITION) {
			updatePolicy = CFG_SYNC;
		} else if (updater instanceof PrioritySetDefinition) {
			PrioritySetDefinition priorities = (PrioritySetDefinition)updater;
			if (priorities.size() == 1) {
				if (priorities.getPclass(model.getComponents())[0][1] == 0) {
					updatePolicy = CFG_SYNC;
				} else {
					updatePolicy = CFG_ASYNC;
				}
			} else {
				updatePolicy = CFG_PCLASS;
			}
		} else {
			updatePolicy = CFG_COMPLETE;
		}
	}

	public void updateModel(LogicalModel model) {
		this.model = model;
	}

	public void setUpdatePolicy(int policy) {
		updatePolicy = policy;
	}

	public int getUpdatePolicy() {
		return updatePolicy;
	}
	
	public Map<NamedState, Object> getInitialState() {
		return m_initStates;
	}

	public Map<NamedState, Object> getInputState() {
		return m_input;
	}

	public LogicalModel getModel() {
		return model;
	}

	public void setFixedInputs(boolean isFixed) {
		this.bFixedInputs = isFixed;
	}
	
	public boolean isFixedInputs() {
		return this.bFixedInputs;
	}

	@Override
	public UpdaterDefinition getUpdatingMode() {
		return updater;
	}

	@Override
	public void setUpdatingMode(UpdaterDefinition pcdef) {
		this.updater = pcdef;
		setUpdatePolicy();
	}
}
