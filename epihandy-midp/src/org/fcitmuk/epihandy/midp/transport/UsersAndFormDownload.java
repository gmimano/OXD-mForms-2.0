package org.fcitmuk.epihandy.midp.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.epihandy.UserList;
import org.fcitmuk.epihandy.midp.db.EpihandyDataStorage;
import org.fcitmuk.epihandy.midp.db.MalformedStreamException;
import org.fcitmuk.epihandy.midp.db.StoredStudyDef;
import org.fcitmuk.epihandy.midp.db.StudyStore;
import org.fcitmuk.epihandy.midp.model.Model;

/**
 * A class that enables form download with the existing transport layer.
 * 
 * @author Brent
 * 
 */
public class UsersAndFormDownload implements Persistent {

	UserList userList;
	StudyStore store;
	Model model;
	Exception e;

	public UsersAndFormDownload(StudyStore store, Model model) {
		userList = new UserList();
		this.store = store;
		this.model = model;
	}

	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		try {
			userList.read(dis);
			EpihandyDataStorage.saveUsers(userList);
			store.storeStudyFromStream(dis, true);
			// FIXME: Move to controller, reuse on study selection
			StoredStudyDef study = model.getSelectedStudyDef();
			model.setStudyForms(store.getFormDefList(study));
		} catch (MalformedStreamException e) {
			this.e = e;
		}
	}

	public UserList getUserList() {
		return userList;
	}

	public StudyStore getStore() {
		return store;
	}

	public Model getModel() {
		return model;
	}

	public Exception getException() {
		return e;
	}

	public void write(DataOutputStream dos) throws IOException {

	}

}
