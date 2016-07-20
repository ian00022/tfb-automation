package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.MtmActionCustContInfoDMWatch;
import com.ibm.tfb.ext.action.MtmActionCustInfoDMWatch;
import com.ibm.tfb.ext.action.MtmActionDataTableWatch;

public class MtmMainTaskPlan extends DPFTBaseTaskPlan {
	public MtmMainTaskPlan(String id) {
		super(id);
	}

	public MtmMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new MtmActionDataTableWatch());
		this.getActionList().add(new MtmActionCustInfoDMWatch());
		this.getActionList().add(new MtmActionCustContInfoDMWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
