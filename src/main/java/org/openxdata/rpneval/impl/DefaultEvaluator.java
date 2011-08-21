package org.openxdata.rpneval.impl;

import java.util.Hashtable;
import java.util.Stack;

import org.openxdata.rpneval.Operator;

public class DefaultEvaluator extends AbstractEvaluator {

	private Stack evalStack;
	private Hashtable operators;
	private Hashtable environment;

	public DefaultEvaluator() {
		evalStack = new Stack();
		operators = new Hashtable();
	}

	public Stack getStack() {
		return evalStack;
	}

	public void setStack(Stack stack) {
		this.evalStack = stack;
	}

	public boolean isOperator(Object term) {
		return operators.containsKey(term);
	}

	public Operator getOperator(Object term) {
		return (Operator) operators.get(term);
	}

	public void setOperators(Hashtable opMap) {
		operators = opMap;
	}

	public void setEnvironment(Hashtable table) {
		environment = table;
	}

	public Hashtable getEnvironment() {
		return environment;
	}
}
