package org.openxdata.rpneval.ops.arith;

public class SubOp extends ArithmeticOp {

	@Override
	public String getName() {
		return "-";
	}

	@Override
	Object eval(double op1, double op2) {
		return op1 - op2;
	}

}
