package org.fcitmuk.epihandy.xform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import junit.framework.TestCase;

import org.fcitmuk.epihandy.FormData;
import org.fcitmuk.epihandy.FormDef;
import org.fcitmuk.epihandy.QuestionData;
import org.fcitmuk.epihandy.QuestionDef;
import org.fcitmuk.epihandy.RepeatQtnsData;
import org.fcitmuk.epihandy.RepeatQtnsDataList;
import org.fcitmuk.epihandy.RepeatQtnsDef;
import org.kxml2.kdom.Document;

public class EpihandyXformTest extends TestCase{

	public void testUpdateXformModel() throws Exception {
		Document doc = EpihandyXform.getDocument(new FileReader("adult opd.xml"));
		FormDef def = EpihandyXform.getFormDef(doc);
		FormData formData = new FormData(def);
		
		
		byte qtnId = 16;
		QuestionDef questionDef = def.getQuestion(qtnId);
		RepeatQtnsDef repeatQtnsDef = questionDef.getRepeatQtnsDef();
		QuestionDef childQtnDef = (QuestionDef)repeatQtnsDef.getQuestions().elementAt(0);
		
		RepeatQtnsData repeatQtnsData = new RepeatQtnsData();
		
		QuestionData childQtnData = new QuestionData(childQtnDef);
		childQtnData.setOptionAnswer("5276^FEMALE STERILIZATION^99DCT");
		repeatQtnsData.addQuestion(childQtnData);
		
		childQtnData = new QuestionData(childQtnDef);
		childQtnData.setOptionAnswer("190^CONDOMS^99DCT");
		repeatQtnsData.addQuestion(childQtnData);
		
		RepeatQtnsDataList repeatQtnsDataList = new RepeatQtnsDataList();
		repeatQtnsDataList.addRepeatQtnsData(repeatQtnsData);
		
		
		QuestionData questionData = formData.getQuestion(qtnId);
		questionData.setAnswer(repeatQtnsDataList);
		
		qtnId = 17;
		questionDef = def.getQuestion(qtnId);
		repeatQtnsDef = questionDef.getRepeatQtnsDef();
		childQtnDef = (QuestionDef)repeatQtnsDef.getQuestions().elementAt(0);
		
		repeatQtnsData = new RepeatQtnsData();
		
		childQtnData = new QuestionData(childQtnDef);
		childQtnData.setOptionAnswer("1387^ARMED FORCES^99DCT");
		repeatQtnsData.addQuestion(childQtnData);
		
		childQtnData = new QuestionData(childQtnDef);
		childQtnData.setOptionAnswer("1386^BUSINESS^99DCT");
		repeatQtnsData.addQuestion(childQtnData);
		
		childQtnData = new QuestionData(childQtnDef);
		childQtnData.setOptionAnswer("1385^FARMER^99DCT");
		repeatQtnsData.addQuestion(childQtnData);
		
		childQtnData = new QuestionData(childQtnDef);
		childQtnData.setOptionAnswer("1175^NOT APPLICABLE^99DCT");
		repeatQtnsData.addQuestion(childQtnData);
		
		childQtnData = new QuestionData(childQtnDef);
		childQtnData.setOptionAnswer("1484^UNEMPLOYED^99DCT");
		repeatQtnsData.addQuestion(childQtnData);
		
		childQtnData = new QuestionData(childQtnDef);
		childQtnData.setOptionAnswer("1384^CIVIL SERVANT^99DCT");
		repeatQtnsData.addQuestion(childQtnData);
		
		repeatQtnsDataList = new RepeatQtnsDataList();
		repeatQtnsDataList.addRepeatQtnsData(repeatQtnsData);
		
		questionData = formData.getQuestion(qtnId);
		questionData.setAnswer(repeatQtnsDataList);
		
		String xformModel = EpihandyXform.updateXformModel(doc, formData);
		File outFile = new File("problem_lists_model.xml");
		FileWriter out = new FileWriter(outFile);
		out.write(xformModel);
		out.close();
	}
	
	public void testFromXform2FormDef() throws IOException{
		try{
			
			Document doc = EpihandyXform.getDocument(new FileReader("repeatadvanced.txt"));
			FormDef def = EpihandyXform.getFormDef(doc);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			def.write(dos);
			
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
			FormDef f = new FormDef();
			f.read(dis);
			
			Vector qtns = f.getPageAt(1).getQuestions();
			for(int i=0; i<qtns.size(); i++){
				QuestionDef q = (QuestionDef)qtns.get(i);
				if(q.getType() == QuestionDef.QTN_TYPE_REPEAT){
					Vector qs = q.getRepeatQtnsDef().getQuestions();
					for(int index = 0; index < qs.size(); index ++){
						QuestionDef qn = (QuestionDef)qs.get(index);
						System.out.println(qn.getText());
						System.out.println("id= "+qn.getId());
						System.out.println(qn.getType());
					}
				}
			}
			System.out.println(f.getName());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
}
