package org.fcitmuk.epihandy.midp.db;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import org.fcitmuk.epihandy.FormDef;
import org.fcitmuk.midp.db.util.Storage;
import org.fcitmuk.midp.db.util.StorageFactory;

/**
 * A storage-based directory for study and form definitions. This is the
 * foundation for a scalable form browser. Instead of loading all form
 * definitions in a study, only a single form definition is required to be in
 * memory at a time. This allows for a much smaller memory footprint because it
 * only requires enough memory to load the largest form, rather than the entire
 * study. This is appropriate because the user is typically only interacting
 * with a single form at a time.
 * 
 * @author batkinson
 * 
 */
public class StudyStore {

	/**
	 * Returns the list of stored study definitions.
	 * 
	 * @return
	 */
	public StoredStudyDef[] getStudyDefList() {
		Storage studyListStore = getStudyListStore();
		StoredStudyDef[] studyDefs = new StoredStudyDef[studyListStore
				.getNumRecords()];
		for (int i = 0; i < studyDefs.length; i++)
			studyDefs[i] = (StoredStudyDef) studyListStore.read(i + 1,
					StoredStudyDef.class);
		return studyDefs;
	}

	/**
	 * Returns the list of stored form definitions for the specified study.
	 * 
	 * @param study
	 * @return
	 */
	public StoredFormDef[] getFormDefList(StoredStudyDef study) {
		Storage formListStore = getFormListStore(study);
		StoredFormDef[] formDefs = new StoredFormDef[formListStore
				.getNumRecords()];
		for (int i = 0; i < formDefs.length; i++)
			formDefs[i] = (StoredFormDef) formListStore.read(i + 1,
					StoredFormDef.class);
		return formDefs;
	}

	/**
	 * Loads the stored form definition into memory.
	 * 
	 * @param study
	 * @param form
	 * @return
	 */
	public FormDef getFormDef(StoredStudyDef study, StoredFormDef form) {
		Storage formDefStore = getFormDefStore(study);
		return (FormDef) formDefStore.read(form.getRecordId(), FormDef.class);
	}
	
	public FormDef getFormDef(int studyDefId, int formDefId) {
		Storage formDefStore = getFormDefStore(studyDefId);
		Vector forms = formDefStore.read(FormDef.class);
		for (int i=0; i<forms.size(); i++) {
			FormDef form = (FormDef) forms.elementAt(i);
			if (form.getId() == formDefId) {
				return form;
			}
		}
		return null;
	}

	public void storeStudiesFromStream(DataInputStream dis)
			throws MalformedStreamException {
		storeStudyListFromStream(dis, true);
	}

	public void storeStudyListFromStream(DataInputStream dis)
			throws MalformedStreamException {
		storeStudyListFromStream(dis, true);
	}

	/**
	 * Stores the list of study definitions using the given input stream.
	 * 
	 * @param dis
	 * @param storeForms
	 * @throws MalformedStreamException
	 */
	public void storeStudyListFromStream(DataInputStream dis, boolean storeForms)
			throws MalformedStreamException {

		short studyCount;
		try {
			studyCount = dis.readShort();
		} catch (IOException e) {
			throw new MalformedStreamException("Failed to read study count: "
					+ e.getMessage());
		}
		// Wipe out existing studies and their forms if they exist
		if (storeForms) {
			getStudyListStore().delete();
		}
		// Store studies one a time
		for (int i = 0; i < studyCount; i++) {
			storeStudyFromStream(dis, storeForms);
		}
	}

	/**
	 * Stores a study definition directly using the given input stream.
	 * 
	 * @param dis
	 * @param storeForms
	 * @throws MalformedStreamException
	 */
	public void storeStudyFromStream(DataInputStream dis, boolean storeForms)
			throws MalformedStreamException {

		int size, id, numForms;
		String name, varName;
		try {
			size = dis.readInt();
			id = dis.readInt();
			name = dis.readUTF();
			varName = dis.readUTF();
			numForms = dis.readShort();
		} catch (IOException e) {
			throw new MalformedStreamException("Failed to read study header: "
					+ e.getMessage());
		}

		StoredStudyDef studyDef = new StoredStudyDef(id, name, varName,
				numForms, size);

		Storage studyListStore = getStudyListStore();
		Storage formDefListStore = getFormListStore(studyDef);
		Storage formDefStore = getFormDefStore(studyDef);

		// Find record in study list, if possible
		StoredStudyDef[] storedStudyDefs = getStudyDefList();
		for (int i = 0; i < storedStudyDefs.length; i++) {
			StoredStudyDef storedStudyDef = storedStudyDefs[i];
			if (studyDef.getId() == storedStudyDef.getId()) {
				studyDef.setRecordId(storedStudyDef.getRecordId());
				break;
			}
		}

		// Adds or updates study
		studyListStore.save(studyDef);

		// Wipe out existing form stores if they exist
		if (storeForms) {
			formDefStore.delete();
			formDefListStore.delete();
		}

		// Store the forms, one in memory at a time
		for (int i = 0; i < numForms; i++) {
			if (storeForms) {
				// This can be optimized 
				FormDef formDef = new FormDef();
				try {
					formDef.read(dis);
				} catch (Exception e) {
					throw new MalformedStreamException("Failed to read form: "
							+ e.getMessage());
				}
				StoredFormDef storedDef = new StoredFormDef(formDef);
				formDefListStore.save(storedDef);
				formDefStore.addNew(formDef);
			} else {
				int formSize;
				try {
					formSize = dis.readInt();
					dis.skipBytes(formSize);
				} catch (IOException e) {
					throw new MalformedStreamException("Failed to skip form: "
							+ e.getMessage());
				}
			}
		}
	}

	private Storage getStudyListStore() {
		Storage studyListStore = StorageFactory
				.getStorage("StudyDefList", null);
		return studyListStore;
	}

	private Storage getFormListStore(StoredStudyDef studyDef) {
		Storage formDefListStore = StorageFactory.getStorage("FormDefList."
				+ studyDef.getId(), null);
		return formDefListStore;
	}

	private Storage getFormDefStore(StoredStudyDef studyDef) {
		Storage formDefStore = StorageFactory.getStorage("FormDefs."
				+ studyDef.getId(), null);
		return formDefStore;
	}
	
	private Storage getFormDefStore(int studyDefId) {
		Storage formDefStore = StorageFactory.getStorage("FormDefs."
				+ studyDefId, null);
		return formDefStore;
	}

}
