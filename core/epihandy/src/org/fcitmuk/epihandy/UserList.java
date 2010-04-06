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

	private Vector users = new Vector();

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
		users.addElement(user);
	}

	public void addUsers(Vector userList){
		if(userList != null){
			for(int i=0; i<userList.size(); i++ )
				this.users.addElement(userList.elementAt(i));
		}
	}

	public int size(){
		return users.size();
	}

	public User getUser(int index){
		return (User)users.elementAt(index);
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setUsers(PersistentHelper.read(dis,new User().getClass()));
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getUsers(), dos);
	}
}
