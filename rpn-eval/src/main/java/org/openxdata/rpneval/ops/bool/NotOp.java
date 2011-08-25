package org.openxdata.rpneval.ops.bool;

import java.util.Hashtable;
import java.util.Stack;

import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Operator;

public class NotOp implements Operator {

	public String getName() {
		return "not";
	}

	public int getArity(Stack stack) throws EvaluationException {
		return 1;
	}

	public Object eval(Object[] operands, Hashtable env)
			throws EvaluationException {

		if (operands[0] == null)
			throw new EvaluationException("not op expects non null operand");

		if (!(operands[0] instanceof Boolean))
			throw new EvaluationException("not op expects boolean operand");

		Boolean operand = (Boolean) operands[0];

		return new Boolean(!operand.booleanValue());
	}
}
