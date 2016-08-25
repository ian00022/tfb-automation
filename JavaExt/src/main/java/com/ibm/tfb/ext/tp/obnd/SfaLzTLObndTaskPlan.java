package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.LzActionObndDataTableWatch;
import com.ibm.tfb.ext.action.SfaLzTLActionDataFileOutput;

public class SfaLzTLObndTaskPlan extends DPFTBaseTaskPlan {
	public SfaLzTLObndTaskPlan(String id) {
		super(id);
	}


	public SfaLzTLObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new LzActionObndDataTableWatch());
		this.getActionList().add(new SfaLzTLActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
