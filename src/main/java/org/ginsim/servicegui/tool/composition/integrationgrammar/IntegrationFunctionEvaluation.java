package org.ginsim.servicegui.tool.composition.integrationgrammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.colomoto.biolqm.NodeInfo;
import org.ginsim.servicegui.tool.composition.integrationgrammar.IntegrationFunctionSpecification.IntegrationAtom;
import org.ginsim.servicegui.tool.composition.integrationgrammar.IntegrationFunctionSpecification.IntegrationExpression;
import org.ginsim.servicegui.tool.composition.integrationgrammar.IntegrationFunctionSpecification.IntegrationNegation;
import org.ginsim.servicegui.tool.composition.integrationgrammar.IntegrationFunctionSpecification.IntegrationOperation;

public class IntegrationFunctionEvaluation {

	private IntegrationExpression expression = null;
	private CompositionContext context = null;

	public IntegrationFunctionEvaluation(IntegrationExpression expression,
			CompositionContext context) {
		this.expression = expression;
		this.context = context;
	}

	public boolean evaluate(int instance, byte[][] state) {
		return traverseTreeEvaluate(instance, state, expression);
	}

	private boolean traverseTreeEvaluate(int instance, byte[][] state,
			IntegrationExpression expression) {

		boolean result = false;

		if (expression instanceof IntegrationOperation) {
			List<IntegrationExpression> listOperands = ((IntegrationOperation) expression)
					.getOperands();

			IntegrationLogicalOperator operator = ((IntegrationOperation) expression)
					.getOperation();

			switch (operator) {
			case AND:
				result = true;
				for (IntegrationExpression operand : listOperands)
					if (operand == null)
						continue;
					else if (!traverseTreeEvaluate(instance, state, operand)) {
						return false;
					}
				break;
			case OR:
				result = false;
				for (IntegrationExpression operand : listOperands)
					if (operand == null)
						continue;
					else if (traverseTreeEvaluate(instance, state, operand)) {
						return true;
					}
				break;
			}
		} else if (expression instanceof IntegrationNegation) {
			return !traverseTreeEvaluate(instance, state,
					((IntegrationNegation) expression).getNegatedExpression());

		} else if (expression instanceof IntegrationAtom) {
			IntegrationAtom atom = (IntegrationAtom) expression;
			String componentName = atom.getComponentName();

			NodeInfo node = null;
			for (NodeInfo n : context.getLowLevelComponents())
				if (n.getNodeID().equalsIgnoreCase(componentName))
					node = n;

			Set<Integer> neighbours = context.getNeighbourIndices(instance,
					atom.getMinDistance(), atom.getMaxDistance());

			byte threshold = atom.getThreshold();
			if (threshold < 0)
				threshold = node.getMax();

			int min = atom.getMinNeighbours();
			if (min < 0)
				min = neighbours.size();

			int max = atom.getMaxNeighbours();
			if (max < 0)
				max = neighbours.size();

			int habilitations = 0;

			if (min > neighbours.size() || min > max) {
				// condition is trivially impossible to satisfy
				return false;
			} else if (threshold == 0 && max < neighbours.size()) {
				// condition is trivially impossible to satisfy
				return false;
			} else if (min == 0 && max == neighbours.size()) {
				// condition is trivially tautological
				return true;
			} else if (threshold == 0 && max == neighbours.size()) {
				// condition is trivially tautological
				return true;
			}

			List<Byte> listValues = new ArrayList<Byte>();
			for (Integer nindex : neighbours)
				listValues.add(state[nindex][context.getLowLevelComponents()
						.indexOf(node)]);

			for (Byte value : listValues)
				if (value.byteValue() >= threshold)
					habilitations++;

			if (habilitations >= min && habilitations <= max)
				return true;
			else
				return false;

		}

		return result;
	}

}
