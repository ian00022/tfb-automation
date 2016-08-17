package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.CtmLzActionDataTableWatch;

public class CtmLzCCMainTaskPlan extends DPFTBaseTaskPlan {
	public CtmLzCCMainTaskPlan(String id) {
		super(id);
	}

	public CtmLzCCMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new CtmLzActionDataTableWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
