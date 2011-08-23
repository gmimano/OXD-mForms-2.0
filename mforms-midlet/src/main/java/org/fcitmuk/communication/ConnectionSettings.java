package org.fcitmuk.communication;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;

import org.fcitmuk.midp.mvc.AbstractView;
import org.fcitmuk.util.DefaultCommands;
import org.fcitmuk.util.MenuText;
import org.fcitmuk.util.Properties;
import org.fcitmuk.util.SimpleOrderedHashtable;

/**
 * This class shows existing connection parameters and lets the user modify them,
 * before passing them over.
 * 
 * @author Daniel
 *
 */
public class ConnectionSettings extends AbstractView {

	private static final int CA_NONE = 0;
	private static final int CA_CON_TYPES = 1;
	private static final int CA_CON_PARAMS = 2;
	
	String userName = "";
	String password = "";
	
	/** The connection type to use. */
	private byte conType = TransportLayer.CON_TYPE_NULL;
	
	/** The connection parameters. */
	protected Hashtable conParams = new Hashtable();
	
	/** The connection parameters. */
	protected Vector connectionParameters = new Vector();
	
	private SimpleOrderedHashtable conTypes;
	
	/** Reference to the parent. */
	TransportLayer defTransLayer;
	
	private int currentAction = CA_NONE;
	
	public ConnectionSettings(){
		
	}
	
	/** 
	 * Displays a screen for the user to select a connection type.
	 * 
	 * @param display - reference to the current display.
	 * @param prevScreen - the screen to display after dismissing our screens.
	 */
	public void getUserSettings(Display display, Displayable prevScreen, byte conType, SimpleOrderedHashtable conTypes,Hashtable conParams, TransportLayer defTransLayer, String name, String password, Vector connectionParameters){
		AbstractView.display = display;
		this.prevScreen = prevScreen;
		this.conTypes = conTypes;
		this.conParams = conParams;
		this.connectionParameters = connectionParameters;
		this.defTransLayer = defTransLayer;
		
		// If there is only one option, ask for params directly
		if (conTypes.size() == 1) {
			this.conType = fromIndexToConType(0);
			showConParams();
			return;
		}
		
		currentAction = CA_CON_TYPES;
		
		List list = new List(MenuText.CONNECTION_TYPE(), Choice.IMPLICIT);
		list.setFitPolicy(List.TEXT_WRAP_ON);
		screen = list;
		
		//TODO This hashtable does not maintain the order on devices like sony erickson.
		Enumeration keys = conTypes.keys();
		Object key;
		while(keys.hasMoreElements()){
			key = keys.nextElement();
			((List)screen).append(conTypes.get(key).toString(), null);
		}
			
		screen.addCommand(DefaultCommands.cmdOk);
		screen.addCommand(DefaultCommands.cmdCancel);
		byte index = fromConTypeToIndex(conType);
		if(index >= 0 && index < ((List)screen).size())
			((List)screen).setSelectedIndex(index,true);
		screen.setCommandListener(this);
				
		AbstractView.display.setCurrent(screen);
	}
	
	private byte fromConTypeToIndex(byte conType){
		return (byte)conTypes.getIndex(new Byte(conType));
	}
	
	private byte fromIndexToConType(int index){
		return ((Byte)conTypes.keyAt(index)).byteValue();
	}
		
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == DefaultCommands.cmdOk || c == List.SELECT_COMMAND)
				handleOkCommand(d);
			else if(c == DefaultCommands.cmdCancel)
				handleCancelCommand(d);
		}
		catch(Exception e){
			// TODO: Handle this gracefully
		}
	}
	
	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleCancelCommand(Displayable d){
		if(currentAction == CA_CON_PARAMS && conTypes.size() > 1){
			currentAction = CA_CON_TYPES;
			display.setCurrent(screen);
		}
		else
			defTransLayer.onConnectionSettingsClosed(false);
	}
	
	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){
		if(currentAction == CA_CON_PARAMS){
			if(d != null){
				if(conType == TransportLayer.CON_TYPE_HTTP){
					conParams.put(TransportLayer.KEY_FORM_DOWNLOAD_HTTP_URL, ((TextField)((Form)d).get(0)).getString());
					conParams.put(TransportLayer.KEY_DATA_UPLOAD_HTTP_URL, ((TextField)((Form)d).get(1)).getString());
					
					for(int i=0; i<connectionParameters.size(); i++)
					{
						ConnectionParameter conParam = (ConnectionParameter)connectionParameters.elementAt(i);
						if(conParam.getConnectionType() == TransportLayer.CON_TYPE_HTTP)
							conParam.setValue(((TextField)((Form)d).get(i+3)).getString());
					}
				}
			}
			defTransLayer.onConnectionSettingsClosed(true);
		}
		else{
			conType = fromIndexToConType(((List)d).getSelectedIndex());
			showConParams();
		}
	}
	
	private void showConParams(){
		currentAction = CA_CON_PARAMS;
		
		if(conType == TransportLayer.CON_TYPE_HTTP)
			showHttpConParams();
		else
			handleOkCommand(null);
	}
	
	private Properties getDefaults() {
		Properties p = new Properties();
		try {
			p.load(ConnectionSettings.class
					.getResourceAsStream("/defaults.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}
	
	private void showHttpConParams(){
		Form frm = new Form(this.title);
		
		Properties defaults = getDefaults();
		
		String s = (String)conParams.get(TransportLayer.KEY_FORM_DOWNLOAD_HTTP_URL);
		if(s == null) s = defaults.getProperty("httpDownloadUrl");
				
		TextField txtField = new TextField(MenuText.FORM_DOWNLOAD_URL(),s,500,TextField.ANY);
		frm.append(txtField);
		
		s = (String)conParams.get(TransportLayer.KEY_DATA_UPLOAD_HTTP_URL);
		if(s == null) s = defaults.getProperty("httpUploadUrl");

		txtField = new TextField(MenuText.DATA_UPLOAD_URL(),s,500,TextField.ANY);
		frm.append(txtField);
		
		for(int i=0; i<connectionParameters.size(); i++)
		{
			ConnectionParameter conParam = (ConnectionParameter)connectionParameters.elementAt(i);
			if(conParam.getConnectionType() == TransportLayer.CON_TYPE_HTTP)
			{
				txtField = new TextField(conParam.getName(),conParam.getValue(),500,TextField.ANY);
				frm.append(txtField);
			}
		}
		
		frm.addCommand(DefaultCommands.cmdCancel);
		frm.addCommand(DefaultCommands.cmdOk);
		frm.setCommandListener(this);
					
		display.setCurrent(frm);
	}
	
	public byte getConType(){
		return conType;
	}
	
	public Hashtable getConParams(){
		return conParams;
	}
	
	public Vector getConnectionParameters(){
		return connectionParameters;
	}
}
