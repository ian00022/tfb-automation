package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.SfaActionDataTableWatch;

public class SfaMainTaskPlan extends DPFTBaseTaskPlan {
	public SfaMainTaskPlan(String id) {
		super(id);
	}

	public SfaMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new SfaActionDataTableWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
