package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.QsmSvActionDataFileOutput;
import com.ibm.tfb.ext.action.QsmSvObndDataTableWatch;

public class QsmSvObndTaskPlan extends DPFTBaseTaskPlan {

	public QsmSvObndTaskPlan(String id) {
		super(id);
	}

	public QsmSvObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new QsmSvObndDataTableWatch());
		this.getActionList().add(new QsmSvActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
