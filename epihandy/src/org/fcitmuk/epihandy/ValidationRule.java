package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.db.util.PersistentHelper;

/**
 * Does data validations eg value should be in range (1,90) etc
 * 
 * @author daniel
 *
 */
public class ValidationRule implements Persistent {
	
	/** The unique identifier of the question referenced by this condition. */
	private short questionId;
	
	/** A list of conditions (Condition object) to be tested for a rule. 
	 * E.g. age is greater than 4. etc
	 */
	private Vector conditions;
	
	
	/** The validation rule name. */
	private String errorMessage;
	
	/** Operator for combining more than one condition. (And, Or) only these two for now. */
	private int conditionsOperator = EpihandyConstants.CONDITIONS_OPERATOR_NULL;
	
	private FormData formData;
	
	
	public ValidationRule(){
		
	}
	
	/** Copy constructor. */
	public ValidationRule(ValidationRule validationRule){
		setQuestionId(validationRule.getQuestionId());
		setErrorMessage(validationRule.getErrorMessage());
		setConditionsOperator(validationRule.getConditionsOperator());
		copyConditions(validationRule.getConditions());
	}
	
	/** Construct a Rule object from parameters. 
	 * 
	 * @param ruleId 
	 * @param conditions 
	 * @param action
	 * @param actionTargets
	 */
	public ValidationRule(short questionId, Vector conditions , String errorMessage) {
		setQuestionId(questionId);
		setConditions(conditions);
		setErrorMessage(errorMessage);
	}

	public Vector getConditions() {
		return conditions;
	}

	public void setConditions(Vector conditions) {
		this.conditions = conditions;
	}

	public short getQuestionId() {
		return questionId;
	}

	public void setQuestionId(short questionId) {
		this.questionId = questionId;
	}
	
	public int getConditionsOperator() {
		return conditionsOperator;
	}

	public void setConditionsOperator(int conditionsOperator) {
		this.conditionsOperator = conditionsOperator;
	}
	
	public Condition getConditionAt(int index) {
		if(conditions == null)
			return null;
		return (Condition)conditions.elementAt(index);
	}
	
	public int getConditionCount() {
		if(conditions == null)
			return 0;
		return conditions.size();
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void addCondition(Condition condition){
		if(conditions == null)
			conditions = new Vector();
		conditions.addElement(condition);
	}
	
	public boolean containsCondition(Condition condition){
		if(conditions == null)
			return false;
		return conditions.contains(condition);
	}
	
	/** 
	 * Checks conditions of a rule and executes the corresponding actions
	 * 
	 * @param data
	 */
	public boolean isValid(){
		boolean trueFound = false, falseFound = false;
		
		for(int i=0; i<getConditions().size(); i++){
			Condition condition = (Condition)this.getConditions().elementAt(i);
			if(condition.isTrue(formData,true))
				trueFound = true;
			else
				falseFound = true;
		}
		
		if(getConditions().size() == 1 || getConditionsOperator() == EpihandyConstants.CONDITIONS_OPERATOR_AND)
			return !falseFound;
		else if(getConditionsOperator() == EpihandyConstants.CONDITIONS_OPERATOR_OR)
			return trueFound;
		
		return false;
	}
	
	private void copyConditions(Vector conditions){
		this.conditions = new Vector();
		for(int i=0; i<conditions.size(); i++)
			this.conditions.addElement(new Condition((Condition)conditions.elementAt(i)));
	}
	
	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setQuestionId(dis.readShort());
		setConditions(PersistentHelper.readMedium(dis,Condition.class));
		setErrorMessage(dis.readUTF().intern());
		setConditionsOperator(dis.readByte());

	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeShort(getQuestionId());
		PersistentHelper.writeMedium(getConditions(), dos);
		dos.writeUTF(getErrorMessage());
		dos.writeByte(getConditionsOperator());
	}

	public FormData getFormData() {
		return formData;
	}

	public void setFormData(FormData formData) {
		this.formData = formData;
	}
}
