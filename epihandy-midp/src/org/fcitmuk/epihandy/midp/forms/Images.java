package org.fcitmuk.epihandy.midp.forms;

import java.io.IOException;

import javax.microedition.lcdui.Image;

/**
 * Utility class to hold references to loaded images.
 * 
 * @author batkinson
 * 
 */
public class Images {

	public static Image ERROR_IMAGE;
	public static Image HASDATA_IMAGE;
	public static Image REQUIRED_IMAGE;
	public static Image DISABLED_QUESTION;
	public static Image EMPTY_IMAGE;

	static {
		try {
			EMPTY_IMAGE = Image.createImage("/icons/empty.png");
			ERROR_IMAGE = Image.createImage("/icons/alert.png");
			HASDATA_IMAGE = Image.createImage("/icons/dot.png");
			REQUIRED_IMAGE = Image.createImage("/icons/asterisk.png");
			DISABLED_QUESTION = Image.createImage("/icons/delete-gray.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
