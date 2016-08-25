package com.ibm.tfb.ext.tp.other;

import com.ibm.dpft.engine.core.action.DPFTActionSleep;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.SfaActionCapacityFileWatch;

public class SfaCapacityTaskPlan extends DPFTBaseTaskPlan {
	public SfaCapacityTaskPlan(String id) {
		super(id);
	}

	public SfaCapacityTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new SfaActionCapacityFileWatch());
		this.getActionList().add(new DPFTActionSleep(10000));
	}

	@Override
	public boolean isRecurring() {
		return true;
	}

}
