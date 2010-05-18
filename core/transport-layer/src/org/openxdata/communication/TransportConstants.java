package org.openxdata.communication;

/**
 * This class contains properties specifying the server urls used for
 * http connections.
 * 
 * See the ant file build-configuration.xml that replaces the tokens
 * with the values in the build.properties and creates the class 
 * in the correct package so it can be used in the application.
 *  
 * @author dagmar@cell-life.org
 */
public class TransportConstants  {
	
	public static final String FORMSDOWNLOAD_URL = "http://localhost:8888/openxdata/formdownloadservlet";
	public static final String DATAUPLOAD_URL = "http://localhost:8888/openxdata/formdownloadservlet";
	public static final String USERSSDOWNLOAD_URL = "http://localhost:8888/openxdata/formdownloadservlet";

}