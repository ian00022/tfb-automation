package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.LtmActionDataFileOutput;

public class LtmObndTaskPlan extends DPFTBaseTaskPlan {
	public LtmObndTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public LtmObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new LtmActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
