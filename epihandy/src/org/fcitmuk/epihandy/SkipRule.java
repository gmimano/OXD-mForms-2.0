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
	private byte id = EpihandyConstants.NULL_ID;

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
	public SkipRule(byte ruleId, Vector conditions, byte action, Vector actionTargets /*, String name*/) {
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

	public byte getId() {
		return id;
	}

	public void setId(byte id) {
		this.id = id;
	}

	//TODO This is very very wiered
	public byte getConditionsOperator() {
		return id;
	}

	public void setConditionsOperator(byte id) {
		this.id = id;
	}

	/** 
	 * Checks conditions of a rule and executes the corresponding actions
	 * 
	 * @param data
	 */
	public void fire(FormData data){
		boolean trueFound = false, falseFound = false;

		for(byte i=0; i<getConditions().size(); i++){
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

	/** Executes the action of a rule for its conditition's true or false value. */
	public void ExecuteAction(FormData data,boolean conditionTrue){
		Vector qtns = this.getActionTargets();
		for(byte i=0; i<qtns.size(); i++)
			ExecuteAction(data.getQuestion(Byte.parseByte(qtns.elementAt(i).toString())),conditionTrue);
	}

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
		setId(dis.readByte());
		setAction(dis.readByte());
		setConditions(PersistentHelper.read(dis,Condition.class));
		setActionTargets(PersistentHelper.readBytes(dis));
		setConditionsOperator(dis.readByte());

	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getId());
		dos.writeByte(getAction());
		PersistentHelper.write(getConditions(), dos);
		PersistentHelper.writeBytes(getActionTargets(), dos);
		dos.writeByte(getConditionsOperator());
	}

	private void copyConditions(Vector conditions){
		this.conditions = new Vector();
		for(byte i=0; i<conditions.size(); i++)
			this.conditions.addElement(new Condition((Condition)conditions.elementAt(i)));
	}

	private void copyActionTargets(Vector actionTargets){
		this.actionTargets = new Vector();
		for(byte i=0; i<actionTargets.size(); i++)
			this.actionTargets.addElement(new Byte(((Byte)actionTargets.elementAt(i)).byteValue()));
	}
}
