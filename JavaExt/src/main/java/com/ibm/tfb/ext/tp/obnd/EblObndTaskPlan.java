package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.EblActionDataFileOutput;
import com.ibm.tfb.ext.action.EblObndDataTableWatch;

public class EblObndTaskPlan extends DPFTBaseTaskPlan {
	public EblObndTaskPlan(String id) {
		super(id);
	}

	public EblObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new EblObndDataTableWatch());
		this.getActionList().add(new EblActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
