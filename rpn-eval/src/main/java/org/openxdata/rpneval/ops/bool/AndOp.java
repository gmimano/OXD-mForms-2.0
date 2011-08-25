package org.openxdata.rpneval.ops.bool;

import org.openxdata.rpneval.EvaluationException;

public class AndOp extends BooleanOp {

	public String getName() {
		return "and";
	}

	public Object eval(boolean op1, boolean op2) throws EvaluationException {
		return new Boolean(op1 && op2);
	}
}
