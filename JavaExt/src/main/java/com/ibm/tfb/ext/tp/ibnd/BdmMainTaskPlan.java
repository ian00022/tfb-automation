package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.BdmActionCustContInfoDMWatch;
import com.ibm.tfb.ext.action.BdmActionCustInfoDMWatch;
import com.ibm.tfb.ext.action.BdmActionDataTableWatch;

public class BdmMainTaskPlan extends DPFTBaseTaskPlan {
	
	public BdmMainTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public BdmMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new BdmActionDataTableWatch());
		this.getActionList().add(new BdmActionCustInfoDMWatch());
		this.getActionList().add(new BdmActionCustContInfoDMWatch());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
