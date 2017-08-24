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

public class MbnActionDataTableWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		// TODO Auto-generated method stub
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "D_MBN";
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
		Object[] params = {"行銀"};
		if(this.getDataSet().isEmpty())
			throw new DPFTActionException(this, "CUSTOM", "TFB00001E", params);
		DPFTUtil.pushNotification(
				DPFTUtil.getCampaignOwnerEmail(getInitialData().getString("camp_code")), 
				new DPFTMessage("CUSTOM", "TFB00008I", params)
		);
		
		/*Get Data set from "D_MBN"*/
		DPFTDboSet dMbnSet = this.getDataSet();
		
		/*Set Query criteria for "O_MBN"*/
		String qString = DPFTUtil.getFKQueryString(dMbnSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_MBN"*/
		DPFTOutboundDboSet oMbnSet = (DPFTOutboundDboSet) this.getDBConnector().getDboSet("O_MBN", qString);
		if(oMbnSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oMbnSet.deleteAll();
		}
		
		/*Validate records with personal info data & add record to outbound data table*/		
		ArrayList<String> cell_code_list = new ArrayList<String>();
		ArrayList<String> cell_name_list = new ArrayList<String>();
		for(int i = 0; i < dMbnSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oMbnSet.add();
			new_dbo.setValue(dMbnSet.getDbo(i));
			
			//replace keyword with url
			DPFTDbo dMbn = dMbnSet.getDbo(i);
			String context = dMbn.getString("CONTENT_TXT");
			for (int j = 1 ; j < 6 ; j++){
			
				String text = dMbn.getString("URL_TEXT" + String.valueOf(j));
				String url = dMbn.getString("URL" + String.valueOf(j));
				
				if(text != null){	
					context = context.replaceFirst(text, "<a href=\"javascript:window.open('" + url + "','_system');\" class=\"link1\">" + text + "</a>");
				}
			}
			new_dbo.setValue("CONTENT_TXT" , context);
			
			new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			
			//find distinct cell code
			if(!cell_code_list.contains(new_dbo.getString("cell_code"))){
				cell_code_list.add(new_dbo.getString("cell_code"));
			}
			if(!cell_name_list.contains(new_dbo.getString("cellname"))){
				cell_name_list.add(new_dbo.getString("cellname"));
			}
		}
		oMbnSet.setRefresh(false);
		oMbnSet.save();
		
		/*Wrtie Usage code to O_USAGECODE*/
		TFBUtil.processUsageCode(oMbnSet, "MBN");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(this.getDBConnector(), oMbnSet, cell_code_list, cell_name_list, "MBN", false);
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
