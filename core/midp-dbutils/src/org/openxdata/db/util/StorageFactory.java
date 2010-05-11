package org.openxdata.db.util;


/**
 * Factory for creating data storage instance.
 * 
 * @author Daniel Kayiwa
 *
 */
public class StorageFactory {
	
	/** No external creation allowed. */
	private StorageFactory(){
		
	}
	
	/**
	 * Gets an instance of the storage to use. This could be RMS, FileStorage, 
	 * Local Database, or even a live connection to the server. But the caller
	 * is shielded from the implementation details.
	 * 
	 * @param name - the name of storage.
	 * @param eventListener - listener to storage events.
	 * @return - the storage instance.
	 */
	public static Storage getStorage(String name,StorageListener eventListener){
		/*if(storage == null)
			storage = new RMSStorage(name,eventListener);
		return storage;*/
		
		return new RMSStorage(name,eventListener);
	}
}
