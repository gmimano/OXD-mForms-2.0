package org.openxdata.mforms.eval;

import junit.framework.TestCase;

import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Evaluator;
import org.openxdata.rpneval.EvaluatorFactory;
import org.openxdata.rpneval.impl.DefaultEvaluator;

public class EvaluatorTestCase extends TestCase {

	public void testEvaluatorCreate() throws EvaluationException {
		Evaluator eval = EvaluatorFactory.getInstance();
		assertNotNull("evaluator should not be null", eval);
		assertTrue("evaluator should be default implementation",
				eval instanceof DefaultEvaluator);
		DefaultEvaluator defaultEval = (DefaultEvaluator) eval;
		assertTrue("arith ops should be present",
				defaultEval.getOperator("+") != null);
		assertTrue("boolean ops should be present",
				defaultEval.getOperator("not") != null);
		assertTrue("mforms specific ops should be present",
				defaultEval.getOperator("$") != null);
	}
}
