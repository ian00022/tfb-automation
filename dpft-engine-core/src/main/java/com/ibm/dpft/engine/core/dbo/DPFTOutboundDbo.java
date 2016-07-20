package com.ibm.dpft.engine.core.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.util.DPFTLogger;

public class DPFTOutboundDbo extends DPFTDbo {

	public DPFTOutboundDbo(String dboname, HashMap<String, Object> data, DPFTDboSet thisSet) {
		super(dboname, data, thisSet);
		// TODO Auto-generated constructor stub
	}

	public void validateWithGateKeeper(HashMap<String, Boolean> gkresult) {
		// TODO Auto-generated method stub
		String cust_id = this.getString("customer_id");
		String treatment_code = this.getString("treatment_code");
		String key = cust_id + treatment_code;
		if(gkresult.get(key) != null){
			return;
		}
		this.setValue("process_status", GlobalConstants.O_DATA_GK_EXCLUDE);
		DPFTLogger.debug(this, "Customer : " + cust_id + ", treatment code:" + treatment_code + " Excluded by GK");
	}

}
