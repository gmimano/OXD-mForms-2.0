package org.openxdata.mforms.midp.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openxdata.mforms.midp.db.MalformedStreamException;
import org.openxdata.mforms.midp.db.StudyStore;
import org.openxdata.mforms.midp.model.Model;
import org.openxdata.mforms.persistent.Persistent;

/**
 * A class that enables study download with the existing transport layer.
 * 
 * @author Brent
 * 
 */
public class StudyDownload implements Persistent {

	StudyStore store;
	Model model;
	Exception e;

	public StudyDownload(StudyStore store, Model model) {
		this.store = store;
		this.model = model;
	}

	public void read(DataInputStream dis) throws IOException,
			InstantiationException, IllegalAccessException {
		try {
			int originalStudy = model.getSelectedStudyIndex();
			store.storeStudyListFromStream(dis);
			model.setStudies(store.getStudyDefList());
			if (originalStudy >= 0 && originalStudy < model.getStudies().length)
				model.setSelectedStudyIndex(originalStudy);
		} catch (MalformedStreamException e) {
			this.e = e;
		}
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
		// Unused
	}

}
