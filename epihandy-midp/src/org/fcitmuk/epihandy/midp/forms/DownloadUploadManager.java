package org.fcitmuk.epihandy.midp.forms;

import java.util.Vector;

import javax.microedition.lcdui.Displayable;

import org.fcitmuk.communication.TransportLayer;
import org.fcitmuk.communication.TransportLayerListener;
import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.db.util.PersistentArray;
import org.fcitmuk.db.util.PersistentInt;
import org.fcitmuk.db.util.PersistentString;
import org.fcitmuk.epihandy.EpihandyConstants;
import org.fcitmuk.epihandy.FormData;
import org.fcitmuk.epihandy.FormDataSummary;
import org.fcitmuk.epihandy.LanguageList;
import org.fcitmuk.epihandy.MenuTextList;
import org.fcitmuk.epihandy.RequestHeader;
import org.fcitmuk.epihandy.ResponseHeader;
import org.fcitmuk.epihandy.StudyData;
import org.fcitmuk.epihandy.StudyDataList;
import org.fcitmuk.epihandy.UploadError;
import org.fcitmuk.epihandy.UploadResponse;
import org.fcitmuk.epihandy.UserList;
import org.fcitmuk.epihandy.midp.db.EpihandyDataStorage;
import org.fcitmuk.epihandy.midp.db.StoredFormDef;
import org.fcitmuk.epihandy.midp.db.StoredStudyDef;
import org.fcitmuk.epihandy.midp.transport.FormUpload;
import org.fcitmuk.epihandy.midp.transport.StudyDownload;
import org.fcitmuk.epihandy.midp.transport.UsersAndFormDownload;
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
	
	/** Current alert is for single data upload confirmation. */
	private static final byte CA_SINGLE_DATA_UPLOAD = 8;

	/** Reference to the commnunication layer. */
	private TransportLayer transportLayer;

	/** Reference to the communication parameter. */
	private RequestHeader requestHeader;

	private ResponseHeader responseHeader;

	private EpihandyController controller;

	private StoredStudyDef currentStudy;

	private byte currentAction = CA_NONE;

	private AlertMessage alertMsg;

	private String userName;

	private String password;

	private TransportLayerListener transportLayerListener;
	
	/** Reference to the currentFormData to upload (for single form data upload) */
	private FormData currentFormData;

	private static final String STORAGE_NAME_SETTINGS = "fcitmuk.DefaultTransportLayer";


	public DownloadUploadManager(TransportLayer transportLayer,EpihandyController controller, String title,TransportLayerListener transportLayerListener) {
		this.transportLayer = transportLayer;
		this.controller = controller;
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
		
		if (currentScreen != null)
			alertMsg.setPrevScreen(currentScreen);

		currentStudy = controller.getCurrentStudy();
		if (currentStudy == null) {
			currentAction = CA_NONE;
		} 
		else {
			currentAction = CA_FORMS_DOWNLOAD; //First dowload the list of users.

			if(confirm){
				
				if(controller.getModel().storedDataExistsForStudy(currentStudy.getId())){
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

	public void downloadForms(Displayable currentScreen,String userName,String password,boolean confirm) {
		this.userName = userName;
		this.password = password;

		currentStudy = null;
		currentAction = CA_FORMS_DOWNLOAD; // First dowload the list of users.
		
		if (currentScreen != null)
			alertMsg.setPrevScreen(currentScreen);

		if(confirm){
			if(!isThereCollectedData(MenuText.FORMS()))
				alertMsg.showConfirm(MenuText.DOWNLOAD_FORMS_PROMPT());
		}
		else
			downloadForms();
	}

	private boolean isThereCollectedData(String name) {
		if (controller.getModel().storedDataExists()) {
			this.currentAction = CA_NONE;
			this.alertMsg.show(MenuText.UN_UPLOADED_DATA_PROMPT() + " " + name
					+ ".");
			return true;
		}
		return false;
	}

	public void downloadStudies(Displayable currentScreen,String userName,String password, boolean confirm) {
		this.userName = userName;
		this.password = password;

		currentAction = CA_STUDY_LIST_DOWNLOAD;
		
		if (currentScreen != null)
			alertMsg.setPrevScreen(currentScreen);

		if(confirm){
			if(!isThereCollectedData(MenuText.STUDIES()))
				alertMsg.showConfirm(MenuText.DOWNLOAD_STUDIES_PROMPT());
		}
		else
			downloadStudies();
	}
	
	public void downloadLanguages(Displayable currentScreen, String userName,String password, boolean confirm) {
		this.userName = userName;
		this.password = password;
		
		if (currentScreen != null)
			alertMsg.setPrevScreen(currentScreen);

		currentAction = CA_LANGUAGES_DOWNLOAD;
		
		if(confirm)
			alertMsg.showConfirm(MenuText.DOWNLOAD_LANGUAGES_PROMPT());
		else
			downloadLanguages();
	}
	
	public void downloadMenuText(Displayable currentScreen, String userName,String password, boolean confirm) {
		this.userName = userName;
		this.password = password;
		
		if (currentScreen != null)
			alertMsg.setPrevScreen(currentScreen);

		currentAction = CA_MENU_TEXT_DOWNLOAD;
		downloadMenuText();
	}
	
	public void uploadData(Displayable currentScreen, StoredStudyDef [] studies, FormData formData, String userName, String password) {
		this.userName = userName;
		this.password = password;
		this.currentFormData = formData;
		
		if (currentScreen != null)
			alertMsg.setPrevScreen(currentScreen);

		if (studies == null || studies.length == 0) {
			currentAction = CA_ERROR_MSG_DISPLAY;
			alertMsg.show(MenuText.DOWNLOAD_FORMS_FIRST());
		} 
		else {
			currentAction = CA_DATA_UPLOAD;
			if (formData != null) 
				currentAction = CA_SINGLE_DATA_UPLOAD;
			alertMsg.showConfirm(MenuText.UPLOAD_DATA_PROMPT());
		}
	}
	
	public void downloadUsers(Displayable currentScreen, String userName, String password) {
		this.userName = userName;
		this.password = password;

		currentAction = CA_USERS_DOWNLOAD;
		
		if (currentScreen != null)
			alertMsg.setPrevScreen(currentScreen);

		downloadUsers();
	}

	private void downloadStudies() {
		alertMsg.showProgress(MenuText.STUDY_LIST_DOWNLOAD(),MenuText.DOWNLOADING_STUDY_LIST());

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_DOWNLOAD_STUDY_LIST);

		setCommunicationParams();
		transportLayer.download(requestHeader, null, responseHeader,controller.getStudyDownload(), this, userName, password);
	}

	private void downloadForms() {
		alertMsg.showProgress(MenuText.FORM_DOWNLOAD(), MenuText.DOWNLOADING_FORMS());

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_DOWNLOAD_USERS_AND_FORMS); // ACTION_DOWNLOAD_STUDY_FORMS

		PersistentInt studyIdParam = new PersistentInt(EpihandyConstants.NULL_ID);
		if (this.currentStudy != null)
			studyIdParam = new PersistentInt(currentStudy.getId());
		PersistentArray dataInParams = new PersistentArray();
		dataInParams.setValues(new Persistent[] {new PersistentString(userName), studyIdParam});
		setCommunicationParams();
		transportLayer.download(requestHeader, dataInParams, responseHeader,controller.getUserAndFormDownload(), this, userName, password); // StudyDef
	}

	private void downloadUsers() {
		alertMsg.showProgress(MenuText.FORM_DOWNLOAD(), MenuText.DOWNLOADING_USERS());

		requestHeader.setLocale(LanguageSettings.getLocale());
		requestHeader.setAction(RequestHeader.ACTION_DOWNLOAD_USERS);
		PersistentString userParam = new PersistentString(userName);
		setCommunicationParams();
		transportLayer.download(requestHeader, userParam, responseHeader,new UserList(), this, userName, password);
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

		if (!controller.getModel().storedDataExists()) {
			this.currentAction = CA_NONE;
			this.alertMsg.show(MenuText.NO_UPLOAD_DATA());
		} 
		else {
			Persistent dataIn = null;
			if (currentAction == CA_SINGLE_DATA_UPLOAD) {
				StudyData studyData = new StudyData();
				studyData.addForm(currentFormData);
				dataIn = new StudyDataList();
				((StudyDataList)dataIn).addStudy(studyData);
			} else {
				dataIn = new FormUpload(controller.getModel());
			}
			UploadResponse uploadResponse = new UploadResponse();
			requestHeader.setLocale(LanguageSettings.getLocale());
			requestHeader.setAction(RequestHeader.ACTION_UPLOAD_DATA);
			setCommunicationParams();
			transportLayer.upload(requestHeader, dataIn, responseHeader, uploadResponse, this, userName, password);
		}
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
		case CA_FORMS_DOWNLOAD:
		case CA_STUDY_LIST_DOWNLOAD:
		case CA_USERS_DOWNLOAD:
			url = settings.getSetting(TransportLayer.KEY_FORM_DOWNLOAD_HTTP_URL);
			break;
		case CA_DATA_UPLOAD: 
		case CA_SINGLE_DATA_UPLOAD:
			url = settings.getSetting(TransportLayer.KEY_DATA_UPLOAD_HTTP_URL);
			break;
		}
		
		if (url != null && !url.trim().equals("")) {
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
			if (currentFormData != null) currentAction = CA_SINGLE_DATA_UPLOAD;
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
	public void downloaded(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut) {
		String message = MenuText.PROBLEM_SAVING_DOWNLOAD();
		boolean errorsOccured = false;;
		try {
			if (currentAction == CA_STUDY_LIST_DOWNLOAD) {
				StudyDownload dl = ((StudyDownload) dataOut);
				if (dl.getException() != null) {
					errorsOccured = true;
					message = dl.getException().getMessage();
				} else
					message = dl.getModel().getStudies().length + " "
							+ MenuText.STUDY_DOWNLOAD_SAVED();
			}
			else if (currentAction == CA_USERS_DOWNLOAD) {
				EpihandyDataStorage.saveUsers((UserList) dataOut);
				message = ((UserList)dataOut).size()+" "+MenuText.USER_DOWNLOAD_SAVED();
			} 
			else if (currentAction == CA_FORMS_DOWNLOAD) {
				UsersAndFormDownload dl = ((UsersAndFormDownload) dataOut);

				if (dl.getException() != null) {
					errorsOccured = true;
					message = dl.getException().getMessage();
				}
				else {

				StoredStudyDef study = dl.getModel().getSelectedStudyDef();
				StoredFormDef [] forms = dl.getModel().getStudyForms();
				
				if(forms == null || forms.length == 0)
					message = MenuText.NO_SERVER_STUDY_FORMS() + "  {"+ study.getName()+ " ID:"+ study.getId() + "}?";
				else
					message = forms.length+" " +MenuText.FORM_DOWNLOAD_SAVED();
				}
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
				
				if(menuTextList.size() == 0)
					message = MenuText.NO_MENU_TEXT();
				else
					message = menuTextList.size()+" " + MenuText.MENU_TEXT_DOWNLOAD_SAVED();
			}

		} catch (Exception e) {
			errorsOccured = true;
			message += e.getMessage();
		}

		currentAction = CA_NONE;
		alertMsg.show(message);

		if (!errorsOccured && transportLayerListener != null)
			transportLayerListener.downloaded(dataInParams, dataIn, dataOutParams, dataOut);
	}
	
	private int deleteSuccessfulForms(FormUpload formUpload,
			UploadResponse uploadResponse) {

		int studyCount = formUpload.getStudyCount();
		int successCount = uploadResponse.getTotalForms()
				- uploadResponse.getErrorCount();

		if (successCount <= 0)
			return 0;

		Vector idsToDelete = new Vector();

		out: for (int studyidx = 0; studyidx < studyCount; studyidx++) {
			int formCount = formUpload.getFormCount(studyidx);
			for (int formidx = 0; formidx < formCount; formidx++) {
				if (!uploadResponse.isFailedForm((byte) studyidx,
						(short) formidx)) {
					int[] formId = formUpload.getFormAtPos(studyidx, formidx);
					idsToDelete.addElement(new int[] { formId[0], formId[1],
							formId[2] });
					if (idsToDelete.size() >= successCount)
						break out;
				}
			}
		}

		if (idsToDelete.size() > 0)
			controller.getModel().deleteFormData(idsToDelete);

		return idsToDelete.size();
	}
	
	private void replaceUploadErrors(FormUpload formUpload,
			UploadResponse uploadResponse) {

		UploadError[] errors = uploadResponse.getUploadErrors();
		Vector formCoordsWithErrors = new Vector();
		for (int erridx = 0; erridx < errors.length; erridx++) {
			UploadError error = errors[erridx];
			int[] formId = formUpload.getFormAtPos(error.getStudyIndex(), error
					.getFormIndex());
			formCoordsWithErrors.addElement(new Object[] { formId,
					error.getDescription() });
		}

		controller.getModel().replaceFormErrors(formCoordsWithErrors);
	}

	/**
	 * Called after data has been successfully uploaded.
	 * 
	 * @param dataOutParams -
	 *            parameters sent after data has been uploaded.
	 * @param dataOut -
	 *            data sent after the upload.
	 */
	public void uploaded(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut) {
		String message = MenuText.DATA_UPLOAD_PROBLEM();

		if (currentAction == CA_DATA_UPLOAD) {
			try {
				ResponseHeader status = (ResponseHeader) dataOutParams;
				if (status.isSuccess()) {
					
					FormUpload formUpload = (FormUpload)dataIn;
					
					UploadResponse uploadResponse = (UploadResponse) dataOut;
					StringBuffer uploadMessage = new StringBuffer();
					boolean errorsPresent = uploadResponse.getErrorCount() > 0;
					
					int sentForms = 0;
					for (int i=0; i<formUpload.getStudyCount(); i++) {
						sentForms += formUpload.getFormCount(0);
					}
					
					if (sentForms == uploadResponse.getTotalForms()) {
						uploadMessage.append("Upload complete");
						if (errorsPresent)
							uploadMessage.append("d with errors");
					} else {
						uploadMessage.append("Partial upload complete");
						if (errorsPresent)
							uploadMessage.append("d with errors");
					}
					uploadMessage.append(". Uploaded ");
					uploadMessage.append(uploadResponse.getTotalForms());
					if (uploadResponse.getTotalForms() == 1)
						uploadMessage.append(" form, ");
					else
						uploadMessage.append(" forms, ");
					if (errorsPresent) {
						uploadMessage.append(uploadResponse.getErrorCount());
						uploadMessage.append(" failed, ");
					}
					
					// Display session references
					StringBuffer sessionReferences = new StringBuffer();
					if (uploadResponse.getSuccessCount() > 0) {
						sessionReferences.append("\nSession reference(s):");
						FormDataSummary[] summary = uploadResponse.getUploadFormDataSummary();
						for (int i=0, n=summary.length; i<n; i++) {
							sessionReferences.append("\n'");
							String description = formUpload.getDataDescription(summary[i].getStudyIndex(), summary[i].getFormIndex());
							sessionReferences.append(description);
							sessionReferences.append("' = ");
							sessionReferences.append(summary[i].getReference());
						}
					}

					// Remove the successfully uploaded forms, if settings allow
					if (GeneralSettings.deleteDataAfterUpload()) {
						int deletedCount = deleteSuccessfulForms(formUpload,
								uploadResponse);
						uploadMessage.append(deletedCount);
						uploadMessage.append(" removed.");
					}
					
					uploadMessage.append(sessionReferences);
					
					message = uploadMessage.toString().intern();
					
					replaceUploadErrors(formUpload, uploadResponse);

					if (transportLayerListener != null)
						transportLayerListener.uploaded(dataInParams, dataIn, dataOutParams, dataOut);
				} 
				else
					message = MenuText.DATA_UPLOAD_FAILURE();
			} catch (Exception e) {
				e.printStackTrace();
				message = MenuText.PROBLEM_CLEANING_STORE();
			}
		} else if (currentAction == CA_SINGLE_DATA_UPLOAD) {
			ResponseHeader status = (ResponseHeader) dataOutParams;
			if (status.isSuccess()) {
				StudyDataList studyDataList = (StudyDataList)dataIn;
				UploadResponse uploadResponse = (UploadResponse) dataOut;
				
				StudyData studyData = (StudyData)studyDataList.getStudies().elementAt(0);
				FormData formData = (FormData)studyData.getForms().elementAt(0);
				if (GeneralSettings.deleteDataAfterUpload()) {
					controller.getModel().deleteFormData(
							controller.getCurrentStudy().getId(), 
							formData);
				}
				
				StringBuffer uploadMessage = new StringBuffer();
				uploadMessage.append(MenuText.DATA_UPLOAD_SUCCESS());
				FormDataSummary[] summary = uploadResponse.getUploadFormDataSummary();
				if (summary.length > 0) {
					uploadMessage.append("\nSession reference: ");
					uploadMessage.append(summary[0].getReference());
				}
				message = uploadMessage.toString();
			} else {
				message = MenuText.DATA_UPLOAD_FAILURE();
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
			if (e.getMessage() != null)
				errorMessage += " : " + e.getMessage();
			else
				errorMessage += " : " + e.getClass().getName();
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
			else if (currentAction == CA_USERS_DOWNLOAD)
				downloadUsers();
			else if (currentAction == CA_FORMS_DOWNLOAD) // TODO May need to be done after downloading users.
				downloadForms();
			else if (currentAction == CA_DATA_UPLOAD || currentAction == CA_SINGLE_DATA_UPLOAD) 
				uploadData();
			else if (currentAction == CA_LANGUAGES_DOWNLOAD)
				downloadLanguages();
			else
				alertMsg.turnOffAlert();
		}
		else
			alertMsg.turnOffAlert();
	}
	
	public void setPrevSrceen(Displayable screen){
		alertMsg.setPrevScreen(screen); //TODO Need to fix this hack
	}
	
	public void restorePrevScreen(){
		setPrevSrceen(transportLayer.getPrevScreen());
	}
}
