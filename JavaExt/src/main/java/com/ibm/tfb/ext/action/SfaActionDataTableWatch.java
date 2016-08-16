package com.ibm.tfb.ext.action;

import java.util.ArrayList;

import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDbo;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTMessage;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBUtil;

public class SfaActionDataTableWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		return "D_SFA";
	}

	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		String where = DPFTUtil.getFKQueryString(getInitialData());
		DPFTLogger.info(this, "Table select where = " + where);
		return where;
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		Object[] params = {"新個金"};
		if(this.getDataSet().isEmpty())
			throw new DPFTActionException(this, "CUSTOM", "TFB00001E", params);
		DPFTUtil.pushNotification(
				DPFTUtil.getCampaignOwnerEmail(getInitialData().getString("camp_code")), 
				new DPFTMessage("CUSTOM", "TFB00008I", params)
		);
		
		/*Get Data set from "D_SFA"*/
		DPFTDboSet dSfaSet = this.getDataSet();
		
		/*Set Query criteria for "O_SFA"*/
		String qString = DPFTUtil.getFKQueryString(dSfaSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_SFA"*/
		DPFTOutboundDboSet oSfaSet = (DPFTOutboundDboSet) this.getDBConnector().getDboSet("O_SFA", qString);
		if(oSfaSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oSfaSet.deleteAll();
		}
		
		/*Validate records with personal info data & add record to outbound data table*/		
		ArrayList<String> cell_code_list = new ArrayList<String>();
		for(int i = 0; i < dSfaSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oSfaSet.add();
			new_dbo.setValue(dSfaSet.getDbo(i));
			new_dbo.setValue("sfa_lead_id", new_dbo.getString("customer_id") + "||" + new_dbo.getString("treatment_code"));
			new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			
			//find distinct cell code
			if(!cell_code_list.contains(new_dbo.getString("cell_code"))){
				cell_code_list.add(new_dbo.getString("cell_code"));
			}
		}
		oSfaSet.save();
		
		/*Write Usage code to O_USAGECODE*/
		TFBUtil.processUsageCode(oSfaSet, "SFA");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(this.getDBConnector(), oSfaSet, cell_code_list, "SFA", false);
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		//set correspond h_inbound record to error
		DPFTInboundControlDboSet hIbndSet = (DPFTInboundControlDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
											.getDboSet("H_INBOUND", DPFTUtil.getFKQueryString(getInitialData()));
		hIbndSet.error();
		hIbndSet.close();
		throw e;
	}

}
