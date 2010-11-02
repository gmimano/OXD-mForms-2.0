package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.fcitmuk.db.util.AbstractRecord;

/**
 * Contains a summary of submitted form data
 * (form data description + identifier) 
 * 
 * @author dagmar@cell-life.org
 */
public class FormDataSummary  extends AbstractRecord {
	
	byte studyIndex;
	short formIndex;
	private String reference;

	/** Constructs a form data summary object. */
	public FormDataSummary(){
		super();
	}

	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		studyIndex = dis.readByte();
		formIndex = dis.readShort();
		reference = dis.readUTF().intern();
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(studyIndex);
		dos.writeShort(formIndex);
		dos.writeUTF(reference);
	}

	public byte getStudyIndex() {
		return studyIndex;
	}

	public short getFormIndex() {
		return formIndex;
	}
	
	public String getReference() {
		return reference;
	}
}
