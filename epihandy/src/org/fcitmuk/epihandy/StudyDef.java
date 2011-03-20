package org.fcitmuk.epihandy;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.db.util.PersistentHelper;


/**
 * This class encapsulates all form definitions of a particular study.
 * 
 * @author Daniel Kayiwa
 *
 */
public class StudyDef implements Persistent{

	/** The text indentifier of the study. */
	private String variableName = EpihandyConstants.EMPTY_STRING;

	/** The name of the study. */
	private String name = EpihandyConstants.EMPTY_STRING;
	
	/** The numeric identifier of the study. */
	private int id = EpihandyConstants.NULL_ID;

	/** A list of form definitions (FormDef) in the the study. */
	private Vector forms;

	/** Constructs a new study definitions. */
	public StudyDef() {

	}

	/** Copy constructor. */
	public StudyDef(StudyDef studyDef) {
		this(studyDef.getId(),studyDef.getName(),studyDef.getVariableName());
		copyForms(studyDef.getForms());
	}

	/**
	 * 
	 * @param id
	 * @param name
	 * @param variableName
	 */
	public StudyDef(int id, String name, String variableName) {
		setId(id);
		setName(name);
		setVariableName(variableName);;
	}

	/** 
	 * Constructs a new study definition from the following parameters.
	 * 
	 * @param id - the numeric unique identifier of the study.
	 * @param name - the display name of the study.
	 * @param variableName - the text unique identifier of the study.
	 * @param forms - the collection of form definitions in the study.
	 */
	public StudyDef(int id, String name, String variableName,Vector forms) {
		this(id,name,variableName);
		setForms(forms);
	}

	public Vector getForms() {
		return forms;
	}

	public void setForms(Vector forms) {
		this.forms = forms;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public FormDef getFormAt(short index){
		return (FormDef)forms.elementAt(index);
	}

	public void addForm(FormDef formDef){
		if(forms == null)
			forms = new Vector();
		forms.addElement(formDef);
	}

	public void addForms(Vector formList){
		if(formList != null){
			if(forms == null)
				forms = formList;
			else{
				for(int i=0; i<formList.size(); i++ )
					forms.addElement(formList.elementAt(i));
			}
		}
	}

	/** 
	 * Reads the study definition object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		dis.readInt(); // Skip over content length
		setId(dis.readInt());
		setName(dis.readUTF().intern());
		setVariableName(dis.readUTF().intern());
		setForms(PersistentHelper.readMedium(dis,FormDef.class));
	}

	/** 
	 * Writes the study definition object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		
		// Write contents to memory so we can count content length
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream mdos = new DataOutputStream(baos);
		mdos.writeInt(getId());
		mdos.writeUTF(getName());
		mdos.writeUTF(getVariableName());
		PersistentHelper.writeMedium(getForms(), mdos);
		
		// Write length, then output contents
		dos.writeInt(baos.size());
		dos.write(baos.toByteArray());
	}

	/**
	 * Gets a form definition with a given string identifier.
	 * 
	 * @param varName - the string identifier.
	 * @return - the form definition.
	 */
	public FormDef getForm(String varName){
		for(int i=0; i<forms.size(); i++){
			FormDef def = (FormDef)forms.elementAt(i);
			if(def.getVariableName().equals(varName))
				return def;
		}

		return null;
	}

	/**
	 * Gets a form definition with a given numeric identifier.
	 * 
	 * @param formId - the numeric identifier.
	 * @return - the form definition.
	 */
	public FormDef getForm(int formId){
		for(int i=0; i<forms.size(); i++){
			FormDef def = (FormDef)forms.elementAt(i);
			if(def.getId() == formId)
				return def;
		}

		return null;
	}

	public String toString() {
		return getName();
	}

	private void copyForms(Vector forms){
		this.forms = new Vector();
		for(int i=0; i<forms.size(); i++)
			this.forms.addElement(new FormDef((FormDef)forms.elementAt(i)));
	}
}
