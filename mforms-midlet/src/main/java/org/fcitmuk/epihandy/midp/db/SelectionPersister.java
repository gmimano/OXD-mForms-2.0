package org.fcitmuk.epihandy.midp.db;

import org.fcitmuk.epihandy.EpihandyConstants;
import org.fcitmuk.epihandy.midp.model.Model;
import org.fcitmuk.epihandy.midp.model.ModelListener;
import org.fcitmuk.midp.db.util.Settings;

/**
 * Listens to selection events from the model and persists them so that they
 * persist across application executions.
 * 
 * @author batkinson
 * 
 */
public class SelectionPersister implements ModelListener {

	private Settings settings;

	public SelectionPersister(Settings settings) {
		this.settings = settings;
	}

	public void formDataChanged(Model m) {
		// Unused
	}

	public void formDataSelected(Model m) {
		String selectedIndex = Integer.toString(m.getSelectedFormDataIndex());
		settings.setSetting(EpihandyConstants.KEY_LAST_SELECTED_FORMDATA,
				selectedIndex);
		settings.saveSettings();
	}

	public void formErrorsChanged(Model m) {
		// Unused
	}

	public void formSelected(Model m) {
		String selectedIndex = Integer.toString(m.getSelectedFormIndex());
		settings.setSetting(EpihandyConstants.KEY_LAST_SELECTED_FORMDEF,
				selectedIndex);
		settings.saveSettings();
	}

	public void formsChanged(Model m) {
		// Unused
	}

	public void studiesChanged(Model m) {
		// Unused
	}

	public void studySelected(Model m) {
		String selectedIndex = Integer.toString(m.getSelectedStudyIndex());
		settings.setSetting(EpihandyConstants.KEY_LAST_SELECTED_STUDY,
				selectedIndex);
		settings.saveSettings();
	}
}
