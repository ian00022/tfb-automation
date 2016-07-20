package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.EdmActionDataTableWatch;
import com.ibm.tfb.ext.action.EdmActionPersonalDataTableWatch;

public class EdmMainTaskPlan extends DPFTBaseTaskPlan {

	public EdmMainTaskPlan(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}
	
	public EdmMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void setActionsForPlan() {
		// TODO Auto-generated method stub
		this.getActionList().add(new EdmActionDataTableWatch());
		this.getActionList().add(new EdmActionPersonalDataTableWatch());
	}

	@Override
	public boolean isRecurring() {
		// TODO Auto-generated method stub
		return false;
	}

}
