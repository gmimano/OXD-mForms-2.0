package org.fcitmuk.epihandy;

import java.util.List;

/**
 * An interface enabling notification of events during the serialization
 * process.
 * 
 * @author Brent
 * 
 */
public interface DeserializationListener {

	void start();

	void deserializedModel(StudyDataList studyDataList);

	void processingStudy(StudyData studyData);

	void processingForm(StudyData studyData, FormData formData);

	void formProcessed(StudyData studyData, FormData formData, String xml);

	void formError(Throwable t);

	void studyProcessed(StudyData studyData);

	void studyError(Throwable t);

	void complete(StudyDataList studyDataList, List<String> xmlForms);

	void failed(Throwable t);
}
