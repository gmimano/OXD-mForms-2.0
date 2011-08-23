package org.fcitmuk.epihandy.midp.forms;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.fcitmuk.epihandy.LanguageList;
import org.fcitmuk.epihandy.midp.db.EpihandyDataStorage;
import org.fcitmuk.midp.db.util.Settings;
import org.fcitmuk.midp.mvc.AbstractView;
import org.fcitmuk.util.AlertMessage;
import org.fcitmuk.util.AlertMessageListener;
import org.fcitmuk.util.DefaultCommands;
import org.fcitmuk.util.MenuText;


/**
 * 
 * @author daniel
 *
 */
public class LanguageSettings extends AbstractView implements AlertMessageListener{

	private static final String KEY_LAST_SELECTED_ITEM = "KEY_LAST_SELECTED_ITEM_LANG";
	private static final String KEY_LOCALE = "LOCALE ";
	public static final String STORAGE_NAME_SETTINGS = GeneralSettings.STORAGE_NAME_SETTINGS;

	private Vector languages;
	private AlertMessage alertMsg;
	private List languagesDisplay;
	private boolean downloadMenuTextConfirm = false;
	private LanguageList langlist;

	public void display(Display display, Displayable prevScreen){

		setDisplay(display);
		setPrevScreen(prevScreen);

		List list = new List(MenuText.LANGUAGE(), Choice.IMPLICIT);
		list.setFitPolicy(List.TEXT_WRAP_ON);
		screen = list; 

		alertMsg = new AlertMessage(display,title,screen,this);

		screen.addCommand(DefaultCommands.cmdBack);
		screen.addCommand(DefaultCommands.cmdSel);

		((List)screen).append(MenuText.SELECT(), null);
		((List)screen).append(MenuText.DOWNLOAD(), null);

		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		String val = settings.getSetting(KEY_LAST_SELECTED_ITEM);
		if(val != null)
			((List)screen).setSelectedIndex(Integer.parseInt(val),true);

		screen.setCommandListener(this);

		AbstractView.display.setCurrent(screen);
	}

	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		if(c == DefaultCommands.cmdSel || c == DefaultCommands.cmdSelect)
			handleOkCommand(d);
		else if(c == DefaultCommands.cmdBack){
			if(d == languagesDisplay)
				show();
			else
				display.setCurrent(prevScreen);
			
			FormManager.getInstance().restorePrevScreen();
		}
	}


	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){

		int index = ((List)d).getSelectedIndex();

		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		
		if(d == languagesDisplay){
			String locale = (String)languages.elementAt(languagesDisplay.getSelectedIndex());
			settings.setSetting(KEY_LOCALE, locale);
			alertMsg.showConfirm(MenuText.MENU_TEXT_DOWNLOAD_PROMPT() + " {" + (String)langlist.getLanguages().get(locale) + "}");
			downloadMenuTextConfirm = true;
		}
		else{
			if(index == 0){
				langlist = EpihandyDataStorage.getLanguages();
				if(langlist == null || langlist.size() == 0)
					alertMsg.showConfirm(MenuText.NO_LANGUAGES_FOUND());
				else{
					List list = new List(MenuText.SELECT_LANGUAGE(), Choice.IMPLICIT);
					list.setFitPolicy(List.TEXT_WRAP_ON);
					languagesDisplay = list;
					languages = new Vector();
					String selLang = settings.getSetting(KEY_LOCALE);
					int selectedLangIndex = 0;
					int curIndex = -1;
					
					Enumeration keys = langlist.getLanguages().keys();
					String key;
					while(keys.hasMoreElements()){
						key  = (String)keys.nextElement();
						languages.addElement(key);
						languagesDisplay.append((String)langlist.getLanguages().get(key),null);
						
						curIndex++;
						if(key.equals(selLang))
							selectedLangIndex = curIndex;
					}
					
					languagesDisplay.setSelectedIndex(selectedLangIndex, true);
					languagesDisplay.addCommand(DefaultCommands.cmdBack);
					languagesDisplay.addCommand(DefaultCommands.cmdSel);
					languagesDisplay.setCommandListener(this);
					AbstractView.display.setCurrent(languagesDisplay);
				}
			}
			else
				FormManager.getInstance().downloadLanguages(screen, true);

			settings.setSetting(KEY_LAST_SELECTED_ITEM, String.valueOf(((List)screen).getSelectedIndex()));
		}
		
		settings.saveSettings();
	}

	public void onAlertMessage(byte msg){
		if(msg == AlertMessageListener.MSG_OK){
			if(downloadMenuTextConfirm)
				FormManager.getInstance().downloadMenuText(screen, false);
			else
				FormManager.getInstance().downloadLanguages(screen, false);
		}
		else
			show();
		
		downloadMenuTextConfirm = false;
	}

	public static String getLocale(){
		return new Settings(STORAGE_NAME_SETTINGS,true).getSetting(KEY_LOCALE,"en");
	}
}
