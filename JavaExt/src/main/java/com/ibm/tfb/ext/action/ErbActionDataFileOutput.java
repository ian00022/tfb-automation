package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicFileOutput;
import com.ibm.dpft.engine.core.util.DPFTFileFTPUtil;
import com.ibm.dpft.engine.core.util.DPFTFileFormatter;

public class ErbActionDataFileOutput extends DPFTActionObndPeriodicFileOutput {

	@Override
	public HashMap<DPFTFileFormatter, DPFTFileFTPUtil> getAdditionalDataFormatters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getChannelName() {
		// TODO Auto-generated method stub
		return "ERB";
	}

	@Override
	public boolean needNotification() {
		return true;
	}

}
