package org.fcitmuk.epihandy.midp.forms;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import org.fcitmuk.midp.db.util.Settings;
import org.fcitmuk.midp.mvc.AbstractView;
import org.fcitmuk.util.DefaultCommands;
import org.fcitmuk.util.MenuText;
import org.fcitmuk.util.Utilities;

public class GeneralSettings extends AbstractView{

	private static final String KEY_SINGLE_QUESTION_EDIT = "SINGLE_QUESTION_EDIT";
	public static final String KEY_QUESTION_NUMBERING = "QUESTION_NUMBERING";
	public static final String KEY_OK_ON_RIGHT = "OK_ON_RIGHT";
	public static final String KEY_DELETE_DATA_AFTER_UPLOAD = "DELETE_DATA_AFTER_UPLOAD";
	public static final String STORAGE_NAME_SETTINGS = "fcitmuk.GeneralSettings";
	public static final String KEY_AUTO_SAVE = "KEY_AUTO_SAVE";
	
	private ChoiceGroup currentCtrl;
	
	public void display(Display display, Displayable prevScreen){
		
		setDisplay(display);
		setPrevScreen(prevScreen);
		
		screen = new Form(MenuText.SETTINGS());
		currentCtrl = new ChoiceGroup(MenuText.SETTINGS(),Choice.MULTIPLE);
			
		screen.addCommand(DefaultCommands.cmdOk);
		screen.addCommand(DefaultCommands.cmdCancel);
		
		currentCtrl.append(MenuText.SINGLE_QUESTION_EDIT(), null);
		currentCtrl.append(MenuText.NUMBERING(), null);
		currentCtrl.append(MenuText.OK_ON_RIGHT(), null);
		currentCtrl.append(MenuText.DELETE_AFTER_UPLOAD(), null);
		currentCtrl.append(MenuText.AUTO_SAVE(), null);
		
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		currentCtrl.setSelectedIndex(0,Utilities.stringToBoolean(settings.getSetting(KEY_SINGLE_QUESTION_EDIT)));
		currentCtrl.setSelectedIndex(1,Utilities.stringToBoolean(settings.getSetting(KEY_QUESTION_NUMBERING)));
		currentCtrl.setSelectedIndex(2,Utilities.stringToBoolean(settings.getSetting(KEY_OK_ON_RIGHT)));
		currentCtrl.setSelectedIndex(3,Utilities.stringToBoolean(settings.getSetting(KEY_DELETE_DATA_AFTER_UPLOAD),true));
		currentCtrl.setSelectedIndex(4,Utilities.stringToBoolean(settings.getSetting(KEY_AUTO_SAVE), false));
		
		screen.setCommandListener(this);
		((Form)screen).append(currentCtrl);
				
		AbstractView.display.setCurrent(screen);
	}
	
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == DefaultCommands.cmdOk)
				handleOkCommand(d);
			else if(c == DefaultCommands.cmdCancel)
				handleCancelCommand(d);
		}
		catch(Exception e){
			//alertMsg.showError(e.getMessage());
			//e.printStackTrace();
		}
	}
	

	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){

		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		settings.setSetting(KEY_SINGLE_QUESTION_EDIT,Utilities.booleanToString((currentCtrl.isSelected(0))));
		settings.setSetting(KEY_QUESTION_NUMBERING,Utilities.booleanToString((currentCtrl.isSelected(1))));
		settings.setSetting(KEY_OK_ON_RIGHT,Utilities.booleanToString((currentCtrl.isSelected(2))));
		settings.setSetting(KEY_DELETE_DATA_AFTER_UPLOAD,Utilities.booleanToString((currentCtrl.isSelected(3))));
		settings.setSetting(KEY_AUTO_SAVE, Utilities.booleanToString((currentCtrl.isSelected(4))));
		settings.saveSettings();
		
		DefaultCommands.cmdOk = new Command(MenuText.OK(), currentCtrl.isSelected(3) ? Command.CANCEL : Command.OK, 1);
		
		display.setCurrent(getPrevScreen());
	}
	
	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleCancelCommand(Displayable d){
		display.setCurrent(getPrevScreen());
	}
	
	public static boolean isSingleQtnEdit(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		return Utilities.stringToBoolean(settings.getSetting(KEY_SINGLE_QUESTION_EDIT));
	}
	
	public static boolean isOkOnRight(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		return Utilities.stringToBoolean(settings.getSetting(KEY_OK_ON_RIGHT));
	}

	public static boolean isQtnNumbering() {
		Settings settings = new Settings(STORAGE_NAME_SETTINGS, true);
		return Utilities.stringToBoolean(settings
				.getSetting(KEY_QUESTION_NUMBERING));
	}

	public static boolean deleteDataAfterUpload(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		return Utilities.stringToBoolean(settings.getSetting(KEY_DELETE_DATA_AFTER_UPLOAD),true);
	}
	
	public static void setDeleteDataAfterUpload(boolean delete){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		settings.setSetting(KEY_DELETE_DATA_AFTER_UPLOAD,Utilities.booleanToString(delete));
	}

	public static boolean isAutoSave() {
		Settings settings = new Settings(KEY_AUTO_SAVE, true);
		return Utilities.stringToBoolean(settings.getSetting(KEY_AUTO_SAVE), false);
	}
}
