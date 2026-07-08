package org.ginsim.service.tool.reg2dyn.limitedsimulation;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.hierarchicaltransitiongraph.HierarchicalNode;
import org.ginsim.core.graph.objectassociation.BasicGraphAssociatedManager;
import org.ginsim.core.graph.objectassociation.GraphAssociatedObjectManager;
import org.kohsuke.MetaInfServices;

/**
 * An GraphAssociatedObjectManager providing an HashMap
 * <code>&lt;DynamicNode, HierarchicalNode&gt; storing a mapping between states and their corresponding HierarchicalNode</code>
 * 
 * Usage : 
 * <code>(HashMap&lt;DynamicNode, HierarchicalNode&gt;) ObjectAssociationManager.getInstance().getObject(dynamicGraph, StatesToHierarchicalMappingManager.key, true);</code>
 * @author Duncan Berenguier
 */
@MetaInfServices(GraphAssociatedObjectManager.class)
public class StatesToHierarchicalMappingManager extends BasicGraphAssociatedManager {

	public static final String KEY = "statesToHierarchical";

	public StatesToHierarchicalMappingManager() {
		super(KEY, null, DynamicGraph.class);
	}
	
	@Override
	public boolean needSaving(Graph graph) {
		return false;
	}

	@Override
	public void doSave(OutputStreamWriter out, Graph graph) throws GsException {
	}

	@Override
	public Object doOpen(InputStream is, Graph graph) throws GsException {
		return null;
	}

	@Override
	public Object doCreate(Graph graph) {
		return new HashMap<byte[], HierarchicalNode>();
	}

}
