package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicDataTableWatch;

public class QmbSvObndDataTableWatch extends DPFTActionObndPeriodicDataTableWatch {

	@Override
	public String getTableName() {
		return "O_QMB";
	}

}