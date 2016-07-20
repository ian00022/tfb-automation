package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.CtmActionCustContInfoDMWatch;
import com.ibm.tfb.ext.action.CtmActionCustInfoDMWatch;
import com.ibm.tfb.ext.action.CtmActionDataTableWatch;

public class CtmMainTaskPlan extends DPFTBaseTaskPlan {
	public CtmMainTaskPlan(String id) {
		super(id);
	}

	public CtmMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new CtmActionDataTableWatch());
		this.getActionList().add(new CtmActionCustInfoDMWatch());
		this.getActionList().add(new CtmActionCustContInfoDMWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
