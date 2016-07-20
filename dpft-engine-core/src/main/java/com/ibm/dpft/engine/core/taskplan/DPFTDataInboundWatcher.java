package com.ibm.dpft.engine.core.taskplan;

import com.ibm.dpft.engine.core.action.DPFTActionIbndTrigger;
import com.ibm.dpft.engine.core.action.DPFTActionIbndTableWatch;

public class DPFTDataInboundWatcher extends DPFTBaseTaskPlan {

	public DPFTDataInboundWatcher(String plan_id) {
		super(plan_id);
		// TODO Auto-generated constructor stub
	}
	
	public DPFTDataInboundWatcher(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new DPFTActionIbndTableWatch());
		this.getActionList().add(new DPFTActionIbndTrigger());
		
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return true;
	}

}
