package org.fcitmuk.epihandy.midp.forms;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import org.fcitmuk.epihandy.FormData;
import org.fcitmuk.epihandy.QuestionData;
import org.fcitmuk.epihandy.RepeatQtnsData;
import org.fcitmuk.epihandy.RepeatQtnsDataList;
import org.fcitmuk.epihandy.RepeatQtnsDef;
import org.fcitmuk.epihandy.SkipRule;
import org.fcitmuk.epihandy.ValidationRule;
import org.fcitmuk.midp.mvc.AbstractView;
import org.fcitmuk.midp.mvc.CommandAction;
import org.fcitmuk.midp.mvc.Controller;
import org.fcitmuk.midp.mvc.View;
import org.fcitmuk.util.DefaultCommands;


/**
 * This serves as the controller for repeat questions.
 * 
 * @author daniel
 *
 */
public class RepeatTypeEditor extends AbstractView implements TypeEditor, TypeEditorListener , Controller{
	
	private QuestionData questionData;
	private RepeatQtnsDataList rptQtnsDataList;
	private RepeatQtnsDef rptQtnsDef;
	private RepeatQtnsData rptQtnsData; //the current one.
	private int pos; //question position pn the form.
	private int count; //total number of questions on the form.
	
	private RptQtnsDataListView dataListView = new RptQtnsDataListView();
	private RptQtnsDataView dataView = new RptQtnsDataView();
	
	private TypeEditor typeEditor = new DefaultTypeEditor();
	private ValidationRule validationRule;
	private FormData currentFormData;
	
	/**
	 * gets the current FormData
	 * @return
	 */
	public FormData getCurrentFormData() {
		return currentFormData;
	}

	/**
	 * sets the current form data for the repeat type editor
	 * @param currentFormData
	 */
	public void setCurrentFormData(FormData currentFormData) {
		this.currentFormData = currentFormData;
	}

	public void startEdit(QuestionData data, ValidationRule validationRule, boolean singleQtnEdit,int pos, int count, TypeEditorListener listener){
		questionData = data;
		this.validationRule = validationRule;
		this.pos = pos;
		this.count = count;
		
		rptQtnsDef = questionData.getDef().getRepeatQtnsDef();
		
		if(questionData.getAnswer() != null)
			rptQtnsDataList = new RepeatQtnsDataList((RepeatQtnsDataList)questionData.getAnswer());
		else{
			rptQtnsDataList = new RepeatQtnsDataList();
			questionData.setAnswer(rptQtnsDataList);
		}

		showQtnsData(validationRule);
		
		typeEditor.setController(this);
	}
	
	private void showQtnsData(ValidationRule validationRule){
		dataListView.showQtnDataList(rptQtnsDef,rptQtnsDataList,this,validationRule);
	}
	
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == DefaultCommands.cmdOk || c == DefaultCommands.cmdNext)
				getEpihandyController().endEdit(true, questionData, c);
			else if(c == DefaultCommands.cmdCancel || c == DefaultCommands.cmdPrev)
				getEpihandyController().endEdit(false, questionData, c);
			else
				getEpihandyController().endEdit(false, questionData, c);
		}
		catch(Exception e){
			// TODO: Handle this gracefully.
		}
	}
	
	private EpihandyController getEpihandyController(){
		return (EpihandyController)controller;
	}
	
	public void endEdit(boolean save, QuestionData data, Command cmd){
		rptQtnsData.setQuestionDataById(data);		
		
		//fire skip rules here to enable the repeat skip rules
		/*if(this.getCurrentFormData() != null)
			this.getEpihandyController().FireSkipRules(getCurrentFormData());*/
		fireRepeatSkipRules();
		
		dataView.showQtnData(rptQtnsData, this);
	}
	
	private void fireRepeatSkipRules(){
		Vector rules = this.currentFormData.getDef().getSkipRules();
		
		if(rules != null && rules.size() > 0){
			for(int i = 0; i < rules.size(); i++){
				SkipRule rule = (SkipRule)rules.elementAt(i);				
				boolean repeatSkipRule = isRepeatSkipRule(rule);
				if(repeatSkipRule)
					rule.fire(this.rptQtnsData);
			}
		}
	}
	
	private boolean isRepeatSkipRule(SkipRule rule){
		Vector actionTargets = rule.getActionTargets();
		if(actionTargets != null){
			for(int index = 0; index < actionTargets.size(); index++){
				short qtnId = Short.parseShort(actionTargets.elementAt(index).toString());
				QuestionData rQtnData = this.rptQtnsData.getQuestionByDefId(qtnId);
				if(rQtnData != null)
					return true;
			}
		}
		
		return false;
	}
	
	public void execute(View view, Object commandAction, Object data){
		
		if(view == dataListView){
			if(commandAction == CommandAction.NEW){
				rptQtnsData = new RepeatQtnsData((short)(rptQtnsDataList.size()+1),rptQtnsDef);
				fireRepeatSkipRules();
				dataView.showQtnData(rptQtnsData, this);
			}
			else if(commandAction == CommandAction.EDIT){
				rptQtnsData = new RepeatQtnsData((RepeatQtnsData)data);
				fireRepeatSkipRules();
				dataView.showQtnData(rptQtnsData, this);
			}
			else if(commandAction == CommandAction.OK){
				questionData.setAnswer(rptQtnsDataList);
				getEpihandyController().endEdit(false, questionData, null);
			}
			else if(commandAction == CommandAction.CANCEL)
				getEpihandyController().endEdit(false, null, null);
		}
		else if(view == dataView){
			if(commandAction == CommandAction.EDIT)
				typeEditor.startEdit(new QuestionData((QuestionData)data),null, false,pos,count,this);
			else if(commandAction == CommandAction.OK)
				rptQtnsDataList.setRepeatQtnsDataById((RepeatQtnsData)data);
			
			if(commandAction == CommandAction.OK || commandAction == CommandAction.CANCEL)
				showQtnsData(validationRule);
		}
	}
}
