package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.ErbActionDataTableWatch;

public class ErbMainTaskPlan extends DPFTBaseTaskPlan {
	public ErbMainTaskPlan(String id) {
		super(id);
	}

	public ErbMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new ErbActionDataTableWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
