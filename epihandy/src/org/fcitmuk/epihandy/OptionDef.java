package org.fcitmuk.epihandy;

import java.io.*;

import org.fcitmuk.db.util.Persistent;


/** 
 * Definition of an answer option or one of the possible answers of a question
 * with a given set of allowed answers..
 * 
 * @author Daniel Kayiwa
 *
 */
public class OptionDef implements Persistent {
	/** The numeric unique identifier of an answer option. */
	private short id = EpihandyConstants.NULL_ID;

	/** The display text of the answer option. */
	private String text = EpihandyConstants.EMPTY_STRING;

	//TODO May not need to serialize this property for smaller pay load. Then we would just rely on the id.
	/** The unique text ientifier of an answer option. */
	private String variableName = EpihandyConstants.EMPTY_STRING;

	public static final char SEPARATOR_CHAR = ',';

	/** Constructs the answer option definition object where
	 * initialization parameters are not supplied. */
	public OptionDef() {  

	}

	/** The copy constructor  */
	public OptionDef(OptionDef optionDef) {  
		setId(optionDef.getId());
		setText(optionDef.getText());
		setVariableName(optionDef.getVariableName());
	}

	/** Constructs a new option answer definition object from the following parameters.
	 * 
	 * @param id
	 * @param text
	 * @param variableName
	 */
	public OptionDef(short id, String text, String variableName) {
		this();
		setId(id);
		setText(text);
		setVariableName(variableName);
	}

	public short getId() {
		return id;
	}

	public void setId(short id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public String toString() {
		return getText();
	}

	/** Reads the answer option definition from the stream. 
	 * 
	 */
	public void read(DataInputStream dis) throws IOException {
		setId(dis.readShort());
		setText(dis.readUTF().intern());
		setVariableName(dis.readUTF().intern());
	}

	/** Writes the answer option definition to the stream. 
	 * 
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeShort(getId());
		dos.writeUTF(getText());
		dos.writeUTF(getVariableName());
	}
}
