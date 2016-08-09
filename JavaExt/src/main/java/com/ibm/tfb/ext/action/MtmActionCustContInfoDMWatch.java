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

public class MtmActionCustContInfoDMWatch extends DPFTActionTableWatch {

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
		sb.append(") and cont_cd in ('" + TFBConstants.MKTDM_CONT_CD_TEL_DAY + "','" 
										+ TFBConstants.MKTDM_CONT_CD_TEL_NIGHT + "','" 
										+ TFBConstants.MKTDM_CONT_CD_MOBILE_1 + "','"
										+ TFBConstants.MKTDM_CONT_CD_MOBILE_2 + "')");
		DPFTLogger.info(this, "Table select where = " + sb.toString());
		return sb.toString();
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		/*get D_MTM Set*/
		DPFTDboSet dMtmSet = this.getPreviousAction().getResultSet();
		
		/*Contact data from MKTDM*/
		MKTDMCustomerContactDboSet contSet = (MKTDMCustomerContactDboSet) this.getDataSet();

		/*set Addr by IBM Campaign Priority setting*/
		long ps_start_time = System.currentTimeMillis();
		for(int i = 0; i < dMtmSet.count(); i++){
			DPFTDbo dMtm = dMtmSet.getDbo(i);
			
			String mobile = null;
			if(dMtm.isNull("mobile_priority")){
				mobile = contSet.getPrioritizedMobilePhone(dMtm.getString("customer_id"), "MTM_MBL", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				mobile = contSet.getPrioritizedMobilePhone(dMtm.getString("customer_id"), "MTM_MBL", dMtm.getString("mobile_priority"));
			}
			dMtm.setValue("PHONEMOBILE", mobile);
			dMtm.setValue("PHONEHOMETEL" , contSet.getDayTelByBizType(dMtm.getString("customer_id"), TFBConstants.MKTDM_CONT_BIZTYPE_BNK));
			dMtm.setValue("PHONEOFFICETEL" , contSet.getNightTelByBizType(dMtm.getString("customer_id"), TFBConstants.MKTDM_CONT_BIZTYPE_BNK));
			
			if((i+1)%100 == 0)
				DPFTLogger.debug(this, "Processed " + (i+1) + " records...");
		}
		long ps_fin_time = System.currentTimeMillis();
		DPFTLogger.info(this, "Processed total " + dMtmSet.count() + ", process time = " + (ps_fin_time - ps_start_time)/60000 + " min.");
		
		/*Set Query criteria for "O_MTM"*/
		String qString = DPFTUtil.getFKQueryString(dMtmSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_MTM"*/
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig());
		DPFTOutboundDboSet oMtmSet = (DPFTOutboundDboSet) connector.getDboSet("O_MTM", qString);
		oMtmSet.load();
		/*Data set from "O_MTM" should be empty*/
		if(oMtmSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oMtmSet.deleteAll();
		}
		
		ArrayList<String> cell_code_list = new ArrayList<String>();
		for(int i = 0; i < dMtmSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oMtmSet.add();
			new_dbo.setValue(dMtmSet.getDbo(i));
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
		oMtmSet.save();
		
		/*Write usage codes to O_USAGECODE Table*/
		TFBUtil.processUsageCode(oMtmSet, "MTM");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(connector, oMtmSet, cell_code_list, "MTM", true);
		oMtmSet.close();
	}

	private boolean isValidCustInfo(DPFTOutboundDbo new_dbo) {
		//check phone info
		if(new_dbo.isNull("PHONEMOBILE") && new_dbo.isNull("PHONEHOMETEL") && new_dbo.isNull("PHONEOFFICETEL"))
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
