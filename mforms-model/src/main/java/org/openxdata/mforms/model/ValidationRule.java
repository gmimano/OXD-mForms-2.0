package org.openxdata.mforms.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.openxdata.mforms.persistent.Persistent;
import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Evaluator;
import org.openxdata.rpneval.EvaluatorFactory;

/**
 * Does data validations eg value should be in range (1,90) etc
 * 
 * @author daniel
 * 
 */
public class ValidationRule implements Persistent {

	/** The unique identifier of the question referenced by this condition. */
	private short questionId;

	private String[] expression;

	private String errorMessage;

	private FormData formData;

	public ValidationRule() {

	}

	/** Copy constructor. */
	public ValidationRule(ValidationRule validationRule) {
		setQuestionId(validationRule.getQuestionId());
		setExpression(validationRule.getExpression());
		setErrorMessage(validationRule.getErrorMessage());
	}

	/**
	 * Construct a Rule object from parameters.
	 * 
	 * @param ruleId
	 * @param conditions
	 * @param action
	 * @param actionTargets
	 */
	public ValidationRule(short questionId, String[] expression,
			String errorMessage) {
		setQuestionId(questionId);
		setExpression(expression);
		setErrorMessage(errorMessage);
	}

	public short getQuestionId() {
		return questionId;
	}

	public void setQuestionId(short questionId) {
		this.questionId = questionId;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setExpression(String[] expression) {
		this.expression = expression;
	}

	public String[] getExpression() {
		return expression;
	}

	/**
	 * Checks conditions of a rule and executes the corresponding actions
	 * 
	 * @param data
	 * @throws EvaluationException
	 */
	public boolean isValid() throws EvaluationException {

		if (expression == null)
			return true;

		Evaluator eval = EvaluatorFactory.getInstance();
		Hashtable env = new Hashtable();
		env.put("data", formData);
		eval.setEnvironment(env);
		Object result = eval.evaluate(expression);
		return ((Boolean) result).booleanValue();
	}

	/**
	 * @see org.openxdata.mforms.persistent.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		setQuestionId(dis.readShort());
		int exprLen = dis.readShort();
		String[] expr = new String[exprLen];
		for (int i = 0; i < exprLen; i++)
			expr[i] = dis.readUTF().intern();
		setExpression(expr);
		setErrorMessage(dis.readUTF().intern());
	}

	/**
	 * @see org.openxdata.mforms.persistent.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeShort(getQuestionId());
		dos.writeShort(expression.length);
		for (int i = 0; i < expression.length; i++)
			dos.writeUTF(expression[i]);
		dos.writeUTF(getErrorMessage());
	}

	public FormData getFormData() {
		return formData;
	}

	public void setFormData(FormData formData) {
		this.formData = formData;
	}
}
