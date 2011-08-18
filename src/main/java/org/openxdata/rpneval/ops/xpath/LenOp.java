package org.openxdata.rpneval.ops.xpath;

import java.util.Hashtable;
import java.util.Stack;

import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Operator;

@SuppressWarnings("rawtypes")
public class LenOp implements Operator {

	@Override
	public String getName() {
		return "len";
	}

	@Override
	public int getArity(Stack stack) throws EvaluationException {
		return 1;
	}

	@Override
	public Object eval(Object[] operands, Hashtable env)
			throws EvaluationException {
		String val = (String) env.get("xml");
		return val.length();
	}

}
