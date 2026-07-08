package org.ginsim.servicegui.tool.composition;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;

import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.service.tool.composition.IntegrationFunctionMapping;

/**
 * Interface for the Specification of Composition Parameters
 * 
 * @author Nuno D. Mendes
 */
public interface CompositionSpecificationDialog {

	/**
	 * Getter of the NumberInstances
	 * @return the NumberInstances
	 */
	public int getNumberInstances();

	/**
	 * Update the NumberInstances
	 * @param instances update numbre of instance
	 */
	public void updateNumberInstances(int instances);

	/**
	 * Set  RegulatoryNode as Mapped
	 * @param node the RegulatoryNode
	 */
	public void setAsMapped(RegulatoryNode node);

	/**
	 * Unset function
	 * @param node the RegulatoryNode
	 */
	public void unsetAsMapped(RegulatoryNode node);

	/**
	 * MappedNodes getter
	 * @return the list of RegulatoryNode
	 */
	public List<RegulatoryNode> getMappedNodes();

	/**
	 * Add a Neighbours
	 * @param m index m
	 * @param n index n
	 */
	public void addNeighbour(int m, int n);

	/**
	 * Remove neighboor
	 * @param m index m
	 * @param n index n
	 */
	public void removeNeighbour(int m, int n);

	/**
	 * Test if boolean
	 * @param m  state m
	 * @return boolean if boolean
	 */
	public boolean hasNeihgbours(int m);

	/**
	 * Test if are Neighbours
	 * @param m index
	 * @param n index
	 * @return boolean if are Neighbours
	 */
	public boolean areNeighbours(int m, int n);

	/**
	 * Getter of  IntegrationFunctionMapping
	 * @return the  IntegrationFunctionMapping
	 */
	public IntegrationFunctionMapping getMapping();

	/**
	 * Graph getter
	 * @return the  RegulatoryGraph
	 */
	public RegulatoryGraph getGraph();

	/**
	 * Test if TrulyMapped
	 * @param node the RegulatoryNode
	 * @param m state m int
	 * @return boolean if TrulyMapped
	 */
	public boolean isTrulyMapped(RegulatoryNode node, int m);

	/**
	 * InfluencedModuleInputs Getter
	 * @param proper the RegulatoryNode
	 * @param moduleIndex the indice
	 * @return collection of map for InfluencedModuleInputs
	 */
	public Collection<AbstractMap.Entry<RegulatoryNode, Integer>> getInfluencedModuleInputs(
			RegulatoryNode proper, int moduleIndex);

	/**
	 * Getter of MappedToModuleArguments
	 * @param input
	 * @param moduleIndex
	 * @return collection of map for MappedToModuleArguments
	 */
	public Collection<AbstractMap.Entry<RegulatoryNode, Integer>> getMappedToModuleArguments(
			RegulatoryNode input, int moduleIndex);

	/**
	 * IntegrationMappingChang
	 */
	public void fireIntegrationMappingChange();
}
