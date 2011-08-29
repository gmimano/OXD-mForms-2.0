package org.openxdata.rpneval;

import java.util.Hashtable;

public interface Evaluator {

	Object evaluate(Object[] expression) throws EvaluationException;

	void setOperators(Operator[] ops);

	void setEnvironment(Hashtable env);

}
