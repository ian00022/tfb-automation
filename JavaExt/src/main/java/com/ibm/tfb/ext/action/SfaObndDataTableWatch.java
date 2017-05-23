package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicDataTableWatch;

public class SfaObndDataTableWatch extends DPFTActionObndPeriodicDataTableWatch {
	@Override
	public String getTableWatchCriteria() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getTableWatchCriteria());
		sb.append(" chal_name='").append(this.getInitialData().getString("chal_name")).append("'");
		return sb.toString();
	}

	@Override
	public String getTableName() {
		return "O_SFA";
	}

}
