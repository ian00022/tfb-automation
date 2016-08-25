package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.SfaLzActionDataTableWatch;

public class SfaLzMainTaskPlan extends DPFTBaseTaskPlan {
	public SfaLzMainTaskPlan(String id) {
		super(id);
	}

	public SfaLzMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new SfaLzActionDataTableWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
