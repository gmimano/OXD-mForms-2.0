package org.fcitmuk.epihandy.midp.forms;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.fcitmuk.epihandy.EpihandyConstants;
import org.fcitmuk.epihandy.FormData;
import org.fcitmuk.epihandy.PageData;
import org.fcitmuk.epihandy.QuestionData;
import org.fcitmuk.epihandy.QuestionDef;
import org.fcitmuk.epihandy.ValidationRule;
import org.fcitmuk.midp.db.util.Settings;
import org.fcitmuk.midp.mvc.AbstractView;
import org.fcitmuk.util.AlertMessage;
import org.fcitmuk.util.AlertMessageListener;
import org.fcitmuk.util.DefaultCommands;
import org.fcitmuk.util.MenuText;
import org.fcitmuk.util.Utilities;

/**
 * Displays a form. This means displaying the list of questions on a form.
 * This view may also optionally like to display the answers of the questions if any.
 * It is up to the view to either display the questions either one at a time
 * or all at the same time. The view does not know how to edit a question (as in doesnt know
 * how to edit images, dates, numbers, text etc). All it does is provide the user with
 * a way of starting editing of a question. This could be by a button, voice or any other
 * way depending on its implementation. The view also doesnt know where and how to save a form.
 * All it does is delegate to the controller and pass it the modified model.
 * It is the view which decides whether to group questions in pages or any other format.
 * It should allow the user to:
 * 1. Browse through the questions.
 * 2. Edit a question, 
 * 3. Save the form.
 * 4. Close the form.
 * 
 * @author Daniel
 *
 */
public class FormView extends AbstractView implements AlertMessageListener {

	/** Command for displaying the next page. */
	private Command cmdNext = new Command(MenuText.NEXT_PAGE(),Command.SCREEN,1);

	/** Command for displaying the previous page. */
	private Command cmdPrev = new Command(MenuText.PREVIOUS_PAGE(),Command.SCREEN,2);

	private FormData formData;
	private PageData currentPage; //TODO is this really necessary????
	private AlertMessage alertMsg;
	private FormListener listener;

	//for managing state
	private int currentPageIndex = EpihandyConstants.NO_SELECTION;
	private int currentQuestionIndex = EpihandyConstants.NO_SELECTION;
	private QuestionData currentQuestion = null;

	private final byte CA_NONE = 0;
	private final byte CA_CONFIRM_CANCEL = 1;
	private final byte CA_CONFIRM_DELETE = 2;
	private final byte CA_ERROR = 3;
	//private final byte CA_NO_VISIBLE_QTNS = 4;

	private byte currentAction = CA_NONE;

	private boolean dirty = false;


	/** Keeps a mapping of displayed questions (in a page) to their indices in the list control.
	 *  We were originally using the questions collection of the page in formdata which
	 *  did not work as their indices get out of sync with those of the List control
	 *  because of invisible questions not being put in the list.
	 */
	private Vector displayedQuestions;

	/*public FormView(){

	}*/

	/**
	 * Called by the controller after an editing operation.
	 * 
	 * @param saved - true when the edit was commited, else false.
	 */
	public void onEndEdit(boolean saved, Command cmd){

		if(!dirty && saved)
			dirty = true;

		display.setCurrent(screen);

		if(cmd == DefaultCommands.cmdBackParent){
			if(currentQuestionIndex > 0)
				currentQuestionIndex-=1;
			else
				currentQuestionIndex = displayedQuestions.size() - 1;
		}
		else if(cmd == DefaultCommands.cmdPrev){
			if(currentQuestionIndex > 1)
				currentQuestionIndex-=2;
			else{
				currentQuestionIndex -=1; // displayedQuestions.size() - 1;	
				cmd = DefaultCommands.cmdFirst;
			}
		}
		else if(cmd == DefaultCommands.cmdFirst)
			currentQuestionIndex = 0;
		else if(cmd == DefaultCommands.cmdLast)
			currentQuestionIndex = displayedQuestions.size() - 1;

		if((currentQuestionIndex == 0 && (cmd == DefaultCommands.cmdFirst))||
				(currentQuestionIndex == displayedQuestions.size() - 1 && (cmd == DefaultCommands.cmdBackParent))){
			currentQuestionIndex = 0;
			currentQuestion = null;
		}
		else
			currentQuestion  = (QuestionData)displayedQuestions.elementAt(currentQuestionIndex);

		//if(saved)
		//	currentQuestionIndex = getNextQuestionIndex(false);
		showPage(this.currentPageIndex,new Integer(currentQuestionIndex));

		if(cmd != DefaultCommands.cmdBackParent && getEpihandyController().isSingleQuestionEdit())
		{
			//if we are on the last question.
			if(currentQuestionIndex == displayedQuestions.size()){
				//if no on the last page
				if(currentPageIndex < formData.getPages().size()){
					currentPageIndex++;
					if(currentPageIndex == formData.getPages().size()){
						currentPageIndex = 0;
						currentQuestionIndex = 0;
					}
					showPage(currentPageIndex,new Integer(0));
				}
			}

			handleListSelectCommand(screen);
		}
	}

	/**
	 * Gets the index of the next question for editing.
	 * Gets the next visible and enabled question, else stays at the current question.
	 * 
	 * @param answered - set to true if you want to skip questions with answers already.
	 * 
	 * @return - the next question display index.
	 */
	private int getNextQuestionIndex(boolean notAnswered){
		int index = currentQuestionIndex;//+1;

		while(index < displayedQuestions.size()){
			QuestionData qtn = (QuestionData)displayedQuestions.elementAt(index);
			QuestionDef def = qtn.getDef();
			if(def.isVisible() && def.isEnabled()){
				if(notAnswered){
					if(!qtn.isAnswered()){
						currentQuestionIndex = index;
						break;
					}
				}
				else{
					if(currentQuestion == null){
						currentQuestion = qtn;
						currentQuestionIndex = index;
						break;
					}
					else if(currentQuestion.getId() == qtn.getId()){
						currentQuestionIndex = ++index;
						if(currentQuestionIndex < displayedQuestions.size())
							currentQuestion = (QuestionData)displayedQuestions.elementAt(currentQuestionIndex);
						break;
					}
				}
			}
			index++;
		}
		return currentQuestionIndex;
	}

	public void showForm(FormData data,FormListener listener, boolean allowDelete){
		try{
			this.formData = new FormData(data);
			this.listener  = listener;

			currentPageIndex = 0;
			currentQuestionIndex = 0;	
			currentQuestion = null;
			dirty = false;

			//create here such that the show page can use.
			screen = new List(this.formData.getDef().getName() + " - " + title, Choice.IMPLICIT);
			alertMsg = new AlertMessage(display,title,screen,this);

			showPage(currentPageIndex,new Integer(currentQuestionIndex));

			//commands are added here because the show page can remove all of them for ease
			//of current implementation.
			if(displayedQuestions.size() > 0){
				screen.setCommandListener(this);
				if(allowDelete)
					screen.addCommand(DefaultCommands.cmdDelete);
				screen.addCommand(DefaultCommands.cmdSave);
				screen.addCommand(DefaultCommands.cmdCancel);

				display.setCurrent(screen);
			}
		}
		catch(Exception e){
			String s = MenuText.FORM_DISPLAY_PROBLEM();
			if(e.getMessage() != null && e.getMessage().trim().length() > 0)
				s = e.getMessage();
			currentAction = CA_ERROR;
			alertMsg.showError(s);
			//e.printStackTrace();
		}
	}

	/**
	 * Shows a particular page.
	 * 
	 * @param pageIndex - the index of the page.
	 * @param currentQuestionIndex - the index of the question to preselect.
	 */
	private void showPage(int pageIndex,Integer currentQuestionIndex){
		currentPageIndex = pageIndex;
		((List)screen).deleteAll();

		Vector pages = formData.getPages();
		currentPage = ((PageData)pages.elementAt(pageIndex));
		Vector qns = currentPage.getQuestions();

		Settings settings = new Settings(GeneralSettings.STORAGE_NAME_SETTINGS,true);
		boolean numbering = Utilities.stringToBoolean(settings.getSetting(GeneralSettings.KEY_QUESTION_NUMBERING));

		displayedQuestions = new Vector();
		QuestionData qn; 
		for(int i=0; i<qns.size(); i++){
			qn = (QuestionData)qns.elementAt(i);
			if(qn.getDef().isVisible()){
				String s = "";
				if(qn.getDef().isMandatory() && !qn.isAnswered())
					s += "? ";
				((List)screen).append((numbering ? String.valueOf(i+1)+" " : "") + s + qn.toString(),null);
				displayedQuestions.addElement(qn);
			}
		}

		if(pageIndex < pages.size()-1)
			screen.addCommand(cmdNext);
		else
			screen.removeCommand(cmdNext);

		if(pageIndex > 0)
			screen.addCommand(cmdPrev);
		else
			screen.removeCommand(cmdPrev);

		if(displayedQuestions.size() == 0){
			//currentAction = CA_NO_VISIBLE_QTNS;
			//alertMsg.show(MenuText.NO_VISIBLE_QUESTION() + " "+qns.size()+ MenuText.QUESTIONS());	
		}
		else{
			selectNextQuestion(currentQuestionIndex);

			//screen.setTitle(title + " {" + this.formData.getDef().getName() /* + currentPage.getDef().getName()*/ + "}");
			//screen.setTitle(this.formData.getDef().getName() + " - "+title);	
			/*String name = "";
			if(formData.getDef().getPageCount() > 1)
				name = currentPage.getDef().getName()+ " - ";*/
			screen.setTitle((formData.getDef().getPageCount() > 1 ? currentPage.getDef().getName()+ " - " : "") + formData.getDef().getName() + " - " + title);
		}
	}

	/**
	 * Selects the next question to edit.
	 * 
	 * @param currentQuestionIndex - index of the current question to edit
	 */
	private void selectNextQuestion(Integer currentQuestionIndex){
		if(currentQuestionIndex == null)
			((List)screen).setSelectedIndex(0, true);
		else{
			currentQuestionIndex = new Integer(getNextQuestionIndex(false));
			if(currentQuestionIndex != null && currentQuestionIndex.intValue() < ((List)screen).size())
				((List)screen).setSelectedIndex(currentQuestionIndex.intValue(), true);
			else if(currentQuestionIndex != null  && currentQuestionIndex.intValue() == ((List)screen).size()){
				//TODO Restructure this with the above. Added temporarily to prevent jumping to the
				//first question from the last one.
				currentQuestionIndex = new Integer(((List)screen).size() - 1);
				((List)screen).setSelectedIndex(currentQuestionIndex.intValue(), true);
			}
		}
	}

	/** Moves to the next page. */
	private void nextPage(){
		showPage(++this.currentPageIndex,null);
	}

	/** Moves to the previous page. */
	private void prevPage(){
		showPage(--this.currentPageIndex,null);
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == List.SELECT_COMMAND)
				handleListSelectCommand(d);
			else if(c == DefaultCommands.cmdSave)
				handleSaveCommand(d);
			/*else if(c == DefaultCommands.cmdOk)
				handleOkCommand(d);*/
			else if(c == DefaultCommands.cmdCancel)
				handleCancelCommand(d);
			else if(c == cmdNext)
				nextPage();
			else if(c == cmdPrev)
				prevPage();
			else if(c == DefaultCommands.cmdDelete)
				handleDeleteCommand(d);
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
			//e.printStackTrace();
		}
	}

	/**
	 * Processes the delete command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleDeleteCommand(Displayable d){
		currentAction = CA_CONFIRM_DELETE;
		alertMsg.showConfirm(MenuText.DATA_DELETE_PROMPT());
		//getEpihandyController().handleCancelCommand(this);
	}

	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleCancelCommand(Displayable d){
		currentAction = CA_CONFIRM_CANCEL;

		if(dirty)
			alertMsg.showConfirm(MenuText.FORM_CLOSE_PROMPT());
		else
			onAlertMessage(AlertMessageListener.MSG_OK);
		//getEpihandyController().handleCancelCommand(this);
	}

	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleOkCommand(Displayable d){
		//saveData();
		//controller.endEdit(true, this.currentQuestion);
	}*/

	/**
	 * Processes the Save command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleSaveCommand(Displayable d){
		//Check if user entered data correctly.
		if(!formData.isRequiredAnswered()){
			alertMsg.show(MenuText.REQUIRED_PROMPT());
			selectMissingValueQtn();
		}
		else if(!formData.isFormAnswered())
			alertMsg.show(MenuText.ANSWER_MINIMUM_PROMPT());
		else{
			String errMsg = selectInvalidQtn();			
			if(errMsg != null){
				alertMsg.show(errMsg);
				return;
			}

			boolean save = true;
			if(listener != null)
				save = listener.beforeFormSaved(formData,formData.isNew()); //Give the API user a chance to do some custom validations.

			if(save){
				getEpihandyController().saveForm(formData);

				if(listener != null)
					listener.afterFormSaved(formData,formData.isNew());
			}
		}
	}

	private boolean selectMissingValueQtn(byte pageNo){	

		if(pageNo != currentPageIndex)
			showPage(pageNo,new Integer(0));

		for(byte i=0; i<displayedQuestions.size(); i++){
			QuestionData qtn = (QuestionData)displayedQuestions.elementAt(i);
			QuestionDef def = qtn.getDef();
			if(def.isMandatory() && !qtn.isAnswered()){
				((List)screen).setSelectedIndex(i, true);
				return true;
			}
		}

		return false;
	}

	private String selectInvalidQtn(byte pageNo){	

		if(pageNo != currentPageIndex)
			showPage(pageNo,new Integer(0));

		for(byte i=0; i<displayedQuestions.size(); i++){
			QuestionData qtn = (QuestionData)displayedQuestions.elementAt(i);

			ValidationRule rule = formData.getDef().getValidationRule(qtn.getId());
			if(rule == null)
				continue;

			rule.setFormData(formData);

			if(!rule.isValid()){
				((List)screen).setSelectedIndex(i, true);
				return rule.getErrorMessage();
			}
		}

		return null;
	}

	private void selectMissingValueQtn(){
		Vector pages = formData.getPages();
		for(byte i = 0; i < pages.size(); i++){
			if(selectMissingValueQtn(i))
				break;
		}
	}

	private String selectInvalidQtn(){
		Vector pages = formData.getPages();
		for(byte i = 0; i < pages.size(); i++){
			String errorMsg = selectInvalidQtn(i);
			if(errorMsg != null)
				return errorMsg;
		}
		return null;
	}

	/**
	 * Processes the list selection command event. This is the command that the user
	 * invokes to start editing of a question.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	public void handleListSelectCommand(Displayable d){
		//handleOkCommand(d);
		//save the user state for more friendliness
		currentQuestionIndex = ((List)d).getSelectedIndex();
		currentQuestion = (QuestionData)displayedQuestions.elementAt(currentQuestionIndex);
		if(currentQuestion.getDef().isEnabled()){
			boolean edit = true;
			if(listener != null)
				edit = listener.beforeQuestionEdit(currentQuestion); //give the API user a chance to override this editing.

			if(edit)
				getEpihandyController().startEdit(currentQuestion,(byte)(currentQuestionIndex+1),(byte)displayedQuestions.size());
		}
	}

	private EpihandyController getEpihandyController(){
		return (EpihandyController)controller;
	}

	/**
	 * If in cancel mode, user is sure wants to cancel saving changed (discard form data)
	 */
	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			if(currentAction == CA_CONFIRM_CANCEL || currentAction == CA_ERROR)
				getEpihandyController().handleCancelCommand(this);
			else if(currentAction == CA_CONFIRM_DELETE)
				getEpihandyController().deleteForm(formData,this);
			else
				display.setCurrent(screen);
		}
		else
			show();

		currentAction = CA_NONE;
	}

	public FormData getFormData(){
		return formData;
	}
}
