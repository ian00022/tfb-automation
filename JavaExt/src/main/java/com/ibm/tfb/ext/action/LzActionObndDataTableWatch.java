package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundControlDbo;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundControlDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTMessage;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class LzActionObndDataTableWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		DPFTOutboundControlDbo data = (DPFTOutboundControlDbo) this.getInitialData();
		return data.getString("target_ds");
	}

	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		DPFTOutboundControlDbo data = (DPFTOutboundControlDbo) this.getInitialData();
		StringBuilder sb = new StringBuilder();
		sb.append("chal_name='").append(data.getString("chal_name"))
		.append("' and timestamp='").append(data.getString("timestamp")).append("'");
		return sb.toString();
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		Object[] params = {getTableName()};
		if(this.getDataSet().isEmpty())
			throw new DPFTActionException(this, "CUSTOM", "TFB00003E", params);
		
		Object[] params1 = {getInitialData().getString("chal_name")};
		DPFTUtil.pushNotification(
				 new DPFTMessage("CUSTOM", "TFB00010I", params1)
		);
		
		this.setResultSet(getDataSet());
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		//set correspond h_inbound record to error
		DPFTOutboundControlDboSet hObndSet = (DPFTOutboundControlDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
											.getDboSet("H_OUTBOUND", getTableWatchCriteria() + " and process_status='" + GlobalConstants.DPFT_OBND_STAT_RUN + "'");
		hObndSet.error();
		hObndSet.close();
		throw e;
	}

}
