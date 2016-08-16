package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicDataTableWatch;
import com.ibm.tfb.ext.common.TFBConstants;

public class SfaLzHouseLoanObndDataTableWatch extends DPFTActionObndPeriodicDataTableWatch {

	@Override
	public String getTableName() {
		return "O_SFA";
	}
	
	@Override
	public String getTableWatchCriteria() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getTableWatchCriteria()).append(" and lead_type='" + TFBConstants.SFA_LZ_LEADTYPE_HL + "'");
		return sb.toString();
	}

}
