package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicFileOutput;
import com.ibm.dpft.engine.core.util.DPFTFileFTPUtil;
import com.ibm.dpft.engine.core.util.DPFTFileFormatter;

public class SfaActionDataFileOutput extends DPFTActionObndPeriodicFileOutput {

	@Override
	public HashMap<DPFTFileFormatter, DPFTFileFTPUtil> getAdditionalDataFormatters() {
		return null;
	}

	@Override
	public boolean needNotification() {
		return true;
	}

	@Override
	public String getChannelName() {
		return "SFA";
	}


}
