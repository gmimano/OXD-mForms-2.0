package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.fcitmuk.db.util.Persistent;

/**
 * Represents a response from a form data upload. This is used for constructing
 * and storing the response for users to use in correcting upload errors caused
 * by bad forms.
 * 
 * @author batkinson
 * 
 */
public class UploadResponse implements Persistent {

	int totalForms;
	int errorCount;
	final Vector uploadErrors;

	public UploadResponse() {
		totalForms = 0;
		errorCount = 0;
		uploadErrors = new Vector();
	}

	public UploadResponse(byte totalForms, Vector uploadErrors) {
		this.totalForms = totalForms;
		this.errorCount = (byte) uploadErrors.size();
		this.uploadErrors = new Vector(this.errorCount);
		for (int formidx = 0; formidx < errorCount; formidx++)
			this.uploadErrors.addElement(uploadErrors.elementAt(formidx));
	}

	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		totalForms = dis.readInt();
		errorCount = dis.readInt();
		for (int formidx = 0; formidx < errorCount; formidx++) {
			UploadError error = new UploadError();
			((Persistent) error).read(dis);
			uploadErrors.addElement(error);
		}
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(totalForms);
		dos.writeInt(errorCount);
		for (int formidx = 0; formidx < uploadErrors.size(); formidx++)
			((Persistent) uploadErrors.elementAt(formidx)).write(dos);
	}

	public int getTotalForms() {
		return totalForms;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void clear() {
		totalForms = 0;
		errorCount = 0;
		if (uploadErrors.size() > 0)
			uploadErrors.removeAllElements();
	}

	public UploadError[] getUploadErrors() {
		UploadError[] errors = new UploadError[errorCount];
		uploadErrors.copyInto(errors);
		return errors;
	}

	public boolean isFailedForm(byte studyIndex, short formIndex) {
		for (int i = 0; i < uploadErrors.size(); i++) {
			UploadError uploadError = (UploadError) uploadErrors.elementAt(i);
			if (uploadError.getStudyIndex() == studyIndex
					&& uploadError.getFormIndex() == formIndex)
				return true;
		}
		return false;
	}
}
