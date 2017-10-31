package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.QemSvActionDataFileOutput;
import com.ibm.tfb.ext.action.QemSvObndDataTableWatch;

public class QemSvObndTaskPlan extends DPFTBaseTaskPlan {

	public QemSvObndTaskPlan(String id) {
		super(id);
	}

	public QemSvObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new QemSvObndDataTableWatch());
		this.getActionList().add(new QemSvActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
