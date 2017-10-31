package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.QemActionDataFileOutput;

public class QemObndTaskPlan extends DPFTBaseTaskPlan {
	public QemObndTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}


	public QemObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new QemActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
