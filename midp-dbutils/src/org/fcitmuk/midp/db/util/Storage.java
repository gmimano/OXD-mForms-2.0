package org.fcitmuk.midp.db.util;

import java.io.OutputStream;
import java.util.Vector;

import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.db.util.Record;

/**
 * An interface that all concrete storages should implement. This interface shields 
 * the user from the underlying storage type.
 * 
 * @author Daniel Kayiwa
 *
 */
public interface Storage {
	
	/**
	 * Inspects the number of logical records in the store.
	 * 
	 * @return an integer specifying the number of logical records.
	 */
	public int getNumRecords();
	

	/**
	 * Writes the contents of the storage unit to the stream.
	 * 
	 * @param os
	 * @return a list of record ids written.
	 */
	public Vector writeBytes(OutputStream os);
	
	/** Deletes all records from the storage. */
	public boolean delete();
	
	/**
	 * Deletes a record from the data store.
	 * 
	 * @param recId - the numeric identifier of the record to be deleted.
	 */
	public boolean delete(int recId);
	
	/** 
	 * Reads a list of objects of a given class from persistent storage.
	 * 
	 * @param cls - the class of the object to be retrieved.
	 * @return - the list of objects retrieved.
	 */
	public Vector read(Class cls);
	
	/**
	 * Reads the list of record ids for the given class from storage.
	 * @return - vector of ints, record ids
	 */
	public Vector readIds();
	
	/** 
	 * Reads an object from persistent store 
	 * using its numeric unique identifier and class.
	 * 
	 * @param id - the unique identifier of the object.
	 * @param cls - the class of the object.
	 * @return the object
	 */
	public Object read(int id,Class cls);
	
	/** 
	 * Reads the first object from persistent store
	 * 
	 * @param cls - the class of the object.
	 * @return the object
	 */
	public Persistent readFirst(Class cls);
	
	/** 
	 * Saves a persistent object to storage. 
	 * A peristent object is one which implements the Persistent interface.
	 * 
	 * @param obj - the object to save.
	 * @return - the unique identifier of the saved object. 
	 * This identifier can be used to later on retrieve this particular object form persistent storage.
	 */
	public int addNew(Persistent obj);
	
	/** 
	 * updates a persistent object in storage. 
	 * A peristent object is one which implements the Persistent interface.
	 * 
	 * @param id - the recordid of the object to save.
	 * @param obj - the object to save.
	 */
	public boolean update(int id,Persistent obj);
	
	/** 
	 * Saves a list of persistent objects to storage. 
	 * A peristent object is one which implements the Persistent interface.
	 * 
	 * @param persistentObjects - the list of objects to save.
	 * @return - the unique identifiers of the saved object. 
	 * These identifiers can be used to later on retrieve these particular objects form persistent storage.
	 */
	public Vector addNew(Vector persistentObjects);
	
	/**
	 * Saves a record to the data store.
	 * 
	 * @param rec
	 */
	public boolean save(Record rec);
	
	/**
	 * Deletes a record from the data store.
	 * 
	 * @param rec
	 */
	public boolean delete(Record rec);
}
