package com.ibm.dpft.engine.core.action;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class DPFTActionObndTpWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		// TODO Auto-generated method stub
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "DPFT_OBND_TP_DEF";
	}

	@Override
	public String getTableWatchCriteria() {
		// TODO Auto-generated method stub
		return "obnd_rule='" + GlobalConstants.DPFT_TP_OBND_RULE_CHAL + "' and trg_time is not null";
	}

	@Override
	public String getTriggerKeyCol() {
		// TODO Auto-generated method stub
		return "chal_name";
	}

	@Override
	public void postAction() {
		this.setResultSet(getDataSet());
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTActionException {
		// TODO Auto-generated method stub
		
	}

}
