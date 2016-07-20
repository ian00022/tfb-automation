package com.ibm.dpft.engine.core.auto.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class DPFTAutomationVariablesSet extends DPFTDboSet {

	public DPFTAutomationVariablesSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new DPFTAutomationVariables(dboname, d, this);
	}

	public void addNewVariable(String group_id, String var, String value) throws DPFTRuntimeException {
		DPFTAutomationVariables new_vars = (DPFTAutomationVariables) this.add();
		new_vars.setValue("group_id", group_id);
		new_vars.setValue("var", var);
		new_vars.setValue("value", value);
	}

}
