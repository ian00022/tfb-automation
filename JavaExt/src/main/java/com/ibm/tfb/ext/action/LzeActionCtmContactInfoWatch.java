package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBConstants;
import com.ibm.tfb.ext.common.TFBUtil;

public class LzeActionCtmContactInfoWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		/*set Table watch db connection properties*/
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		return "O_CTM";
	}

	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		return "camp_code is null and process_status != 'Dummy' and customer_id in ("
				+ TFBUtil.buildCustomerSelectINString(((DPFTActionTableWatch)this.getPreviousAction()).getDataSet())
				+ ")";
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		DPFTDboSet dLzeSet = ((DPFTActionTableWatch)this.getPreviousAction()).getDataSet();
		HashMap<String, String> mobileHM = this.getDataSet().getKeyValueMap("customer_id", "mobile");
		String cmp_owner_email = DPFTUtil.getCampaignOwnerEmail(dLzeSet.getDbo(0).getString("camp_code"));
		String it_adm_email = TFBUtil.getMailGroup(TFBConstants.TFB_MAILGROUP_ITADM);
		
		for(int i = 0; i < dLzeSet.count(); i++){
			DPFTDbo dLze = dLzeSet.getDbo(i);
			if(dLze.getString("src_chal_name").equalsIgnoreCase("CTM")){
				//only process ctm source
				TFBUtil.setSSMHeaderProperties(dLze, dLze);
				String mobile_no = mobileHM.get(dLze.getString("customer_id"));
				
				//get Contact email info
				dLze.setValue("contactemail", TFBUtil.getSMSContactEmail(cmp_owner_email, it_adm_email));
				
				if(mobile_no == null){
					//person record doesn't have mobile number info
					dLze.setValue("process_status", GlobalConstants.O_DATA_EXCLUDE);
				}else if(!mobile_no.startsWith("09")){
					dLze.setValue("process_status", GlobalConstants.O_DATA_EXCLUDE);
				}else{
					//person record has mobile number info
					dLze.setValue("destno", mobile_no);
					dLze.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
				}
			}
		}
		
		this.setResultSet(dLzeSet);
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
