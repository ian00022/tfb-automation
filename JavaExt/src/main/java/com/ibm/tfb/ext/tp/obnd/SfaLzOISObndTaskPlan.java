package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.LzActionObndDataTableWatch;
import com.ibm.tfb.ext.action.SfaLzOISActionDataFileOutput;

public class SfaLzOISObndTaskPlan extends DPFTBaseTaskPlan {
	public SfaLzOISObndTaskPlan(String id) {
		super(id);
	}

	public SfaLzOISObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new LzActionObndDataTableWatch());
		this.getActionList().add(new SfaLzOISActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
