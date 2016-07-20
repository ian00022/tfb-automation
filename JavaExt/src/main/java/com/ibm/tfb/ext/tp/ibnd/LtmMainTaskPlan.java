package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.LtmActionDataTableWatch;

public class LtmMainTaskPlan extends DPFTBaseTaskPlan {
	public LtmMainTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public LtmMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new LtmActionDataTableWatch());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
