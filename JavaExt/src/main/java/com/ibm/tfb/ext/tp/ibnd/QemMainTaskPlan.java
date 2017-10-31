package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.QemActionContactInfoDMWatch;
import com.ibm.tfb.ext.action.QemActionDataTableWatch;

public class QemMainTaskPlan extends DPFTBaseTaskPlan {
	public QemMainTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}
	

	public QemMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new QemActionDataTableWatch());
		this.getActionList().add(new QemActionContactInfoDMWatch());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
