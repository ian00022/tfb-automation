package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.LzeActionCtmContactInfoWatch;
import com.ibm.tfb.ext.action.LzeActionDataTableWatch;
import com.ibm.tfb.ext.action.LzeActionSfaContactInfoWatch;

public class LzeMainTaskPlan extends DPFTBaseTaskPlan {
	public LzeMainTaskPlan(String id) {
		super(id);
	}

	public LzeMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new LzeActionDataTableWatch());
		this.getActionList().add(new LzeActionCtmContactInfoWatch());
		this.getActionList().add(new LzeActionSfaContactInfoWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
