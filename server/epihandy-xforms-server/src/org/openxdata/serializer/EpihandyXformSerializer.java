package org.openxdata.serializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.kxml2.kdom.Document;
import org.openxdata.model.FormData;
import org.openxdata.model.FormDef;
import org.openxdata.model.StudyData;
import org.openxdata.model.StudyDataList;
import org.openxdata.model.StudyDef;
import org.openxdata.model.StudyDefList;
import org.openxdata.model.User;
import org.openxdata.xform.EpihandyXform;


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
	
	public void serializeStudies(OutputStream os,Object data) throws Exception{
		//try{
			StudyDefList studyList = new StudyDefList();
			
			List<Object[]> studies = (List<Object[]>)data;
			for(Object[] study : studies)
				studyList.addStudy(new StudyDef(((Integer)study[0]).intValue(),(String)study[1],(String)study[1]));
			
			studyList.write(new DataOutputStream(os));
		/*}
		catch(Exception e){
			e.printStackTrace();
		}*/
	}
	
	public void serializeForms(OutputStream os,Object data, Integer studyId, String studyName, String studyKey) throws Exception{
		//try{
			List<String> xforms = (List<String>)data;
			
			StudyDef studyDef = new StudyDef();
			studyDef.setForms(new Vector()); //Just to temporarily work on a possible bug of handling null forms.
			
			for(String xml : xforms){
				
				//Wrapped in a try catch block such that when a form fails, we carry on with the rest
				try{
					FormDef formDef = EpihandyXform.fromXform2FormDef(new StringReader(xml));
					studyDef.addForm(formDef);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}			
						
			studyDef.setName(studyName);
			studyDef.setId(studyId.intValue());
			studyDef.setVariableName(studyKey);
			studyDef.write(new DataOutputStream(os));
		/*}
		catch(Exception e){
			e.printStackTrace();
		}*/
	}
	
	/**
	 * @see org.openmrs.module.xforms.SerializableData#serialize(java.io.OutputStream,java.lang.Object)
	 */
	public void serializeUsers(OutputStream os,Object data) throws Exception{
		//try{
			List<Object[]> users = (List<Object[]>)data; 

			DataOutputStream dos = new DataOutputStream(os);
			
			dos.writeByte(users.size());
			for(Object[] user : users)
				serializeUser(new User((Integer)user[0],(String)user[1],(String)user[2],(String)user[3]),dos);
			
		/*}catch(Exception e){
			e.printStackTrace();
		}*/
	}
	
	/**
	 * Serializes a user to the stream.
	 * 
	 * @param user - the user to serialize.
	 * @param dos  - the stream to write to.
	 */
	private void serializeUser(User user, OutputStream os) throws Exception{
		//try{
			DataOutputStream dos = new DataOutputStream(os);
			
			dos.writeInt(user.getUserId());
			dos.writeUTF(user.getName());
			dos.writeUTF(user.getPassword());
			dos.writeUTF(user.getSalt());
		/*}
		catch(Exception e){
			e.printStackTrace();
		}*/
	}
	
	/**
	 * @see org.openmrs.module.xforms.SerializableData#deSerialize(java.io.InputStream, java.lang.Object)
	 */
	public Object deSerialize(InputStream is, Object data) throws Exception{
		DataInputStream dis = new DataInputStream(is);
		
		List<String> xmlforms = new ArrayList<String>();
		Map<Integer, String> xformMap = (Map<Integer, String>)data;

		//try{
			StudyDataList studyDataList = new StudyDataList();
			studyDataList.read(dis);
			Vector<StudyData> studies = studyDataList.getStudies();
			for(StudyData studyData: studies)
				deSerialize(studyData,xmlforms,xformMap);
		/*}
		catch(Exception e){
			e.printStackTrace();
		}*/
		
		return xmlforms;
	}
	
	private void deSerialize(StudyData studyData,List<String> xmlforms,Map<Integer, String> xformMap) throws Exception{
		Vector<FormData> forms = studyData.getForms();
		for(FormData formData : forms){
			String xml = xformMap.get(formData.getDefId());
			
			//Form could be deleted on server when mobile has reference to it
			if(xml == null)
				throw new Exception("Cannot find form with id = "+formData.getDefId());
			
			Document doc = EpihandyXform.getDocument(new StringReader(xml));
			formData.setDef(EpihandyXform.getFormDef(doc));
			xml = EpihandyXform.updateXformModel(doc,formData);
			xmlforms.add(xml);
		}
	}
}
