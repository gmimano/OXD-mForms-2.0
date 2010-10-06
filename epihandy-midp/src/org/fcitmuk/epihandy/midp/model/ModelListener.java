package org.fcitmuk.epihandy.midp.model;

/**
 * An interface for delivering model events.
 * 
 * @author Brent
 * 
 */
public interface ModelListener {

	void studiesChanged(Model m);

	void studySelected(Model m);

	void formsChanged(Model m);

	void formSelected(Model m);

	void formDataChanged(Model m);

	void formDataSelected(Model m);

	void formErrorsChanged(Model m);
}
