package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.QmbSvActionDataFileOutput;
import com.ibm.tfb.ext.action.QmbSvObndDataTableWatch;

public class QmbSvObndTaskPlan extends DPFTBaseTaskPlan {

	public QmbSvObndTaskPlan(String id) {
		super(id);
	}

	public QmbSvObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new QmbSvObndDataTableWatch());
		this.getActionList().add(new QmbSvActionDataFileOutput());
		
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
