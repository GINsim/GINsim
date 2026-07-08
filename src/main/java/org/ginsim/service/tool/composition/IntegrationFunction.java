package org.ginsim.service.tool.composition;

import java.util.ArrayList;
import java.util.Collection;

import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;

/**
 * The enumeration of all prototypical Integration functions
 * 
 * @author Nuno D. Mendes
 */

public enum IntegrationFunction {
	MAX, MIN, AND, OR, THRESHOLD2, MAX_LEFT, MAX_RIGHT;

	/**
	 * @param integrationFunction an integration function
	 * 
	 * @return  True if the given integration function is implemented, false
	 *         otherwise.
	 */
	public static boolean isImplemented(IntegrationFunction integrationFunction) {
		if (integrationFunction.equals(AND) || integrationFunction.equals(OR)
				|| integrationFunction.equals(MIN)
				|| integrationFunction.equals(MAX)
				|| integrationFunction.equals(THRESHOLD2)) {
			return true;
		}
		return false;
	}

	/**
	 * @param input
	 *            an input component
	 * 
	 * @param properComponents
	 *            a list of proper components
	 * 
	 * @return a list of integration functions that can be applied to the type
	 *         of input/proper components given
	 */
	public static Collection<IntegrationFunction> whichCanApply(
			RegulatoryNode input, Collection<RegulatoryNode> properComponents) {
		Collection<IntegrationFunction> canApply = new ArrayList<IntegrationFunction>();

		for (IntegrationFunction integrationFunction : IntegrationFunction
				.values()) {

			switch (integrationFunction) {
			case MAX: {

				int maxvalue = 0;
				if (properComponents != null) {
					for (RegulatoryNode proper : properComponents) {
						if (proper.getMaxValue() > maxvalue)
							maxvalue = proper.getMaxValue();
					}
					if (input.getMaxValue() >= maxvalue && maxvalue > 1) {
						canApply.add(integrationFunction);
					}
				} else {
					if (input.getMaxValue() > 1)
						canApply.add(integrationFunction);
				}
			}

				continue;
			case MIN: {
				int maxvalue = 0;
				if (properComponents != null) {
					for (RegulatoryNode proper : properComponents) {
						if (proper.getMaxValue() > maxvalue)
							maxvalue = proper.getMaxValue();
					}
					if (input.getMaxValue() == maxvalue && maxvalue > 1) {
						canApply.add(integrationFunction);
					}
				} else {
					if (input.getMaxValue() > 1)
						canApply.add(integrationFunction);
				}
			}

				continue;

			case AND:
			case OR:
				if (input.getMaxValue() > 1)
					continue;
				boolean authorized = true;
				if (properComponents != null) {
					for (RegulatoryNode proper : properComponents) {
						if (proper.getMaxValue() > 1) {
							authorized = false;
							continue;
						}
					}
				}
				if (authorized)
					canApply.add(integrationFunction);
				continue;
			case THRESHOLD2:
				{
					if (properComponents != null) {
						boolean multiValuedArguments = false;
						for (RegulatoryNode proper : properComponents) {
							if (proper.getMaxValue() > 1)
								multiValuedArguments = true;
						}
						if (input.getMaxValue() == 1 && multiValuedArguments) {
							canApply.add(integrationFunction);
						}
					} else {
						if (input.getMaxValue() == 1)
							canApply.add(integrationFunction);
					}
				}
				continue;
			case MAX_LEFT:
			case MAX_RIGHT:
				continue;

			}
		}

		Collection<IntegrationFunction> implementedCanApply = new ArrayList<IntegrationFunction>();
		for (IntegrationFunction integrationFunction : canApply)
			if (IntegrationFunction.isImplemented(integrationFunction))
				implementedCanApply.add(integrationFunction);
		return implementedCanApply;

	}

	/**
	 * 
	 * @param input
	 *            an input component
	 * @return a list of integration functions that can be applied to this input
	 *         components, assuming adequate proper components
	 */
	public static Collection<IntegrationFunction> whichCanApply(
			RegulatoryNode input) {
		return whichCanApply(input, null);
	}

	/**
	 * 
	 * @param integrationFunction
	 * @return an instance of IntegrationFunctionReification able to implement
	 *         the specified integrationFunction
	 */
	public static IntegrationFunctionReification getIntegrationFunctionComputer(
			IntegrationFunction integrationFunction) {
		return new IntegrationFunctionReification(integrationFunction);
	}

	public static class IntegrationFunctionReification {
		IntegrationFunction integrationFunction = null;

		public IntegrationFunctionReification(
				IntegrationFunction integrationFunction) {
			this.integrationFunction = integrationFunction;
		}

		public Integer compute(Collection<Integer> arguments) {
			int result = -1;

			switch (this.integrationFunction) {
			case MAX:
				for (Integer argument : arguments)
					if (argument > result)
						result = argument.intValue();
				break;
			case MIN:
				for (Integer argument : arguments)
					if (result < 0 || result > argument.intValue())
						result = argument.intValue();
				break;
			case AND:
				result = 1;
				for (Integer argument : arguments)
					result *= argument.intValue();
				break;

			case OR:
				result = 0;
				for (Integer argument : arguments)
					result += argument.intValue();

				if (result > 0)
					result = 1;
				break;

			case THRESHOLD2:
				result = 1;
				for (Integer argument : arguments)
					result *= (argument >= 2 ? 1 : 0);
				break;
			default:
				break;

			}

			if (result < 0)
				return null;

			return new Integer(result);

		}

	}

}