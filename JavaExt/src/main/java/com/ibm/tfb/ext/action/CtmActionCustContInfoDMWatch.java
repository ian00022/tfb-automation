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

public class CtmActionCustContInfoDMWatch extends DPFTActionTableWatch {

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
		DPFTDboSet dCtmSet = this.getPreviousAction().getResultSet();
		
		/*Contact data from MKTDM*/
		MKTDMCustomerContactDboSet contSet = (MKTDMCustomerContactDboSet) this.getDataSet();

		/*set Addr by IBM Campaign Priority setting*/
		long ps_start_time = System.currentTimeMillis();
		for(int i = 0; i < dCtmSet.count(); i++){
			DPFTDbo dCtm = dCtmSet.getDbo(i);
			String[] addr_info = null;
			if(dCtm.isNull("addr_priority")){
				/*use default addr priority rule*/
				addr_info = contSet.getPrioritizedAddrWithoutAddrCode(dCtm.getString("customer_id"), "CTM_ADDR", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				/*use ADDR_PRIORITY Setting*/
				addr_info = contSet.getPrioritizedAddrWithoutAddrCode(dCtm.getString("customer_id"), "CTM_ADDR", dCtm.getString("addr_priority"));
			}
			dCtm.setValue("addr", addr_info[0]);
			dCtm.setValue("zip_cod", addr_info[1]);
			
			String mobile = null;
			if(dCtm.isNull("mobile_priority")){
				mobile = contSet.getPrioritizedMobilePhone(dCtm.getString("customer_id"), "CTM_MBL", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				mobile = contSet.getPrioritizedMobilePhone(dCtm.getString("customer_id"), "CTM_MBL", dCtm.getString("mobile_priority"));
			}
			dCtm.setValue("mobile", mobile);
			dCtm.setValue("day_use" , contSet.getDayTelByBizType(dCtm.getString("customer_id"), TFBConstants.MKTDM_CONT_BIZTYPE_BNK));
			dCtm.setValue("night_use" , contSet.getNightTelByBizType(dCtm.getString("customer_id"), TFBConstants.MKTDM_CONT_BIZTYPE_BNK));
			
			String email = null;
			if(dCtm.isNull("email_priority")){
				/*use default email priority rule*/
				email = contSet.getPrioritizedEmail(dCtm.getString("customer_id"), "CTM_MAIL", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				/*use email_priority Setting*/
				email = contSet.getPrioritizedEmail(dCtm.getString("customer_id"), "CTM_MAIL", dCtm.getString("email_priority"));
			}
			dCtm.setValue("email", email);
			
			if((i+1)%100 == 0)
				DPFTLogger.debug(this, "Processed " + (i+1) + " records...");
		}
		long ps_fin_time = System.currentTimeMillis();
		DPFTLogger.info(this, "Processed total " + dCtmSet.count() + ", process time = " + (ps_fin_time - ps_start_time)/60000 + " min.");
		
		/*Set Query criteria for "O_CTM"*/
		String qString = DPFTUtil.getFKQueryString(dCtmSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_CTM"*/
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig());
		DPFTOutboundDboSet oCtmSet = (DPFTOutboundDboSet) connector.getDboSet("O_CTM", qString);
		oCtmSet.load();
		/*Data set from "O_CTM" should be empty*/
		if(oCtmSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oCtmSet.deleteAll();
		}
		
		ArrayList<String> cell_code_list = new ArrayList<String>();
		for(int i = 0; i < dCtmSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oCtmSet.add();
			new_dbo.setValue(dCtmSet.getDbo(i));
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
		}
		oCtmSet.setRefresh(false);
		oCtmSet.save();
		
		/*Write usage codes to O_USAGECODE Table*/
		TFBUtil.processUsageCode(oCtmSet, "CTM");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(connector, oCtmSet, cell_code_list, "CTM", true);
		oCtmSet.close();
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
