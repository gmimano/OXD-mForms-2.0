package org.fcitmuk.epihandy.midp.forms;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.fcitmuk.epihandy.EpihandyConstants;
import org.fcitmuk.epihandy.FormDef;
import org.fcitmuk.epihandy.StudyDef;
import org.fcitmuk.midp.db.util.Settings;
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

						Settings settings = new Settings(EpihandyConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
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
			getEpihandyController().handleCancelCommand(this);
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
			lastSelectionIndex = ((List)d).getSelectedIndex();
			getEpihandyController().showFormDataList((FormDef)formDefList.elementAt(lastSelectionIndex)/*(FormDef)studyDef.getForms().elementAt(lastSelectionIndex)*/);

			Settings settings = new Settings(EpihandyConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
			settings.setSetting(KEY_LAST_SELECTED_FORMDEF, String.valueOf(lastSelectionIndex));
			settings.saveSettings();
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

	public void setStudy(StudyDef study){
		studyDef = study;
	}

	public StudyDef getStudy(){
		return studyDef;
	}

	private EpihandyController getEpihandyController(){
		return (EpihandyController)controller;
	}
}
