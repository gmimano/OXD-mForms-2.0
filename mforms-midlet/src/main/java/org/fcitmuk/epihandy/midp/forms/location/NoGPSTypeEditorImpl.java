package org.fcitmuk.epihandy.midp.forms.location;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import org.fcitmuk.epihandy.QuestionData;
import org.fcitmuk.epihandy.ValidationRule;
import org.fcitmuk.epihandy.midp.forms.GPSTypeEditor;
import org.fcitmuk.epihandy.midp.forms.TypeEditorListener;
import org.fcitmuk.util.AlertMessage;
import org.fcitmuk.util.AlertMessageListener;
import org.fcitmuk.util.DefaultCommands;
import org.fcitmuk.util.MenuText;


/**
 * 
 * @author dagmar@cell-life.org
 * @author daniel
 */
public class NoGPSTypeEditorImpl extends GPSTypeEditor implements AlertMessageListener {

	public static char GPS_SEPARATOR = ',';

	private AlertMessage alertMsg;
	private QuestionData questionData;
	private TypeEditorListener listener;

	private boolean confirmDelete = false;

	String latitude = "";
	String longitude = "";
	String altitude = "";

	public NoGPSTypeEditorImpl(){

	}

	public void startEdit(QuestionData data, ValidationRule validationRule, boolean singleQtnEdit,int pos, int count, TypeEditorListener listener){
		try{
			this.questionData = data;
			this.listener = listener;

			confirmDelete = false;

			screen = new Form(questionData.getDef().getText());
			screen.addCommand(DefaultCommands.cmdOk);
			screen.addCommand(DefaultCommands.cmdCancel);
			screen.setCommandListener(this);

			alertMsg = new AlertMessage(display,questionData.getDef().getText(),screen,this);
			view(questionData);
		}
		catch(Exception ex){
			alertMsg.show(MenuText.INIT_PROBLEM() + ex.getMessage());
		}
	}

	private void view(QuestionData questionData){
		try {
			boolean answered = false;
			if (questionData != null) {
				String answer = (String)questionData.getAnswer();
				if (answer != null) {
					answered = true;
					
					int pos1 = answer.indexOf(GPS_SEPARATOR);
					latitude = answer.substring(0,pos1);
	
					int pos2 = answer.lastIndexOf(GPS_SEPARATOR);
					longitude = answer.substring(pos1+1,pos2);
	
					altitude = answer.substring(pos2+1);
				}
			}

			TextField txtField = new TextField("Latitude",latitude,20,TextField.DECIMAL);
			((Form)screen).append(txtField);

			txtField = new TextField("Longitude",longitude,20,TextField.DECIMAL);
			((Form)screen).append(txtField);

			txtField = new TextField("Altitude",altitude,20,TextField.DECIMAL);
			((Form)screen).append(txtField);

			if (answered)
			    screen.addCommand(DefaultCommands.cmdDelete);

			display.setCurrent(screen);
		}
		catch(Exception ex){
			questionData.setAnswer(null);
			alertMsg.show(MenuText.VIEW_PROBLEM() + ex.getMessage());
		}
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		boolean save = false;

		if (c == DefaultCommands.cmdOk) {
			String s = ((TextField)((Form)screen).get(0)).getString();
			s += GPS_SEPARATOR + ((TextField)((Form)screen).get(1)).getString();
			s += GPS_SEPARATOR + ((TextField)((Form)screen).get(2)).getString();

			questionData.setAnswer(s);
			save = true;
		}
		else if(c == DefaultCommands.cmdDelete){
			confirmDelete = true;
			alertMsg.showConfirm(MenuText.DELETE_PROMPT() + " "+questionData.getDef().getText()+"?");
		}

		if(c == DefaultCommands.cmdOk || c == DefaultCommands.cmdCancel)
			listener.endEdit(save, questionData, null);
	}

	public void onAlertMessage(byte msg) {
		if(confirmDelete){
			confirmDelete = false;
			if(msg == AlertMessageListener.MSG_OK){
				questionData.setAnswer(null);
				listener.endEdit(true, questionData, null);
			}
			else
				show();
		}
		else
			listener.endEdit(false, questionData, null);
	}
}