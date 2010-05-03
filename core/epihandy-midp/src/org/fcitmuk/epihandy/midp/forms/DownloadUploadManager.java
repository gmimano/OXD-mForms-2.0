package org.fcitmuk.epihandy.midp.forms;

import java.util.Vector;

import javax.microedition.lcdui.Displayable;

import org.fcitmuk.communication.TransportLayer;
import org.fcitmuk.communication.TransportLayerListener;
import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.db.util.PersistentInt;
import org.fcitmuk.db.util.PersistentString;
import org.fcitmuk.epihandy.EpihandyConstants;
import org.fcitmuk.epihandy.FormData;
import org.fcitmuk.epihandy.FormDef;
import org.fcitmuk.epihandy.LanguageList;
import org.fcitmuk.epihandy.MenuTextList;
import org.fcitmuk.epihandy.RequestHeader;
import org.fcitmuk.epihandy.ResponseHeader;
import org.fcitmuk.epihandy.StudyData;
import org.fcitmuk.epihandy.StudyDataList;
import org.fcitmuk.epihandy.StudyDef;
import org.fcitmuk.epihandy.StudyDefList;
import org.fcitmuk.epihandy.UserList;
import org.fcitmuk.epihandy.UserStudyDefLists;
import org.fcitmuk.epihandy.midp.db.EpihandyDataStorage;
import org.fcitmuk.midp.db.util.Settings;
import org.fcitmuk.util.AlertMessage;
import org.fcitmuk.util.AlertMessageListener;
import org.fcitmuk.util.MenuText;


/**
 * 
 * @author daniel
 *
 */
public class DownloadUploadManager implements TransportLayerListener,AlertMessageListener {

	/** No alert is currently displayed. */
	private static final byte CA_NONE = -1;

	/** Current alert is for form download confirmation. */
	private static final byte CA_FORMS_DOWNLOAD = 1;

	/** Current alert is for study list download confirmation. */
	private static final byte CA_STUDY_LIST_DOWNLOAD = 2;

	/** Current alert is for data upload confirmation. */
	private static final byte CA_DATA_UPLOAD = 3;

	/** Current alert is for dsiplay of an error message. */
	private static final byte CA_ERROR_MSG_DISPLAY = 4;

	/** Current action is for users download. */
	private static final byte CA_USERS_DOWNLOAD = 5;

	/** Current action is for languages download. */
	private static final byte CA_LANGUAGES_DOWNLOAD = 6;

	/** Current action is for menu text download. */
	private static final byte CA_MENU_TEXT_DOWNLOAD = 7;

	/** Reference to the commnunication layer. */
	private TransportLayer transportLayer;

	/** Reference to the communication parameter. */
	private RequestHeader requestHeader;

	private ResponseHeader responseHeader;

	private EpihandyController controller;

	private StudyDef currentStudy;

	//private String title;

	private byte currentAction = CA_NONE;

	private AlertMessage alertMsg;

	//private Displayable prevScreen;

	private Vector studyList;

	private String userName;

	private String password;

	private TransportLayerListener transportLayerListener;

	private static final String STORAGE_NAME_SETTINGS = "fcitmuk.DefaultTransportLayer";

	/** Keeps trac of the current number of form data being uploaded to the server. */
	private int currentDataCount = 0;
	
	/** The total number of forms of data that needs to be uploaded to the server. */
	private int totalDataCount = 0;

	/** The current form data that is being uploaded to the server. */
	private FormData formData = null;

	/** The id of the study to which the form data that is being uploaded to the server belongs. */
	private int studyId;

	/** The list of form data that has not yet been uploaded to the server. */
	private StudyDataList studyDataList = null;


	public DownloadUploadManager(TransportLayer transportLayer,EpihandyController controller, String title,TransportLayerListener transportLayerListener) {
		this.transportLayer = transportLayer;
		this.controller = controller;
		//this.title = title;
		this.transportLayerListener = transportLayerListener; // for propagating back transport layer events.

		this.alertMsg = new AlertMessage(this.transportLayer.getDisplay(),title, this.transportLayer.getPrevScreen(), this);

		this.requestHeader = new RequestHeader();
		this.responseHeader = new ResponseHeader();
	}

	public void setTransportLayerListener(TransportLayerListener transportLayerListener){
		this.transportLayerListener = transportLayerListener;
	}

	public void downloadStudyForms(Displayable currentScreen, String userName,String password,boolean confirm) {
		this.userName = userName;
		this.password = password;
		//this.prevScreen = currentScreen;

		currentStudy = controller.getCurrentStudy();
		if (currentStudy == null) {
			currentAction = CA_NONE;
			//alertMsg.show("Please first select a study.");
		} 
		else {
			currentAction = CA_FORMS_DOWNLOAD; // CA_USERS_DOWNLOAD; CA_FORMS_DOWNLOAD; First dowload the list of users.

			if(confirm){
				if(getCollectedStudyData(controller.getCurrentStudy()) != null){
					alertMsg.show(MenuText.STUDY() + " " + getCurrentStudyName() + " " + MenuText.UPLOAD_BEFORE_DOWNLOAD_PROMPT());
					currentAction = CA_NONE;
				}
				else
					alertMsg.showConfirm(MenuText.DOWNLOAD_STUDY_FORMS_PROMPT() + getCurrentStudyName());
			}
			else
				downloadForms();
		}
	}

	public String getCurrentStudyName(){
		return "{"+ controller.getCurrentStudy().getName()+ " ID:"+ controller.getCurrentStudy().getId() + "}";
	}

	public void downloadForms(Displayable currentScreen, Vector studyList, String userName,String password,boolean confirm) {
		this.userName = userName;
		this.password = password;
		//this.prevScreen = currentScreen;
		this.studyList = studyList;

		currentStudy = null;
		currentAction = CA_FORMS_DOWNLOAD; // CA_USERS_DOWNLOAD; CA_FORMS_DOWNLOAD; First dowload the list of users.

		if(confirm){
			if(!isThereCollectedData(MenuText.FORMS(),studyList))
				alertMsg.showConfirm(MenuText.DOWNLOAD_FORMS_PROMPT());
		}
		else
			downloadForms();
	}

	public boolean isThereCollectedData(String name, Vector studyList){
		this.studyList = studyList;

		StudyDataList studyDataList = getCollectedData();
		if(!(studyDataList == null || studyDataList.getStudies() == null || studyDataList.getStudies().size() == 0)){
			this.currentAction = CA_NONE;
			this.alertMsg.show(MenuText.UN_UPLOADED_DATA_PROMPT() + " " + name + ".");
			return true;
		} 

		return false;
	}

	public void downloadStudies(Displayable currentScreen, Vector studyList, String userName,String password, boolean confirm) {
		this.userName = userName;
		this.password = password;
		//this.prevScreen = currentScreen;
		this.studyList = studyList;

		currentAction = CA_STUDY_LIST_DOWNLOAD;

		if(confirm){
			if(!isThereCollectedData(MenuText.STUDIES(),studyList))
				alertMsg.showConfirm(MenuText.DOWNLOAD_STUDIES_PROMPT());
		}
		else
			downloadStudies();
	}

	public void downloadLanguages(Displayable currentScreen, Vector studyList, String userName,String password, boolean confirm) {
		this.userName = userName;
		this.password = password;
		//this.prevScreen = currentScreen;
		alertMsg.setPrevScreen(currentScreen); //TODO Need to fix this hack
		this.studyList = studyList;

		currentAction = CA_LANGUAGES_DOWNLOAD;

		if(confirm)
			alertMsg.showConfirm(MenuText.DOWNLOAD_LANGUAGES_PROMPT());
		else
			downloadLanguages();
	}

	public void downloadMenuText(Displayable currentScreen, Vector studyList, String userName,String password, boolean confirm) {
		this.userName = userName;
		this.password = password;
		//this.prevScreen = currentScreen;
		alertMsg.setPrevScreen(currentScreen); //TODO Need to fix this hack
		this.studyList = studyList;

		currentAction = CA_MENU_TEXT_DOWNLOAD;
		downloadMenuText();
	}

	public void uploadData(Displayable currentScreen, Vector studyList,String userName, String password) {
		this.userName = userName;
		this.password = password;
		//this.prevScreen = currentScreen;
		this.studyList = studyList;

		if (studyList == null || studyList.size() == 0) {
			currentAction = CA_ERROR_MSG_DISPLAY;
			alertMsg.show(MenuText.DOWNLOAD_FORMS_FIRST());
		} 
		else {
			/*for(byte i=0; i<studyList.size(); i++){
				StudyDef studyDef = (StudyDef)studyList.elementAt(i);
				if(studyDef == null || studyDef.getForms() == null){
					currentAction = CA_ERROR_MSG_DISPLAY;
					alertMsg.show("Problem looking for forms in study."+studyDef.getName());
					return;
				}
			}*/

			currentAction = CA_DATA_UPLOAD;
			alertMsg.showConfirm(MenuText.UPLOAD_DATA_PROMPT());
		}
	}

	private void downloadStudies() {
		alertMsg.showProgress(MenuText.STUDY_LIST_DOWNLOAD(),MenuText.DOWNLOADING_STUDY_LIST());

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_DOWNLOAD_STUDY_LIST);

		setCommunicationParams();
		transportLayer.download(requestHeader, null, responseHeader,new StudyDefList(), this, userName, password);
	}

	private void downloadForms() {
		alertMsg.showProgress(MenuText.FORM_DOWNLOAD(), MenuText.DOWNLOADING_FORMS());

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_DOWNLOAD_USERS_AND_FORMS); // ACTION_DOWNLOAD_STUDY_FORMS

		Persistent studyIdParam = new PersistentInt(EpihandyConstants.NULL_ID);
		if (this.currentStudy != null){
			if(FormManager.useStudyNumericId)
				studyIdParam = new PersistentInt(currentStudy.getId());
			else
				studyIdParam = new PersistentString(this.currentStudy.getVariableName());
		}

		setCommunicationParams();
		transportLayer.download(requestHeader, studyIdParam, responseHeader,new UserStudyDefLists(), this, userName, password); // StudyDef
	}

	private void downloadUsers() {
		alertMsg.showProgress(MenuText.FORM_DOWNLOAD(), MenuText.DOWNLOADING_USERS());

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_DOWNLOAD_USERS);
		setCommunicationParams();
		transportLayer.download(requestHeader, null, responseHeader,new UserList(), this, userName, password);
	}

	private void downloadLanguages() {
		alertMsg.showProgress(MenuText.LANGUAGE_DOWNLOAD(), MenuText.DOWNLOADING_LANGUAGES());

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_DOWNLOAD_LANGUAGES);
		setCommunicationParams();
		transportLayer.download(requestHeader, null, responseHeader,new LanguageList(), this, userName, password);
	}

	private void downloadMenuText() {
		alertMsg.showProgress(MenuText.MENU_TEXT_DOWNLOAD(), MenuText.DOWNLOADING_MENU_TEXT());

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_DOWNLOAD_MENU_TEXT);
		setCommunicationParams();
		transportLayer.download(requestHeader, null, responseHeader,new MenuTextList(), this, userName, password);
	}

	/** Uploads collected data to the server. */
	private void uploadData() {
		alertMsg.showProgress(MenuText.DATA_UPLOAD(), MenuText.UPLOADING_DATA());

		totalDataCount = currentDataCount = 0;
		/*StudyDataList*/ studyDataList = getCollectedData();
		if (studyDataList == null || studyDataList.getStudies() == null || studyDataList.getStudies().size() == 0) {
			this.currentAction = CA_NONE;
			this.alertMsg.show(MenuText.NO_UPLOAD_DATA());
		} 
		else{
			/*requestHeader.setLocale(LanguageSettings.getLocale());
			requestHeader.setAction(RequestHeader.ACTION_UPLOAD_DATA);
			setCommunicationParams();
			transportLayer.upload(requestHeader, studyDataList, responseHeader,responseHeader, this, userName, password);*/

			currentDataCount = 1;
			
			uploadFormData();
		}
	}
	
	private void uploadFormData(){
		Vector studies = studyDataList.getStudies();
		for(int index = 0; index < studyDataList.getStudies().size(); index++){
			StudyData studyData = (StudyData)studies.elementAt(index);
			studyId = studyData.getId();
			
			Vector forms = studyData.getForms();
			for(int i = 0; i < forms.size(); i++){
				formData = (FormData)forms.elementAt(i);
				forms.removeElementAt(i);
				break;
			}
		}
		
		alertMsg.showProgress(MenuText.DATA_UPLOAD(), "Uploading " + currentDataCount + " of " + totalDataCount);
		
		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_UPLOAD_DATA);
		setCommunicationParams();
		transportLayer.upload(requestHeader, new StudyDataList(new StudyData(studyId,formData)), responseHeader,responseHeader, this, userName, password);
	}

	/**
	 * Sets the communication parameters which depend on the connection type and
	 * current action.
	 * 
	 */
	public void setCommunicationParams() {
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		String url = "";

		switch (currentAction) {
		case CA_LANGUAGES_DOWNLOAD:
		case CA_MENU_TEXT_DOWNLOAD:
		case CA_FORMS_DOWNLOAD: // 72.249.82.103 //192.168.23.3
			url = settings.getSetting(TransportLayer.KEY_FORM_DOWNLOAD_HTTP_URL); // "http://localhost:8080/openmrs/moduleServlet/xforms/xformDownload?target=xforms&uname="+userName+"&pw="+password;
			break;
		case CA_USERS_DOWNLOAD: // 72.249.82.103
			url = settings.getSetting(TransportLayer.KEY_USER_DOWNLOAD_HTTP_URL); // "http://localhost:8080/openmrs/moduleServlet/xforms/userDownload?uname="+userName+"&pw="+password;
			break;
		case CA_STUDY_LIST_DOWNLOAD:
			url = settings.getSetting(TransportLayer.KEY_FORM_DOWNLOAD_HTTP_URL); // "";
			break;
		case CA_DATA_UPLOAD:
			url = settings.getSetting(TransportLayer.KEY_DATA_UPLOAD_HTTP_URL); // "http://localhost:8080/openmrs/module/xforms/xformDataUpload.form?batchEntry=true&uname="+userName+"&pw="+password;
			break;
		}

		if (url != null) {
			/*if (url.indexOf('?') > 0)
				url += "&";
			else
				url += "?";

			url += "uname=" + userName + "&pw=" + password;*/

			transportLayer.setCommnucationParameter(TransportLayer.KEY_HTTP_URL, url);
		}

		requestHeader.setUserName(userName);
		requestHeader.setPassword(password);
	}

	public void updateCommunicationParams() {
		byte prevCurrentAction = currentAction;

		switch(requestHeader.getAction()){
		case RequestHeader.ACTION_DOWNLOAD_USERS_AND_FORMS:
		case RequestHeader.ACTION_DOWNLOAD_STUDY_FORMS:
			currentAction = CA_FORMS_DOWNLOAD;
			break;
		case RequestHeader.ACTION_DOWNLOAD_USERS:
			currentAction = CA_USERS_DOWNLOAD;
			break;
		case RequestHeader.ACTION_DOWNLOAD_STUDY_LIST:
			currentAction = CA_STUDY_LIST_DOWNLOAD;
			break;
		case RequestHeader.ACTION_UPLOAD_DATA:
			currentAction = CA_DATA_UPLOAD;
			break;
		case RequestHeader.ACTION_DOWNLOAD_LANGUAGES:
			currentAction = CA_LANGUAGES_DOWNLOAD;
			break;
		case RequestHeader.ACTION_DOWNLOAD_MENU_TEXT:
			currentAction = CA_MENU_TEXT_DOWNLOAD;
			break;
		}

		setCommunicationParams();

		currentAction = prevCurrentAction;
	}

	/**
	 * Called after data has been successfully downloaded.
	 * 
	 * @param dataOutParams -
	 *            the parameters sent with the data.
	 * @param dataOut -
	 *            the downloaded data.
	 */
	public void downloaded(Persistent dataOutParams, Persistent dataOut) {
		String message = MenuText.PROBLEM_SAVING_DOWNLOAD();
		boolean wasUserDownload = false, errorsOccured = false;;
		try {
			if (currentAction == CA_STUDY_LIST_DOWNLOAD) {
				deleteAllForms(); //delete all existing forms to prevent bugs of orphan forms who studies are no longer on server, and more.
				EpihandyDataStorage.saveStudyList((StudyDefList) dataOut);
				this.controller.setStudyList(((StudyDefList) dataOut).getStudies());
				message = ((StudyDefList) dataOut).getStudies().size()+" "+MenuText.STUDY_DOWNLOAD_SAVED();
			} 
			else if (currentAction == CA_USERS_DOWNLOAD) {
				EpihandyDataStorage.saveUsers((UserList) dataOut);
				wasUserDownload = true;
				alertMsg.showProgress(MenuText.FORM_DOWNLOAD(),((UserList)dataOut).size()+" "+MenuText.USER_DOWNLOAD_SAVED());
				currentAction = CA_FORMS_DOWNLOAD;
				downloadForms();
				alertMsg.showProgress(MenuText.FORM_DOWNLOAD(), MenuText.DOWNLOADING_FORMS());
			} 
			else if (currentAction == CA_FORMS_DOWNLOAD) {
				UserStudyDefLists lists = ((UserStudyDefLists) dataOut);

				UserList users = lists.getUsers();
				EpihandyDataStorage.saveUsers(users);

				StudyDef studyDef = lists.getStudyDef();

				//TODO these three lines have been put from some clients like openclinica
				//which may have a different oc study id from that of oxd study id
				studyDef.setId(controller.getCurrentStudy().getId());
				studyDef.setName(controller.getCurrentStudy().getName());
				studyDef.setVariableName(controller.getCurrentStudy().getVariableName());

				EpihandyDataStorage.saveStudy(studyDef);
				this.controller.setStudy(studyDef);

				if(studyDef.getForms() == null || studyDef.getForms().size() == 0)
					message = MenuText.NO_SERVER_STUDY_FORMS() + "  {"+ studyDef.getName()+ " ID:"+ studyDef.getId() + "}?";
				else
					message = studyDef.getForms().size()+" " +MenuText.FORM_DOWNLOAD_SAVED();

				/*
				 * EpihandyDataStorage.saveStudy((StudyDef)dataOut);
				 * this.controller.setStudy((StudyDef)dataOut); message = "Forms
				 * downloaded and saved successfully";
				 */
			}
			else if (currentAction == CA_LANGUAGES_DOWNLOAD) {
				LanguageList languages = (LanguageList)dataOut;
				EpihandyDataStorage.saveLanguages(languages);

				if(languages.size() == 0)
					message = MenuText.NO_LANGUAGES();
				else
					message = languages.size()+" " + MenuText.LANGUAGE_DOWNLOAD_SAVED();
			}
			else if (currentAction == CA_MENU_TEXT_DOWNLOAD) {
				MenuTextList menuTextList = (MenuTextList)dataOut;
				EpihandyDataStorage.saveMenuText(menuTextList);

				MenuText.setMenuTextList(menuTextList);

				if(menuTextList.size() == 0)
					message = MenuText.NO_MENU_TEXT();
				else
					message = menuTextList.size()+" " + MenuText.MENU_TEXT_DOWNLOAD_SAVED();
			}

			//if (transportLayerListener != null && !wasUserDownload)
			//	transportLayerListener.downloaded(dataOutParams, dataOut);

		} catch (Exception e) {
			errorsOccured = true;
			//e.printStackTrace();
			message += e.getMessage();
		}

		if (!wasUserDownload) { // after downloading users, we want to continue downloading forms.
			currentAction = CA_NONE;
			alertMsg.show(message);
		}

		if (!errorsOccured && transportLayerListener != null && !wasUserDownload)
			transportLayerListener.downloaded(dataOutParams, dataOut);
	}

	/**
	 * Called after data has been successfully uploaded.
	 * 
	 * @param dataOutParams -
	 *            parameters sent after data has been uploaded.
	 * @param dataOut -
	 *            data sent after the upload.
	 */
	public void uploaded(Persistent dataOutParams, Persistent dataOut) {
		String message = MenuText.DATA_UPLOAD_PROBLEM();

		if (currentAction == CA_DATA_UPLOAD) {
			try {
				ResponseHeader status = (ResponseHeader) dataOut;
				if (status.isSuccess()) {
					//if(GeneralSettings.deleteDataAfterUpload()){
						//EpihandyDataStorage.deleteData(new StudyDefList(studyList));
						//assert(formData != null);
						EpihandyDataStorage.deleteFormData(studyId, formData);
					//}

					if(currentDataCount == totalDataCount){
						message = MenuText.DATA_UPLOAD_SUCCESS();
						if (transportLayerListener != null)
							transportLayerListener.uploaded(dataOutParams, dataOut);
					}
					else{
						currentDataCount++;
						uploadFormData();
						return;
					}
				} 
				else
					message = MenuText.DATA_UPLOAD_FAILURE();
			} catch (Exception e) {
				//e.printStackTrace();
				message = MenuText.PROBLEM_CLEANING_STORE();
			}
		} else
			message = MenuText.UNKNOWN_UPLOAD();

		currentAction = CA_NONE;
		alertMsg.show(message);
	}

	/**
	 * Called when an error occurs during any operation.
	 * 
	 * @param errorMessage -
	 *            the error message.
	 * @param e -
	 *            the exception, if any, that did lead to this error.
	 */
	public void errorOccured(String errorMessage, Exception e) {
		currentAction = CA_NONE; // if not set to this value, the alert will
		// be on forever.
		if (e != null) {
			//e.printStackTrace();
			errorMessage += " : " + e.getMessage();
		}
		alertMsg.show(errorMessage);
	}

	public void cancelled() {
		if (transportLayerListener != null)
			transportLayerListener.cancelled();
	}

	/**
	 * Called when the OK commad of an alert is clicked.
	 */
	public void onAlertMessage(byte msg) {
		if(msg == AlertMessageListener.MSG_OK){
			if (currentAction == CA_STUDY_LIST_DOWNLOAD) 
				downloadStudies();
			else if (currentAction == CA_USERS_DOWNLOAD /* CA_FORMS_DOWNLOAD */)
				downloadUsers();
			else if (currentAction == CA_FORMS_DOWNLOAD) // TODO May need to be done after downloading users.
				downloadForms();
			else if (currentAction == CA_DATA_UPLOAD) 
				uploadData();
			else if (currentAction == CA_LANGUAGES_DOWNLOAD)
				downloadLanguages();
			else
				alertMsg.turnOffAlert();
		}
		else
			alertMsg.turnOffAlert();
	}

	private StudyDataList getCollectedData() {
		if(studyList == null)
			return null;

		StudyDataList studyDatalist = new StudyDataList();
		for (int i = 0; i < studyList.size(); i++){
			StudyDef studyDef = (StudyDef) studyList.elementAt(i);
			//Study list always has no forms, so we have to get them from the database.
			studyDef = EpihandyDataStorage.getStudy(studyDef.getId());

			//If no forms downloaded yet, then we don't expect any data to save.
			if(studyDef != null){
				StudyData studyData = getCollectedStudyData(studyDef);
				if(studyData != null)
					studyDatalist.addStudy(studyData);
			}
		}

		return studyDatalist;
	}

	private void setFormDefs(Vector formDatas, FormDef formDef) {
		for (int i = 0; i < formDatas.size(); i++) {
			FormData formData = (FormData) formDatas.elementAt(i);
			formData.setDef(formDef);
		}
	}

	/*private void fillCollectedStudyData(StudyDef studyDef,StudyDataList studyDataList) {
		StudyData studyData = new StudyData(studyDef.getId());
		Vector formDefs = studyDef.getForms();
		if (formDefs != null && formDefs.size() > 0) {
			for (int i = 0; i < formDefs.size(); i++) {
				FormDef formDef = ((FormDef) formDefs.elementAt(i));
				Vector formDatas = EpihandyDataStorage.getFormData(studyDef.getId(), formDef.getId());
				if (formDatas != null) {
					setFormDefs(formDatas, formDef); // These are for writing to stream but they are not persisted.
					studyData.addForms(formDatas);
				}
			}
			if (studyData.getForms() != null && studyData.getForms().size() > 0)
				studyDataList.addStudy(studyData);
		}
	}*/

	public StudyData getCollectedStudyData(StudyDef studyDef) {
		StudyData studyData = new StudyData(studyDef.getId());
		Vector formDefs = studyDef.getForms();
		if (formDefs != null && formDefs.size() > 0) {
			for (int i = 0; i < formDefs.size(); i++) {
				FormDef formDef = ((FormDef) formDefs.elementAt(i));
				Vector formDatas = EpihandyDataStorage.getFormData(studyDef.getId(), formDef.getId());
				
				if (formDatas != null) {
					setFormDefs(formDatas, formDef); // These are for writing to stream but they are not persisted.
					studyData.addForms(formDatas);
					totalDataCount = formDatas.size();
				}
			}
			
			if (studyData.getForms() != null && studyData.getForms().size() > 0)
				return studyData;
		}

		return null;
	}

	public void setPrevSrceen(Displayable screen){
		alertMsg.setPrevScreen(screen); //TODO Need to fix this hack
	}


	/**
	 * Deletes all form definitions.
	 *
	 */
	private void deleteAllForms(){
		Vector list = controller.getStudyList();
		if(list == null)
			return;

		for(byte i=0; i<list.size(); i++)
			EpihandyDataStorage.deleteStudy((StudyDef)list.elementAt(i));
	}
}
