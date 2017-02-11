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
		
		DPFTDboSet IDSet = (DPFTDboSet) connector.getDboSet("DPFT_IDMAPPING");
		IDSet.load();
		if(oSsmSet.getDbo(0) != null){
			String idStr = (String)oSsmSet.getDbo(0).getColumnValue("TREATMENT_CODE");
		    IDSet.filter("TREATMENT_CODE", idStr);
		    if(IDSet.count() > 0){
				DPFTLogger.info(this, "ID Records exist in output data set...Delete All Records...");
				IDSet.deleteAll();
			}
		}
		
		/*Data set from "O_SSM" should be empty*/
		if(oSsmSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oSsmSet.deleteAll();
		}
		
		/*Validate records with personal info data & add record to outbound data table*/		
		MKTDMCustomerContactDboSet custSet = (MKTDMCustomerContactDboSet) this.getDataSet();
		ArrayList<String> cell_code_list = new ArrayList<String>();
		ArrayList<String> cell_name_list = new ArrayList<String>();
		String cmp_owner_email = DPFTUtil.getCampaignOwnerEmail(dSsmSet.getDbo(0).getString("camp_code"));
		String it_adm_email = TFBUtil.getMailGroup(TFBConstants.TFB_MAILGROUP_ITADM);
		long ps_start_time = System.currentTimeMillis();
		
		for(int i = 0; i < dSsmSet.count(); i++){
			String cust_id = dSsmSet.getDbo(i).getString("customer_id");
			String mobile_no = custSet.getMobile(cust_id);
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oSsmSet.add();
			new_dbo.setValue(dSsmSet.getDbo(i));
			DPFTDbo id_dbo = (DPFTDbo) IDSet.add();
			String id_num = String.format("%09d", i+1);
			TFBUtil.setSSMHeaderProperties(new_dbo, dSsmSet.getDbo(i), id_dbo, "S"+id_num);
			DPFTDbo dSsm = dSsmSet.getDbo(i);
			if(dSsm.isNull("mobile_priority")){
				/*use default mobile priority rule*/
				mobile_no = custSet.getPrioritizedMobilePhone(dSsm.getString("customer_id"), "SSM", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				/*use mobile_priority Setting*/
				mobile_no = custSet.getPrioritizedMobilePhone(dSsm.getString("customer_id"), "SSM", dSsm.getString("mobile_priority"));
			}
			
			//get Contact email info
			new_dbo.setValue("contactemail", TFBUtil.getSMSContactEmail(cmp_owner_email, it_adm_email));
			
			if(mobile_no == null){
				//person record doesn't have mobile number info
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_EXCLUDE);
			}else if(!mobile_no.startsWith("09")){	//	sms判斷+09開頭
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_EXCLUDE);
			}else{
				//person record has mobile number info
				new_dbo.setValue("destno", mobile_no);
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			}
			
			//find distinct cell code
			if(!cell_code_list.contains(new_dbo.getString("cell_code"))){
				cell_code_list.add(new_dbo.getString("cell_code"));
			}
			if(!cell_name_list.contains(new_dbo.getString("cellname"))){
				cell_name_list.add(new_dbo.getString("cellname"));
			}
			
			if((i+1)%100 == 0)
				DPFTLogger.debug(this, "Processed " + (i+1) + " records...");
		}
		long ps_fin_time = System.currentTimeMillis();
		DPFTLogger.info(this, "Processed total " + dSsmSet.count() + ", process time = " + (ps_fin_time - ps_start_time)/60000 + " min.");
		oSsmSet.setRefresh(false);
		oSsmSet.save();
		IDSet.save();
		
		/*Wrtie Usage Code to O_USAGECODE*/
		TFBUtil.processUsageCode(oSsmSet, "SSM");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(connector, oSsmSet, cell_code_list, cell_name_list, "SSM", true);
		oSsmSet.close();
		IDSet.close();
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
