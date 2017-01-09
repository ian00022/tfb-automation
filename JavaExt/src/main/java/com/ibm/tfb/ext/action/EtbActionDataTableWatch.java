package com.ibm.tfb.ext.action;

import java.util.ArrayList;

import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTMessage;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBUtil;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDbo;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class EtbActionDataTableWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		// TODO Auto-generated method stub
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "D_ETB";
	}

	@Override
	public String getTableWatchCriteria() {
		// TODO Auto-generated method stub
		String where = DPFTUtil.getFKQueryString(getInitialData());
		DPFTLogger.info(this, "Table select where = " + where);
		return where;
	}

	@Override
	public String getTriggerKeyCol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		Object[] params = {"端末"};
		if(this.getDataSet().isEmpty())
			throw new DPFTActionException(this, "CUSTOM", "TFB00001E", params);
		DPFTUtil.pushNotification(
				DPFTUtil.getCampaignOwnerEmail(getInitialData().getString("camp_code")), 
				new DPFTMessage("CUSTOM", "TFB00008I", params)
		);
		
		/*Get Data set from "D_ETB"*/
		DPFTDboSet dEtbSet = this.getDataSet();
		
		/*Set Query criteria for "O_ETB"*/
		String qString = DPFTUtil.getFKQueryString(dEtbSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_ETB"*/
		DPFTOutboundDboSet oEtbSet = (DPFTOutboundDboSet) this.getDBConnector().getDboSet("O_ETB", qString);
		if(oEtbSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oEtbSet.deleteAll();
		}
		
		/*Validate records with personal info data & add record to outbound data table*/		
		ArrayList<String> cell_code_list = new ArrayList<String>();
		ArrayList<String> cell_name_list = new ArrayList<String>();
		for(int i = 0; i < dEtbSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oEtbSet.add();
			new_dbo.setValue(dEtbSet.getDbo(i));
			new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			
			//find distinct cell code
			if(!cell_code_list.contains(new_dbo.getString("cell_code"))){
				cell_code_list.add(new_dbo.getString("cell_code"));
			}
			if(!cell_name_list.contains(new_dbo.getString("cellname"))){
				cell_name_list.add(new_dbo.getString("cellname"));
			}
		}
		oEtbSet.setRefresh(false);
		oEtbSet.save();
		
		/*Write Usage code to O_USAGECODE*/
		TFBUtil.processUsageCode(oEtbSet, "ETB");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(this.getDBConnector(), oEtbSet, cell_code_list, cell_name_list, "ETB", false);
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
