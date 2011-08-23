package org.openxdata.mforms.midp.forms;

import javax.microedition.lcdui.Command;

import org.openxdata.mforms.QuestionData;



/**
 * Events fired by type editors. E.g when finished editing.
 * 
 * @author Daniel Kayiwa
 *
 */
public interface TypeEditorListener {
	public void endEdit(boolean save, QuestionData value, Command cmd);
}
