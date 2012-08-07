package org.ginsim.service.tool.modelsimplifier;

import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.Service;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(Service.class)
@Alias("reduction")
public class ModelSimplifierService implements Service {

    static {
    	if( !ObjectAssociationManager.getInstance().isObjectManagerRegistred( RegulatoryGraph.class, ModelSimplifierConfigManager.KEY)){
    		ObjectAssociationManager.getInstance().registerObjectManager( RegulatoryGraph.class, new ModelSimplifierConfigManager());
        }
    }

    public ModelRewiring getRewirer( RegulatoryGraph graph) {
    	return new ModelRewiring(graph);
    }
}