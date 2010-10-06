package org.fcitmuk.epihandy.midp.forms;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import org.fcitmuk.epihandy.midp.db.StoredFormDef;
import org.fcitmuk.epihandy.midp.model.Model;
import org.fcitmuk.epihandy.midp.model.ModelListener;
import org.fcitmuk.midp.mvc.AbstractView;
import org.fcitmuk.util.AlertMessage;
import org.fcitmuk.util.AlertMessageListener;
import org.fcitmuk.util.DefaultCommands;
import org.fcitmuk.util.MenuText;


/**
 * Display a list of form defintions.
 * 
 * @author Daniel Kayiwa.
 *
 */
public class FormDefListView extends AbstractView implements AlertMessageListener, ModelListener {

	private AlertMessage alertMsg;
	private int lastSelectionIndex = 0;

	private final byte CA_NONE = 0;
	private final byte CA_ERROR = 3;

	private byte currentAction = CA_NONE;

	Model model;
	
	public FormDefListView(Model model) {
		List list = new List("", Choice.IMPLICIT);
		list.setFitPolicy(List.TEXT_WRAP_ON);
		screen = list;
		this.model = model;
		model.addModelListener(this);
		
		screen.setCommandListener(this);
		//if (GeneralSettings.isHideStudies()) {
		//	screen.addCommand(DefaultCommands.cmdExit);
		//} else {
			screen.addCommand(DefaultCommands.cmdBack);
		//}
		screen.addCommand(DefaultCommands.cmdSel);
		screen.addCommand(DefaultCommands.cmdDownloadForm);
		//if (GeneralSettings.isHideStudies()) {
		//	screen.addCommand(DefaultCommands.cmdUploadData);
		//}
		screen.addCommand(DefaultCommands.cmdSettings);
	}

	/**
	 * Displays the list of forms in a study.
	 * 
	 * @param studyId - the numeric unique identifier of the study.
	 */
	public void showFormList() {
		
		((List) screen).setTitle(MenuText.SELECT_FORM() + " - " + title);
		alertMsg = new AlertMessage(display, title, screen, this);
		
		try{
			if(model.getSelectedStudyDef() == null)
				alertMsg.show(MenuText.NO_SELECTED_STUDY());
			else{
				StoredFormDef [] formDefs = model.getStudyForms();
				if(formDefs != null && formDefs.length > 0) {
					display.setCurrent(screen);
				} else {
					alertMsg.show(MenuText.NO_STUDY_FORMS());
				}
			}
		}
		catch(Exception e){
			alertMsg.showError("Prrr"+ e.getMessage());
		}
	}
	
	private void selectItem(int selection) {
		List list = (List) screen;
		if (selection >= 0 && selection < list.size()) {
			list.setSelectedIndex(selection, true);
		}
	}
	
	private void updateList(StoredFormDef[] formDefs) {

		((List) screen).deleteAll();

		if (formDefs != null) {
			for (int i = 0; i < formDefs.length; i++) {
				StoredFormDef formDef = formDefs[i];
				int studyId = model.getSelectedStudyDef().getId();
				Image img = null;
				if (model.formDefInError(studyId, formDef.getId()))
					img = Images.ERROR_IMAGE;
				else if (model.storedDataExistsForFormDef(studyId, formDef
						.getId()))
					img = Images.HASDATA_IMAGE;
				((List) screen).append(formDef.getName(), img);
			}
			selectItem(model.getSelectedFormIndex());
		}
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		if(c == DefaultCommands.cmdSel || c == List.SELECT_COMMAND)
			handleOkCommand(d);
		else if(c == DefaultCommands.cmdBack)
			getEpihandyController().handleCancelCommand(this);
		else if (c == DefaultCommands.cmdSettings) {
			getEpihandyController().displayUserSettings(this.getScreen());
		}
		else if (c == DefaultCommands.cmdDownloadForm) {
			getEpihandyController().downloadStudyForms(this.getScreen());
		}
		else if (c == DefaultCommands.cmdUploadData) {
			getEpihandyController().uploadData(this.getScreen());
		}
		else if (c == DefaultCommands.cmdExit) {
			getEpihandyController().logout();
		}
	}

	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){
		try{
			lastSelectionIndex = ((List)d).getSelectedIndex();
			getEpihandyController().showFormDataList(lastSelectionIndex);
		}
		catch(Exception ex){
			//TODO Looks like we should help the user out of this by say enabling them
			//delete any existing data.
			String s = MenuText.FORM_DATA_DISPLAY_PROBLEM();
			if(ex.getMessage() != null && ex.getMessage().trim().length() > 0)
				s = ex.getMessage();
			currentAction = CA_ERROR;
			alertMsg.showError(s);
			//ex.printStackTrace();
		}
	}

	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			if(currentAction == CA_ERROR)
				show();
			else
				getEpihandyController().handleCancelCommand(this);

			currentAction = CA_NONE;
		}
		else
			getEpihandyController().handleCancelCommand(this);
	}

	private EpihandyController getEpihandyController(){
		return (EpihandyController)controller;
	}

	public void formSelected(Model m) {
		((List) screen).setSelectedIndex(m.getSelectedFormIndex(), true);
	}

	public void formsChanged(Model m) {
		updateList(model.getStudyForms());
	}

	public void studiesChanged(Model m) {
		// Don't care about studies changing, only forms
	}

	public void studySelected(Model m) {
		// Don't care about study selection, only forms
	}

	public void formDataChanged(Model m) {
		updateList(model.getStudyForms());
	}

	public void formErrorsChanged(Model m) {
		updateList(model.getStudyForms());
	}

	public void formDataSelected(Model m) {
		// Don't care about which form data is selected
	}
}
