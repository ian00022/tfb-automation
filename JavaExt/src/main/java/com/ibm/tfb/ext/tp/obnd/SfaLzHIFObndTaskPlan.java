package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.LzActionObndDataTableWatch;
import com.ibm.tfb.ext.action.SfaLzHIFActionDataFileOutput;

public class SfaLzHIFObndTaskPlan extends DPFTBaseTaskPlan {
	public SfaLzHIFObndTaskPlan(String id) {
		super(id);
	}

	public SfaLzHIFObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new LzActionObndDataTableWatch());
		this.getActionList().add(new SfaLzHIFActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
