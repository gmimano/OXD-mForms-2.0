package org.openxdata.rpneval.ops.bool;

import java.util.Hashtable;
import java.util.Stack;

import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Operator;

public abstract class BooleanOp implements Operator {

	public int getArity(Stack stack) throws EvaluationException {
		return 2;
	}

	public Object eval(Object[] operands, Hashtable env)
			throws EvaluationException {

		if (operands[0] == null || operands[1] == null)
			throw new EvaluationException("and op expects non-null operands");

		if (!(operands[0] instanceof Boolean)
				|| !(operands[1] instanceof Boolean))
			throw new EvaluationException("and op expects boolean operands");

		Boolean op1 = (Boolean) operands[0], op2 = (Boolean) operands[1];

		return eval(op1.booleanValue(), op2.booleanValue());
	}

	public abstract Object eval(boolean op1, boolean op2)
			throws EvaluationException;

}
