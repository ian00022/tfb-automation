package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.CtmActionDataFileOutput;

public class CtmObndTaskPlan extends DPFTBaseTaskPlan {
	public CtmObndTaskPlan(String id) {
		super(id);
	}


	public CtmObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new CtmActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
