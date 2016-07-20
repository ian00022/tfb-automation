package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.PcbActionDataFileOutput;

public class PcbObndTaskPlan extends DPFTBaseTaskPlan {
	public PcbObndTaskPlan(String id) {
		super(id);
	}

	public PcbObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new PcbActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
