package org.openxdata.rpneval;

public interface Evaluator {

	Object evaluate(Object[] expression) throws EvaluationException;

	void setOperators(Operator[] ops);

}
