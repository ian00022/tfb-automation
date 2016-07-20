package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.PcbActionCustInfoDMWatch;
import com.ibm.tfb.ext.action.PcbActionDataTableWatch;

public class PcbMainTaskPlan extends DPFTBaseTaskPlan {
	public PcbMainTaskPlan(String id) {
		super(id);
	}

	public PcbMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new PcbActionDataTableWatch());
		this.getActionList().add(new PcbActionCustInfoDMWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
