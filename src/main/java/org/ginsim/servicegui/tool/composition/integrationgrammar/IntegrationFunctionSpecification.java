package org.ginsim.servicegui.tool.composition.integrationgrammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.colomoto.logicalmodel.NodeInfo;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.operators.MDDBaseOperators;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.servicegui.tool.composition.CompositionSpecificationDialog;

public class IntegrationFunctionSpecification {

	public static abstract interface IntegrationExpression {
	}

	public static abstract class IntegrationOperation implements
			IntegrationExpression {
		private IntegrationLogicalOperator operator = null;
		private IntegrationExpression expression1 = null;
		private IntegrationExpression expression2 = null;

		public IntegrationOperation(IntegrationExpression exp1,
				IntegrationExpression exp2, IntegrationLogicalOperator operator) {
			this.expression1 = exp1;
			this.expression2 = exp2;
			this.operator = operator;
		}

		public IntegrationLogicalOperator getOperation() {
			return this.operator;
		}

		public List<IntegrationExpression> getOperands() {
			List<IntegrationExpression> listExpression = new ArrayList<IntegrationExpression>();
			listExpression.add(this.expression1);
			listExpression.add(this.expression2);

			return listExpression;
		}

	}

	public static class IntegrationDisjunction extends IntegrationOperation {

		public IntegrationDisjunction(IntegrationExpression exp1,
				IntegrationExpression exp2) {
			super(exp1, exp2, IntegrationLogicalOperator.OR);
		}

	}

	public static class IntegrationConjunction extends IntegrationOperation {

		public IntegrationConjunction(IntegrationExpression exp1,
				IntegrationExpression exp2) {
			super(exp1, exp2, IntegrationLogicalOperator.AND);
		}

	}

	public static class IntegrationAtom implements IntegrationExpression {
		private String componentName = null;
		private byte threshold = 0;
		private int minNeighbours = 0;
		private int maxNeighbours = 0;
		private int distance = 1;

		public IntegrationAtom(String componentName, byte threshold,
				int minNeighbours, int maxNeighbours) {
			this.componentName = componentName;
			this.threshold = threshold;
			this.minNeighbours = minNeighbours;
			this.maxNeighbours = maxNeighbours;
			this.distance = 1;
		}

		public IntegrationAtom(String componentName, byte threshold,
				int minNeighbours, int maxNeighbours, int distance) {
			this.componentName = componentName;
			this.threshold = threshold;
			this.minNeighbours = minNeighbours;
			this.maxNeighbours = maxNeighbours;
			this.distance = distance;

		}

		public String getComponentName() {
			return this.componentName;
		}

		public byte getThreshold() {
			return this.threshold;
		}

		public int getMinNeighbours() {
			return this.minNeighbours;
		}

		public int getMaxNeighbours() {
			return this.maxNeighbours;
		}

		public int getDistance() {
			return this.distance;
		}

	}

	public static class IntegrationNegation implements IntegrationExpression {
		private IntegrationExpression atom = null;

		public IntegrationNegation(IntegrationExpression atom) {
			this.atom = atom;
		}

		public IntegrationExpression getNegatedExpression() {
			return this.atom;
		}
	}

	public static IntegrationExpression createNegation(
			IntegrationExpression atom) {
		return new IntegrationNegation(atom);
	}

	public static IntegrationExpression createAtom(String componentName,
			String thresholdString, String minString, String maxString) {

		byte threshold;
		if (thresholdString.equals("_"))
			threshold = -1;
		else
			threshold = (byte) Integer.parseInt(thresholdString);
		int min;
		if (minString.equals("_"))
			min = -1;
		else
			min = Integer.parseInt(minString);
		int max;
		if (maxString.equals("_"))
			max = -1;
		else
			max = Integer.parseInt(maxString);
		return new IntegrationAtom(componentName, threshold, min, max);
	}

	public static IntegrationExpression createAtom(String componentName,
			String thresholdString, String minString, String maxString,
			String distString) {
		byte threshold;
		if (thresholdString.equals("_"))
			threshold = -1;
		else
			threshold = (byte) Integer.parseInt(thresholdString);
		int min;
		if (minString.equals("_"))
			min = -1;
		else
			min = Integer.parseInt(minString);
		int max;
		if (maxString.equals("_"))
			max = -1;
		else
			max = Integer.parseInt(maxString);
		int distance;
		if (distString.equals("_"))
			distance = 1;
		else
			distance = Integer.parseInt(distString);

		return new IntegrationAtom(componentName, threshold, min, max, distance);
	}

	public static IntegrationExpression createAtom(
			IntegrationExpression expression) {
		return expression;
	}

	public static IntegrationExpression createConjunction(
			IntegrationExpression e1, IntegrationExpression e2) {
		return new IntegrationConjunction(e1, e2);
	}

	public static IntegrationExpression createDisjunction(
			IntegrationExpression e1, IntegrationExpression e2) {
		return new IntegrationDisjunction(e1, e2);
	}

	public static Set<String> getInvalidComponentSpecification(
			CompositionContext context, IntegrationExpression expression) {
		List<RegulatoryNode> listNodes = context.getComponents();

		Map<String, RegulatoryNode> mapNameNode = new HashMap<String, RegulatoryNode>();
		for (RegulatoryNode node : listNodes)
			mapNameNode.put(node.getNodeInfo().getNodeID(), node);

		Set<String> invalidComponentNames = new HashSet<String>();
		traverseTreeSemanticVerification(invalidComponentNames, mapNameNode,
				expression);

		return invalidComponentNames;
	}

	private static void traverseTreeSemanticVerification(Set<String> invalid,
			Map<String, RegulatoryNode> mapNameNode,
			IntegrationExpression expression) {

		if (expression instanceof IntegrationOperation) {
			List<IntegrationExpression> listOperands = ((IntegrationOperation) expression)
					.getOperands();
			for (IntegrationExpression operand : listOperands)
				traverseTreeSemanticVerification(invalid, mapNameNode, operand);

		} else if (expression instanceof IntegrationAtom) {

			IntegrationAtom atom = (IntegrationAtom) expression;
			String componentName = atom.getComponentName();
			byte threshold = atom.getThreshold();
			int min = atom.getMinNeighbours();
			int max = atom.getMaxNeighbours();
			// TODO: deal with distance

			if (!mapNameNode.containsKey(componentName))
				invalid.add(componentName);
			else if (mapNameNode.get(componentName).isInput())
				invalid.add(componentName);
			else if (threshold > mapNameNode.get(componentName).getMaxValue())
				invalid.add(componentName);
			else if (min > max && max >= 0)
				invalid.add(componentName);

		}
	}

	public static boolean evaluate(
			Map<RegulatoryNode, List<Integer>> argumentValues,
			IntegrationExpression expression) {
		Map<String, RegulatoryNode> mapNameNode = new HashMap<String, RegulatoryNode>();
		for (RegulatoryNode node : argumentValues.keySet())
			mapNameNode.put(node.getNodeInfo().getNodeID(), node);

		return traverseTreeEvaluate(argumentValues, mapNameNode, expression);

	}

	private static boolean traverseTreeEvaluate(
			Map<RegulatoryNode, List<Integer>> argumentValues,
			Map<String, RegulatoryNode> mapNameNode,
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
					if (!traverseTreeEvaluate(argumentValues, mapNameNode,
							operand)) {
						result = false;
						break;
					}
				break;
			case OR:
				result = false;
				for (IntegrationExpression operand : listOperands)
					if (traverseTreeEvaluate(argumentValues, mapNameNode,
							operand)) {
						result = true;
						break;
					}
				break;
			}
		} else if (expression instanceof IntegrationNegation) {
			return !traverseTreeEvaluate(argumentValues, mapNameNode,
					((IntegrationNegation) expression).getNegatedExpression());

		} else if (expression instanceof IntegrationAtom) {
			IntegrationAtom atom = (IntegrationAtom) expression;
			String componentName = atom.getComponentName();

			RegulatoryNode node = mapNameNode.get(componentName);
			List<Integer> listValues = argumentValues.get(node);

			byte threshold = atom.getThreshold();
			if (threshold < 0)
				threshold = node.getMaxValue();

			int min = atom.getMinNeighbours();
			if (min < 0)
				min = listValues.size();

			int max = atom.getMaxNeighbours();
			if (max < 0)
				max = listValues.size();

			// TODO: we have to deal with distance too

			int habilitations = 0;

			for (Integer value : listValues) {
				if (value.intValue() >= threshold)
					habilitations++;
			}

			if (habilitations >= min && habilitations <= max)
				return true;
			else
				return false;

		}

		return result;
	}

	public IntegrationExpression parse(String specificationString)
			throws RecognitionException {
		ANTLRStringStream in = new ANTLRStringStream(specificationString);
		IntegrationGrammarLexer lexer = new IntegrationGrammarLexer(in);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		IntegrationGrammarParser parser = new IntegrationGrammarParser(tokens);
		return parser.eval();
	}
	
	
	// obtain CNF paths for integration expression as a List of int[]
	public static List<byte[]> getClauses(IntegrationExpression expression,
			int instance, CompositionContext context) {

		if (expression instanceof IntegrationOperation) {
			List<IntegrationExpression> listOperands = ((IntegrationOperation) expression)
					.getOperands();

			IntegrationLogicalOperator operator = ((IntegrationOperation) expression)
					.getOperation();

			switch (operator) {
			case AND: {
				List<byte[]> intermediate = new ArrayList<byte[]>();
				for (IntegrationExpression operand : listOperands)
					intermediate = conjunctionStates(intermediate,
							getClauses(operand, instance, context));

				return intermediate;
			}
			case OR: {
				List<byte[]> intermediate = new ArrayList<byte[]>();
				for (IntegrationExpression operand : listOperands)
					intermediate = disjunctionStates(intermediate,
							getClauses(operand, instance, context));

				return intermediate;
			}
			default:
				return null;
			
			}
		} else if (expression instanceof IntegrationNegation) {
			List<byte[]> toNegate = new ArrayList<byte[]>();
			toNegate = getClauses(
					((IntegrationNegation) expression).getNegatedExpression(),
					instance, context);

			List<byte[]> intermediate = new ArrayList<byte[]>();
			for (byte[] clause : toNegate) {
				List<byte[]> negation = negateState(clause, context);
				intermediate = conjunctionStates(intermediate, negation);
			}
			return intermediate;
			
		} else if (expression instanceof IntegrationAtom) {
			IntegrationAtom atom = (IntegrationAtom) expression;

			byte threshold = atom.getThreshold();
			int min = atom.getMinNeighbours();
			int max = atom.getMaxNeighbours();
			int distance = atom.getDistance();

			Set<Integer> neighbours = context.getNeighbourIndices(instance,
					distance);

			if (min == -1)
				min = neighbours.size();

			if (max == -1)
				max = neighbours.size();

			List<byte[]> result = new ArrayList<byte[]>();
			for (int v = min; v <= max; v++) {
				List<boolean[]> masks = generateNeighboursMask(v, neighbours);

				for (boolean[] mask : masks) {
					for (int i = 0; i < mask.length; i++) {
						List<byte[]> conjunction = new ArrayList<byte[]>();
						if (mask[i]) {
							NodeInfo node = context
									.getLowLevelComponentFromName(
											atom.getComponentName(), instance);
							List<byte[]> disjunction = new ArrayList<byte[]>();
							for (byte l = threshold; l <= node.getMax(); l++) {
								byte[] clause = new byte[context
										.getLowLevelComponents().size()];
								for (int j = 0; j < context
										.getLowLevelComponents().size(); j++) {
									if (j == context.getLowLevelComponents()
											.indexOf(node)) {
										clause[j] = l;
									} else {
										clause[j] = -1;
									}
								}
								disjunction.add(clause);
							}
							conjunction = conjunctionStates(conjunction,
									disjunction);
						}

						result = disjunctionStates(result, conjunction);

					}
				}

			}

			return result;

		} else {
			// should never be reached
			return null;
		}

	}

	private static List<byte[]> conjunctionStates(List<byte[]> clausesA,
			List<byte[]> clausesB) {
		List<byte[]> result = new ArrayList<byte[]>();

		if (clausesA == null || clausesB == null)
			return null;

		if (clausesA.isEmpty())
			return clausesB;

		if (clausesB.isEmpty())
			return clausesA;

		for (byte[] clauseA : clausesA)
			for (byte[] clauseB : clausesB) {
				int size = clauseA.length;
				byte[] clauseC = new byte[size];

				for (int i = 0; i < size; i++) {
					if (clauseA[i] == -1)
						clauseC[i] = clauseB[i];
					else if (clauseB[i] == -1)
						clauseC[i] = clauseA[i];
					else if (clauseB[i] == clauseA[i])
						clauseC[i] = clauseA[i];
					else
						continue;
				}

				result.add(clauseC);

			}

		if (result.isEmpty())
			return null;
		else
			return result;

	}

	private static List<byte[]> disjunctionStates(List<byte[]> clausesA,
			List<byte[]> clausesB) {
		List<byte[]> result = new ArrayList<byte[]>();

		if (clausesA == null)
			return clausesB;

		if (clausesB == null)
			return clausesA;

		for (byte[] clauseA : clausesA)
			result.add(clauseA);

		for (byte[] clauseB : clausesB)
			result.add(clauseB);

		return result;
	}

	private static List<byte[]> negateState(byte[] clause,
			CompositionContext context) {
		List<byte[]> negation = new ArrayList<byte[]>();

		for (int i = 0; i < clause.length; i++) {
			if (clause[i] != -1) {
				for (byte v = 0; v <= context.getLowLevelComponents().get(i)
						.getMax(); v++)
					if (v != clause[i]) {
						byte[] negatedClause = new byte[clause.length];
						for (int j = 0; j < clause.length; j++)
							negatedClause[j] = -1;
						negatedClause[i] = v;
						negation.add(negatedClause);
					}

			}

		}

		if (negation.isEmpty())
			return null;
		else
			return negation;

	}

	private static List<boolean[]> generateNeighboursMask(int v,
			Set<Integer> neighbours) {
		return generateNeighboursMask(v, neighbours, null);
	}

	private static List<boolean[]> generateNeighboursMask(int v,
			Set<Integer> neighbours, boolean[] frozen) {
		List<boolean[]> masks = new ArrayList<boolean[]>();

		if (v == 0) {
			if (frozen != null)
				masks.add(frozen);
		} else {
			for (Integer instance : neighbours) {
				if (frozen != null && frozen[instance.intValue()])
					continue;
				boolean[] mask = new boolean[neighbours.size()];
				for (int i = 0; i < mask.length; i++)
					if (i == instance.intValue() || frozen[i])
						mask[i] = true;
					else
						mask[i] = false;

				masks.addAll(generateNeighboursMask(v - 1, neighbours, mask));
			}
		}

		return masks;
	}

}
