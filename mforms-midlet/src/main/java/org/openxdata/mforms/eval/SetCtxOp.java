package org.openxdata.mforms.eval;

import java.util.Hashtable;
import java.util.Stack;

import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.NoResultException;
import org.openxdata.rpneval.Operator;

public class SetCtxOp implements Operator {

	public String getName() {
		return "sc";
	}

	public int getArity(Stack stack) throws EvaluationException {
		return 1;
	}

	public Object eval(Object[] operands, Hashtable env)
			throws EvaluationException {
		if (operands[0] == null || !(operands[0] instanceof String))
			throw new IllegalArgumentException("expects non-null String");
		env.put("context", operands[0]);
		throw new NoResultException();
	}

}
