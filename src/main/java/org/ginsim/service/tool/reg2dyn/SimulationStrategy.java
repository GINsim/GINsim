package org.ginsim.service.tool.reg2dyn;

public enum SimulationStrategy {
	STG("State Transition Graph", "STG"),
	HTG("Hierarchical Transition Graph", "HTG"),
	SCCG("Strongly Connected Components Graph", "SCCG");

	private String description, acronym;

	private SimulationStrategy(String description, String acronym) {
		this.description = description;
		this.acronym = acronym;
	}

	public String getAcronym() {
		return this.acronym;
	}

	public String toString() {
		return this.description;
	}
	
	public static SimulationStrategy fromString(String text) {
		if (text.equals(SimulationStrategy.HTG.toString())) {
			return SimulationStrategy.HTG;
		} else if (text.equals(SimulationStrategy.SCCG.toString())) {
			return SimulationStrategy.SCCG;
		}
		return SimulationStrategy.STG;
	}
}
