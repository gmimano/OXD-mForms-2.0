package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.fcitmuk.db.util.Persistent;

/**
 * Represents a failed upload for a form. Essentially, this is just enough
 * information to identify the forms in an upload that are in error and why they
 * failed.
 * 
 * @author batkinson
 * 
 */
public class UploadError implements Persistent {

	short studyIndex;
	short formIndex;
	String description;

	public UploadError() {
		studyIndex = -1;
		formIndex = -1;
		description = "Uninitialized Error";
	}

	public UploadError(short study, short form, String description) {
		studyIndex = study;
		formIndex = form;
		this.description = description;
	}

	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		studyIndex = dis.readShort();
		formIndex = dis.readShort();
		description = dis.readUTF().intern();
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeShort(studyIndex);
		dos.writeShort(formIndex);
		dos.writeUTF(description);
	}

	public short getStudyIndex() {
		return studyIndex;
	}

	public short getFormIndex() {
		return formIndex;
	}

	public String getDescription() {
		return description;
	}
}
