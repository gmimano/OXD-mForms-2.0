package org.openxdata.rpneval.ops.arith;

import org.openxdata.rpneval.EvaluationException;

public class LessThanOp extends ArithmeticOp {

	public String getName() {
		return "<";
	}

	Object eval(double op1, double op2) throws EvaluationException {
		return new Boolean(op1 < op2);
	}

	Object eval(long op1, long op2) throws EvaluationException {
		return new Boolean(op1 < op2);
	}

}
