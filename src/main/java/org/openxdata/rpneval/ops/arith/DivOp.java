package org.openxdata.rpneval.ops.arith;

import org.openxdata.rpneval.EvaluationException;

public class DivOp extends ArithmeticOp {

	public String getName() {
		return "/";
	}

	Object eval(double op1, double op2) throws EvaluationException {
		if (op2 == 0.0d || op2 == Double.NaN)
			throw new EvaluationException("result not defined: " + op1 + "/"
					+ op2);
		return new Double(op1 / op2);
	}
}
