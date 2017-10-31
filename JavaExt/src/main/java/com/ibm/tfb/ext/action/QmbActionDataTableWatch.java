package com.ibm.tfb.ext.action;

import java.util.ArrayList;

import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
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

public class QmbActionDataTableWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		// TODO Auto-generated method stub
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "D_QMB";
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
		Object[] params = {"Survey 行銀"};
		if(this.getDataSet().isEmpty())
			throw new DPFTActionException(this, "CUSTOM", "TFB00001E", params);
		DPFTUtil.pushNotification(
				DPFTUtil.getCampaignOwnerEmail(getInitialData().getString("camp_code")), 
				new DPFTMessage("CUSTOM", "TFB00008I", params)
		);
		
		/*Get Data set from "D_QMB"*/
		DPFTDboSet dQmbSet = this.getDataSet();
		
		/*Set Query criteria for "O_QMB"*/
		String qString = DPFTUtil.getFKQueryString(dQmbSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_QMB"*/
		DPFTOutboundDboSet oQmbSet = (DPFTOutboundDboSet) this.getDBConnector().getDboSet("O_QMB", qString);
		if(oQmbSet.count() > 0){
			DPFTLogger.info(this, "Records exist in QMB output data set...Delete All Records...");
			oQmbSet.deleteAll();
		}
		
		/*Get Data set from "O_MBN"*/
		DPFTOutboundDboSet oMbnSet = (DPFTOutboundDboSet) this.getDBConnector().getDboSet("O_MBN", qString);
		if(oMbnSet.count() > 0){
			DPFTLogger.info(this, "Records exist in MBN output data set...Delete All Records...");
			oMbnSet.deleteAll();
		}
		
		/*Validate records with personal info data & add record to outbound data table*/		
		ArrayList<String> cell_code_list = new ArrayList<String>();
		ArrayList<String> cell_name_list = new ArrayList<String>();
		for(int i = 0; i < dQmbSet.count(); i++){
			// write O_QMB
			DPFTOutboundDbo new_qmb_dbo = (DPFTOutboundDbo) oQmbSet.add();
			new_qmb_dbo.setValue(dQmbSet.getDbo(i));
			
			new_qmb_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			
			// write O_MBN
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oMbnSet.add();
			String[] ignore_cols = {"SURVEY_URL","CUST_ATTR1","CUST_ATTR2","CUST_ATTR3","CUST_ATTR4","CUST_ATTR5","CUST_ATTR6",
									"CUST_ATTR7","CUST_ATTR8","CUST_ATTR9","CUST_ATTR10","CUST_ATTR11","CUST_ATTR12",
									"CUST_ATTR13","CUST_ATTR14","CUST_ATTR15","CUST_ATTR16","CUST_ATTR17","CUST_ATTR18",
									"CUST_ATTR19","CUST_ATTR20","CUST_ATTR21","CUST_ATTR22","CUST_ATTR23"};
			new_dbo.setValue(dQmbSet.getDbo(i), ignore_cols);
			
			DPFTDbo dQmb = dQmbSet.getDbo(i);
			String context = dQmb.getString("CONTENT_TXT");
			for (int j = 1 ; j < 6 ; j++){
			
				String text = dQmb.getString("URL_TEXT" + String.valueOf(j));
				String url = dQmb.getString("URL" + String.valueOf(j));
				
				if(text != null){	
					context = context.replaceFirst(text, "<a href=\"javascript:window.open('" + url + "','_system');\" class=\"link1\">" + text + "</a>");
				}
			}
			new_dbo.setValue("CONTENT_TXT" , context);
			
			new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			
			//find distinct cell code
			if(!cell_code_list.contains(new_qmb_dbo.getString("cell_code"))){
				cell_code_list.add(new_qmb_dbo.getString("cell_code"));
			}
			if(!cell_name_list.contains(new_qmb_dbo.getString("cellname"))){
				cell_name_list.add(new_qmb_dbo.getString("cellname"));
			}
		}
		oQmbSet.setRefresh(false);
		oQmbSet.save();
		
		oMbnSet.setRefresh(false);
		oMbnSet.save();
		
		/*Wrtie Usage code to O_USAGECODE*/
		TFBUtil.processUsageCode(oQmbSet, "QMB");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(this.getDBConnector(), oQmbSet, cell_code_list, cell_name_list, "QMB", false);
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
