package org.openxdata.mforms.forms;


import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.openxdata.communication.TransportLayer;
import org.openxdata.communication.TransportLayerListener;
import org.openxdata.db.util.Persistent;
import org.openxdata.mforms.RequestHeader;
import org.openxdata.mforms.midp.db.EpihandyDataStorage;
import org.openxdata.mforms.midp.forms.FormManager;
import org.openxdata.mforms.midp.forms.LogonListener;
import org.openxdata.mforms.midp.forms.LogoutListener;
import org.openxdata.mforms.midp.forms.UserManager;
import org.openxdata.midp.db.util.StorageListener;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.MenuText;


/** This is the main midlet entry point into the openXdata mobile application
 * 
 * @author Daniel Kayiwa
 * @author dagmar@cell-life.orgs
 *O
 */
public class MainForm extends MIDlet implements StorageListener,AlertMessageListener,LogonListener,LogoutListener,TransportLayerListener {
	
	/** Reference to the current display. */
	private Display display;
	
	/** The main screen - either list of studies or forms. */
	private Displayable mainScreen;
	
	/** Application tittle. */
	private String title;
	
	/** Reference to epihandy form manager. */
	private FormManager formMgr;
	
	/** Reference to the transportLayer. */
	private TransportLayer transportLayer;
	
	private AlertMessage alertMsg;
						
	/** The user manager object. */
	private UserManager userMgr;
	
	private boolean exitConfirmMode = false;

	/** Construct the main UI midlet. */
	public MainForm() {
		super();
		
		title = getAppProperty("MIDlet-Name") + " " + getAppProperty("MIDlet-Version");
		
		display = Display.getDisplay(this);

		transportLayer = new TransportLayer(/*new EpihandyTransportLayer().getClass()*/);
		transportLayer.setDisplay(display);
		transportLayer.setDefaultCommnucationParameter(TransportLayer.KEY_HTTP_URL, "");
	
		formMgr = new FormManager(title, display, null, null, transportLayer, this, this);
		FormManager.setGlobalInstance(formMgr);
		
		mainScreen = formMgr.getPrevScreen(); // this was initialised for us
		transportLayer.setPrevScreen(mainScreen);
		
		alertMsg = new AlertMessage(display, title, mainScreen, this);
		
		EpihandyDataStorage.storageListener = this;
	}

	protected void destroyApp(boolean arg0) {
	}

	protected void pauseApp() {
	}

	protected void startApp() {
		userMgr = formMgr.getUserManager();
		userMgr.setLogonListener(this);
		userMgr.logOn();
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
	
	public boolean onLoggedOn(){
		//if (GeneralSettings.isHideStudies()) {
		//	formMgr.selectForm(true, display.getCurrent());
		//} else {
			formMgr.selectStudy(false);
		//}

		return false;
	}
	
	public void onLogonCancel(){
		exit();
	}
	
	public void onLogout() {
		exitConfirmMode = true;
		alertMsg.showConfirm(MenuText.EXIT_PROMPT());
	}

	// TransportLayerListener
	public void cancelled() {
	}

	// TransportLayerListener
	public void downloaded(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut) {
		RequestHeader requestHeader = (RequestHeader)dataInParams;
		if (requestHeader.getAction() == RequestHeader.ACTION_DOWNLOAD_USERS) {
			userMgr.validateUser();
		}
	}

	// TransportLayerListener
	public void updateCommunicationParams() {
	}

	// TransportLayerListener
	public void uploaded(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut) {
	}
}