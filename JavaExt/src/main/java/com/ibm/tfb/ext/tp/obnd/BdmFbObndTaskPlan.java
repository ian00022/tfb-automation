package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.BdmFbActionDataFileOutput;
import com.ibm.tfb.ext.action.BdmFbObndDataTableWatch;

public class BdmFbObndTaskPlan extends DPFTBaseTaskPlan {
	public BdmFbObndTaskPlan(String id) {
		super(id);
	}


	public BdmFbObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new BdmFbObndDataTableWatch());
		this.getActionList().add(new BdmFbActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
