package org.openxdata.rpneval;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.openxdata.rpneval.helpers.EnvironmentAwareOp;
import org.openxdata.rpneval.helpers.NoOp;
import org.openxdata.rpneval.impl.DefaultEvaluator;
import org.openxdata.rpneval.ops.arith.AddOp;
import org.openxdata.rpneval.ops.arith.DivOp;
import org.openxdata.rpneval.ops.arith.GreaterThanOp;
import org.openxdata.rpneval.ops.arith.GreaterThanOrEqualOp;
import org.openxdata.rpneval.ops.arith.LessThanOp;
import org.openxdata.rpneval.ops.arith.LessThanOrEqualOp;
import org.openxdata.rpneval.ops.arith.MulOp;
import org.openxdata.rpneval.ops.arith.SubOp;
import org.openxdata.rpneval.ops.bool.AndOp;
import org.openxdata.rpneval.ops.bool.NotOp;
import org.openxdata.rpneval.ops.bool.OrOp;
import org.openxdata.util.StringUtils;

public class DefaultEvaluatorTest extends TestCase {

	DefaultEvaluator eval = new DefaultEvaluator();

	public void setUp() {
		Hashtable opMap = new Hashtable();
		Operator[] ops = { new AddOp(), new SubOp(), new MulOp(), new DivOp(),
				new LessThanOp(), new GreaterThanOp(), new AndOp(), new OrOp(),
				new GreaterThanOrEqualOp(), new LessThanOrEqualOp(),
				new NotOp(), new EnvironmentAwareOp(), new NoOp() };
		for (int i = 0; i < ops.length; i++) {
			Operator op = ops[i];
			opMap.put(op.getName(), op);
		}
		eval.setOperators(opMap);
	}

	public void testArithEvaluation() throws EvaluationException {
		String[] expression = StringUtils.split("3 2 / 15 3 - * 1 +");
		Object result = eval.evaluate(expression);
		assertEquals("evaluation incorrect", new Long(13), result);

		expression = StringUtils.split("3.0 2 / 15 3 - * 1 +");
		result = eval.evaluate(expression);
		assertEquals("evaluation incorrect", new Double(19), result);
	}

	public void testEnvironmentEvaluation() throws EvaluationException {
		String[] expression = StringUtils.split("envop");
		Hashtable env = new Hashtable();
		env.put("key", "value");
		eval.setEnvironment(env);
		Object result = eval.evaluate(expression);
		assertEquals("value", result);
	}

	public void testBooleanEvaluation() throws EvaluationException {
		String[] expression = StringUtils
				.split("1 1 + 1 2 + < 1 2 + 2 0 + > and");
		Object result = eval.evaluate(expression);
		assertEquals("result should be true", Boolean.TRUE, result);

		expression = StringUtils.split("2 1 <= 3 3 <= or");
		result = eval.evaluate(expression);
		assertEquals("result should be true", Boolean.TRUE, result);

		expression = StringUtils.split("2 3 >= 3 2 > and");
		result = eval.evaluate(expression);
		assertEquals("result should be false", Boolean.FALSE, result);

		expression = StringUtils.split("2 3 >= 3 2 > and not");
		result = eval.evaluate(expression);
		assertEquals("result should be true", Boolean.TRUE, result);
	}

	public void testNoResultEvaluation() throws EvaluationException {
		String[] expression = StringUtils
				.split("1 1 + noop 1 2 + < 1 2 + noop 2 0 + > and noop");
		Object result = eval.evaluate(expression);
		assertEquals("result should be true", Boolean.TRUE, result);
	}
}
