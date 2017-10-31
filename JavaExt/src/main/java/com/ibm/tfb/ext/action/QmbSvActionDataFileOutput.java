package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicFileOutput;
import com.ibm.dpft.engine.core.util.DPFTFileFTPUtil;
import com.ibm.dpft.engine.core.util.DPFTFileFormatter;



public class QmbSvActionDataFileOutput extends DPFTActionObndPeriodicFileOutput {
	@Override
	public String getChannelName() {
		// TODO Auto-generated method stub
		return "QMB_Q";
	}

	@Override
	public boolean needNotification() {
		return true;
	}

	@Override
	public HashMap<DPFTFileFormatter, DPFTFileFTPUtil> getAdditionalDataFormatters(){
		return null;

	}

}