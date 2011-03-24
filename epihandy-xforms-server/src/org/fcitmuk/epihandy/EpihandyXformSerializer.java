package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.fcitmuk.epihandy.xform.EpihandyXform;
import org.kxml2.kdom.Document;

/**
 * Provides custom serialization of xforms for epihandy. We do binary serialization
 * to reduce the number of bytes sent over. It is very important that this class
 * does not swallow any exceptions but instead propagate them to the caller such
 * that any user data that is for instance is being submitted does not get lost
 * by this class absorbing exceptions and caller assumes everything went fine.
 * 
 * @author Daniel
 *
 */
public class EpihandyXformSerializer {
	
	@SuppressWarnings("unchecked")
	public void serializeStudies(OutputStream os, Object data) throws Exception {
		StudyDefList studyList = new StudyDefList();

		List<Object[]> studies = (List<Object[]>) data;
		for (Object[] study : studies)
			studyList.addStudy(new StudyDef(((Integer) study[0]).intValue(),
					(String) study[1], (String) study[1]));

		studyList.write(new DataOutputStream(os));
	}
	
	@SuppressWarnings("unchecked")
	public void serializeForms(OutputStream os, Object data, Integer studyId,
			String studyName) throws Exception {
		List<String> xforms = (List<String>) data;

		StudyDef studyDef = new StudyDef();
		studyDef.setForms(new Vector<Object>()); // Just to temporarily work on a possible bug of handling null forms.

		for (String xml : xforms) {

			// Wrapped in a try catch block such that when a form fails, we carry on with the rest
			try {
				FormDef formDef = EpihandyXform
						.fromXform2FormDef(new StringReader(xml));
				studyDef.addForm(formDef);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		studyDef.setName(studyName);
		studyDef.setId(studyId.intValue());
		studyDef.setVariableName(" ");
		studyDef.write(new DataOutputStream(os));
	}
	
	/**
	 * @see org.openmrs.module.xforms.SerializableData#serialize(java.io.OutputStream,java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public void serializeUsers(OutputStream os, Object data) throws Exception {
		List<Object[]> users = (List<Object[]>) data;

		DataOutputStream dos = new DataOutputStream(os);

		dos.writeShort(users.size());
		for (Object[] user : users) {
			serializeUser(new User((Integer) user[0], (String) user[1],
					(String) user[2], (String) user[3]), dos);
		}
	}
	
	/**
	 * Serializes a user to the stream.
	 * 
	 * @param user - the user to serialize.
	 * @param dos  - the stream to write to.
	 */
	private void serializeUser(User user, OutputStream os) throws Exception {
		DataOutputStream dos = new DataOutputStream(os);

		dos.writeInt(user.getUserId());
		dos.writeUTF(user.getName());
		dos.writeUTF(user.getPassword());
		dos.writeUTF(user.getSalt());
	}
	
	private List<DeserializationListener> deserializationListeners = new ArrayList<DeserializationListener>();

	public void addDeserializationListener(DeserializationListener listener) {
		deserializationListeners.add(listener);
	}

	public void removeDeserializationListener(DeserializationListener listener) {
		deserializationListeners.remove(listener);
	}

	protected void fireDeserializationStart() {
		for (DeserializationListener listener : deserializationListeners) {
			try {
				listener.start();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	protected void fireDeserializedModel(StudyDataList studyList) {
		for (DeserializationListener listener : deserializationListeners) {
			try {
				listener.deserializedModel(studyList);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	protected void fireProcessingStudy(StudyData studyData) {
		for (DeserializationListener listener : deserializationListeners) {
			try {
				listener.processingStudy(studyData);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	protected void fireProcessingForm(StudyData studyData, FormData formData) {
		for (DeserializationListener listener : deserializationListeners) {
			try {
				listener.processingForm(studyData, formData);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	protected void fireFormProcessed(StudyData studyData, FormData formData,
			String xml) {
		for (DeserializationListener listener : deserializationListeners) {
			try {
				listener.formProcessed(studyData, formData, xml);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	protected void fireFormError(Throwable formError) {
		for (DeserializationListener listener : deserializationListeners) {
			try {
				listener.formError(formError);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	protected void fireStudyProcessed(StudyData studyData) {
		for (DeserializationListener listener : deserializationListeners) {
			try {
				listener.studyProcessed(studyData);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	protected void fireStudyError(Throwable studyError) {
		for (DeserializationListener listener : deserializationListeners) {
			try {
				listener.studyError(studyError);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	protected void fireDeserializationComplete(StudyDataList studyList,
			List<String> formXml) {
		for (DeserializationListener listener : deserializationListeners) {
			try {
				listener.complete(studyList, formXml);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	protected void fireDeserializationError(Throwable deserialzationError) {
		for (DeserializationListener listener : deserializationListeners) {
			try {
				listener.failed(deserialzationError);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * Deserializes study upload data and notified listeners of events.
	 * 
	 * @param is
	 * @param data
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void deserializeStudiesWithEvents(InputStream is, Object data)
			throws Exception {

		try {
			fireDeserializationStart();

			DataInputStream dis = new DataInputStream(is);

			Map<Integer, String> xformMap = (Map<Integer, String>) data;

			StudyDataList studyDataList = new StudyDataList();
			studyDataList.read(dis);

			fireDeserializedModel(studyDataList);

			Vector<StudyData> studies = studyDataList.getStudies();
			List<String> formXml = new ArrayList<String>();

			for (StudyData studyData : studies) {
				try {
					fireProcessingStudy(studyData);
					for (FormData formData : (Vector<FormData>) studyData
							.getForms()) {
						try {
							fireProcessingForm(studyData, formData);
							String xml = deserializeFormToXML(formData,
									xformMap);
							formXml.add(xml);
							fireFormProcessed(studyData, formData, xml);
						} catch (Exception t) {
							fireFormError(t);
							throw t;
						}
					}
					fireStudyProcessed(studyData);
				} catch (Exception t) {
					fireStudyError(t);
					throw t;
				}
			}

			fireDeserializationComplete(studyDataList, formXml);

		} catch (Exception t) {
			fireDeserializationError(t);
			throw t;
		}
	}

	private String deserializeFormToXML(FormData formData,
			Map<Integer, String> xformMap) throws Exception {

		String xml = xformMap.get(formData.getDefId());

		if (xml == null)
			throw new FormNotFoundException("Cannot find form with id = "
					+ formData.getDefId());

		Document doc = EpihandyXform.getDocument(new StringReader(xml));
		formData.setDef(EpihandyXform.getFormDef(doc));
		xml = EpihandyXform.updateXformModel(doc, formData);

		return xml;
	}
	
	/**
	 * @see org.openmrs.module.xforms.SerializableData#deSerialize(java.io.InputStream, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Object deSerialize(InputStream is, Object data) throws Exception{
		DataInputStream dis = new DataInputStream(is);
		
		List<String> xmlforms = new ArrayList<String>();
		Map<Integer, String> xformMap = (Map<Integer, String>)data;

		StudyDataList studyDataList = new StudyDataList();
		studyDataList.read(dis);
		Vector<StudyData> studies = studyDataList.getStudies();
		for (StudyData studyData : studies) {
			deSerialize(studyData, xmlforms, xformMap);
		}
		return xmlforms;
	}
	
	@SuppressWarnings("unchecked")
	private void deSerialize(StudyData studyData,List<String> xmlforms,Map<Integer, String> xformMap) throws Exception{
		Vector<FormData> forms = studyData.getForms();
		for(FormData formData : forms){
			String xml = xformMap.get(formData.getDefId());
			
			//Form could be deleted on server when mobile has reference to it
			if(xml == null)
				throw new FormNotFoundException("Cannot find form with id = "+formData.getDefId());
			
			Document doc = EpihandyXform.getDocument(new StringReader(xml));
			formData.setDef(EpihandyXform.getFormDef(doc));
			xml = EpihandyXform.updateXformModel(doc,formData);
			xmlforms.add(xml);
		}
	}
}
