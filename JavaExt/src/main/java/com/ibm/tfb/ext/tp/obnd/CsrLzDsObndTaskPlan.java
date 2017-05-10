package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.CsrLzDsActionDataFileOutput;
import com.ibm.tfb.ext.action.LzActionObndDataTableWatch;

public class CsrLzDsObndTaskPlan extends DPFTBaseTaskPlan {
	public CsrLzDsObndTaskPlan(String id) {
		super(id);
	}

	public CsrLzDsObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new LzActionObndDataTableWatch());
		this.getActionList().add(new CsrLzDsActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
