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
	final Vector sessionReferences;

	public UploadResponse() {
		totalForms = 0;
		errorCount = 0;
		uploadErrors = new Vector();
		sessionReferences = new Vector();
	}

	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		totalForms = dis.readInt();
		errorCount = dis.readInt();
		for (int formidx = 0; formidx < errorCount; formidx++) {
			UploadError error = new UploadError();
			error.read(dis);
			uploadErrors.addElement(error);
		}
		for (int formidx = 0; formidx < getSuccessCount(); formidx++) {
			FormDataSummary summary = new FormDataSummary();
			summary.read(dis);
			sessionReferences.addElement(summary);
		}
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(totalForms);
		dos.writeInt(errorCount);
		for (int formidx = 0; formidx < uploadErrors.size(); formidx++)
			((Persistent) uploadErrors.elementAt(formidx)).write(dos);
		for (int formidx = 0; formidx < sessionReferences.size(); formidx++)
			((Persistent) sessionReferences.elementAt(formidx)).write(dos);
	}

	public int getTotalForms() {
		return totalForms;
	}

	public int getErrorCount() {
		return errorCount;
	}
	
	public int getSuccessCount() {
		return totalForms - errorCount;
	}

	public void clear() {
		totalForms = 0;
		errorCount = 0;
		if (uploadErrors.size() > 0)
			uploadErrors.removeAllElements();
		if (sessionReferences.size() > 0)
			sessionReferences.removeAllElements();
	}

	public UploadError[] getUploadErrors() {
		UploadError[] errors = new UploadError[errorCount];
		uploadErrors.copyInto(errors);
		return errors;
	}
	
	public FormDataSummary[] getUploadFormDataSummary() {
		FormDataSummary[] summary = new FormDataSummary[(totalForms-errorCount)];
		sessionReferences.copyInto(summary);
		return summary;
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
