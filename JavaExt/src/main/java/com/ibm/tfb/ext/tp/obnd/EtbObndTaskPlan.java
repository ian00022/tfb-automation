package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.EtbActionDataFileOutput;
import com.ibm.tfb.ext.action.EtbObndDataTableWatch;

public class EtbObndTaskPlan extends DPFTBaseTaskPlan {
	public EtbObndTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public EtbObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new EtbObndDataTableWatch());
		this.getActionList().add(new EtbActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
