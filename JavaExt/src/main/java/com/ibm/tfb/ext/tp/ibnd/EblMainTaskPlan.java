package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.EblActionDataTableWatch;

public class EblMainTaskPlan extends DPFTBaseTaskPlan {
	public EblMainTaskPlan(String id) {
		super(id);
	}

	public EblMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new EblActionDataTableWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
