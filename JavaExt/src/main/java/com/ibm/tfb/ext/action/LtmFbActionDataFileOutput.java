package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicFileOutput;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.meta.DPFTFileMetaData;
import com.ibm.dpft.engine.core.util.DPFTFileFTPUtil;
import com.ibm.dpft.engine.core.util.DPFTFileFormatter;
import com.ibm.tfb.ext.util.LTMReportGKFileFormatter;

public class LtmFbActionDataFileOutput extends DPFTActionObndPeriodicFileOutput {

	@Override
	public HashMap<DPFTFileFormatter, DPFTFileFTPUtil> getAdditionalDataFormatters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getChannelName() {
		// TODO Auto-generated method stub
		return "LTM_FB";
	}
	
	@Override
	public DPFTFileFormatter getFileFormatter() throws DPFTRuntimeException {
		return new LTMReportGKFileFormatter(new DPFTFileMetaData(meta), new DPFTFileMetaData(meta, dicSet));
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean needNotification() {
		return false;
	}

}
