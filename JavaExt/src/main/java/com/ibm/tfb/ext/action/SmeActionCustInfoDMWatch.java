package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBUtil;
import com.ibm.tfb.ext.dbo.MKTDMCustomerDboSet;

public class SmeActionCustInfoDMWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		return TFBUtil.getMKTDMConfig();
	}

	@Override
	public String getTableName() {
		return DPFTEngine.getSystemProperties("mktdb.tbl.cust");
	}

	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		StringBuilder sb = new StringBuilder();
		sb.append("cust_id in (");
		sb.append(TFBUtil.getCustomerSelectINString(((DPFTActionTableWatch)this.getPreviousAction()).getDataSet(), "customer_id"));//取D檔的CUSTOMER_ID取得CUSTOMER的INFOR
		sb.append(")");
		DPFTLogger.info(this, "Table select where = " + sb.toString());
		return sb.toString();
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		/*get data set from D_SME*/
		DPFTDboSet dSmeSet = ((DPFTActionTableWatch)this.getPreviousAction()).getDataSet();
		
		/*get data set from Customer info*/
		MKTDMCustomerDboSet custDboSet = (MKTDMCustomerDboSet) this.getDataSet();
			
		String custString = "cust_id in (" + TFBUtil.getColumnSelectINString("resp_id", "D_SME", DPFTUtil.getFKQueryString(dSmeSet.getDbo(0))) + ")";
		// MKT_CUST
		MKTDMCustomerDboSet custDbo = (MKTDMCustomerDboSet) this.getDBConnector().getDboSet(DPFTEngine.getSystemProperties("mktdb.tbl.cust"), custString);
		custDbo.load();
		
		/*set value to personal info column*/
		for(int i = 0; i < dSmeSet.count(); i++){
			DPFTDbo dSme = dSmeSet.getDbo(i);
			dSme.setValue("cust_name",   custDboSet.getCustName(dSme.getString("customer_id")));
			dSme.setValue("resp_name",   custDbo.getCustName(dSme.getString("resp_id")));
			
		}
		
		/*set temp value to result set for next action*/
		this.setResultSet(dSmeSet);
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		DPFTInboundControlDboSet hIbndSet = (DPFTInboundControlDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
				.getDboSet("H_INBOUND", DPFTUtil.getFKQueryString(((DPFTActionTableWatch)this.getPreviousAction()).getDataSet().getDbo(0)));
		hIbndSet.error();
		hIbndSet.close();
		throw e;
	}

}
