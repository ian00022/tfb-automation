package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicDataTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.dbo.DPFTTriggerMapDefDbo;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

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

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		
	}

}
