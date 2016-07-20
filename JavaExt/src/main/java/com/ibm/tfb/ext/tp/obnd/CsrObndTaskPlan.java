package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.CsrActionDataFileOutput;

public class CsrObndTaskPlan extends DPFTBaseTaskPlan {
	public CsrObndTaskPlan(String id) {
		super(id);
	}

	public CsrObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new CsrActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
