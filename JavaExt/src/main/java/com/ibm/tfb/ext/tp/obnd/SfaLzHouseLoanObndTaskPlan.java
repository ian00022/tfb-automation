package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.SfaLzHouseLoanActionDataFileOutput;
import com.ibm.tfb.ext.action.SfaLzHouseLoanObndDataTableWatch;

public class SfaLzHouseLoanObndTaskPlan extends DPFTBaseTaskPlan {
	public SfaLzHouseLoanObndTaskPlan(String id) {
		super(id);
	}

	public SfaLzHouseLoanObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new SfaLzHouseLoanObndDataTableWatch());
		this.getActionList().add(new SfaLzHouseLoanActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
