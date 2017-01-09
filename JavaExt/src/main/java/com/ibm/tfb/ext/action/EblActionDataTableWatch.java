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

public class EblActionDataTableWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		return "D_EBL";
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
		Object[] params = {"銀行電子對帳單"};
		if(this.getDataSet().isEmpty())
			throw new DPFTActionException(this, "CUSTOM", "TFB00001E", params);
		DPFTUtil.pushNotification(
				DPFTUtil.getCampaignOwnerEmail(getInitialData().getString("camp_code")), 
				new DPFTMessage("CUSTOM", "TFB00008I", params)
		);
		
		/*Get Data set from "D_EBL"*/
		DPFTDboSet dEblSet = this.getDataSet();
		
		/*Set Query criteria for "O_EBL"*/
		String qString = DPFTUtil.getFKQueryString(dEblSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_EBL"*/
		DPFTOutboundDboSet oEblSet = (DPFTOutboundDboSet) this.getDBConnector().getDboSet("O_EBL", qString);
		if(oEblSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oEblSet.deleteAll();
		}
		
		/*Validate records with personal info data & add record to outbound data table*/		
		ArrayList<String> cell_code_list = new ArrayList<String>();
		ArrayList<String> cell_name_list = new ArrayList<String>();
		for(int i = 0; i < dEblSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oEblSet.add();
			new_dbo.setValue(dEblSet.getDbo(i));
			new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			
			//find distinct cell code
			if(!cell_code_list.contains(new_dbo.getString("cell_code"))){
				cell_code_list.add(new_dbo.getString("cell_code"));
			}
		}
		oEblSet.setRefresh(false);
		oEblSet.save();
		
		/*Wrtie Usage code to O_USAGECODE*/
		TFBUtil.processUsageCode(oEblSet, "EBL");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(this.getDBConnector(), oEblSet, cell_code_list, cell_name_list, "EBL", false);
		oEblSet.close();
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
