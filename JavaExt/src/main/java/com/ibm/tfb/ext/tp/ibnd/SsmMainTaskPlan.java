package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.SsmActionContactInfoDMWatch;
import com.ibm.tfb.ext.action.SsmActionDataTableWatch;

public class SsmMainTaskPlan extends DPFTBaseTaskPlan {
	public SsmMainTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public SsmMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new SsmActionDataTableWatch());
		this.getActionList().add(new SsmActionContactInfoDMWatch());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
