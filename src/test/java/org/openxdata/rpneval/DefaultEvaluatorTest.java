package org.openxdata.rpneval;

import static org.junit.Assert.assertEquals;

import java.util.Hashtable;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openxdata.rpneval.impl.DefaultEvaluator;
import org.openxdata.rpneval.ops.arith.AddOp;
import org.openxdata.rpneval.ops.arith.DivOp;
import org.openxdata.rpneval.ops.arith.MulOp;
import org.openxdata.rpneval.ops.arith.SubOp;
import org.openxdata.rpneval.ops.xpath.LenOp;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DefaultEvaluatorTest {

	DefaultEvaluator eval = new DefaultEvaluator();

	@Before
	public void setupOperators() {
		Hashtable opMap = new Hashtable();
		Operator[] ops = { new AddOp(), new SubOp(), new MulOp(), new DivOp(), new LenOp() };
		for (int i = 0; i < ops.length; i++) {
			Operator op = ops[i];
			opMap.put(op.getName(), op);
		}
		eval.setOperators(opMap);
	}

	@Test
	public void testArithEvaluation() throws EvaluationException {
		String[] expression = StringUtils.split("3 2 / 15 3 - * 1 +");
		Object result = eval.evaluate(expression);
		assertEquals("evaluation incorrect", 19.0, result);
	}

	@Test
	public void testSimpleXPathEvaluation() throws EvaluationException {
		String[] expression = StringUtils.split("/form/instance/element len");
		Hashtable env = new Hashtable<String, Object>();
		env.put("xml", "how are you doing?");
		eval.setEnvironment(env);
		Object result = eval.evaluate(expression);
		assertEquals("length didn't match", 18, result);
	}
}
