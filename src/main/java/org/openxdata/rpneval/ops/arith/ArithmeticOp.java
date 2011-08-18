package org.openxdata.rpneval.ops.arith;

import java.util.Hashtable;
import java.util.Stack;

import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Operator;

@SuppressWarnings("rawtypes")
public abstract class ArithmeticOp implements Operator {

	@Override
	public int getArity(Stack stack) {
		return 2;
	}

	@Override
	public Object eval(Object[] operands, Hashtable env)
			throws EvaluationException {
		double op1 = coerceDouble(operands[0]), op2 = coerceDouble(operands[1]);
		return eval(op1, op2);
	}

	double coerceDouble(Object op) {
		if (op instanceof Number)
			return ((Number) op).doubleValue();
		else
			return Double.parseDouble(op.toString());
	}

	abstract Object eval(double op1, double op2) throws EvaluationException;

	@Override
	public String toString() {
		return getName();
	}
}
