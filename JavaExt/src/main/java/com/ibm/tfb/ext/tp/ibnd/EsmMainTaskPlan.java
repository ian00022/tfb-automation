package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.EsmActionContactInfoDMWatch;
import com.ibm.tfb.ext.action.EsmActionDataTableWatch;

public class EsmMainTaskPlan extends DPFTBaseTaskPlan {
	public EsmMainTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}
	

	public EsmMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new EsmActionDataTableWatch());
		this.getActionList().add(new EsmActionContactInfoDMWatch());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
