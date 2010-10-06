package org.fcitmuk.epihandy;

import java.util.List;

/**
 * Empty stub to make implementing deserialization listeners more convenient.
 * 
 * @author Brent
 * 
 */
public class DeserializationListenerAdapter implements DeserializationListener {

	public void complete(StudyDataList studyDataList, List<String> xmlForms) {
	}

	public void deserializedModel(StudyDataList studyDataList) {
	}

	public void failed(Throwable t) {
	}

	public void formError(Throwable t) {
	}

	public void formProcessed(StudyData studyData, FormData formData, String xml) {
	}

	public void processingForm(StudyData studyData, FormData formData) {
	}

	public void processingStudy(StudyData studyData) {
	}

	public void start() {
	}

	public void studyError(Throwable t) {
	}

	public void studyProcessed(StudyData studyData) {
	}

}
