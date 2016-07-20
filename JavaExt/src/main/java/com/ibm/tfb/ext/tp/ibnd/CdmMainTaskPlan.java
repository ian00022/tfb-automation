package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.CdmActionCustContInfoDMWatch;
import com.ibm.tfb.ext.action.CdmActionCustInfoDMWatch;
import com.ibm.tfb.ext.action.CdmActionDataTableWatch;

public class CdmMainTaskPlan extends DPFTBaseTaskPlan {
	public CdmMainTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public CdmMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new CdmActionDataTableWatch());
		this.getActionList().add(new CdmActionCustInfoDMWatch());
		this.getActionList().add(new CdmActionCustContInfoDMWatch());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
