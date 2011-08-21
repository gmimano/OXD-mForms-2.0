package org.openxdata.rpneval.ops.arith;

import java.util.Hashtable;
import java.util.Stack;

import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Operator;

public abstract class ArithmeticOp implements Operator {

	public int getArity(Stack stack) {
		return 2;
	}

	public Object eval(Object[] operands, Hashtable env)
			throws EvaluationException {
		double op1 = coerceDouble(operands[0]), op2 = coerceDouble(operands[1]);
		return eval(op1, op2);
	}

	double coerceDouble(Object op) {
		if (op instanceof Double)
			return ((Double) op).doubleValue();
		if (op instanceof Integer)
			return ((Integer) op).doubleValue();
		return Double.parseDouble(op.toString());
	}

	abstract Object eval(double op1, double op2) throws EvaluationException;

	public String toString() {
		return getName();
	}
}
