package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicDataTableWatch;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class UcodActionDataTableWatch extends DPFTActionObndPeriodicDataTableWatch {
	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "O_USAGECODE";
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		
	}
	
}
