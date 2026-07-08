package org.ginsim.service.tool.composition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.ginsim.common.application.GsException;
import org.ginsim.core.annotation.Annotation;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryEdgeSign;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameter;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameterList;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.service.tool.modelreduction.ReductionConfig;
import org.ginsim.service.tool.modelreduction.ReductionTask;
import org.ginsim.service.tool.modelreduction.ReconstructionTask;
import org.kohsuke.MetaInfServices;

/**
 * Module composition service
 * @author Nuno D. Mendes
 */
@MetaInfServices(Service.class)
@Alias("composition")
@ServiceStatus(EStatus.RELEASED)
public class CompositionService implements Service {

	/**
	 * Run the Composition by replicating a Regulatory graph, according to a
	 * Topology and an IntegrationFunctionMapping and return the composed graph
	 * after invoking the necessary reduction operations
	 * 
	 * @param graph the current RegulatoryGraph
	 * @param config   the composition Topology, indicating the neighbourhood
	 *                 relationships
	 *                 the integration functions to apply to each mapped input
	 * 
	 * @return RegulatoryGraph the graph
	 * @throws GsException the graph exception
	 */
	public RegulatoryGraph run(RegulatoryGraph graph, CompositionConfig config)
			throws Exception {

		return computeComposedGraph(graph, config);

	}

	protected RegulatoryGraph computeComposedGraph(RegulatoryGraph graph,
			CompositionConfig config) throws Exception {

		Topology topology = config.getTopology();
		IntegrationFunctionMapping mapping = config.getMapping();
		boolean reduce = config.isReduce();

		RegulatoryGraph composedGraph = GSGraphManager.getInstance().getNewGraph(
				RegulatoryGraph.class);

		NodeAttributesReader oldNodeReader = graph.getNodeAttributeReader();
		NodeAttributesReader newNodeReader = composedGraph
				.getNodeAttributeReader();

		EdgeAttributesReader oldEdgeReader = graph.getEdgeAttributeReader();
		EdgeAttributesReader newEdgeReader = composedGraph
				.getEdgeAttributeReader();

		double minX = -1;
		double maxX = 0;
		double minY = -1;
		double maxY = 0;

		for (RegulatoryNode node : graph.getNodeOrder()) {
			oldNodeReader.setNode(node);

			if (oldNodeReader.getX() < minX || minX < 0)
				minX = oldNodeReader.getX();

			if (oldNodeReader.getX() > maxX)
				maxX = oldNodeReader.getX();

			if (oldNodeReader.getY() < minY || minY < 0)
				minY = oldNodeReader.getY();

			if (oldNodeReader.getY() > maxY)
				maxY = oldNodeReader.getY();

		}

		double width = maxX - minX;
		double height = maxY - minY;
		double deltaX = 1.8 * width;
		double deltaY = 1.7 * height;

		for (RegulatoryNode node : graph.getNodeOrder()) {

			oldNodeReader.setNode(node);

			for (int i = 0; i < topology.getNumberInstances(); i++) {
				RegulatoryNode newNode = composedGraph.addNewNode(
						computeNewName(node.getNodeInfo().getNodeID(), i),
						computeNewName(node.getNodeInfo().getNodeID(), i),
						node.getMaxValue());

				newNode.getNodeInfo().setMax(node.getNodeInfo().getMax());
				newNode.getNodeInfo().setNodeID(
						computeNewName(node.getNodeInfo().getNodeID(), i));
				newNode.setName(computeNewName(node.getNodeInfo().getNodeID(),
						i), composedGraph);
				// FIXME: transfer annotations

				// mapped inputs with neighbours will no longer be inputs
				// unmapped input remain free
				if (node.isInput()
						&& (!mapping.isMapped(node) || !topology
								.hasNeighbours(i))) {
					newNode.setInput(node.isInput(), composedGraph);
				}

				newNodeReader.setNode(newNode);
				newNodeReader.copyFrom(oldNodeReader);

				if (i > 0)
					newNodeReader.move((int) deltaX * i, 0);

				if (i % 2 == 1)
					newNodeReader.move(0, (int) deltaY);

			}

		}

		HashMap<RegulatoryMultiEdge, List<RegulatoryMultiEdge>> edgeAssociation = new HashMap<RegulatoryMultiEdge, List<RegulatoryMultiEdge>>();

		for (RegulatoryMultiEdge multiEdge : graph.getEdges()) {
			for (int i = 0; i < topology.getNumberInstances(); i++) {
				RegulatoryNode source = multiEdge.getSource();
				RegulatoryNode target = multiEdge.getTarget();

				RegulatoryNode newSource = composedGraph
						.getNodeByName(computeNewName(source.getNodeInfo()
								.getNodeID(), i));
				RegulatoryNode newTarget = composedGraph
						.getNodeByName(computeNewName(target.getNodeInfo()
								.getNodeID(), i));

				RegulatoryMultiEdge newMultiEdge = new RegulatoryMultiEdge(
						composedGraph, newSource, newTarget,
						RegulatoryEdgeSign.POSITIVE);

				newMultiEdge.copyFrom(multiEdge);
				composedGraph.addEdge(newMultiEdge);

				oldEdgeReader.setEdge(multiEdge);
				newEdgeReader.setEdge(newMultiEdge);
				newEdgeReader.copyFrom(oldEdgeReader);

				if (!edgeAssociation.containsKey(multiEdge)) {
					edgeAssociation.put(multiEdge,
							new ArrayList<RegulatoryMultiEdge>());
				}

				edgeAssociation.get(multiEdge).add(i, newMultiEdge);

			}
		}

		for (RegulatoryNode node : graph.getNodeOrder()) {
			for (int i = 0; i < topology.getNumberInstances(); i++) {

				RegulatoryNode newNode = composedGraph
						.getNodeByName(computeNewName(node.getNodeInfo()
								.getNodeID(), i));
				LogicalParameterList logicalParameterList = node
						.getV_logicalParameters();
				for (LogicalParameter logicalParameter : logicalParameterList) {
					@SuppressWarnings("unchecked")
					List<RegulatoryEdge> edgeList = (List<RegulatoryEdge>) logicalParameter
							.getEdges();
					List<RegulatoryEdge> newEdgeIndex = new ArrayList<RegulatoryEdge>();
					for (RegulatoryEdge edge : edgeList) {
						RegulatoryMultiEdge oldMultiEdge = edge.me;
						RegulatoryMultiEdge newMultiEdge = edgeAssociation.get(
								oldMultiEdge).get(i);
						newEdgeIndex.add(newMultiEdge.getEdge(edge.index));
					}

					// logical parameters are copied
					// logical functions, if any, should be copied too
					// TODO: Deal with logical functions
					newNode.addLogicalParameter(new LogicalParameter(
							newEdgeIndex, logicalParameter.getValue()), true);
				}
			}
		}

		// Add integration function interactions
		Collection<RegulatoryNode> mappedInputs = mapping.getMappedInputs();
		Collection<RegulatoryNode> newMappedInputs = new ArrayList<RegulatoryNode>();

		for (int i = 0; i < topology.getNumberInstances(); i++) {

			for (RegulatoryNode oldInput : mappedInputs) {
				RegulatoryNode newMapped = composedGraph
						.getNodeByName(computeNewName(oldInput.getNodeInfo()
								.getNodeID(), i));
				if (topology.hasNeighbours(i))
					newMappedInputs.add(newMapped);

				List<RegulatoryNode> properList = mapping
						.getProperComponentsForInput(oldInput);

				IntegrationFunction integrationFunction = mapping
						.getIntegrationFunctionForInput(oldInput);

				for (int j = 0; j < topology.getNumberInstances(); j++) {
					if (!topology.areNeighbours(i, j))
						continue;
					for (RegulatoryNode oldProper : properList) {
						RegulatoryNode newProper = composedGraph
								.getNodeByName(computeNewName(oldProper
										.getNodeInfo().getNodeID(), j));

						if (integrationFunction != null) {
							switch (integrationFunction) {
							case OR:
							case AND: {
								RegulatoryMultiEdge newInteraction = new RegulatoryMultiEdge(
										composedGraph, newProper, newMapped);
								composedGraph.addEdge(newInteraction);
							}
								continue;
							case MIN:
							case MAX: {

								RegulatoryMultiEdge newInteraction = new RegulatoryMultiEdge(
										composedGraph, newProper, newMapped,
										RegulatoryEdgeSign.POSITIVE, (byte) 1);

								for (int v = 2; v <= newProper.getMaxValue(); v++)
									newInteraction.addEdge(
											RegulatoryEdgeSign.POSITIVE, v,
											composedGraph);

								composedGraph.addEdge(newInteraction);
							}

								continue;
							case THRESHOLD2: {
								RegulatoryMultiEdge newInteraction = new RegulatoryMultiEdge(
										composedGraph, newProper, newMapped,
										RegulatoryEdgeSign.POSITIVE, (byte) 2);
								composedGraph.addEdge(newInteraction);
							}

							case MAX_RIGHT:
							case MAX_LEFT:
								// unimplemented
							}
						}

					}
				}

				// add appropriate logical parameters to the function
				// depending on the integration function

				Collection<RegulatoryMultiEdge> listMultiEdges = composedGraph
						.getIncomingEdges(newMapped);
				List<RegulatoryEdge> listEdges = new ArrayList<RegulatoryEdge>();

				if (listMultiEdges != null) {
					// Make sure the list is not null
					// which will happen if (i,j) are not neighbours
					for (RegulatoryMultiEdge multiEdge : listMultiEdges) {
						for (int k = 0; k < multiEdge.getEdgeCount(); k++) {
							listEdges.add(multiEdge.getEdge(k));
						}
					}
				}

				if (integrationFunction != null && listEdges.size() != 0) {

					// TODO: Logical Parameters should be generated by
					// IntegrationFunction.getLogicalParameters()
					switch (integrationFunction) {
					case OR: {
						int bitmask = 0x00;
						while (++bitmask < Math.pow(2, (listEdges.size()))) {
							List<RegulatoryEdge> edgeList = new ArrayList<RegulatoryEdge>();
							for (int pos = 0; pos < listEdges.size(); pos++) {
								int curmask = (int) Math.pow(2, pos);
								if ((bitmask & curmask) != 0)
									edgeList.add(listEdges.get(pos));
							}

							newMapped.addLogicalParameter(new LogicalParameter(
									edgeList, 1), true);

						}
					}
						continue;
					case AND:
						newMapped.addLogicalParameter(new LogicalParameter(
								listEdges, 1), true);
						continue;
					case MIN: {
						int bitmask = 0x00;
						while (++bitmask < Math.pow(2, (listEdges.size()))) {
							List<RegulatoryEdge> edgeList = new ArrayList<RegulatoryEdge>();
							List<RegulatoryNode> addedRegulators = new ArrayList<RegulatoryNode>();
							for (int pos = 0; pos < listEdges.size(); pos++) {
								int curmask = (int) Math.pow(2, pos);
								RegulatoryEdge curEdge = listEdges.get(pos);
								RegulatoryNode curSource = curEdge.me
										.getSource();
								if ((bitmask & curmask) != 0
										&& !addedRegulators.contains(curSource)) {
									edgeList.add(curEdge);
									addedRegulators.add(curSource);
								}
							}

							byte minValue = -1;
							for (RegulatoryEdge edge : edgeList) {
								if (edge.getMin() < minValue || minValue == -1)
									minValue = edge.getMin();
							}

							newMapped.addLogicalParameter(new LogicalParameter(
									edgeList, minValue), true);

						}
					}

						continue;
					case MAX: {
						int bitmask = 0x00;
						while (++bitmask < Math.pow(2, (listEdges.size()))) {
							List<RegulatoryEdge> edgeList = new ArrayList<RegulatoryEdge>();
							List<RegulatoryNode> addedRegulators = new ArrayList<RegulatoryNode>();
							for (int pos = 0; pos < listEdges.size(); pos++) {
								int curmask = (int) Math.pow(2, pos);
								RegulatoryEdge curEdge = listEdges.get(pos);
								RegulatoryNode curSource = curEdge.me
										.getSource();
								if ((bitmask & curmask) != 0
										&& !addedRegulators.contains(curSource)) {
									edgeList.add(curEdge);
									addedRegulators.add(curSource);
								}
							}

							byte maxValue = 0;
							for (RegulatoryEdge edge : edgeList) {
								if (edge.getMin() > maxValue)
									maxValue = edge.getMin();
							}

							newMapped.addLogicalParameter(new LogicalParameter(
									edgeList, maxValue), true);

						}
					}
						continue;
					case THRESHOLD2: {
						int bitmask = 0x00;
						while (++bitmask < Math.pow(2, (listEdges.size()))) {
							List<RegulatoryEdge> edgeList = new ArrayList<RegulatoryEdge>();
							for (int pos = 0; pos < listEdges.size(); pos++) {
								int curmask = (int) Math.pow(2, pos);
								if ((bitmask & curmask) != 0)
									edgeList.add(listEdges.get(pos));
							}

							newMapped.addLogicalParameter(new LogicalParameter(
									edgeList, 1), true);

						}
					}

						continue;
					case MAX_LEFT:
						continue;
					case MAX_RIGHT:
						continue;
					}
				}

			}

		}

		// Reduce the graph

		if (reduce) {
			// Build a ReductionConfig object
			ReductionConfig simplifierConfig = new ReductionConfig();
			for (RegulatoryNode input : newMappedInputs) 
				simplifierConfig.remove(input);
			

			// Mimmick a ReductionLauncher object
			ReductionStub launcher = new ReductionStub(composedGraph);

			ReductionTask simplifier = new ReductionTask(composedGraph,	simplifierConfig, null);
			RegulatoryGraph finalComposedGraph = new ReconstructionTask(simplifier.call(), composedGraph, simplifierConfig).call();

			return finalComposedGraph;
		}

		return composedGraph;
	}

	/**
	 * @param original
	 *            The original name of the component
	 * 
	 * @param moduleId
	 *            The index of the module if belongs to
	 * 
	 * @return The new name of the component
	 */
	private String computeNewName(String original, int moduleId) {
		// moduleId starts at 1, as all iterations begin at 0, we add 1 here
		return original + "_" + (moduleId + 1);
	}

}