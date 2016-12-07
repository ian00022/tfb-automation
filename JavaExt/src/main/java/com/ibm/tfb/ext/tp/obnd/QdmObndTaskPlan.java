package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.action.DPFTActionObndDataTableWatch;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.QdmActionDataFileOutput;

public class QdmObndTaskPlan extends DPFTBaseTaskPlan {

	public QdmObndTaskPlan(String id) {
		super(id);
	}

	public QdmObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new DPFTActionObndDataTableWatch());
		this.getActionList().add(new QdmActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}
}
