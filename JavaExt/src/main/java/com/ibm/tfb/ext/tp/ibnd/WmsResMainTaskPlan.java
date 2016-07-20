package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.WmsActionResDataWatch;

public class WmsResMainTaskPlan extends DPFTBaseTaskPlan {
	public WmsResMainTaskPlan(String id) {
		super(id);
	}

	public WmsResMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new WmsActionResDataWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
