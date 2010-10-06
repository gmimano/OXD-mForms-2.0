package org.fcitmuk.epihandy;

/**
 * Represents when a serialization operation fails due to a missing form
 * definition.
 * 
 * @author batkinson
 * 
 */
public class FormNotFoundException extends Exception {

	private static final long serialVersionUID = -6647600033619991121L;

	public FormNotFoundException() {
	}

	public FormNotFoundException(String message) {
		super(message);
	}

	public FormNotFoundException(Throwable cause) {
		super(cause);
	}

	public FormNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
