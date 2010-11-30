package org.fcitmuk.epihandy.midp.db;

import java.util.Vector;

import org.fcitmuk.epihandy.LanguageList;
import org.fcitmuk.epihandy.MenuTextList;
import org.fcitmuk.epihandy.User;
import org.fcitmuk.epihandy.UserList;
import org.fcitmuk.midp.db.util.Storage;
import org.fcitmuk.midp.db.util.StorageFactory;
import org.fcitmuk.midp.db.util.StorageListener;

/**
 * Handles data storage operations for epihandy.
 * 
 * @author Daniel Kayiwa
 *
 */
public class EpihandyDataStorage {
	
	/** The unique identifier for storage of form data of a particular study. */
	
	private static final String IDENTIFIER_USER_STORAGE = "User";
	
	private static final String IDENTIFIER_LANGUAGES_STORAGE = "Languages";
	
	private static final String IDENTIFIER_MENU_TEXT_STORAGE = "MenuText";
	
	public static StorageListener storageListener;
	
	public static void saveUsers(UserList users){
		Storage store = StorageFactory.getStorage(IDENTIFIER_USER_STORAGE,storageListener);

		UserList currentUserList = getUsers();
		if (currentUserList != null) {
			// merge new list with old list
			Vector newUsers = users.getUsers();
			if (newUsers == null) {
				newUsers = new Vector();
				users.setUsers(newUsers);
			}
			Vector currentUsers = currentUserList.getUsers();
			if (currentUsers != null) {
				for (int i=0, j=currentUsers.size(); i<j; i++) {
					User user1 = (User)currentUsers.elementAt(i);
					if (!newUsers.contains(user1)) {
						newUsers.addElement(user1);
					}
				}
			}
		}
		
		store.delete(); //only one list is allowed.
		store.addNew(users);
	}
	
	public static void saveMenuText(MenuTextList MenuTextList){
		Storage store = StorageFactory.getStorage(IDENTIFIER_MENU_TEXT_STORAGE,storageListener);
		store.delete(); //only one list is allowed.
		store.addNew(MenuTextList);
	}
	
	public static void saveLanguages(LanguageList languages){
		Storage store = StorageFactory.getStorage(IDENTIFIER_LANGUAGES_STORAGE,storageListener);
		store.delete(); //only one list is allowed.
		store.addNew(languages);
	}
	
	/**
	 * Gets a list of users.
	 * 
	 * @return - the UserList object.
	 */
	public static UserList getUsers(){
		Storage store = StorageFactory.getStorage(IDENTIFIER_USER_STORAGE,storageListener);
		Vector vect = store.read(UserList.class);
		if(vect != null && vect.size() > 0)
			return (UserList)vect.elementAt(0); //There can only be one record for the users list object.
		return null;
	}
	
	/**
	 * Gets a list of languages.
	 * 
	 * @return - the LanguageList object.
	 */
	public static LanguageList getLanguages(){
		Storage store = StorageFactory.getStorage(IDENTIFIER_LANGUAGES_STORAGE,storageListener);
		Vector vect = store.read(LanguageList.class);
		if(vect != null && vect.size() > 0)
			return (LanguageList)vect.elementAt(0); //There can only be one record for the users list object.
		return null;
	}
	
	/**
	 * Gets a list of menu text.
	 * 
	 * @return - the LanguageList object.
	 */
	public static MenuTextList getMenuText(){
		Storage store = StorageFactory.getStorage(IDENTIFIER_MENU_TEXT_STORAGE,storageListener);
		Vector vect = store.read(MenuTextList.class);
		if(vect != null && vect.size() > 0)
			return (MenuTextList)vect.elementAt(0); //There can only be one record for the users list object.
		return new MenuTextList();
	}
}
