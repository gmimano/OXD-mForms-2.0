package org.openxdata.rpneval;

import org.openxdata.rpneval.impl.DefaultEvaluator;

import junit.framework.TestCase;

public class EvaluatorFactoryTest extends TestCase {

	public void testGetInstance() throws EvaluationException {
		Evaluator eval = EvaluatorFactory.getInstance();
		assertNotNull(eval);
	}

	public void testGetInstanceNull() throws EvaluationException {
		Evaluator eval = EvaluatorFactory.getInstance(null);
		assertNotNull(eval);
	}

	public void testGetInstanceDefault() throws EvaluationException {
		Evaluator eval = EvaluatorFactory.getInstance("default");
		assertNotNull(eval);
		assertTrue(eval instanceof DefaultEvaluator);
		DefaultEvaluator defaultEval = (DefaultEvaluator) eval;
		assertNotNull(defaultEval.getOperator("+"));
	}

	public void testGetInstanceOverriddenDefault() throws EvaluationException {
		Evaluator eval = EvaluatorFactory.getInstance();
		assertNotNull(eval);
		assertTrue(eval instanceof NonDefaultEvaluator);
		NonDefaultEvaluator overriddenEval = (NonDefaultEvaluator) eval;
		assertNotNull(overriddenEval.getOperator("+"));
		assertNull(overriddenEval.getOperator("len")); // shouldn't be present
	}

	public void testGetInstanceOverridenNamedProfile()
			throws EvaluationException {
		Evaluator eval = EvaluatorFactory.getInstance("arithmetic");
		assertNotNull(eval);
		assertTrue(eval instanceof NonDefaultEvaluator);
		NonDefaultEvaluator nonDefaultEval = (NonDefaultEvaluator) eval;
		assertNotNull(nonDefaultEval.getOperator("+"));
		assertNull(nonDefaultEval.getOperator("or")); // shouldn't be present
	}
}
