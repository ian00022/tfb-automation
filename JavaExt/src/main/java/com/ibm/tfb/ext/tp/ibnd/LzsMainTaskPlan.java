package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.LzsActionCtmContactInfoWatch;
import com.ibm.tfb.ext.action.LzsActionDataTableWatch;
import com.ibm.tfb.ext.action.LzsActionSfaContactInfoWatch;

public class LzsMainTaskPlan extends DPFTBaseTaskPlan {
	public LzsMainTaskPlan(String id) {
		super(id);
	}

	public LzsMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new LzsActionDataTableWatch());
		this.getActionList().add(new LzsActionCtmContactInfoWatch());
		this.getActionList().add(new LzsActionSfaContactInfoWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
