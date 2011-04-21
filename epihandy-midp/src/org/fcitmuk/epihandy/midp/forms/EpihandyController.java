package org.fcitmuk.epihandy.midp.forms;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.fcitmuk.communication.TransportLayer;
import org.fcitmuk.communication.TransportLayerListener;
import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.epihandy.EpihandyConstants;
import org.fcitmuk.epihandy.FormData;
import org.fcitmuk.epihandy.QuestionData;
import org.fcitmuk.epihandy.QuestionDef;
import org.fcitmuk.epihandy.RepeatQtnsDef;
import org.fcitmuk.epihandy.SkipRule;
import org.fcitmuk.epihandy.ValidationRule;
import org.fcitmuk.epihandy.midp.db.FormDataStore;
import org.fcitmuk.epihandy.midp.db.SelectionPersister;
import org.fcitmuk.epihandy.midp.db.StoredStudyDef;
import org.fcitmuk.epihandy.midp.db.StudyStore;
import org.fcitmuk.epihandy.midp.model.Model;
import org.fcitmuk.epihandy.midp.transport.StudyDownload;
import org.fcitmuk.epihandy.midp.transport.UsersAndFormDownload;
import org.fcitmuk.midp.db.util.Settings;
import org.fcitmuk.midp.db.util.StorageListener;
import org.fcitmuk.midp.mvc.Controller;
import org.fcitmuk.midp.mvc.View;
import org.fcitmuk.util.AlertMessage;
import org.fcitmuk.util.AlertMessageListener;
import org.fcitmuk.util.DefaultCommands;
import org.fcitmuk.util.MenuText;


/**
 * Manages cordination of views within the application. It knows all the views.
 * Views do not know about each other. All they know is the controller and as a result
 * send requests to it. These requests normally require switching of views and its the 
 * controller that knows which view to switch to.
 * In otherwards, views speak to each other through the controller.
 * 
 * @author Daniel Kayiwa
 *
 */
public class EpihandyController implements Controller, StorageListener, AlertMessageListener,TypeEditorListener, TransportLayerListener {

	private boolean studyEditingMode;
	private Displayable prevScreen;
	private AlertMessage alertMsg;
	private Hashtable transitionTable;
	private View currentView;
	private Display display;

	/** A list of user defined data type editors. */
	private Hashtable typeEditors = new Hashtable(); //TODO This need to be implemmted such that we allow flexibilty of user defined type editors for those who dont want to subclass the default type editor.

	private FormView formViewer;
	private FormDefListView formDefListViewer;
	private FormDataListView formDataListViewer;
	private TypeEditor typeEditor = new DefaultTypeEditor();
	private StudyListView studyListViewer;
	private RptQtnsDataListView rptQtnsDataListViewer = new RptQtnsDataListView();

	private DownloadUploadManager downloadMgr;
	private UserManager userMgr;
	private FormManager formMgr;

	/** No alert is currently displayed. */
	private static final byte CA_NONE = -1;
	private static final byte CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD = 1;
	private static final byte CA_SELECT_FORM_AFTER_STUDY_DOWNLOAD = 2;
	private static final byte CA_SELECT_FORM_AFTER_STUDY_SELECT = 3;
	private static final byte CA_DOWNLOAD_FORMS_AFTER_STUDY_SELECT = 4;

	private byte currentAction = CA_NONE;
	
	private Model model;
	private StudyStore studyStore;
	private FormDataStore dataStore;
	
	private LogoutListener logoutListener;

	public EpihandyController() {
		studyStore = new StudyStore();
		dataStore = new FormDataStore();
		model = new Model(studyStore, dataStore);
		studyListViewer = new StudyListView(model);
		formDefListViewer = new FormDefListView(model);
		formDataListViewer = new FormDataListView(model);
		formViewer = new FormView(model);
	}

	public void init(String title, Display display, Displayable currentScreen,TransportLayer transportLayer, LogoutListener logoutListener){
		this.logoutListener = logoutListener;
		if (currentScreen != null) {
			this.prevScreen = currentScreen;
		} else {
			//if (GeneralSettings.isHideStudies()) {
			//	formDefListViewer.setStudy(null);
			//	this.prevScreen = formDefListViewer.getScreen();
			//} else {
				this.prevScreen = studyListViewer.getScreen();
			//}
		}
		this.display = display;
		
		setDefaults(title);
		
		// Initialize the study list to start
		model.setStudies(studyStore.getStudyDefList());
		
		// Now select what was selected last time
		Settings settings = new Settings(
				EpihandyConstants.STORAGE_NAME_EPIHANDY_SETTINGS, true);
		String studySelection = settings.getSetting(
				EpihandyConstants.KEY_LAST_SELECTED_STUDY, "-1");
		String formSelection = settings.getSetting(
				EpihandyConstants.KEY_LAST_SELECTED_FORMDEF, "-1");
		String dataSelection = settings.getSetting(
				EpihandyConstants.KEY_LAST_SELECTED_FORMDATA, "-1");
		int lastStudy = Integer.parseInt(studySelection);
		int lastForm = Integer.parseInt(formSelection);
		int lastData = Integer.parseInt(dataSelection);
		if (lastStudy >= 0 && lastStudy < model.getStudies().length) {
			model.setSelectedStudyIndex(lastStudy);
			if (lastForm >= 0 && lastForm < model.getStudyForms().length) {
				model.setSelectedFormIndex(lastForm);
				if (lastData >= 0 && lastData < model.getSelectedFormDataCount())
					model.setSelectedFormDataIndex(lastData);
			}
		}
		
		// Start listening to selection changes and persisting
		model.addModelListener(new SelectionPersister(settings));
		
		alertMsg = new AlertMessage(display, title, this.prevScreen, this);
		transitionTable = new Hashtable();

		if(model.getSelectedStudyDef() != null){
			String name = model.getSelectedStudyDef().getName();
			if(name != null && name.trim().length() > 0)
				prevScreen.setTitle(title + " - " + name);
		}
	}

	private void setDefaults(String title){
		studyListViewer.setController(this);
		formDefListViewer.setController(this);
		formDataListViewer.setController(this);
		formViewer.setController(this);
		typeEditor.setController(this);

		studyListViewer.setDisplay(display);
		formDefListViewer.setDisplay(display);
		formDataListViewer.setDisplay(display);
		formViewer.setDisplay(display);
		typeEditor.setDisplay(display);

		studyListViewer.setTitle(title);
		formDefListViewer.setTitle(title);
		formDataListViewer.setTitle(title);
		formViewer.setTitle(title);
		typeEditor.setTitle(title);
	}

	public void setPrevScreen(Displayable prevScreen){
		this.prevScreen = prevScreen;
	}

	public Displayable getPrevScreen() {
		return this.prevScreen;
	}

	public void setStudyEditingMode(boolean studyEditingMode){
		this.studyEditingMode = studyEditingMode;
	}

	private void showErrorMessage(String text, Exception e){
		alertMsg.showError(text);
	}

	/**
	 * Shows a form given its data.
	 */
	public void showForm(int selectedData){
		
		boolean isNewForm = selectedData == -1;

		if (isNewForm && model.isSelectedStudyFull()) {
			alertMsg.showError(MenuText.STUDY_FULL_UPLOAD());
			return;
		}
		
		try{
			setStudyEditingMode(true);
			model.setSelectedFormDataIndex(selectedData);
			formViewer.showForm();
			if (selectedData < 0) // Fixes cancel when skipping data list
				transitionTable.put(formDataListViewer, formDefListViewer);
			saveCurrentView(formViewer);
		}
		catch(Exception e){
			showErrorMessage("Exception:"+ e.getMessage(),e);
		}
	}
	
	public void startEdit(QuestionData currentQuestion,int pos, int count){
		//Inform the user that we are about to start editing.
		
		TypeEditor editor = typeEditor;
		Byte type = new Byte(currentQuestion.getDef().getType());
		if(typeEditors.containsKey(type))
			editor = (TypeEditor)typeEditors.get(type);

		FormData formData = formViewer.getFormData();
		ValidationRule rule = formData.getDef().getValidationRule(currentQuestion.getId());
		if(rule != null)
			rule.setFormData(formData);
		editor.setTitle(formData.getDef().getName()+ " - " + formViewer.getTitle());
		editor.startEdit(currentQuestion,rule, GeneralSettings.isSingleQtnEdit(), pos, count, this);
		
		if(editor instanceof RepeatTypeEditor)
			((RepeatTypeEditor)editor).setCurrentFormData(formViewer.getFormData());
		
		//no need to save the current view since its managed by the form viewer.
	}

	/**
	 * Sets a custom editor of a question type.
	 * 
	 * @param type - the question type.
	 * @param typeEditor - the editor.
	 */
	public void setTypeEditor(byte type, TypeEditor typeEditor){
		this.typeEditors.put(new Byte(type), typeEditor);
	}

	/** Stops editing of a question. */
	public void endEdit(boolean save, QuestionData data, Command cmd){
		if(save){
			FireSkipRules(formViewer.getFormData());
			formViewer.getFormData().buildQuestionDataDescription();
			
			int type = data.getDef().getType();
			if(type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || type == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)
				formViewer.getFormData().updateDynamicOptions(data,false);
		}

		this.formViewer.onEndEdit(save,cmd);
		//no saving of current view since it was type editor displayed.
	}

	/** Fires rules in the form. */
	void FireSkipRules(FormData formData){		
		Vector rules = formData.getDef().getSkipRules();
		if(rules != null && rules.size() > 0){
			for(int i=0; i<rules.size(); i++){
				SkipRule rule = (SkipRule)rules.elementAt(i);
				rule.fire(formData);
			}
		}
	}

	/** Saves the current form data. */
	public void saveFormData(FormData formData){	
		boolean isNew = formData.isNew();
		
		formData.setDateValue("/"+formData.getDef().getVariableName()+"/endtime", new Date());
		
		if(model.saveFormData(model.getSelectedStudyDef().getId(),formData)){
			model.setSelectedFormDataIndex(model.indexOf(formData));
			currentView = (View)transitionTable.get(formViewer);
			transitionTable.remove(formViewer);

			if(this.studyEditingMode)
				formDataListViewer.onFormSaved(formData, isNew);
			else
				display.setCurrent(this.prevScreen);
		}
	}

	public void deleteForm(FormData formData) {

		model.deleteFormData(model.getSelectedStudyDef().getId(), formData);

		// We show def list of no data left, data list if there is still data
		if (model.getSelectedFormDataCount() > 0)
			formDataListViewer.showFormList();
		else
			formDefListViewer.show();
	}

	public StoredStudyDef [] getStudyList(){
		return model.getStudies();
	}

	public void showFormDefList(){
		if(formsDownloaded2()){
			formDefListViewer.showFormList();
			saveCurrentView(formDefListViewer);
		}
	}

	public void showFormDataList(int formDefIndex) {
		model.setSelectedFormIndex(formDefIndex);
		if (model.getSelectedFormDataCount() > 0) {
			formDataListViewer.showFormList();
			saveCurrentView(formDataListViewer);
		} else
			showForm(-1);
	}

	public void selectStudy(boolean forEditing){
		if(model.getStudies() == null || model.getStudies().length == 0){
			if(currentAction != CA_DOWNLOAD_FORMS_AFTER_STUDY_SELECT)
				currentAction  = CA_SELECT_FORM_AFTER_STUDY_DOWNLOAD;
			downloadMgr.setTransportLayerListener(this);
			downloadMgr.downloadStudies(prevScreen,userMgr.getUserName(),userMgr.getPassword(),false);
		}
		else{
			this.setStudyEditingMode(forEditing);
			this.studyListViewer.showStudyList(model.getStudies());
		}
	}
	
	public void downloadStudies() {
		downloadMgr.downloadStudies(prevScreen,userMgr.getUserName(), userMgr.getPassword(),true);
	}
	
	public void downloadStudyForms(Displayable currentScreen) {
		downloadMgr.downloadStudyForms(currentScreen, userMgr.getUserName(), userMgr.getPassword(), true);
	}
	
	public void displayUserSettings(Displayable currentScreen) {
		formMgr.displayUserSettings(display, currentScreen);
	}
	
	public void uploadData(Displayable currentScreen) {
		downloadMgr.uploadData(currentScreen, getStudyList(), null, userMgr.getUserName(), userMgr.getPassword());
	}
	
	public void uploadData(Displayable currentScreen, int studyId, int formDefId, int recordId) {
		FormData formData = getModel().getFormData(studyId, formDefId, recordId);
		formData.setDef(getModel().getActiveForm()); // formData doesn't contain the form definition
		downloadMgr.uploadData(currentScreen, getStudyList(), formData, userMgr.getUserName(), userMgr.getPassword());
	}
	
	public void logout() {
		logoutListener.onLogout();
	}

	public void closeStudyList(boolean save, StoredStudyDef studyDef){

		if(save){
			//if(studyEditingMode)
				showFormDefList();
			prevScreen.setTitle(alertMsg.getTitle() + " - " + studyDef.getName());
		}

		//if(!studyEditingMode)
			//sdisplay.setCurrent(prevScreen);
		

		/*if (studyDef.getId() != currentStudy.getId()) {
			// only open selected study if a different/new study is selected
			studyDef = getStudyWithForms(null,studyDef);
	
			if (save) {
				//Save settings for next run (i think this is always set??)
				Settings settings = new Settings(OpenXdataConstants.STORAGE_NAME_EPIHANDY_SETTINGS,true);
				settings.setSetting(OpenXdataConstants.KEY_LAST_SELECTED_STUDY,String.valueOf(studyDef.getId()));
				settings.saveSettings();
	
				formDefListViewer.setStudy(studyDef);
	
				prevScreen.setTitle(alertMsg.getTitle() + " - " + studyDef.getName());
			}
		} else {
			studyDef = currentStudy; // currentStudy could contain forms
		}
		
		if (studyEditingMode || !GeneralSettings.isMainMenu()) {
			showFormDefList(studyDef);
		} else {
			display.setCurrent(prevScreen);
		}*/

	}

	public void errorOccured(String errorMessage, Exception e){
		if(e != null)
			errorMessage += " : " + e.getMessage();
		showErrorMessage(errorMessage,e);
	}

	/**
	 * This is callback when one hits the Ok button for an alert message.
	 */
	public void onAlertMessage(byte msg){
		display.setCurrent(prevScreen);
	}

	/**
	 * Any view where the user hits the cancel command, calls into this method
	 * to allow the controller display the previous view.
	 */
	public void handleCancelCommand(Object viewer){
		View view = (View)transitionTable.get(viewer);
		if(view != null){
			transitionTable.remove(viewer);
			view.show(); //AbstractView.getDisplay().setCurrent(view.getScreen());
			currentView = view;
		}
		else{
			display.setCurrent(prevScreen);
			currentView = null;
		}
	}

	/**
	 * Before displaying a view, saves the one which was current.
	 * This is for rembering view to diplay on closing one or pressing the back button.
	 * 
	 * @param newView - the view which is to be displayed.
	 */
	private void saveCurrentView(View newView){
		if(currentView != null)
			transitionTable.put(newView, currentView);

		currentView = newView;
	}

	/**
	 * For the curren study, shows alist of forms and allows one to start entering data in any selected one.
	 */
	public void selectForm(boolean editingMode, Displayable currentScreen){
		setStudyEditingMode(editingMode);
		setPrevScreen(currentScreen);

		if(model.getSelectedStudyDef() == null){
			currentAction  = CA_SELECT_FORM_AFTER_STUDY_SELECT;
			this.selectStudy(editingMode);
		}
		else if(formsDownloaded2()){
			formDefListViewer.showFormList();
			saveCurrentView(formDefListViewer);
		}
		else
			currentAction  = CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD;
	}

	private boolean formsDownloaded2(){
		boolean bReturn = false;

		if(model.getSelectedStudyDef() == null || model.getStudyForms() == null || model.getStudyForms().length == 0){
			currentAction  = CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD;
			downloadMgr.setTransportLayerListener(this);
			downloadMgr.downloadStudyForms(prevScreen,userMgr.getUserName(), userMgr.getPassword(),false);
		}
		else
			bReturn = true;


		return bReturn;
	}

	public void execute(View view, Object cmd, Object data){
		if(cmd == DefaultCommands.cmdCancel)
			display.setCurrent(prevScreen);
		else{
			if(view instanceof StudyListView)
				closeStudyList(true,(StoredStudyDef)data);
		}
	}

	public StoredStudyDef getCurrentStudy(){
		StoredStudyDef study = model.getSelectedStudyDef();

		if(study == null){
			currentAction = CA_DOWNLOAD_FORMS_AFTER_STUDY_SELECT;
			this.selectStudy(this.studyEditingMode);
		}
		return study;
	}

	public void showRepeatQtnsDataList(RepeatQtnsDef repeatQtnsDef){
		saveCurrentView(rptQtnsDataListViewer);

	}

	public void backToMainMenu(){
		display.setCurrent(prevScreen);
	}

	public void setDownloadManager(DownloadUploadManager downloadMgr){
		this.downloadMgr = downloadMgr;
	}

	public void setUserManager(UserManager userMgr){
		this.userMgr = userMgr;
	}
	
	public void setFormManager(FormManager formMgr) {
		this.formMgr = formMgr;
	}

	public void downloaded(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut) {
		if(currentAction == CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD)
			selectForm(this.studyEditingMode, prevScreen);
		else if(currentAction == CA_SELECT_FORM_AFTER_STUDY_DOWNLOAD){
			if(model.getStudies().length == 1){
				model.setSelectedStudyIndex(0);
				this.execute(studyListViewer, DefaultCommands.cmdOk, model.getStudies()[0]);
				currentAction = CA_SELECT_FORM_AFTER_FORMS_DOWNLOAD;
				return;
			}
			else
				selectStudy(this.studyEditingMode);
		}
		else if(currentAction == CA_DOWNLOAD_FORMS_AFTER_STUDY_SELECT)
			this.downloadMgr.downloadStudyForms(prevScreen, userMgr.getUserName(), userMgr.getPassword(), false);
		currentAction = CA_NONE;
	}

	public void uploaded(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut) {
	}

	public void cancelled(){

	}

	public void updateCommunicationParams(){

	}

	public Model getModel() {
		return model;
	}
	
	public StudyDownload getStudyDownload() {
		return new StudyDownload(studyStore, model);
	}

	public UsersAndFormDownload getUserAndFormDownload() {
		return new UsersAndFormDownload(studyStore, model);
	}
}
