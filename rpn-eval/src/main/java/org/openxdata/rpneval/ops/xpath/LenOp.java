package org.openxdata.rpneval.ops.xpath;

import java.util.Hashtable;
import java.util.Stack;

import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Operator;

public class LenOp implements Operator {

	public String getName() {
		return "len";
	}

	public int getArity(Stack stack) throws EvaluationException {
		return 1;
	}

	public Object eval(Object[] operands, Hashtable env)
			throws EvaluationException {
		String val = (String) env.get("xml");
		return new Double(val.length());
	}

}
