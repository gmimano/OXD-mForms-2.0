package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.fcitmuk.db.util.Persistent;


/**
 * Containts the connection request header details of user name, password,
 * and what action to execute.
 * 
 * @author Daniel Kayiwa
 *
 */
public class RequestHeader implements Persistent{

	/** No status specified yet. */
	public static final byte ACTION_NONE = -1;

	/** Status to get a list of studies. */
	public static final byte ACTION_DOWNLOAD_STUDY_LIST = 2;

	/** Status to get a list of form definitions in a study. */
	public static final byte ACTION_DOWNLOAD_STUDY_FORMS = 3;

	/** Status to get a list of form definitions in a list of studies. */
	public static final byte ACTION_DOWNLOAD_STUDIES_FORMS = 4;

	/** Status to save a list of form data. */
	public static final byte ACTION_UPLOAD_DATA = 5;

	/** Status to download a list of users from the server. */
	public static final byte ACTION_DOWNLOAD_USERS = 7;

	/** Status to download a list of users and forms from the server. */
	public static final byte ACTION_DOWNLOAD_USERS_AND_FORMS = 11;
	
	/** Status to download a list of languages. */
	public static final byte ACTION_DOWNLOAD_LANGUAGES = 15;
	
	/** Status to download menu text in the selected language. */
	public static final byte ACTION_DOWNLOAD_MENU_TEXT = 16;
	

	/** The current status. This could be a request or return code status. */
	public byte action = ACTION_NONE;

	private String userName = EpihandyConstants.EMPTY_STRING;
	private String password = EpihandyConstants.EMPTY_STRING;
	private static String serializer = "mforms-proto-1.3.1";
	private String locale = "en";


	/** Constructs a new communication parameter. */
	public RequestHeader(){
		super();
	}

	public byte getAction() {
		return action;
	}

	public void setAction(byte action) {
		this.action = action;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public static String getSerializer() {
		return serializer;
	}

	public static void setSerializer(String serializer) {
		RequestHeader.serializer = serializer;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException{
		dos.writeUTF(getUserName());
		dos.writeUTF(getPassword());
		dos.writeUTF(getSerializer());
		dos.writeUTF(getLocale());
		dos.writeByte(getAction());
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException,InstantiationException,IllegalAccessException{
		setUserName(dis.readUTF().intern());
		setPassword(dis.readUTF().intern());
		setSerializer(dis.readUTF().intern());
		setLocale(dis.readUTF().intern());
		setAction(dis.readByte());
	}
}
