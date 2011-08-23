package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.fcitmuk.db.util.AbstractRecord;

/**
 * A persistent object used to store error messages for forms. The study and
 * form id are intended to identify the study and form definitions. The formData
 * is meant to identify the precise record that the error pertains to. It is
 * worth noting that usage of this class suggests maintenance of these records
 * in tandem with movement or deletion of the form data records they pertain to.
 * 
 * @author Brent
 * 
 */
public class FormDataError extends AbstractRecord {

	int studyId = -1;
	int formId = -1;
	int formDataId = -1;
	String message = "Unitialized error";

	public FormDataError() {
	}

	public FormDataError(int studyId, int formId, int formRecordId,
			String message) {
		this.studyId = studyId;
		this.formId = formId;
		this.formDataId = formRecordId;
		this.message = message;
	}

	public int getStudyId() {
		return studyId;
	}

	public void setStudyId(int studyId) {
		this.studyId = studyId;
	}

	public int getFormId() {
		return formId;
	}

	public void setFormId(int formId) {
		this.formId = formId;
	}

	public int getFormDataId() {
		return formDataId;
	}

	public void setFormDataId(int formDataId) {
		this.formDataId = formDataId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		studyId = dis.readInt();
		formId = dis.readInt();
		formDataId = dis.readInt();
		message = dis.readUTF().intern();
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(studyId);
		dos.writeInt(formId);
		dos.writeInt(formDataId);
		dos.writeUTF(message);
	}

}
