package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.SfaActionDataFileOutput;
import com.ibm.tfb.ext.action.SfaObndDataTableWatch;

public class SfaObndTaskPlan extends DPFTBaseTaskPlan {
	public SfaObndTaskPlan(String id) {
		super(id);
	}

	public SfaObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new SfaObndDataTableWatch());
		this.getActionList().add(new SfaActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
