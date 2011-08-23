package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.db.util.PersistentHelper;


/**
 * A definition for skipping or branching rules. 
 * These could for example be enabling or disabling, hiding or showing, maing mandatory or optional 
 * of questions basing on values of others.
 * 
 * @author Daniel Kayiwa
 *
 */
public class SkipRule implements Persistent{

	/** The numeric identifier of a rule. This is assigned in code and hence
	 * is not known by the user.
	 */
	private short id = EpihandyConstants.NULL_ID;

	/** A list of conditions (Condition object) to be tested for a rule. 
	 * E.g. If sex is Male. If age is greatern than 4. etc
	 */
	private Vector conditions;

	/** The action taken when conditions are true.
	 * Example of actions are Disable, Hide, Show, etc
	 */
	private byte action = EpihandyConstants.ACTION_NONE;

	/** A list of question identifiers (bytes) acted upon when conditions for the rule are true. */
	private Vector actionTargets;


	/** Constructs a rule object ready to be initialized. */
	public SkipRule(){

	}

	/** Copy constructor. */
	public SkipRule(SkipRule skipRule){
		setId(skipRule.getId());
		setAction(skipRule.getAction());
		setConditionsOperator(skipRule.getConditionsOperator());
		copyConditions(skipRule.getConditions());
		copyActionTargets(skipRule.getActionTargets());
	}

	/** Construct a Rule object from parameters. 
	 * 
	 * @param ruleId 
	 * @param conditions 
	 * @param action
	 * @param actionTargets
	 */
	public SkipRule(short ruleId, Vector conditions, byte action, Vector actionTargets) {
		setId(ruleId);
		setConditions(conditions);
		setAction(action);
		setActionTargets(actionTargets);
	}

	public byte getAction() {
		return action;
	}

	public void setAction(byte action) {
		this.action = action;
	}

	public Vector getActionTargets() {
		return actionTargets;
	}

	public void setActionTargets(Vector actionTargets) {
		this.actionTargets = actionTargets;
	}

	public Vector getConditions() {
		return conditions;
	}

	public void setConditions(Vector conditions) {
		this.conditions = conditions;
	}

	public short getId() {
		return id;
	}

	public void setId(short id) {
		this.id = id;
	}

	//TODO This is very very wiered
	public short getConditionsOperator() {
		return id;
	}

	public void setConditionsOperator(short id) {
		this.id = id;
	}

	/** 
	 * Checks conditions of a rule and executes the corresponding actions
	 * 
	 * @param data
	 */
	public void fire(FormData data){
		boolean trueFound = false, falseFound = false;

		for(int i=0; i<getConditions().size(); i++){
			Condition condition = (Condition)this.getConditions().elementAt(i);
			if(condition.isTrue(data,false))
				trueFound = true;
			else
				falseFound = true;
		}

		if(getConditions().size() == 1 || getConditionsOperator() == EpihandyConstants.CONDITIONS_OPERATOR_AND)
			ExecuteAction(data,!falseFound);
		else if(getConditionsOperator() == EpihandyConstants.CONDITIONS_OPERATOR_OR)
			ExecuteAction(data,trueFound);
		//else do nothing
	}
	
	public void fire(RepeatQtnsData data){
		boolean trueFound = false, falseFound = false;

		for(int i=0; i<getConditions().size(); i++){
			Condition condition = (Condition)this.getConditions().elementAt(i);
			if(condition.isTrue(data,false))
				trueFound = true;
			else
				falseFound = true;
		}
		
		if(getConditions().size() == 1 || getConditionsOperator() == EpihandyConstants.CONDITIONS_OPERATOR_AND)
			ExecuteAction(data,!falseFound);
		else if(getConditionsOperator() == EpihandyConstants.CONDITIONS_OPERATOR_OR)
			ExecuteAction(data,trueFound);
	}

	private void ExecuteAction(RepeatQtnsData data, boolean conditionTrue) {
		Vector qtns = this.getActionTargets();
		for(int i=0; i<qtns.size(); i++){
			QuestionData qData = data.getQuestionByDefId(Short.parseShort(qtns.elementAt(i).toString()));
			if(qData != null)
				ExecuteAction(qData,conditionTrue);
		}
	}

	/** Executes the action of a rule for its conditition's true or false value. */
	public void ExecuteAction(FormData data,boolean conditionTrue){
		Vector qtns = this.getActionTargets();
		for(int i=0; i<qtns.size(); i++){
			QuestionData qData = data.getQuestion(Short.parseShort(qtns.elementAt(i).toString()));
			if(qData != null)
				ExecuteAction(qData,conditionTrue);
		}
	}
	
	/*public void ExecuteAction(QuestionDef def, boolean conditionTrue){
		def.setVisible(true);
		def.setEnabled(true);
		def.setMandatory(false);
		
		if((action & EpihandyConstants.ACTION_ENABLE) != 0)
			def.setEnabled(conditionTrue);
		else if((action & EpihandyConstants.ACTION_DISABLE) != 0)
			def.setEnabled(!conditionTrue);
		else if((action & EpihandyConstants.ACTION_SHOW) != 0)
			def.setVisible(conditionTrue);
		else if((action & EpihandyConstants.ACTION_HIDE) != 0)
			def.setVisible(!conditionTrue);
		
		if((action & EpihandyConstants.ACTION_MAKE_MANDATORY) != 0)
			def.setMandatory(conditionTrue);
	}*/

	/** Executes the rule action on the supplied question. */
	public void ExecuteAction(QuestionData data,boolean conditionTrue){
		
		QuestionDef qtn = data.getDef();
		
		qtn.setVisible(true);
		qtn.setEnabled(true);
		qtn.setMandatory(false);
		
		if((action & EpihandyConstants.ACTION_ENABLE) != 0)
			qtn.setEnabled(conditionTrue);
		else if((action & EpihandyConstants.ACTION_DISABLE) != 0)
			qtn.setEnabled(!conditionTrue);
		else if((action & EpihandyConstants.ACTION_SHOW) != 0)
			qtn.setVisible(conditionTrue);
		else if((action & EpihandyConstants.ACTION_HIDE) != 0)
			qtn.setVisible(!conditionTrue);
		
		if((action & EpihandyConstants.ACTION_MAKE_MANDATORY) != 0)
			qtn.setMandatory(conditionTrue);
		
		if(!qtn.isEnabled() || !qtn.isVisible())
			data.setAnswer(null);
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setId(dis.readShort());
		setAction(dis.readByte());
		setConditions(PersistentHelper.readMedium(dis,Condition.class));
		setActionTargets(PersistentHelper.readShorts(dis));
		setConditionsOperator(dis.readByte());

	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeShort(getId());
		dos.writeByte(getAction());
		PersistentHelper.writeMedium(getConditions(), dos);
		PersistentHelper.writeShorts(getActionTargets(), dos);
		dos.writeByte(getConditionsOperator());
	}

	private void copyConditions(Vector conditions){
		this.conditions = new Vector();
		for(int i=0; i<conditions.size(); i++)
			this.conditions.addElement(new Condition((Condition)conditions.elementAt(i)));
	}

	private void copyActionTargets(Vector actionTargets){
		this.actionTargets = new Vector();
		for(int i=0; i<actionTargets.size(); i++)
			this.actionTargets.addElement(new Short(((Short)actionTargets.elementAt(i)).shortValue()));
	}
}
