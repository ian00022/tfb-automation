package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.ZackActionDataTableWatch;
import com.ibm.tfb.ext.action.ZackActionPersonalDataTableWatch;

public class ZackMainTaskPlan extends DPFTBaseTaskPlan {

	public ZackMainTaskPlan(String plan_id) {
		super(plan_id);
	}
	
	public ZackMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new ZackActionDataTableWatch());
		this.getActionList().add(new ZackActionPersonalDataTableWatch());

	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
