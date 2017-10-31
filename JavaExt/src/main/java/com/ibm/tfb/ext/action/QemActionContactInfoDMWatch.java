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

public class QemActionContactInfoDMWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		// TODO Auto-generated method stub
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
		
		/*Get Data set from "D_QEM"*/
		DPFTDboSet dQemSet = ((DPFTActionTableWatch)this.getPreviousAction()).getDataSet();
		
		/*Set Query criteria for "O_QEM"*/
		String qString = DPFTUtil.getFKQueryString(dQemSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_QEM"*/
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(config);
		DPFTOutboundDboSet oQemSet = (DPFTOutboundDboSet) connector.getDboSet("O_QEM", qString);
		oQemSet.load();
		
		String idStr = (String)dQemSet.getDbo(0).getColumnValue("TREATMENT_CODE");
		DPFTDboSet IDSet = (DPFTDboSet) connector.getDboSet("DPFT_IDMAPPING", "TREATMENT_CODE = '" +idStr+"'");
		IDSet.load();
		if(IDSet.count() > 0){
			DPFTLogger.info(this, "ID Records exist in output data set...Delete All Records...");
			IDSet.deleteAll();
		}
		
		/*Data set from "O_QEM" should be empty*/
		if(oQemSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oQemSet.deleteAll();
		}
		
		/*Validate records with personal info data & add record to outbound data table*/		
		MKTDMCustomerContactDboSet custSet = (MKTDMCustomerContactDboSet) this.getDataSet();
		ArrayList<String> cell_code_list = new ArrayList<String>();
		ArrayList<String> cell_name_list = new ArrayList<String>();
		String cmp_owner_email = DPFTUtil.getCampaignOwnerEmail(dQemSet.getDbo(0).getString("camp_code"));
		String it_adm_email = TFBUtil.getMailGroup(TFBConstants.TFB_MAILGROUP_ITADM);
		long ps_start_time = System.currentTimeMillis();
		
		for(int i = 0; i < dQemSet.count(); i++){
			String cust_id = dQemSet.getDbo(i).getString("customer_id");
			String mobile_no = custSet.getMobile(cust_id);
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oQemSet.add();
			new_dbo.setValue(dQemSet.getDbo(i));
			DPFTDbo id_dbo = (DPFTDbo) IDSet.add();
			String id_num = String.format("%09d", i+1);
			TFBUtil.setSSMHeaderProperties(new_dbo, dQemSet.getDbo(i), id_dbo, "E"+id_num);
			DPFTDbo dQem = dQemSet.getDbo(i);
			if(dQem.isNull("mobile_priority")){
				/*use default mobile priority rule*/
				mobile_no = custSet.getPrioritizedMobilePhone(dQem.getString("customer_id"), "QEM", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				/*use mobile_priority Setting*/
				mobile_no = custSet.getPrioritizedMobilePhone(dQem.getString("customer_id"), "QEM", dQem.getString("mobile_priority"));
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
		DPFTLogger.info(this, "Processed total " + dQemSet.count() + ", process time = " + (ps_fin_time - ps_start_time)/60000 + " min.");
		oQemSet.setRefresh(false);
		oQemSet.save();
		IDSet.save();
		
		/*Write Usage code to O_USAGECODE*/
		TFBUtil.processUsageCode(oQemSet, "QEM");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(connector, oQemSet, cell_code_list, cell_name_list, "QEM", true);
		oQemSet.close();
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
