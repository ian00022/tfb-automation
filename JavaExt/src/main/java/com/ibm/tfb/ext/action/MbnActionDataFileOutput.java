package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicFileOutput;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileFTPUtil;
import com.ibm.dpft.engine.core.util.DPFTFileFormatter;

public class MbnActionDataFileOutput extends DPFTActionObndPeriodicFileOutput {

	@Override
	public HashMap<DPFTFileFormatter, DPFTFileFTPUtil> getAdditionalDataFormatters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getChannelName() {
		// TODO Auto-generated method stub
		return "MBN";
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean needNotification() {
		return true;
	}


}
