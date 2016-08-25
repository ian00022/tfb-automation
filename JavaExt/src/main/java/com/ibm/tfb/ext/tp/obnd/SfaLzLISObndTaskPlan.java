package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.LzActionObndDataTableWatch;
import com.ibm.tfb.ext.action.SfaLzLISActionDataFileOutput;

public class SfaLzLISObndTaskPlan extends DPFTBaseTaskPlan {
	public SfaLzLISObndTaskPlan(String id) {
		super(id);
	}

	public SfaLzLISObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new LzActionObndDataTableWatch());
		this.getActionList().add(new SfaLzLISActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
