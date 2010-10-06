package org.fcitmuk.epihandy.midp.db;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.db.util.Record;
import org.fcitmuk.epihandy.FormDef;

/**
 * An abbreviated representation of a form definition. It can be thought of as a
 * light weight index for a real form definition. The class has enough
 * information to populate form lists and can be used with {@link StudyStore} to
 * load the actual form definition.
 * 
 * @author batkinson
 */
public class StoredFormDef implements Persistent, Record {

	int id;
	String name;
	String variableName;
	String descriptionTemplate;
	int recordId = -1;

	public StoredFormDef() {
	}

	public StoredFormDef(int id, String name, String variableName,
			String descriptionTemplate) {
		this.id = id;
		this.name = name;
		this.variableName = variableName;
		this.descriptionTemplate = descriptionTemplate;
	}

	public StoredFormDef(FormDef formDef) {
		this(formDef.getId(), formDef.getName(), formDef.getVariableName(),
				formDef.getDescriptionTemplate());
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

	public String getDescriptionTemplate() {
		return descriptionTemplate;
	}

	public int getRecordId() {
		return recordId;
	}

	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		id = dis.readInt();
		name = dis.readUTF();
		variableName = dis.readUTF();
		descriptionTemplate = dis.readUTF();
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(id);
		dos.writeUTF(name);
		dos.writeUTF(variableName);
		dos.writeUTF(descriptionTemplate);
	}

	public boolean isNew() {
		return recordId == -1;
	}

	public void setRecordId(int id) {
		recordId = id;
	}
}
