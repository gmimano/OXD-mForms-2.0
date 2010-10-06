package org.fcitmuk.epihandy.midp.forms;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import org.fcitmuk.epihandy.midp.db.StoredStudyDef;
import org.fcitmuk.epihandy.midp.model.Model;
import org.fcitmuk.epihandy.midp.model.ModelListener;
import org.fcitmuk.midp.mvc.AbstractView;
import org.fcitmuk.util.DefaultCommands;
import org.fcitmuk.util.MenuText;


/**
 * 
 * @author daniel
 *
 */
public class StudyListView extends AbstractView implements CommandListener, ModelListener {
	
	Model model;
	
	public StudyListView(Model model) {
		List list = new List("", Choice.IMPLICIT);
		list.setFitPolicy(List.TEXT_WRAP_ON);
		screen = list;
		this.model = model;
		model.addModelListener(this);

		screen.setCommandListener(this);
		screen.addCommand(DefaultCommands.cmdExit);
		screen.addCommand(DefaultCommands.cmdSel);
		screen.addCommand(DefaultCommands.cmdDownloadStudy);
		screen.addCommand(DefaultCommands.cmdUploadData);
		screen.addCommand(DefaultCommands.cmdSettings);
	}
	
	public void showStudyList(StoredStudyDef[] studyList) {
		((List)screen).setTitle(MenuText.SELECT_STUDY() + " - " + title);
		//screen.setCommandListener(this);
		//screen.addCommand(DefaultCommands.cmdOk);
		//screen.addCommand(DefaultCommands.cmdCancel);
		display.setCurrent(screen);
	}

	private void updateList(StoredStudyDef[] studyList) {

		((List) screen).deleteAll();

		for (int i = 0; i < studyList.length; i++) {
			StoredStudyDef study = studyList[i];
			Image img = null;
			if (model.studyInError(study.getId()))
				img = Images.ERROR_IMAGE;
			else if (model.storedDataExistsForStudy(study.getId()))
				img = Images.HASDATA_IMAGE;
			((List) screen).append(study.getName(), img);
		}
	}
	
	private void selectItem(int selection) {
		List list = (List) screen;
		if (selection >= 0 && selection < list.size()) {
			list.setSelectedIndex(selection, true);
		}
	}
	
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {

		
		try {
			EpihandyController controller = getEpihandyController();
			if (c == List.SELECT_COMMAND || c == DefaultCommands.cmdOk || c == DefaultCommands.cmdSel) {
				int selected = ((List) d).getSelectedIndex();
				model.setSelectedStudyIndex(selected);
				controller.execute(this, DefaultCommands.cmdOk, model
						.getSelectedStudyDef());
			}
			else if (c == DefaultCommands.cmdCancel) {
				controller.execute(this, DefaultCommands.cmdCancel, null);
			}
			else if (c == DefaultCommands.cmdSettings) {
				controller.displayUserSettings(this.getScreen());
			}
			else if (c == DefaultCommands.cmdDownloadStudy) {
				controller.downloadStudies();
			}
			else if (c == DefaultCommands.cmdUploadData) {
				controller.uploadData(this.getScreen());
			}
			else if (c == DefaultCommands.cmdExit) {
				controller.logout();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private EpihandyController getEpihandyController(){
		return (EpihandyController)controller;
	}

	public void formSelected(Model m) {
		// Don't care about forms 
	}

	public void formsChanged(Model m) {
		// Don't care about studies
	}

	public void studiesChanged(Model m) {
		updateList(model.getStudies());
	}

	public void studySelected(Model m) {
		selectItem(model.getSelectedStudyIndex());
	}

	public void formDataChanged(Model m) {
		updateList(model.getStudies());
	}

	public void formErrorsChanged(Model m) {
		updateList(model.getStudies());
	}

	public void formDataSelected(Model m) {
		// Don't care about form data selection
	}
}
