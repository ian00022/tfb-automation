package com.ibm.dpft.engine.core.action;

import com.ibm.dpft.engine.core.common.GlobalConstants;

public class DPFTActionInstaIbndTableWatch extends DPFTActionIbndTableWatch {
	@Override
	public String getTableWatchCriteria() {
		/* Watch new insert records */
		return "process_status='" + GlobalConstants.DPFT_CTRL_STAT_INIT + "' and insta=1";
	}
}
