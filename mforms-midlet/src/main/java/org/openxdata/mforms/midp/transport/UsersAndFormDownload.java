package org.openxdata.mforms.midp.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.db.util.Persistent;
import org.openxdata.mforms.UserList;
import org.openxdata.mforms.midp.db.EpihandyDataStorage;
import org.openxdata.mforms.midp.db.MalformedStreamException;
import org.openxdata.mforms.midp.db.StoredStudyDef;
import org.openxdata.mforms.midp.db.StudyStore;
import org.openxdata.mforms.midp.model.Model;

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
