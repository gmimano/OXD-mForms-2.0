package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import org.fcitmuk.db.util.PersistentHelper;
import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.epihandy.User;

/**
 * Contains a list of users.
 * 
 * @author Daniel
 *
 */
public class UserList implements Persistent{

	private Vector users;
	
	public UserList(){
		
	}
	
	public UserList(Vector users){
		this.users = users;
	}
	
	public Vector getUsers() {
		return users;
	}

	public void setUsers(Vector users) {
		this.users = users;
	}

	public void addUser(User user){
		if(users == null)
			users = new Vector();
		users.addElement(user);
	}
	
	public void addUsers(Vector userList){
		if(userList != null){
			if(users == null)
				users = userList;
			else{
				for(int i=0; i<userList.size(); i++ )
					this.users.addElement(userList.elementAt(i));
			}
		}
	}
	
	public int size(){
		if(getUsers() == null)
			return 0;
		return getUsers().size();
	}
	
	public User getUser(int index){
		return (User)getUsers().elementAt(index);
	}
	
	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setUsers(PersistentHelper.readMedium(dis,User.class));
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.writeMedium(getUsers(), dos);
	}
}
