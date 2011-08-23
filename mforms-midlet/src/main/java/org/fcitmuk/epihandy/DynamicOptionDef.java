package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.db.util.PersistentHelper;


/**
 * 
 * @author daniel
 *
 */
public class DynamicOptionDef  implements Persistent {

	/** The question whose values are determined by or dependent on the answer of another question. **/
	private short questionId;

	/** A map between each parent option and a list of possible options for the dependant question. */
	private Hashtable parentToChildOptions;


	public DynamicOptionDef(){

	}
	
	/** The copy constructor  */
	public DynamicOptionDef(DynamicOptionDef dynamicOptionDef) {  
		setQuestionId(dynamicOptionDef.getQuestionId());
		copyQuestionOptions(dynamicOptionDef.getParentToChildOptions());
	}

	public Hashtable getParentToChildOptions() {
		return parentToChildOptions;
	}


	public void setParentToChildOptions(Hashtable parentToChildOptions) {
		this.parentToChildOptions = parentToChildOptions;
	}


	public short getQuestionId() {
		return questionId;
	}


	public void setQuestionId(short questionId) {
		this.questionId = questionId;
	}
	
	private void copyQuestionOptions(Hashtable parentToChildOptions){
		if(parentToChildOptions == null)
			return;

		this.parentToChildOptions = new Hashtable();
		
		Enumeration keys = parentToChildOptions.keys();
		Short key;
		while(keys.hasMoreElements()){
			key = (Short)keys.nextElement();
			this.parentToChildOptions.put(key, QuestionDef.copyQuestionOptions((Vector)parentToChildOptions.get(key)));
		}
	}
	
	public Vector getOptionList(short optionId){
		if(parentToChildOptions == null)
			return null;
		return (Vector)parentToChildOptions.get(new Short(optionId));
	}
	
	public void read(DataInputStream dis) throws IOException , InstantiationException, IllegalAccessException {
		setQuestionId(dis.readShort());
		
		short len = dis.readShort();
		if(len == 0)
			return;
		
		parentToChildOptions = new Hashtable();
		for (int i = 0; i < len; i++) {
			short parentOptId = dis.readShort();
			Vector childOpts = PersistentHelper.readMedium(dis, OptionDef.class);
			if (childOpts != null)
				parentToChildOptions.put(new Short(parentOptId), childOpts);
		}
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeShort(getQuestionId());

		if(parentToChildOptions != null){
			dos.writeShort(parentToChildOptions.size());
			Enumeration keys = parentToChildOptions.keys();
			while (keys.hasMoreElements()) {
				Short parentOptId = (Short) keys.nextElement();
				Vector childOpts = (Vector) parentToChildOptions
						.get(parentOptId);
				dos.writeShort(parentOptId.shortValue());
				PersistentHelper.writeMedium(childOpts, dos);
			}
		}
		else
			dos.writeShort(0);
	}
}
