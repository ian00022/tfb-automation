package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.CtmLzActionDataFileOutput;
import com.ibm.tfb.ext.action.LzActionObndDataTableWatch;

public class CtmLzCCObndTaskPlan extends DPFTBaseTaskPlan {
	public CtmLzCCObndTaskPlan(String id) {
		super(id);
	}

	public CtmLzCCObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new LzActionObndDataTableWatch());
		this.getActionList().add(new CtmLzActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
