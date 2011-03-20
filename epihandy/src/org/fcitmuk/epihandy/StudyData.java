package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import org.fcitmuk.db.util.*;


/**
 * This calls encapsulates data collected in all forms of a particular study.
 * 
 * @author Daniel Kayiwa
 *
 */
public class StudyData implements Persistent{
	private int id = EpihandyConstants.NULL_ID; //this is just for storage;
	private StudyDef def;
	private Vector forms;

	/** Creates a new study data object. */
	public StudyData(){
		super();
	}

	/**
	 * Creates a new study data object form these parameters.
	 * 
	 * @param id - the id of the study definition represented by this data.
	 */
	public StudyData(int id) {
		this();
		setId(id);
	}

	/**
	 * Creates a new study data object form these parameters.
	 * 
	 * @param def - reference to the study definition represented by this data.
	 */
	public StudyData(StudyDef def) {
		this();
		setDef(def);
		setId(def.getId());
	}

	public StudyDef getDef() {
		return def;
	}

	public void setDef(StudyDef def) {
		this.def = def;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Vector getForms() {
		return forms;
	}

	public void setForms(Vector forms) {
		this.forms = forms;
	}

	public void addForm(FormData formData){
		if(forms == null)
			forms = new Vector();
		forms.addElement(formData);
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
	 * Reads the study data object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setId(dis.readInt());
		setForms(PersistentHelper.readMedium(dis,FormData.class));
	}

	/** 
	 * Writes the study data object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getId());
		PersistentHelper.writeMedium(getForms(), dos);
	}
}
