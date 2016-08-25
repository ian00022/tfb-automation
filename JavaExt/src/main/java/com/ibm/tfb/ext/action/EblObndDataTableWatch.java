package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicDataTableWatch;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTMessage;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBConstants;
import com.ibm.tfb.ext.dbo.EblOutboundDboSet;

public class EblObndDataTableWatch extends DPFTActionObndPeriodicDataTableWatch {

	@Override
	public String getTableName() {
		return "O_EBL";
	}
	
	@Override
	public String getTableWatchCriteria() {
		String where = super.getTableWatchCriteria();
		return where + " and bill_month='" + DPFTEngine.getSystemProperties(TFBConstants.EBL_PROP_BILL_MONTH) + "'";
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		Object[] params = {getInitialData().getString("chal_name")};
		DPFTUtil.pushNotification(
				 new DPFTMessage("CUSTOM", "TFB00010I", params)
		);

		EblOutboundDboSet oEblSet = (EblOutboundDboSet) this.getDataSet();
		oEblSet.processOutputRecords();
		this.setResultSet(getDataSet());
	}
}
