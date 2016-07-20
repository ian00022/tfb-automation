package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.LtmFbActionDataFileOutput;
import com.ibm.tfb.ext.action.LtmFbObndDataTableWatch;

public class LtmFbObndTaskPlan extends DPFTBaseTaskPlan {
	public LtmFbObndTaskPlan(String id) {
		super(id);
	}

	public LtmFbObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new LtmFbObndDataTableWatch());
		this.getActionList().add(new LtmFbActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
