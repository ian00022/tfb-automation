package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.NtmActionCustContInfoDMWatch;
import com.ibm.tfb.ext.action.NtmActionCustInfoDMWatch;
import com.ibm.tfb.ext.action.NtmActionDataTableWatch;

public class NtmMainTaskPlan extends DPFTBaseTaskPlan {
	public NtmMainTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public NtmMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new NtmActionDataTableWatch());
		this.getActionList().add(new NtmActionCustInfoDMWatch());
		this.getActionList().add(new NtmActionCustContInfoDMWatch());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
