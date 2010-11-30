package org.fcitmuk.epihandy;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.fcitmuk.db.util.Persistent;


/**
 * A user of the system.
 * 
 * @author Daniel
 *
 */
public class User implements Persistent{

	private int userId;

	/** User name is stored in clear text. */
	private String name;

	/** This is a hashed password (no clear text) */
	private String password;

	/** The is used hash the user passed in password (in clear text) and 
	 * then compared with the stored hashed one.
	 */
	private String salt;

	public User(){

	}

	public User(int userId,String name, String password, String salt){
		this.userId = userId;
		this.name = name;
		this.password = password;
		this.salt = salt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	/** 
	 * Reads the User object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setUserId(dis.readInt());
		setName(dis.readUTF().intern());
		setPassword(dis.readUTF().intern());
		setSalt(dis.readUTF().intern());
	}

	/** 
	 * Writes the User object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getUserId());
		dos.writeUTF(getName());
		dos.writeUTF(getPassword());
		dos.writeUTF(getSalt());
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + userId;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}
}
