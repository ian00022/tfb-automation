package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.CsrLzTmActionDataFileOutput;
import com.ibm.tfb.ext.action.LzActionObndDataTableWatch;

public class CsrLzTmObndTaskPlan extends DPFTBaseTaskPlan {
	public CsrLzTmObndTaskPlan(String id) {
		super(id);
	}

	public CsrLzTmObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new LzActionObndDataTableWatch());
		this.getActionList().add(new CsrLzTmActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
