package com.ibm.tfb.ext.action;

import java.util.ArrayList;
import java.util.HashMap;

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

public class LzsActionSfaContactInfoWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		/*set Table watch db connection properties*/
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		return "O_SFA";
	}

	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		return "chal_name != 'SFA' and process_status != 'Dummy' and customer_id in ("
				+ TFBUtil.buildCustomerSelectINString(((DPFTActionTableWatch)this.getPreviousAction()).getResultSet())
				+ ")";
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		DPFTDboSet dLzsSet = ((DPFTActionTableWatch)this.getPreviousAction()).getResultSet();
		DPFTDboSet oSfaSet = this.getDataSet();
		HashMap<String, String> mobileHM_V3 = oSfaSet.getKeyValueMap("customer_id", "VAR_FIELD_VALUE3");
		HashMap<String, String> mobileHM_V5 = oSfaSet.getKeyValueMap("customer_id", "VAR_FIELD_VALUE5");
		HashMap<String, String> mobileHM_V6 = oSfaSet.getKeyValueMap("customer_id", "VAR_FIELD_VALUE6");
		HashMap<String, String> leadtypeHM  = oSfaSet.getKeyValueMap("customer_id", "LEAD_TYPE");
		
		String cmp_owner_email = DPFTUtil.getCampaignOwnerEmail(dLzsSet.getDbo(0).getString("camp_code"));
		String it_adm_email = TFBUtil.getMailGroup(TFBConstants.TFB_MAILGROUP_ITADM);
		
		for(int i = 0; i < dLzsSet.count(); i++){
			DPFTDbo dLzs = dLzsSet.getDbo(i);
			if(dLzs.getString("src_chal_name").equalsIgnoreCase("SFA")){
				//only process sfa source
				String customer_id = dLzs.getString("customer_id");
				TFBUtil.setSSMHeaderProperties(dLzs, dLzs);
				String leadtype = leadtypeHM.get(customer_id);
				String mobile_no = null;
				if(leadtype.equalsIgnoreCase(TFBConstants.SFA_LZ_LEADTYPE_EBN_POST_LOGON)){
					mobile_no = mobileHM_V6.get(customer_id);
				}else if(leadtype.equalsIgnoreCase(TFBConstants.SFA_LZ_LEADTYPE_EBN_PRE_LOGON)){
					mobile_no = mobileHM_V5.get(customer_id);
				}else{
					mobile_no = mobileHM_V3.get(customer_id);
				}
				//get Contact email info
				dLzs.setValue("contactemail", TFBUtil.getSMSContactEmail(cmp_owner_email, it_adm_email));
				
				if(mobile_no == null){
					//person record doesn't have mobile number info
					dLzs.setValue("process_status", GlobalConstants.O_DATA_EXCLUDE);
				}else if(!mobile_no.startsWith("09")){
					dLzs.setValue("process_status", GlobalConstants.O_DATA_EXCLUDE);
				}else{
					//person record has mobile number info
					dLzs.setValue("destno", mobile_no);
					dLzs.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
				}
			}
		}
		
		/*Get Data set from "O_LZS"*/
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig());
		DPFTOutboundDboSet oLzsSet = (DPFTOutboundDboSet) connector.getDboSet("O_LZS", DPFTUtil.getFKQueryString(dLzsSet.getDbo(0)));
		oLzsSet.load();
		
		/*Data set from "O_LZS" should be empty*/
		if(oLzsSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oLzsSet.deleteAll();
		}
		
		ArrayList<String> cell_code_list = new ArrayList<String>();
		ArrayList<String> cell_name_list = new ArrayList<String>();
		
		for(int i = 0; i < dLzsSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oLzsSet.add();
			new_dbo.setValue(dLzsSet.getDbo(i));
			
			//find distinct cell code
			if(!cell_code_list.contains(new_dbo.getString("cell_code"))){
				cell_code_list.add(new_dbo.getString("cell_code"));
			}
			if(!cell_name_list.contains(new_dbo.getString("cellname"))){
				cell_name_list.add(new_dbo.getString("cellname"));
			}
		}
		
		oLzsSet.setRefresh(false);
		oLzsSet.save(false);
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(connector, oLzsSet, cell_code_list, cell_name_list, "LZS", true);
		oLzsSet.close();
		mobileHM_V3.clear();
		mobileHM_V5.clear();
		mobileHM_V6.clear();
		leadtypeHM.clear();
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
