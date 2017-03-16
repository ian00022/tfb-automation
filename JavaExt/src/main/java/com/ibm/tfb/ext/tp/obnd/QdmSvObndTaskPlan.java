package com.ibm.tfb.ext.tp.obnd;

import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.tfb.ext.action.QdmSvActionDataFileOutput;
import com.ibm.tfb.ext.action.QdmSvObndDataTableWatch;

public class QdmSvObndTaskPlan extends DPFTBaseTaskPlan {

	public QdmSvObndTaskPlan(String id) {
		super(id);
	}

	public QdmSvObndTaskPlan(DPFTBaseTaskPlan tp) {
		super(tp);
	}

	@Override
	public void setActionsForPlan() {
		this.getActionList().add(new QdmSvObndDataTableWatch());
		this.getActionList().add(new QdmSvActionDataFileOutput());
	}

	@Override
	public boolean isRecurring() {
		return false;
	}

}
