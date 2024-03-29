package org.openxdata.mforms.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.openxdata.mforms.persistent.Persistent;
import org.openxdata.mforms.persistent.PersistentHelper;


/**
 * 
 * @author daniel
 *
 */
public class LanguageList  implements Persistent{

	private Hashtable hashtable = new Hashtable();
	
	public int size(){
		return hashtable.size();
	}
	
	public Hashtable getLanguages(){
		return hashtable;
	}
	
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		hashtable = PersistentHelper.read(dis);
	}

	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(hashtable, dos);
	}
}
