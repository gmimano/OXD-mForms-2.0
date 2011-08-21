package org.openxdata.rpneval;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.openxdata.rpneval.impl.DefaultEvaluator;
import org.openxdata.rpneval.ops.arith.AddOp;
import org.openxdata.rpneval.ops.arith.DivOp;
import org.openxdata.rpneval.ops.arith.MulOp;
import org.openxdata.rpneval.ops.arith.SubOp;
import org.openxdata.rpneval.ops.xpath.LenOp;

public class DefaultEvaluatorTest extends TestCase {

	DefaultEvaluator eval = new DefaultEvaluator();

	public void setUp() {
		Hashtable opMap = new Hashtable();
		Operator[] ops = { new AddOp(), new SubOp(), new MulOp(), new DivOp(),
				new LenOp() };
		for (int i = 0; i < ops.length; i++) {
			Operator op = ops[i];
			opMap.put(op.getName(), op);
		}
		eval.setOperators(opMap);
	}

	public void testArithEvaluation() throws EvaluationException {
		String[] expression = StringUtils.split("3 2 / 15 3 - * 1 +");
		Object result = eval.evaluate(expression);
		assertEquals("evaluation incorrect", new Double(19.0), result);
	}

	public void testSimpleXPathEvaluation() throws EvaluationException {
		String[] expression = StringUtils.split("/form/instance/element len");
		Hashtable env = new Hashtable();
		env.put("xml", "how are you doing?");
		eval.setEnvironment(env);
		Object result = eval.evaluate(expression);
		assertEquals("length didn't match", new Double(18), result);
	}
}
