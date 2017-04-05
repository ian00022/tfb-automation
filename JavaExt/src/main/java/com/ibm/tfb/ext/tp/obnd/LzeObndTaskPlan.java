package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.EsmActionDataFileOutput;

public class LzeObndTaskPlan extends DPFTBaseTaskPlan {
	public LzeObndTaskPlan(String id) {
		super(id);
	}

	public LzeObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new EsmActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
