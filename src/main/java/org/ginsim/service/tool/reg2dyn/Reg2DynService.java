package org.ginsim.service.tool.reg2dyn;

import org.colomoto.logicalmodel.LogicalModel;
import org.ginsim.common.callable.ProgressListener;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.initialstate.InitialStateManager;
import org.ginsim.core.graph.regulatorygraph.mutant.MutantListManager;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.Service;
import org.ginsim.service.tool.reg2dyn.htg.HTGSimulation;
import org.mangosdk.spi.ProviderFor;

@ProviderFor( Service.class)
@Alias("simulation")
public class Reg2DynService implements Service {

    static {
    	if( !ObjectAssociationManager.getInstance().isObjectManagerRegistred( RegulatoryGraph.class, MutantListManager.KEY)){
    		ObjectAssociationManager.getInstance().registerObjectManager(RegulatoryGraph.class, new MutantListManager());
    	}
        ObjectAssociationManager.getInstance().registerObjectManager( RegulatoryGraph.class, new InitialStateManager());
        ObjectAssociationManager.getInstance().registerObjectManager( RegulatoryGraph.class, new SimulationParametersManager());
    }
    
	public Simulation get( LogicalModel model, ProgressListener<Graph> plist, SimulationParameters currentParameter){
		
		Simulation sim;
		
		if (currentParameter.simulationStrategy == SimulationParameters.STRATEGY_STG) {
			sim = new Simulation( model, plist, currentParameter);
		} else {
			sim = new HTGSimulation( model, plist, currentParameter);
		}
		
		return sim;
	}

}