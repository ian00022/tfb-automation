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

public class NtmActionCustContInfoDMWatch extends DPFTActionTableWatch {

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
										+ TFBConstants.MKTDM_CONT_CD_RES_ZIP + "','"
										+ TFBConstants.MKTDM_CONT_CD_RES_ADDR + "','"
										+ TFBConstants.MKTDM_CONT_CD_TEL_OFF_ARE + "','"
										+ TFBConstants.MKTDM_CONT_CD_TEL_OFF + "','"
										+ TFBConstants.MKTDM_CONT_CD_TEL_OFF_EXT + "','"
										+ TFBConstants.MKTDM_CONT_CD_COM_TEL_ARE + "','"
										+ TFBConstants.MKTDM_CONT_CD_COM_TEL + "','"
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
		DPFTDboSet dNtmSet = this.getPreviousAction().getResultSet();
		
		/*Contact data from MKTDM*/
		MKTDMCustomerContactDboSet contSet = (MKTDMCustomerContactDboSet) this.getDataSet();

		/*set Addr by IBM Campaign Priority setting*/
		long ps_start_time = System.currentTimeMillis();
		for(int i = 0; i < dNtmSet.count(); i++){
			DPFTDbo dNtm = dNtmSet.getDbo(i);
			String[] addr_info = null;
			if(dNtm.isNull("addr_priority")){
				/*use default addr priority rule*/
				addr_info = contSet.getPrioritizedAddr(dNtm.getString("customer_id"), "NTM_ADDR", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				/*use ADDR_PRIORITY Setting*/
				addr_info = contSet.getPrioritizedAddr(dNtm.getString("customer_id"), "NTM_ADDR", dNtm.getString("addr_priority"));
			}
			dNtm.setValue("ContactAddr", addr_info[0]);
			dNtm.setValue("ContactZipCode", addr_info[1]);
			
			String mobile = null;
			if(dNtm.isNull("mobile_priority")){
				mobile = contSet.getPrioritizedMobilePhone(dNtm.getString("customer_id"), "NTM_MBL", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				mobile = contSet.getPrioritizedMobilePhone(dNtm.getString("customer_id"), "NTM_MBL", dNtm.getString("mobile_priority"));
			}
			dNtm.setValue("MobilePhone", mobile);
			
			dNtm.setValue("HomePhone" , contSet.getCommPhoneByBizType(dNtm.getString("customer_id"), TFBConstants.MKTDM_CONT_BIZTYPE_CC));
			
			String officeAre = contSet.getOfficeTelAreaByBizType(dNtm.getString("customer_id"), TFBConstants.MKTDM_CONT_BIZTYPE_CC);
			String officePhone = contSet.getOfficeTelByBizType(dNtm.getString("customer_id"), TFBConstants.MKTDM_CONT_BIZTYPE_CC);
			if(officeAre == null && officePhone == null){
				dNtm.setValue("OfficePhone" , officePhone);
			} else {
				dNtm.setValue("OfficePhone" , (officeAre == null ? "" : officeAre) + (officePhone == null ? "" : officePhone));
			}
			
			dNtm.setValue("OfficePhoneExt" , contSet.getOfficeExtTelByBizType(dNtm.getString("customer_id"), TFBConstants.MKTDM_CONT_BIZTYPE_CC));
			
			String email = null;
			if(dNtm.isNull("email_priority")){
				/*use default email priority rule*/
				email = contSet.getPrioritizedEmail(dNtm.getString("customer_id"), "NTM_MAIL", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				/*use email_priority Setting*/
				email = contSet.getPrioritizedEmail(dNtm.getString("customer_id"), "NTM_MAIL", dNtm.getString("email_priority"));
			}
			dNtm.setValue("EmailAddr", email);
			
			if((i+1)%100 == 0)
				DPFTLogger.debug(this, "Processed " + (i+1) + " records...");
		}
		long ps_fin_time = System.currentTimeMillis();
		DPFTLogger.info(this, "Processed total " + dNtmSet.count() + ", process time = " + (ps_fin_time - ps_start_time)/60000 + " min.");
		
		/*Set Query criteria for "O_NTM"*/
		String qString = DPFTUtil.getFKQueryString(dNtmSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_NTM"*/
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig());
		DPFTOutboundDboSet oNtmSet = (DPFTOutboundDboSet) connector.getDboSet("O_NTM", qString);
		oNtmSet.load();
		/*Data set from "O_NTM" should be empty*/
		if(oNtmSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oNtmSet.deleteAll();
		}
		
		ArrayList<String> cell_code_list = new ArrayList<String>();
		ArrayList<String> cell_name_list = new ArrayList<String>();
		for(int i = 0; i < dNtmSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oNtmSet.add();
			new_dbo.setValue(dNtmSet.getDbo(i));
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
		oNtmSet.save();
		
		/*Write usage codes to O_USAGECODE Table*/
		TFBUtil.processUsageCode(oNtmSet, "NTM");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(connector, oNtmSet, cell_code_list, cell_name_list, "NTM", true);
		oNtmSet.close();
	}

	private boolean isValidCustInfo(DPFTOutboundDbo new_dbo) {
		//check phone info
		if(new_dbo.isNull("MobilePhone") && new_dbo.isNull("HomePhone") && new_dbo.isNull("OfficePhone") && new_dbo.isNull("OfficePhoneExt"))
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
