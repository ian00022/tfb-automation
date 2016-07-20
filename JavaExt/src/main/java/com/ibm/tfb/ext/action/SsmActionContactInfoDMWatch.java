package com.ibm.tfb.ext.action;

import java.util.ArrayList;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBConstants;
import com.ibm.tfb.ext.common.TFBUtil;
import com.ibm.tfb.ext.dbo.MKTDMCustomerContactDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDbo;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class SsmActionContactInfoDMWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		/*set Table watch db connection properties*/
		return TFBUtil.getMKTDMConfig();
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return DPFTEngine.getSystemProperties("mktdb.tbl.cust.cont");
	}

	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append("cust_id in (");
		sb.append(TFBUtil.getCustomerSelectINString(((DPFTActionTableWatch)this.getPreviousAction()).getDataSet(), "customer_id"));
		sb.append(") and cont_cd in ('" + TFBConstants.MKTDM_CONT_CD_MOBILE_1  + "')");
		DPFTLogger.info(this, "Table select where = " + sb.toString());
		return sb.toString();
	}

	@Override
	public String getTriggerKeyCol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		/*set db config = MKTDM*/
		DPFTConfig config = DPFTUtil.getSystemDBConfig();
		
		/*Get Data set from "D_SSM"*/
		DPFTDboSet dSsmSet = ((DPFTActionTableWatch)this.getPreviousAction()).getDataSet();
		
		/*Set Query criteria for "O_SSM"*/
		String qString = DPFTUtil.getFKQueryString(dSsmSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_SSM"*/
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(config);
		DPFTOutboundDboSet oSsmSet = (DPFTOutboundDboSet) connector.getDboSet("O_SSM", qString);
		oSsmSet.load();
		/*Data set from "O_SSM" should be empty*/
		if(oSsmSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oSsmSet.deleteAll();
		}
		
		/*Validate records with personal info data & add record to outbound data table*/		
		MKTDMCustomerContactDboSet custSet = (MKTDMCustomerContactDboSet) this.getDataSet();
		ArrayList<String> cell_code_list = new ArrayList<String>();
		for(int i = 0; i < dSsmSet.count(); i++){
			String cust_id = dSsmSet.getDbo(i).getString("customer_id");
			String mobile_no = custSet.getMobile(cust_id);
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oSsmSet.add();
			new_dbo.setValue(dSsmSet.getDbo(i));
			TFBUtil.setSSMHeaderProperties(new_dbo, dSsmSet.getDbo(i));
			DPFTDbo dSsm = dSsmSet.getDbo(i);
			if(dSsm.isNull("mobile_priority")){
				/*use default mobile priority rule*/
				mobile_no = custSet.getPrioritizedMobilePhone(dSsm.getString("customer_id"), "SSM", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				/*use mobile_priority Setting*/
				mobile_no = custSet.getPrioritizedMobilePhone(dSsm.getString("customer_id"), "SSM", dSsm.getString("mobile_priority"));
			}
			
			//get Contact email info
			new_dbo.setValue("contactemail", TFBUtil.getSMSContactEmail(DPFTUtil.getCampaignOwnerEmail(dSsmSet.getDbo(i).getString("camp_code"))));
			
			if(mobile_no == null){
				//person record doesn't have mobile number info
				DPFTLogger.debug(this, "cust_id = " + cust_id + " cannot find mobile number info");
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_EXCLUDE);
			}else{
				//person record has mobile number info
				DPFTLogger.debug(this, "cust_id = " + cust_id + " find mobile number info = " + mobile_no);
				new_dbo.setValue("destno", mobile_no);
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			}
			
			//find distinct cell code
			if(!cell_code_list.contains(new_dbo.getString("cell_code"))){
				cell_code_list.add(new_dbo.getString("cell_code"));
			}
		}
		oSsmSet.save();
		
		/*Wrtie Usage Code to O_USAGECODE*/
		TFBUtil.processUsageCode(oSsmSet, "SSM");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(connector, oSsmSet, cell_code_list, "SSM", true);
		oSsmSet.close();
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
