package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.ErbActionDataFileOutput;
import com.ibm.tfb.ext.action.ErbActionObndDataTableWatch;

public class ErbObndTaskPlan extends DPFTBaseTaskPlan {
	public ErbObndTaskPlan(String id){
		super(id);
	}


	public ErbObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new ErbActionObndDataTableWatch());
		this.getActionList().add(new ErbActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
