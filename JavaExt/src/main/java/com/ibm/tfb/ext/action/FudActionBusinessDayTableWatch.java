package com.ibm.tfb.ext.action;

import java.util.ArrayList;

import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDbo;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.CalendarUtil;
import com.ibm.tfb.ext.common.TFBUtil;

public class FudActionBusinessDayTableWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		return "D_FUD";
	}

	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		return "";
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}
	
	@Override
	public void postAction() throws DPFTRuntimeException {

		//set db config = MKTDM
		DPFTConfig config = DPFTUtil.getSystemDBConfig();
		
		//Get Data set from "D_FUD"
		DPFTDboSet dFudSet = ((DPFTActionTableWatch)this.getPreviousAction()).getDataSet();
		
		//Set Query criteria for "O_FUD"
		String qString = DPFTUtil.getFKQueryString(dFudSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		
		//Get Data set from "O_FUD"
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(config);
		DPFTOutboundDboSet oFudSet = (DPFTOutboundDboSet) connector.getDboSet("O_FUD", qString);
		oFudSet.load();
		//Data set from "O_FUD" should be empty
		if(oFudSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oFudSet.deleteAll();
		}
		
		//Validate records with personal info data & add record to outbound data table//
		ArrayList<String> cell_code_list = new ArrayList<String>();
		ArrayList<String> cell_name_list = new ArrayList<String>();
		long ps_start_time = System.currentTimeMillis();
		
		CalendarUtil  cal = new CalendarUtil();
		for(int i = 0; i < dFudSet.count(); i++){
			DPFTDbo dFud = dFudSet.getDbo(i);
			
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oFudSet.add();
			new_dbo.setValue(dFudSet.getDbo(i));
			
			cal.setDate(dFud.getDate("st_dt"));
			// START_DATE 放 T+1 
			//cal.getAfterDate(1);	  
			new_dbo.setValue("st_dt", TFBUtil.getROCYearMonthDay(cal.CalendarToDate()));
			
			// 計算 有效日期
			if(dFud.isNull("DURATION_DAYS")){
				new_dbo.setValue("en_dt", TFBUtil.getROCYearMonthDay(new_dbo.getDate("en_dt")));
			}else {
				cal.getAfterDate(Integer.valueOf(dFud.getString("DURATION_DAYS")) - 1);
				new_dbo.setValue("en_dt", TFBUtil.getROCYearMonthDay(cal.CalendarToDate()));
			}

			new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			
			if(!cell_code_list.contains(new_dbo.getString("cell_code"))){
				cell_code_list.add(new_dbo.getString("cell_code"));
			}
			if(!cell_name_list.contains(new_dbo.getString("cellname"))){
				cell_name_list.add(new_dbo.getString("cellname"));
			}
		}

		long ps_fin_time = System.currentTimeMillis();
		DPFTLogger.info(this, "Processed total " + dFudSet.count() + ", process time = " + (ps_fin_time - ps_start_time)/60000 + " min.");
		oFudSet.save();
		
		//Write usage codes to O_USAGECODE Table
		TFBUtil.processUsageCode(oFudSet, "FUD");
		
		//Write results to H_OUTBOUND Table//
		TFBUtil.generateObndCtrlRecord(connector, oFudSet, cell_code_list, cell_name_list, "FUD", false);
		oFudSet.close();
		
		//Set Result set for next action
		setResultSet(oFudSet);
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
