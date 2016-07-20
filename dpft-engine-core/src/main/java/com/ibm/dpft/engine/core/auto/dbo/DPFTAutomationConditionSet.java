package com.ibm.dpft.engine.core.auto.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class DPFTAutomationConditionSet extends DPFTDboSet {

	public DPFTAutomationConditionSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new DPFTAutomationCondition(dboname, d, this);
	}

	public void addNewCondition(String group_id, String condition_id, String condition) throws DPFTRuntimeException {
		DPFTAutomationCondition new_cond = (DPFTAutomationCondition) this.add();
		new_cond.setValue("group_id", group_id);
		new_cond.setValue("condition_id", condition_id);
		new_cond.setValue("condition", condition);
	}

	public boolean isAllConditionsMatched() throws DPFTRuntimeException {
		boolean result = true;
		for(int i = 0; i < count(); i++){
			DPFTAutomationCondition cnd = (DPFTAutomationCondition) this.getDbo(i);
			if(!cnd.isConditionMatched())
				result = false;
		}
		return result;
	}

}
