package org.fcitmuk.epihandy.midp.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.epihandy.midp.model.Model;

/**
 * This class is meant to implement an extremely memory efficient implementation
 * of form upload. It does this by uploading studies directly from storage and
 * without reading them into memory first.
 * 
 * @author batkinson
 * 
 */
public class FormUpload implements Persistent {

	public static final int DEFAULT_BUFFER_SIZE = 512;

	private Model model;
	private Vector uploadedFormsData = new Vector(); // by id: study, form, rec

	public FormUpload(Model model) {
		this.model = model;
	}

	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		// This is only for upload, not implemented
	}

	public void write(DataOutputStream dos) throws IOException {
		uploadedFormsData = model.streamFormData(dos);
	}

	/**
	 * Returns an array containing pertinent identifiers for the form content
	 * that was uploaded in a specified position in the upload batch.
	 * 
	 * @param studyPos
	 *            0-based index of study in upload
	 * @param formPos
	 *            0-based index of form data in uploaded study
	 * @return an int[3] containing: studyId, formId, recordId
	 */
	public int[] getFormAtPos(int studyPos, int formPos) {
		Vector study = (Vector) uploadedFormsData.elementAt(studyPos);
		int[] form = (int[]) study.elementAt(formPos);
		return form;
	}

	/**
	 * Gets the number of studies in upload.
	 * 
	 * @return
	 */
	public int getStudyCount() {
		return uploadedFormsData.size();
	}

	/**
	 * Returns the number of uploaded form data items in study.
	 * 
	 * @param studyIdx
	 * @return
	 */
	public int getFormCount(int studyIdx) {
		return ((Vector) uploadedFormsData.elementAt(studyIdx)).size();
	}
}
