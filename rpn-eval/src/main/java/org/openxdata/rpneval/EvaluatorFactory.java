package org.openxdata.rpneval;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.openxdata.util.Properties;
import org.openxdata.util.StringUtils;

public class EvaluatorFactory {

	public static final String DEFAULT_PROFILE = "default";
	public static final String EVALCLASS_PROP = "implClass";
	public static final String OPCLASSES_PROP = "opClasses";

	// Contains profile -> evaluator Class
	public static Hashtable evalClasses = new Hashtable();

	// Contains profile -> array of operators (operators should be thread safe)
	public static Hashtable evalOps = new Hashtable();

	static {
		try {
			loadProfile(DEFAULT_PROFILE);
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to load default evaluator profile: "
							+ e.getMessage());
		}
	}

	public static void loadProfile(String profile) throws IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		// Don't load a profile if it has already been loaded
		if (evalClasses.contains(profile))
			return;

		Properties props = new Properties();

		String propFileName = "evalprofile.properties";

		if (!DEFAULT_PROFILE.equals(profile))
			propFileName = profile + "-" + propFileName;

		InputStream propStream = EvaluatorFactory.class
				.getResourceAsStream(propFileName);

		props.load(propStream);

		// Load and store the evaluator class
		String evalClassName = props.getProperty(EVALCLASS_PROP);
		Class evalClass = Class.forName(evalClassName);
		evalClasses.put(profile, evalClass);

		// Load and store the operator instances
		String opClassNames = props.getProperty(OPCLASSES_PROP);
		String[] opClasses = StringUtils.split(opClassNames);
		Operator[] ops = new Operator[opClasses.length];
		for (int i = 0; i < opClasses.length; i++)
			ops[i] = (Operator) Class.forName(opClasses[i]).newInstance();
		evalOps.put(profile, ops);
	}

	public static Evaluator getInstance(String profile)
			throws EvaluationException {

		if (profile == null)
			profile = DEFAULT_PROFILE;

		try {
			loadProfile(profile);
			Class evaluatorClass = (Class) evalClasses.get(profile);
			Operator[] ops = (Operator[]) evalOps.get(profile);
			Evaluator eval = (Evaluator) evaluatorClass.newInstance();
			eval.setOperators(ops);
			return eval;
		} catch (Exception e) {
			throw new EvaluationException("failed to create evaluator: "
					+ e.getMessage());
		}
	}

	public static Evaluator getInstance() throws EvaluationException {
		return getInstance(null);
	}

}
