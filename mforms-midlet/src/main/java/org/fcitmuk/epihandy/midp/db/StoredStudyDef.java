package org.fcitmuk.epihandy.midp.db;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.db.util.Record;

/**
 * A light weight representation for a {@studyDef}. It is intended to be used as
 * a place holder or index, to avoid having to load an entire study.
 * 
 * @author batkinson
 * 
 */
public class StoredStudyDef implements Persistent, Record {

	int id;
	String name;
	String variableName;
	int formCount;
	int size;
	int recordId = -1;

	public StoredStudyDef() {
	}

	public StoredStudyDef(int id, String name, String variableName,
			int formCount, int size) {
		this.id = id;
		this.name = name;
		this.variableName = variableName;
		this.formCount = formCount;
		this.size = size;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getVariableName() {
		return variableName;
	}

	public int getFormCount() {
		return formCount;
	}

	public int getSize() {
		return size;
	}

	public int getRecordId() {
		return recordId;
	}

	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		id = dis.readInt();
		name = dis.readUTF();
		variableName = dis.readUTF();
		formCount = dis.readInt();
		size = dis.readInt();
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(id);
		dos.writeUTF(name);
		dos.writeUTF(variableName);
		dos.writeInt(formCount);
		dos.writeInt(size);
	}

	public boolean isNew() {
		return recordId == -1;
	}

	public void setRecordId(int id) {
		recordId = id;
	}
}
