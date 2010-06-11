package org.openxdata.forms;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.openxdata.db.util.Settings;
import org.openxdata.model.OpenXdataConstants;
import org.openxdata.model.StudyDef;
import org.openxdata.mvc.AbstractView;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;


/**
 * 
 * @author daniel
 *
 */
public class StudyListView extends AbstractView implements CommandListener  {
	private Vector studyList;

	public StudyListView(){

	}

	public void showStudyList(Vector studyList){
		this.studyList = studyList;

		screen = new List(MenuText.SELECT_STUDY()+" - "+title , Choice.IMPLICIT);
		((List)screen).setFitPolicy(List.TEXT_WRAP_ON);

		StudyDef study; int selectedIndex = OpenXdataConstants.NO_SELECTION;
		Settings settings = new Settings(OpenXdataConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
		String val = settings.getSetting(OpenXdataConstants.KEY_LAST_SELECTED_STUDY);

		for(int i=0; i<studyList.size(); i++){
			study = (StudyDef)studyList.elementAt(i);
			if(selectedIndex == OpenXdataConstants.NO_SELECTION && val != null){
				if(study.getId() == Byte.parseByte(val))
					selectedIndex = i;
			}

			((List)screen).append(study.getName(), null);
		}

		if(selectedIndex != OpenXdataConstants.NO_SELECTION)
			((List)screen).setSelectedIndex(selectedIndex, true);

		screen.setCommandListener(this);
		screen.addCommand(DefaultCommands.cmdOk);
		screen.addCommand(DefaultCommands.cmdCancel);
		display.setCurrent(screen);
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			//This block is commented out to have the next block from MoTeCH
			/*if(c == List.SELECT_COMMAND || c == DefaultCommands.cmdOk)
				getOpenXdataController().execute(this,DefaultCommands.cmdOk,(StudyDef)studyList.elementAt(((List)d).getSelectedIndex()));
			else if(c == DefaultCommands.cmdCancel)
				getOpenXdataController().execute(this,DefaultCommands.cmdCancel,null);*/

			OpenXdataController controller = getOpenXdataController();
			if (c == List.SELECT_COMMAND || c == DefaultCommands.cmdOk) {
				StudyDef selectedStudy = (StudyDef) studyList.elementAt(((List) d).getSelectedIndex());
				StudyDef currentStudy = controller.getCurrentStudy();
				if (selectedStudy != null && selectedStudy.getId() != currentStudy.getId())
					controller.execute(this, DefaultCommands.cmdOk, selectedStudy);
			} 
			else if (c == DefaultCommands.cmdCancel)
				controller.execute(this, DefaultCommands.cmdCancel, null);
		}
		catch(Exception e){
			//alertMsg.showError(e.getMessage());
			//e.printStackTrace();
		}
	}

	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleCancelCommand(Displayable d){
		getEpihandyController().execute(this,DefaultCommands.cmdCancel,null);
	}

	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleOkCommand(Displayable d){
		StudyDef study = (StudyDef)studyList.elementAt(((List)d).getSelectedIndex());
		getEpihandyController().execute(this,DefaultCommands.cmdOk,study);
	}*/

	public void setStudyList(Vector list){
		studyList = list;
	}

	public Vector getStudyList(){
		return studyList;
	}

	private OpenXdataController getOpenXdataController(){
		return (OpenXdataController)controller;
	}
}
