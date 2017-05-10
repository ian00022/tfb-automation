package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.CsrLzTmActionDataTableWatch;

public class CsrLzTmMainTaskPlan extends DPFTBaseTaskPlan {
	public CsrLzTmMainTaskPlan(String id) {
		super(id);
	}

	public CsrLzTmMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new CsrLzTmActionDataTableWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
