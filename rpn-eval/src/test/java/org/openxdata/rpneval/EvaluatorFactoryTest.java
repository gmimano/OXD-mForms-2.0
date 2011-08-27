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
		Evaluator eval = EvaluatorFactory
				.getInstance(EvaluatorFactory.DEFAULT_PROFILE);
		assertNotNull(eval);
		assertTrue(eval instanceof DefaultEvaluator);
		DefaultEvaluator defaultEval = (DefaultEvaluator) eval;
		assertNotNull(defaultEval.getOperator("+"));
	}

	public void testGetInstanceNonDefault() throws EvaluationException {
		Evaluator eval = EvaluatorFactory.getInstance("nondefault");
		assertNotNull(eval);
		assertTrue(eval instanceof NonDefaultEvaluator);
		NonDefaultEvaluator defaultEval = (NonDefaultEvaluator) eval;
		assertNotNull(defaultEval.getOperator("+"));
		assertNull(defaultEval.getOperator("len")); // shouldn't be present
	}
}
