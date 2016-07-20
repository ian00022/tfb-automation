package com.ibm.dpft.engine.core.taskplan;

import com.ibm.dpft.engine.core.action.DPFTActionObndTableWatch;
import com.ibm.dpft.engine.core.action.DPFTActionObndTrigger;

public class DPFTDataOutboundWatcher extends DPFTBaseTaskPlan {
	public DPFTDataOutboundWatcher(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}


	public DPFTDataOutboundWatcher(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new DPFTActionObndTableWatch());
		this.getActionList().add(new DPFTActionObndTrigger());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return true;
	}

}
