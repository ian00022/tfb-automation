package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.EcbActionCustInfoDMWatch;
import com.ibm.tfb.ext.action.EcbActionDataTableWatch;

public class EcbMainTaskPlan extends DPFTBaseTaskPlan {
	public EcbMainTaskPlan(String id) {
		super(id);
	}

	public EcbMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new EcbActionDataTableWatch());
		this.getActionList().add(new EcbActionCustInfoDMWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
