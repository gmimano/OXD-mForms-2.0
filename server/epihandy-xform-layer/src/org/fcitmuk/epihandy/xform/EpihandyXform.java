package org.fcitmuk.epihandy.xform;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

import minixpath.XPathExpression;

import org.fcitmuk.epihandy.Condition;
import org.fcitmuk.epihandy.DynamicOptionDef;
import org.fcitmuk.epihandy.EpihandyConstants;
import org.fcitmuk.epihandy.FormData;
import org.fcitmuk.epihandy.FormDef;
import org.fcitmuk.epihandy.OptionDef;
import org.fcitmuk.epihandy.PageData;
import org.fcitmuk.epihandy.PageDef;
import org.fcitmuk.epihandy.QuestionData;
import org.fcitmuk.epihandy.QuestionDef;
import org.fcitmuk.epihandy.RepeatQtnsData;
import org.fcitmuk.epihandy.RepeatQtnsDataList;
import org.fcitmuk.epihandy.RepeatQtnsDef;
import org.fcitmuk.epihandy.SkipRule;
import org.fcitmuk.epihandy.ValidationRule;
import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;



/**
 * Provides conversion from xform to epihandy object model and vice vasa.
 * 
 * @author Daniel Kayiwa
 *
 */
public class EpihandyXform{

	public static final String ATTRIBUTE_VALUE_ENABLE = "enable";
	public static final String ATTRIBUTE_VALUE_DISABLE = "disable";
	public static final String ATTRIBUTE_VALUE_SHOW = "show";
	public static final String ATTRIBUTE_VALUE_HIDE = "hide";

	private static byte nextOptionId = -128;
	private static byte nextQuestionId = -128;

	/**
	 * Default Constructor
	 *
	 */
	public EpihandyXform(){

	}
	
	private static byte getNextOptionId() {
		return nextOptionId++;
	}
	
	private static byte getNextQuestionId() {
		return nextQuestionId++;
	}
	
	private static void initIds(){
		nextOptionId = -128;
		nextQuestionId = -128;
	}
	
	private static boolean reachedMaxOptionId(){
		return nextOptionId > 127;
	}
	
	private static boolean reachedMaxQuestionId(){
		return nextQuestionId > 127;
	}

	private static String getXmlType(byte type, Element node){
		if(type == QuestionDef.QTN_TYPE_VIDEO)
			node.setAttribute(null,"format", "video");
		else if(type == QuestionDef.QTN_TYPE_AUDIO)
			node.setAttribute(null,"format", "audio");
		else if(type == QuestionDef.QTN_TYPE_IMAGE)
			node.setAttribute(null,"format", "image");
		else if(type == QuestionDef.QTN_TYPE_GPS)
			node.setAttribute(null,"format", "gps");

		switch(type){
		case QuestionDef.QTN_TYPE_BOOLEAN:
			return "xsd:boolean";
		case QuestionDef.QTN_TYPE_DATE:
			return "xsd:date";
		case QuestionDef.QTN_TYPE_DATE_TIME:
			return "xsd:dateTime";
		case QuestionDef.QTN_TYPE_TIME:
			return "xsd:time";
		case QuestionDef.QTN_TYPE_DECIMAL:
			return "xsd:decimal";
		case QuestionDef.QTN_TYPE_NUMERIC:
			return "xsd:int";
		case QuestionDef.QTN_TYPE_TEXT:
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
		case QuestionDef.QTN_TYPE_GPS:
			return "xsd:string";
		case QuestionDef.QTN_TYPE_IMAGE:
		case QuestionDef.QTN_TYPE_VIDEO:
		case QuestionDef.QTN_TYPE_AUDIO:
			return "xsd:base64Binary";
		}

		return "";
	}

	/*public static String fromFormDef2Xform(FormDef formDef){
		Document doc = new Document();
		doc.setEncoding("UTF-8");
		Element htmlNode = doc.createElement("http://www.w3.org/1999/xhtml", null);
		htmlNode.setName("html");
		htmlNode.setPrefix(null, "http://www.w3.org/1999/xhtml");
		htmlNode.setPrefix("xf", "http://www.w3.org/2002/xforms");
		htmlNode.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
		htmlNode.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		doc.addChild(org.kxml2.kdom.Element.ELEMENT, htmlNode);

		Element headNode = doc.createElement("http://www.w3.org/1999/xhtml", null);
		headNode.setName("head");
		htmlNode.addChild(org.kxml2.kdom.Element.ELEMENT, headNode);

		Element titleNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
		titleNode.setName("title");
		titleNode.addChild(Element.TEXT,formDef.getName());
		headNode.addChild(Element.ELEMENT,titleNode);

		Element modelNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
		modelNode.setName("model");
		headNode.addChild(Element.ELEMENT,modelNode);

		Element instanceNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
		instanceNode.setName("instance");
		modelNode.addChild(Element.ELEMENT,instanceNode);

		Element formNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
		formNode.setName(formDef.getVariableName());
		//formNode.setAttribute(null, "id", Integer.toString(formDef.getId()));
		instanceNode.addChild(Element.ELEMENT,formNode);

		Element bodyNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
		bodyNode.setName("body");
		htmlNode.addChild(Element.ELEMENT,bodyNode);

		for(int pageNo=0; pageNo<formDef.getPages().size(); pageNo++){
			PageDef page = (PageDef)formDef.getPages().elementAt(pageNo);
			Vector questions = page.getQuestions();
			for(int i=0; i<questions.size(); i++){
				QuestionDef qtn = (QuestionDef)questions.elementAt(i);
				Element dataNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
				dataNode.setName(qtn.getVariableName());
				//dataNode.addChild(Element.TEXT,"data");
				formNode.addChild(Element.ELEMENT,dataNode);

				Element bindNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
				bindNode.setName("bind");
				bindNode.setAttribute(null, "id", qtn.getVariableName());
				bindNode.setAttribute(null, "nodeset", formDef.getVariableName() + "/" + qtn.getVariableName());
				bindNode.setAttribute(null, "type", getXmlType(qtn.getType(),bindNode));	
				if(qtn.isMandatory())
					bindNode.setAttribute(null, "required", "true()");
				if(!qtn.isEnabled())
					bindNode.setAttribute(null, "readonly", "true()");
				modelNode.addChild(Element.ELEMENT,bindNode);


				Element inputNode =  getXformInputElementName(doc,qtn);
				bodyNode.addChild(Element.ELEMENT,inputNode);

				Element labelNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
				labelNode.setName("label");
				labelNode.addChild(Element.TEXT,qtn.getText());
				inputNode.addChild(Element.ELEMENT,labelNode);

				String helpText = qtn.getHelpText();
				if(helpText != null && helpText.length() > 0){
					Element hintNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
					hintNode.setName("hint");
					hintNode.addChild(Element.TEXT,helpText);
					inputNode.addChild(Element.ELEMENT,hintNode);
				}

				Vector options = qtn.getOptions();
				if(options != null && options.size() > 0){
					for(int j=0; j<options.size(); j++){
						OptionDef optionDef = (OptionDef)options.elementAt(j);

						Element itemNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
						itemNode.setName("item");

						Element node =  doc.createElement("http://www.w3.org/2002/xforms", null);
						node.setName("label");
						node.addChild(Element.TEXT,optionDef.getText());
						itemNode.addChild(Element.ELEMENT,node);

						node =  doc.createElement("http://www.w3.org/2002/xforms", null);
						node.setName("value");
						node.addChild(Element.TEXT,optionDef.getVariableName());
						itemNode.addChild(Element.ELEMENT,node);

						inputNode.addChild(Element.ELEMENT,itemNode);
					}
				}
			}
		}

		return fromDoc2String(doc);
	}*/

	private static Element getXformInputElementName(Document doc, QuestionDef qtnDef){
		Element node = doc.createElement("http://www.w3.org/2002/xforms", null);
		node.setAttribute(null, "bind", qtnDef.getVariableName());

		switch(qtnDef.getType()){
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
			node.setName("select1");
			node.setAttribute(null, "selection", "closed");
			break;
		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
			node.setName("select");
			node.setAttribute(null, "selection", "closed");
			break;
		default:
			node.setName("input");
		}

		return node;
	}

	/*/**
	 * Updates the XForm model with the answers.
	 * 
	 * @param xml -  the XForm xml having the model.
	 * @param formData - the form data having the answers.
	 * @return - a string representing the xml of the updated model only.
	 */
	/*public static String updateXformModel(String xml, FormData formData){
		Document doc = getDocument(new java.io.StringReader(xml));

		for(int pageNo=0; pageNo<formData.getPages().size(); pageNo++){
			PageData page = (PageData)formData.getPages().elementAt(pageNo);
			Vector questions = page.getQuestions();
			for(int i=0; i<questions.size(); i++)
				updateModel(doc,(QuestionData)questions.elementAt(i));
		}

		Element instanceDataNode = getInstanceDataNode(doc);
		return fromDoc2String(getDocumentFromNode(instanceDataNode));
	}*/

	/**
	 * Updates the XForm model with the answers.
	 * 
	 * @param doc -  the XForm document having the model.
	 * @param formData - the form data having the answers.
	 * @return - a string representing the xml of the updated model only.
	 */
	public static String updateXformModel(Document doc, FormData formData) throws Exception {
		for(int pageNo=0; pageNo<formData.getPages().size(); pageNo++){
			PageData page = (PageData)formData.getPages().elementAt(pageNo);
			Vector questions = page.getQuestions();
			if(questions != null){
				for(int i=0; i<questions.size(); i++)
					updateModel(doc,(QuestionData)questions.elementAt(i),formData.getDef());
			}
		}

		Element instanceDataNode = getInstanceDataNode(doc);
		return fromDoc2String(getDocumentFromNode(instanceDataNode));
	}

	public static Document getDocumentFromNode(Element element){
		Document doc = new Document();
		doc.setEncoding("UTF-8");
		/*Element rootNode = doc.createElement(null, null);
		rootNode.setName(element.getName());
		doc.addChild(org.kxml2.kdom.Element.ELEMENT, rootNode);

		updateDocumentFromNode(doc,rootNode,element);*/

		doc.addChild(org.kxml2.kdom.Element.ELEMENT, element);

		element.setPrefix("xf", "http://www.w3.org/2002/xforms");
		element.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
		element.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");

		return doc;
	}

	private static void updateDocumentFromNode(Document doc,Element copyTo,Element copyFrom){
		int numOfEntries = copyFrom.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			Element oldNode = copyFrom.getElement(i);
			Element newNode = doc.createElement(null, null);
			if(!copyFrom.isText(i)){
				newNode.setName(oldNode.getName());
				copyTo.addChild(Element.ELEMENT, newNode);
				updateDocumentFromNode(doc,newNode,oldNode);
			}
			else
				copyTo.addChild(Element.TEXT, copyFrom.getText(i));
		}
	}

	/**
	 * Fills an xforms document model with answers from a question.
	 * 
	 * @param doc
	 * @param qtnData
	 */
	private static void updateModel(Document doc, QuestionData qtnData, FormDef formDef) throws Exception{
		//we dot spit out answers for invisible and disabled questions since
		//they are considered non-relevant. (Is this really correct?????)
		if(true/*qtnData.getDef().isVisible() && qtnData.getDef().isEnabled()*/){		
			String value = qtnData.getValueAnswer();

			if (value != null) {
				if(qtnData.getDef().getType() == QuestionDef.QTN_TYPE_AUDIO ||
						qtnData.getDef().getType() == QuestionDef.QTN_TYPE_IMAGE ||
						qtnData.getDef().getType() == QuestionDef.QTN_TYPE_VIDEO){

					value = Base64.encode((byte[])qtnData.getAnswer());					
				}

				Element elem = getInstanceNode(doc);
				String xpath = qtnData.getDef().getVariableName();

				if(qtnData.getDef().getType() != QuestionDef.QTN_TYPE_REPEAT){
					xpath = new String(xpath.toCharArray(), 1, xpath.length()-1);
					int pos = xpath.lastIndexOf('@'); String attributeName = null;
					if(pos > 0){
						attributeName = xpath.substring(pos+1,xpath.length());
						xpath = xpath.substring(0,pos-1);
					}
					XPathExpression xpls = new XPathExpression(elem, xpath);
					Vector result = xpls.getResult();

					for (Enumeration e = result.elements(); e.hasMoreElements();) {
						Object obj = e.nextElement();
						if (obj instanceof Element){
							if(pos > 0) //Check if we are to set attribute value.
								((Element) obj).setAttribute(null, attributeName, value);
							else{
								removeTextNode((Element)obj);
								((Element) obj).addChild(Node.TEXT, value);
							}

							break;
						}
					}
				}
				else
					updateRepeatModel(elem,qtnData,formDef);
			}
		}
	}

	/**
	 * Updates an xforms document with answers from a repeat question.
	 * 
	 * @param doc the xforms document.
	 * @param qtnData the answer data.
	 */
	private static void updateRepeatModel(Element instanceNode, QuestionData qtnData, FormDef formDef) throws Exception{
		RepeatQtnsDataList repeatQtnsDataList = (RepeatQtnsDataList)qtnData.getAnswer();
		if(repeatQtnsDataList == null)
			return; //Not a single repeat row filled.

		String xpath = qtnData.getDef().getVariableName();

		String formVarName = formDef.getVariableName();
		if(!xpath.startsWith(formVarName))
			formVarName = "/" + formVarName;
		formVarName += "/";

		boolean nested = xpath.substring(formVarName.length()).contains("/");

		//Element elem = getInstanceNode(doc);
		xpath = new String(xpath.toCharArray(), 1, xpath.length()-1);
		XPathExpression xpls = new XPathExpression(instanceNode, xpath);
		Vector result = xpls.getResult();

		for (Enumeration e = result.elements(); e.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof Element){
				Element elem = (Element) obj;
				Element parent = (Element)((Element) obj).getParent();
				for(int i=0; i<repeatQtnsDataList.size(); i++){
					if(i > 0 /*&& nested*/){
						elem = copyNode((Element) obj);
						removeChildTextNodes(elem);
						parent.addChild(Element.ELEMENT, elem);
					}
					updateRepeatModel(repeatQtnsDataList.getRepeatQtnsData(i),elem,instanceNode,i,nested);
				}

				break;
			}
		}
	}

	private static void removeChildTextNodes(Element node){
		for(int i = 0; i < node.getChildCount(); i++){
			Object child = node.getChild(i);
			if(child instanceof Element)
				removeTextNode((Element)child);
		}
	}

	/**
	 * Updates an xforms document repeat model element with answers.
	 * 
	 * @param doc the xforms document.
	 * @param repeatQtnsDataList the repeat answers.
	 * @param parentNode the model repeat element.
	 */
	private static void updateRepeatModel(RepeatQtnsData repeatQtnsData, Element rptParentNode, Element instanceNode,int index, boolean nested){
		for(int i=0; i<repeatQtnsData.size(); i++){
			QuestionData data = repeatQtnsData.getQuestion(i);
			if(data.getValueAnswer() != null)
				updateRepeatRowModel(data,rptParentNode,instanceNode,index,nested);
		}
	}

	private static void updateRepeatRowModel(QuestionData questionData, Element rptParentNode,Element instanceNode, int index, boolean nested){
		//make multiple copies of this repeat node and fill the with answers in each item in the repeatQtnsDataList
		String xpath = questionData.getDef().getVariableName();

		/*if(xpath.contains("/")){
			xpath = xpath.substring(0, xpath.lastIndexOf('/')); //xpath = xpath.substring(id.lastIndexOf('/')+1);
			xpath = new String(xpath.toCharArray(), 1, xpath.length()-1);
		}*/

		XPathExpression xpls = new XPathExpression(rptParentNode/*instanceNode*/, xpath);
		Vector result = xpls.getResult();

		if(result.size() > 0){
			Element node = (Element)result.elementAt(0);
			Element newNode = node;
			/*if(index > 0 && !nested){
				newNode = copyNode(node);
				rptParentNode.addChild(Node.ELEMENT, newNode);
			}*/

			removeTextNode(newNode); //remove text if any.
			newNode.addChild(Node.TEXT,questionData.getValueAnswer());
			//updateRepeatModelValue(questionData.getValueAnswer(),node,rptParentNode);
			//throw new Exception("Needs serious work");
		}
	}

	/*private static void updateRepeatModelValue(String valueAnswer, Element rptNode, Element rptParentNode){
		String xpath = "value";
		XPathExpression xpls = new XPathExpression(rptNode, xpath);
		Vector result = xpls.getResult();

		boolean found = false;
		Element valueNode = null;
		for (Enumeration e = result.elements(); e.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof Element){
				valueNode = ((Element) obj);
				if(!hasTextNode(valueNode)){
					found = true;
					valueNode.addChild(Node.TEXT, valueAnswer);
					rptParentNode.addChild(Element.TEXT, "  ");
					break;
				}	
			}
		}

		if(!found){ //no empty node found.
			Element newNode = copyNode(rptNode);

			rptParentNode.addChild(Node.ELEMENT, newNode);
			rptParentNode.addChild(Element.TEXT, "\n      ");

			valueNode = getValueNode(newNode);
			if(valueNode != null){
				removeTextNode(valueNode); //must have copied one which already had value. So remove it.
				valueNode.addChild(Node.TEXT,valueAnswer);
			}
			else
				newNode.addChild(Node.TEXT,valueAnswer);
		}
	}*/

	private static Element getFirstChildElement(Element node, String name){
		Vector list = new Vector();
		Element ret = null;
		for(int i=0; i<node.getChildCount(); i++){
			if(node.getType(i) == Element.ELEMENT ){
				Element child  = (Element)node.getChild(i);
				if(child.getName().equalsIgnoreCase(name)){
					ret = child;
					list.addElement(new Integer(i));
				}
			}
		}

		while(list.size() > 1){
			Integer pos = (Integer)list.elementAt(1);
			node.removeChild(pos.intValue());
			list.remove(1);
		}
		return ret;
	}

	private static Element getValueNode(Element node){
		String xpath = "value";
		XPathExpression xpls = new XPathExpression(node, xpath);
		Vector result = xpls.getResult();

		for (Enumeration e = result.elements(); e.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof Element)
				return (Element)obj;
		}

		return null;
	}

	private static boolean hasTextNode(Element node){
		for(int i=0; i<node.getChildCount(); i++){
			if(node.getType(i) == Element.TEXT)
				return true;
		}
		return false;
	}

	private static void removeTextNode(Element node){
		for(int i=0; i<node.getChildCount(); i++){
			if(node.getType(i) == Element.TEXT){
				String text = node.getText(i).trim();
				if(text.length() > 0 && !text.equalsIgnoreCase("\n")){
					node.removeChild(i);
					return;
				}
			}
		}
	}

	private static Element copyNode(Element node) throws Exception{
		Document doc = new Document();
		doc.setEncoding("UTF-8");
		Element htmlNode = doc.createElement("http://www.w3.org/2001/XMLSchema-instance", null);
		htmlNode.setName("html");
		htmlNode.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		doc.addChild(Element.ELEMENT, htmlNode);
		htmlNode.addChild(Element.ELEMENT, node);

		String xml = EpihandyXform.fromNode2String(doc.getRootElement()); //doc.getRootElement()

		doc = getDocument(new StringReader(xml));
		Element retNode = doc.getRootElement().getElement(0);

		return retNode;
	}

	private static Element getInstanceNode(Document doc){
		return getInstanceNode(doc.getRootElement());

	}

	private static Element getInstanceDataNode(Document doc){
		return getInstanceDataNode(getInstanceNode(doc));
	}

	private static Element getInstanceNode(Element element){
		int numOfEntries = element.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (!element.isText(i) && element.getType(i) == Element.ELEMENT) {
				Element child = element.getElement(i);
				String tagname = child.getName();
				if (tagname.equals("instance"))
					return child;
				else{
					child = getInstanceNode(child);
					if(child != null)
						return child;
				}
			}
		}
		return null;
	}

	private static Element getInstanceDataNode(Element element){
		int numOfEntries = element.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (!element.isText(i) && element.getType(i) == Element.ELEMENT) 
				return element.getElement(i);
		}

		return null;
	}

	public static String fromFormData2XformModel(FormData formData) throws Exception {
		FormDef formDef = formData.getDef();
		Document doc = new Document();
		doc.setEncoding("UTF-8");
		Element rootNode = doc.createElement(null, null);
		rootNode.setName(formDef.getVariableName());
		//rootNode.setAttribute(null, "id", Integer.toString(formDef.getId()));
		doc.addChild(org.kxml2.kdom.Element.ELEMENT, rootNode);

		for(int pageNo=0; pageNo<formData.getPages().size(); pageNo++){
			PageData page = (PageData)formData.getPages().elementAt(pageNo);
			Vector questions = page.getQuestions();
			if(questions != null){
				for(int i=0; i<questions.size(); i++){
					QuestionData qtnData = (QuestionData)questions.elementAt(i);
					//we dot spit out answers for invisible and disabled questions since
					//they are considered non-relevant.
					if(qtnData.getDef().isVisible() && qtnData.getDef().isEnabled()){
						Element node =  doc.createElement(null, null);
						node.setName(qtnData.getDef().getVariableName());
						if(qtnData.getValueAnswer() != null)
							node.addChild(Element.TEXT,qtnData.getValueAnswer());
						rootNode.addChild(Element.ELEMENT,node);
					}
				}	
			}
		}

		return fromDoc2String(doc);
	}	

	//TODO This and the one below need to be refactored.
	public static String fromNode2String(Node node) throws Exception {
		KXmlSerializer serializer = new KXmlSerializer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		try{
			serializer.setOutput(dos,"UTF-8");
			node.write(serializer);
			serializer.flush();
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}

		return new String(bos.toByteArray(),"UTF-8");
		
		/*KXmlSerializer serializer = new KXmlSerializer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		try{
			serializer.setOutput(dos,null);
			node.write(serializer);
			serializer.flush();
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}

		byte[] byteArr = bos.toByteArray();
		char[]charArray = new char[byteArr.length];
		for(int i=0; i<byteArr.length; i++)
			charArray[i] = (char)byteArr[i];

		return String.valueOf(charArray);*/
	}


	public static String fromDoc2String(Document doc) throws Exception {
		KXmlSerializer serializer = new KXmlSerializer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		try{
			serializer.setOutput(dos,"UTF-8");
			doc.write(serializer);
			serializer.flush();
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}

		return new String(bos.toByteArray(),"UTF-8");
		
		/*byte[] byteArr = bos.toByteArray();
		char[]charArray = new char[byteArr.length];
		for(int i=0; i<byteArr.length; i++)
			charArray[i] = (char)byteArr[i];
		
		try{
			String s = new String(byteArr,"UTF-8");
			return s;
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return String.valueOf(charArray);*/
	}

	/*public static String fromFormData2Xform(FormData formData){
		FormDef formDef = formData.getDef();
		Document doc = new Document();
		doc.setEncoding("UTF-8");
		Element htmlNode = doc.createElement("http://www.w3.org/1999/xhtml", null);
		htmlNode.setName("html");
		htmlNode.setPrefix(null, "http://www.w3.org/1999/xhtml");
		htmlNode.setPrefix("xf", "http://www.w3.org/2002/xforms");
		htmlNode.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
		htmlNode.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		doc.addChild(org.kxml2.kdom.Element.ELEMENT, htmlNode);

		Element headNode = doc.createElement("http://www.w3.org/1999/xhtml", null);
		headNode.setName("head");
		htmlNode.addChild(org.kxml2.kdom.Element.ELEMENT, headNode);

		Element titleNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
		titleNode.setName("title");
		titleNode.addChild(Element.TEXT,formDef.getName());
		headNode.addChild(Element.ELEMENT,titleNode);

		Element modelNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
		modelNode.setName("model");
		headNode.addChild(Element.ELEMENT,modelNode);

		Element instanceNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
		instanceNode.setName("instance");
		modelNode.addChild(Element.ELEMENT,instanceNode);

		Element formNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
		formNode.setName(formDef.getVariableName());
		//formNode.setAttribute(null, "id", Integer.toString(formDef.getId()));
		instanceNode.addChild(Element.ELEMENT,formNode);

		Element bodyNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
		bodyNode.setName("body");
		htmlNode.addChild(Element.ELEMENT,bodyNode);

		for(int pageNo=0; pageNo<formData.getPages().size(); pageNo++){
			PageData page = (PageData)formData.getPages().elementAt(pageNo);
			Vector questions = page.getQuestions();
			for(int i=0; i<questions.size(); i++){
				QuestionData qtn = (QuestionData)questions.elementAt(i);
				Element dataNode =  doc.createElement("http://www.w3.org/1999/xhtml", null);
				dataNode.setName(qtn.getDef().getVariableName());
				if(qtn.getValueAnswer() != null)
					dataNode.addChild(Element.TEXT,qtn.getValueAnswer());
				formNode.addChild(Element.ELEMENT,dataNode);

				Element bindNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
				bindNode.setName("bind");
				bindNode.setAttribute(null, "id", qtn.getDef().getVariableName());
				bindNode.setAttribute(null, "nodeset", formDef.getVariableName() + "/" + qtn.getDef().getVariableName());
				bindNode.setAttribute(null, "type", getXmlType(qtn.getDef().getType(),bindNode));	
				if(qtn.getDef().isMandatory())
					bindNode.setAttribute(null, "required", "true()");
				if(!qtn.getDef().isEnabled())
					bindNode.setAttribute(null, "readonly", "true()");
				modelNode.addChild(Element.ELEMENT,bindNode);


				Element inputNode =  getXformInputElementName(doc,qtn.getDef());
				bodyNode.addChild(Element.ELEMENT,inputNode);

				Element labelNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
				labelNode.setName("label");
				labelNode.addChild(Element.TEXT,qtn.getDef().getText());
				inputNode.addChild(Element.ELEMENT,labelNode);

				String helpText = qtn.getDef().getHelpText();
				if(helpText != null && helpText.length() > 0){
					Element hintNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
					hintNode.setName("hint");
					hintNode.addChild(Element.TEXT,helpText);
					inputNode.addChild(Element.ELEMENT,hintNode);
				}

				Vector options = qtn.getDef().getOptions();
				if(options != null && options.size() > 0){
					for(int j=0; j<options.size(); j++){
						OptionDef optionDef = (OptionDef)options.elementAt(j);

						Element itemNode =  doc.createElement("http://www.w3.org/2002/xforms", null);
						itemNode.setName("item");

						Element node =  doc.createElement("http://www.w3.org/2002/xforms", null);
						node.setName("label");
						node.addChild(Element.TEXT,optionDef.getText());
						itemNode.addChild(Element.ELEMENT,node);

						node =  doc.createElement("http://www.w3.org/2002/xforms", null);
						node.setName("value");
						node.addChild(Element.TEXT,optionDef.getVariableName());
						itemNode.addChild(Element.ELEMENT,node);

						inputNode.addChild(Element.ELEMENT,itemNode);
					}
				}
			}
		}

		return fromDoc2String(doc);
	}*/

	public static FormDef fromXform2FormDef(Reader reader){
		Document doc = getDocument(reader);
		return getFormDef(doc);
	}

	public static Document getDocument(Reader reader){
		Document doc = new Document();

		try{
			KXmlParser parser = new KXmlParser();
			parser.setInput(reader);
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

			doc.parse(parser);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		return doc;
	}

	public static FormDef getFormDef(Document doc){
		initIds();

		Element rootNode = doc.getRootElement();
		Element instanceNode = getInstanceDataNode(doc);
		FormDef formDef = new FormDef();
		Hashtable id2VarNameMap = new Hashtable();
		Hashtable relevants = new Hashtable();
		Hashtable constraints = new Hashtable();
		Hashtable actions = new Hashtable();
		Hashtable constraintMsgs = new Hashtable();
		Vector repeats = new Vector();
		Hashtable rptKidMap = new Hashtable();
		parseElement(formDef,rootNode,id2VarNameMap,null,relevants,actions,constraints,constraintMsgs,repeats,rptKidMap,(int)0,null,instanceNode);
		if(formDef.getName() == null || formDef.getName().length() == 0)
			formDef.setName(formDef.getVariableName());
		setDefaultValues(instanceNode,formDef,id2VarNameMap);
		addSkipRules(formDef,id2VarNameMap,relevants,actions);
		addValidationRules(formDef,id2VarNameMap,constraints,constraintMsgs);
		return formDef;
	}

	private static String getNodeTextValue(Element dataNode,String name){
		Element node = getNode(dataNode,name);
		if(node != null)
			return getTextValue(node);
		return null;
	}

	private static void setDefaultValues(Element dataNode,FormDef formDef,Hashtable id2VarNameMap){
		/*String id, val;
		Enumeration keys = id2VarNameMap.keys();
		while(keys.hasMoreElements()){
			id = (String)keys.nextElement();
			val = getNodeTextValue(dataNode,id);
			if(val == null || val.trim().length() == 0) //we are not allowing empty strings for now.
				continue;
			QuestionDef def = formDef.getQuestion((String)id2VarNameMap.get(id));
			if(def != null)
				def.setDefaultValue(val);
		}*/

		String id, val;
		Iterator keys = id2VarNameMap.keySet().iterator();
		while(keys.hasNext()){
			id = (String)keys.next();
			String variableName = (String)id2VarNameMap.get(id);

			QuestionDef def = formDef.getQuestion(variableName);
			if(def == null)
				continue;

			if(variableName.contains("@"))
				setAttributeDefaultValue(def,variableName,dataNode,formDef);
			else{
				val = getNodeTextValue(dataNode,id);
				if(val == null || val.trim().length() == 0) //we are not allowing empty strings for now.
					continue;

				def.setDefaultValue(val);
			}
		}

		//Now do set default values for repeats since they are not part of the id2VarNameMap
		for(int pageNo=0; pageNo<formDef.getPageCount(); pageNo++){
			PageDef pageDef = formDef.getPageAt(pageNo);
			Vector questions = pageDef.getQuestions();
			if(questions != null){
				for(int qtnNo=0; qtnNo<questions.size(); qtnNo++){
					QuestionDef questionDef = (QuestionDef)questions.elementAt(qtnNo);
					if(questionDef.getType() == QuestionDef.QTN_TYPE_REPEAT)
						;//setQtnsDefaultValues(questionDef.getDataNode(),formDef,questionDef.getRepeatQtnsDef());
				}
			}
		}
	}

	private static void setAttributeDefaultValue(QuestionDef qtn, String variableName,Element dataNode,FormDef formDef){
		String xpath = variableName;

		if(xpath.startsWith(formDef.getVariableName() + "/"))
			xpath = xpath.replace(formDef.getVariableName() + "/", "");
		else if(xpath.startsWith("/"+formDef.getVariableName() + "/"))
			xpath = xpath.replace("/"+formDef.getVariableName() + "/", "");
		else if(xpath.startsWith("/"))
			xpath = new String(xpath.toCharArray(), 1, xpath.length()-1);

		int pos = xpath.lastIndexOf('@'); String attributeName = null;
		if(pos == 0){
			attributeName = variableName.substring(1,variableName.length());
			String value = dataNode.getAttributeValue(null,attributeName);
			if(value != null && value.trim().length() > 0) //we are not allowing empty strings for now.
				qtn.setDefaultValue(value);
			return;
		}

		attributeName = xpath.substring(pos+1,xpath.length());
		xpath = xpath.substring(0,pos-1);

		XPathExpression xpls = new XPathExpression(dataNode, xpath);
		Vector result = xpls.getResult();

		for (Enumeration e = result.elements(); e.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof Element){
				String value = ((Element) obj).getAttributeValue(null,attributeName);
				if(value != null && value.trim().length() > 0){ //we are not allowing empty strings for now.
					qtn.setDefaultValue(value);
					break;
				}
			}
		}
	}

	private static String getTextValue(Element node){
		int numOfEntries = node.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (node.isText(i))
				return node.getText(i);

			if(node.getType(i) == Element.ELEMENT){
				String val = getTextValue(node.getElement(i));
				if(val != null)
					return val;
			}
		}

		return null;
	}

	/**
	 * Gets a child element of a parent node with a given name.
	 * 
	 * @param parent - the parent element
	 * @param name - the name of the child.
	 * @return - the child element.
	 */
	private static Element getNode(Element parent, String name){
		for(int i=0; i<parent.getChildCount(); i++){
			if(parent.getType(i) != Element.ELEMENT)
				continue;

			Element child = (Element)parent.getChild(i);
			if(child.getName().equals(name))
				return child;

			child = getNode(child,name);
			if(child != null)
				return child;
		}

		return null;
	}

	public static FormData fromXform2FormData(Reader reader){			
		Document doc = getDocument(reader);		
		FormData formData = new FormData(getFormDef(doc));

		Element dataNode = doc.getRootElement().getElement(0).getElement(1).getElement(0).getElement(0);
		for(int i=0; i<dataNode.getChildCount(); i++){
			Element node = dataNode.getElement(i);
			if(node != null && node.getChildCount() > 0)
				formData.setValue(node.getName(), node.getText(0));
		}

		return formData;
	}

	private static QuestionDef parseElement(FormDef formDef, Element element, Hashtable map,QuestionDef questionDef,Hashtable relevants,Hashtable actions,Hashtable constraints,Hashtable constraintMsgs,Vector repeats, Hashtable rptKidMap, int currentPageNo,QuestionDef parentQtn, Element instanceDataNode){
		String label = ""; //$NON-NLS-1$
		String value = ""; //$NON-NLS-1$

		int numOfEntries = element.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (!element.isText(i) && element.getType(i) == Element.ELEMENT) {
				Element child = element.getElement(i);
				String tagname = child.getName();

				if(tagname.equals("submit"))
					continue;
				else if (tagname.equals("head"))
					parseElement(formDef,child,map,questionDef,relevants,actions,constraints,constraintMsgs,repeats,rptKidMap,currentPageNo,parentQtn,instanceDataNode);
				else if (tagname.equals("body"))
					parseElement(formDef, child,map,questionDef,relevants,actions,constraints,constraintMsgs,repeats,rptKidMap,currentPageNo,parentQtn,instanceDataNode);
				else if (tagname.equals("title")){
					if(child.getChildCount() > 0 && child.isText(0))
						formDef.setName(child.getText(0).trim());
				}
				else if (tagname.equals("model"))
					parseElement(formDef, child,map,questionDef,relevants,actions,constraints,constraintMsgs,repeats,rptKidMap,currentPageNo,parentQtn,instanceDataNode);
				else if (tagname.equals("group")){
					String parentName = ((Element)child.getParent()).getName();
					if(!(parentName.equalsIgnoreCase("group"))){
						if(formDef.getPageCount() < ++currentPageNo)
							formDef.addPage();
					}
					parseElement(formDef, child,map,questionDef,relevants,actions,constraints,constraintMsgs,repeats,rptKidMap,currentPageNo,parentQtn,instanceDataNode);
				}
				else if (tagname.equals("instance")) {
					if(formDef.getVariableName() != null && formDef.getVariableName().trim().length() > 0)
						continue; //we only take the first instance node for formdef ref

					Element dataNode = null;
					for(int k=0; k<child.getChildCount(); k++){
						if(!child.isText(k))
							dataNode = (Element)child.getChild(k);
					}
					formDef.setVariableName(dataNode.getName());
					if(dataNode.getAttributeValue(null, "formId") != null)
						formDef.setId(Integer.parseInt(dataNode.getAttributeValue(null, "formId")));
					if(dataNode.getAttributeValue(null, "description-template") != null)
						formDef.setDescriptionTemplate(dataNode.getAttributeValue(null, "description-template"));
					if(dataNode.getAttributeValue(null, "id") != null)
						formDef.setId(Integer.parseInt(dataNode.getAttributeValue(null, "id")));
					if(dataNode.getAttributeValue(null, "name") != null)
						formDef.setName(dataNode.getAttributeValue(null, "name"));
					if(dataNode.getAttributeValue(null, "formKey") != null)
						formDef.setFormKey(dataNode.getAttributeValue(null, "formKey"));
				} else if (tagname.equals("bind") || tagname.equals("ref")) {
					QuestionDef qtn = new QuestionDef();
					//qtn.setId(Byte.parseByte(String.valueOf(i)));
					if(formDef.getPages() == null)
						qtn.setId(getNextQuestionId());
					else{
						if(reachedMaxQuestionId()){
							System.out.println("Failed parsing Xform because it exceeds the currently supported maximum number of questions. Count=255");
							break;
						}
						qtn.setId(getNextQuestionId());
					}
					qtn.setVariableName(child.getAttributeValue(null, "nodeset"));
					setQuestionType(qtn,child.getAttributeValue(null, "type"),child);
					if(child.getAttributeValue(null, "required") != null && child.getAttributeValue(null, "required").equals("true()"))
						qtn.setMandatory(true);
					if(child.getAttributeValue(null, "readonly") != null && child.getAttributeValue(null, "readonly").equals("true()"))
						qtn.setEnabled(false);
					if(child.getAttributeValue(null, "locked") != null && child.getAttributeValue(null, "locked").equals("true()"))
						qtn.setLocked(true);
					if(child.getAttributeValue(null, "visible") != null && child.getAttributeValue(null, "visible").equals("false()"))
						qtn.setVisible(false);


					if(!addRepeatChildQtn(qtn,repeats,child,map,rptKidMap)){
						if(child.getAttributeValue(null, "id") != null){
							map.put(child.getAttributeValue(null, "id"), qtn.getVariableName());
							formDef.addQuestion(qtn);
						}
						else
							System.out.println("Problem trying to get a missing id attribute"+qtn);
					}

					if(child.getAttributeValue(null, "relevant") != null){
						relevants.put(qtn,child.getAttributeValue(null, "relevant"));
						String action  = child.getAttributeValue(null, "action");
						if(action != null){
							String required = child.getAttributeValue(null, "required");
							if(required != null)
								action += "|" + required;
						}
						actions.put(qtn,action);
					}

					if(child.getAttributeValue(null,"constraint") != null){
						constraints.put(qtn,child.getAttributeValue(null,"constraint"));
						constraintMsgs.put(qtn,child.getAttributeValue(null, "message"));
					}

					if(qtn.getType() == QuestionDef.QTN_TYPE_REPEAT){
						RepeatQtnsDef repeatQtnsDef = new RepeatQtnsDef(qtn);
						qtn.setRepeatQtnsDef(repeatQtnsDef);
						repeats.addElement(qtn);

						questionDef = qtn;
					}

				} else if (tagname.equals("input") || tagname.equals("select1") || tagname.equals("select") || tagname.equals("repeat")) {
					String ref = child.getAttributeValue(null, "ref");
					String bind = child.getAttributeValue(null, "bind");
					String varName = (String)map.get(((ref != null) ? ref : bind));

					//new addition may cause bugs
					if(varName == null){
						varName = addNonBindControl(formDef,child,relevants,ref,bind);
						if(ref != null)
							map.put(ref, ref);
					}

					if(reachedMaxQuestionId())
						break;

					if(varName != null){
						QuestionDef qtn = formDef.getQuestion(varName);
						if(qtn == null)
							qtn = (QuestionDef)rptKidMap.get(varName);

						if(tagname.equals("select1") || tagname.equals("select")){
							qtn.setType((tagname.equals("select1")) ? QuestionDef.QTN_TYPE_LIST_EXCLUSIVE : QuestionDef.QTN_TYPE_LIST_MULTIPLE);
							qtn.setOptions(new Vector());
						}
						else if((tagname.equals("repeat")) && !label.equals("")){
							qtn.setType(QuestionDef.QTN_TYPE_REPEAT);
							qtn.setText(label);
							qtn.setRepeatQtnsDef(new RepeatQtnsDef(qtn));
							formDef.moveQuestion2Page(qtn, currentPageNo);
							label = "";
							int pageNo = currentPageNo;
							if(pageNo == 0) pageNo = 1; //Xform may not have groups for pages.
							parentQtn = qtn;
						}

						//TODO second addition for repeats
						Element parent = (Element)child.getParent(); 
						if(parent.getName().equals("repeat")){
							QuestionDef rptQtnDef = formDef.getQuestion((String)map.get(parent.getAttributeValue(null,"bind") != null ? parent.getAttributeValue(null,"bind") : parent.getAttributeValue(null,"nodeset")));
							rptQtnDef.addRepeatQtnsDef(qtn);
							formDef.removeQuestion(qtn);

							String val = getNodeTextValue(instanceDataNode,qtn.getVariableName());
							if(!(val == null || val.trim().length() == 0))
								qtn.setDefaultValue(val);
						}

						questionDef = qtn;
						parseElement(formDef, child, map,questionDef,relevants,actions,constraints,constraintMsgs,repeats,rptKidMap,currentPageNo,parentQtn,instanceDataNode);
					}
				} else if (tagname.equals("label")){
					String parentName = ((Element)child.getParent()).getName();
					if(parentName.equalsIgnoreCase("input") || parentName.equalsIgnoreCase("select") || parentName.equalsIgnoreCase("select1") || parentName.equalsIgnoreCase("item")){
						if(child.getChildCount() > 0 && child.isText(0))
							label = child.getText(0).trim(); //questionDef.setText(child.getText(0).trim());
						else
							label = "";
					}
					else if(parentName.equalsIgnoreCase("repeat")){
						if(questionDef != null && child.getChildCount() > 0 && child.isText(0))
							questionDef.setText(child.getText(0).trim());
					}
					else if(parentName.equalsIgnoreCase("group")){
						if(child.getChildCount() > 0 && child.isText(0))
							label = child.getText(0).trim();
						else
							label = "";
					}
				}
				else if (tagname.equals("hint")){
					if(questionDef != null && child.getChildCount() > 0 && child.isText(0))
						questionDef.setHelpText(child.getText(0).trim());
				}
				else if (tagname.equals("item"))
					parseElement(formDef, child,map,questionDef,relevants,actions,constraints,constraintMsgs,repeats,rptKidMap,currentPageNo,parentQtn,instanceDataNode);
				else if (tagname.equals("value")){
					if(child.getChildCount() > 0 && child.isText(0))
						value = child.getText(0).trim();
					else
						value = "";
				}
				else if(tagname.equals("itemset")){
					questionDef.setType(QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC);
					parseDynamicOptionsList(questionDef,child.getAttributeValue(null,"nodeset"),formDef,(Element)((Element)instanceDataNode.getParent()).getParent());
				}
				else
					parseElement(formDef, child,map,questionDef,relevants,actions,constraints,constraintMsgs,repeats,rptKidMap,currentPageNo,parentQtn,instanceDataNode);
				// TODO - how are other elements like html:p or br handled?
			}
		}

		if (!label.equals("") && !value.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (questionDef != null && questionDef.getOptions() != null)
				questionDef.addOption(new OptionDef(Byte.parseByte(String.valueOf(questionDef.getOptions().size())),label, value));
		} 
		else if (!label.equals("") && questionDef != null){
			if(questionDef.getText() == null || questionDef.getText().trim().length()==0){
				questionDef.setText(label);
				int pageNo = currentPageNo;
				if(pageNo == 0) pageNo = 1; //Xform may not have groups for pages.
				formDef.moveQuestion2Page(questionDef, pageNo);
			}
			else
				formDef.setPageName(label);
		}

		return questionDef;
	}

	private static void parseDynamicOptionsList(QuestionDef questionDef, String nodeset, FormDef formDef, Element modelNode){
		if(nodeset == null || nodeset.trim().length() == 0)
			return;

		String binding = getDynamicOptionParentInstanceId(nodeset);
		if(binding == null)
			return;

		binding = "/" + formDef.getVariableName() + "/" + binding;
		QuestionDef parentQuestionDef = formDef.getQuestion(binding);
		if(parentQuestionDef == null)
			return;

		String instanceId = getDynamicOptionChildInstanceId(nodeset);
		if(instanceId == null)
			return;

		Element instanceNode = getInstanceNode(modelNode, instanceId);
		if(instanceNode == null)
			return;

		Hashtable parentOptionIdMap = new Hashtable();
		DynamicOptionDef dynamicOptionDef = new DynamicOptionDef();
		dynamicOptionDef.setQuestionId(questionDef.getId());
		int count = instanceNode.getChildCount();
		for(int index = 0; index < count; index++){
			if(instanceNode.getType(index) == Element.ELEMENT){
				Element child = (Element)instanceNode.getChild(index);
				parseDynamicOptions(dynamicOptionDef,questionDef,parentQuestionDef,child,null,parentOptionIdMap,formDef);
				break;
			}
		}

		setDynamicOptionDef(formDef,parentQuestionDef.getId(), dynamicOptionDef);
	}

	private static void setDynamicOptionDef(FormDef formDef, Integer questionId, DynamicOptionDef dynamicOptionDef){
		if(formDef.getDynamicOptions() == null)
			formDef.setDynamicOptions(new Hashtable());
		formDef.getDynamicOptions().put(questionId, dynamicOptionDef);
	}

	private static void parseDynamicOptions(DynamicOptionDef dynamicOptionDef, QuestionDef questionDef, QuestionDef parentQuestionDef, Node node, OptionDef optionDef, Hashtable parentOptionIdMap, FormDef formDef){
		String label = "";
		String value = "";

		int count = node.getChildCount();
		for(int index = 0; index < count; index++){
			if(node.getType(index) != Node.ELEMENT)
				continue;

			Element child = (Element)node.getChild(index);
			String name = child.getName();
			if(name.equals("item")){
				String parent = ((Element)child).getAttributeValue(null,"parent");
				if(parent == null || parent.trim().length() == 0)
					continue;

				optionDef = new OptionDef(getNextOptionId(),label, value);
				Byte optionId = (Byte)parentOptionIdMap.get(parent);
				if(optionId == null){
					OptionDef optnDef = parentQuestionDef.getOptionWithValue(parent);
					if(parentQuestionDef.getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC){
						DynamicOptionDef dynOptionsDef = getChildDynamicOptions(formDef,parentQuestionDef.getId());
						if(dynOptionsDef == null)
							return;
						optnDef = getOptionWithValue(dynOptionsDef,parent);
					}
					if(optnDef == null)
						continue;
					optionId = new Byte(optnDef.getId());
					parentOptionIdMap.put(parent, optionId);
				}
				Vector optionList = getOptionList(dynamicOptionDef,optionId);
				if(optionList == null){
					optionList = new Vector();
					setOptionList(dynamicOptionDef,optionId, optionList);
				}
				optionList.add(optionDef);

				parseDynamicOptions(dynamicOptionDef,questionDef,parentQuestionDef,child,optionDef,parentOptionIdMap,formDef);
			}
			else if(name.equals("label")){
				if(child.getChildCount() > 0 && child.isText(0))
					label = child.getText(0).trim(); //questionDef.setText(child.getChildNodes().item(0).getNodeValue().trim());
			}
			else if(name.equals("value")){
				if(child.getChildCount() > 0 && child.isText(0))
					value = child.getText(0).trim();
			}
		}

		if (!label.equals("") && !value.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (optionDef != null){
				optionDef.setText(label);
				optionDef.setVariableName(value);
			}
		} 
	}

	private static void setOptionList(DynamicOptionDef dynamicOptionDef,Byte optionId, Vector list){
		if(dynamicOptionDef.getParentToChildOptions() == null)
			dynamicOptionDef.setParentToChildOptions(new Hashtable());
		dynamicOptionDef.getParentToChildOptions().put(optionId, list);
	}

	private static OptionDef getOptionWithValue(DynamicOptionDef dynamicOptionDef, String value){
		if(dynamicOptionDef.getParentToChildOptions() == null || value == null)
			return null;

		Iterator iterator = dynamicOptionDef.getParentToChildOptions().entrySet().iterator();
		while(iterator.hasNext()){
			OptionDef optionDef = getOptionWithValue((Vector)((Entry)iterator.next()).getValue(),value);
			if(optionDef != null)
				return optionDef;
		}
		return null;
	}

	private static OptionDef getOptionWithValue(Vector options, String value){
		for(int i=0; i<options.size(); i++){
			OptionDef optionDef = (OptionDef)options.get(i);
			if(optionDef.getVariableName().equals(value))
				return optionDef;
		}
		return null;
	}

	private static DynamicOptionDef getChildDynamicOptions(FormDef formDef, byte questionId){
		if(formDef.getDynamicOptions() == null)
			return null;

		Iterator iterator = formDef.getDynamicOptions().entrySet().iterator();
		while(iterator.hasNext()){
			Entry entry = (Entry)iterator.next();
			DynamicOptionDef dynamicOptionDef = (DynamicOptionDef)entry.getValue();
			if(dynamicOptionDef.getQuestionId() == questionId)
				return dynamicOptionDef;
		}
		return null;
	}

	private static Vector getOptionList(DynamicOptionDef dynamicOptionDef, Byte optionId){
		if(dynamicOptionDef.getParentToChildOptions() == null)
			return null;
		return (Vector)dynamicOptionDef.getParentToChildOptions().get(optionId);
	}

	private static void setDynamicOptionDef(FormDef formDef, byte questionId, DynamicOptionDef dynamicOptionDef){
		if(formDef.getDynamicOptions() == null)
			formDef.setDynamicOptions(new Hashtable());
		formDef.getDynamicOptions().put(new Byte(questionId), dynamicOptionDef);
	}

	private static Element getInstanceNode(Element modelNode, String instanceId){
		String xpath = "instance[@id=" + instanceId + "]";
		XPathExpression xpls = new XPathExpression(modelNode, xpath);
		Vector result = xpls.getResult();
		if(result.size() > 0)
			return (Element)result.get(0);
		return null;
	}

	private static String getDynamicOptionChildInstanceId(String nodeset){
		if(nodeset == null)
			return null;

		int pos1 = nodeset.indexOf('\'');
		if(pos1 < 0)
			return null;

		int pos2 = nodeset.indexOf('\'', pos1 + 1);
		if(pos2 < 0 || (pos1 == pos2))
			return null;

		return nodeset.substring(pos1 + 1, pos2);
	}

	private static String getDynamicOptionParentInstanceId(String nodeset){
		if(nodeset == null)
			return null;

		int pos1 = nodeset.lastIndexOf('/');
		if(pos1 < 0)
			return null;

		int pos2 = nodeset.lastIndexOf(']');
		if(pos2 < 0 || (pos1 == pos2))
			return null;

		return nodeset.substring(pos1 + 1, pos2);
	}

	private static String getDynamicOptionFormInstanceId(String nodeset){
		if(nodeset == null)
			return null;

		String token = "\')/item[@parent=instance(\'";
		int pos1 = nodeset.indexOf(token);
		if(pos1 < 0)
			return null;

		int pos2 = nodeset.indexOf("\')/", pos1 + token.length());
		if(pos2 < 0 || (pos1 == pos2))
			return null;

		return nodeset.substring(pos1+token.length(), pos2);
	}

	/**
	 * Checks if this is a repeat child question and adds it.
	 * @param qtn the questions to check
	 * @param repeats the list of repeat questions
	 * @return true if so, else false.
	 */
	private static boolean addRepeatChildQtn(QuestionDef qtn, Vector repeats,Element child,Hashtable map,Hashtable rptKidmap){
		for(int i=0; i<repeats.size(); i++){
			QuestionDef rptQtn = (QuestionDef)repeats.get(i);
			if(qtn.getVariableName().contains(rptQtn.getVariableName())){
				RepeatQtnsDef rptQtnsDef = rptQtn.getRepeatQtnsDef();
				//rptQtnsDef.addQuestion(qtn); //TODO This is temporarily removed to solve the wiered problem list bug
				String varname = qtn.getVariableName().substring(rptQtn.getVariableName().length()+1);
				//varname = varname.substring(0, varname.indexOf('/'));
				//map.put(child.getAttributeValue(null, "id"), varname);
				map.put(varname, varname);
				rptKidmap.put(varname, qtn);
				return true;		
			}
		}
		return false;
	}

	/*private static boolean isAllowedNodeName(String name){
		return name.equalsIgnoreCase("bind") || name.equalsIgnoreCase("input") ||
			   name.equalsIgnoreCase("label") || name.equalsIgnoreCase("select1") ||
			   name.equalsIgnoreCase("item") || name.equalsIgnoreCase("hint") ||
			   name.equalsIgnoreCase("repeat") || name.equalsIgnoreCase("select") ||
			   name.equalsIgnoreCase("model");
	}*/

	//secret,textarea, submit, trigger, upload, range
	/*<range ref="length" start="0" end="100" step="5">
	  <label>Length:</label>
	</range>*/
	private static void setQuestionType(QuestionDef def, String type, Element node){
		if(type != null){
			if(type.equals("xsd:string") || type.indexOf("string") != -1 ){
				String format = node.getAttributeValue(null,"format");
				if("gps".equals(format))
					def.setType(QuestionDef.QTN_TYPE_GPS);
				else
					def.setType(QuestionDef.QTN_TYPE_TEXT);
			}
			else if((type.equals("xsd:integer") || type.equals("xsd:int")) || (type.indexOf("integer") != -1 || type.indexOf("int") != -1))
				def.setType(QuestionDef.QTN_TYPE_NUMERIC);
			else if(type.equals("xsd:decimal") || type.indexOf("decimal") != -1 )
				def.setType(QuestionDef.QTN_TYPE_DECIMAL);
			else if(type.equals("xsd:dateTime") || type.indexOf("dateTime") != -1 )
				def.setType(QuestionDef.QTN_TYPE_DATE_TIME);
			else if(type.equals("xsd:time") || type.indexOf("time") != -1 )
				def.setType(QuestionDef.QTN_TYPE_TIME);
			else if(type.equals("xsd:date") || type.indexOf("date") != -1 )
				def.setType(QuestionDef.QTN_TYPE_DATE);
			else if(type.equals("xsd:boolean") || type.indexOf("boolean") != -1 )
				def.setType(QuestionDef.QTN_TYPE_BOOLEAN);
			else if(type.equals("xsd:base64Binary") || type.indexOf("base64Binary") != -1 ){
				String format = node.getAttributeValue(null,"format");
				if("video".equals(format))
					def.setType(QuestionDef.QTN_TYPE_VIDEO);
				else if("audio".equals(format))
					def.setType(QuestionDef.QTN_TYPE_AUDIO);
				else
					def.setType(QuestionDef.QTN_TYPE_IMAGE);
			}
		}
		else
			def.setType(QuestionDef.QTN_TYPE_REPEAT);
	}
	/*public StudyDef fromXform2StudyDef(String xform){
		return null;
	}

	public String fromStudyDef2Xform(StudyDef def){
		return null;
	}*/

	private static byte getAction(Object actn){
		if(actn == null)
			return EpihandyConstants.ACTION_DISABLE;

		String value = actn.toString();

		String required = null;
		int pos = value.indexOf('|');
		if(pos > 0){
			required = value.substring(pos+1, value.length());
			value = value.substring(0,pos);
		}

		byte action = 0;
		if(value.equalsIgnoreCase(ATTRIBUTE_VALUE_ENABLE))
			action |= EpihandyConstants.ACTION_ENABLE;
		else if(value.equalsIgnoreCase(ATTRIBUTE_VALUE_DISABLE))
			action |= EpihandyConstants.ACTION_DISABLE;
		else if(value.equalsIgnoreCase(ATTRIBUTE_VALUE_SHOW))
			action |= EpihandyConstants.ACTION_SHOW;
		else if(value.equalsIgnoreCase(ATTRIBUTE_VALUE_HIDE))
			action |= EpihandyConstants.ACTION_HIDE;

		if(required != null && required.equalsIgnoreCase("true()"))
			action |= EpihandyConstants.ACTION_MAKE_MANDATORY;
		else 
			action |= EpihandyConstants.ACTION_MAKE_OPTIONAL;

		return action;

		//return EpihandyConstants.ACTION_SHOW;
	}

	private static void addSkipRules(FormDef formDef, Hashtable map, Hashtable relevants, Hashtable actions){
		Vector rules = new Vector();

		Enumeration keys = relevants.keys();
		byte id = 0;
		while(keys.hasMoreElements()){
			QuestionDef qtn = (QuestionDef)keys.nextElement();
			SkipRule skipRule = buildSkipRule(formDef, qtn.getId(),(String)relevants.get(qtn),++id,getAction(actions.get(qtn)));
			if(skipRule != null)
				rules.add(skipRule);
		}

		formDef.setSkipRules(rules);
	}

	private static void addValidationRules(FormDef formDef, Hashtable map, Hashtable constraints, Hashtable constraintMsgs){
		Vector rules = new Vector();

		Iterator keys = constraints.keySet().iterator();
		int id = 0;
		while(keys.hasNext()){
			QuestionDef qtn = (QuestionDef)keys.next();
			ValidationRule validationRule = buildValidationRule(formDef, qtn.getId(),(String)constraints.get(qtn),(String)constraintMsgs.get(qtn));
			if(validationRule != null)
				rules.add(validationRule);
		}

		formDef.setValidationRules(rules);
	}

	private static SkipRule buildSkipRule(FormDef formDef, byte questionId, String relevant, byte id, byte action){

		SkipRule skipRule = new SkipRule();
		skipRule.setId(id);
		//TODO For now we are only dealing with hiding and showing.
		skipRule.setAction(action); //TODO This should depend on whats in the xforms file
		skipRule.setConditions(getSkipRuleConditions(formDef,relevant,skipRule.getAction()));
		skipRule.setConditionsOperator(getConditionsOperator(relevant));

		Vector actionTargets = new Vector();
		actionTargets.add(new Byte(questionId));
		skipRule.setActionTargets(actionTargets);
		//skipRule.setName(name);

		if(skipRule.getConditions() == null || skipRule.getConditions().size() == 0)
			return null;
		return skipRule;
	}

	private static ValidationRule buildValidationRule(FormDef formDef, byte questionId, String constraint, String constraintMsg){

		ValidationRule validationRule = new ValidationRule(questionId,null,null);
		validationRule.setConditions(getValidationRuleConditions(formDef,constraint,questionId));
		validationRule.setConditionsOperator(getConditionsOperator(constraint));

		if(constraintMsg == null)
			validationRule.setErrorMessage("");
		else
			validationRule.setErrorMessage(constraintMsg);

		if(validationRule.getConditions() == null || validationRule.getConditions().size() == 0)
			return null;
		return validationRule;
	}

	private static Vector getSkipRuleConditions(FormDef formDef, String relevant, byte action){
		Vector conditions = new Vector();

		Vector list = getConditionsOperatorTokens(relevant);

		Condition condition  = new Condition();
		for(byte i=0; i<list.size(); i++){
			condition = getSkipRuleCondition(formDef,(String)list.elementAt(i),(byte)(i+1),action);
			if(condition != null)
				conditions.add(condition);
		}

		return conditions;
	}

	private static Vector getValidationRuleConditions(FormDef formDef, String constraint, byte questionId){
		Vector conditions = new Vector();

		Vector list = getConditionsOperatorTokens(constraint);

		Condition condition  = new Condition();
		for(int i=0; i<list.size(); i++){
			condition = getValidationRuleCondition(formDef,(String)list.elementAt(i),questionId);
			if(condition != null)
				conditions.add(condition);
		}

		return conditions;
	}

	private static Condition getSkipRuleCondition(FormDef formDef, String relevant, byte id, byte action){		
		/*Condition condition  = new Condition();
		condition.setId(id);
		condition.setOperator(getOperator(relevant,action));

		//eg relevant="/data/question10='7'"
		int pos = getOperatorPos(relevant);
		String varName = relevant.substring(0, pos);
		condition.setQuestionId(formDef.getQuestionId(varName.trim()));

		String value;
		//first try a value delimited by '
		int pos1 = relevant.lastIndexOf('\'');
		if(pos1 > 0){
			pos1++;
			int pos2 = relevant.lastIndexOf('\'',pos1);
			if(pos2 < 0){
				System.out.println("Relevant value not closed with ' characher");
				return null;
			}
			value = relevant.substring(pos1,pos2);
		}
		else //else we take whole value after operator	
			value = relevant.substring(pos+1,relevant.length());

		condition.setValue(value.trim());

		if(condition.getOperator() == EpihandyConstants.OPERATOR_NULL)
			return null; //no operator set hence making the condition invalid
		return condition;*/

		final byte OPERATOR_NULL = 0;
		final byte OPERATOR_IS_NULL = 9;

		Condition condition  = new Condition();
		condition.setId(id);
		condition.setOperator(getOperator(relevant,action));

		//eg relevant="/data/question10='7'"
		int pos = getOperatorPos(relevant);
		if(pos < 0)
			return null;

		String varName = relevant.substring(0, pos);
		QuestionDef questionDef = formDef.getQuestion(varName.trim());
		if(questionDef == null){
			String prefix = "/" + formDef.getVariableName() + "/";
			if(varName.startsWith(prefix))
				questionDef = formDef.getQuestion(varName.trim().substring(prefix.length(), varName.trim().length()));
			if(questionDef == null)
				return null;
		}
		condition.setQuestionId(questionDef.getId());

		String value;
		//first try a value delimited by '
		int pos2 = relevant.lastIndexOf('\'');
		if(pos2 > 0){
			//pos1++;
			int pos1 = relevant.substring(0, pos2).lastIndexOf('\'',pos2);
			if(pos1 < 0){
				System.out.println("Relevant value not closed with ' characher");
				return null;
			}
			pos1++;
			value = relevant.substring(pos1,pos2);
		}
		else //else we take whole value after operator	
			value = relevant.substring(pos+getOperatorSize(condition.getOperator(),action),relevant.length());

		if(!(value.equals("null") || value.equals(""))){
			condition.setValue(value.trim());

			if(condition.getOperator() == OPERATOR_NULL)
				return null; //no operator set hence making the condition invalid
		}
		else
			condition.setOperator(OPERATOR_IS_NULL);

		return condition;
	}

	private static Condition getValidationRuleCondition(FormDef formDef, String constraint, byte questionId){		
		Condition condition  = new Condition();
		condition.setId(questionId);
		condition.setOperator(getOperator(constraint,EpihandyConstants.ACTION_ENABLE));
		condition.setQuestionId(questionId);

		//eg . &lt;= 40"
		int pos = getOperatorPos(constraint);
		if(pos < 0)
			return null;

		QuestionDef questionDef = formDef.getQuestion(questionId);
		if(questionDef == null)
			return null;

		String value;
		//first try a value delimited by '
		int pos2 = constraint.lastIndexOf('\'');
		if(pos2 > 0){
			//pos1++;
			int pos1 = constraint.substring(0, pos2).lastIndexOf('\'',pos2);
			if(pos1 < 0){
				System.out.println("constraint value not closed with ' characher");
				return null;
			}
			pos1++;
			value = constraint.substring(pos1,pos2);
		}
		else //else we take whole value after operator	
			value = constraint.substring(pos+getOperatorSize(condition.getOperator(),EpihandyConstants.ACTION_ENABLE),constraint.length());

		if(!(value.equals("null") || value.equals(""))){
			condition.setValue(value.trim());

			if(condition.getOperator() == EpihandyConstants.OPERATOR_NULL)
				return null; //no operator set hence making the condition invalid
		}
		else
			condition.setOperator(EpihandyConstants.OPERATOR_IS_NULL);

		if(constraint.contains("length(.)") || constraint.contains("count(.)"))
			condition.setFunction(EpihandyConstants.FUNCTION_LENGTH);

		return condition;
	}


	private static boolean isPositiveAction(int action){
		return ((action & EpihandyConstants.ACTION_ENABLE) != 0) || ((action & EpihandyConstants.ACTION_SHOW) != 0);
	}

	//TODO Add the other xpath operators
	private static byte getOperator(String relevant, int action){
		/*if(relevant.indexOf('=') > 0)
			return EpihandyConstants.OPERATOR_EQUAL;
		else if(relevant.indexOf('>') > 0)
			return EpihandyConstants.OPERATOR_GREATER;
		else if(relevant.indexOf(">=") > 0)
			return EpihandyConstants.OPERATOR_GREATER_EQUAL;
		else if(relevant.indexOf('<') > 0)
			return EpihandyConstants.OPERATOR_LESS;
		else if(relevant.indexOf("<=") > 0)
			return EpihandyConstants.OPERATOR_LESS_EQUAL;
		else if(relevant.indexOf("<>") > 0 || relevant.indexOf("!=") > 0)
			return EpihandyConstants.OPERATOR_NOT_EQUAL;*/

		if(relevant.indexOf(">=") > 0 || relevant.indexOf("&gt;=") > 0){
			if(isPositiveAction(action))
				return EpihandyConstants.OPERATOR_GREATER_EQUAL;
			return EpihandyConstants.OPERATOR_LESS;
		}
		else if(relevant.indexOf('>') > 0 || relevant.indexOf("&gt;") > 0){
			if(isPositiveAction(action))
				return EpihandyConstants.OPERATOR_GREATER;
			return EpihandyConstants.OPERATOR_LESS_EQUAL;
		}
		else if(relevant.indexOf("<=") > 0 || relevant.indexOf("&lt;=") > 0){
			if(isPositiveAction(action))
				return EpihandyConstants.OPERATOR_LESS_EQUAL;
			return EpihandyConstants.OPERATOR_GREATER;
		}
		else if(relevant.indexOf('<') > 0 || relevant.indexOf("&lt;") > 0){
			if(isPositiveAction(action))
				return EpihandyConstants.OPERATOR_LESS;
			return EpihandyConstants.OPERATOR_GREATER_EQUAL;
		}
		else if(relevant.indexOf("!=") > 0 || relevant.indexOf("!=") > 0){
			if(isPositiveAction(action))
				return EpihandyConstants.OPERATOR_NOT_EQUAL;
			return EpihandyConstants.OPERATOR_EQUAL;
		}
		else if(relevant.indexOf('=') > 0){
			if(isPositiveAction(action))
				return EpihandyConstants.OPERATOR_EQUAL;
			return EpihandyConstants.OPERATOR_NOT_EQUAL;
		}

		return EpihandyConstants.OPERATOR_NULL;
	}

	private static int getOperatorSize(byte operator, int action){
		if(operator == EpihandyConstants.OPERATOR_GREATER_EQUAL || 
				operator == EpihandyConstants.OPERATOR_LESS_EQUAL ||
				operator == EpihandyConstants.OPERATOR_NOT_EQUAL)
			return isPositiveAction(action) ? 2 : 1;
		else if(operator == EpihandyConstants.OPERATOR_LESS ||
				operator == EpihandyConstants.OPERATOR_GREATER || 
				operator == EpihandyConstants.OPERATOR_EQUAL)
			return isPositiveAction(action) ? 1 : 2;

		return 0;
	}

	private static int getOperatorPos(String relevant){
		//Using lastindexof because of expressions like:
		//relevant="/ClinicalData/SubjectData/StudyEventData/FormData/ItemGroupData/ItemData[@ItemOID='I_REVI_IMPROVEMENT']/@Value = '1'"

		/*int pos = relevant.lastIndexOf('=');
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf('>');
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf(">=");
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf('<');
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf("<=");
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf("<>");
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf("!=");
		if(pos > 0)
			return pos;

		return pos;*/

		int pos = relevant.lastIndexOf("!=");
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf(">=");
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf("<=");
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf('>');
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf('<');
		if(pos > 0)
			return pos;

		pos = relevant.lastIndexOf('=');
		if(pos > 0)
			return pos;

		return pos;
	}

	private static Vector getConditionsOperatorTokens(String relevant){
		//TODO For now we are only dealing with one AND or OR, for simplicity
		//If one mixes both in the same relevant statement, then we take the first.
		Vector list = new Vector();

		int pos = 0;
		do{
			pos = extractConditionsOperatorTokens(relevant,pos,list);
		}while(pos > 0);

		/*int opSize = 3;

		int pos = relevant.toUpperCase().indexOf("AND");
		if(pos <0){
			pos = relevant.toUpperCase().indexOf("OR");
			opSize = 2;
		}

		if(pos < 0)
			list.add(relevant.trim());
		else{
			list.add(relevant.substring(0,pos).trim());

			int pos2 = relevant.toUpperCase().indexOf("AND",pos+opSize);
			if(pos2 <0)
				pos2 = relevant.toUpperCase().indexOf("OR",pos+opSize);

			if(pos2 < 0)
				list.add(relevant.substring(pos+3, relevant.length()).trim());
			else
				list.add(relevant.substring(pos+3, pos2).trim());
		}*/

		return list;
	}

	private static int extractConditionsOperatorTokens(String relevant,int startPos, Vector list){
		int pos,pos2,opSize = 5;

		pos = relevant.toUpperCase().indexOf(" AND ",startPos);
		if(pos <0){
			pos = relevant.toUpperCase().indexOf(" OR ",startPos);
			opSize = 4;
		}

		//AND may be the last token when we have starting ORs hence skipping them. eg (relevant="/data/question10=7 OR /data/question6=4    OR  /data/question8=1 AND /data/question1='daniel'")
		pos2 = relevant.toUpperCase().indexOf(" OR ",startPos);
		if(pos2 > 0 && pos2 < pos){
			pos = pos2;
			opSize = 4;
		}


		if(pos < 0){
			list.add(relevant.substring(startPos).trim());
			opSize = 0;
		}
		else
			list.add(relevant.substring(startPos,pos).trim());

		return pos+opSize;
	}

	private static byte getConditionsOperator(String relevant){
		if(relevant.toUpperCase().indexOf(" AND ") > 0)
			return EpihandyConstants.CONDITIONS_OPERATOR_AND;
		else if(relevant.toUpperCase().indexOf(" OR ") > 0)
			return EpihandyConstants.CONDITIONS_OPERATOR_OR;
		return EpihandyConstants.CONDITIONS_OPERATOR_NULL;
	}

	private static String addNonBindControl(FormDef formDef,Element child,Hashtable relevants, String ref, String bind){
		QuestionDef qtn = new QuestionDef();
		if(formDef.getPages() == null)
			qtn.setId(getNextQuestionId());
		else{
			if(reachedMaxQuestionId())
				return null;
			qtn.setId(getNextQuestionId());
		}

		if(child.getAttributeValue(null, "type") == null)
			qtn.setType(QuestionDef.QTN_TYPE_TEXT);
		else
			setQuestionType(qtn,child.getAttributeValue(null, "type"),child);

		if(child.getAttributeValue(null, "required") != null && child.getAttributeValue(null, "required").equals("true()"))
			qtn.setMandatory(true);
		if(child.getAttributeValue(null, "readonly") != null && child.getAttributeValue(null, "readonly").equals("true()"))
			qtn.setEnabled(false);
		if(child.getAttributeValue(null, "locked") != null && child.getAttributeValue(null, "locked").equals("true()"))
			qtn.setLocked(true);
		if(child.getAttributeValue(null, "visible") != null && child.getAttributeValue(null, "visible").equals("false()"))
			qtn.setVisible(false);

		qtn.setVariableName(((ref != null) ? ref : bind));
		formDef.addQuestion(qtn);
		if(child.getAttributeValue(null, "relevant") != null)
			relevants.put(qtn,child.getAttributeValue(null, "relevant"));

		return qtn.getVariableName();
	}
}
