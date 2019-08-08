package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.WmsActionResDataWatchEFL;

public class WmsResMainTaskPlanEFL extends DPFTBaseTaskPlan {
	public WmsResMainTaskPlanEFL(String id) {
		super(id);
	}

	public WmsResMainTaskPlanEFL(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new WmsActionResDataWatchEFL());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
