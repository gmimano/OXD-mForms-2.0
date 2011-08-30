package org.openxdata.rpneval.helpers;

import java.util.Hashtable;
import java.util.Stack;

import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.NoResultException;
import org.openxdata.rpneval.Operator;

public class NoOp implements Operator {

	public String getName() {
		return "noop";
	}

	public int getArity(Stack stack) throws EvaluationException {
		return 0;
	}

	public Object eval(Object[] operands, Hashtable env)
			throws EvaluationException {
		throw new NoResultException();
	}

}
