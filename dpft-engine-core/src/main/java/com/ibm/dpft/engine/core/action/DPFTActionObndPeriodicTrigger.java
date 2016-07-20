package com.ibm.dpft.engine.core.action;

import com.ibm.dpft.engine.core.DPFTTaskRunner;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.dbo.DPFTTriggerMapDefDbo;
import com.ibm.dpft.engine.core.dbo.DPFTTriggerMapDefDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class DPFTActionObndPeriodicTrigger extends DPFTActionTaskTrigger {

	@Override
	public DPFTTriggerMapDefDboSet getTriggerMap() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void action() throws DPFTRuntimeException {
		getRunnerlist().clear();
		DPFTTriggerMapDefDboSet tmapSet = (DPFTTriggerMapDefDboSet) this.getPreviousAction().getResultSet();
		for(int i = 0; i < tmapSet.count(); i++){
			DPFTTriggerMapDefDbo dbo = (DPFTTriggerMapDefDbo) tmapSet.getDbo(i);
			String tpid = dbo.getString("planid");
			if(tpid != null && !tpid.isEmpty() && dbo.canInvoke()){
				DPFTTaskRunner thread = (DPFTTaskRunner) DPFTTaskRunner.currentThread();
				DPFTTaskRunner new_runner = thread.getMgr().createNewRunnerByTaskPlanID(tpid);
				new_runner.setInitialData(dbo);
				getRunnerlist().add(new_runner);
			}
		}
		logRunner();
		this.changeActionStatus(GlobalConstants.DPFT_ACTION_STAT_COMP);
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTActionException {
		
	}
}
