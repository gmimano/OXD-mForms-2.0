package org.openxdata.rpneval;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.openxdata.util.Properties;
import org.openxdata.util.StringUtils;

public class EvaluatorFactory {

	public static final String PROPFILE_NAME = "rpneval.properties";
	public static final String EVALCLASS_PROP = "implClass";
	public static final String OPCLASSES_PROP = "opClasses";
	public static final String DEFPROF_PROP = "defaultProfile";

	public static Properties configProps = new Properties();
	public static String defaultProfile;

	// Contains profile -> evaluator Class
	public static Hashtable evalClasses = new Hashtable();

	// Contains profile -> array of operators (operators should be thread safe)
	public static Hashtable evalOps = new Hashtable();

	static {
		// Load properties, first default file, then overwrite with overrides
		String[] propFiles = { PROPFILE_NAME, "/" + PROPFILE_NAME };
		for (int i = 0; i < propFiles.length; i++) {
			InputStream propStream = EvaluatorFactory.class
					.getResourceAsStream(propFiles[i]);
			if (propStream != null)
				try {
					configProps.load(propStream);
				} catch (IOException e) {
					throw new RuntimeException("failed to init evaluator: "
							+ e.getMessage());
				}
		}

		// Set the default profile from loaded properties
		defaultProfile = configProps.getProperty(DEFPROF_PROP);
	}

	public static void loadProfile(String profile) throws IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		// Don't load a profile if it has already been loaded
		if (evalClasses.contains(profile))
			return;

		// Load and store the evaluator class
		String evalClassName = configProps.getProperty(profile + "."
				+ EVALCLASS_PROP);
		Class evalClass = Class.forName(evalClassName);
		evalClasses.put(profile, evalClass);

		// Load and store the operator instances
		String opClassNames = configProps.getProperty(profile + "."
				+ OPCLASSES_PROP);
		String[] opClasses = StringUtils.split(opClassNames);
		Operator[] ops = new Operator[opClasses.length];
		for (int i = 0; i < opClasses.length; i++)
			ops[i] = (Operator) Class.forName(opClasses[i]).newInstance();
		evalOps.put(profile, ops);
	}

	public static Evaluator getInstance(String profile)
			throws EvaluationException {

		if (profile == null)
			profile = defaultProfile;

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
