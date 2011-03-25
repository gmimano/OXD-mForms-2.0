package org.fcitmuk.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.fcitmuk.db.util.Persistent;
import org.fcitmuk.epihandy.ResponseHeader;
import org.fcitmuk.midp.db.util.Settings;
import org.fcitmuk.util.AlertMessage;
import org.fcitmuk.util.AlertMessageListener;
import org.fcitmuk.util.MenuText;
import org.fcitmuk.util.SimpleOrderedHashtable;

import com.jcraft.jzlib.ZInputStream;

/**
 * Abstracts the communication details. This class is threaded such that the
 * user does not have to deal with threading issues which are a must for midlets
 * handling blocking calls. When requests are processed the user is notified by
 * callbacks.
 * 
 * @author Daniel Kayiwa
 * 
 */
public class TransportLayer implements Runnable, AlertMessageListener {

	/** Connection type not set. */
	public static final byte CON_TYPE_NULL = -1;

	/** HTTP connection. */
	public static final byte CON_TYPE_HTTP = 1;

	public static final String KEY_HTTP_URL = "HTTP_URL";

	public static final String KEY_FORM_DOWNLOAD_HTTP_URL = "FORM_DOWNLOAD_HTTP_URL";

	public static final String KEY_DATA_UPLOAD_HTTP_URL = "DATA_UPLOAD_HTTP_URL";
	
	/** HTTP connection. */
	public static final String CON_TYPE_NAME_HTTP = MenuText.HTTP();

	/** No action is currently in progress. */
	private int ACTION_NONE = 0;

	/** Currently processing a user request line form download, upload, etc. */
	private int ACTION_PROCESSING_REQUEST = 1;

	/** The connection type to use. */
	private byte conType = CON_TYPE_NULL;

	/** The connection parameters. */
	protected Hashtable conParams = new Hashtable();

	protected Vector connectionParameters = new Vector();

	/** Data request parameters. */
	protected Persistent dataInParams;

	/** Data receive parameters. */
	protected Persistent dataOutParams;

	/** Data to be sent. */
	protected Persistent dataIn;

	/** Data received. */
	protected Persistent dataOut;

	/** Reference to the listener for communication events. */
	protected TransportLayerListener eventListener;

	/** Flag which when true mean we are downloading data, orelse we are uploading. */
	protected boolean isDownload;

	/** The display reference for user selecting the connection type. */
	private Display display;

	/** The screen to display after closing our connection type selection screen. */
	private Displayable prevScreen;

	/** The alert, if any, to display after closing our connection type selection screen. */
	private Alert currentAlert;

	/** The action currently processed. */
	private int currentAction = ACTION_NONE;

	private SimpleOrderedHashtable conTypes;

	private AlertMessage alertMsg;

	private String title = "Server Connection"; //TODO Should be parameterised.

	private ConnectionSettings conSettings;

	private static final String KEY_CONNECTION_TYPE = "CONNECTION_TYPE";
	private static final String STORAGE_NAME_SETTINGS = "fcitmuk.DefaultTransportLayer";
	private static final String STORAGE_NAME_HTTP_SETTINGS = "fcitmuk.util.HttpSettings";

	private Connection con;
	private boolean cancelled = false;
	private boolean cancelPrompt = false;

	String userName = "";
	String password = "";

	/** Cconstructs a transport layer object. */
	public TransportLayer(){
		super();
		initConnectionTypes();
		conSettings = new ConnectionSettings();
	}

	/**
	 * Constructs a transport layer object with the following parameters:
	 * 
	 * @param display - reference to the display.
	 * @param displayable - the screen to show after closing any of ours.
	 */
	public TransportLayer(Display display, Displayable displayable){
		this();
		setDisplay(display);
		setPrevScreen(displayable);
	}

	protected void initConnectionTypes(){
		conTypes = new SimpleOrderedHashtable();
		conTypes.put(new Byte(TransportLayer.CON_TYPE_HTTP), TransportLayer.CON_TYPE_NAME_HTTP);
		loadUserSettings(); //TODO Some issues to resolve here.
	}

	public static byte getConnectionType(String name){
		if(name.equals(TransportLayer.CON_TYPE_NAME_HTTP))
			return TransportLayer.CON_TYPE_HTTP;
		return TransportLayer.CON_TYPE_NULL;
	}

	private void loadUserSettings(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		if(settings.getSetting(KEY_CONNECTION_TYPE) != null)
			conType = Byte.parseByte(settings.getSetting(KEY_CONNECTION_TYPE));

		Enumeration keys = settings.keys();
		while(keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			if(isConnectionParam(key))
				conParams.put(key, settings.getSetting(key));
		}

		settings = new Settings(STORAGE_NAME_HTTP_SETTINGS,true);
		keys = settings.keys();
		while(keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			connectionParameters.addElement(new ConnectionParameter(TransportLayer.CON_TYPE_HTTP,key,settings.getSetting(key)));
		}
	}

	private static boolean isConnectionParam(String key){
		return !key.equals("LAST_SELECTED_MAIN_MENU_ITEM"); //TODO This is very very bad design. Should change it.
	}

	protected void addConnectionType(byte Id, String name){
		conTypes.put(new Byte(Id), name);
	}

	/**
	 * Sets the value of a communication parameter. If it already exists, it is
	 * overwritten.
	 * 
	 * @param key
	 * @param value
	 */
	public void setCommnucationParameter(String key, String value){
		conParams.put(key, value);
	}

	/**
	 * Sets the value of a communication parameter if it does not already exist.
	 * 
	 * @param key
	 * @param value
	 */
	public void setDefaultCommnucationParameter(String key, String value){
		if(!conParams.containsKey(key))
			conParams.put(key, value);
	}

	protected TransportLayerListener getEventListener(){
		return this.eventListener;
	}

	/**
	 * Downloads data over the transport layer.
	 * 
	 * @param dataInParams - Data input connection parameters. eg which request type.
	 * @param dataIn - Data to be sent if any.
	 * @param dataOutParams - Data received parameters. eg failure or success status.
	 * @param dataOut - Data received.
	 * @param eventListener - Reference to listener for communication events.
	 */
	public void download(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut,TransportLayerListener eventListener, String userName, String password){
		saveParameters(dataInParams,dataIn,dataOutParams,dataOut,eventListener,true,userName, password);
		new Thread(this).start();
	}

	/**
	 * Uploads data over the transport layer.
	 * 
	 * @param dataInParams - Data input connection parameters. eg which request type.
	 * @param dataIn - Data to be sent.
	 * @param dataOutParams - Data received parameters. eg failure or success status.
	 * @param dataOut - Data received if any.
	 * @param eventListener - Reference to listener to communication events.
	 */
	public void upload(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut,TransportLayerListener eventListener, String userName, String password){
		saveParameters(dataInParams,dataIn,dataOutParams,dataOut,eventListener,false,userName, password);
		new Thread(this).start();
	}

	/**
	 * Saves parameters to class level variables.
	 * 
	 * @param dataInParams - Data input connection parameters. eg which request type.
	 * @param dataIn - Data to be sent if any.
	 * @param dataOutParams - Data received parameters. eg failure or success status.
	 * @param dataOut - Data received if any.
	 * @param eventListener - Reference to listener to communication events.
	 */
	private void saveParameters(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut,TransportLayerListener eventListener, boolean isDownload, String userName, String password){
		this.dataInParams = dataInParams;
		this.dataIn = dataIn;
		this.dataOutParams = dataOutParams;
		this.dataOut = dataOut;
		this.eventListener = eventListener;
		this.isDownload = isDownload;
		this.userName = userName;
		this.password = password;
	}

	/** 
	 * Called when the thread starts to run the user request.
	 *
	 */
	protected synchronized void handleRequest() {

		cancelled = false;

		//TODO Need to parameterise the title.
		alertMsg  = new AlertMessage(display, title, prevScreen,this);

		showConnectionProgress(MenuText.CONNECTING_TO_SERVER());

		try{
			switch(conType){
			case CON_TYPE_HTTP:
				connectHttp();
				break;
			case CON_TYPE_NULL:
				this.currentAction = this.ACTION_PROCESSING_REQUEST;
				getUserSettings(this.display,this.prevScreen,userName,password);
				break;
			default:
				break;
			}
		}catch(Exception e){
			e.printStackTrace();
			this.eventListener.errorOccured(MenuText.PROBLEM_HANDLING_REQUEST(),e);
		}
	}

	/**
	 * Writes and writes data to and from the streams.
	 * @param dos - Stream to write data to.
	 * @param dis - Stream to read data from.
	 * @throws IOException - Thrown when there is a problem for a read or write operation.
	 */
	protected void handleStreams(DataOutputStream dos,DataInputStream dis) throws IOException {
		try{			
			showConnectionProgress(MenuText.TRANSFERING_DATA());

			if(dataInParams != null) //for now http will not send this value.
				dataInParams.write(dos); 

			if(dataIn != null)
				dataIn.write(dos);

			dos.flush(); //if you dont do this, the client will block on a read.
			dos.close();
			dos = null; //Not setting to null results in KErrCouldNotConnect on the client.

			readResponseData(dis);

		}catch(Exception e){
			this.eventListener.errorOccured(MenuText.PROBLEM_HANDLING_STREAMS(),e);
			// TODO: May need to report this to user.
		}finally{
			try{
				dis.close();
				dis = null;
			}catch(Exception ex){
			}
		}
	}

	private void readResponseData(DataInputStream dis) throws Exception {

		ZInputStream zis = null;
		DataInputStream zdis = null;

		try {
			zis = new ZInputStream(dis);
			zdis = new DataInputStream(zis);

			// This should never be null
			dataOutParams.read(zdis);
			byte status = ((ResponseHeader) dataOutParams).getStatus();
			if (status == ResponseHeader.STATUS_SUCCESS) {
				if (dataOut != null) // FO cases where we are not getting any data back.
					dataOut.read(zdis); // When the out param shows failure status, this can be null hence throwing EOF exceptions.

				if (this.isDownload)
					this.eventListener.downloaded(dataInParams, dataIn, dataOutParams, dataOut);
				else
					this.eventListener.uploaded(dataInParams, dataIn, dataOutParams, dataOut);
			} else {
				String s = MenuText.SERVER_PROCESS_FAILURE();
				if (status == ResponseHeader.STATUS_ACCESS_DENIED)
					s = "Access denied";
				else if (status == ResponseHeader.STATUS_FORMS_STALE)
					s = "Please update forms/studies";
				this.eventListener.errorOccured(s, null);
			}
		} finally {
			if (zdis != null)
				try { zdis.close();	} catch (IOException ioe) { }
			if (zis != null)
				try { zis.close(); } catch (IOException ioe) { }
		}
	}

	/**
	 * Handles HTTP communications. eg GPRS.
	 * 
	 * @throws IOException - Thrown when there is a problem reading from or writting
	 * 						 to the connection stream.
	 */
	protected void connectHttp() throws IOException{

		DataOutputStream dos = null;
		DataInputStream dis = null;
		
		try{
			String HTTP_URL = (String)conParams.get(TransportLayer.KEY_HTTP_URL);  
			con = (HttpConnection)Connector.open(HTTP_URL);
			showConnectionProgress(MenuText.TRANSFERING_DATA());

			((HttpConnection)con).setRequestMethod(HttpConnection.POST);
			((HttpConnection)con).setRequestProperty("Content-Type","application/octet-stream");
			((HttpConnection)con).setRequestProperty("User-Agent","Profile/MIDP-2.0 Configuration/CLDC-1.0");
			((HttpConnection)con).setRequestProperty("Content-Language", "en-US");

			dos = ((HttpConnection)con).openDataOutputStream();
			if(dataInParams != null)
				dataInParams.write(dos);

			if(dataIn != null){
				dataIn.write(dos);
			}

			int status  = ((HttpConnection)con).getResponseCode();
			if(status != HttpConnection.HTTP_OK)
				this.eventListener.errorOccured(MenuText.RESPONSE_CODE_FAIL()+status, null);
			else {
				// TODO: May need some more specific failure codes
				// For HTTP, we get only data back. Status is via HTTP status code. So no need of readin the data in param.				
				dis = ((HttpConnection)con).openDataInputStream();
				readResponseData(dis);
			}
		}
		catch(SecurityException e){
			this.eventListener.errorOccured(MenuText.DEVICE_PERMISSION_DENIED(),e);
		}
		catch(Exception e){
			e.printStackTrace();
			this.eventListener.errorOccured(MenuText.PROBLEM_HANDLING_REQUEST(),e);
		}
		finally{
			if (dos != null)
				try { dos.close(); } catch (IOException ioe) {}
			if (dis != null)
				try { dis.close(); } catch (IOException ioe) {}
			if (con != null) {
				try { con.close(); } catch (IOException ioe) {}
				con = null; // To prevent subsequent usage
			}
		}
	}

	/** 
	 * Called when the thread starts executing.
	 */
	public void run(){
		handleRequest();
	}

	private void showConnectionProgress(String message){
		alertMsg.showProgress(title, message);
	}
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void onConnectionSettingsClosed(boolean save) {
		try{
			if(save){
				conType = conSettings.getConType();
				conParams = conSettings.getConParams();
				connectionParameters = conSettings.getConnectionParameters();

				Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);

				Enumeration keys = conParams.keys();
				while(keys.hasMoreElements()){
					String key = (String)keys.nextElement();
					settings.setSetting(key, (String)conParams.get(key));
				}

				settings.setSetting(KEY_CONNECTION_TYPE, String.valueOf(conType));
				settings.saveSettings();

				settings = new Settings(STORAGE_NAME_HTTP_SETTINGS,true);	

				for(int i=0; i<connectionParameters.size(); i++)
				{
					ConnectionParameter conParam = (ConnectionParameter)connectionParameters.elementAt(i);
					if(conParam.getValue() != null) {
						settings.setSetting(conParam.getName(), conParam.getValue());
					}
				}
				settings.saveSettings();
			}

			if(eventListener != null)
				eventListener.updateCommunicationParams();

			if(currentAlert != null)
				display.setCurrent(currentAlert, prevScreen);

			//Check to see if we were invoked due to a user request where
			//the connection type was not specified.
			if(save && this.currentAction == this.ACTION_PROCESSING_REQUEST)
				new Thread(this).start(); 
			else
				display.setCurrent(prevScreen);

			this.currentAction = this.ACTION_NONE;
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
		}
	}

	public int getConType() {
		return conType;
	}

	/** Connection type. Set to -1 if you want to display a select connection type screen. */
	public void setConType(byte conType) {
		this.conType = conType;
	}

	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public Displayable getPrevScreen() {
		return prevScreen;
	}

	public void setPrevScreen(Displayable prevScreen) {
		this.prevScreen = prevScreen;
	}

	public Alert getCurrentAlert() {
		return currentAlert;
	}

	public void setCurrentAlert(Alert currentAlert) {
		this.currentAlert = currentAlert;
	}

	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			// For now, canceling is only for bluetooth since it seems to be
			// the slow one when searching for bluetooth devices and services.
			if(cancelPrompt){
				cancelled = true;
				cancelPrompt = false;
			}

			synchronized (this) {
				if (con != null) {
					try {
						con.close();
					} catch (IOException e) {
					}
				}
				display.setCurrent(prevScreen);
			}
		}
		else{
			if(cancelPrompt){ //User was asked if really want to cancel current operation and said NO.
				alertMsg.showProgress(); //continue with the current progress operation.
				cancelPrompt = false;
			}
			else{
				if(!cancelled){ //if not already canceled
					alertMsg.showConfirm(MenuText.OPERATION_CANCEL_PROMPT());
					cancelPrompt = true;
				}
				else
					display.setCurrent(prevScreen);
			}
		}
	}

	/** 
	 * Displays a screen for the user to select a connection type.
	 * 
	 * @param display - reference to the current display.
	 * @param prevScreen - the screen to display after dismissing our screens.
	 */
	public void getUserSettings(Display display, Displayable prevScreen, String name, String password){
		this.prevScreen = prevScreen;
		conSettings.setDisplay(display);
		conSettings.setPrevScreen(prevScreen);
		conSettings.setTitle(MenuText.CONNECTION_SETTINGS());
		conSettings.getUserSettings(display, prevScreen,conType,conTypes,conParams,this,name,password,connectionParameters);
	}
}
