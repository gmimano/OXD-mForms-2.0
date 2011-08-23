package org.fcitmuk.util;

import java.io.IOException;

/**
 * 
 * @author Jonny Heggheim
 * 
 */
public class MenuText {

	private static final Properties menuText = new Properties();
	private static final String DEFAULT_MENU_TEXT_PROPERTIES_FILENAME = "/menu_text.properties";

	static {
		try {
			String locale = System.getProperty("microedition.locale");
			String fileName = "/menu_text_" + locale + ".properties";
			try {
				menuText.load(MenuText.class.getResourceAsStream(fileName));
			} catch (NullPointerException e) {
				menuText.load(MenuText.class
						.getResourceAsStream(DEFAULT_MENU_TEXT_PROPERTIES_FILENAME));
			} catch (IOException e) {
				menuText.load(MenuText.class
						.getResourceAsStream(DEFAULT_MENU_TEXT_PROPERTIES_FILENAME));
			}
		} catch (IOException e) {
			throw new RuntimeException(
					"Could not find or load the translation file: "
							+ e.getMessage());
		}
	}

	private static String getText(String key) {
		String text = menuText.getProperty(key);
		if (text == null) {
			throw new RuntimeException("Could not find translation for '" + key
					+ "'");
		}
		return text;
	}

	public static String LOGIN() {
		return getText("LOGIN");
	}

	public static String USER_NAME() {
		return getText("USER_NAME");
	}

	public static String PASSWORD() {
		return getText("PASSWORD");
	}

	public static String EXIT() {
		return getText("EXIT");
	}

	public static String CANCEL() {
		return getText("CANCEL");
	}

	public static String OK() {
		return getText("OK");
	}

	public static String EDIT() {
		return getText("EDIT");
	}

	public static String NEW() {
		return getText("NEW");
	}

	public static String SAVE() {
		return getText("SAVE");
	}

	public static String DELETE() {
		return getText("DELETE");
	}

	public static String BACK() {
		return getText("BACK");
	}

	public static String YES() {
		return getText("YES");
	}

	public static String NO() {
		return getText("NO");
	}

	public static String BACK_TO_LIST() {
		return getText("BACK_TO_LIST");
	}

	public static String NEXT() {
		return getText("NEXT");
	}

	public static String PREVIOUS() {
		return getText("PREVIOUS");
	}

	public static String FIRST() {
		return getText("FIRST");
	}

	public static String LAST() {
		return getText("LAST");
	}

	public static String SELECT() {
		return getText("SELECT");
	}

	public static String MAIN_MENU() {
		return getText("MAIN_MENU");
	}

	public static String SELECT_STUDY() {
		return getText("SELECT_STUDY");
	}

	public static String SELECT_FORM() {
		return getText("SELECT_FORM");
	}

	public static String DOWNLOAD_STUDIES() {
		return getText("DOWNLOAD_STUDIES");
	}

	public static String DOWNLOAD_FORMS() {
		return getText("DOWNLOAD_FORMS");
	}

	public static String UPLOAD_DATA() {
		return getText("UPLOAD_DATA");
	}

	public static String SETTINGS() {
		return getText("SETTINGS");
	}

	public static String LOGOUT() {
		return getText("LOGOUT");
	}

	public static String EXIT_PROMPT() {
		return getText("EXIT_PROMPT");
	}

	public static String GENERAL() {
		return getText("GENERAL");
	}

	public static String DATE_FORMAT() {
		return getText("DATE_FORMAT");
	}

	public static String MULTIMEDIA() {
		return getText("MULTIMEDIA");
	}

	public static String LANGUAGE() {
		return getText("LANGUAGE");
	}

	public static String CONNECTION() {
		return getText("CONNECTION");
	}

	public static String SINGLE_QUESTION_EDIT() {
		return getText("SINGLE_QUESTION_EDIT");
	}

	public static String NUMBERING() {
		return getText("NUMBERING");
	}

	public static String OK_ON_RIGHT() {
		return getText("OK_ON_RIGHT");
	}

	public static String DELETE_AFTER_UPLOAD() {
		return getText("DELETE_AFTER_UPLOAD");
	}

	public static String DAY_FIRST() {
		return getText("DAY_FIRST");
	}

	public static String MONTH_FIRST() {
		return getText("MONTH_FIRST");
	}

	public static String YEAR_FIRST() {
		return getText("YEAR_FIRST");
	}

	public static String DOWNLOAD() {
		return getText("DOWNLOAD");
	}

	public static String HTTP() {
		return getText("HTTP");
	}

	public static String PICTURE_FORMAT() {
		return getText("PICTURE_FORMAT");
	}

	public static String PICTURE_WIDTH() {
		return getText("PICTURE_WIDTH");
	}

	public static String PICTURE_HEIGHT() {
		return getText("PICTURE_HEIGHT");
	}

	public static String VIDEO_FORMAT() {
		return getText("VIDEO_FORMAT");
	}

	public static String AUDIO_FORMAT() {
		return getText("AUDIO_FORMAT");
	}

	public static String ENCODINGS() {
		return getText("ENCODINGS");
	}

	public static String STUDY() {
		return getText("STUDY");
	}

	public static String UPLOAD_BEFORE_DOWNLOAD_PROMPT() {
		return getText("UPLOAD_BEFORE_DOWNLOAD_PROMPT");
	}

	public static String DOWNLOAD_STUDY_FORMS_PROMPT() {
		return getText("DOWNLOAD_STUDY_FORMS_PROMPT");
	}

	public static String DOWNLOAD_FORMS_PROMPT() {
		return getText("DOWNLOAD_FORMS_PROMPT");
	}

	public static String UN_UPLOADED_DATA_PROMPT() {
		return getText("UN_UPLOADED_DATA_PROMPT");
	}

	public static String FORMS() {
		return getText("FORMS");
	}

	public static String STUDIES() {
		return getText("STUDIES");
	}

	public static String DOWNLOAD_STUDIES_PROMPT() {
		return getText("DOWNLOAD_STUDIES_PROMPT");
	}

	public static String DOWNLOAD_LANGUAGES_PROMPT() {
		return getText("DOWNLOAD_LANGUAGES_PROMPT");
	}

	public static String DOWNLOAD_FORMS_FIRST() {
		return getText("DOWNLOAD_FORMS_FIRST");
	}

	public static String UPLOAD_DATA_PROMPT() {
		return getText("UPLOAD_DATA_PROMPT");
	}

	public static String STUDY_LIST_DOWNLOAD() {
		return getText("STUDY_LIST_DOWNLOAD");
	}

	public static String DOWNLOADING_STUDY_LIST() {
		return getText("DOWNLOADING_STUDY_LIST");
	}

	public static String FORM_DOWNLOAD() {
		return getText("FORM_DOWNLOAD");
	}

	public static String DOWNLOADING_FORMS() {
		return getText("DOWNLOADING_FORMS");
	}

	public static String DOWNLOADING_USERS() {
		return getText("DOWNLOADING_USERS");
	}

	public static String LANGUAGE_DOWNLOAD() {
		return getText("LANGUAGE_DOWNLOAD");
	}

	public static String DOWNLOADING_LANGUAGES() {
		return getText("DOWNLOADING_LANGUAGES");
	}

	public static String DATA_UPLOAD() {
		return getText("DATA_UPLOAD");
	}

	public static String UPLOADING_DATA() {
		return getText("UPLOADING_DATA");
	}

	public static String NO_UPLOAD_DATA() {
		return getText("NO_UPLOAD_DATA");
	}

	public static String PROBLEM_SAVING_DOWNLOAD() {
		return getText("PROBLEM_SAVING_DOWNLOAD");
	}

	public static String STUDY_DOWNLOAD_SAVED() {
		return getText("STUDY_DOWNLOAD_SAVED");
	}

	public static String USER_DOWNLOAD_SAVED() {
		return getText("USER_DOWNLOAD_SAVED");
	}

	public static String NO_SERVER_STUDY_FORMS() {
		return getText("NO_SERVER_STUDY_FORMS");
	}

	public static String FORM_DOWNLOAD_SAVED() {
		return getText("FORM_DOWNLOAD_SAVED");
	}

	public static String NO_LANGUAGES() {
		return getText("NO_LANGUAGES");
	}

	public static String LANGUAGE_DOWNLOAD_SAVED() {
		return getText("LANGUAGE_DOWNLOAD_SAVED");
	}

	public static String DATA_UPLOAD_PROBLEM() {
		return getText("DATA_UPLOAD_PROBLEM");
	}

	public static String DATA_UPLOAD_SUCCESS() {
		return getText("DATA_UPLOAD_SUCCESS");
	}

	public static String DATA_UPLOAD_FAILURE() {
		return getText("DATA_UPLOAD_FAILURE");
	}

	public static String PROBLEM_CLEANING_STORE() {
		return getText("PROBLEM_CLEANING_STORE");
	}

	public static String UNKNOWN_UPLOAD() {
		return getText("UNKNOWN_UPLOAD");
	}

	public static String CONNECTION_TYPE() {
		return getText("CONNECTION_TYPE");
	}

	public static String PROBLEM_HANDLING_REQUEST() {
		return getText("PROBLEM_HANDLING_REQUEST");
	}

	public static String CONNECTING_TO_SERVER() {
		return getText("CONNECTING_TO_SERVER");
	}

	public static String TRANSFERING_DATA() {
		return getText("TRANSFERING_DATA");
	}

	public static String PROBLEM_HANDLING_STREAMS() {
		return getText("PROBLEM_HANDLING_STREAMS");
	}

	public static String SERVER_PROCESS_FAILURE() {
		return getText("SERVER_PROCESS_FAILURE");
	}

	public static String ACCESS_DENIED() {
		return getText("ACCESS_DENIED");
	}

	public static String RESPONSE_CODE_FAIL() {
		return getText("RESPONSE_CODE_FAIL");
	}

	public static String DEVICE_PERMISSION_DENIED() {
		return getText("DEVICE_PERMISSION_DENIED");
	}

	public static String GETTINGS_STREAM() {
		return getText("GETTINGS_STREAM");
	}

	public static String OPEN_CONNECTION_FAIL() {
		return getText("OPEN_CONNECTION_FAIL");
	}

	public static String PROBLEM_OPENING_STREAMS() {
		return getText("PROBLEM_OPENING_STREAMS");
	}

	public static String OPERATION_CANCEL_PROMPT() {
		return getText("OPERATION_CANCEL_PROMPT");
	}

	public static String CONNECTION_SETTINGS() {
		return getText("CONNECTION_SETTINGS");
	}

	public static String PLAYING() {
		return getText("PLAYING");
	}

	public static String INIT_PROBLEM() {
		return getText("INIT_PROBLEM");
	}

	public static String VIEW_PROBLEM() {
		return getText("VIEW_PROBLEM");
	}

	public static String NOT_SUPPORTED_FEATURE() {
		return getText("NOT_SUPPORTED_FEATURE");
	}

	public static String NO_VIDEO_CONTROL() {
		return getText("NO_VIDEO_CONTROL");
	}

	public static String EDIT_PROBLEM() {
		return getText("EDIT_PROBLEM");
	}

	public static String RECORDING() {
		return getText("RECORDING");
	}

	public static String PLAY_PROBLEM() {
		return getText("PLAY_PROBLEM");
	}

	public static String DELETE_PROMPT() {
		return getText("DELETE_PROMPT");
	}

	public static String IMAGE_SAVE_PROBLEM() {
		return getText("IMAGE_SAVE_PROBLEM");
	}

	public static String RECODING_SAVE_PROBLEM() {
		return getText("RECODING_SAVE_PROBLEM");
	}

	public static String DATA_LIST() {
		return getText("DATA_LIST");
	}

	public static String DATA_LIST_DISPLAY_PROBLEM() {
		return getText("DATA_LIST_DISPLAY_PROBLEM");
	}

	public static String FORM_DELETE_PROMPT() {
		return getText("FORM_DELETE_PROMPT");
	}

	public static String FORM_SAVE_SUCCESS() {
		return getText("FORM_SAVE_SUCCESS");
	}

	public static String NO_SELECTED_STUDY() {
		return getText("NO_SELECTED_STUDY");
	}

	public static String NO_STUDY_FORMS() {
		return getText("NO_STUDY_FORMS");
	}

	public static String FORM_DATA_DISPLAY_PROBLEM() {
		return getText("FORM_DATA_DISPLAY_PROBLEM");
	}

	public static String NEXT_PAGE() {
		return getText("NEXT_PAGE");
	}

	public static String PREVIOUS_PAGE() {
		return getText("PREVIOUS_PAGE");
	}

	public static String FORM_DISPLAY_PROBLEM() {
		return getText("FORM_DISPLAY_PROBLEM");
	}

	public static String NO_VISIBLE_QUESTION() {
		return getText("NO_VISIBLE_QUESTION");
	}

	public static String QUESTIONS() {
		return getText("QUESTIONS");
	}

	public static String FORM_CLOSE_PROMPT() {
		return getText("FORM_CLOSE_PROMPT");
	}

	public static String REQUIRED_PROMPT() {
		return getText("REQUIRED_PROMPT");
	}

	public static String ANSWER_MINIMUM_PROMPT() {
		return getText("ANSWER_MINIMUM_PROMPT");
	}

	public static String INVALID_NAME_PASSWORD() {
		return getText("INVALID_NAME_PASSWORD");
	}

	public static String SELECT_LANGUAGE() {
		return getText("SELECT_LANGUAGE");
	}

	public static String NO_LANGUAGES_FOUND() {
		return getText("NO_LANGUAGES_FOUND");
	}

	public static String NO_FORM_DEF() {
		return getText("NO_FORM_DEF");
	}

	public static String MENU_TEXT_DOWNLOAD() {
		return getText("MENU_TEXT_DOWNLOAD");
	}

	public static String DOWNLOADING_MENU_TEXT() {
		return getText("DOWNLOADING_MENU_TEXT");
	}

	public static String MENU_TEXT_DOWNLOAD_SAVED() {
		return getText("MENU_TEXT_DOWNLOAD_SAVED");
	}

	public static String USER_DOWNLOAD_URL() {
		return getText("USER_DOWNLOAD_URL");
	}

	public static String FORM_DOWNLOAD_URL() {
		return getText("FORM_DOWNLOAD_URL");
	}

	public static String DATA_UPLOAD_URL() {
		return getText("DATA_UPLOAD_URL");
	}

	public static String DATA_DELETE_PROMPT() {
		return getText("DATA_DELETE_PROMPT");
	}

	public static String NO_MENU_TEXT() {
		return getText("NO_MENU_TEXT");
	}

	public static String MENU_TEXT_DOWNLOAD_PROMPT() {
		return getText("MENU_TEXT_DOWNLOAD_PROMPT");
	}

	public static String SHOW_ERRORS() {
		return getText("SHOW_ERRORS");
	}

	public static String CLEAR_ERRORS() {
		return getText("CLEAR_ERRORS");
	}

	public static String FORM_SAVE_STUDY_FULL() {
		return getText("FORM_SAVE_STUDY_FULL");
	}

	public static String STUDY_FULL_UPLOAD() {
		return getText("STUDY_FULL_UPLOAD");
	}

	public static String GOTO_PAGE() {
		return getText("GOTO_PAGE");
	}

	public static String GOTO_QUESTION() {
		return getText("GOTO_QUESTION");
	}

	public static String GOTO_PAGE_SELECTION_LIST() {
		return getText("GOTO_PAGE_SELECTION_LIST");
	}

	public static String GOTO_QUESTION_INPUT() {
		return getText("GOTO_QUESTION_INPUT");
	}

	public static String GOTO_QUESTION_INPUT_LABEL() {
		return getText("GOTO_QUESTION_INPUT_LABEL");
	}
	
	public static String AUTO_SAVE(){
		return getText("AUTO_SAVE");
	}
}
