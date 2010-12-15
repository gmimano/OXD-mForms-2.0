package org.fcitmuk.epihandy.midp.model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.fcitmuk.epihandy.FormData;
import org.fcitmuk.epihandy.FormDef;
import org.fcitmuk.epihandy.midp.db.FormDataStore;
import org.fcitmuk.epihandy.midp.db.StoredFormDef;
import org.fcitmuk.epihandy.midp.db.StoredStudyDef;
import org.fcitmuk.epihandy.midp.db.StudyStore;

/**
 * The state of the application.
 * 
 * @author batkinson
 * 
 */
public class Model {

	private StudyStore studyStore;
	private FormDataStore dataStore;

	private StoredStudyDef[] studies = {};
	private StoredFormDef[] studyForms = {};
	private FormDef activeForm = null; // Needed to avoid loading all in memory

	private Vector formDataIds = null;
	private int selectedStudy = -1;
	private int selectedForm = -1;
	private int selectedData = -1;

	private Vector listeners = new Vector();

	public Model(StudyStore studyStore, FormDataStore dataStore) {
		this.studyStore = studyStore;
		this.dataStore = dataStore;
	}

	public StoredStudyDef[] getStudies() {
		return studies;
	}

	public void setStudies(StoredStudyDef[] studies) {
		if (studies == null)
			studies = new StoredStudyDef[] {};
		this.studies = studies;
		selectedStudy = -1;
		fireStudiesChanged();
	}

	private void fireStudiesChanged() {
		for (int i = 0; i < listeners.size(); i++)
			((ModelListener) listeners.elementAt(i)).studiesChanged(this);
	}

	public StoredFormDef[] getStudyForms() {
		return studyForms;
	}

	public void setStudyForms(StoredFormDef[] studyForms) {
		if (studyForms == null)
			studyForms = new StoredFormDef[] {};
		this.studyForms = studyForms;
		selectedForm = -1;
		fireFormsChanged();
	}

	private void fireFormsChanged() {
		for (int i = 0; i < listeners.size(); i++)
			((ModelListener) listeners.elementAt(i)).formsChanged(this);
	}

	public StoredStudyDef getStudyDef(int study) {
		if (studies != null && study >= 0 && study < studies.length)
			return studies[study];
		return null;
	}

	public int getSelectedStudyIndex() {
		return selectedStudy;
	}

	public StoredStudyDef getSelectedStudyDef() {
		return getStudyDef(selectedStudy);
	}

	public void setSelectedStudyIndex(int selectedStudy) {
		boolean isReselection = selectedStudy == this.selectedStudy;
		this.selectedStudy = selectedStudy;
		fireStudySelected();
		if (!isReselection)
			setStudyForms(studyStore.getFormDefList(getSelectedStudyDef()));
	}

	private void fireStudySelected() {
		for (int i = 0; i < listeners.size(); i++)
			((ModelListener) listeners.elementAt(i)).studySelected(this);
	}

	public StoredFormDef getFormDef(int form) {
		if (studyForms != null && form >= 0 && form < studyForms.length)
			return studyForms[form];
		return null;
	}
	
	public FormDef getFormDef(int studyId, int formId) {
		return studyStore.getFormDef(studyId, formId);
	}

	public int getSelectedFormIndex() {
		return selectedForm;
	}

	public void setSelectedFormIndex(int selectedForm) {
		boolean isReselection = this.selectedForm == selectedForm;
		this.selectedForm = selectedForm;
		activeForm = studyStore.getFormDef(studies[selectedStudy],
				studyForms[selectedForm]);
		if (!isReselection) {
			formDataIds = null; // Form data may not match form def
			selectedData = -1;
		}
		fireFormSelected();
		if (!isReselection)
			setFormData(); // Pull up form data for selected form def
	}

	public StoredFormDef getSelectedFormDef() {
		return getFormDef(selectedForm);
	}

	private void fireFormSelected() {
		for (int i = 0; i < listeners.size(); i++)
			((ModelListener) listeners.elementAt(i)).formSelected(this);
	}

	public FormDef getActiveForm() {
		return activeForm;
	}

	private void setFormData() {
		if (selectedStudy >= 0 && selectedStudy < studies.length
				&& selectedForm >= 0 && selectedForm < studyForms.length)
			formDataIds = dataStore.getFormDataIds(getSelectedStudyDef()
					.getId(), getSelectedFormDef().getId());
		fireDataChanged();
	}

	public int indexOf(FormData searchItem) {
		int loc = -1;
		int studyId = getSelectedStudyDef().getId();
		int formDefId = getSelectedFormDef().getId();
		if (formDataIds != null)
			for (int i = 0; i < formDataIds.size(); i++) {
				int recId = ((Integer) formDataIds.elementAt(i)).intValue();
				FormData item = dataStore
						.getFormData(studyId, formDefId, recId);
				if (item.getDefId() == searchItem.getDefId()
						&& item.getRecordId() == searchItem.getRecordId())
					loc = i;
			}
		return loc;
	}

	public void setSelectedFormDataIndex(int selectedData) {
		this.selectedData = selectedData;
		fireDataSelected();
	}

	private void fireDataSelected() {
		for (int i = 0; i < listeners.size(); i++)
			((ModelListener) listeners.elementAt(i)).formDataSelected(this);
	}

	public int getSelectedFormDataIndex() {
		return selectedData;
	}

	public int getSelectedFormDataCount() {
		if (formDataIds != null)
			return formDataIds.size();
		else
			return 0;
	}
	
	public FormData getSelectedFormData() {
		if (formDataIds != null && selectedData >= 0
				&& selectedData < formDataIds.size()) {
			int selectedDataRecId = ((Integer) formDataIds
					.elementAt(selectedData)).intValue();
			FormData data = getFormData(getSelectedStudyDef().getId(),
					getSelectedFormDef().getId(), selectedDataRecId);
			data.setDef(getActiveForm());
			return data;
		}
		return null;
	}

	public boolean storedDataExists() {
		return dataStore.hasStoredData();
	}

	public boolean storedDataExistsForStudy(int studyId) {
		return dataStore.hasStoredDataForStudy(studyId);
	}
	
	public boolean storedDataExistsForFormDef(int studyId, int formId) {
		return dataStore.hasStoredDataForFormDef(studyId, formId);
	}

	public boolean studyInError(int study) {
		return dataStore.studyInError(study);
	}

	public boolean formDefInError(int studyId, int formId) {
		return dataStore.formDefInError(studyId, formId);
	}

	public boolean formDataInError(int studyId, int formId, int recordId) {
		return dataStore.formDataInError(studyId, formId, recordId);
	}

	public Vector getFormDataIds(int studyId, int formId) {
		return dataStore.getFormDataIds(studyId, formId);
	}
	
	public Vector getFormData(int studyId, int formId) {
		return dataStore.getFormData(studyId, formId);
	}

	public FormData getFormData(int studyId, int formId, int recordId) {
		return dataStore.getFormData(studyId, formId, recordId);
	}
	
	public FormData getFormDataByPosition(int listPos) {
		int recId = ((Integer) formDataIds.elementAt(listPos)).intValue();
		return dataStore.getFormData(getSelectedStudyDef().getId(),
				getSelectedFormDef().getId(), recId);
	}

	public boolean saveFormData(int studyId, FormData formData) {
		try {
			return dataStore.saveFormData(studyId, formData);
		} finally {
			setFormData();
		}
	}

	public void deleteFormData(int studyId, FormData formData) {
		deleteFormData(studyId, formData.getDefId(), formData.getRecordId());
	}

	public void deleteFormData(int studyId, int formId, int recordId) {
		try {
			dataStore.deleteFormData(studyId, formId, recordId);
		} finally {
			setFormData();
			fireErrorsChanged();
		}
	}

	public void deleteFormData(Vector formCoords) {
		for (int i = 0; i < formCoords.size(); i++) {
			int[] formDataId = (int[]) formCoords.elementAt(i);
			dataStore.deleteFormData(formDataId[0], formDataId[1],
					formDataId[2]);
		}
		setFormData();
		fireErrorsChanged();
	}

	private void fireDataChanged() {
		for (int i = 0; i < listeners.size(); i++)
			((ModelListener) listeners.elementAt(i)).formDataChanged(this);
	}

	public String getFormError(int studyId, FormData formData) {
		return dataStore.getFormDataError(studyId, formData);
	}

	public void deleteFormError(int studyId, FormData formData) {
		dataStore.deleteFormDataError(studyId, formData.getDefId(), formData
				.getRecordId());
		fireErrorsChanged();
	}

	public void replaceFormErrors(Vector formCoordsWithErrors) {
		dataStore.clearFormDataErrors();
		for (int i = 0; i < formCoordsWithErrors.size(); i++) {
			Object[] elem = (Object[]) formCoordsWithErrors.elementAt(i);
			int[] formDataId = (int[]) elem[0];
			String formError = (String) elem[1];
			dataStore.saveFormDataError(formDataId[0], formDataId[1],
					formDataId[2], formError);
		}
		fireErrorsChanged();
	}

	private void fireErrorsChanged() {
		for (int i = 0; i < listeners.size(); i++)
			((ModelListener) listeners.elementAt(i)).formErrorsChanged(this);
	}

	public Vector streamFormData(DataOutputStream dos) throws IOException {
		return dataStore.streamStoredForms(dos);
	}

	public void addModelListener(ModelListener listener) {
		listeners.addElement(listener);
	}

	public void removeModelListener(ModelListener listener) {
		listeners.removeElement(listener);
	}

	public boolean isSelectedStudyFull() {
		return dataStore.getDataCountForStudy(selectedStudy) >= Short.MAX_VALUE;
	}
}
