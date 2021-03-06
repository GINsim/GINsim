package org.ginsim.servicegui.tool.composition.integrationgrammar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.colomoto.biolqm.NodeInfo;

/**
 * 
 * A clause for the Integration Function, representing a conjunction of
 * conditions
 * 
 * @author Nudo D Mendes
 * 
 */
public class IntegrationFunctionClause {

	protected Map<NodeInfo, Byte> constraints = new HashMap<NodeInfo, Byte>();

	public IntegrationFunctionClause() {

	}

	/**
	 * Add a constraint to a clause
	 * 
	 * @param node
	 *            the component
	 * @param value
	 *            the value
	 */

	public void addConstraint(NodeInfo node, byte value) {
		if (!this.isImpossible())
			this.constraints.put(node, new Byte(value));
	}

	/**
	 * 
	 * @param node
	 *            the component
	 * @return whether there is a constraint on the given component
	 */
	public boolean hasConstraint(NodeInfo node) {
		if (this.constraints == null)
			return false;
		return this.constraints.containsKey(node);
	}

	/**
	 * 
	 * @param node
	 *            the component
	 * @return the value the node is constrained to, if any
	 */

	public Byte getConstraintValue(NodeInfo node) {
		return this.constraints.get(node);
	}

	// public disjunctionWith()
	// in general, this disjunction produces a set of clauses, or the
	// tautological clause
	// since the verification of this is too costly, we leave that to the MDD

	/**
	 * 
	 * @param clause
	 *            another clause
	 * @return the clause resulting from performing the conjunction of the
	 *         current clause with another
	 */
	public IntegrationFunctionClause conjunctionWith(
			IntegrationFunctionClause clause) {

		IntegrationFunctionClause result = new IntegrationFunctionClause();

		if (this.isImpossible() || clause.isImpossible())
			result.setImpossible();
		else if (this.isTautological())
			for (NodeInfo node : clause.getKeySet())
				result.addConstraint(node, clause.getConstraintValue(node));
		else if (clause.isTautological())
			for (NodeInfo node : this.getKeySet())
				result.addConstraint(node, this.getConstraintValue(node));
		else {
			Set<NodeInfo> jointList = new HashSet<NodeInfo>();
			jointList.addAll(this.getKeySet());
			jointList.addAll(clause.getKeySet());

			for (NodeInfo node : jointList) {
				if (this.hasConstraint(node) && !clause.hasConstraint(node))
					result.addConstraint(node, this.getConstraintValue(node));
				else if (!this.hasConstraint(node)
						&& clause.hasConstraint(node))
					result.addConstraint(node, clause.getConstraintValue(node));
				else if (this.hasConstraint(node)
						&& clause.hasConstraint(node)
						&& this.getConstraintValue(node).byteValue() == clause
								.getConstraintValue(node).byteValue())
					result.addConstraint(node, clause.getConstraintValue(node));
				else
					result.setImpossible();
			}

		}

		return result;
	}

	protected Set<NodeInfo> getKeySet() {
		return this.constraints.keySet();
	}

	public boolean isImpossible() {
		return this.constraints == null;
	}

	public boolean isTautological() {
		return this.constraints != null && this.constraints.isEmpty();
	}

	public void setImpossible() {
		this.constraints = null;
	}

	public void setTautological() {
		this.constraints = new HashMap<NodeInfo, Byte>();

	}

	public byte[] toByteArray(CompositionContext context) {
		int size = context.getLowLevelComponents().size();
		byte[] clause = new byte[size];

		int i = 0;
		for (NodeInfo node : context.getLowLevelComponents()) {
			clause[i] = (this.constraints.containsKey(node) ? this
					.getConstraintValue(node).byteValue() : (byte) -1);
			i++;
		}

		return clause;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof IntegrationFunctionClause) || (object == null))
			return false;
		IntegrationFunctionClause clause = (IntegrationFunctionClause) object;
		return this.constraints.equals(clause.constraints);

	}

	@Override
	public int hashCode() {
		if (this.constraints != null)
			return this.constraints.hashCode();
		else
			return 0;
	}

	public String toString() {
		String out = "CLAUSE[" + this.hashCode() + ":[";
		if (this.isImpossible())
			out += "IMPOSSIBLE";
		else if (this.isTautological())
			out += "UNIVERSAL";
		else
			for (NodeInfo node : this.constraints.keySet())
				out += node.getNodeID() + "="
						+ this.constraints.get(node).byteValue() + ",";

		return out + "]";
	}

}
