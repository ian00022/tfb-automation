package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.WmsActionResDataWatchMVP;

public class WmsResMainTaskPlanMVP extends DPFTBaseTaskPlan {
	public WmsResMainTaskPlanMVP(String id) {
		super(id);
	}

	public WmsResMainTaskPlanMVP(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new WmsActionResDataWatchMVP());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
