package com.ibm.dpft.engine.core.action;

import com.ibm.dpft.engine.core.common.GlobalConstants;

public class DPFTActionInstaObndTableWatch extends DPFTActionObndTableWatch {
	@Override
	public String getTableWatchCriteria() {
		return "process_status='" + GlobalConstants.DPFT_OBND_STAT_STAGE + "' and ftp_time is null and insta=1";
	}
}
