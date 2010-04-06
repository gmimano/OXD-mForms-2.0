package org.fcitmuk.purcai.forms;

import org.fcitmuk.communication.TransportLayer;
import org.fcitmuk.epihandy.RequestHeader;
import org.fcitmuk.epihandy.ResponseHeader;
import org.fcitmuk.purcai.MarkSheetData;
import org.fcitmuk.purcai.MarkSheetDataList;
import org.fcitmuk.purcai.MarkSheetTest;
import org.fcitmuk.purcai.PurcaiConstants;
import org.fcitmuk.purcai.TestDataTest;

public class PurcaiTransportLayer extends TransportLayer{
	
	private final static byte CON_TYPE_TESTING = 6;
	
	public PurcaiTransportLayer(){
		super();
	}
	
	protected void initConnectionTypes(){
		super.initConnectionTypes();
		addConnectionType(CON_TYPE_TESTING, "Testing");
		//if(getConType() == TransportLayer.CON_TYPE_NULL)
		//	setConType(CON_TYPE_TESTING);
	}
	
	protected void handleRequest() {
		if(this.getConType() == CON_TYPE_TESTING)
			handleTestConnectionType();
		else
			super.handleRequest();
	}
	
	private void handleTestConnectionType(){
		try{
			int action = ((RequestHeader)dataInParams).getAction();

			//The next stream value is determined by the status of the communication parameter.
			if(action == PurcaiConstants.ACTION_DOWNLOAD_TESTDATA)
				downloadTestData();
			else if(action == PurcaiConstants.ACTION_DOWNLOAD_MARKSHEET)
				downloadMarkSheet();
			else if(action == PurcaiConstants.ACTION_UPLOAD_MARKS)
				uploadMarks();

			if(this.isDownload)
				getEventListener().downloaded(dataOutParams,dataOut);
			else
				getEventListener().uploaded(dataOutParams,dataOut);
		}
		catch(Exception e){
			e.printStackTrace();
			getEventListener().errorOccured(e.getMessage(), e);
		}
	}
	
	private void downloadTestData(){
		dataOut = TestDataTest.getTestDataTest();
		((ResponseHeader)dataOutParams).setStatus((dataOut != null) ? ResponseHeader.STATUS_SUCCESS : ResponseHeader.STATUS_ERROR);
	}
	
	private void downloadMarkSheet(){
		dataOut = MarkSheetTest.getMarkSheetTest();
		((ResponseHeader)dataOutParams).setStatus((dataOut != null) ? ResponseHeader.STATUS_SUCCESS : ResponseHeader.STATUS_ERROR);
	}
	
	private void uploadMarks(){
		MarkSheetDataList markSheetDataList = (MarkSheetDataList)dataIn;
		System.out.println("Server got :" + markSheetDataList.getMarkSheets().size() + " MarkSheets");	
		MarkSheetData data = markSheetDataList.getMarkSheet(1);
		System.out.println(data.getStudentMarks().size());
		System.out.println(data.getHeader().getTestTypeId());
		System.out.println(data.getHeader().getOutOf());
		((ResponseHeader)dataOutParams).setStatus(ResponseHeader.STATUS_SUCCESS);
	}
}
