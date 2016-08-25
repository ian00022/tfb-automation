package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.LzActionObndDataTableWatch;
import com.ibm.tfb.ext.action.SfaLzHLActionDataFileOutput;

public class SfaLzHLObndTaskPlan extends DPFTBaseTaskPlan {
	public SfaLzHLObndTaskPlan(String id) {
		super(id);
	}


	public SfaLzHLObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new LzActionObndDataTableWatch());
		this.getActionList().add(new SfaLzHLActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
