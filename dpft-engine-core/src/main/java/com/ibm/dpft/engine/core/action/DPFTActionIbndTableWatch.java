package com.ibm.dpft.engine.core.action;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class DPFTActionIbndTableWatch extends DPFTActionTableWatch {
	private final static long sleep_time = 60000;
	
	public DPFTActionIbndTableWatch(){
		super();
		
		/*set watch intervals to 1 min*/
		this.setPostActionSleepTime(sleep_time);
	}
	
	@Override
	public void finish() throws DPFTRuntimeException {
		/*lock record by setting process status = 'R'*/
		DPFTInboundControlDboSet hSet = (DPFTInboundControlDboSet) this.getDataSet();
		hSet.run();
		super.finish();
	}

	@Override
	public DPFTConfig getDBConfig() {
		/*set Table watch db connection properties*/
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "H_INBOUND";
	}

	@Override
	public String getTableWatchCriteria() {
		/* Watch new insert records */
		return "process_status='" + GlobalConstants.DPFT_CTRL_STAT_INIT + "' and gk_flg='Y'";
	}

	@Override
	public String getTriggerKeyCol() {
		// TODO Auto-generated method stub
		return "chal_name";
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		//if no watch data, wait for next execution
		if(this.getDataSet().count() == 0)
			this.changeActionStatus(GlobalConstants.DPFT_ACTION_STAT_RUN);
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		DPFTInboundControlDboSet hSet = (DPFTInboundControlDboSet) this.getDataSet();
		hSet.error();		
	}

}
