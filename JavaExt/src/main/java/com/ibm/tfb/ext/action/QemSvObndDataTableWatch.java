package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicDataTableWatch;

public class QemSvObndDataTableWatch extends DPFTActionObndPeriodicDataTableWatch {

	@Override
	public String getTableName() {
		return "O_QEM";
	}

}