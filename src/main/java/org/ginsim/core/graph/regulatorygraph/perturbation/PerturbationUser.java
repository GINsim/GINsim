package org.ginsim.core.graph.regulatorygraph.perturbation;

import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;

/**
 * Simple standalone perturbation user.
 * Remembers the selected perturbation through the list of users included in {@link ListOfPerturbations}.
 * 
 * @author Aurelien Naldi
 */
public class PerturbationUser implements PerturbationHolder {

	private static final ObjectAssociationManager OManager = ObjectAssociationManager.getInstance();
	
	private final String userID;
	private final ListOfPerturbations perturbations;
	private Perturbation perturbation;
	
	public PerturbationUser(RegulatoryGraph graph, String userID) {
		this((ListOfPerturbations)OManager.getObject(graph, PerturbationManager.KEY, true), userID);
	}

	public PerturbationUser(ListOfPerturbations perturbations, String userID) {
		this.perturbations = perturbations;
		this.userID = userID;
		this.perturbation = perturbations.getUsedPerturbation(userID);
	}
	
	@Override
	public Perturbation getPerturbation() {
		return perturbation;
	}

	@Override
	public void setPerturbation(Perturbation perturbation) {
		perturbations.usePerturbation(userID, perturbation);
		this.perturbation = perturbations.getUsedPerturbation(userID);
	}
}
