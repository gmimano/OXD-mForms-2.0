package org.openxdata.communication;

import org.openxdata.mforms.persistent.*;

/**
 * Interface through which the transport layer communicates to the user.
 * 
 * @author Daniel Kayiwa
 *
 */
public interface TransportLayerListener {
	
	/**
	 * Called after data has been successfully uploaded.
	 * 
	 * @param dataInParams
	 * @param dataIn
	 * @param dataOutParams - parameters sent after data has been uploaded.
	 * @param dataOut - data sent after the upload.
	 */
	public void uploaded(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut);
	
	/**
	 * Called after data has been successfully downloaded.
	 * 
	 * @param dataInParams
	 * @param dataIn
	 * @param dataOutParams - the parameters sent with the data.
	 * @param dataOut - the downloaded data.
	 */
	public void downloaded(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut);
	
	/**
	 * Called when an error occurs during a data upload or download.
	 * 
	 * @param errorMessage - the error message.
	 * @param e - the exception, if any, that did lead to this error.
	 */
	public void errorOccured(String errorMessage, Exception e);
	
	public void cancelled();
	
	public void updateCommunicationParams();
}
