package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.FudActionDataFileOutput;

public class FudObndTaskPlan extends DPFTBaseTaskPlan {

	public FudObndTaskPlan(String id) {
		super(id);

	}

	public FudObndTaskPlan(DPFTBaseTaskPlan taskplan) {
		super(taskplan);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new FudActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}
}
