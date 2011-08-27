package org.openxdata.util;

import java.io.IOException;

import junit.framework.TestCase;

public class PropertiesTest extends TestCase {

	public void testCreate() {
		new Properties();
	}

	public void testLoad() throws IOException {
		Properties props = new Properties();
		props.load(PropertiesTest.class.getResourceAsStream("test.properties"));
		assertEquals("firstvalue", props.getProperty("firstprop"));
		assertEquals("lastvalue", props.getProperty("lastprop"));
	}
}
