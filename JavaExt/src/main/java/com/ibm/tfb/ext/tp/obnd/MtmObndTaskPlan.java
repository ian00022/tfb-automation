package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.MtmActionDataFileOutput;

public class MtmObndTaskPlan extends DPFTBaseTaskPlan {
	public MtmObndTaskPlan(String id) {
		super(id);
	}

	public MtmObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new MtmActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
