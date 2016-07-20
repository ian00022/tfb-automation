package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.EcbActionDataFileOutput;

public class EcbObndTaskPlan extends DPFTBaseTaskPlan {
	public EcbObndTaskPlan(String id) {
		super(id);
	}

	public EcbObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new EcbActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
