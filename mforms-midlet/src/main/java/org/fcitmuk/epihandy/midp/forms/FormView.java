package org.fcitmuk.epihandy.midp.forms;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;

import org.fcitmuk.epihandy.EpihandyConstants;
import org.fcitmuk.epihandy.FormData;
import org.fcitmuk.epihandy.PageData;
import org.fcitmuk.epihandy.QuestionData;
import org.fcitmuk.epihandy.QuestionDef;
import org.fcitmuk.epihandy.ValidationRule;
import org.fcitmuk.epihandy.midp.db.StoredStudyDef;
import org.fcitmuk.epihandy.midp.model.Model;
import org.fcitmuk.epihandy.midp.model.ModelListener;
import org.fcitmuk.midp.mvc.AbstractView;
import org.fcitmuk.util.AlertMessage;
import org.fcitmuk.util.AlertMessageListener;
import org.fcitmuk.util.DefaultCommands;
import org.fcitmuk.util.MenuText;

/**
 * Displays a form. This means displaying the list of questions on a form. This
 * view may also optionally like to display the answers of the questions if any.
 * It is up to the view to either display the questions either one at a time or
 * all at the same time. The view does not know how to edit a question (as in
 * doesnt know how to edit images, dates, numbers, text etc). All it does is
 * provide the user with a way of starting editing of a question. This could be
 * by a button, voice or any other way depending on its implementation. The view
 * also doesnt know where and how to save a form. All it does is delegate to the
 * controller and pass it the modified model. It is the view which decides
 * whether to group questions in pages or any other format. It should allow the
 * user to: 1. Browse through the questions. 2. Edit a question, 3. Save the
 * form. 4. Close the form.
 * 
 * @author Daniel
 * 
 */
public class FormView extends AbstractView implements AlertMessageListener, ModelListener {

	/** Command for displaying the next page. */
	private Command cmdNext = new Command(MenuText.NEXT_PAGE(), Command.SCREEN, 1);

	/** Command for displaying the previous page. */
	private Command cmdPrev = new Command(MenuText.PREVIOUS_PAGE(), Command.SCREEN, 2);

	/** Command for jumping to a particular page */
	private Command cmdGotoPage = new Command(MenuText.GOTO_PAGE(), Command.SCREEN, 3);

	/** Command for jumping to a particular question */
	private Command cmdGotoQuestion = new Command(MenuText.GOTO_QUESTION(), Command.SCREEN, 4);

	private FormData formData;
	private PageData currentPage; // TODO is this really necessary????
	private AlertMessage alertMsg;

	// for managing state
	private int currentPageIndex = EpihandyConstants.NO_SELECTION;
	private int currentQuestionIndex = EpihandyConstants.NO_SELECTION;
	private QuestionData currentQuestion = null;

	private final byte CA_NONE = 0;
	private final byte CA_CONFIRM_CANCEL = 1;
	private final byte CA_CONFIRM_DELETE = 2;
	private final byte CA_ERROR = 3;

	private byte currentAction = CA_NONE;

	private boolean dirty = false;

	/**
	 * Keeps a mapping of displayed questions (in a page) to their indices in
	 * the list control. We were originally using the questions collection of
	 * the page in formdata which did not work as their indices get out of sync
	 * with those of the List control because of invisible questions not being
	 * put in the list.
	 */
	private Vector displayedQuestions;

	private Model model;

	public FormView(Model model) {
		this.model = model;
		model.addModelListener(this);
		List list = new List("", Choice.IMPLICIT);
		list.setFitPolicy(List.TEXT_WRAP_ON);
		screen = list;
		alertMsg = new AlertMessage(display, title, screen, this);
	}

	/**
	 * Called by the controller after an editing operation.
	 * 
	 * @param saved
	 *            - true when the edit was commited, else false.
	 */
	public void onEndEdit(boolean saved, Command cmd) {

		if (!dirty && saved)
			dirty = true;

		display.setCurrent(screen);

		if (cmd == DefaultCommands.cmdBackParent) {
			if (currentQuestionIndex > 0)
				currentQuestionIndex -= 1;
			else
				currentQuestionIndex = displayedQuestions.size() - 1;
		} else if (cmd == DefaultCommands.cmdPrev) {
			if (currentQuestionIndex > 1)
				currentQuestionIndex -= 2;
			else {
				currentQuestionIndex -= 1;
				cmd = DefaultCommands.cmdFirst;
			}
		} else if (cmd == DefaultCommands.cmdFirst)
			currentQuestionIndex = 0;
		else if (cmd == DefaultCommands.cmdLast)
			currentQuestionIndex = displayedQuestions.size() - 1;

		if ((currentQuestionIndex == 0 && cmd == DefaultCommands.cmdFirst)
				|| (currentQuestionIndex == (displayedQuestions.size() - 1) && cmd == DefaultCommands.cmdBackParent)) {
			currentQuestionIndex = 0;
			currentQuestion = null;
		} else
			currentQuestion = (QuestionData) displayedQuestions.elementAt(currentQuestionIndex);

		// FIXME: serious code smell going on here with the
		// "currentQuestionIndex" variables that shadow the class attribute -
		// sometimes. Along with sneaky "pass by reference" to ensure the class
		// variable is updated (sometimes)!
		showPage(this.currentPageIndex, new Integer(currentQuestionIndex));

		if (cmd != DefaultCommands.cmdBackParent && GeneralSettings.isSingleQtnEdit()) {
			if (cmd != DefaultCommands.cmdLast && currentQuestionIndex == displayedQuestions.size()) {
				// if we are beyond the last question
				// start at the first question ("back to list" functionality
				// which is consistent with "single question edit" setting on
				// the DefaultTypeEditor)
				currentQuestionIndex = 0;
				showPage(currentPageIndex, new Integer(currentQuestionIndex));
			} else {
				handleListSelectCommand(screen);
			}
		}

		// automatic saving of the form after a question edit.
		// we call the model saveFromData method directly because we don't want to be 
		// redirected from current screen by the controller
		if(GeneralSettings.isAutoSave() && this.formData.isFormAnswered()){
			this.model.saveFormData(model.getSelectedStudyDef().getId(), this.formData);
			
			//we set the dirty flag to false because we have saved the form
			dirty = false;
		}
	}

	/**
	 * Gets the index of the next question for editing. Gets the next visible
	 * and enabled question, else stays at the current question.
	 * 
	 * @param answered
	 *            - set to true if you want to skip questions with answers
	 *            already.
	 * 
	 * @return - the next question display index.
	 */
	private int getNextQuestionIndex(boolean notAnswered) {
		int index = currentQuestionIndex;// +1;

		while (index < displayedQuestions.size()) {
			QuestionData qtn = (QuestionData) displayedQuestions.elementAt(index);
			QuestionDef def = qtn.getDef();
			if (def.isVisible() && def.isEnabled()) {
				if (notAnswered) {
					if (!qtn.isAnswered()) {
						currentQuestionIndex = index;
						break;
					}
				} else {
					if (currentQuestion == null) {
						currentQuestion = qtn;
						currentQuestionIndex = index;
						break;
					} else if (currentQuestion.getId() == qtn.getId()) {
						do {
							currentQuestionIndex = ++index;
							if (currentQuestionIndex < displayedQuestions.size()) {
								currentQuestion = (QuestionData) displayedQuestions
										.elementAt(currentQuestionIndex);
							} else {
								break; // if we are at the end, stop looking
							}
						} while (!currentQuestion.getDef().isEnabled());
						break;
					}
				}
			}
			index++;
		}

		return currentQuestionIndex;
	}

	public void showForm() {
		display.setCurrent(screen);
	}

	private void initForm(FormData data) {
		try {
			formData = new FormData(data);

			screen.setTitle(formData.getDef().getName() + " - " + title);

			getEpihandyController().FireSkipRules(formData);

			currentPageIndex = 0;
			currentQuestionIndex = 0;
			currentQuestion = null;
			dirty = false;

			// create here such that the show page can use.

			showPage(currentPageIndex, new Integer(currentQuestionIndex));

			// commands are added here because the show page can remove all of
			// them for ease
			// of current implementation.
			if (displayedQuestions.size() > 0) {
				screen.setCommandListener(this);
				if (!data.isNew())
					screen.addCommand(DefaultCommands.cmdDelete);
				else
					screen.removeCommand(DefaultCommands.cmdDelete);

				// adding the goto commands
				if (GeneralSettings.isQtnNumbering())
					screen.addCommand(cmdGotoQuestion);

				screen.addCommand(cmdGotoPage);

				screen.addCommand(DefaultCommands.cmdSave);
				screen.addCommand(DefaultCommands.cmdCancel);
				// TODO: Should get from model
				StoredStudyDef study = getEpihandyController().getCurrentStudy();

				// Only show error commands if there are errors present.
				if (model.formDataInError(study.getId(), data.getDefId(), data.getRecordId())) {
					screen.addCommand(DefaultCommands.cmdShowErrors);
					screen.addCommand(DefaultCommands.cmdClearErrors);
				} else {
					screen.removeCommand(DefaultCommands.cmdShowErrors);
					screen.removeCommand(DefaultCommands.cmdClearErrors);
				}
			}
		} catch (Exception e) {
			String s = MenuText.FORM_DISPLAY_PROBLEM();
			if (e.getMessage() != null && e.getMessage().trim().length() > 0)
				s = e.getMessage();
			currentAction = CA_ERROR;
			alertMsg.showError(s);
		}
	}

	/**
	 * Shows a particular page.
	 * 
	 * @param pageIndex
	 *            - the index of the page.
	 * @param currentQuestionIndex
	 *            - the index of the question to preselect.
	 */
	private void showPage(int pageIndex, Integer currentQuestionIndex) {

		Vector pages = formData.getPages();
		if (pageIndex < 0 && pageIndex >= pages.size()) {
			alertMsg.showError(MenuText.FORM_DISPLAY_PROBLEM());
			return;
		}

		currentPageIndex = pageIndex;
		((List) screen).deleteAll();

		currentPage = ((PageData) pages.elementAt(pageIndex));
		boolean useQtnNumbering = GeneralSettings.isQtnNumbering();
		int qtnNumberCount = (useQtnNumbering ? previousQuestionCount(pages, pageIndex) : 0);
		Vector qns = currentPage.getQuestions();

		Image image = null;
		QuestionData qn;
		displayedQuestions = new Vector();
		for (int i = 0; i < qns.size(); i++) {
			qn = (QuestionData) qns.elementAt(i);
			if (qn.getDef().isVisible()) {

				if (qn.getDef().isMandatory() && !qn.isAnswered())
					image = Images.REQUIRED_IMAGE;
				else if (!qn.getDef().isEnabled())
					image = Images.DISABLED_QUESTION;
				else
					image = Images.EMPTY_IMAGE;

				((List) screen).append((useQtnNumbering ? String.valueOf(qtnNumberCount + i + 1)
						+ " " : "")
						+ qn.toString(), image);

				displayedQuestions.addElement(qn);
			}
		}

		if (pageIndex < pages.size() - 1)
			screen.addCommand(cmdNext);
		else
			screen.removeCommand(cmdNext);

		if (pageIndex > 0)
			screen.addCommand(cmdPrev);
		else
			screen.removeCommand(cmdPrev);

		if (displayedQuestions.size() != 0) {
			selectNextQuestion(currentQuestionIndex);

			String name = "";
			if (formData.getDef().getPageCount() > 1)
				name = currentPage.getDef().getName() + " - ";
			screen.setTitle(name + formData.getDef().getName() + " - " + title);
		}
	}

	private int previousQuestionCount(Vector pages, int pageIndex) {
		int qtnNumberCount = 0;
		for (int i = 0; i < pageIndex; i++) {
			PageData pd = (PageData) pages.elementAt(i);
			qtnNumberCount += pd.getNumberOfQuestions();
		}
		return qtnNumberCount;
	}

	/**
	 * Selects the next question to edit.
	 * 
	 * @param currentQuestionIndex
	 *            - index of the current question to edit
	 */
	private void selectNextQuestion(Integer currentQuestionIndex) {
		if (currentQuestionIndex == null) {
			((List) screen).setSelectedIndex(0, true);
		} else {
			currentQuestionIndex = new Integer(getNextQuestionIndex(false));
			if (currentQuestionIndex != null
					&& currentQuestionIndex.intValue() < ((List) screen).size()) {
				((List) screen).setSelectedIndex(currentQuestionIndex.intValue(), true);
			} else if (currentQuestionIndex != null
					&& currentQuestionIndex.intValue() == ((List) screen).size()) {
				// TODO Restructure this with the above. Added temporarily to
				// prevent jumping to the
				// first question from the last one.
				currentQuestionIndex = new Integer(((List) screen).size() - 1);
				((List) screen).setSelectedIndex(currentQuestionIndex.intValue(), true);
			}
		}
	}

	/** Moves to the next page. */
	private void nextPage() {
		showPage(++this.currentPageIndex, null);
	}

	/** Moves to the previous page. */
	private void prevPage() {
		showPage(--this.currentPageIndex, null);
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c
	 *            - the issued command.
	 * @param d
	 *            - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try {
			if (c == List.SELECT_COMMAND)
				handleListSelectCommand(d);
			else if (c == DefaultCommands.cmdSave)
				handleSaveCommand(d);
			else if (c == DefaultCommands.cmdCancel)
				handleCancelCommand(d);
			else if (c == cmdNext)
				nextPage();
			else if (c == cmdPrev)
				prevPage();
			else if (c == DefaultCommands.cmdDelete)
				handleDeleteCommand(d);
			else if (c == DefaultCommands.cmdShowErrors)
				handleShowErrors();
			else if (c == DefaultCommands.cmdClearErrors)
				handleClearErrors();
			else if (c == cmdGotoPage)
				handleGotoPageCommand(d);
			else if (c == cmdGotoQuestion)
				handleGotoQuestion(d);
		} catch (Exception e) {
			alertMsg.showError(e.getMessage());
		}
	}

	private void handleGotoQuestion(Displayable d) {
		Form gotoQuestionForm = new Form(MenuText.GOTO_QUESTION_INPUT());
		final TextField questionInput = new TextField(MenuText.GOTO_QUESTION_INPUT_LABEL(), null,
				10, TextField.NUMERIC);
		gotoQuestionForm.append(questionInput);
		gotoQuestionForm.addCommand(DefaultCommands.cmdOk);
		gotoQuestionForm.setCommandListener(new CommandListener() {

			public void commandAction(Command cmd, Displayable d) {
				if (cmd == DefaultCommands.cmdOk) {
					try {
						int questionNo = Integer.parseInt(questionInput.getString());
						Vector formQuestions = getFormQuestions(formData);
						if (formQuestions != null) {
							if (questionNo <= 0 || questionNo > formQuestions.size())
								throw new Exception("supplied question doesnot exist");

							QuestionData qData = (QuestionData) formQuestions
									.elementAt(questionNo - 1);
							int pageIndex = 0, questionIndex = 0;
							for (int index = 0; index < formData.getPages().size(); index++) {
								PageData pageData = (PageData) formData.getPages().elementAt(index);
								if (pageData != null && pageData.getQuestions() != null
										&& pageData.getQuestions().size() > 0) {

									boolean indexFound = false;
									for (int qIndex = 0; qIndex < pageData.getQuestions().size(); qIndex++) {
										QuestionData questionData = (QuestionData) pageData
												.getQuestions().elementAt(qIndex);

										if (questionData.getDef().isVisible()) {
											if (questionData
													.getDef()
													.getVariableName()
													.equalsIgnoreCase(
															qData.getDef().getVariableName())) {

												pageIndex = index;
												questionIndex = qIndex;
												indexFound = true;
												break;
											}
										}
									}

									if (indexFound)
										break;
								}
							}

							showForm();
							showPage(pageIndex, null);
							if (questionIndex < 0 && questionIndex > ((List) screen).size() - 1)
								((List) screen).setSelectedIndex(0, true);
							else
								((List) screen).setSelectedIndex(questionIndex, true);
						}
					} catch (NumberFormatException e) {
						alertMsg.show("Invalid Numeric Input");
					} catch (Exception e) {
						alertMsg.show(e.getMessage());
					}
				}
			}
		});

		display.setCurrent(gotoQuestionForm);
	}

	/**
	 * gets the PageData the given QuestionData belongs to in the given FormData
	 * 
	 * @param formData
	 * @param qData
	 * @return
	 */
	protected PageData getPageData(FormData formData, QuestionData qData) {
		if ((formData != null && formData.getPages() != null && formData.getPages().size() > 0)
				&& qData != null) {

			for (int index = 0; index < formData.getPages().size(); index++) {
				PageData pageData = (PageData) formData.getPages().elementAt(index);
				if (pageData != null && pageData.getQuestions() != null
						&& pageData.getQuestions().size() > 0) {

					for (int qIndex = 0; qIndex < pageData.getQuestions().size(); qIndex++) {
						QuestionData questionData = (QuestionData) pageData.getQuestions()
								.elementAt(qIndex);

						if (questionData.getDef().getVariableName()
								.equalsIgnoreCase(qData.getDef().getVariableName()))
							return pageData;
					}
				}
			}
		}
		return null;
	}

	private Vector getFormQuestions(FormData formData) {
		if (formData != null && formData.getPages() != null && formData.getPages().size() > 0) {

			Vector questions = new Vector();
			for (int index = 0; index < formData.getPages().size(); index++) {
				PageData pageData = (PageData) formData.getPages().elementAt(index);
				if (pageData != null && pageData.getQuestions() != null
						&& pageData.getQuestions().size() > 0) {

					for (int qIndex = 0; qIndex < pageData.getQuestions().size(); qIndex++) {
						questions.addElement(pageData.getQuestions().elementAt(qIndex));
					}
				}
			}

			return questions.size() <= 0 ? null : questions;
		} else
			return null;
	}

	private void handleGotoPageCommand(Displayable d) {
		List gotoPageList = new List(MenuText.GOTO_PAGE_SELECTION_LIST(), Choice.IMPLICIT);
		if (this.formData.getPages() != null && this.formData.getPages().size() > 1) {

			for (int index = 0; index < this.formData.getPages().size(); index++) {
				PageData pageData = (PageData) this.formData.getPages().elementAt(index);

				gotoPageList.append(pageData.getDef().getName().toLowerCase(), null);
			}
		}

		gotoPageList.addCommand(DefaultCommands.cmdOk);
		gotoPageList.setCommandListener(new CommandListener() {

			public void commandAction(Command cmd, Displayable d) {
				if (cmd == DefaultCommands.cmdOk) {
					int selectedIndex = ((List) d).getSelectedIndex();
					showForm();
					showPage(selectedIndex, null);
				}
			}
		});

		display.setCurrent(gotoPageList);
	}

	private void handleShowErrors() {
		currentAction = CA_NONE;
		int studyDefId = getEpihandyController().getCurrentStudy().getId();
		String errorMessage = model.getFormError(studyDefId, formData);
		alertMsg.show(errorMessage);
	}

	private void handleClearErrors() {
		currentAction = CA_NONE;
		int studyDefId = getEpihandyController().getCurrentStudy().getId();
		// TODO: Clean this up...
		model.deleteFormError(studyDefId, formData);
		alertMsg.show("Cleared form errors.");
	}

	/**
	 * Processes the delete command event.
	 * 
	 * @param d
	 *            - the screen object the command was issued for.
	 */
	private void handleDeleteCommand(Displayable d) {
		currentAction = CA_CONFIRM_DELETE;
		alertMsg.showConfirm(MenuText.DATA_DELETE_PROMPT());
	}

	/**
	 * Processes the cancel command event.
	 * 
	 * @param d
	 *            - the screen object the command was issued for.
	 */
	private void handleCancelCommand(Displayable d) {
		currentAction = CA_CONFIRM_CANCEL;

		if (dirty)
			alertMsg.showConfirm(MenuText.FORM_CLOSE_PROMPT());
		else
			onAlertMessage(AlertMessageListener.MSG_OK);
	}

	/**
	 * Processes the Save command event.
	 * 
	 * @param d
	 *            - the screen object the command was issued for.
	 */
	private void handleSaveCommand(Displayable d) {
		// if autosave is enabled, we save the form regardless 
		// whether the form is unanswered required questions
		
		if (GeneralSettings.isAutoSave()) {
			if(this.formData.isFormAnswered()){
				getEpihandyController().saveFormData(formData);
			}else
				alertMsg.show(MenuText.ANSWER_MINIMUM_PROMPT());
		} else {
			// Check if user entered data correctly.
			if (!formData.isRequiredAnswered()) {
				alertMsg.show(MenuText.REQUIRED_PROMPT());
				selectMissingValueQtn();
			} else if (!formData.isFormAnswered())
				alertMsg.show(MenuText.ANSWER_MINIMUM_PROMPT());
			else {
				String errMsg = selectInvalidQtn();
				if (errMsg != null) {
					alertMsg.show(errMsg);
					return;
				}

				getEpihandyController().saveFormData(formData);
			}
		}
	}

	private boolean selectMissingValueQtn(int pageNo){	

		if (pageNo != currentPageIndex)
			showPage(pageNo, new Integer(0));

		for (int i = 0; i < displayedQuestions.size(); i++) {
			QuestionData qtn = (QuestionData) displayedQuestions.elementAt(i);
			QuestionDef def = qtn.getDef();
			if (def.isMandatory() && !qtn.isAnswered()) {
				((List) screen).setSelectedIndex(i, true);
				return true;
			}
		}

		return false;
	}

	private String selectInvalidQtn(int pageNo){	

		if (pageNo != currentPageIndex)
			showPage(pageNo, new Integer(0));

		for (int i = 0; i < displayedQuestions.size(); i++) {
			QuestionData qtn = (QuestionData) displayedQuestions.elementAt(i);

			ValidationRule rule = formData.getDef().getValidationRule(qtn.getId());
			if (rule == null)
				continue;

			rule.setFormData(formData);

			if (!rule.isValid()) {
				((List) screen).setSelectedIndex(i, true);
				return rule.getErrorMessage();
			}
		}

		return null;
	}

	private void selectMissingValueQtn() {
		Vector pages = formData.getPages();
		for(int i = 0; i < pages.size(); i++){
			if(selectMissingValueQtn(i))
				break;
		}
	}

	private String selectInvalidQtn() {
		Vector pages = formData.getPages();
		for(int i = 0; i < pages.size(); i++){
			String errorMsg = selectInvalidQtn(i);
			if (errorMsg != null)
				return errorMsg;
		}
		return null;
	}

	/**
	 * Processes the list selection command event. This is the command that the
	 * user invokes to start editing of a question.
	 * 
	 * @param d
	 *            - the screen object the command was issued for.
	 */
	public void handleListSelectCommand(Displayable d) {
		// save the user state for more friendliness
		currentQuestionIndex = ((List) d).getSelectedIndex();
		currentQuestion = (QuestionData) displayedQuestions.elementAt(currentQuestionIndex);
		if (currentQuestion.getDef().isEnabled()) {
			getEpihandyController().startEdit(currentQuestion, currentQuestionIndex + 1,
					displayedQuestions.size());
		}
	}

	private EpihandyController getEpihandyController() {
		return (EpihandyController) controller;
	}

	/**
	 * If in cancel mode, user is sure wants to cancel saving changed (discard
	 * form data)
	 */
	public void onAlertMessage(byte msg) {
		if (msg == AlertMessageListener.MSG_OK) {
			if (currentAction == CA_CONFIRM_CANCEL || currentAction == CA_ERROR)
				getEpihandyController().handleCancelCommand(this);
			else if (currentAction == CA_CONFIRM_DELETE)
				getEpihandyController().deleteForm(formData);
			else
				display.setCurrent(screen);
		} else
			show();

		currentAction = CA_NONE;
	}

	public FormData getFormData() {
		return formData;
	}

	public void formDataChanged(Model m) {
		// TODO Auto-generated method stub
	}

	public void formDataSelected(Model m) {
		FormData selectedData = model.getSelectedFormData();
		if (selectedData == null)
			selectedData = new FormData(model.getActiveForm());
		initForm(selectedData);
	}

	public void formErrorsChanged(Model m) {
		// TODO Auto-generated method stub
	}

	public void formSelected(Model m) {
		// TODO Auto-generated method stub
	}

	public void formsChanged(Model m) {
		// TODO Auto-generated method stub
	}

	public void studiesChanged(Model m) {
		// TODO Auto-generated method stub
	}

	public void studySelected(Model m) {
		// TODO Auto-generated method stub
	}
}
