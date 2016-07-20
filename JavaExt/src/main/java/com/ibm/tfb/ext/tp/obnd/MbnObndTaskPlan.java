package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.MbnActionDataFileOutput;
import com.ibm.tfb.ext.action.MbnObndDataTableWatch;

public class MbnObndTaskPlan extends DPFTBaseTaskPlan {
	public MbnObndTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public MbnObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new MbnObndDataTableWatch());
		this.getActionList().add(new MbnActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
