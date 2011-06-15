package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.db.util.PersistentHelper;


/**
 * Data collected for repeat sets of questions.
 * This represents question data (list of QuestionData objects) for a single repeating row.
 * 
 * @author daniel
 *
 */
public class RepeatQtnsData implements Persistent {

	/** A list of repeat question data (QuestionData). */
	private Vector questions;
	
	/** A reference to the Definition for repeat sets of questions we have. */
	private RepeatQtnsDef def;
	
	/** Unique identifier of repeat questions data. 
	 * This value does not have to be stored.
	 * Only used for identintification at runtime.
	 * */
	private short id;
	
	
	public RepeatQtnsData(){
		
	}
	
	/** Copy constructor. */
	public RepeatQtnsData(RepeatQtnsData data){
		setId(data.getId());
		def = data.getDef();
		
		questions = new Vector();
		
		Vector qtns = data.getQuestions();
		for(int i=0; i<qtns.size(); i++)
			questions.addElement(new QuestionData((QuestionData)qtns.elementAt(i)));		
	}

	public RepeatQtnsData(short id, RepeatQtnsDef def) {
		setId(id);
		setDef(def);
	}
	
	public RepeatQtnsData(short id, Vector questions, RepeatQtnsDef def) {
		setId(id);
		setQuestions(questions);
		setDef(def);
	}
	
	public short getId() {
		return id;
	}
	
	public void setId(short id) {
		this.id = id;
	}
	
	public Vector getQuestions() {
		return questions;
	}

	public void setQuestions(Vector questions) {
		this.questions = questions;
	}

	public RepeatQtnsDef getDef() {
		return def;
	}

	public void setDef(RepeatQtnsDef def) {
		this.def = def;
		updateQuestionData();
		
	}
	
	public int size(){
		if(getQuestions() == null)
			return 0;
		return getQuestions().size();
	}
	
	private void updateQuestionData(){
		if(questions == null || questions.size() == 0)
			createQuestionData();
		
		for(int j=0; j<questions.size(); j++){
			QuestionData qtnData = (QuestionData)questions.elementAt(j);
			QuestionDef qtnDef = def.getQuestion(qtnData.getId());
			qtnData.setDef(qtnDef);
			if(qtnData.getAnswer() != null && qtnDef.getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE)
				((OptionData)qtnData.getAnswer()).setDef((OptionDef)qtnDef.getOptions().elementAt(Integer.parseInt(qtnData.getOptionAnswerIndices().toString())));
			else if(qtnData.getAnswer() != null && qtnDef.getType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
				Vector answers = (Vector)qtnData.getAnswer();
				for(int k=0; k<answers.size(); k++){
					OptionData option = (OptionData)answers.elementAt(k);
					option.setDef((OptionDef)qtnDef.getOptions().elementAt(((Short)((Vector)qtnData.getOptionAnswerIndices()).elementAt(k)).shortValue()));
				}
			}
		}	
	}
	
	/** Creates question data from their corresponding definitions. */
	private void createQuestionData(){
		questions = new Vector();
		Vector qtns = def.getQuestions();

		if(qtns == null)
			return;
		
		for(int i=0; i<qtns.size(); i++)
			questions.addElement(new QuestionData((QuestionDef)qtns.elementAt(i)));
	}
	
	public QuestionData getQuestion(int index){
		return (QuestionData)questions.elementAt(index);
	}
	
	public QuestionData getQuestion(String variableName){
		for(int i=0; i<this.getDef().getQuestions().size(); i++){
			QuestionDef def = (QuestionDef)this.getDef().getQuestions().elementAt(i);
			
				if(def.getVariableName().equals(variableName))
					return getQuestionByDefId(def.getId());
		}
		
		return null;
	}
	
	public void setQuestionDataById(QuestionData questionData){
		QuestionData data;
		for(int i=0; i<questions.size(); i++){
			data = (QuestionData)questions.elementAt(i);
			if(data.getId() == questionData.getId()){
				questions.setElementAt(questionData, i);
				return;
			}
		}
	}
	
	public void addQuestion(QuestionData questionData){
		if(questions == null)
			questions = new Vector();
		questions.addElement(questionData);
	}
	
	public String toString() {
		String val = "";
		if(questions != null && questions.size() > 0){
			for(int i=0; i<questions.size(); i++){
				QuestionData data = (QuestionData)questions.elementAt(i);
				if(data.getTextAnswer() != null && data.getTextAnswer().length() > 0){
					if(val.trim().length() > 0)
						val += ",";
					val += data.getTextAnswer();
				}
			}
		}
		return val;
	}
	
	/**
	 * Checks if if a repeat questions row has been answered.
	 * 
	 * @return
	 */
	public boolean isAnswered(){
		for(int i=0; i<questions.size(); i++){
			if(((QuestionData)questions.elementAt(i)).isAnswered())
				return true; 
		}
		return false; //Not even one question answered.
	}
	
	/** Reads a page definition object from the supplied stream. */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setQuestions(PersistentHelper.readMedium(dis,QuestionData.class));
	}

	/** Write the page definition object to the supplied stream. */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.writeMedium(getQuestions(), dos);
	}

	public QuestionData getQuestionByDefId(short id) {
		QuestionData result = null;
		for(int i=0; i<questions.size(); i++){
			QuestionData qd = (QuestionData)questions.elementAt(i);
			if(qd.getDef().getId() == id){
				result = qd;
			}
		}
		
		return result;
	}
}
