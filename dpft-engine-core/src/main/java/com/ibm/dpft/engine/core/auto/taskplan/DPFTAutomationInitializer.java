package com.ibm.dpft.engine.core.auto.taskplan;

import com.ibm.dpft.engine.core.auto.action.DPFTActionAutomationReadScript;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;

public class DPFTAutomationInitializer extends DPFTBaseTaskPlan {
	public DPFTAutomationInitializer(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public DPFTAutomationInitializer(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new DPFTActionAutomationReadScript());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
