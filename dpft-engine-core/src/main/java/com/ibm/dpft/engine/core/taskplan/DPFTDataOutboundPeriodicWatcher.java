package com.ibm.dpft.engine.core.taskplan;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicTrigger;
import com.ibm.dpft.engine.core.action.DPFTActionObndTpWatch;
import com.ibm.dpft.engine.core.action.DPFTActionSleep;

public class DPFTDataOutboundPeriodicWatcher extends DPFTBaseTaskPlan {
	private final static long sleep_time = 6000000;
	
	public DPFTDataOutboundPeriodicWatcher(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public DPFTDataOutboundPeriodicWatcher(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new DPFTActionObndTpWatch());
		this.getActionList().add(new DPFTActionObndPeriodicTrigger());
		this.getActionList().add(new DPFTActionSleep(sleep_time));
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return true;
	}

}
