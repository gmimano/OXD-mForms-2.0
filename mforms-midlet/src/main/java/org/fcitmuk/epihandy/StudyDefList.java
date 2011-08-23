package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import org.fcitmuk.db.util.PersistentHelper;
import org.fcitmuk.db.util.Persistent;


/**
 * This class holds a collection of study definitions.
 * NOTE CAREFULLY: None of the studies contains any forms because this is to just provide
 * info on what studes are available.
 * 
 * @author Daniel Kayiwa
 *
 */
public class StudyDefList implements Persistent{
	
	/** Collection of study definitions (StudyDef objects). */
	private Vector studies;
	
	/** Constructs a new study collection. */
	public StudyDefList(){
		
	}
	
	/** Copy Constructor. */
	public StudyDefList(StudyDefList studyDefList){
		studies = new Vector();
		for(byte i=0; i<studyDefList.size(); i++)
			studies.addElement(new StudyDef(studyDefList.getStudy(i)));
	}
	
	public StudyDefList(Vector studies){
		setStudies(studies);
	}
	
	public Vector getStudies() {
		return studies;
	}
	
	public int size(){
		if(studies == null)
			return 0;
		return studies.size();
	}

	public void setStudies(Vector studies) {
		this.studies = studies;
	}
	
	public StudyDef getStudy(byte index){
		return (StudyDef)studies.elementAt(index);
	}
	
	public void addStudy(StudyDef studyDef){
		if(studies == null)
			studies = new Vector();
		studies.addElement(studyDef);
	}
	
	public void addStudies(Vector studyList){
		if(studyList != null){
			if(studies == null)
				studies = studyList;
			else{
				for(byte i=0; i<studyList.size(); i++ )
					studies.addElement(studyList.elementAt(i));
			}
		}
	}
	
	/** 
	 * Reads the study collection object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setStudies(PersistentHelper.readMedium(dis,StudyDef.class));
	}

	/** 
	 * Writes the study collection object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.writeMedium(getStudies(), dos);
	}
}
