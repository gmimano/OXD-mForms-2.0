package org.openxdata.mforms.eval;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.openxdata.mforms.model.FormData;
import org.openxdata.mforms.model.FormDef;
import org.openxdata.mforms.model.PageDef;
import org.openxdata.mforms.model.QuestionDef;
import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Evaluator;
import org.openxdata.rpneval.EvaluatorFactory;
import org.openxdata.util.StringUtils;

public class VarRefTestCase extends TestCase {

	FormData formData;
	Evaluator eval;

	protected void setUp() throws Exception {
		eval = EvaluatorFactory.getInstance();

		// Create sample form definition
		FormDef formDef = new FormDef();
		formDef.addPage();
		PageDef pageDef = (PageDef) formDef.getPages().elementAt(0);

		QuestionDef qDef = new QuestionDef();

		qDef = new QuestionDef();
		qDef.setId((short) 1);
		qDef.setVariableName("/instance/field");
		pageDef.addQuestion(qDef);

		qDef = new QuestionDef();
		qDef.setId((short) 2);
		qDef.setVariableName("/instance/theanswer");
		pageDef.addQuestion(qDef);

		// Create sample form data
		formData = new FormData(formDef);
		formData.setValue("/instance/field", "avalue");
		formData.setValue("/instance/theanswer", new Integer(42));

		// Create an environment containing data->(form data object)
		Hashtable env = new Hashtable();
		env.put("data", formData);
		eval.setEnvironment(env);
	}

	public void testStringRef() throws EvaluationException {
		String[] expression = StringUtils.split("/instance/field $");
		Object result = eval.evaluate(expression);
		assertEquals("avalue", result);
	}

	public void testObjectRef() throws EvaluationException {
		String[] expression = StringUtils.split("/instance/theanswer $");
		Object result = eval.evaluate(expression);
		assertEquals(new Integer(42), result);
	}
}
