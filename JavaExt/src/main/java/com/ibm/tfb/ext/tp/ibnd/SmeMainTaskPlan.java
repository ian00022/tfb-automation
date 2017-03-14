package com.ibm.tfb.ext.tp.ibnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.SmeActionCustContInfoDMWatch;
import com.ibm.tfb.ext.action.SmeActionCustInfoDMWatch;
import com.ibm.tfb.ext.action.SmeActionDataTableWatch;

public class SmeMainTaskPlan extends DPFTBaseTaskPlan {
	public SmeMainTaskPlan(String id) {
		super(id);
	}

	public SmeMainTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new SmeActionDataTableWatch());//取D檔的資料
		this.getActionList().add(new SmeActionCustInfoDMWatch());//串個資
		this.getActionList().add(new SmeActionCustContInfoDMWatch());//
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
