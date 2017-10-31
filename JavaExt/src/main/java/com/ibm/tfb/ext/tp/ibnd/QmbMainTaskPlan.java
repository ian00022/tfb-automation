package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.QmbActionDataTableWatch;

public class QmbMainTaskPlan extends DPFTBaseTaskPlan {
	
	public QmbMainTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public QmbMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new QmbActionDataTableWatch());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
