package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.FudActionBusinessDayTableWatch;
import com.ibm.tfb.ext.action.FudActionDataTableWatch;

public class FudMainTaskPlan extends DPFTBaseTaskPlan {

	public FudMainTaskPlan(String id) {
		super(id);
	}
	
	public FudMainTaskPlan(DPFTBaseTaskPlan taskplan) {
		super(taskplan);
	}

	@Override
	public void setActionsForPlan() {
		// get D_FILE
		this.getActionList().add(new FudActionDataTableWatch());
		this.getActionList().add(new FudActionBusinessDayTableWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
