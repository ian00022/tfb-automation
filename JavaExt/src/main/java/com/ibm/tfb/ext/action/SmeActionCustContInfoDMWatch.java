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
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDbo;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBConstants;
import com.ibm.tfb.ext.common.TFBUtil;
import com.ibm.tfb.ext.dbo.MKTDMCustomerContactDboSet;

public class SmeActionCustContInfoDMWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		return TFBUtil.getMKTDMConfig();
	}

	@Override
	public String getTableName() {
		return DPFTEngine.getSystemProperties("mktdb.tbl.cust.cont");
	}

	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		StringBuilder sb = new StringBuilder();
		sb.append("cust_id in (");
		sb.append(TFBUtil.getCustomerSelectINString(this.getPreviousAction().getResultSet(), "customer_id"));
		sb.append(") and cont_cd in ('" + TFBConstants.MKTDM_CONT_CD_ZIPCD_COMM + "','" 
										+ TFBConstants.MKTDM_CONT_CD_ADDR_COMM + "','" 
										+ TFBConstants.MKTDM_CONT_CD_TEL_DAY + "','" 
										+ TFBConstants.MKTDM_CONT_CD_TEL_NIGHT + "','" 
										+ TFBConstants.MKTDM_CONT_CD_MOBILE_1 + "','"
										+ TFBConstants.MKTDM_CONT_CD_MOBILE_2 + "','"
										+ TFBConstants.MKTDM_CONT_CD_EMAIL + "')");
		DPFTLogger.info(this, "Table select where = " + sb.toString());
		return sb.toString();
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		/*get D_CDM Set*/
		DPFTDboSet dSmeSet = this.getPreviousAction().getResultSet();
		
		/*Contact data from MKTDM*/
		MKTDMCustomerContactDboSet contSet = (MKTDMCustomerContactDboSet) this.getDataSet();

		/*set Addr by IBM Campaign Priority setting*/
		long ps_start_time = System.currentTimeMillis();
		for(int i = 0; i < dSmeSet.count(); i++){
			DPFTDbo dSme = dSmeSet.getDbo(i);
			String[] addr_info = null;
			if(dSme.isNull("addr_priority")){
				/*use default addr priority rule*/
				addr_info = contSet.getPrioritizedAddr(dSme.getString("customer_id"), "SME_ADDR", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				/*use ADDR_PRIORITY Setting*/
				addr_info = contSet.getPrioritizedAddr(dSme.getString("customer_id"), "SME_ADDR", dSme.getString("addr_priority"));
			}
			dSme.setValue("addr", addr_info[0]);//客戶地址
			//dSme.setValue("zip_cod", addr_info[1]);
			
			String mobile = null;
			if(dSme.isNull("mobile_priority")){
				mobile = contSet.getPrioritizedMobilePhone(dSme.getString("customer_id"), "SME_MBL", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				mobile = contSet.getPrioritizedMobilePhone(dSme.getString("customer_id"), "SME_MBL", dSme.getString("mobile_priority"));
			}
			dSme.setValue("tel_no2", mobile);//客戶手機
			//客戶市話
			dSme.setValue("tel_no1" , contSet.getDayTelByBizType(dSme.getString("customer_id"), TFBConstants.MKTDM_CONT_BIZTYPE_BNK));
			//dSme.setValue("night_use" , contSet.getNightTelByBizType(dSme.getString("customer_id"), TFBConstants.MKTDM_CONT_BIZTYPE_BNK));
												
			if((i+1)%100 == 0)
				DPFTLogger.debug(this, "Processed " + (i+1) + " records...");
		}
		long ps_fin_time = System.currentTimeMillis();
		DPFTLogger.info(this, "Processed total " + dSmeSet.count() + ", process time = " + (ps_fin_time - ps_start_time)/60000 + " min.");
		
		/*Set Query criteria for "O_CTM"*/
		String qString = DPFTUtil.getFKQueryString(dSmeSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_CTM"*/
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig());
		DPFTOutboundDboSet oSmeSet = (DPFTOutboundDboSet) connector.getDboSet("O_SME", qString);
		oSmeSet.load();
		/*Data set from "O_CTM" should be empty*/
		if(oSmeSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oSmeSet.deleteAll();
		}
		
		ArrayList<String> cell_code_list = new ArrayList<String>();
		ArrayList<String> cell_name_list = new ArrayList<String>();
		for(int i = 0; i < dSmeSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oSmeSet.add();
			new_dbo.setValue(dSmeSet.getDbo(i));
			if(!isValidCustInfo(new_dbo)){
				//Exclude records
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_EXCLUDE);
			}else{
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			}
			//find distinct cell code
			if(!cell_code_list.contains(new_dbo.getString("cell_code"))){
				cell_code_list.add(new_dbo.getString("cell_code"));
			}
			if(!cell_name_list.contains(new_dbo.getString("cellname"))){
				cell_name_list.add(new_dbo.getString("cellname"));
			}
		}
		oSmeSet.save();
		
		/*Write usage codes to O_USAGECODE Table*/
		TFBUtil.processUsageCode(oSmeSet, "CDM");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(connector, oSmeSet, cell_code_list, cell_name_list, "SME", true);
		oSmeSet.close();
	}

	private boolean isValidCustInfo(DPFTOutboundDbo new_dbo) {
		//check phone info
		if(new_dbo.isNull("mobile") && new_dbo.isNull("day_use") && new_dbo.isNull("night_use"))
			return false;
		return true;
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		DPFTInboundControlDboSet hIbndSet = (DPFTInboundControlDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
				.getDboSet("H_INBOUND", DPFTUtil.getFKQueryString(((DPFTActionTableWatch)this.getPreviousAction()).getResultSet().getDbo(0)));
		hIbndSet.error();
		hIbndSet.close();
		throw e;		
	}

}
