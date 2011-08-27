package org.openxdata.util;

import java.util.Vector;

public class StringUtils {

	public static String[] split(String value) {
		return split(value, " ");
	}

	public static String[] split(String value, String splitOn) {

		if (value == null)
			throw new IllegalArgumentException("expects non-null argument");

		Vector values = new Vector();
		int lastIndex = 0;
		int splitOnLen = splitOn.length();

		while (true) {
			int splitIndex = value.indexOf(splitOn);
			if (splitIndex != -1) {
				values.addElement(value.substring(lastIndex, splitIndex));
				value = value.substring(splitIndex + splitOnLen);
			} else {
				values.addElement(value.substring(lastIndex, value.length()));
				break;
			}
		}

		// Copy into typed array
		String[] result = new String[values.size()];
		for (int i = 0; i < result.length; i++)
			result[i] = (String) values.elementAt(i);

		return result;
	}
}
