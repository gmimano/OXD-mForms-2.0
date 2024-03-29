package org.openxdata.mforms.midp.forms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.media.Control;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;
import javax.microedition.media.control.VideoControl;

import org.openxdata.mforms.model.QuestionData;
import org.openxdata.mforms.model.QuestionDef;
import org.openxdata.mforms.model.ValidationRule;
import org.openxdata.midp.mvc.AbstractView;
import org.openxdata.util.AlertMessage;
import org.openxdata.util.AlertMessageListener;
import org.openxdata.util.DefaultCommands;
import org.openxdata.util.MenuText;


/**
 * 
 * @author daniel
 *
 */
public class MultmediaTypeEditor extends AbstractView implements TypeEditor, AlertMessageListener{

	private AlertMessage alertMsg;

	private QuestionData questionData;
	private TypeEditorListener listener;

	private Player player = null;
	private Control control = null;
	private byte type = QuestionDef.QTN_TYPE_NULL;

	private boolean confirmDelete = false;
	private boolean editing = false;

	//for recording
	private ByteArrayOutputStream baos;

	public MultmediaTypeEditor(){

	}

	public void startEdit(QuestionData data, ValidationRule validationRule, boolean singleQtnEdit,int pos, int count, TypeEditorListener listener){
		try{
			this.questionData = data;
			this.listener = listener;

			confirmDelete = false;
			editing = false;

			screen = new Form(questionData.getDef().getText());
			screen.addCommand(DefaultCommands.cmdCancel);
			screen.setCommandListener(this);

			alertMsg = new AlertMessage(display,questionData.getDef().getText(),screen,this);

			if(questionData.getAnswer() != null)
				view();
			else
				edit();
		}
		catch(Exception ex){
			alertMsg.show(MenuText.INIT_PROBLEM() + ex.getMessage());
		}
	}

	private void view(){
		int len = 0;
		try{
			editing = false;

			byte[] data = (byte[])questionData.getAnswer();
			len = data.length;

			if(questionData.getDef().getType() == QuestionDef.QTN_TYPE_IMAGE)
				addImageToForm(data);
			else if(questionData.getDef().getType() == QuestionDef.QTN_TYPE_AUDIO){
				((Form)screen).append(MenuText.PLAYING());
				player = Manager.createPlayer(new ByteArrayInputStream(data), "audio/"+MultMediaSettings.getAudioFormat()); //audio/mpeg(mp3)
				player.start();
			}
			else if(questionData.getDef().getType() == QuestionDef.QTN_TYPE_VIDEO){
				player = Manager.createPlayer(new ByteArrayInputStream(data), "video/"+MultMediaSettings.getVideoFormat());
				player.realize();
				control = player.getControl("VideoControl");
				Item item = (Item)((VideoControl)control).initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
				item.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_VEXPAND);
				((Form)screen).append(item);
				((VideoControl)control).setDisplayFullScreen(true);
				((VideoControl)control).setVisible(true);
				player.start();
			}

			screen.addCommand(DefaultCommands.cmdEdit);
			screen.addCommand(DefaultCommands.cmdDelete);

			display.setCurrent(screen);
		}
		catch(Exception ex){
			questionData.setAnswer(null);
			alertMsg.show(len + MenuText.VIEW_PROBLEM() + ex.getMessage());
		}
	}

	private void addImageToForm(byte[] data){
		Image image = null;
		try{
			image = Image.createImage(data, 0, data.length);
		}catch(Exception ex){
			try{
				image = Image.createImage(data, 0, data.length);
			}
			catch(Exception e){
				image = Image.createImage(data, 0, data.length);
			}
		}

		if(image != null)
			((Form)screen).append(image);
	}

	private void edit(){
		try{
			byte type = questionData.getDef().getType();
			CreatePlayer(type);
			if(player == null){
				alertMsg.show(MenuText.NOT_SUPPORTED_FEATURE()); 
				return;
			}
			CreateControl(type);

			if (control != null){	
				play(type);
				this.type = type;
			}
			else 
				alertMsg.show(MenuText.NO_VIDEO_CONTROL()); 
		}
		catch (Exception me) {
			alertMsg.show(MenuText.EDIT_PROBLEM() + " " + me.getMessage());
		}
	}

	private void play(byte type){
		try{
			screen.addCommand(DefaultCommands.cmdOk);
			display.setCurrent(screen);
			player.start();

			if(type == QuestionDef.QTN_TYPE_IMAGE){
				((Form)screen).append((Item)((VideoControl)control).initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null));
				((VideoControl)control).setDisplayFullScreen(true);
				((VideoControl)control).setVisible(true);
			}
			else if(type == QuestionDef.QTN_TYPE_VIDEO || type == QuestionDef.QTN_TYPE_AUDIO){

				if(type == QuestionDef.QTN_TYPE_VIDEO){
					VideoControl videoControl = (VideoControl) (player.getControl("VideoControl"));
					((Form)screen).append((Item)videoControl.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null));
					videoControl.setDisplayFullScreen(true);
					videoControl.setVisible(true);
				}

				baos = new ByteArrayOutputStream();
				((RecordControl)control).setRecordStream(baos);
				((RecordControl)control).startRecord();
				((Form)screen).append(MenuText.RECORDING());
			}

			editing = true;
		}
		catch (Exception me) {
			alertMsg.show(MenuText.PLAY_PROBLEM() + " " + me.getMessage());
		} 
	}

	private void CreatePlayer(byte type) throws Exception{
		
		if(type == QuestionDef.QTN_TYPE_VIDEO || type == QuestionDef.QTN_TYPE_IMAGE){
			try {
				player = Manager.createPlayer("capture://" + (type == QuestionDef.QTN_TYPE_VIDEO ? "video" : "image"));
			} catch (Exception e) {
				try {
					player = Manager.createPlayer("capture://" + (type == QuestionDef.QTN_TYPE_VIDEO ? "image" : "video"));
				} catch (Exception ex) {
					try {
						player = Manager.createPlayer("capture://camera");
					} catch (Exception ex2) {
						// TODO: Handle this gracefully
					}
				}
			}
		}
		else if(type == QuestionDef.QTN_TYPE_AUDIO){
			try {
				player = Manager.createPlayer("capture://audio");
			} catch (Exception e) {

			}
		} 

		if(player != null){
			player.realize();
			player.prefetch();
		}
	}

	private void CreateControl(byte type){
		if(type == QuestionDef.QTN_TYPE_IMAGE)
			control = player.getControl("VideoControl");
		else if(type == QuestionDef.QTN_TYPE_AUDIO || type == QuestionDef.QTN_TYPE_VIDEO)
			control = player.getControl("RecordControl");
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {

		boolean save = false;

		if (c == DefaultCommands.cmdOk && editing) {
			if(type == QuestionDef.QTN_TYPE_IMAGE)
				save = saveImage();
			else if(type == QuestionDef.QTN_TYPE_VIDEO || type == QuestionDef.QTN_TYPE_AUDIO)
				save = saveRecording();
		}
		else if(c == DefaultCommands.cmdDelete){
			confirmDelete = true;
			alertMsg.showConfirm(MenuText.DELETE_PROMPT() + " "+questionData.getDef().getText()+"?");
		}
		else if(c == DefaultCommands.cmdEdit){
			screen = new Form(questionData.getDef().getText());
			screen.addCommand(DefaultCommands.cmdCancel);
			screen.setCommandListener(this);
			alertMsg = new AlertMessage(display,questionData.getDef().getText(),screen,this);

			stop(type);
			edit();
			return;
		}

		if(c != DefaultCommands.cmdEdit && !(c == DefaultCommands.cmdOk && save))
			stop(type);

		if(c == DefaultCommands.cmdCancel)
			listener.endEdit(save, questionData, null);
	}

	private boolean saveImage(){
		
		new Thread() {
			public void run() {
				try {
					byte[] image = ((VideoControl) control)
							.getSnapshot(MultMediaSettings
									.getPictureParameters());
					questionData.setAnswer(image);
					MultmediaTypeEditor.this.stop(type);
					listener.endEdit(true, questionData, null);
				} catch (Exception me) {
					alertMsg.show(MenuText.IMAGE_SAVE_PROBLEM() + " "
							+ me.getMessage());
				}
			}
		}.start();

		return true;
	}

	private boolean saveRecording(){
		boolean save = false;

		try{
			((RecordControl)control).commit();
			questionData.setAnswer(baos.toByteArray());
			save = true;
		}
		catch(Exception ex){
			alertMsg.show(MenuText.RECODING_SAVE_PROBLEM() + " " + ex.getMessage());
		}

		return save;
	}

	/**
	 *	to stop the player. First the videoControl has to be set invisible
	 *	than the player can be stopped
	 **/
	synchronized void stop(byte type) {
		try{
			try{
			if (control != null) {
				if(type == QuestionDef.QTN_TYPE_IMAGE)
					((VideoControl)control).setVisible(false);
				else if(type == QuestionDef.QTN_TYPE_VIDEO || type == QuestionDef.QTN_TYPE_AUDIO){
					if(editing)
						((RecordControl)control).stopRecord();
					else if(type == QuestionDef.QTN_TYPE_VIDEO)
						((VideoControl)player.getControl("VideoControl")).setVisible(false);
				}
			}
			}catch(Exception ex){}

			if (player != null) {
				try{
					player.stop();
				}catch(Exception ex){}
				try{
					player.close();
				}catch(Exception ex){}
				player = null;
				control = null;
			}
		} 
		catch (Exception me) {
			// TODO: Handle this gracefully
		}
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
