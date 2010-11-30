package org.fcitmuk.epihandy.midp.forms;


import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.fcitmuk.communication.TransportLayer;
import org.fcitmuk.communication.TransportLayerListener;
import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.epihandy.QuestionData;
import org.fcitmuk.epihandy.QuestionDef;
import org.fcitmuk.midp.mvc.AbstractView;
import org.fcitmuk.util.DefaultCommands;
import org.fcitmuk.util.MenuText;


/** 
 * Handles display of forms for data entry. This class acts as a facade to the epihandy forms engine.
 * This class uses the epihandy controller to manage view interactions with the user.
 * It also handles security issues.
 * 
 * @author Daniel Kayiwa
 *
 */
public class FormManager implements TransportLayerListener{
	
	/** The screen to display after all our screens have been closed.
	 * This, for the user of this class, would be the current screen displayed
	 * before a method of this class is called.
	 */
	private Displayable prevScreen;
	
	private DownloadUploadManager downloadMgr;
	private EpihandyController controller;
	private UserManager userMgr;
	TransportLayer transportLayer;
	TransportLayerListener transportLayerListener;
	
	private static FormManager instance;
	
	
	public static FormManager getInstance(){
		return instance;
	}
	
	//TODO This is a temporary hack and should be dealt with smartly.
	public static void setGlobalInstance(FormManager formManager){
		instance = formManager;
	}
	
	/**
	 * Creates a new instance of form manager.
	 * 
	 * @param title - the title of the application. This is to be used for titles like in alerts.
	 * @param display - a reference to the display.
	 * @param formEventListener - a listener to the form events.
	 * @param currentScreen - the screen currently displayed.
	 * @param transportLayer - a reference to the transportLayer object.
	 * @param transportLayerListener - a reference to the listener to transport layer events.
	 */
	public FormManager(String title,Display display, FormListener formEventListener, Displayable currentScreen,
			TransportLayer transportLayer, TransportLayerListener transportLayerListener,
			LogoutListener logoutListener){

		this.prevScreen = currentScreen;
		this.transportLayerListener = transportLayerListener;
		this.transportLayer = transportLayer;
		AbstractView.display = display;
				
		controller = new EpihandyController();
		controller.init(title, display, currentScreen, transportLayer, logoutListener);
		//controller.init(title, display, formEventListener, currentScreen, transportLayer, logoutListener);
		if (currentScreen == null) {
			// it will be initialised by the controller if it was null
			this.prevScreen = controller.getPrevScreen();
			transportLayer.setPrevScreen(this.prevScreen);
		}
		
		//register repeat type editor;
		RepeatTypeEditor rptEditor = new RepeatTypeEditor();
		rptEditor.setController(controller);
		controller.setTypeEditor(QuestionDef.QTN_TYPE_REPEAT, rptEditor);
		
		//register multimedia type editor
		MultmediaTypeEditor mmEditor = new MultmediaTypeEditor();
		mmEditor.setController(controller);
		controller.setTypeEditor(QuestionDef.QTN_TYPE_IMAGE, mmEditor);
		controller.setTypeEditor(QuestionDef.QTN_TYPE_VIDEO, mmEditor);
		controller.setTypeEditor(QuestionDef.QTN_TYPE_AUDIO, mmEditor);
		
		//register GPS type editor if it is available on the phone, otherwise use the default GPS one
		controller.setTypeEditor(QuestionDef.QTN_TYPE_GPS, GPSTypeEditor.getGPSTypeEditor());
		
		downloadMgr = new DownloadUploadManager(transportLayer, controller, title, this);
		userMgr = new UserManager(display,prevScreen,title,null, downloadMgr);
		
		((EpihandyController)controller).setDownloadManager(downloadMgr);
		((EpihandyController)controller).setUserManager(userMgr);
		((EpihandyController)controller).setFormManager(this);
		
		if(GeneralSettings.isOkOnRight()){
			DefaultCommands.cmdOk = new Command(MenuText.OK(), Command.CANCEL, 1);
			DefaultCommands.cmdSave = new Command(MenuText.SAVE(),Command.CANCEL,2);
		}
		
		QuestionData.dateDisplayFormat = DateSettings.getDateFormat();
	}

	public UserManager getUserManager() {
		return userMgr;
	}
	
	/**
	 * Sets a custom editor of a question type.
	 * 
	 * @param type - the question type.
	 * @param typeEditor - the editor.
	 */
	public void setTypeEditor(byte type, TypeEditor typeEditor){
		controller.setTypeEditor(type, typeEditor);
	}
	
	private boolean isUserLoggedOn(){
		if(userMgr.isLoggedOn())
			return true;
		else
			userMgr.logOn();
		return false;
	}

	/**
	 * Displays a list of studies.
	 *
	 *@param forEditing - when true, means that if a study is selected, 
	 *					  we should display its forms for editing.
	 */
	public void selectStudy(boolean forEditing){
		if(isUserLoggedOn())
			this.controller.selectStudy(forEditing);
	}
	
	public void downloadLanguages(Displayable currentScreen, boolean confirm){
		if(isUserLoggedOn())
			downloadMgr.downloadLanguages(currentScreen,userMgr.getUserName(), userMgr.getPassword(),confirm);
	}
	
	public void downloadMenuText(Displayable currentScreen, boolean confirm){
		if(isUserLoggedOn())
			downloadMgr.downloadMenuText(currentScreen,userMgr.getUserName(),userMgr.getPassword(),confirm);
	}
	
	public void uploaded(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut){
		if(transportLayerListener != null)
			transportLayerListener.uploaded(dataInParams, dataIn, dataOutParams, dataOut);
	}
	
	public void downloaded(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut){
		if(transportLayerListener != null)
			transportLayerListener.downloaded(dataInParams, dataIn, dataOutParams, dataOut);
	}
	
	public void errorOccured(String errorMessage, Exception e){
		if(transportLayerListener != null)
			transportLayerListener.errorOccured(errorMessage, e);
	}
	
	public void cancelled(){
		if(transportLayerListener != null)
			transportLayerListener.cancelled();
	}
	
	public void displayUserSettings(Display display, Displayable prevScreen){
		UserSettings userSettings = new UserSettings();
		userSettings.display(display, prevScreen, transportLayer,userMgr.getUserName(),userMgr.getPassword());
	}
	
	public void updateCommunicationParams(){
		
	}
	
	public void restorePrevScreen(){
		downloadMgr.setPrevSrceen(transportLayer.getPrevScreen());
	}

	public Displayable getPrevScreen() {
		return this.prevScreen;
	}
}
