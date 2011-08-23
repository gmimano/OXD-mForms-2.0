package org.fcitmuk.epihandy.midp.db;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.fcitmuk.db.util.Record;
import org.fcitmuk.epihandy.FormData;
import org.fcitmuk.epihandy.FormDataError;
import org.fcitmuk.midp.db.util.Storage;
import org.fcitmuk.midp.db.util.StorageFactory;

public class FormDataStore {

	private static final String IDENTIFIER_FORM_DATA_STORAGE = "FormData";
	private static final String IDENTIFIER_FORMDATA_ERROR_STORAGE = "FormErrors";
	private static final String STORAGE_NAME_SEPARATOR = ".";

	Vector errors;
	
	/**
	 * Returns whether there are records related to form data.
	 * 
	 * @return true if there are form data records, false otherwise.
	 */
	public boolean hasStoredData() {
		String[] names = StorageFactory.getNames();
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if (name.startsWith(IDENTIFIER_FORM_DATA_STORAGE)) {
				Storage store = StorageFactory.getStorage(name, null);
				if (store.getNumRecords() > 0)
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether there are form data records for the specified study.
	 * 
	 * @param studyId
	 * @return
	 */
	public boolean hasStoredDataForStudy(int studyId) {
		String sep = STORAGE_NAME_SEPARATOR;
		String studyDataPrefix = IDENTIFIER_FORM_DATA_STORAGE + sep + studyId
				+ sep;
		String[] names = StorageFactory.getNames();
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if (name.startsWith(studyDataPrefix)) {
				Storage store = StorageFactory.getStorage(name, null);
				if (store.getNumRecords() > 0)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the number of form data records for the specified study.
	 * 
	 * @param studyId
	 * @return count of forms
	 */
	public int getDataCountForStudy(int studyId) {
		int count = 0;
		String sep = STORAGE_NAME_SEPARATOR;
		String dataPrefix = IDENTIFIER_FORM_DATA_STORAGE + sep + studyId + sep;
		String[] names = StorageFactory.getNames();
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if (name.startsWith(dataPrefix)) {
				Storage store = StorageFactory.getStorage(name, null);
				count += store.getNumRecords();
			}
		}
		return count;
	}
	
	/**
	 * Returns whether there are form data records for the specified form
	 * definition.
	 * 
	 * @param studyId
	 * @return
	 */
	public boolean hasStoredDataForFormDef(int studyId, int formId) {
		String storageName = getFormDataStorageName(studyId, formId);
		Storage store = StorageFactory.getStorage(storageName, null);
		if (store.getNumRecords() > 0)
			return true;
		return false;
	}

	private Vector getFormDataStorageNames() {
		// Find all form data storage record names
		String[] names = StorageFactory.getNames();
		Vector formNames = new Vector(names.length);
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if (name.startsWith(IDENTIFIER_FORM_DATA_STORAGE)) {
				formNames.addElement(name);
			}
		}
		names = null;
		return formNames;
	}

	public Vector streamStoredForms(DataOutputStream dos) throws IOException {

		int[][] ids = {};

		Vector formDataFileNames = getFormDataStorageNames();
		// Filter so we only consider stores that contain records.
		for (int i = formDataFileNames.size() - 1; i >= 0; i--) {
			String formDataFileName = (String) formDataFileNames.elementAt(i);
			Storage store = StorageFactory.getStorage(formDataFileName, null);
			if (store.getNumRecords() <= 0)
				formDataFileNames.removeElementAt(i);
		}

		// Use the storage names to construct studyId/formId pairs
		ids = new int[formDataFileNames.size()][];
		for (int i = 0; i < formDataFileNames.size(); i++) {
			ids[i] = getIdsFromFormStorageName((String) formDataFileNames
					.elementAt(i));
		}
		formDataFileNames = null;

		sortIds(ids);

		// Organize the ids into studies and compute form counts per study
		Vector studyFormIds = new Vector();
		Hashtable studyCounts = new Hashtable();
		for (int i = 0, lastId = -1; i < ids.length; i++) {
			if (i == 0 || ids[i][0] != lastId) {
				lastId = ids[i][0];
				studyFormIds.addElement(new Vector());
			}

			// Add the form ids to the most recently added study
			((Vector) studyFormIds.elementAt(studyFormIds.size() - 1))
					.addElement(ids[i]);

			int studyId = ids[i][0], formId = ids[i][1];

			// Get the number of records for formdef
			String formDataStorageName = getFormDataStorageName(studyId, formId);
			int formDefRecordCount = StorageFactory.getStorage(
					formDataStorageName, null).getNumRecords();

			// Add the count to the total for the study
			Integer studyCount = (Integer) studyCounts
					.get(new Integer(studyId));
			if (studyCount == null)
				studyCount = new Integer(formDefRecordCount);
			else
				studyCount = new Integer(studyCount.intValue()
						+ formDefRecordCount);

			// Update the table with the form total
			studyCounts.put(new Integer(studyId), studyCount);
		}

		dos.writeShort(studyFormIds.size());

		// Tabulate forms ids grouped into studies: each form is studyId,
		// formId, and record number
		Vector uploadedStudies = new Vector();
		for (int sfidx = 0; sfidx < studyFormIds.size(); sfidx++) {
			Vector formIds = (Vector) studyFormIds.elementAt(sfidx);
			Vector uploadedStudy = new Vector();
			uploadedStudies.addElement(uploadedStudy);
			for (int fidx = 0; fidx < formIds.size(); fidx++) {
				int[] id = (int[]) formIds.elementAt(fidx);
				int studyId = id[0], formId = id[1];

				if (fidx == 0) {
					dos.writeInt(studyId);
					int studyFormCount = ((Integer) studyCounts
							.get(new Integer(studyId))).intValue();
					dos.writeShort(studyFormCount);
				}

				String formDataStorageName = getFormDataStorageName(studyId,
						formId);
				Vector records = StorageFactory.getStorage(formDataStorageName,
						null).writeBytes(dos);
				for (int ridx = 0; ridx < records.size(); ridx++) {
					int recordNum = ((Integer) records.elementAt(ridx))
							.intValue();
					uploadedStudy.addElement(new int[] { studyId, formId,
							recordNum });
				}
			}
		}

		return uploadedStudies;
	}

	/**
	 * Returns the study and form id contained within a form data storage name.
	 * 
	 * @param name
	 *            the form data storage name
	 * @return an int[2] containing studyId and formId, or int[0]
	 */
	private int[] getIdsFromFormStorageName(String name) {
		int[] result = {};
		int[] ids = new int[2];
		int sep1 = name.indexOf(STORAGE_NAME_SEPARATOR);
		if (sep1 >= 0) {
			int sep2 = name.indexOf(STORAGE_NAME_SEPARATOR, sep1 + 1);
			if (sep2 >= 0) {
				ids[0] = Integer.parseInt(name.substring(sep1 + 1, sep2));
				ids[1] = Integer.parseInt(name.substring(sep2 + 1));
				result = ids;
			}
		}
		return result;
	}

	/**
	 * Sorts an array of int[2] by using the integer at index 0. Uses insertion
	 * sort (anticipates small collection).
	 * 
	 * @param ids
	 */
	private void sortIds(int[][] ids) {
		for (int i = 1; i < ids.length; i++) {
			int[] idpair = ids[i];
			int j = i - 1;
			boolean complete = false;
			do {
				if (ids[j][0] > idpair[0]) {
					ids[j + 1] = ids[j];
					j--;
					if (j < 0)
						complete = true;
				} else
					complete = true;
			} while (!complete);
			ids[j + 1] = idpair;
		}
	}

	/**
	 * Saves form data.
	 * 
	 * @param studyId
	 *            - the numeric unique identifier of the study.
	 * @param data
	 *            - the data to be saved.
	 */
	public boolean saveFormData(int studyId, FormData data) {
		Storage store = StorageFactory.getStorage(getFormDataStorageName(
				studyId, data.getDefId()), null);
		return store.save((Record) data);
	}

	/**
	 * Gets data collected for a form of a particular type (definition).
	 * 
	 * @param studyId
	 *            - the numeric unique identifier of the study.
	 * @param formDefId
	 *            - the numeric unique identifier of the form definition
	 * @return - a list of collected data for this form.
	 */
	public Vector getFormData(int studyId, int formDefId) {
		Storage store = StorageFactory.getStorage(getFormDataStorageName(
				studyId, formDefId), null);
		return store.read(FormData.class);
	}

	/**
	 * Gets data collected for a form.
	 * 
	 * @param studyId
	 *            - the numeric unique identifier of the study.
	 * @param formDefId
	 *            - the numeric unique identifier of the form definition
	 * @param recordId
	 *            - the numeric unique identifier of the form data record.
	 * @return - the form data.
	 */
	public FormData getFormData(int studyId, int formDefId, int recordId) {
		Storage store = StorageFactory.getStorage(getFormDataStorageName(
				studyId, formDefId), null);
		return (FormData) store.read(recordId, FormData.class);
	}

	/**
	 * Gets the record ids of the form data for the specified store.
	 * 
	 * @param studyId
	 * @param formDefId
	 * @return an array of Integer record ids
	 */
	public Vector getFormDataIds(int studyId, int formDefId) {
		Storage store = StorageFactory.getStorage(getFormDataStorageName(
				studyId, formDefId), null);
		return store.readIds();
	}

	/**
	 * Deletes form data.
	 * 
	 * @param studyId
	 *            - the numeric unique identifier of the study.
	 * @param data
	 *            - the data to be deleted.
	 */
	public void deleteFormData(int studyId, FormData data) {
		deleteFormData(studyId, data.getDefId(), data.getRecordId());
	}

	/**
	 * Deletes form data using the storage coordinates of the form.
	 * 
	 * @param studyId
	 * @param formId
	 * @param recId
	 */
	public void deleteFormData(int studyId, int formId, int recId) {
		Storage store = StorageFactory.getStorage(getFormDataStorageName(
				studyId, formId), null);
		store.delete(recId);
		deleteFormDataError(studyId, formId, recId);
	}

	public void deleteFormDataError(int studyId, int formId, int recId) {
		Storage store = StorageFactory.getStorage(
				IDENTIFIER_FORMDATA_ERROR_STORAGE, null);
		FormDataError existingError = getFormDataErrorObj(studyId, formId,
				recId);
		if (existingError != null) {
			store.delete(existingError);
			requestErrorReload();
		}
	}

	private void requestErrorReload() {
		errors = null;
	}
	
	public boolean studyInError(int studyId) {
		Vector errors = getFormDataErrors();

		if (errors == null)
			return false;

		for (int i = 0; i < errors.size(); i++) {
			FormDataError error = (FormDataError) errors.elementAt(i);
			if (error.getStudyId() == studyId)
				return true;
		}
		return false;
	}

	public boolean formDefInError(int studyId, int formId) {
		Vector errors = getFormDataErrors();

		if (errors == null)
			return false;

		for (int i = 0; i < errors.size(); i++) {
			FormDataError error = (FormDataError) errors.elementAt(i);
			if (error.getStudyId() == studyId && error.getFormId() == formId)
				return true;
		}
		return false;
	}

	public boolean formDataInError(int studyId, int formId, int recId) {
		return getFormDataErrorObj(studyId, formId, recId) != null;
	}

	private FormDataError getFormDataErrorObj(int studyId, int formId, int recId) {
		Vector vect = getFormDataErrors();
		if (vect != null)
			for (int i = 0; i < vect.size(); i++) {
				FormDataError error = (FormDataError) vect.elementAt(i);
				if (error.getStudyId() == studyId
						&& error.getFormId() == formId
						&& error.getFormDataId() == recId)
					return error;
			}
		return null;
	}

	private Vector getFormDataErrors() {

		if (errors == null) {
			Storage store = StorageFactory.getStorage(
					IDENTIFIER_FORMDATA_ERROR_STORAGE, null);
			errors = store.read(FormDataError.class);
		}
		return errors;
	}

	public void clearFormDataErrors() {
		Storage store = StorageFactory.getStorage(
				IDENTIFIER_FORMDATA_ERROR_STORAGE, null);
		store.delete();
		requestErrorReload();
	}

	public String getFormDataError(int studyId, FormData form) {
		return getFormDataError(studyId, form.getDefId(), form.getRecordId());
	}

	public String getFormDataError(int studyId, int formId, int recId) {
		FormDataError existingError = getFormDataErrorObj(studyId, formId,
				recId);
		return existingError != null ? existingError.getMessage()
				: "No upload error for this form.";
	}

	public void saveFormDataError(int studyId, int formId, int recId,
			String uploadError) {
		Storage store = StorageFactory.getStorage(
				IDENTIFIER_FORMDATA_ERROR_STORAGE, null);
		FormDataError existingError = getFormDataErrorObj(studyId, formId,
				recId);
		if (existingError == null) {
			// New error, add one
			FormDataError error = new FormDataError(studyId, formId, recId,
					uploadError);
			store.addNew(error);
			requestErrorReload();
		} else {
			existingError.setMessage(uploadError);
			store.save(existingError);
		}
	}

	/**
	 * Gets the name of the storage for form data of a particular study and of a
	 * particular form definition. For performance and common usage scenarios,
	 * form data is stored per study and per form definition. As in each study's
	 * form definition data is stored separately from that of another form
	 * definition in the same study.
	 * 
	 * @param studyId
	 *            - the numeric identifier of the study where this data belongs.
	 * @param formId
	 *            - the numeric identifier of the form definition that this data
	 *            represents.
	 * @return - the storage name
	 */
	private String getFormDataStorageName(int studyId, int formId) {
		String sep = STORAGE_NAME_SEPARATOR;
		return IDENTIFIER_FORM_DATA_STORAGE + sep + studyId + sep + formId;
	}
}
