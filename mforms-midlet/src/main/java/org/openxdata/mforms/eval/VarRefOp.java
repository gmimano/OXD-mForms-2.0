package org.openxdata.mforms.eval;

import java.util.Hashtable;
import java.util.Stack;

import org.openxdata.mforms.model.FormData;
import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Operator;

public class VarRefOp implements Operator {

	public String getName() {
		return "$";
	}

	public int getArity(Stack stack) throws EvaluationException {
		return 1;
	}

	public Object eval(Object[] operands, Hashtable env)
			throws EvaluationException {

		if (env == null || env.get("data") == null)
			throw new IllegalStateException(
					"environment, with bound variable 'data' is required");
		FormData formData = (FormData) env.get("data");
		String context = (String) env.get("context");

		String varName = (String) operands[0];

		if (".".equals(varName))
			varName = context;

		return formData.getAnswer(varName);
	}

}
