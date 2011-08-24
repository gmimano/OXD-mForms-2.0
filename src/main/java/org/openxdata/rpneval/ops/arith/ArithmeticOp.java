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

		Class widenedTo = widen(operands);

		if (widenedTo == Long.class) {
			Long op1 = (Long) operands[0], op2 = (Long) operands[1];
			return eval(op1.longValue(), op2.longValue());
		} else if (widenedTo == Double.class) {
			Double op1 = (Double) operands[0], op2 = (Double) operands[1];
			return eval(op1.doubleValue(), op2.doubleValue());
		} else
			throw new EvaluationException("widened to unsupported type: "
					+ widenedTo.toString());
	}

	Class widen(Object[] operands) throws EvaluationException {

		if (operands.length > 2)
			throw new EvaluationException("expecting <= 2 operands");

		// Convert strings to numeric types
		for (int i = 0; i < operands.length; i++)
			if (operands[i].getClass() == String.class) {
				String op = (String) operands[i];
				try {
					if (op.indexOf('.') < 0)
						operands[i] = new Long(op);
					else
						operands[i] = new Double(op);
				} catch (NumberFormatException nfe) {
					throw new EvaluationException(
							"failed to convert value to number: " + op);
				}
			}

		Class op1class = operands[0].getClass(), op2class = operands[1]
				.getClass();

		if (op1class != op2class) {
			if (op1class == Long.class && op2class == Double.class) {
				Long op1 = (Long) operands[0];
				operands[0] = new Double(op1.doubleValue());
			} else if (op1class == Double.class && op2class == Long.class) {
				Long op2 = (Long) operands[1];
				operands[1] = new Double(op2.doubleValue());
			} else
				throw new EvaluationException(
						"can't widen to compatible types: " + op1class
								+ " and " + op2class);
			return Double.class; // has to be double
		}

		return op1class;
	}

	abstract Object eval(double op1, double op2) throws EvaluationException;

	abstract Object eval(long op1, long op2) throws EvaluationException;

	public String toString() {
		return getName();
	}
}
