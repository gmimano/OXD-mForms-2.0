package org.openxdata.mforms.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.openxdata.mforms.persistent.Persistent;
import org.openxdata.mforms.persistent.PersistentHelper;
import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Evaluator;
import org.openxdata.rpneval.EvaluatorFactory;


/**
 * A definition for skipping or branching rules. 
 * These could for example be enabling or disabling, hiding or showing, maing mandatory or optional 
 * of questions basing on values of others.
 * 
 * @author Daniel Kayiwa
 *
 */
public class SkipRule implements Persistent{

	private String [] expression;
	
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
		setExpression(getExpression());
		setAction(skipRule.getAction());
		copyActionTargets(skipRule.getActionTargets());
	}
	
	public SkipRule(String [] expression, byte action, Vector actionTargets) {
		setExpression(expression);
		setAction(action);
		setActionTargets(actionTargets);
	}

	public void setExpression(String[] expression) {
		this.expression = expression;
	}

	public String[] getExpression() {
		return expression;
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
	
	/**
	 * Checks conditions of a rule and executes the corresponding actions
	 * 
	 * @param data
	 * @throws EvaluationException
	 */
	public void fire(FormData data) throws EvaluationException {
		Object result = evaluate(data);
		executeAction(data, ((Boolean) result).booleanValue());
	}

	private Object evaluate(FormData data) throws EvaluationException {
		Evaluator eval = EvaluatorFactory.getInstance();
		Hashtable env = new Hashtable();
		env.put("data", data);
		Object result = eval.evaluate(expression);
		return result;
	}

	/** Executes the action of a rule for its conditition's true or false value. */
	public void executeAction(FormData data,boolean conditionTrue){
		Vector qtns = this.getActionTargets();
		for(int i=0; i<qtns.size(); i++){
			QuestionData qData = data.getQuestion(Short.parseShort(qtns.elementAt(i).toString()));
			if(qData != null)
				executeAction(qData,conditionTrue);
		}
	}

	/** Executes the rule action on the supplied question. */
	public void executeAction(QuestionData data,boolean conditionTrue){
		
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
	 * @see org.openxdata.mforms.persistent.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		String[] expression = PersistentHelper.readStrings(dis);
		setExpression(expression);
		setAction(dis.readByte());
		setActionTargets(PersistentHelper.readShorts(dis));
	}

	/**
	 * @see org.openxdata.mforms.persistent.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		String[] expression = getExpression();
		PersistentHelper.writeStrings(expression, dos);
		dos.writeByte(getAction());
		PersistentHelper.writeShorts(getActionTargets(), dos);
	}

	private void copyActionTargets(Vector actionTargets){
		this.actionTargets = new Vector();
		for(int i=0; i<actionTargets.size(); i++)
			this.actionTargets.addElement(new Short(((Short)actionTargets.elementAt(i)).shortValue()));
	}
}
