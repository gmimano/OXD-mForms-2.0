package org.openxdata.mforms.eval;

import java.util.Hashtable;

import org.openxdata.mforms.model.FormData;
import org.openxdata.mforms.model.FormDef;
import org.openxdata.mforms.model.PageDef;
import org.openxdata.mforms.model.QuestionDef;
import org.openxdata.rpneval.EvaluationException;
import org.openxdata.rpneval.Evaluator;
import org.openxdata.rpneval.EvaluatorFactory;
import org.openxdata.util.StringUtils;

import junit.framework.TestCase;

public class SetCtxOpTest extends TestCase {
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

		// Create sample form data
		formData = new FormData(formDef);
		formData.setValue("/instance/field", "avalue");

		// Create an environment containing data->(form data object)
		Hashtable env = new Hashtable();
		env.put("data", formData);
		eval.setEnvironment(env);
	}

	public void testContextSensitiveRef() throws EvaluationException {
		String[] expression = StringUtils.split("/instance/field sc . $");
		Object result = eval.evaluate(expression);
		assertEquals("avalue", result);
	}
}
