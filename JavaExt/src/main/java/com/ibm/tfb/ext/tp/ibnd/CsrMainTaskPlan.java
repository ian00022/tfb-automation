package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.CsrActionDataTableWatch;

public class CsrMainTaskPlan extends DPFTBaseTaskPlan {
	public CsrMainTaskPlan(String id) {
		super(id);
	}

	public CsrMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new CsrActionDataTableWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
