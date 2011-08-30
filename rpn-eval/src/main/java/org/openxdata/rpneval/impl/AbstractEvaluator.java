package org.openxdata.rpneval.impl;

import java.util.Hashtable;
import java.util.Stack;

import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Evaluator;
import org.openxdata.rpneval.NoResultException;
import org.openxdata.rpneval.Operator;

public abstract class AbstractEvaluator implements Evaluator {

	public Object evaluate(Object[] expression) throws EvaluationException {

		Stack evalStack = new Stack(); // Ensure we're working clean

		setStack(evalStack); // So downstream objects can access

		for (int i = 0; i < expression.length; i++) {
			Object term = expression[i];
			if (isOperator(term)) {
				Operator operator = getOperator(term);
				try {
					Object result = evaluateOp(operator);
					evalStack.push(result);
				} catch (NoResultException e) {
					// operator doesn't produce a value, so don't alter stack
				}
			} else
				evalStack.push(term);
		}

		// Throw an exception if there isn't a single result
		int finalStackSize = evalStack.size();
		if (finalStackSize != 1)
			throw new EvaluationException(
					"expected one and only one result, instead got "
							+ finalStackSize);

		// Return the final result
		Object finalResult = evalStack.pop();
		return finalResult;
	}

	public abstract Stack getStack();

	public abstract void setStack(Stack stack);

	public abstract Hashtable getEnvironment();

	public abstract boolean isOperator(Object term);

	public abstract Operator getOperator(Object term);

	public Object evaluateOp(Operator op) throws EvaluationException {

		Stack evalStack = getStack();
		Hashtable env = getEnvironment();

		int arity = op.getArity(evalStack);
		// using stack, operands are in lifo order
		Object[] operands = new Object[arity];
		for (int i = arity; i >= 1; i--) {
			operands[i - 1] = evalStack.pop();
		}

		Object result = op.eval(operands, env);
		return result;
	}
}
