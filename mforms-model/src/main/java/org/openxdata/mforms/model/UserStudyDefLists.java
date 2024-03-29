package org.openxdata.mforms.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.mforms.persistent.Persistent;

/**
 * Contains a list of users and form definitions in a study, combined together.
 * This is to allow a forms download connection to get all the data in just one
 * connection request instead of making separate requests for each. This can be
 * useful for bluetooth connections where the first succeeds and the following
 * ones fail randomly.
 * 
 * @author Daniel Kayiwa
 * 
 */
public class UserStudyDefLists implements Persistent{

	/** A list of users. */
	private UserList users;

	/** A list of form definitions in a study. */
	private StudyDef studyDef;

	/** Default constructor. */
	public UserStudyDefLists(){

	}

	/**
	 * Constructs a new users and study form definitions list.
	 * 
	 * @param users the users list.
	 * @param studyDef the list of form difinitions in a study.
	 */
	public UserStudyDefLists(UserList users,StudyDef studyDef){
		setUsers(users);
		setStudyDef(studyDef);
	}

	public StudyDef getStudyDef() {
		return studyDef;
	}

	public void setStudyDef(StudyDef studyDef) {
		this.studyDef = studyDef;
	}

	public UserList getUsers() {
		return users;
	}

	public void setUsers(UserList users) {
		this.users = users;
	}

	/**
	 * @see org.openxdata.mforms.persistent.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		UserList users = new UserList();
		users.read(dis);
		setUsers(users);

		StudyDef studyDef = new StudyDef();
		studyDef.read(dis);
		setStudyDef(studyDef);
	}

	/**
	 * @see org.openxdata.mforms.persistent.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		getUsers().write(dos);
		getStudyDef().write(dos);
	}
}

