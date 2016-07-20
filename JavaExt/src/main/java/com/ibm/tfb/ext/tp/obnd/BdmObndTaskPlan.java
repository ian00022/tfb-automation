package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.BdmActionDataFileOutput;

public class BdmObndTaskPlan extends DPFTBaseTaskPlan {
	public BdmObndTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public BdmObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new BdmActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
