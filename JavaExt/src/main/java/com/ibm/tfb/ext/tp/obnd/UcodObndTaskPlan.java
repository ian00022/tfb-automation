package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.UcodActionDataFileOutput;
import com.ibm.tfb.ext.action.UcodActionDataTableWatch;

public class UcodObndTaskPlan extends DPFTBaseTaskPlan {
	public UcodObndTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public UcodObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new UcodActionDataTableWatch());
		this.getActionList().add(new UcodActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
