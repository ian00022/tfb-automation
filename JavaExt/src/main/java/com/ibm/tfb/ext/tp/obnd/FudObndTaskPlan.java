package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.FudActionDataFileOutput;
import com.ibm.tfb.ext.action.FudObndDataTableWatch;

public class FudObndTaskPlan extends DPFTBaseTaskPlan {

	public FudObndTaskPlan(String id) {
		super(id);

	}

	public FudObndTaskPlan(DPFTBaseTaskPlan taskplan) {
		super(taskplan);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new FudObndDataTableWatch());
		this.getActionList().add(new FudActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}
}
