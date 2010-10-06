package org.fcitmuk.epihandy.midp.forms;

import org.fcitmuk.epihandy.midp.forms.location.NoGPSTypeEditorImpl;
import org.fcitmuk.midp.mvc.AbstractView;


/**
 * This is an abstract class place holder for the Location based Type Editor.
 * It provides a way to create an instance of a GPSTypeEditor without causing
 * JSR-179 code to be loaded (for cases where a phone does not support GPS).
 * 
 * @see org.fcitmuk.epihandy.midp.forms.location.GPSTypeEditorImpl
 * 
 * @author dagmar@cell-life.org
 */
public abstract class GPSTypeEditor extends AbstractView implements TypeEditor {

	public static boolean isGPSAvailable() {
		boolean classFound = false;
        try {
            // this will throw an exception if JSR-179 is missing
            Class.forName("javax.microedition.location.Location");
            classFound = true;
        } catch (Exception e) {
        	// GPS is not available
        }
        return classFound;
	}
	
	public static GPSTypeEditor getGPSTypeEditor() {
		GPSTypeEditor editor = null;
		if (isGPSAvailable()) {
	        try {
	        	// load the actual implementation
	            Class c = Class.forName("org.fcitmuk.epihandy.midp.forms.location.GPSTypeEditorImpl");
	            editor = (GPSTypeEditor)(c.newInstance());
	        } catch (Exception e) {
	            // could not create the actual implementation of the GPSTypeEditor
	        }
		}
		if (editor == null) {
            editor = new NoGPSTypeEditorImpl();			
		}
        return editor;
	}
}