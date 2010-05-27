package org.openxdata.forms;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.db.OpenXdataDataStorage;
import org.openxdata.db.util.Settings;
import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.StudyDef;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;


/**
 * Display a list of form defintions.
 * 
 * @author Daniel Kayiwa.
 *
 */
public class FormDefListView extends AbstractView implements AlertMessageListener{

	private StudyDef studyDef;
	private AlertMessage alertMsg;
	private int lastSelectionIndex = 0;

	private final byte CA_NONE = 0;
	private final byte CA_ERROR = 3;

	private byte currentAction = CA_NONE;

	private static final String KEY_LAST_SELECTED_FORMDEF =  "LAST_SELECTED_FORMDEF";
	private Vector formDefList;


	public FormDefListView(){

	}

	public void showFormList(FormListener formListener){
		showFormList(studyDef,formListener);
	}

	/**
	 * Displays the list of forms in a study.
	 * 
	 * @param studyId - the numeric unique identifier of the study.
	 */
	public void showFormList(StudyDef currentStudy, FormListener formListener){
		studyDef = currentStudy;

		screen = new List(MenuText.SELECT_FORM() + " - "+title , Choice.IMPLICIT);
		((List)screen).setFitPolicy(List.TEXT_WRAP_ON);
		
		alertMsg = new AlertMessage(display, title, screen, this);

		try{
			if(currentStudy == null)
				alertMsg.show(MenuText.NO_SELECTED_STUDY());
			else{
				formDefList = copyFormDefs(currentStudy.getForms());
				if(formDefList != null && formDefList.size() > 0){		
					boolean showList = true;
					if(formListener != null)
						showList = formListener.beforeFormDefListDisplay(formDefList);

					if(showList){
						for(int i=0; i<formDefList.size(); i++)
							((List)screen).append(((FormDef)formDefList.elementAt(i)).getName(), null);

						screen.setCommandListener(this);
						screen.addCommand(DefaultCommands.cmdSel);
						screen.addCommand(DefaultCommands.cmdBack);

						Settings settings = new Settings(OpenXdataConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
						String val = settings.getSetting(KEY_LAST_SELECTED_FORMDEF);
						if(val != null)
							lastSelectionIndex = Integer.parseInt(val);

						if(lastSelectionIndex < formDefList.size())
							((List)screen).setSelectedIndex(lastSelectionIndex, true);

						display.setCurrent(screen);
					}
				}
				else
					alertMsg.show(MenuText.NO_STUDY_FORMS());
			}
		}
		catch(Exception e){
			alertMsg.showError("Prrr"+ e.getMessage());
		}
	}

	private Vector copyFormDefs(Vector formDefs){
		Vector forms = new Vector();
		for(int i=0; i<formDefs.size(); i++)
			forms.addElement(formDefs.elementAt(i));
		return forms;
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
			getOpenXdataController().handleCancelCommand(this);
	}

	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleBackCommand(Displayable d){
	*/

	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){
		try{
			int studyId = getStudy().getId();			
			FormDef fdef = (FormDef)formDefList.elementAt(lastSelectionIndex);
			Vector formData = OpenXdataDataStorage.getFormData(studyId, fdef.getId());
			if(formData != null && !formData.isEmpty()){
				getOpenXdataController().showFormDataList((FormDef)formDefList.elementAt(lastSelectionIndex));
				Settings settings = new Settings(OpenXdataConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
				settings.setSetting(KEY_LAST_SELECTED_FORMDEF, String.valueOf(lastSelectionIndex));
				settings.saveSettings();				
			}else{
				FormDef fd = (FormDef)formDefList.elementAt(lastSelectionIndex);
				getOpenXdataController().showForm(true, new FormData(fd), false, this.getPrevScreen());
			}
			
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
				getOpenXdataController().handleCancelCommand(this);

			currentAction = CA_NONE;
		}
		else
			getOpenXdataController().handleCancelCommand(this);
	}

	public void setStudy(StudyDef study){
		studyDef = study;
	}

	public StudyDef getStudy(){
		return studyDef;
	}

	private OpenXdataController getOpenXdataController(){
		return (OpenXdataController)controller;
	}
}
