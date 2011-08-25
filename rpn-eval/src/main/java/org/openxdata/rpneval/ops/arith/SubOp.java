package org.openxdata.rpneval.ops.arith;

import org.openxdata.rpneval.EvaluationException;

public class SubOp extends ArithmeticOp {

	public String getName() {
		return "-";
	}

	Object eval(double op1, double op2) {
		return new Double(op1 - op2);
	}

	Object eval(long op1, long op2) throws EvaluationException {
		return new Long(op1 - op2);
	}

}
