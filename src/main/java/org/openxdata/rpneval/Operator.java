package org.openxdata.rpneval;

import java.util.Hashtable;
import java.util.Stack;

@SuppressWarnings("rawtypes")
public interface Operator {

	String getName();

	int getArity(Stack stack) throws EvaluationException;

	Object eval(Object[] operands, Hashtable env) throws EvaluationException;

}
