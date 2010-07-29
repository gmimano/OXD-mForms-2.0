package org.openxdata.forms;


import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.openxdata.communication.TransportLayer;
import org.openxdata.db.OpenXdataDataStorage;
import org.openxdata.db.util.StorageListener;
import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.QuestionData;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.MenuText;


/** This is the main midlet that displays the main user inteface for epihandy. 
 * 
 * @author Daniel Kayiwa
 *
 */
public class MainForm extends MIDlet implements FormListener,StorageListener,AlertMessageListener,LogonListener, LogoutListener {
	
	/** Reference to the current display. */
	private Display display;
	
	/** The main menu screen. */
	//private List mainList;
	
	/** The main screen - either list of studies or forms. */
	private Displayable mainScreen;
	
	/** Index for selecting a study menu item. */
	//private static final int INDEX_SELECT_STUDY = 0;
	
	/** Index for selecting an encounter form menu item. */
	//private static final int INDEX_SELECT_FORM = 1;
	
	/** Index for downloading study list menu item. */
	//private static final int INDEX_DOWNLOAD_STUDY_LIST = 2;
	
	/** Index for downloading forms menu item. */
	//private static final int INDEX_DOWNLOAD_FORMS = 3;
	
	/** Index for uploading data menu item. */
	//private static final int INDEX_UPLOAD_DATA = 4;
	
	/** Index for specifying settings like server connection parameters. */
	//private static final int INDEX_SETTINGS = 5;
	
	/** Index for selecting a study menu item. */
	//private static final int INDEX_LOGOUT = 6;
	
	/** Reference to epihandy form manager. */
	private FormManager formMgr;
	
	/** Reference to the transportLayer. */
	private TransportLayer transportLayer;
	
	private AlertMessage alertMsg;
						
	/** The user manager object. */
	private UserManager userMgr;
	
	/** The current selected index of the main menu. For now, this is used to keep track of
	 * the user's action to return to after successfully logging in. This happens when the user
	 * tries to do something before logging in, and the logon mananer intervenes by requiring the
	 * user to first login. This happens after downloading forms because a new list of users is got
	 * which makes void the current users info. */
	//private int selectedIndex = OpenXdataConstants.NO_SELECTION;
		
	//private static final String KEY_LAST_SELECTED_MAIN_MENU_ITEM =  "LAST_SELECTED_MAIN_MENU_ITEM";
	
	private boolean exitConfirmMode = false;

	/** Construct the main UI midlet. */
	public MainForm() {
		super();
		
		display = Display.getDisplay(this);
		
		MenuText.setMenuTextList(OpenXdataDataStorage.getMenuText());

		transportLayer = new TransportLayer();
		transportLayer.setDisplay(display);
		// FIXME: these shouldn't be hardcoded
		transportLayer.setDefaultCommnucationParameter(TransportLayer.KEY_BLUETOOTH_SERVER_ID, /*"F0E0D0C0B0A000908070605040302010"*/ "F0E0D0C0B0A000908070605040301116");
		transportLayer.setDefaultCommnucationParameter(TransportLayer.KEY_HTTP_URL, "");
		transportLayer.setDefaultCommnucationParameter(TransportLayer.KEY_SMS_DESTINATION_ADDRESS,"sms://+256712330386"); //256782380638 "sms://+256782380638:1234"
		transportLayer.setDefaultCommnucationParameter(TransportLayer.KEY_SMS_SOURCE_ADDRESS,"sms://:1234"); 
	
		formMgr = new FormManager(MidletConstants.TITLE, display, this, null, transportLayer, null, this);
		FormManager.setGlobalInstance(formMgr);
		
		//if (GeneralSettings.isMainMenu()) {
		//	initMainList();
		//	alertMsg = new AlertMessage(this.display, MidletConstants.TITLE, mainList, this);
		//} else {
			mainScreen = formMgr.getPrevScreen(); // this was initialised for us
			alertMsg = new AlertMessage(this.display, MidletConstants.TITLE, mainScreen, this);
		//}
		
		OpenXdataDataStorage.storageListener = this;
	}
	
	/*private void initMainList(){
		mainList = new List(MidletConstants.TITLE, Choice.IMPLICIT);
		((List)mainList).setFitPolicy(List.TEXT_WRAP_ON);
		
		mainList.insert(INDEX_SELECT_STUDY, MenuText.SELECT_STUDY(), null);
		mainList.insert(INDEX_SELECT_FORM, MenuText.SELECT_FORM(), null);
		mainList.insert(INDEX_DOWNLOAD_STUDY_LIST, MenuText.DOWNLOAD_STUDIES(), null);
		mainList.insert(INDEX_DOWNLOAD_FORMS, MenuText.DOWNLOAD_FORMS(), null);
		mainList.insert(INDEX_UPLOAD_DATA, MenuText.UPLOAD_DATA(), null);
		mainList.insert(INDEX_SETTINGS, MenuText.SETTINGS(), null);
		mainList.insert(INDEX_LOGOUT, MenuText.LOGOUT(), null);

		mainList.addCommand(DefaultCommands.cmdSel);
		mainList.addCommand(DefaultCommands.cmdExit);
		
		Settings settings = new Settings(OpenXdataConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
		String val = settings.getSetting(KEY_LAST_SELECTED_MAIN_MENU_ITEM);
		if(val != null)
			mainList.setSelectedIndex(Integer.parseInt(val),true);
		else
			mainList.setSelectedIndex(INDEX_SELECT_FORM,true); //make select form the default
		
		mainList.setCommandListener(this);
	}*/

	protected void destroyApp(boolean arg0) {
	}

	protected void pauseApp() {
	}

	protected void startApp() {
		userMgr = new UserManager(display,mainScreen,MidletConstants.TITLE,this);
		userMgr.logOn();
		formMgr.setUserManager(userMgr);
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	/*public void commandAction(Command c, Displayable d) {
		try{
	        if (c == DefaultCommands.cmdExit)
	        	handledExitCommand();
	        else if(c == List.SELECT_COMMAND)
	        	handleListSelectCommand(((List)d).getSelectedIndex());
	        else if(c == DefaultCommands.cmdCancel)
	        	handledCancelCommand(d);
	        else if(c == DefaultCommands.cmdSel)
	        	handleOkCommand(d);
	        else if(c == DefaultCommands.cmdBack)
	        	handledBackCommand(d);
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
		}
    }*/
	
	/**
	 * Handles the back command.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handledBackCommand(Displayable d){
		handledCancelCommand(d);
	}*/
	
	/**
	 * Handles the cancel command.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handledCancelCommand(Displayable d){
    	this.display.setCurrent(mainList);
	}*/
	
	/**
	 * Handles the exit command.
	 *
	 */
	/*private void c(){
		exitConfirmMode = true;
		alertMsg.showConfirm(MenuText.EXIT_PROMPT());
	}*/
	
	/**
	 * Handles the list selection command.
	 * 
	 * @param selectedIndex - the index of the selected item.
	 */
	/*private void handleListSelectCommand(int selectedIndex){
		Displayable currentScreen = display.getCurrent();
		
		if(currentScreen == mainList)
			handleMainListSelectCommand(selectedIndex);
	}*/
	
	/**
	 * Handles the main list selection command.
	 * 
	 * @param selectedIndex - the index of the selected item.
	 */
	/*private void handleMainListSelectCommand(int selectedIndex){
		
		this.selectedIndex = selectedIndex;
		
		if(!userMgr.isLoggedOn()){
			userMgr.logOn();
			return;
		}
		
		switch(selectedIndex){
			case INDEX_SELECT_STUDY:
				this.formMgr.selectStudy(false);
				break;
	 		case INDEX_DOWNLOAD_STUDY_LIST:
				this.formMgr.downloadStudies(mainList);
				break;
			case INDEX_DOWNLOAD_FORMS:
				this.formMgr.downloadStudyForms(mainList,true);
				break;
			case INDEX_UPLOAD_DATA:
				this.formMgr.uploadData(mainList);
				break;
			case INDEX_SELECT_FORM:
				formMgr.selectForm(true, display.getCurrent());
				break;
			case INDEX_LOGOUT:
				logout();
				break;
			case INDEX_SETTINGS:
				formMgr.displayUserSettings(display, mainList);
				break;
		}
		
		Settings settings = new Settings(OpenXdataConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
		settings.setSetting(KEY_LAST_SELECTED_MAIN_MENU_ITEM, String.valueOf(selectedIndex));
		settings.saveSettings();
	}*/
	
	/**
	 * Handles the ok command.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	/*private void handleOkCommand(Displayable d){
		handleListSelectCommand(mainList.getSelectedIndex());
	}*/
	
		
	/**
	 * Called by the epihandy form manager when a form has been closed without saving.
	 * 
	 * @param data - the data in the form that has been cancelled.
	 */
	public void afterFormCancelled(FormData data){
		//Alert alert = new Alert("FormCancelled","The form has not been saved",null,AlertType.CONFIRMATION);
		//alert.setTimeout(Alert.FOREVER);
	}
	
	/**
	 * Called by the epihandy form manager when a form is about to be displayed.
	 * 
	 * @param data - the data in the form that is to be displayed.
	 * 
	 */
	public boolean beforeFormDisplay(FormData data){
		return true;
	}
	
	public boolean beforeFormDataListDisplay(FormDef formDef){		
		return true;
	}
	
	/***
	 * Called just immediately after a form has been displayed.
	 * This can be useful for form level constraints which u can
	 * use to determine whether to display a form or not.
	 * 
	 * @param data the data contained in the form about to be displayed.
	 * @param save a flag whose value determines whether we go ahead and display the form or not.
	 */
	public void afterFormDisplay(FormData data, boolean save){
		
	}
	
	/**
	 * Called just before a question is displayed.
	 * 
	 * @param data
	 * @param edit
	 */
	public boolean beforeQuestionEdit(QuestionData data){
		return true;
	}
	
	/**
	 * @see org.fcitmuk.epihandy.midp.forms.FormListener#afterFormSaved(org.fcitmuk.epihandy.FormData,java.lang.boolean)
	 */
	public void afterFormSaved(FormData formData, boolean isNew){		
		//alertMsg.show("Form Saved Successfully.");
	}

	/**
	 * Called after a question has been displayed.
	 */
	public boolean afterQuestionEdit(QuestionData data){
		return true;
	}

	public boolean beforeFormCancelled(FormData data){
		return true;
	}
	
	public boolean beforeFormSaved(FormData formData,boolean isNew){
		return true;
	}
	
	/**
	 * Could be useful for user confirmation before form deletion.
	 * 
	 * @param data the data on the form about to be deleted.
	 * @param delete a flag to determine whether
	 */
	public boolean beforeFormDelete(FormData data){
		return true;
	}
	
	/**
	 * Called after a form has been deleted.
	 * By the time this is called, there is no chance to cancel deleting of the form.
	 * 
	 * @param data the data in the form about to be deleted.
	 */
	public void afterFormDelete(FormData data){
	}
		
	/**
	 * Called when an error occurs during any operation.
	 * 
	 * @param errorMessage - the error message.
	 * @param e - the exception, if any, that did lead to this error.
	 */
	public void errorOccured(String errorMessage, Exception e){
		if(e != null && e.getMessage() != null)
			errorMessage += " : "+ e.getMessage();
		alertMsg.showError(errorMessage);
	}
	
	/*public void cancelled(){
		display.setCurrent(mainList);
	}*/
	
	public void onAlertMessage(byte msg){
		if(exitConfirmMode){
			if(msg == AlertMessageListener.MSG_OK)
				exit();
			else
				alertMsg.turnOffAlert();
			
			exitConfirmMode = false;
		}
		else
			alertMsg.turnOffAlert();
	}
	
	private void exit(){
		destroyApp(false);
        notifyDestroyed();
	}
	
	public boolean onLoggedOn() {
		if (GeneralSettings.isHideStudies()) {
			formMgr.selectForm(true, display.getCurrent());
		} else {
			formMgr.selectStudy(false);
		}
		/*boolean displayPrevScreen = false;
		if(selectedIndex != OpenXdataConstants.NO_SELECTION)
			handleMainListSelectCommand(selectedIndex);
		else
			displayPrevScreen = true;
		
		return displayPrevScreen;*/
		return false;
	}
	
	public void onLogonCancel(){
		/*if(selectedIndex == OpenXdataConstants.NO_SELECTION)
			exit();
		else
			display.setCurrent(mainList);*/
		exit();
	}
	
	
	/*private void logout(){
		// If this is not reset, after loggin in, we shall wrongly execute an action that the user did not intend to.
		//this.selectedIndex = OpenXdataConstants.NO_SELECTION;
		
		userMgr.logOut();
		userMgr.logOn();
	}*/
	
	public boolean beforeFormDefListDisplay(Vector formDefList){
		return true;
	}

	public void onLogout() {
		exitConfirmMode = true;
		alertMsg.showConfirm(MenuText.EXIT_PROMPT());
	}
}

/*private String test(){

//First Set criteria for selecting a location provider:
Criteria cr= new Criteria();
cr.setHorizontalAccuracy(500);  // accurate to 500 meters horizontally

try {
	// Now get an instance of the provider
	LocationProvider lp= LocationProvider.getInstance(cr);
	
	// Request the location, setting a one-minute timeout
	Location l = lp.getLocation(60);
	Coordinates c = l.getQualifiedCoordinates();
	
	if(c != null ) {
		// Get coordinate information
		double lat = c.getLatitude();
		double lon = c.getLongitude();

		// Now recenter map to given location, zoom to street level
		mapItem.setMiddlePoint(new WgsPoint(lon,lat), 12);
	}	
	    
} catch (Exception e) {
}

String s = "NULL";
try {

   // Create a Criteria object for defining desired selection criteria
   Criteria cr = new Criteria();
   // Specify horizontal accuracy of 500 meters, leave other parameters 
   // at default values.
   cr.setHorizontalAccuracy(500);
  
   LocationProvider lp = LocationProvider.getInstance(cr);

   // get the location, one minute timeout
   Location l = lp.getLocation(60);

   Coordinates c = l.getQualifiedCoordinates();
   
   if (c != null) {
      //s = "ALT="+c.getAltitude();
      //s = " LAT="+c.getLatitude();
      //s = " LONG="+c.getLongitude();
   }
} catch (Exception e) {
  s = e.getMessage();
} 

return s;
}*/
