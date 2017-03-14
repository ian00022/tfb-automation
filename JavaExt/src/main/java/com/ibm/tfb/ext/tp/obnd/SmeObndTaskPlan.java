package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.SmeActionDataFileOutput;

public class SmeObndTaskPlan extends DPFTBaseTaskPlan {
	public SmeObndTaskPlan(String id) {
		super(id);
	}

	public SmeObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new SmeActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
