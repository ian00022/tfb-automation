package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicDataTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.dbo.DPFTTriggerMapDefDbo;

public class LtmFbObndDataTableWatch extends DPFTActionObndPeriodicDataTableWatch {

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "O_LTM";
	}
	
	@Override
	public String getTableWatchCriteria() {
		DPFTTriggerMapDefDbo data = (DPFTTriggerMapDefDbo) this.getInitialData();
		return data.getDataSelectCriteria(GlobalConstants.O_DATA_GK_EXCLUDE);
	}


}
