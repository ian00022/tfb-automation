package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.QdmActionDataTableWatch;
import com.ibm.tfb.ext.action.QdmActionPersonalDataTableWatch;

public class QdmMainTaskPlan extends DPFTBaseTaskPlan {

	public QdmMainTaskPlan(String id) {
		super(id);
	}
	
	public QdmMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}


	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new QdmActionDataTableWatch());
		this.getActionList().add(new QdmActionPersonalDataTableWatch());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
