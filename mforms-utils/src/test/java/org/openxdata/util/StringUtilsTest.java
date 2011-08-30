package org.openxdata.util;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

	public void testSplit() {
		String testString = ",one,two,three,four,five,";
		String[] values = StringUtils.split(testString, ",");
		assertEquals(7, values.length);
		assertEquals("", values[0]);
		assertEquals("", values[6]);
	}

	public void testSplitEmpty() {
		String testString = "";
		String[] values = StringUtils.split(testString, ",");
		assertEquals("", values[0]);
	}

	public void testSplitEmpties() {
		String testString = ",";
		String[] values = StringUtils.split(testString, ",");
		assertEquals(2, values.length);
		assertEquals("", values[0]);
		assertEquals("", values[1]);
	}

	public void testDefaultSplit() {
		String testString = " one two three four five ";
		String[] values = StringUtils.split(testString);
		assertEquals(7, values.length);
		assertEquals("", values[0]);
		assertEquals("", values[6]);
		assertEquals("three", values[3]);
	}

	public void testToBoolean() {
		assertEquals(Boolean.TRUE, StringUtils.toBoolean("true"));
		assertEquals(Boolean.FALSE, StringUtils.toBoolean("false"));
		assertEquals(Boolean.FALSE, StringUtils.toBoolean(null));
		assertEquals(Boolean.FALSE, StringUtils.toBoolean("blahblah"));
	}
}
