package org.fcitmuk.db.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Helper class to serialize a few Persistent objects together
 * 
 * @author Dagmar
 */
public class PersistentArray implements Persistent{

	private Persistent[] values;
	
	public PersistentArray() {
	}
	
	public PersistentArray(Persistent[] values) {
		setValues(values);
	}

	public Persistent[] getValues() {
		return values;
	}

	public void setValues(Persistent[] values) {
		this.values = values;
	}
	
	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		for (int i=0, j=values.length; i<j; i++) {
			Persistent persistent = values[i];
			persistent.read(dis);
		}
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		for (int i=0, j=values.length; i<j; i++) {
			Persistent persistent = values[i];
			persistent.write(dos);
		}
	}
}
