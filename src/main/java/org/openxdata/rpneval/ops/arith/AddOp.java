package org.openxdata.rpneval.ops.arith;

public class AddOp extends ArithmeticOp {

	public String getName() {
		return "+";
	}

	Object eval(double op1, double op2) {
		return new Double(op1 + op2);
	}
}
