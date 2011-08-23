package org.fcitmuk.epihandy.midp.forms;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import org.fcitmuk.epihandy.FormData;
import org.fcitmuk.epihandy.FormDef;
import org.fcitmuk.epihandy.midp.model.Model;
import org.fcitmuk.epihandy.midp.model.ModelListener;
import org.fcitmuk.midp.mvc.AbstractView;
import org.fcitmuk.util.AlertMessage;
import org.fcitmuk.util.AlertMessageListener;
import org.fcitmuk.util.DefaultCommands;
import org.fcitmuk.util.MenuText;


/**
 * Display a list of data collected forms.
 * 
 * @author Daniel Kayiwa.
 *
 */
public class FormDataListView extends AbstractView implements AlertMessageListener, ModelListener {	

	private boolean deleting = false;
	private AlertMessage alertMsg;

	private final byte CA_NONE = 0;
	private final byte CA_ERROR = 1;

	private byte currentAction = CA_NONE;
	
	private Model model;

	public FormDataListView(Model model) {
		model.addModelListener(this);
		this.model = model;
		List list = new List("", Choice.IMPLICIT);
		list.setFitPolicy(List.TEXT_WRAP_ON);
		screen = list;
		alertMsg = new AlertMessage(display, title, screen, this);
		screen.setCommandListener(this);
		screen.addCommand(DefaultCommands.cmdNew);
		screen.addCommand(DefaultCommands.cmdBack);
		screen.addCommand(DefaultCommands.cmdUploadData);
	}

	/**
	 * Displays data collected for a form type.
	 * 
	 * @param def - the form definition.
	 */
	public void showFormList(){
		display.setCurrent(screen);
	}
	
	private void updateList() {

		FormDef formDef = model.getActiveForm();
		int studyId = model.getSelectedStudyDef().getId();
		int formId = model.getSelectedFormDef().getId();
		Vector formDataIds = model.getFormDataIds(studyId, formId);

		if (formDef == null)
			return;

		List dataListUI = (List) screen;
		dataListUI.deleteAll();
		dataListUI.setTitle(formDef.getName() + " - " + MenuText.DATA_LIST()
				+ " - " + title);

		if (formDataIds != null) {
			for (int i = 0; i < formDataIds.size(); i++) {
				int dataRecId = ((Integer)formDataIds.elementAt(i)).intValue();
				FormData data = model.getFormData(studyId, formId, dataRecId);
				data.setDef(formDef);
				data.buildDataDescription();
				Image errorImage = null;
				if (model.formDataInError(studyId, formDef.getId(), data
						.getRecordId()))
					errorImage = Images.ERROR_IMAGE;
				dataListUI.append(data.toString(), errorImage);
			}
		}

		if (formDataIds != null && formDataIds.size() > 0)
			screen.addCommand(DefaultCommands.cmdDelete);
		else
			screen.removeCommand(DefaultCommands.cmdDelete);
	}

	private void selectItem(int selection) {
		List list = (List) screen;
		if (selection >= 0 && selection < list.size()) {
			list.setSelectedIndex(selection, true);
		}
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == DefaultCommands.cmdOk || c == List.SELECT_COMMAND)
				getEpihandyController().showForm(((List)d).getSelectedIndex());
			else if(c == DefaultCommands.cmdBack)
				getEpihandyController().handleCancelCommand(this);
			else if(c == DefaultCommands.cmdNew)
				getEpihandyController().showForm(-1);
			else if(c == DefaultCommands.cmdDelete)
				handleDeleteCommand(d);
			else if (c == DefaultCommands.cmdUploadData) {
				getEpihandyController().uploadData(this.getScreen(), 
						model.getSelectedStudyDef().getId(), 
						model.getSelectedFormDef().getId(),
						model.getFormDataByPosition(((List)d).getSelectedIndex()).getRecordId());
			}
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
		}
	}

	/**
	 * Processes the delete command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleDeleteCommand(Displayable d){	
		int selection = ((List)screen).getSelectedIndex();
		FormData formData = model.getFormDataByPosition(selection);
		alertMsg.showConfirm(MenuText.FORM_DELETE_PROMPT() + " " + formData.toString());
		deleting = true;
	}

	public void deleteCurrentForm(){
		int selection = ((List)screen).getSelectedIndex();
		FormData formData = model.getFormDataByPosition(selection);
		getEpihandyController().deleteForm(formData);
	}
	
	public boolean hasSelectedForm(){
		return ((List)screen).getSelectedIndex() > 0;
	}

	public void onFormSaved(FormData formData, boolean isNew) {
		if (model.isSelectedStudyFull())
			alertMsg.show(MenuText.FORM_SAVE_STUDY_FULL());
		else
			alertMsg.show(MenuText.FORM_SAVE_SUCCESS());
	}

	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			if(deleting){
				deleting = false;
				deleteCurrentForm();
				return;
			}
			else if(currentAction == CA_ERROR){
				getEpihandyController().handleCancelCommand(this);
				return;
			}
		}
		
		display.setCurrent(screen);
	}

	private EpihandyController getEpihandyController(){
		return (EpihandyController)controller;
	}

	public void formSelected(Model m) {
		// Don't care, we look for form data changed
	}

	public void formsChanged(Model m) {
		// Don't care, we look for form data changed
	}

	public void studiesChanged(Model m) {
		// Don't care, we look for form data changed
	}

	public void studySelected(Model m) {
		// Don't care, we look for form data changed
	}

	public void formDataChanged(Model m) {
		updateList();
	}

	public void formErrorsChanged(Model m) {
		updateList();
	}

	public void formDataSelected(Model m) {
		selectItem(model.getSelectedFormDataIndex());
	}
}
