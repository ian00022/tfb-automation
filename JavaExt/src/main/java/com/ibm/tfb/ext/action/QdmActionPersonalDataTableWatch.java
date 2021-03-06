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

public class QdmActionPersonalDataTableWatch extends DPFTActionTableWatch {

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
		sb.append(") and cont_cd in ('" + TFBConstants.MKTDM_CONT_CD_EMAIL + "')");
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
		
		/*Get Data set from "D_QDM"*/
		DPFTDboSet dQdmSet = ((DPFTActionTableWatch)this.getPreviousAction()).getDataSet();
		
		/*Set Query criteria for "O_QDM"*/
		String qString = DPFTUtil.getFKQueryString(dQdmSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_QDM"*/
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(config);
		DPFTOutboundDboSet oQdmSet = (DPFTOutboundDboSet) connector.getDboSet("O_QDM", qString);
		oQdmSet.load();
		/*Data set from "O_QDM" should be empty*/
		if(oQdmSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oQdmSet.deleteAll();
		}
		
		/*Get URL Datasource*/
		//DPFTDboSet urlSet = connector.getDboSet("MKT_SURVEYURL_H", "campaignid='" + dQdmSet.getDbo(0).getString("camp_code") + "'");
		//urlSet.load();
		
		/*Validate records with personal info data & add record to outbound data table*/		
		MKTDMCustomerContactDboSet custSet = (MKTDMCustomerContactDboSet) this.getDataSet();
		ArrayList<String> cell_code_list = new ArrayList<String>();
		ArrayList<String> cell_name_list = new ArrayList<String>();
		long ps_start_time = System.currentTimeMillis();
		for(int i = 0; i < dQdmSet.count(); i++){
			String cust_id = dQdmSet.getDbo(i).getString("customer_id");
			String email = custSet.getEmail(cust_id);
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oQdmSet.add();
			new_dbo.setValue(dQdmSet.getDbo(i));
			DPFTDbo dQdm = dQdmSet.getDbo(i);
			if(dQdm.isNull("email_priority")){
				/*use default email priority rule*/
				email = custSet.getPrioritizedEmail(dQdm.getString("customer_id"), "QDM", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				/*use email_priority Setting*/
				email = custSet.getPrioritizedEmail(dQdm.getString("customer_id"), "QDM", dQdm.getString("email_priority"));
			}
			String url = dQdmSet.getDbo(i).getString("resv1");
			if(url == null){
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_EXCLUDE);
			}
			
			if(email == null){
				//person record doesn't have email info
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_EXCLUDE);
			}else{
				//person record has email info
				new_dbo.setValue("email", email);
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
		DPFTLogger.info(this, "Processed total " + dQdmSet.count() + ", process time = " + (ps_fin_time - ps_start_time)/60000 + " min.");
		oQdmSet.save();
		
		/*Write usage codes to O_USAGECODE Table*/
		TFBUtil.processUsageCode(oQdmSet, "QDM");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(connector, oQdmSet, cell_code_list, cell_name_list, "QDM", true);
		oQdmSet.close();
		
		/*Set Result set for next action*/
		setResultSet(oQdmSet);
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
