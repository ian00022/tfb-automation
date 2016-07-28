package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.ErbActionDataFileOutput;

public class ErbObndTaskPlan extends DPFTBaseTaskPlan {
	public ErbObndTaskPlan(String id){
		super(id);
	}


	public ErbObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new ErbActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
