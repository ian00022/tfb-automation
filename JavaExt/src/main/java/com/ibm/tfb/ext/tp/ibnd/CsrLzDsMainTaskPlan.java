package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.CsrLzDsActionDataTableWatch;

public class CsrLzDsMainTaskPlan extends DPFTBaseTaskPlan {
	public CsrLzDsMainTaskPlan(String id) {
		super(id);
	}

	public CsrLzDsMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new CsrLzDsActionDataTableWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
