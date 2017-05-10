package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDbo;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDboSet;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTMessage;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBConstants;
import com.ibm.tfb.ext.common.TFBUtil;
import com.ibm.tfb.ext.dbo.MKTDMCustomerContactDboSet;
import com.ibm.tfb.ext.dbo.MKTDMCustomerDboSet; 

public class CsrLzTmActionDataTableWatch extends LzActionDataTableWatch {

	
	public void postAction() throws DPFTRuntimeException {
		Object[] params = {"客服轉介TM"};
//		if(this.getDataSet().isEmpty())
//			throw new DPFTActionException(this, "CUSTOM", "TFB00001E", params);
		DPFTUtil.pushNotification(
				new DPFTMessage("CUSTOM", "TFB00008I", params)
		);
		
		/*Get Data set from Source Table*/
		DPFTDboSet dLzSet = this.getDataSet();
		
		String timestamp = this.getInitialData().getString("timestamp");
		String chal_name = this.getInitialData().getString("chal_name");
		
		/*Set Query criteria for target Table*/
		StringBuilder custString = new StringBuilder();
		custString.append("cust_id in (");
		for(int i = 0; i < dLzSet.count(); i++){
			custString.append("'" + dLzSet.getDbo(i).getString("customer_id") + "'");
			if( i != dLzSet.count()-1){
				custString.append(",");
			}
		}
		custString.append(")");
		DPFTLogger.info(this, "Table select where = " + custString.toString());
		
		// MKT_CUST
		MKTDMCustomerDboSet custDbo = (MKTDMCustomerDboSet) this.getDBConnector().getDboSet(DPFTEngine.getSystemProperties("mktdb.tbl.cust"), custString.toString());
		custDbo.load();
		
		StringBuilder custInfoString = new StringBuilder();
		custInfoString.append(custString);
		custInfoString.append(" and cont_cd in ('" + TFBConstants.MKTDM_CONT_CD_MOBILE_1 + "','"
				+ TFBConstants.MKTDM_CONT_CD_MOBILE_2 + "')");
		
		DPFTLogger.info(this, "Table select where = " + custInfoString.toString());
		//MKT_CUST_CONT
		MKTDMCustomerContactDboSet custInfo = (MKTDMCustomerContactDboSet) this.getDBConnector().getDboSet(DPFTEngine.getSystemProperties("mktdb.tbl.cust.cont"), custInfoString.toString());
		custInfo.load();
		
		
		String target_tbl = mSet.getDbo(0).getString("target_tbl");
		String qString = TFBUtil.buildLZQueryString(timestamp, chal_name);
		DPFTOutboundDboSet oSet = (DPFTOutboundDboSet) this.getDBConnector().getDboSet(target_tbl, qString);
		if(oSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oSet.deleteAll();
		}
		
		String[] t_code_list = TFBUtil.generateSEQ(TFBConstants.SFA_LZ_TCODE_SEQ_ID, dLzSet.count());
		
		/*set Treatment Code for CSR_LZ_TM*/
		for(int i = 0; i < dLzSet.count(); i++){
			DPFTDbo dLz = dLzSet.getDbo(i);
			dLz.setValue("treatment_code", t_code_list[i]); 
		}
		
		/*add record to outbound data table*/		
		HashMap<String, String> map = mSet.getTargetColumnMapping();
		for(int i = 0; i < dLzSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oSet.add();
			new_dbo.setValue("timestamp", timestamp);
			new_dbo.setValue("chal_name", chal_name);
			
			new_dbo.setValue("treatment_code", dLzSet.getDbo(i).getString("treatment_code"));
			new_dbo.setValue("cell_code", timestamp.substring(GlobalConstants.DFPT_DATE_FORMAT.length()));
			new_dbo.setValue("offr_effectivedate", timestamp.substring(0, GlobalConstants.DFPT_DATE_FORMAT.length()));
			new_dbo.setValue("resv1", custDbo.getCustName(dLzSet.getDbo(i).getString("customer_id")));
			
			String mobile = custInfo.getMobileByBizType(dLzSet.getDbo(i).getString("customer_id"), TFBConstants.MKTDM_CONT_BIZTYPE_CC);
			if(mobile == null){
				mobile = custInfo.getMobileByBizType(dLzSet.getDbo(i).getString("customer_id"), TFBConstants.MKTDM_CONT_BIZTYPE_BNK);
			}
			
			new_dbo.setValue("resv2", mobile);
			
			for(String target_col: map.keySet()){
				new_dbo.setValue(target_col, getSourceValue(dLzSet.getDbo(i), map.get(target_col)));
			}
			new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			setMoreValue(new_dbo, dLzSet.getDbo(i));
		}
		int quantity = oSet.count();
		
		//if empty data, add dummy data for output file
		if(quantity == 0){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oSet.add();
			new_dbo.setValue("timestamp", timestamp);
			new_dbo.setValue("chal_name", chal_name);
			new_dbo.setValue("cell_code", timestamp.substring(GlobalConstants.DFPT_DATE_FORMAT.length()));
			new_dbo.setValue("offr_effectivedate", timestamp.substring(0, GlobalConstants.DFPT_DATE_FORMAT.length()));
			for(String target_col: map.keySet()){
				new_dbo.setValue(target_col, getSourceValue(null, map.get(target_col)));
			}
			new_dbo.setValue("process_status", GlobalConstants.O_DATA_DUMMY);
		}
		
		oSet.save(false);
		oSet.close();
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlForLZRecord(this.getDBConnector(), chal_name, timestamp, target_tbl, quantity);
	}
	
	private Object getSourceValue(DPFTDbo dbo, String source_col) {
		if(source_col.indexOf("{") == 0 
				&& source_col.indexOf("}") == (source_col.length() - 1))
			return source_col.substring(1, source_col.length() - 1);
		if(dbo == null)
			return null;
		return dbo.getString(source_col);
	}
	
	@Override
	protected void setMoreValue(DPFTOutboundDbo new_dbo, DPFTDbo dbo) {
		// TODO Auto-generated method stub
		
	}

}
