package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.QsmActionContactInfoDMWatch;
import com.ibm.tfb.ext.action.QsmActionDataTableWatch;

public class QsmMainTaskPlan extends DPFTBaseTaskPlan {
	public QsmMainTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public QsmMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new QsmActionDataTableWatch());
		this.getActionList().add(new QsmActionContactInfoDMWatch());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
